package Utility;

import java.util.ArrayList;

public class WordPosTokenizer {
	
	String listExtendedPunc = "-/[]:;%(),.'\"*=+\\?!_#@$";
	
	/**
	 * Split further the tokens, obtained using initial tokenizer, if they contain
	 * any of the -/[]:;%(),.'"*=+\?!_#@$ inside them. Also, separate digits from 
	 * alphabetic characters.
	 * 
	 * @param inFileName
	 * @param outFileName
	 * @throws IOException
	 */
	public String[][] segmentPuncSymbolsWithinTokenizedData(String[][] wordAndPos){
		
		String[] temp = new String[0];
	    ArrayList<String[]> listOfWordAndPos = new ArrayList<String[]>();
	    
	    for ( int t=0; t<wordAndPos.length; t++ ){
	    	temp = furtherSplitToken(wordAndPos[t][0]);
			
			// if no punc symbol is found
			if ( temp.length == 0 )
				listOfWordAndPos.add(wordAndPos[t]);
			else{
				// for the remaining parts
				for ( int i=0; i<temp.length; i++ ){
					String[] tmpArr = new String[2];
					tmpArr[0] = temp[i];
					
					if ( listExtendedPunc.contains(temp[i]))
						tmpArr[1] = temp[i];
					else if ( temp[i].matches("[0-9]+"))
						tmpArr[1] = "CD";
					else
						tmpArr[1] = temp[i];
					
					listOfWordAndPos.add(tmpArr);								
				}
			}			
   	    }
	    
	    return DataStrucUtility.listToArrayOfString(listOfWordAndPos);
	}
	
	
	private String[] furtherSplitToken( String curToken ){
		
		ArrayList<String> temp = new ArrayList<String>();	    
		
		/*
		 * checking if the character at i-th index is a punctuation character
		  */				
		for ( int i=0; i<curToken.length(); i++ ){
			int e = i;
			boolean isSplit = false;
			
			if ( listExtendedPunc.contains(Character.toString(curToken.charAt(i)))){
				isSplit = true;
				e++;
			}
			/*
			// Separating alphabetic characters from digits
			else if ( Character.toString(curToken.charAt(i)).matches("[0-9]") ){
				isSplit = true;
				while ( (e<curToken.length() && curToken.charAt(e) >= '0'	&& curToken.charAt(e) <= '9')
						// checking for float
					|| (e+1<curToken.length() && (curToken.charAt(e) == ',' || curToken.charAt(e) <= '.')
							&& curToken.charAt(e+1) >= '0' && curToken.charAt(e+1) <= '9') 
					)							
				
						e++;
			}
			*/
			if ( isSplit ){	
				if ( i > 0 ) 
					temp.add(curToken.substring(0,i));
				
				temp.add(curToken.substring(i,e));					
				curToken = curToken.substring(e);
				i=-1;
			}
					
	  	}
		
		curToken = curToken.trim();
		
		if ( temp.size() > 0 && !curToken.isEmpty() )
			temp.add(curToken);
		
		if ( temp.size() == 0 )
			return new String[0];
		
		return DataStrucUtility.listToStringArray(temp);		
	}
}
