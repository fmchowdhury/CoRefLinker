package Structures;

import java.util.ArrayList;

public class PhraseStrucTreeNode{

	public String word = null, pos = null, lemma = null, NEcategory = null, posGeneral = "", pharasalCat = null;
	public int nodeIndex = -1, startCharIndex = -1, endCharIndex = -1, wordIndexByParser = -1;
	
	public ArrayList<PhraseStrucTreeNode> listOfChildren = null;
		
	public PhraseStrucTreeNode parent = null;
	
	// index of all lower tree nodes in the same branch
	public ArrayList<Integer> allTerminalNodeIndexesUnderThisNode = new ArrayList<Integer>();

	/**
	 * 
	 * @param child
	 */
	public void addChild( PhraseStrucTreeNode child ){
		
		child.parent = this;
		if ( listOfChildren == null )
			listOfChildren = new ArrayList<PhraseStrucTreeNode>();
			
		listOfChildren.add(child);
	}

	/**
	 * 
	 * @param isIncludeWord
	 * @param isIncludePOS
	 * @param isIncludeLemma
	 * @param isIncludePOSGeneral
	 * @param isIncludePharasalCat
	 * @return
	 */
	public String printTree( boolean isIncludeWord, boolean isIncludePOS, boolean isIncludeLemma,
			boolean isIncludePOSGeneral, boolean isIncludePharasalCat){

		return consturctPrintTree(isIncludeWord, isIncludePOS,  
				isIncludeLemma, isIncludePOSGeneral, isIncludePharasalCat);
	}
	
	/**
	 * 
	 * @param isIncludeWord
	 * @param isIncludePOS
	 * @param isIncludeLemma
	 * @param isIncludePOSGeneral
	 * @param isIncludePharasalCat
	 * @return
	 */
	private String consturctPrintTree( boolean isIncludeWord, boolean isIncludePOS, boolean isIncludeLemma,
			boolean isIncludePOSGeneral, boolean isIncludePharasalCat){
		String str = "";
		
		if ( this.NEcategory != null )
			str += " (" + this.NEcategory;
		
		if ( isIncludePOS && this.pos != null )
			str += " (" + this.pos;
		
		if ( isIncludeWord && this.word != null )
			str += " (" + this.word.replaceAll("\\(", "<LB>").replaceAll("\\)", "<RB>"); 
		
		if ( isIncludeLemma && this.lemma != null )
			str += " (" + this.lemma;
		
		if ( listOfChildren != null )
			for ( int i=0; i<listOfChildren.size(); i++ )
				str += listOfChildren.get(i).consturctPrintTree( isIncludeWord, isIncludePOS, isIncludeLemma, isIncludePOSGeneral, isIncludePharasalCat);
			
		if ( isIncludeLemma && this.lemma != null )
			str += ")";
		
		if ( isIncludeWord && this.word != null )
			str += ")";
		
		if ( isIncludePOS && this.pos != null )
			str += ")";
				
		if ( this.NEcategory != null )
			str += ")";
		
		return str;
	}

}
