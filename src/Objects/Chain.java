package Objects;

import java.util.ArrayList;

public class Chain {

	public String corefType = "";
	public ArrayList<Integer> listOfMentionIndexes = new ArrayList<Integer>();	
	
	/**
	 * 
	 * @return
	 */
	public String toString( DataFile df ) {
		
		StringBuilder sb = new StringBuilder();
		for ( int m=0; m<listOfMentionIndexes.size(); m++ )
			sb.append(df.listOfMentions[listOfMentionIndexes.get(m)].toString()).append("||");
		
		sb.append("t=\"coref " + corefType + "\"");			
		//c="syncope" 15:0 15:0||c="a syncopal episode" 19:7 19:9||c="syncope" 96:1 96:1||t="coref problem"
		
		return sb.toString();
	}	
}
