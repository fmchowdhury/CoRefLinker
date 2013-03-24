package Knowledgebase;
import java.util.ArrayList;

import Objects.NPMention;
import Utility.FileUtility;
import Utility.TextUtility;

public class UmlsMetaThesaurus {

	public static String metaMapMatches = "kb/metamap.out";
	
	static ArrayList<String> listOfMentionNames = new ArrayList<String>();
	static ArrayList<String> listOfMentionTypes = new ArrayList<String>();
	static ArrayList<ArrayList<String[]>> listOfUmlsConceptMatches = new ArrayList<ArrayList<String[]>>();
	
	/*
	public static void main ( String[] args ) throws IOException {
		
		File[] fileNames = FileUtility.getAllFilesFromDirectory("metamap");
		StringBuilder sb = new StringBuilder();
		
		ArrayList<String> listOfConcepts = new ArrayList<String>();
		ArrayList<String> listOfTypes = new ArrayList<String>();
		boolean conceptExist = true;
		ArrayList<ArrayList<String>> listOfConceptsMatches = new ArrayList<ArrayList<String>>();
		
		for ( int i=0; i<fileNames.length; i++ ) {
	
			ArrayList<String> listOfLines = FileUtility.readNonEmptyFileLines(fileNames[i].getAbsolutePath());
			
			// find the distinct concepts and their matches
			for ( int k=0; k<listOfLines.size()-1; k++ ) {
				if ( listOfLines.get(k).startsWith("c=\"") && !listOfLines.get(k+1).startsWith("c=\"") ) {
					
					int x = listOfLines.get(k).indexOf("\"", 4);
					String type = listOfLines.get(k).substring( listOfLines.get(k).indexOf("\"", x+1)+1, listOfLines.get(k).lastIndexOf("\""));
					String concept = (TextUtility.removeDeterminerAndPersonalPronounAndTitle(listOfLines.get(k).substring( 3, x)) + "\t" + type).toLowerCase();
					
					// if the current concept is not added already
					if ( !(conceptExist=listOfConcepts.contains(concept)) ) {
						listOfConcepts.add(concept);
						listOfTypes.add(type);
						listOfConceptsMatches.add(new ArrayList<String>());
					}
				}
				else if ( !listOfLines.get(k).startsWith("c=\"") && !conceptExist )
					listOfConceptsMatches.get(listOfConcepts.size()-1).add( listOfLines.get(k));
			}
			
			if ( !conceptExist && listOfLines.size() > 1 && !listOfLines.get(listOfLines.size()-1).startsWith("c=\"") )
				listOfConceptsMatches.get(listOfConcepts.size()-1).add( listOfLines.get(listOfLines.size()-1));
		}
		
		// replace concept name with matched string
		for ( int k=0; k<listOfConcepts.size(); k++ ) {
			String str = "", w = "";
			for ( int cui=0; cui<listOfConceptsMatches.get(k).size(); cui++ ) {
				w = listOfConceptsMatches.get(k).get(cui).split("\\|")[6].split("-tx-[0-9]+-")[1].replaceAll("[\"\\]]", "").toLowerCase();
				if ( !str.equalsIgnoreCase(w) ) {
					// for the 1st match
					if ( str.isEmpty() )
						str = w;
					// for any subsequent match where matched string differ from the previous match
					else {
						str = "";
						break;
					}
				}
			}
			
			if ( !str.isEmpty() ) {
				// if the refined concept name already exists 
				if ( listOfConcepts.indexOf(str + "\t" + listOfTypes.get(k)) < k ) {
					listOfConcepts.remove(k);
					listOfTypes.remove(k);
					listOfConceptsMatches.remove(k);
					k--;
				}
				else
					listOfConcepts.set(k, str + "\t" + listOfTypes.get(k));
			}
		}
		
		// sort in ascending order
		for ( int i=0; i<listOfConcepts.size()-1; i++ ) {
			for ( int k=i+1; k<listOfConcepts.size(); k++ ) {
				
				if ( listOfConcepts.get(i).length() > listOfConcepts.get(k).length() ) {
					String x = listOfConcepts.get(i);
					listOfConcepts.set(i, listOfConcepts.get(k));
					listOfConcepts.set(k, x);
					
					ArrayList<String> tmp = listOfConceptsMatches.get(i);
					listOfConceptsMatches.set(i, listOfConceptsMatches.get(k));
					listOfConceptsMatches.set(k, tmp);
					
				}
			}
		}
		
		for ( int k=0; k<listOfConcepts.size(); k++ ) {
			// add to the string builder for writing
			sb.append( listOfConcepts.get(k)).append("\n");
			for ( int m=0; m<listOfConceptsMatches.get(k).size(); m++ )
				sb.append( listOfConceptsMatches.get(k).get(m)).append("\n");
			sb.append("\n");
		}
		
		FileUtility.writeInFile( metaMapMatches, sb.toString().trim(), false);
	}
	//*/
	
	/**
	 * 
	 */
	public UmlsMetaThesaurus() {
		
		if ( listOfUmlsConceptMatches.isEmpty() ) {
			listOfMentionNames = new ArrayList<String>();
			listOfMentionTypes = new ArrayList<String>();
			listOfUmlsConceptMatches = new ArrayList<ArrayList<String[]>>();
			
			ArrayList<String> listOfLines = FileUtility.readFileLines(metaMapMatches);
			
			for ( int k=0; k<listOfLines.size(); k++ ) {
				String[] str = listOfLines.get(k).split("\t");
				listOfMentionNames.add( " " + str[0].toLowerCase() + " ");
				listOfMentionTypes.add(str[1]);
				listOfUmlsConceptMatches.add(new ArrayList<String[]>());
				k++;
				
				while ( k<listOfLines.size() && !listOfLines.get(k).trim().isEmpty() ) {
					listOfUmlsConceptMatches.get(listOfMentionNames.size()-1).add(listOfLines.get(k).split("\\|"));
					k++;
				}
			}
		}
	}
	
	
	/**
	 * 
	 * @param ant
	 * @param anph
	 * @return
	 */
	public String hasCommonUmlsCUIs( NPMention ant, NPMention anph ) {
		
		ArrayList<String> cuisForAnt = new ArrayList<String>();
		ArrayList<String> cuisForAnph = new ArrayList<String>();
		
		String antName = " " + TextUtility.removeDeterminerAndPersonalPronounAndTitle(ant.name).toLowerCase() + " ";
		String anphName = " " + TextUtility.removeDeterminerAndPersonalPronounAndTitle(anph.name).toLowerCase() + " ";
		
		for ( int i=0; i<listOfMentionNames.size() && (cuisForAnph.isEmpty() || cuisForAnt.isEmpty()); i++ ) {
			if ( cuisForAnt.isEmpty() &&
					listOfMentionNames.get(i).contains(antName) && listOfMentionTypes.get(i).equalsIgnoreCase(ant.type) ) {
				for ( int v=0; v<listOfUmlsConceptMatches.get(i).size(); v++ )
					cuisForAnt.add(listOfUmlsConceptMatches.get(i).get(v)[4]);
			}
			else if ( cuisForAnph.isEmpty() &&
					listOfMentionNames.get(i).contains(anphName) && listOfMentionTypes.get(i).equalsIgnoreCase(anph.type) ) {
				for ( int v=0; v<listOfUmlsConceptMatches.get(i).size(); v++ )
					cuisForAnph.add(listOfUmlsConceptMatches.get(i).get(v)[4]);
			}			
		}
		
		
		if ( cuisForAnph.isEmpty() || cuisForAnt.isEmpty() ) {
			for ( int i=listOfMentionNames.size()-1; i>=0 && (cuisForAnph.isEmpty() || cuisForAnt.isEmpty()); i-- ) {
				if ( cuisForAnt.isEmpty() &&
						antName.contains(listOfMentionNames.get(i)) && listOfMentionTypes.get(i).equalsIgnoreCase(ant.type) ) {
					for ( int v=0; v<listOfUmlsConceptMatches.get(i).size(); v++ )
						cuisForAnt.add(listOfUmlsConceptMatches.get(i).get(v)[4]);
				}
				else if ( cuisForAnph.isEmpty() &&
						anphName.contains(listOfMentionNames.get(i)) && listOfMentionTypes.get(i).equalsIgnoreCase(anph.type) ) {
					for ( int v=0; v<listOfUmlsConceptMatches.get(i).size(); v++ )
						cuisForAnph.add(listOfUmlsConceptMatches.get(i).get(v)[4]);
				}			
			}
		}
		
		if ( cuisForAnph.isEmpty() || cuisForAnt.isEmpty() )
			return "";
			
		for ( int i=0; i<cuisForAnt.size(); i++ )
			if ( cuisForAnph.contains(cuisForAnt.get(i)) )
				return "Y";
					
		return "N";
	}
	
	
	
	public boolean hasCommonUmlsConceptForFullNameMatch( NPMention ant, NPMention anph ) {
		
		int mx = listOfMentionNames.indexOf(ant.name.toString());
		int my = listOfMentionNames.indexOf(anph.name.toString());
		
		if ( mx < 0 || my < 0 )
			return false;
		
		return hasCommonUmlsConceptMatch(mx, my, ant, anph);
	}
	
	/**
	 * 	
	 * @param mx
	 * @param my
	 * @return
	 */
	private boolean hasCommonUmlsConceptMatch( int mx, int my, NPMention ant, NPMention anph ) {
		
		/*
		for ( int k=0; k<listOfUmlsConceptMatches.get(mx).size(); k++ ) {
			for ( int z=0; z<listOfUmlsConceptMatches.get(my).size(); z++ ) {
				//System.out.println(mx + " " + k + "    " + my + " " + z);
				boolean found = false;
				
				if ( listOfUmlsConceptMatches.get(mx).get(k)[3].equalsIgnoreCase(listOfUmlsConceptMatches.get(my).get(z)[3]) ) 
					found = true;
				else if ( listOfUmlsConceptMatches.get(mx).get(k)[6].equalsIgnoreCase(listOfUmlsConceptMatches.get(my).get(z)[6]) )
					found = true;
				
				if ( found ) {
					System.out.println(listOfMentionNames.get(mx) + "     " + listOfMentionNames.get(my));
					return true;
				}
			}
		}
		*/
		boolean found = false;
		
		for ( int k=0; k<listOfUmlsConceptMatches.get(mx).size() && !found; k++ )
			if ( listOfUmlsConceptMatches.get(mx).get(k)[3].equalsIgnoreCase(anph.name) )
				found = true;
			
		for ( int z=0; z<listOfUmlsConceptMatches.get(my).size() && !found; z++ )
			if ( listOfUmlsConceptMatches.get(my).get(z)[3].equalsIgnoreCase(ant.name) ) 
				found = true;
		
		if ( found )
			System.out.println(ant.name + "     " + anph.name);
		
		return found;
	}
}
