/**
 * 
 *  @author Faisal Chowdhury ( fmchowdhury@gmail.com )
 * 
 */

package Utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;

import edu.stanford.nlp.ling.WordLemmaTag;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;

import Utility.FileUtility;


public class SyntacticParser {
	
	static String EMPTY_RELATION = "EMPTY";
	
	/**
	 * 
	 * @param word
	 * @param pos
	 * @return
	 */
	public static String getLemma( String word, String pos ) { /*, boolean onlyIfPosMatches ){
	   	 if ( !onlyIfPosMatches || pos.toLowerCase().charAt(0) == 'n' ){
			    Vector<LexItem> out = null;
				
				try {
					out = Common.apiLvgNorm.Mutate(word, false);
				} catch (Exception e) {
					e.printStackTrace();					
				}
				
				if ( out.size() == 0 )
					word = word.toLowerCase();
				else
					word = ((LexItem)out.elementAt(out.size()-1)).GetTargetTerm().trim();
		 }
		 else
			 word = word.toLowerCase();
	 
	 	return word;
	*/			
		if ( word.matches(".*[a-zA-Z]+.*") && !word.matches(".*\\s+.*") )
			return (new WordLemmaTag(word, pos)).lemma();
		
		return word;
	}
	
	/**
	 * 
	 * @param strParseTreeFlatten
	 * @return
	 */
	private String extractWordLemmaPoS( String tokenWithPos ){
			
		StringBuilder sb = new StringBuilder("");
		System.out.println("Separating POS from tokens -> " + tokenWithPos);
		String[][] wordAndPos = ParseOutputUtility.separateTokenAndPos(tokenWithPos, true);
			//normStanfordParserWord(strParseTreeFlatten).split("\\s+");

		for( int i=0; i<wordAndPos.length; i++ ){

			/**
			 * NOTE: The following method returns lemma of inflectional morphology only, no derivational.
			 */
			sb.append(wordAndPos[i][0]).append("\t").append(getLemma(wordAndPos[i][0], wordAndPos[i][1]))
				.append("\t").append(wordAndPos[i][1]).append("\n");
		}
		
		sb.append("\n");
		return sb.toString();
	}
	
	/**
	 * 
	 * @param inFileName
	 * @param outTokenizeFileName
	 * @param outDepRelFileName
	 * @param isSkipParsing
	 * @throws Exception
	 */
	public void separateTokensAndDepRels( String inFileName, String outTokenizeFileName,
			String outDepRelFileName, String inSenIdFileName, boolean isSkipParsing ) throws Exception{
		
		ArrayList<ArrayList<String>> listOfMultilineInput = FileUtility.readAllMultiLineInputs(inFileName);
		ArrayList<String> listOfSenIDs = FileUtility.readNonEmptyFileLines(inSenIdFileName);
		
		FileUtility.writeInFile(outTokenizeFileName, "", false);
		FileUtility.writeInFile(outDepRelFileName, "", false);
		int senNo = 0;
		
		for ( int i=0; i<listOfMultilineInput.size(); i++ ) {
			
			// read tokens with pos tags
			FileUtility.writeInFile(outTokenizeFileName, extractWordLemmaPoS(listOfMultilineInput.get(i).get(0)), true);			
			
			if ( !isSkipParsing ) {
				i = i+2;
				String str = "[";
				// read dependencies
				for ( int d=0; d<listOfMultilineInput.get(i).size(); d++ ) {
					if ( str.length() != 1 )
		    			str = str + ", " + listOfMultilineInput.get(i).get(d);
		    		else
		    			str = str + listOfMultilineInput.get(i).get(d) ;
				}
				
				FileUtility.writeInFile(outDepRelFileName, listOfSenIDs.get(senNo) + "\n" + str + "]\n\n", true);
			}
			
			senNo++;
		}
	}
	
	
	/**
	 * 
	 * @param inSenFileName
	 * @param outFileName
	 * @param grammarFileForStanfordParser
	 * @param skipParsing
	 * @throws Exception
	 * /
	
	public void callStanfordParser( String inSenFileName, String outFileName, 
			String grammarFileForStanfordParser, boolean skipParsing) throws Exception{
		 
		LexicalizedParser lp = new LexicalizedParser(grammarFileForStanfordParser,
				new String[]{"-maxLength", "500", "-MAX_ITEMS", "300000",  "-retainTmpSubcategories", 
					"-outputFormat", "wordsAndTags,penn,typedDependenciesCollapsed"});
		
		
	        
		BufferedReader input =  new BufferedReader(new FileReader(new File(inSenFileName)));
		
		FileUtility.writeInFile(outFileName, "", false);
		
		String line = null, errors = "";
		int x = 0;
		PrintWriter pw = new PrintWriter(new FileWriter(outFileName));
		
		while (( line = input.readLine()) != null){
	    	
	    	line = line.trim();
	    	
	    	if ( !line.isEmpty() ){
	    		System.out.println("[" + TextUtility.now() + "] Parsing line no. " + x + " -> " + line);
	    		
	    		try{
	    			Tree parseTree = lp.apply(line);
	    			TreePrint tp = lp.getTreePrint();
	    			
	    		    if ( skipParsing )
	    		    	tp = new TreePrint("wordsAndTags");
	    		    else
	    		    	tp = new TreePrint("wordsAndTags,penn,typedDependenciesCollapsed");
	    		    
	    		    tp.printTree(parseTree, pw);
	    	  	    
	    		}
				catch (Exception ex){
					errors = errors + "Tokenization error occured for sentence no. " + x + "\n";
					System.exit(0);
				}
				
				x++;
			}
		}
		
		input.close();
		
		//-- Making sure if there is an empty output for dependency then one additional empty line is added
		ArrayList<String> listOfParsedLines = FileUtility.readFileLines(outFileName);
		StringBuilder sb = new StringBuilder();
		
		boolean prevLineEmpty = false;
		for ( int i=0; i<listOfParsedLines.size(); i++ ) {
			if ( listOfParsedLines.get(i).trim().isEmpty() ) {
				if ( prevLineEmpty )
					sb.append("\n");
				prevLineEmpty = true;
			}
			else 
				prevLineEmpty = false;
			
			sb.append(listOfParsedLines.get(i) + "\n");
		}
		
		FileUtility.writeInFile(outFileName, sb.toString(), false);
		
		System.out.println("Parsing of data inside " + inSenFileName + " completed.\n\n");    	
	}
	
	/**
	 * 
	 * @param rel
	 * @return
	 */
	public String[][] separateRelationAndArgs( String[] rel ){
		
		String[][] allSeparatedRelAndArgs = new String[rel.length][5];
		
		for ( int i=0; i<rel.length; i++ ){
			int k = rel[i].indexOf("(");
			String dep = rel[i].substring(0, k);
			rel[i] = rel[i].substring(k+1).replace(", ", " ");
			String[] temp = rel[i].split("\\s+");
			
			if ( temp.length != 2 ){
				System.err.println("More than 3 elements in the dependency relation!");
				System.exit(0);
			}
									
			int[] tokNumber = new int[temp.length];
			// separate attached token no. at the ending of the tokens
			for ( int x=0; x<temp.length; x++ ){
				// remove unnecessary characters e.g. " or ' after token no., if there any
				k=temp[x].length()-1;
				while( temp[x].charAt(k) > '9' || temp[x].charAt(k) < '0' )
					k--;
				
				if ( k < temp[x].length()-1 )
					temp[x] = temp[x].substring(0, k+1);
					
				for ( k=temp[x].length()-1; k>=0; k-- )
					if ( temp[x].charAt(k) == '-' ){
						tokNumber[x] = Integer.valueOf(temp[x].substring(k+1));
						temp[x] = temp[x].substring(0, k);								
						break;	
					}
			}
			
			allSeparatedRelAndArgs[i][0] = dep;
			allSeparatedRelAndArgs[i][1] = temp[0];
			allSeparatedRelAndArgs[i][2] = tokNumber[0] + "";
			allSeparatedRelAndArgs[i][3] = temp[1];
			allSeparatedRelAndArgs[i][4] = tokNumber[1] + "";
		}
		
		return allSeparatedRelAndArgs;
	}
	

	/**
	 * 
	 * @param parsedFileName
	 * @return
	 * @throws Exception
	 */
  	public String extractDependencyRel( String parsedFileName ) throws Exception{
		String line = null;
		BufferedReader inputParse =  new BufferedReader(new FileReader(new File(parsedFileName)));
		
		Writer output = new BufferedWriter(new FileWriter(parsedFileName + "_senIdDep"));
		while (( line = inputParse.readLine()) != null){
			String senID = line.trim();
			
			line = inputParse.readLine().trim();
			
			if ( !line.equals("[]") ){
			
				output.write(senID + "\n");
						
				// separating the relations
				String[] rel = line.substring(1, line.length()-2).split("[)], ");
				String[][] allSeparatedRelAndArgs = separateRelationAndArgs(rel);

				// write dependency rel
				for ( int i=0; i<allSeparatedRelAndArgs.length; i++ )
					output.write( allSeparatedRelAndArgs[i][0] + " " + allSeparatedRelAndArgs[i][1]
				        + " " + allSeparatedRelAndArgs[i][2] + " " + allSeparatedRelAndArgs[i][3]
				                             + " " + allSeparatedRelAndArgs[i][4] + "\n");
				
				output.write("\n");
			}
			else
				output.write(senID + "\n" + EMPTY_RELATION + "\n\n");
			
			inputParse.readLine();
		}
		
		inputParse.close();
		output.close();
		
		return parsedFileName + "_senIdDep";
	}
  	
  	/**
  	 * 
  	 * @param fileName
  	 * @param senID
  	 * @return
  	 * @throws IOException
  	 */
  	public static ArrayList<String> readDependencyRelBySenID( String fileName, String senID) throws IOException {
		
		String line = null;
		ArrayList<String> tokenList = new ArrayList<String>();
		BufferedReader input =  new BufferedReader(new FileReader(new File(fileName)));  
		
	    while (( line = input.readLine()) != null ){
	    	
	    	line = line.trim();

	    	if ( line.equals(senID) ){
	    		while ( !( line = input.readLine().trim()).isEmpty() )
	    			tokenList.add(line);
	    		
	    		break;
	    	}
	    		
	    	while ( !( line = input.readLine().trim()).isEmpty() )
    			;
	    }
	    
	    input.close();
	    
	    if ( tokenList.size() == 1 && tokenList.contains(EMPTY_RELATION))
	    	return null;
	    	
	    // reached end of file
	    if ( line == null && tokenList.size() == 0)
	    	return null;
	    
	    return tokenList;
	}
  	
  	
  	public static void mergeDepRelFiles( String firstFileName, String secondFileName, String outFileName) throws IOException{
		
		BufferedReader input =  new BufferedReader(new FileReader(new File(firstFileName)));	    
	    StringBuilder contents = new StringBuilder("");
	    String line = null;
	    
	    int lineNo = 0;
		boolean isLineNo = true;
		while (( line = input.readLine()) != null ){
			line = line.trim();
						
			if ( line.isEmpty() ){
				contents.append("\n");
				lineNo++;
				isLineNo = true;
			}
			else if ( isLineNo ){
				contents.append(lineNo + "\n");
				isLineNo = false;
			}
			else
				contents.append(line).append("\n");			
		}
		
	    FileUtility.writeInFile(outFileName, contents.toString(), false);
	    
	    input =  new BufferedReader(new FileReader(new File(secondFileName)));
	    contents = new StringBuilder("");
	    
	    isLineNo = true;
		while (( line = input.readLine()) != null ){
			line = line.trim();
						
			if ( line.isEmpty() ){
				contents.append("\n");
				lineNo++;
				isLineNo = true;
			}
			else if ( isLineNo ){
				contents.append(lineNo + "\n");
				isLineNo = false;
			}
			else
				contents.append(line).append("\n");			
		}
		
	    FileUtility.writeInFile(outFileName, contents.toString(), true);	    	
	}
}