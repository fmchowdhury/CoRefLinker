package CoRef;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import Objects.*;
import Utility.FileUtility;
import Utility.ParseOutputUtility;
import Utility.TextUtility;


/**
 * 
 * @author Md. Faisal Mahbub Chowdhury
 *
 */

public class Preprocessor {

	/**
read each sentence
if there is something like **AGE[in 40s] , then create a list using the line no., 
followed by the token no. where **AGE[ is attached, followed by the token no. wehre ] is attached. 
Then remove **AGE[ and ] from the sentence. Remove them from corresponding concepts, chains and pairs.

save the list.
split the sentence into tokens using space and save them in a list.
parse the sentence. If there is a mismatch among the no. of parsed tokens and the original tokens,
 then identify where such mismatch happened.

do anaphora resolution

correct the token no.

map the tokens to their original tokens.
	 */
	
	
	public static void main ( String[] args ) throws Exception {
		
		
		//String parentDir = "/vol/marot/groups/i2b22011/corpus/tache1/data_for_exp_by_faisal/train/Beth_Partners/appr";
		//String parentDir = "Beth_Partners/appr";
		String parentDir = "/vol/marot/groups/i2b22011/corpus/tache1/data_for_exp_by_faisal/ODIE/train"; // test/i2b2_Beth_Test";
		//String parentDir = "i2b2_1C/train/i2b2_Pittsburgh"; 
			//"i2b2_1C/test/i2b2_Pittsburgh_Test";		
		
		
		args = new String[] {"-anymRm", 
				"-chainF",
				parentDir + "/chains",
				"-conceptF",
				parentDir + "/concepts",
			//	"-pairF",
			//	parentDir + "/pairs",
				"-textF",
				parentDir + "/docs",
				"-outF",
				parentDir + "_preproc" };
		
		boolean removeAnonymisationTags = true, 
				addSenTagsForCharniacParser = false;
		
		String dirConcept = "", dirChain = "", dirPair = "", dirText = "", dirOut = ""; 
		
		for ( int i=0; i<args.length; i++ ) {
			if ( args[i].equals("-anymRm") )
				removeAnonymisationTags = true;
			
			else if ( args[i].equals("-chainF") ) {
				dirChain = args[i+1].trim();
				i++;
			}
			
			else if ( args[i].equals("-conceptF") ) {
				dirConcept = args[i+1].trim();
				i++;
			}

			else if ( args[i].equals("-pairF") ) {
				dirPair = args[i+1].trim();
				i++;
			}

			else if ( args[i].equals("-textF") ) {
				dirText = args[i+1].trim();
				i++;
			}
			
			else if ( args[i].equals("-outF") ) {
				dirOut = args[i+1].trim();
				i++;
			}
		}
		
		if ( removeAnonymisationTags ) {
				new Preprocessor().prepocessDataForParsing(dirChain, dirConcept, dirPair, dirText, dirOut, addSenTagsForCharniacParser);
		}
		
	/*	String bllip_dir="/media/Study/installed_programs/bllip-parser-2011-12-22";
		String inpFile = "/media/Study/test_txt.txt";
		
		String command = bllip_dir + "/first-stage/PARSE/parseIt -l399 -N50 $bllip_dir/biomodel/parser/ " 
				+ inpFile + " | " + bllip_dir + "/second-stage/programs/features/best-parses " +
				" -l " + bllip_dir + "/biomodel/reranker/features.gz " 
				+ bllip_dir + "/biomodel/reranker/weights.gz";
		
		SystemUtility.UnixSystemCall(command, "/media/Study/test_txt.parsed");
		//System.out.println(SystemUtility.UnixSystemCall(command));
		 */
	}
	
	
	
	/**
	 * 
	 * @param dataDirName
	 * @throws IOException
	 */
	public void prepocessDataForParsing ( String dirChain, String dirConcept, 
			String dirPair, String dirText, String dirOut, boolean addSenTagsForCharniacParser ) throws Exception {
	
		String dirChainNew = "", dirConceptNew = "", dirPairNew = "", dirTextNew = "";
		
		if ( !TextUtility.isEmptyString(dirChain) ) {
			dirChainNew = dirOut + dirChain.substring(dirChain.lastIndexOf("/"));
			FileUtility.createDirectory( dirChainNew );
		}
		
		if ( !TextUtility.isEmptyString(dirConcept) ) {
			dirConceptNew = dirOut + dirConcept.substring(dirConcept.lastIndexOf("/"));
			FileUtility.createDirectory( dirConceptNew );
		}
		
		if ( !TextUtility.isEmptyString(dirPair) ) {	
			dirPairNew = dirOut + dirPair.substring(dirChain.lastIndexOf("/"));
			FileUtility.createDirectory( dirPairNew );
		}
		
		if ( !TextUtility.isEmptyString(dirText) ) {
			dirTextNew = dirOut + dirText.substring(dirChain.lastIndexOf("/"));
			FileUtility.createDirectory( dirTextNew );
		}
		
		File[] fileNames = FileUtility.getAllFilesFromDirectory(dirText);
		
		for ( int i=0; i<fileNames.length; i++ ) {
			
			String outSenFileName = fileNames[i].getCanonicalPath().replace( dirText, dirTextNew), 
			outNpMentionFileName = dirConceptNew + "/"+fileNames[i].getName()+".con", 
			outPairFileName = dirPairNew + "/"+fileNames[i].getName()+".pairs", 
			outChainFileName = dirChainNew + "/"+fileNames[i].getName()+".chains";
			
			// remove anonymysation from text file
			removeAnonymysationTags( fileNames[i].getCanonicalPath(),
					outSenFileName, addSenTagsForCharniacParser);
			
			String str = dirConcept+"/"+fileNames[i].getName()+".con";
			// remove anonymysation from mention file
			if ( !TextUtility.isEmptyString(dirConcept) && new File(str).exists() )
				removeAnonymysationTags( str,
						outNpMentionFileName, addSenTagsForCharniacParser);
			
			str = dirChain+"/"+fileNames[i].getName()+".chains";
			// remove anonymysation from chain file
			if ( !TextUtility.isEmptyString(dirChain) && new File(str).exists() )
				removeAnonymysationTags( str,
						outChainFileName, addSenTagsForCharniacParser);
			/*
			str = dirPair+"/"+fileNames[i].getName()+".pairs";
			// remove anonymysation from pair file
			if ( !TextUtility.isEmptyString(dirPair) && new File(str).exists() )
				removeAnonymysationTags( str,
						outPairFileName, false);
			*/
			correctSpellingMistakes(outSenFileName, outNpMentionFileName, outPairFileName, outChainFileName);
			correctBoundaries(outSenFileName, outNpMentionFileName, outPairFileName, outChainFileName);
		}
	}
	
	
	/**
	 * Prepare data for parsing
	 * 
	 * @param senFileName
	 * @throws Exception
	 */
	private void removeAnonymysationTags( String senFileName, String outFileName, 
			boolean addSenTagsForCharniacParser ) throws Exception {
		
		ArrayList<String> listOfSen = FileUtility.readFileLines(senFileName);
		StringBuilder sb = new StringBuilder();
		
		// NOTE: we assume no **TAG includes another **TAG
		for ( int s=0; s<listOfSen.size(); s++ ) {
			String newSen = listOfSen.get(s);
			
			if ( newSen.matches(".*\\*\\*[a-zA-Z]+\\[\\S+.*\\S+\\].*") )  {
				if ( senFileName.contains("-4.txt.con") )
					senFileName.trim();
				
				String[] tmp = newSen.split("\\s+");
				newSen = "";
				boolean isTagBegFound = true;
				
				for ( int tok=0; tok<tmp.length; tok++ ) {
					
					if ( tmp[tok].matches(".*\\*\\*[a-zA-Z]+\\[\\S+.*") ) {
						AnonymysationTags newTag = new AnonymysationTags();
						newTag.senNo = s;
						newTag.tokNo = tok;
						int st = tmp[tok].indexOf("**");
						newTag.tag = tmp[tok].substring(st, tmp[tok].indexOf("[") + 1);
						tmp[tok] = tmp[tok].replace(newTag.tag, "");
						newTag.isAtBeg = true;
						
						isTagBegFound = true;
						if ( (st=tmp[tok].indexOf("]", st+1)) >= 0  )
							tmp[tok] = tmp[tok].substring(0, st) + tmp[tok].substring(st+1);
					}
					else if ( isTagBegFound && tmp[tok].contains("]") ) {
						AnonymysationTags newTag = new AnonymysationTags();
						newTag.senNo = s;
						newTag.tokNo = tok;
						newTag.tag = "]";
						tmp[tok] = tmp[tok].replaceFirst("\\]", "");
						newTag.isAtBeg = false;
						
						isTagBegFound = false;
					}
					
					newSen += " " + tmp[tok];
				}
			}
			
			if ( addSenTagsForCharniacParser )
				sb.append( "<s> " + newSen.trim() + " </s>\n" );
			else
				sb.append( newSen.trim() + "\n" );			
		}
		
		FileUtility.writeInFile( outFileName, sb.toString(), false);
	}
	
	
	/**
	 * 
	 * @param dirParse
	 * @throws IOException 
	 */
	public void createCompleteParseOutput ( String dirParse, String outDir ) throws IOException {
		
		// merge psg and dep parsed data	

		File[] allFiles = FileUtility.getAllFilesFromDirectory(dirParse);
		
		for ( int i=0; i<allFiles.length; i++ ) {
		
			if ( !allFiles[i].getName().contains(".dep") ) {
				ParseOutputUtility.mergePsgAndDepOutput(allFiles[i].getCanonicalPath(), 
						allFiles[i].getCanonicalPath() + ".dep", outDir + "/" + allFiles[i].getName() + ".complete");
			}
		}
	}
	
	/**
	 * 
	 * @param senFileName
	 * @param npMentionFileName
	 * @param pairFileName
	 * @param chainFileName
	 * @throws Exception
	 */
	private void correctSpellingMistakes ( String senFileName, String npMentionFileName, String pairFileName, String chainFileName ) throws Exception {
		
		String[] files = new String[] { senFileName, npMentionFileName, //pairFileName,
				chainFileName};
		
		for ( int f=0; f<files.length; f++ ) {
			ArrayList<String> listOfLines = FileUtility.readFileLines(files[f]);
			StringBuilder sb = new StringBuilder();
			
			for ( int i=0; i<listOfLines.size(); i++ ) {
				String str = listOfLines.get(i).replaceAll("&apos;", "'")
					.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&quot;", "\"")
					.replaceAll(" & ", " and ")
					;
				
				String[] key = new String[] {"mr", "ms", "mrs", "dr", "Mr", "Ms", "Mrs", "Dr"},
					replacement = new String[] {"Mr", "Ms", "Mrs", "Dr", "Mr", "Ms", "Mrs", "Dr"};
				
				for ( int k=0; k<key.length; k++ )
					if ( f!=0 )
						str = replaceWrongSpelling(str, key[k], key[k%4]);
					else
						str = replaceWrongSpelling(str, key[k], replacement[k]);
				
				sb.append(str + "\n");
			}
			
			FileUtility.writeInFile(files[f], sb.toString(), false);
		}
	}
	
	/**
	 * 
	 * @param str
	 * @param key
	 * @param replacement
	 * @return
	 */
	private String replaceWrongSpelling ( String str, String key, String replacement ) {
		
		int x=-1;
		
		//System.out.println(str + " -- " + key);
		while( (x=str.indexOf( key, x+1)) > -1 ) {
			if ( (x == 0 || String.valueOf(str.charAt(x-1)).matches("[^a-zA-z0-9]"))
					&& String.valueOf(str.charAt(x+key.length())).matches("[^a-zA-z0-9]") ) {
					
				String tmp = str.substring(0, x) + replacement;
				if ( String.valueOf(str.charAt(x+key.length())).equals(" ")
						&& String.valueOf(str.charAt(x+key.length()+1)).equals(".")) {
					str = tmp + str.substring(x+key.length()+1);
				}
				else if ( String.valueOf(str.charAt(x+key.length())).equals(".")  )
					str = tmp + str.substring(x+key.length());				
				else
					str = tmp + "." + str.substring(x+key.length());
			}
		}
		
		return str;
	}
	

	/**
	 * 
	 * @param senFileName
	 * @param npMentionFileName
	 * @param pairFileName
	 * @param chainFileName
	 * @throws Exception
	 */
	private void correctBoundaries ( String senFileName, String npMentionFileName, String pairFileName, String chainFileName ) throws Exception {
		
		DataFile df = new DataFile();		
		df.readAllSentences(senFileName, npMentionFileName, chainFileName, null,
				null, false);		
		StringBuilder sb = new StringBuilder();
		
		for ( int m=0; m<df.listOfMentions.length; m++ ) {
			String[] str = df.listOfMentions[m].name.split("\\s+");

			ArrayList<Integer> listOfMatchedStringSIndexes = TextUtility.returnMatchedStringSIndex(
					df.listOfSentences.get(df.listOfMentions[m].senIndx).arrWordBySpace,  
					str);
						
			if ( !listOfMatchedStringSIndexes.contains(df.listOfMentions[m].sTokIndxBySpace) ) {
				for ( int x=1; x < 5; x++ ) {
					if ( listOfMatchedStringSIndexes.contains(df.listOfMentions[m].sTokIndxBySpace-x) ) {
						df.listOfMentions[m].sTokIndxBySpace = df.listOfMentions[m].sTokIndxBySpace - x;
						break;
					}
					else if ( listOfMatchedStringSIndexes.contains(df.listOfMentions[m].sTokIndxBySpace+x) ) {
						df.listOfMentions[m].sTokIndxBySpace = df.listOfMentions[m].sTokIndxBySpace + x;
						break;
					}
				}
			}
			
			df.listOfMentions[m].eTokIndxBySpace = df.listOfMentions[m].sTokIndxBySpace + str.length - 1;
			
			sb.append(df.listOfMentions[m].toStringFull() + "\n");
		}
		
		FileUtility.writeInFile(npMentionFileName, sb.toString(), false);
		/*
		sb = new StringBuilder();
		for ( int m=0; m<df.listOfPairs.size(); m++ )			
			sb.append(df.listOfPairs.get(m).toString(df) + "\n");
		
		FileUtility.writeInFile(pairFileName, sb.toString(), false);
		*/
		sb = new StringBuilder();
		for ( int m=0; m<df.listOfChains.size(); m++ )			
			sb.append(df.listOfChains.get(m).toString(df) + "\n");
		
		FileUtility.writeInFile(chainFileName, sb.toString(), false);
	}
	
}
