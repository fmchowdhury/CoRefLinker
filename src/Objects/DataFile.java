package Objects;

import java.io.File;
import java.util.ArrayList;

import Structures.DependencyGraph;
import Structures.Sentence;
import Utility.DataStrucUtility;
import Utility.FileUtility;
import Utility.ParseOutputUtility;
import Utility.TextUtility;

public class DataFile {

	/* 
	 * TODO: for any pronoun type male/female, create train/test instance of maximum 3
	 * from the closest antecedents which must be persons, and will not mismatch in gender and number.
	 */
	
	public String fileId = "";
	public boolean isClinicalText = true; 
	public ArrayList<Sentence> listOfSentences = new ArrayList<Sentence>();
	public ArrayList<Pair> listOfPairs = new ArrayList<Pair>();
	public ArrayList<Chain> listOfChains = new ArrayList<Chain>();
	public NPMention[] listOfMentions = new NPMention[0];
	public int[] arrOfNoOfCandidatesForMentions = new int[0];
	
	/**
	 * 
	 * @param senFileName
	 * @param npMentionFileName
	 * @param chainFileName
	 * @param pairFileName
	 * @param parsedDataFileName
	 * @throws Exception 
	 */
	public void readAllSentences( String senFileName, String npMentionFileName, String chainFileName,
			String pairFileName, String parsedDataFileName, boolean restoreOriginalTokensFromText ) throws Exception {
		
		fileId = senFileName;
		readAllSentences(senFileName);
		
		if ( !TextUtility.isEmptyString(npMentionFileName) )
			readAllMentionNames(npMentionFileName, restoreOriginalTokensFromText);
		
		if ( !TextUtility.isEmptyString(pairFileName) )
			readAllPairs(pairFileName);
		
		if ( !TextUtility.isEmptyString(chainFileName) )
			readAllChains(chainFileName);
		
		if ( !TextUtility.isEmptyString(parsedDataFileName) ) {			
			listOfSentences = new ParseOutputUtility().readParsedData(parsedDataFileName, true, true, true, listOfSentences);
			detectTokIndxByParserForMentions();
		}
	}
	
	/**
	 * 
	 * @param senFileName
	 */
	private void readAllSentences( String senFileName ) {
		
		listOfSentences = new ArrayList<Sentence>();		
		ArrayList<String> listOfLines = FileUtility.readFileLines(senFileName);
		
		for ( int i=0; i<listOfLines.size(); i++ ) {
			Sentence curSen = new Sentence(listOfLines.get(i));
			curSen.senIndx = i;
			listOfSentences.add(curSen);
		}
		/*
		String str = "";
		for ( int i=0; i<listOfLines.size()-1; i++ ) {
			
			if ( listOfSentences.get(i).text.matches("[A-Z].*") 
					&& !listOfSentences.get(i).text.matches("[A-Z][A-Z]+\\s.*")
					&& !listOfSentences.get(i).text.matches("[0-9].*\\.")
					&& !listOfSentences.get(i).text.trim().endsWith(":")
					&& !listOfSentences.get(i).text.trim().endsWith(".")
					&& !listOfSentences.get(i+1).text.matches("[A-Z].*")
					&& !listOfSentences.get(i+1).text.matches("[0-9].*\\.")
					&& listOfSentences.get(i+1).text.trim().endsWith(" .") ) {
				str = str + (i+1) + ": " + listOfSentences.get(i).text + "\n";
				str = str + (i+2) + ": " + listOfSentences.get(i+1).text + "\n\n";
			}
		}
		
		if ( !str.isEmpty() ) {
			str = fileId + "\n" + str + "======================================================================";
			System.out.println(str);
		}
			*/
	}
	

	public void writeAllSentences( String outSenFileName, String conFileName ) {
		
		// TODO: 
	}
	
	
	/**
	 * 
	 * @param fileName
	 */
	private void readAllChains ( String fileName ) {
		
		listOfChains = new ArrayList<Chain>();
		ArrayList<String> listOfLines = FileUtility.readNonEmptyFileLines(fileName);
		
		if ( fileId.contains("Report132") )
			fileId.trim();
	
		
		for ( int i=0; i<listOfLines.size(); i++ ) {
			if ( !listOfLines.get(i).trim().isEmpty() ) {
				Chain curChain = new Chain();
				String[] tmp = listOfLines.get(i).trim().split("\\|\\|");
				curChain.corefType = NPMention.getTypeFromString(tmp[tmp.length-1]);
					
				for ( int m=0; m<tmp.length-1; m++ ) {
					NPMention mn = checkBoundariesAndTextOfMention(new NPMention(tmp[m]), false);
					if ( mn.sTokIndxBySpace > mn.eTokIndxBySpace )
						continue;
					
					curChain.listOfMentionIndexes.add(getMentionIndex(mn));
				}
				
				if ( curChain.listOfMentionIndexes.size() < 2 )
					continue;
				
				curChain.listOfMentionIndexes = DataStrucUtility.sort(curChain.listOfMentionIndexes);
				listOfChains.add(curChain);
			}
		}
	}
	
	/**
	 * 
	 * @param mn
	 * @return
	 */
	public int getMentionIndex ( NPMention mn ) {
		for ( int i=0; i<listOfMentions.length; i++ )
			if ( listOfMentions[i].isSameMention(mn) )
				return i;
		
		return -1;
	}
	
	/**
	 * 
	 * @param fileName
	 */
	public void readAllPairs ( String fileName ) {
		
		File f = new File(fileName);
	    if (!f.exists())
	    	return;
		
		listOfPairs = new ArrayList<Pair>();
		ArrayList<String> listOfLines = FileUtility.readNonEmptyFileLines(fileName);
		
		for ( int i=0; i<listOfLines.size(); i++ ) {
			if ( !listOfLines.get(i).trim().isEmpty() ) {
				Pair curPair = new Pair();
				String[] tmp = listOfLines.get(i).trim().split("\\|\\|");
	
				curPair.corefType = NPMention.getTypeFromString(tmp[1]);
				
				NPMention mn = checkBoundariesAndTextOfMention(new NPMention(tmp[0]), false);
				curPair.antecedentMenitonIndex = getMentionIndex(mn);
				
				if ( mn.sTokIndxBySpace > mn.eTokIndxBySpace )
					continue;
				
				mn = checkBoundariesAndTextOfMention(new NPMention(tmp[2]), false);
				curPair.anaphoraMenitonIndex = getMentionIndex(mn);
				
				if ( tmp.length > 3 )
					curPair.probabScore = Double.valueOf(tmp[3]);
				
				if ( tmp.length > 4 )
					curPair.polarity = Boolean.valueOf(tmp[4]);
				
				if ( mn.sTokIndxBySpace > mn.eTokIndxBySpace )
					continue;
				
				listOfPairs.add(curPair);
			}
		}
	}
	
	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public void readAllMentionNames ( String fileName, boolean restoreOriginalTokensFromText ) {
	
		if ( !new File(fileName).exists() )
			fileName = fileName + "cept";
		
		ArrayList<String> listOfLines = FileUtility.readNonEmptyFileLines(fileName);
		ArrayList<NPMention> tmpListOfMentions = new ArrayList<NPMention>();
		
		// c="dilated left intrarenal collecting system and proximal ureter" 83:32 83:39||t="problem"
		for ( int c=0; c<listOfLines.size(); c++ ) {
			if ( !listOfLines.get(c).trim().isEmpty() ) {
				String[] tmp = listOfLines.get(c).trim().split("\\|\\|");
			
				NPMention mn = new NPMention(tmp[0]);
				mn.setNPMentionType(NPMention.getTypeFromString(tmp[1]));
				
				if ( mn.senIndx != mn.eSenIndx )
					System.err.println(TextUtility.now() + " Mention spans muplitple sentence: " + mn.toStringFull());
					
				if ( mn.sTokIndxBySpace > mn.eTokIndxBySpace ) {
					//System.out.println("--- Wrong boundary annotation detected in " + fileName + " sen: " + mn.senIndx
						//	+ " mention: " + mn.name);
				}
				else {
					mn = checkBoundariesAndTextOfMention(mn, restoreOriginalTokensFromText);
					tmpListOfMentions.add(mn);
				}
			}
		}
		
		
		// converting from list to array
		listOfMentions = (NPMention []) tmpListOfMentions.toArray (new NPMention [tmpListOfMentions.size()]);
		listOfMentions = DataFile.sortMentionsBySenIndxTokIndx(listOfMentions);
	}
	
	
	/**
	 * 
	 * @param mn
	 * @param restoreOriginalTokensFromText
	 * @return
	 */
	private NPMention checkBoundariesAndTextOfMention ( NPMention mn, boolean restoreOriginalTokensFromText ) {
		
		String tmpName = "";
		
		try {					
			if ( mn.senIndx == mn.eSenIndx )
				for ( int i=mn.sTokIndxBySpace; i<=mn.eTokIndxBySpace; i++ )
					tmpName = tmpName + listOfSentences.get(mn.senIndx).arrWordBySpace[i] + " ";
		}
		catch (Exception ex) {
			tmpName = "";
						
			int indx = listOfSentences.get(mn.senIndx).text.toLowerCase().indexOf(mn.name.toLowerCase());
			
			if ( indx >= 0 ) {
				indx = listOfSentences.get(mn.senIndx).text.substring(0, indx).split("\\s+").length - 1;
				mn.eTokIndxBySpace = mn.eTokIndxBySpace - (mn.sTokIndxBySpace - indx);
				mn.sTokIndxBySpace = indx;
			}
			
			for ( int i=mn.sTokIndxBySpace; i<=mn.eTokIndxBySpace; i++ )
					tmpName = tmpName + listOfSentences.get(mn.senIndx).arrWordBySpace[i] + " ";
			
			System.err.println(TextUtility.now() + " Fixed wrong boundary annotation detected in " + fileId + " sen: " + mn.senIndx	+ " mention: " + mn.name);
		}
		
		if ( !tmpName.isEmpty() && restoreOriginalTokensFromText )
			mn.name = tmpName.trim();
		
		return mn;
	}
	
	/**
	 * 
	 * @param listOfMentions
	 * @return
	 */
	private static NPMention[] sortMentionsBySenIndxTokIndx ( NPMention[] listOfMentions ) {
		
		for ( int i=0; i<listOfMentions.length-1; i++ ) {
			for ( int k=i+1; k<listOfMentions.length; k++ ) {
				
				if ( listOfMentions[i].senIndx > listOfMentions[k].senIndx ||
						( listOfMentions[i].senIndx == listOfMentions[k].senIndx
						&& listOfMentions[i].sTokIndxBySpace > listOfMentions[k].sTokIndxBySpace )
					) {
					NPMention mn = listOfMentions[i];
					listOfMentions[i] = listOfMentions[k];
					listOfMentions[k] = mn;
				}
			}
		}
		
		return listOfMentions;
	}

	
	/**
	 * 
	 */
	private void detectTokIndxByParserForMentions () {
				
	//	System.out.println("Now " + fileId);
		
		int lastMentionChecked = 0;
		
		/* 
		 * TODO: Parser produces distorted output for the following sentence. Have to make this method more robust to adapt to such output.
		 * 		Tissue from mesenteric region (No.1---1.1 x 0.5 x 0.3 cm, No.2---1.5 x 0.5 x 0.3 cm), 51.5 cm of cecum and portion of terminal ileum
		 */
		for ( int s=0; s<listOfSentences.size(); s++ ) {
	
		//	if ( fileId.contains("clinical-221.txt") && s==51 ) 
				//System.out.println("ddd");
			
			Sentence tmpSen = listOfSentences.get(s);
			ArrayList<ArrayList<Integer>> tmpListTokGropus = null;
			tmpListTokGropus = new ArrayList<ArrayList<Integer>>();
			tmpListTokGropus.clear();
			int pt=0;
			
			// map parsed tokens with tokens by space
			for ( int ost=0; ost<tmpSen.arrWordBySpace.length; ost++ ) {
				tmpListTokGropus.add(new ArrayList<Integer>());
				String str = tmpSen.arrWordBySpace[ost];
				int indxOfParsedTok = 0;
				for ( ; pt<tmpSen.arrWordAndPosByParser.length; pt++ ) {
										
					// if there is a perfect match
					if ( (indxOfParsedTok=str.indexOf(tmpSen.arrWordAndPosByParser[pt][0])) == 0 
							&& str.length() == tmpSen.arrWordAndPosByParser[pt][0].length() ) {
						tmpListTokGropus.get(ost).add(pt);
						pt++;
						break;
					}
					
					// if the parsed token has spaces e.g. "7 1/2"
					if ( indxOfParsedTok == 0   
							&& str.length() < tmpSen.arrWordAndPosByParser[pt][0].length() ) {
						tmpListTokGropus.get(ost).add(pt);
						ost++;
						tmpListTokGropus.add(new ArrayList<Integer>());
						tmpListTokGropus.get(ost).add(pt);
						pt++;
						break;
					}
					
					if ( indxOfParsedTok == 0 ) {
						tmpListTokGropus.get(ost).add(pt);
						str = str.substring(tmpSen.arrWordAndPosByParser[pt][0].length());
					}
				}
			}
			
			// if there are remaining tokens even after iteration of all tokens by space  
			for ( ; pt<tmpSen.arrWordAndPosByParser.length; pt++ ) {
				tmpListTokGropus.get(tmpSen.arrWordBySpace.length-1).add(pt);
			}
			
			for ( ; lastMentionChecked<listOfMentions.length; lastMentionChecked++ ) {
					
				if ( listOfMentions[lastMentionChecked].senIndx == tmpSen.senIndx ) {
					for ( int tok=listOfMentions[lastMentionChecked].sTokIndxBySpace; tok<=listOfMentions[lastMentionChecked].eTokIndxBySpace; tok++ )
						if ( listOfMentions[lastMentionChecked].senIndx == listOfMentions[lastMentionChecked].eSenIndx )
							listOfMentions[lastMentionChecked].listOfTokenIndxByParser.addAll(tmpListTokGropus.get(tok));
				}
				else
					break;
			}
				
		}
		
		// set head words and other related properties of the mentions
		for ( int m=0; m<listOfMentions.length; m++ ) {
			// System.out.println(fileId + " " +  m + " " + listOfMentions[m].toString());			
		if ( fileId.contains("232") && m==1 )
			fileId.trim();
			 
			if ( listOfMentions[m].senIndx == listOfMentions[m].eSenIndx && listOfMentions[m].name.matches(".*[A-Za-z].*") ) {
				DependencyGraph dg = listOfSentences.get(listOfMentions[m].senIndx).depGraph;
				if ( dg != null && dg.allNodesByWordIndex != null
						&& dg.allNodesByWordIndex.length > 0 )
					listOfMentions[m].setHeadWord(dg, listOfSentences.get(listOfMentions[m].senIndx).psgTree);
			}
		}
	}
	
	
	/**
	 * 
	 * @param df
	 * @param ant
	 * @param anph
	 * @return
	 */
	public boolean containsPair ( NPMention ant, NPMention anph ) {
		
		ArrayList<Integer> listOfMentionIndexes = new ArrayList<Integer>();
		for ( int m=0; m<listOfMentions.length && listOfMentionIndexes.size() < 2; m++ )
			if ( listOfMentions[m].toStringFull().equalsIgnoreCase(ant.toStringFull()) ||
					listOfMentions[m].toStringFull().equalsIgnoreCase(anph.toStringFull()) )
				listOfMentionIndexes.add(m);
					
					
		for ( int c=0; c<listOfChains.size(); c++ )
			if ( listOfChains.get(c).listOfMentionIndexes.containsAll(listOfMentionIndexes) )
				return true;
		
		return false;
	}
	
	/**
	 * 
	 * @param df
	 * @param antIndex
	 * @param anphIndex
	 * @return
	 */
	public boolean containsPair ( int antIndex, int anphIndex ) {
		
		ArrayList<Integer> listOfMentionIndexes = new ArrayList<Integer>();

		listOfMentionIndexes.add(antIndex);
		listOfMentionIndexes.add(anphIndex);
					
		for ( int c=0; c<listOfChains.size(); c++ )
			if ( listOfChains.get(c).listOfMentionIndexes.containsAll(listOfMentionIndexes) )
				return true;
		
		return false;
	}
	
}
