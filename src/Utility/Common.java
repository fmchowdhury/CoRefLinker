package Utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import Structures.Sentence;

public class Common {
	
	public static boolean isIncludeWord = false, isIncludePOS = false, isIncludeRelName = true,
	isIncludeLemma = true, isIncludePOSGeneral = false, isIncludePharasalCat = false,
	isSimplifyEntity = true, isTrucEtAl2009Format = false;
	
	public static ArrayList<ArrayList<String>> listOfPhrasalCatOfTokens = new ArrayList<ArrayList<String>>();


	/**
	 * 
	 * @throws Exception
	 */
	public void readTokenPhrasalCategory() throws Exception{
		String chunkFileName = "/media/Study/data/AImed/aimed_parsed_chunked",
			parsedFileName = "/media/Study/data/AImed/aimed_parsed_corrected";
		
		ArrayList<ArrayList<String>> listOfAllInput = FileUtility.readAllMultiLineInputs(chunkFileName);
		ArrayList<ArrayList<String>> listOfAllParsedWithDep = FileUtility.readAllMultiLineInputs(parsedFileName);
		ArrayList<ArrayList<String>> listOfAllParsed = new ArrayList<ArrayList<String>>();
		
		for ( int i=0; i<listOfAllParsedWithDep.size(); i++ ){
			if ( listOfAllParsedWithDep.get(i).size() > 0 && !listOfAllParsedWithDep.get(i).get(0).contains("(") )
				listOfAllParsed.add(listOfAllParsedWithDep.get(i));	
		}
		
		
		ArrayList<String> tempList = new ArrayList<String>();
		int sIndex = 0;
		for ( int i=0; i<listOfAllInput.size(); i++ ){
			
			for ( int k=0; k<listOfAllInput.get(i).size(); k++ ){
				String[] eles = listOfAllInput.get(i).get(k).trim().split("\\s+");
				tempList.add(eles[3]);
			}
	
			if ( listOfAllParsed.get(sIndex).get(0).trim().replaceAll("  ", " ").split("\\s+").length == tempList.size() ){
			
				listOfPhrasalCatOfTokens.add(tempList);
				tempList = new ArrayList<String>();
				sIndex++;
			}
		}
		
	}
	
	
	
	/**
	 * 
	 * @param entityName
	 * @param boundary
	 * @return
	 */
	public static ArrayList<Integer> findEntityWordIndexes ( int[] boundary,
			ArrayList<int[]> listOfBoundariesByWordIndexes ){
	
		ArrayList<Integer> listOfIndexesOfEntityWords = new ArrayList<Integer>();

		for ( int i=0; i<listOfBoundariesByWordIndexes.size(); i++ ){
			
			if ( TextUtility.hasOverlap(listOfBoundariesByWordIndexes.get(i), boundary) )
				listOfIndexesOfEntityWords.add(i);
		}

		return listOfIndexesOfEntityWords;
	}

	/**
	 * 
	 * @param boundary
	 * @param listOfBoundariesByWordIndexes
	 * @return
	 */
	public static ArrayList<Integer> findEntityWordIndexes ( int[] boundary,
			int[][] listOfBoundariesByWordIndexes ){

		ArrayList<Integer> listOfIndexesOfEntityWords = new ArrayList<Integer>();

		for ( int i=0; i<listOfBoundariesByWordIndexes.length; i++ ){
			
			if ( TextUtility.hasOverlap(listOfBoundariesByWordIndexes[i], boundary) )
				listOfIndexesOfEntityWords.add(i);
		}

		return listOfIndexesOfEntityWords;
	}

	
	/**
 	x = sentence from aimed full data
	y = parsed sentence
	ny = no of tokens of y
	if all the tokens of y equals all the tokens of x
	then do nothing
	else {
	read next y
	add ny to the token index of each of them
	merge token/tags with previous sentence
	merge dep_rel with previous sentence
	}	
	 * 
	 * @param parsedFileName
	 * @param fullDataFileName
	 * @param senSegmentedFileName
	 * @throws Exception
	 */
	public static void mergeSepratedParsedPartsOfSameSentences( String parsedFileName,
			String fullDataFileName, String senSegmentedFileName, boolean isBioCreative2Format ) throws Exception{
		
		BufferedReader inputParse = new BufferedReader(new FileReader(new File(parsedFileName)));
		ArrayList<Sentence> listSentence = Sentence.readFullData(fullDataFileName, isBioCreative2Format);
		int senIndx = 0, tokIndexAdd = 0, eduSenIndex = 0;
		
		String[] arrSenSegmented = new String[0];
		/* TODO: modify the following in a way so that the current method can be used by both BioEnEx and ModifiedTK
		if ( !senSegmentedFileName.isEmpty() ) {
			arrSenSegmented = new ClauseAnalyser().readSegmentedData(senSegmentedFileName);

			FileUtility.writeInFile(senSegmentedFileName + "_merged", "", false);
		}
		*/
		String EOLmarkerWithoutSpace = CommonUtility.EOLmarker.replaceAll("\\s+", "");
		
		boolean isReadNextSentenceFromAimed = true;
		String tokenWithPos = "", tempParseTree = "", parseTree = "", tempTokenWithPos = "",
			line = "", eduS = "";
		ArrayList<String> listDependencies = new ArrayList<String>(), tempListDependencies = new ArrayList<String>();
		
		StringBuilder sbCorrectedParse = new StringBuilder();
		
		while (( line = inputParse.readLine()) != null){
			// read tokenWithPos
			line = line.trim();
		//	System.out.println(line);
			
			if ( line.isEmpty() )
				continue;
			tempTokenWithPos += " " + line;
						
			String allTokens = "";
			String[][] tokenPosList = ParseOutputUtility.separateTokenAndPos(line, true);
			
			for ( int i=0; i<tokenPosList.length; i++ )
				allTokens += tokenPosList[i][0];
			
			inputParse.readLine();
			tempParseTree = inputParse.readLine();
			while (( line = inputParse.readLine()) != null){
				tempParseTree += "\n" + line;
				if ( line.trim().isEmpty() )
					break;
			}
			
			// read dependencies
			line = inputParse.readLine().trim();
			
			// no dep rel
			if ( line.isEmpty() )
				;//line = inputParse.readLine();
			else {
				ArrayList<String> listTemp = new ArrayList<String>();
				while ( line != null && !(line = line.trim()).isEmpty() ){					
					listTemp.add(line);
					line = inputParse.readLine();
				}
				
				String[][] allSeparatedRelAndArgs = new SyntacticParser().
					separateRelationAndArgs(DataStrucUtility.listToStringArray(listTemp));
				
				for ( int i=0; i<allSeparatedRelAndArgs.length; i++ ){
					int argOneIndx = Integer.valueOf(allSeparatedRelAndArgs[i][2]) + tokIndexAdd,
						argTwoIndx = Integer.valueOf(allSeparatedRelAndArgs[i][4]) + tokIndexAdd;
					
					tempListDependencies.add(allSeparatedRelAndArgs[i][0] + "(" + allSeparatedRelAndArgs[i][1]
					      + "-" + argOneIndx + ", " + allSeparatedRelAndArgs[i][3] + "-"
					      + argTwoIndx + ")");
				}
			}
						
			if ( isReadNextSentenceFromAimed && senIndx < listSentence.size() ){			
				senIndx++;
			}
			
			// checking the sentence end
			if ( allTokens.equals(EOLmarkerWithoutSpace) ) {
				isReadNextSentenceFromAimed = true;
			}
			else {
				isReadNextSentenceFromAimed = false;
				tokIndexAdd += tokenPosList.length;
		
				// collecting original CFG parses to use for clause splitting
			//	sbOrigCFGParse.append(tempParseTree.trim().replaceAll("\\n", " ") + "\n\n");
			
				// 1st line of psg tree
				if ( parseTree.isEmpty() )
					parseTree += "\n";
				parseTree += tempParseTree;
				
				if ( arrSenSegmented.length > 0 ) {
					if ( eduS.isEmpty() )
						eduS = "<S>\n";
					eduS += arrSenSegmented[eduSenIndex].trim();
					eduSenIndex++;
				}
									
				tokenWithPos += tempTokenWithPos;
				listDependencies.addAll(tempListDependencies);
			}
			
			tempParseTree = "";
			tempTokenWithPos = "";
			tempListDependencies.clear();
			
			if ( isReadNextSentenceFromAimed == true) {
				
				parseTree = parseTree.trim();
				if ( parseTree.indexOf("(ROOT", 5) > -1 )
					parseTree = "(SUPERROOT " + parseTree + ")";
				parseTree = parseTree.replaceAll("\\n", " ");
								
				tokenWithPos = tokenWithPos.trim();
				sbCorrectedParse.append(tokenWithPos + "\n\n");
				sbCorrectedParse.append(parseTree + "\n\n");
				tokIndexAdd = 0;
				
				if ( listDependencies.size() == 0 ) 
					sbCorrectedParse.append("\n");
				
				for ( int i=0; i<listDependencies.size(); i++ )
					sbCorrectedParse.append( listDependencies.get(i) + "\n");
				
				sbCorrectedParse.append("\n");
				
				//if ( arrSenSegmented.length > 0 )
					//FileUtility.writeInFile(senSegmentedFileName + "_merged", eduS.trim() + "\n</S>\n", true);
				
				eduS = "";
				tokenWithPos = "";
				parseTree = "";
				listDependencies.clear();
			}
			
		}
		
		inputParse.close();
		FileUtility.writeInFile(parsedFileName + "_corrected", sbCorrectedParse.toString(), false);
	}

	
	
	/**
	 * 
	 * @param listClauseBound
	 * @return
	 */
	public ArrayList<String> separateSenIDsFromClauseBound( ArrayList<String[]> listClauseBound ){
		ArrayList<String> listSenIDs = new ArrayList<String>();
		
		for ( int i=0; i<listClauseBound.size(); i++ )
			listSenIDs.add(listClauseBound.get(i)[0]);
		
		return listSenIDs;
	}
	
	
	public int[][] separateClauseBoundFromSenIDs( ArrayList<String[]> listClauseBound ){
		int[][] arrClauseBoundOfSen = new int[listClauseBound.size()][];
		
		for ( int i=0; i<listClauseBound.size(); i++ ){
			arrClauseBoundOfSen[i] = new int[listClauseBound.get(i).length-1];
			
			for ( int k=0; k<arrClauseBoundOfSen[i].length; k++ ) 
				arrClauseBoundOfSen[i][k] = Integer.valueOf(listClauseBound.get(i)[k+1]);
		}
		
		return arrClauseBoundOfSen;
	}
	
	

	
	public static String[][] arrPosToGeneralPos = new String[][]{
			{"conj", "CC", "Coordinating conjunction"},
			{"", "CD", "Cardinal number"},
			{"", "DT", "Determiner"},
			{"", "EX", "Existential there"},
			{"", "FW", "Foreign word"},
			{"prep", "IN", "Preposition or subordinating conjunction"},
			{"adj", "JJ", "Adjective"},
			{"adj", "JJR", "Adjective, comparative"},
			{"adj", "JJS", "Adjective, superlative"},
			{"", "LS", "List item marker"},
			{"", "MD", "Modal"},
			{"noun", "NN", "Noun, singular or mass"},
			{"noun", "NNS", "Noun, plural"},
			{"noun", "NNP", "Proper noun, singular"},
			{"noun", "NNPS", "Proper noun, plural"},
			{"", "PDT", "Predeterminer"},
			{"", "POS", "Possessive ending"},
			{"", "PRP", "Personal pronoun"},
			{"", "PRP$", "Possessive pronoun"},
			{"adverb", "RB", "Adverb"},
			{"adverb", "RBR", "Adverb, comparative"},
			{"adverb", "RBS", "Adverb, superlative"},
			{"adverb", "RP", "Particle"},
			{"", "SYM", "Symbol"},
			{"prep", "TO", "to"},
			{"", "UH", "Interjection"},
			{"verb", "VB", "Verb, base form"},
			{"verb", "VBD", "Verb, past tense"},
			{"verb", "VBG", "Verb, gerund or present participle"},
			{"verb", "VBN", "Verb, past participle"},
			{"verb", "VBP", "Verb, non-3rd person singular present"},
			{"verb", "VBZ", "Verb, 3rd person singular present"},
			{"", "WDT", "Wh-determiner"},
			{"", "WP", "Wh-pronoun"},
			{"", "WP$", "Possessive wh-pronoun"},
			{"", "WRB", "Wh-adverb"}
	};
}
