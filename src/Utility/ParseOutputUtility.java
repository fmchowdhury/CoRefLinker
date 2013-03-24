package Utility;

import java.io.IOException;
import java.util.ArrayList;

import Structures.DependencyGraph;
import Structures.PhraseStructureTree;
import Structures.Sentence;

public class ParseOutputUtility {

/*	
	public static void main( String[] args ) throws IOException {
		String path = "/home/chowdhury/workspace/DDIExtraction2011";
		new ParseOutputUtility().mergePsgAndDepOutput( path + "/training.parsed.bllip", path + "/training.parsed.bllip.dep", 
				path + "/training.parsed.bllip.complete");
		new ParseOutputUtility().mergePsgAndDepOutput( path + "/test.parsed.bllip", path + "/test.parsed.bllip.dep", 
				path + "/test.parsed.bllip.complete");
	}
	*/
	
	/**
	 * 
	 * @param inFileName
	 * @param hasTokensWithPos
	 * @param hasPsgTree
	 * @param hasDepTree
	 * @param listOfSentence
	 * @return
	 * @throws Exception
	 */
	public ArrayList<Sentence> readParsedData( String inFileName, boolean hasTokensWithPos,
			boolean hasPsgTree, boolean hasDepTree, ArrayList<Sentence> listOfSentence ) throws Exception{
		
		ArrayList<ArrayList<String>> listOfMultilineInput = FileUtility.readAllMultiLineInputsWithEmptyLines(inFileName);
		//System.out.println("Reading parsed data of " + inFileName);
		int senNo = 0;
		
		for ( int i=0; i<listOfMultilineInput.size(); i++ ) {
	
			if ( listOfSentence.get(senNo).text.trim().isEmpty() ) {
				i--;
				senNo++;
				continue;
			}
				
			if ( hasTokensWithPos ) {
				listOfSentence.get(senNo).arrWordAndPosByParser = 
						ParseOutputUtility.separateTokenAndPos( listOfMultilineInput.get(i).get(0), true);
			
				listOfSentence.get(senNo).detectBoundariesAndLemmas();
				i++;
			}
			
			if ( hasPsgTree ) {
				String str = "";
				// read PSG tree
				for ( int p=0; p<listOfMultilineInput.get(i).size(); p++ )
					str = str + " " + listOfMultilineInput.get(i).get(p);
				
				listOfSentence.get(senNo).psgTree = new PhraseStructureTree(str, 
						listOfSentence.get(senNo).arrWordAndPosByParser);
				i++;
			}
			
			if ( hasDepTree )
				listOfSentence.get(senNo).depGraph = new DependencyGraph(listOfSentence.get(senNo).arrWordAndPosByParser,
					DataStrucUtility.listToStringArray(listOfMultilineInput.get(i)));
			
			senNo++;
		}
		
		return listOfSentence;
	}
	
	
	

	/**
	 * 
	 * @param origSenWithOutSpace
	 * @param origWord
	 * @param prevToks
	 * @param nextTok
	 * @return
	 */
	private static String fixUnmatchedToken ( String origSenWithOutSpace, String origWord, String prevToks, String nextTok ){
		
		String pattern = TextUtility.normalizeDangelingCaharactersInRegExp(prevToks 
				+ (origWord.length()>1 ? origWord.charAt(0) :"")) 
				//+ ".*";
				//-- the following line assumes that the length of the unmatched token has maximum 2 characters variation than the original token
				+ ".{" + (origWord.length()-1) + "," + origWord.length() + "}";
		pattern = pattern + TextUtility.normalizeDangelingCaharactersInRegExp(nextTok);
		ArrayList<String> matchedTokens = TextUtility.returnMatchedString(origSenWithOutSpace, pattern);
	
		if ( matchedTokens.size() > 0 ){
			
			origWord = matchedTokens.get(0).substring(prevToks.length());
			pattern = ".*" + TextUtility.normalizeDangelingCaharactersInRegExp(nextTok);
		
			while( matchedTokens.size() > 0 ){
				int ntl = nextTok.length();
				if ( ntl < 1 )
					break;
				
				origWord = origWord.substring(0, origWord.length() - ntl);
				matchedTokens = TextUtility.returnMatchedString(origWord, pattern);	
			}
		}

		return origWord;
	}

	/**
	 * NOTE: no token is deleted or added in this method. Only spelling is checked.
	 * 
	 * @param wordAndPos
	 * @param origSen
	 * @return
	 */
	public static String[][] checkTokensInOriginalSentence ( String[][] wordAndPos, String origSen ) {
		for ( int i=0; i<wordAndPos.length; i++ )
			 wordAndPos[i][0] = wordAndPos[i][0].replaceAll("\\s+", "").replaceAll("\\\\/", "/")
			.replaceAll("``", "\"").replaceAll("''", "\"");
		
		String origSenWithOutSpace = origSen.replaceAll("\\s+", "");
		
		for ( int i=0; i<wordAndPos.length; i++ ){
			
			//-- Checking whether spelling of the original word is changed by the parser
			String prevToks = "";
			if ( i-2 > -1 )
				prevToks = wordAndPos[i-2][0];
			else
				prevToks =  "";
			
			if ( i-1 > -1 )
				prevToks += wordAndPos[i-1][0];
			else
				prevToks +=  "";
										
			if ( !origSenWithOutSpace.contains(prevToks + wordAndPos[i][0]) ){				
				 String tmp = fixUnmatchedToken(origSenWithOutSpace, wordAndPos[i][0], prevToks, 
						(i+1 < wordAndPos.length ? wordAndPos[i+1][0] : "")).trim();
				 
				 if ( !tmp.isEmpty() )
					 wordAndPos[i][0] = tmp;
			}
		}
		
		return wordAndPos;
	}
	
	
	/**
	 * 
	 * @param tokenWithPos
	 * @param isNormalizeBrackets
	 * @return
	 */
	public static String[][] separateTokenAndPos( String tokenWithPos, boolean isNormalizeBrackets ){
		
		String[] tmpList = isNormalizeBrackets == true ? tokenWithPos.replaceAll("-LRB-", "(")
			.replaceAll("-RRB-", ")").replaceAll("-LSB-", "[").replaceAll("-RSB-", "]")
			.replaceAll("-LCB-", "{").replaceAll("-RCB-", "}").replaceAll("\\*", "*").split("\\s+")
			: tokenWithPos.split("\\s+");
		
		ArrayList<String> list = new ArrayList<String>();
		int k = 0;
		
		for ( int i=0; i<tmpList.length; i++ ){
			//-- if a token is found without having a pos tag, e.g. ..... of/IN 7 1\/2/CD ..... here, 7 does not have pos
			//-- also,  ..... patients/NNS aged/VBN <or=10 years and patients aged >/CD 10/CD years/NNS .....
			if ( i > 0 && !tmpList[i-1].contains("/") )
				list.set(k-1, list.get(k-1) + " " + tmpList[i]);
			else {
				list.add(tmpList[i]);
				k++;
			}			
		}
		
	//	System.out.println(tokenWithPos);
		
		String[][] wordAndPos = new String[list.size()][2];
		for ( int i=0; i<list.size(); i++ ){
			String str = list.get(i); 
			k = str.lastIndexOf("/");
			if ( k == str.length() -1 ){
				k--;
				while ( !String.valueOf(str.charAt(k)).equals("/") )
					k--;
			}
			//System.out.println(i);
			wordAndPos[i] = new String[]{ str.substring(0, k), str.substring(k+1) };
			
			wordAndPos[i][0] = reconstructOrigTokensFromPrasedToken(wordAndPos[i][0]);
		}
			 
		return wordAndPos;
	}
	
	
	/**
	 * 
	 * @param token
	 * @return
	 */
	public static String reconstructOrigTokensFromPrasedToken ( String token  ) {
		
		token = token.replaceAll("-LRB-", "(").replaceAll("-RRB-", ")").replaceAll("-LSB-", "[").replaceAll("-RSB-", "]");
	
		while ( token.contains("\\*") )
			token = token.replace("\\*", "*");
		
		while ( token.contains("\\/") )
			token = token.replace("\\/", "/");
		
		return token;
	}
	
	/**
	 * 
	 * @param inPsgFile
	 * @param inDepFile
	 * @param outFile
	 * @throws IOException 
	 */
	public static void mergePsgAndDepOutput ( String inPsgFile, String inDepFile, String outFile ) throws IOException {
	/*
		// making sure that if there is an empty dependency set then a blank line is inserted after that
		ArrayList<String> listTemp = FileUtility.readFileLines(inDepFile);
		int noPrevBlankLines = 0;
		StringBuilder sb = new StringBuilder();
		
		for ( int i=0; i<listTemp.size(); i++ ) {
			if ( listTemp.get(i).trim().isEmpty() )
				noPrevBlankLines++;
			else {
				if ( noPrevBlankLines == 2 )
					sb.append("\n");
				else if ( noPrevBlankLines > 2 ) {
					System.err.println("Possiblly missing dependencies for multiple consecutive linses in file " + inDepFile);
					System.exit(0);
				}
				noPrevBlankLines = 0;
			}
			
			sb.append(listTemp.get(i).trim() + "\n");	
		}
		
		FileUtility.writeInFile(inDepFile, sb.toString(), false);
		*/
		
	    ArrayList<String> listOfParseTrees = FileUtility.readNonEmptyFileLines(inPsgFile);
	    ArrayList<ArrayList<String>> listOfDepTrees = FileUtility.readAllMultiLineInputsWithEmptyLines(inDepFile);
	    StringBuilder sb = new StringBuilder();
	    
	    for ( int pt=0; pt<listOfParseTrees.size(); pt++ ) {
	    
	    	sb.append(getTokensPsgDepForSen(listOfParseTrees.get(pt)));
	    	for ( int dr=0; dr<listOfDepTrees.get(pt).size(); dr++ ) 
	    		sb.append(listOfDepTrees.get(pt).get(dr) + "\n");	
	    	
	    	sb.append("\n");	
	    }
	    
	    FileUtility.writeInFile( outFile, sb.toString(), false);
	}
	
	/**
	 * 
	 * @param pst
	 * @return
	 */
	private static String getTokensPsgDepForSen( String pst ) {
		String[][] wordAndPos = new PhraseStructureTree().getWordAndPosFromParseTree(pst);
		
		String str = "";
		for ( int i=0; i<wordAndPos.length; i++ )
			str += wordAndPos[i][0] + "/" + wordAndPos[i][1] + " ";
		
		str = str.trim() + "\n\n(ROOT " + pst.trim() + ")\n\n";
		
		return str;
	}

}
