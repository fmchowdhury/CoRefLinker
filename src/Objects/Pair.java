package Objects;

public class Pair {

	public String corefType = "", fileId = "";
	public int antecedentMenitonIndex = -1, anaphoraMenitonIndex = -1;
	public double probabScore = 0.0;	
	public boolean polarity = true;
	
	/**
	 * 
	 * @param ant
	 * @param anph
	 * @return
	 */
	public String toString( NPMention ant, NPMention anph) {
		//c="patient" 21:0 21:0||t="coref person"||c="patient" 22:0 22:0
		return ant.toString() + "||t=\"coref " + ant.type + "\"||" + anph.toString();
	}
	
	
	public String toString( DataFile df ) {
		return df.listOfMentions[antecedentMenitonIndex].toString() + "||t=\"coref " 
			+ df.listOfMentions[antecedentMenitonIndex].type + "\"||" 
			+ df.listOfMentions[anaphoraMenitonIndex].toString();
	}
	
	public void getType ( DataFile df ) {
		corefType = df.listOfMentions[antecedentMenitonIndex].type.equalsIgnoreCase("pronoun") ?
				df.listOfMentions[anaphoraMenitonIndex].type : df.listOfMentions[antecedentMenitonIndex].type;
	}
	
	public static void print( NPMention ant, NPMention anph, DataFile df ) {
		System.out.println( df.fileId + "   " + ant.toString() + "   " +  anph.toString() + "    "
				+ df.containsPair(ant, anph));
		System.out.println( df.listOfSentences.get(ant.senIndx).text);
		System.out.println( df.listOfSentences.get(anph.senIndx).text);
		System.out.println();
	}
		
	
}
