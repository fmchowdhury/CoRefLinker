package Utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Knowledgebase.HumanList;


public class TextUtility {
	
	/**
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmptyString ( String str ) {
		if ( str == null || str.trim().isEmpty() )
			return true;
		
		return false;
	}
	
	/**
	 * 
	 * @param str
	 * @return
	 */
	public static String removeDeterminerAndPersonalPronounAndTitle ( String str ) {
		
		String[] words = str.trim().split("\\s+");
		str = "";
		
		for ( int x=0; x<words.length; x++ ) {
			String w = words[x].toLowerCase();
			if ( !HumanList.isFoundInDeterminerList(w) && !HumanList.hasHumanTitle(w)
					&& !HumanList.isFoundInPersonalPronounList(w)
					)
				str = str + " " + words[x];
		}
		
		return str.trim();
	}
	
	/**
	 * 
	 * @param listOfTokens
	 * @param isRemoveSpaceChar
	 * @return
	 */
	public static String mergeTokensOfList ( ArrayList<String> listOfTokens, boolean isRemoveSpaceChar ) {
		
		String text = "";
		
		for ( int i=0; i<listOfTokens.size(); i++ )
			text = text.concat(listOfTokens.get(i));
		
		if ( isRemoveSpaceChar )
			text = text.replaceAll("\\s+", "");
			
		return text; 	
	}
	
	

	/**
	 * 
	 * @param listOfStrings
	 * @return
	 */
	public static ArrayList<String> sortStringsByLength ( ArrayList<String> listOfStrings ) {
		
		for ( int i=0; i<listOfStrings.size()-1; i++ ) {
			for ( int k=i+1; k<listOfStrings.size(); k++ ) {
				
				if ( listOfStrings.get(i).length() > listOfStrings.get(k).length() ) {
					String x = listOfStrings.get(i);
					listOfStrings.set(i, listOfStrings.get(k));
					listOfStrings.set(k, x);
				}
			}
		}
		
		return listOfStrings;
	}
	
	/**
	 * 
	 * @param listOfTokens
	 * @param isRemoveSpaceChar
	 * @return
	 */
	public static String mergeFirstTokensOfList ( ArrayList<String[]> listOfTokens, boolean isRemoveSpaceChar ) {
		
		String text = "";
		
		for ( int i=0; i<listOfTokens.size(); i++ )
			text = text.concat(listOfTokens.get(i)[0]);
		
		if ( isRemoveSpaceChar )
			text = text.replaceAll("\\s+", "");
			
		return text; 	
	}
	
	/**
	 * Merge two overlapping strings
	 * 
	 * @param leftPart
	 * @param rightPart
	 * @param overlapBegIndex
	 * @param isIgnoreWhiteSpace
	 * @return
	 */
	public static String mergeOverlappedStrings(String leftPart, String rightPart, int overlapBegIndex, boolean isIgnoreWhiteSpace){
		
		int k = 0, i = 0, s = 0;
		String fullPart = "";
		
		if ( overlapBegIndex < 0 ){
			while( i<leftPart.length() && k<rightPart.length() ){
				if ( leftPart.charAt(i) == rightPart.charAt(k) ){
					i++;
					k++;				
				}
				else{
					i = s+1;
					s++;
					k = 0;
				}
			}
			
			if ( k >= rightPart.length() )
				return leftPart;
			else if ( s == 0 && leftPart.length() <= rightPart.length() )
				return rightPart;
			
			fullPart = leftPart.substring(0, s).concat(rightPart);
		}
		else{
		//	overlapBegIndex--;
			if ( isIgnoreWhiteSpace ){
				for ( i=0; i<leftPart.length() && i<=overlapBegIndex; i++ )
					if ( String.valueOf(leftPart.charAt(i)).matches("\\s") )
						overlapBegIndex++;
			}
			
			if ( leftPart.length() - overlapBegIndex > rightPart.length() )
				return leftPart;
			else
				fullPart = leftPart.substring(0, overlapBegIndex).concat(rightPart);
		}		
		
		return fullPart;
	}
	
	/**
	 * 
	 * @param text
	 * @return
	 */
	public static boolean containsAlphaNumeric( String text ){
		return text.matches(".*[A-Za-z0-9].*");
	}
	
	/**
	 * 
	 * @param text
	 * @return
	 */
	public static boolean containsLetter( String text ){
		return text.matches(".*[A-Za-z].*");
	}
	
	/**
	 * 
	 * @param text
	 * @return
	 */
	public static boolean isAllCaps( String text ){
		return text.matches("[A-Z]+") ;
	}
		
	/**
	 * 
	 * @param str
	 * @return
	 */
	public static String stringToRegex( String str ){
		String[] specialCharRegex = new String[]{"\\\\", "\\[", "\\^", "\\$", "\\.", "\\|", "\\?", "\\*", "\\+", "\\(", "\\)"};
		String[] specialCharReplace = new String[]{"\\\\\\", "\\[", "\\^", "\\\\$", "\\.", "\\|", "\\?", "\\*", "\\+", "\\(", "\\)"};
		
		for ( int i=0; i < specialCharRegex.length; i++ )
			str = str.replaceAll(specialCharRegex[i], "\\" + specialCharReplace[i]);
		
		return str;
	}
	
	/**
	 * 
	 * @param fullSenWithoutSpace
	 * @param patternStr
	 * @return
	 */
	public static ArrayList<String> returnMatchedString( String fullSenWithoutSpace, String patternStr ){
		 
		ArrayList<String> parserProducedTokens = new ArrayList<String>();
    	
    	// Compile and use regular expression
    	Pattern pattern = Pattern.compile(patternStr);
    	Matcher matcher = pattern.matcher(fullSenWithoutSpace);
    	boolean matchFound = matcher.find();

    	if (matchFound) {
    	    // Get all groups for this match
    	    for (int i=0; i<=matcher.groupCount(); i++) {
    	    	parserProducedTokens.add(matcher.group(i));    	        
    	    }
    	}
    	
    	return parserProducedTokens;
	}
	
	
	/**
	 * 
	 * @param senWords
	 * @param candStr
	 * @return
	 */
	public static ArrayList<Integer> returnMatchedStringSIndex( String[] senWords, String[] candStr ){
		 
		ArrayList<Integer> listOfMatchedStringSIndexes = new ArrayList<Integer>();
		
		for ( int w=0; w<senWords.length; w++ ) {
			int c=0;
			for ( ; c<candStr.length && w+c < senWords.length; c++ )
				if ( !senWords[w+c].equalsIgnoreCase(candStr[c]) )
					break;
			
			if ( c == candStr.length )
				listOfMatchedStringSIndexes.add(w);
		}
    	
		
    	return listOfMatchedStringSIndexes;
	}
	

	/**
	 * 
	 * @param subString
	 * @param text
	 * @return
	 */
	public static int countNumberOfSubstring ( String subString, String text, boolean ignoreSpace, boolean ignoreCase ) {
		
		subString = normalizeDangelingCaharactersInRegExp(subString);
		
		if ( ignoreCase ) {
			subString = subString.toLowerCase();
			text = text.toLowerCase();
		}
		
		if ( ignoreSpace ){
			text = text.replaceAll("\\s+", "");
			subString = subString.replaceAll("\\s+", "");
		}
		
		return text.split(subString).length - 1;
	}
	

	/**
	 * 
	 * @param X
	 * @param Y
	 * @return
	 */
	public static boolean hasOverlap ( int[] X, int[] Y ){	
		
		return (
				// if any of them are same
				X[0] == Y[0] || X[0] == Y[1] || X[1] == Y[0] || X[1] == Y[1]
				  || !(X[1] < Y[0] || X[0] > Y[1] || Y[1] < X[0] || Y[0] > X[1]));
		/*
		return (( checkWith[0] >= input[0] && checkWith[0] <= input[1] )
		|| ( checkWith[1] >= input[0] && checkWith[1] <= input[1] )
		|| ( checkWith[0] <= input[0] && checkWith[1] >= input[1] ));
		*/
	}
	
	/**
	 * 
	 * @param X
	 * @param Y
	 * @return
	 */
	public static boolean hasOverlap ( ArrayList<Integer> X, ArrayList<Integer> Y ){	
		
		return (
				// if any of them are same
				X.get(0) == Y.get(0) || X.get(0) == Y.get(1) || X.get(1) == Y.get(0) || X.get(1) == Y.get(1)
				  || !(X.get(1) < Y.get(0) || X.get(0) > Y.get(1) || Y.get(1) < X.get(0) || Y.get(0) > X.get(1)));
		/*
		return (( checkWith[0] >= input[0] && checkWith[0] <= input[1] )
		|| ( checkWith[1] >= input[0] && checkWith[1] <= input[1] )
		|| ( checkWith[0] <= input[0] && checkWith[1] >= input[1] ));
		*/
	}

	/**
	 * 
	 * @param X
	 * @param Y
	 * @return
	 */
	public static boolean hasOverlappingItems ( int[] X, int[] Y ){	
		
		for ( int i=0; i<X.length; i++ )
			for ( int k=0; k<Y.length; k++ )
			if (  Y[k] == X[i] )
				return true;
		
		return false; 
	}

	
	/**
	 * 
	 * @param d
	 * @return
	 */
	public static double roundTwoDecimals(double d) {
    	DecimalFormat twoDForm = new DecimalFormat("#.##");
    	return Double.valueOf(twoDForm.format(d));
	}
	
	/**
	 * 
	 * @param d
	 * @param n
	 * @return
	 */
	public static double roundNDecimals(double d, int n) {
		
		String str = "#.";
		
		for ( int i=0; i<n; i++ )
			str += "#";
		
    	DecimalFormat twoDForm = new DecimalFormat(str);
    	return Double.valueOf(twoDForm.format(d));
	}
	
	/**
	 * 
	 * @param strRegEx
	 * @return
	 */
	public static String normalizeDangelingCaharactersInRegExp( String strRegEx ){
		
		int i=-1;
		while ( (i=strRegEx.indexOf("\\", i+1)) >= 0 ){
			strRegEx = (i>0 ? strRegEx.substring(0, i) : "") + "\\\\"
					+ (i<strRegEx.length()-1 ? strRegEx.substring(i+1) : "");
			i = i + "\\\\".length();
		}
		
		strRegEx = strRegEx.replaceAll("\\[", "\\\\[")			
			.replaceAll("\\^", "\\\\^")
			.replaceAll("\\$", "\\\\$")
			.replaceAll("\\.", "\\\\.")
			.replaceAll("\\|", "\\\\|")
			.replaceAll("\\?", "\\\\?")
			.replaceAll("\\*", "\\\\*")
			.replaceAll("\\+", "\\\\+")
			.replaceAll("\\(", "\\\\(")
			.replaceAll("\\)", "\\\\)")
			.replaceAll("\\{", "\\\\{");
		//[\^$.|?*+()
		
		return strRegEx;
	}
	
	public static String replaceWithSepcialXmlChars( String str ){		
		return str.replaceAll("&", "&amp;").replaceAll("'", "&apos;")
		.replaceAll("\"", "&quot;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
	}
	
	public static String replaceSepcialXmlCharsWithOriginals( String str ){		
		return str.replaceAll("&apos;", "'").replaceAll("&quot;", "\"")
			.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&amp;", "&");
	}
	
	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

	public static String now() {
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
	    return sdf.format(cal.getTime());
	}
	
	
	/**
	 * 
	 * @param fileOne
	 * @param fileTwo
	 */
	public static void getFileOneLinesNotInFileTwo ( String fileOne, String fileTwo ) {
		
		ArrayList<String> linesOne = FileUtility.readNonEmptyFileLines(fileOne);
		ArrayList<String> linesTwo = FileUtility.readNonEmptyFileLines(fileTwo);
		int x =1;
		
		for ( int i=0; i<linesOne.size(); i++ ) {
			if ( !linesTwo.contains(linesOne.get(i)) ) {
				System.out.println( x + " " + linesOne.get(i));
				x++;
			}
		}
	}

	
	/**
	 * 
	 * @param sentence
	 * @param boundaryOfTarget
	 * @return
	 */
	public static String insertSpaceAtStringAbsBoundary ( String sentence, int[] boundaryOfTarget ){
		int z = 0, v = 0;
		while ( v != boundaryOfTarget[0]  ){			
			z++;
			if ( !String.valueOf(sentence.charAt(z)).matches("\\s")  )
				v++;
		}
		
		String front = sentence.substring(0, z);
		int w = z;
		
		while ( v != boundaryOfTarget[1]  ){			
			z++;
			if ( !String.valueOf(sentence.charAt(z)).matches("\\s")  )
				v++;
		}

		z++;
		String tail = sentence.substring(z);
	
		// insert before
		if ( w > 0 && sentence.charAt(w-1) != ' ' )
			front = front + " "; 
		// insert after
		if ( z < sentence.length() && sentence.charAt(z) != ' ' )
			tail = " " + tail;
				
		sentence = front + sentence.substring(w,z) + tail ;
				
		return sentence;
	}
	
	/**
	 * Convert InputStream object to String object.
	 * 
	 * @param inpStr
	 * @return
	 */
	public static String convertStreamToString(InputStream inpStr) {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(inpStr));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
            	inpStr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }
	
	
	/**
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumber ( String str ) {
	
		String regNumber = "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?";
		
		if ( str.matches(regNumber) )
			return true;
		else if ( str.matches(regNumber +"/"+ regNumber) )
			return true;
		
		return false;
	}
}
