package Structures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import Utility.*;

public class Sentence {

	public String senID = "", absID = "", text = "", tokenWithPosByParser = "";
	public int  senIndx = -1;
	
	public ArrayList<Relation> listRels = new ArrayList<Relation>();
	public ArrayList<Entity> listOfEntities = new ArrayList<Entity>();
	
	public int[][] arrBoundariesByWordIndexes = new int[0][];
	public String[][] arrWordAndPosByParser = new String[0][];
	public String[] arrLemmasByParser = new String[0], arrWordBySpace = new String[0];
	
	public PhraseStructureTree psgTree = null; 
	public DependencyGraph depGraph = null;
	//private DependencyTree depTree = null;
	
	/**
	 * 
	 */
	public Sentence () {}
	
	/**
	 * 
	 * @param str
	 */
	public Sentence ( String str ) {
		
		this.text = str.trim();
		String[] tmp = this.text.split("\\s+");
		this.arrWordBySpace = new String[tmp.length];
		
		for ( int i=0; i<tmp.length; i++ )
			this.arrWordBySpace[i] = tmp[i];
	}

		
	/**
	 * 
	 */
	public void detectBoundariesAndLemmas ( ){
		this.arrWordAndPosByParser = ParseOutputUtility.checkTokensInOriginalSentence(arrWordAndPosByParser, this.text);
		
		arrBoundariesByWordIndexes = new int[arrWordAndPosByParser.length][2];
		arrLemmasByParser = new String[arrWordAndPosByParser.length];
		
		int sCharIndex = 0, eCharIndex = 0;
		
		for ( int i=0; i<arrWordAndPosByParser.length; i++ ){
			
			eCharIndex = sCharIndex + arrWordAndPosByParser[i][0].length();
						
			arrBoundariesByWordIndexes[i] = new int[]{ sCharIndex, eCharIndex - 1 };
			sCharIndex = eCharIndex;

			arrLemmasByParser[i] = SyntacticParser.getLemma( arrWordAndPosByParser[i][0], arrWordAndPosByParser[i][1]);
			if ( !arrLemmasByParser[i].equals("-") )
				arrLemmasByParser[i] = arrLemmasByParser[i].replaceAll("-", "");
		}
	}
	
	/**
	 * 
	 * @param tokenWithPos
	 */
	public void detectBoundariesAndLemmas ( String tokenWithPosByParser ){
		
		this.tokenWithPosByParser = tokenWithPosByParser;
		detectBoundariesAndLemmas();
	}
	
	/**
	 * 
	 * @param isBioCreative2Format
	 * @return
	 */
	public String toString( boolean isBioCreative2Format ) {
		
		if ( isBioCreative2Format )
			return (this.senID + " " + this.text + "\n");
			
		StringBuilder sbTemp = new StringBuilder();
		
		// write abstract id, sentence id and sentence		
		sbTemp.append( "Abstract Id: " + this.absID + "\n");			
		sbTemp.append( "Sentence Id: " + this.senID + "\n");		
		sbTemp.append(this.text + "\n\n");
	
		// write entities
		if ( this.listOfEntities.size() < 1 )
			// no entity
			sbTemp.append("\n\n");
		else {				
			for ( int e=0; e<this.listOfEntities.size(); e++ ) {
				sbTemp.append( this.listOfEntities.get(e).id + " "
						+ this.listOfEntities.get(e).startIndex + " " + this.listOfEntities.get(e).endIndex + "\n");
				sbTemp.append( this.listOfEntities.get(e).type + "\n");
				sbTemp.append( this.listOfEntities.get(e).name + "\n");
			}
			
			sbTemp.append("\n");
		}
		
		// write relations
		if ( this.listRels.size() < 1 )
			// no entity
			sbTemp.append("\n\n");
		else {				
			for ( int r=0; r<this.listRels.size(); r++ )
				sbTemp.append( this.listRels.get(r).toString() + "\n");
			sbTemp.append("\n");
		}
		
		return sbTemp.toString();
	}
	
	/**
	 * 
	 * @param fullDataFileName
	 * @param isBioCreative2Format
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<Sentence> readFullData ( String fullDataFileName, boolean isBioCreative2Format ) throws Exception {
		return readFullData(fullDataFileName, "", isBioCreative2Format);
	}
	
	/**
	 * 	
	 * @param fullDataFileName
	 * @param entPairFileName
	 * @param isBioCreative2Format
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<Sentence> readFullData ( String fullDataFileName, String entPairFileName, boolean isBioCreative2Format ) throws Exception {
		
		String line = "";
		BufferedReader inputFullData = new BufferedReader(new FileReader(new File(fullDataFileName)));
		ArrayList<Sentence> listSentence = new ArrayList<Sentence>();
					
		while (( line = inputFullData.readLine()) != null){
		
			Sentence objSen = new Sentence();
						
			/**
			 * ---########--- Reading full data
			 */				
			// read abstract id, sentence id and sentence
			String[] temp = new String[0];
			line = line.trim();
			
			// i.e. each line contains sentence id followed by the sentence and no empty lines
			if ( isBioCreative2Format ) {
				
				if ( line.isEmpty() )
					break;
				
				int spaceCharFirstInd = line.indexOf(" "); 
				objSen.absID = line.substring(0, spaceCharFirstInd);
				objSen.senID = objSen.absID;
				objSen.text = line.substring(spaceCharFirstInd+1);
				continue;
			}
				
			temp = line.split("\\s+");
			objSen.absID = temp[temp.length-1];
			temp = inputFullData.readLine().trim().split("\\s+");
			objSen.senID = temp[temp.length-1];
			objSen.text = inputFullData.readLine().trim().replaceAll("\\s+", " ");
			inputFullData.readLine();
		
			// read entities
			line = inputFullData.readLine().trim();
					
			// no entity
			if ( line.isEmpty() )
				inputFullData.readLine();
			else {
				while ( line != null && !(line = line.trim()).isEmpty() ){
					Entity objEntity = new Entity(line, inputFullData.readLine(), inputFullData.readLine());
					objSen.listOfEntities.add(objEntity);
					line = inputFullData.readLine();
				}
			}
			
			// read relations
			line = inputFullData.readLine().trim();
			
			// no relation
			if ( line.isEmpty() )
				inputFullData.readLine();
			else {
				while ( line != null && !(line = line.trim()).isEmpty() ){
					// avoiding self-interactions of pairs
				//	String[] ss = line.split("\\s+");
					//if ( !ss[1].equals(ss[2]) ) {					
						objSen.listRels.add( new Relation(line));
						line = inputFullData.readLine();
				//	}
				}
			}
			
			listSentence.add(objSen);
			

			if ( entPairFileName != null && !entPairFileName.isEmpty() ) {
				for ( int i=0; i < objSen.listOfEntities.size()-1; i++ ){
					for ( int j=i+1; j < objSen.listOfEntities.size(); j++ ){
						FileUtility.writeInFile(entPairFileName, objSen.listOfEntities.get(i).id + "\t" + objSen.listOfEntities.get(j).id + "\n", true);
					}
				}
			}
		}
		
		return listSentence;
    }
	
	/**
	 * 
	 * @param e1
	 * @param e2
	 * @return
	 */
	public boolean getPolarityOfRelation ( Entity e1, Entity e2 ) {
		
		for ( int r=0; r<listRels.size(); r++ ) {
			if ( ( listRels.get(r).arg1.equals(e1.id) && listRels.get(r).arg2.equals(e2.id) )
					|| ( listRels.get(r).arg1.equals(e2.id) && listRels.get(r).arg2.equals(e1.id) ) )
				return listRels.get(r).isPositive;
		}
		
		return false; 
	}
	
	
	/**
	 * 
	 * @param eId
	 * @return
	 */
	public Entity getEntityById ( String eId ) {
		
		for ( int e=0; e<listOfEntities.size(); e++ ) {
			if ( listOfEntities.get(e).id.equals(eId)  )					
				return listOfEntities.get(e);
		}
		
		return null; 
	}
}