package Structures;

import java.util.ArrayList;


public class DepTreeNode{

	public String word = null, pos = null, lemma = null, NEcategory = null, posGeneral = "", pharasalCat = null;
	public int wordIndex = -1, startCharIndex = -1, endCharIndex = -1;

	public ArrayList<Integer>  parentWordIndexes = new ArrayList<Integer>();
	public ArrayList<Integer> childrenWordIndex = new ArrayList<Integer>();
	public ArrayList<String> relNamesWithChildren = new ArrayList<String>();
	public ArrayList<String> relNameWithParents = new ArrayList<String>();

	// index of all lower tree nodes in the same branch
	public ArrayList<Integer> allGrandChildrenWordIndex = new ArrayList<Integer>();

	public ArrayList<DepTreeNode> children = null;
	public ArrayList<DepTreeNode> parents = null;

	public DepTreeNode( ){
		
	}
	
	/**
	 * 
	 * @return
	 */
	public int countTotalNodesInSubTree() {
		
		ArrayList<Integer> listOfNodes = new ArrayList<Integer>();
		
		if ( parentWordIndexes != null )
			for( int i=0; i<parentWordIndexes.size(); i++ )
				if ( !listOfNodes.contains(parentWordIndexes.get(i)) )
					listOfNodes.add(parentWordIndexes.get(i));
		
		if ( childrenWordIndex != null )
			for( int i=0; i<childrenWordIndex.size(); i++ )
				if ( !listOfNodes.contains(childrenWordIndex.get(i)) )
					listOfNodes.add(childrenWordIndex.get(i));
		
		if ( allGrandChildrenWordIndex != null )
			for( int i=0; i<allGrandChildrenWordIndex.size(); i++ )
				if ( !listOfNodes.contains(allGrandChildrenWordIndex.get(i)) )
					listOfNodes.add(allGrandChildrenWordIndex.get(i));
		
		if ( listOfNodes.contains(wordIndex) )
			return listOfNodes.size();
			
		return listOfNodes.size() + 1;
	}

	/**
	 * 
	 * @param child
	 * @param isAddBefore
	 */
	public void addChild( DepTreeNode child, boolean isAddBefore ){
		
		if ( this.childrenWordIndex.contains(child.wordIndex) ){
			for ( int i=0; i<children.size(); i++ )
				if ( children.get(i).wordIndex == child.wordIndex ){
					children.remove(i);
					childrenWordIndex.remove(i);				
					break;
				}
		}
				
		if ( this.children == null )
			this.children = new ArrayList<DepTreeNode>();
		
		if ( child.parents == null )
			child.parents = new ArrayList<DepTreeNode>();
		
		int index = children.size();
		if ( isAddBefore )
			index = 0;
				
		this.children.add(index, child);
		this.childrenWordIndex.add(index, child.wordIndex);
		
		int ind = child.parentWordIndexes.indexOf(this.wordIndex);
		
		this.relNamesWithChildren.add(index, child.relNameWithParents.get(ind));
		child.parents.add(this);
	}


	public DepTreeNode copy(){
		DepTreeNode node = new DepTreeNode(); 
		
		node.word = this.word;
		node.wordIndex = this.wordIndex;
		node.lemma = this.lemma;
		node.pos = this.pos;
		node.posGeneral = this.posGeneral;
		node.pharasalCat = this.pharasalCat;
		node.NEcategory = this.NEcategory;
		
		// NOTE: children are not copied
		if (  this.parentWordIndexes != null ) {
			node.parentWordIndexes = new ArrayList<Integer>();
			node.parentWordIndexes.addAll(this.parentWordIndexes);
		}
		
		if (  this.relNameWithParents != null ) {
			node.relNameWithParents = new ArrayList<String>();
			node.relNameWithParents.addAll(this.relNameWithParents);
		}
		
		if (  this.allGrandChildrenWordIndex != null ) {
			node.allGrandChildrenWordIndex = new ArrayList<Integer>();
			node.allGrandChildrenWordIndex.addAll(this.allGrandChildrenWordIndex);
		}
		
		return node;
	}
	
	public DepTreeNode copyFull(){
		DepTreeNode node = new DepTreeNode(); 
		
		node.word = this.word;
		node.pos = this.pos;
		node.lemma = this.lemma;
		node.NEcategory = this.NEcategory;
		node.posGeneral = this.posGeneral;
		node.pharasalCat = this.pharasalCat;
		node.wordIndex = this.wordIndex;
		node.startCharIndex = this.startCharIndex;
		node.endCharIndex = this.endCharIndex;

		if (  this.parentWordIndexes != null ) {
			node.parentWordIndexes = new ArrayList<Integer>();
			node.parentWordIndexes.addAll(this.parentWordIndexes);
		}		
	
		if (  this.childrenWordIndex != null ) {
			node.childrenWordIndex = new ArrayList<Integer>();
			node.childrenWordIndex.addAll(this.childrenWordIndex);
		}

		if (  this.relNamesWithChildren != null ) {
			node.relNamesWithChildren = new ArrayList<String>();
			node.relNamesWithChildren.addAll(this.relNamesWithChildren);
		}
			
		if (  this.relNameWithParents != null ) {
			node.relNameWithParents = new ArrayList<String>();
			node.relNameWithParents.addAll(this.relNameWithParents);
		}

		if (  this.allGrandChildrenWordIndex != null ) {
			node.allGrandChildrenWordIndex = new ArrayList<Integer>();
			node.allGrandChildrenWordIndex.addAll(this.allGrandChildrenWordIndex);
		}

		if ( this.children != null ) {
			node.children = new ArrayList<DepTreeNode>();
			node.children.addAll(this.children);
		}

		if ( this.parents != null ) {
			node.parents = new ArrayList<DepTreeNode>();
			node.parents.addAll(this.parents);
		}
		
		return node;
	}

	public String printTree( boolean isTrucEtAl2009Format, boolean isIncludeWord, boolean isIncludePOS, boolean isIncludeRelName, boolean isIncludeLemma,
			boolean isIncludePOSGeneral, boolean isIncludePharasalCat){

		if ( isTrucEtAl2009Format )
			return //"(root " +
				consturctPrintTreeTrucEtAl2009(isIncludeWord, isIncludePOS, isIncludeRelName, isIncludeLemma, 
					isIncludePOSGeneral, isIncludePharasalCat)
					//+ ")"
					;
		else
			return //"(root " + 
			consturctPrintTree(isIncludeWord, isIncludePOS, isIncludeRelName, isIncludeLemma, 
				isIncludePOSGeneral, isIncludePharasalCat) 
				//+ ")"
				;
	}

	/**
	 * 
	 * @param isIncludeWord
	 * @param isIncludePOS
	 * @param isIncludeRelName
	 * @param isIncludeLemma
	 * @param isIncludePOSGeneral
	 * @param isIncludePharasalCat
	 * @return
	 */
	private String consturctPrintTreeTrucEtAl2009( boolean isIncludeWord, boolean isIncludePOS, boolean isIncludeRelName, boolean isIncludeLemma,
			boolean isIncludePOSGeneral, boolean isIncludePharasalCat){
		String str = "";
		
		// in case, the root of the subtree is the head of one of the enitites
		if ( this.parents == null && this.NEcategory != null )
			str += " (" + this.NEcategory;
				
		
		if ( isIncludePharasalCat )
			str += " (" + this.pharasalCat;
		
		if ( isIncludePOS )
			str += " (" + this.pos;
		
		if ( isIncludeWord )
			str += " (" + this.word.replaceAll("\\(", "<LB>").replaceAll("\\)", "<RB>"); 
		
		if ( isIncludeLemma )
			str += " (" + this.lemma;		
		
		
		if ( isIncludePOSGeneral ){
			if (this.posGeneral != null && !this.posGeneral.equals("") )
				str += " (" + this.posGeneral;
			else
				str += " (" + this.pos;				
		}
				
		if ( children != null ){
			for ( int i=0; i<children.size(); i++ ){
							
				if ( children.get(i).NEcategory != null )
					str += " (" + children.get(i).NEcategory;
				
				if ( isIncludeRelName )
					str += " (" + relNamesWithChildren.get(i) + " "; 
				
				str += children.get(i).consturctPrintTreeTrucEtAl2009( isIncludeWord, isIncludePOS, isIncludeRelName, isIncludeLemma, isIncludePOSGeneral, isIncludePharasalCat);

				if ( isIncludeRelName )
					str += ")";				
				
				if ( children.get(i).NEcategory != null )
					str += ")";		
			}
		}
		
		if ( isIncludePOSGeneral ){
			//if (this.posGeneral != null && !this.posGeneral.equals("") )
				str += ")";
		}
		
		if ( isIncludePharasalCat )
			str += ")";
				
		if ( isIncludeLemma )
			str += ")";
		
		if ( isIncludeWord )
			str += ")";
		
		if ( isIncludePOS )
			str += ")";
		
		if ( this.parents == null && this.NEcategory != null )
			str += ")";
		
		return str;
	}

	/**
	 * 
	 * @param isIncludeWord
	 * @param isIncludePOS
	 * @param isIncludeRelName
	 * @param isIncludeLemma
	 * @param isIncludePOSGeneral
	 * @param isIncludePharasalCat
	 * @return
	 */
	private String consturctPrintTree( boolean isIncludeWord, boolean isIncludePOS, boolean isIncludeRelName, boolean isIncludeLemma,
			boolean isIncludePOSGeneral, boolean isIncludePharasalCat){
		String str = "";
		
		// in case, the root of the subtree is the head of one of the enitites
		if ( this.parents == null && this.NEcategory != null )
			str += " (" + this.NEcategory;
				
		
		if ( isIncludePharasalCat )
			str += " (" + this.pharasalCat;
		
		if ( isIncludePOS )
			str += " (" + this.pos;
		
		if ( isIncludeWord )
			str += " (" + this.word.replaceAll("\\(", "<LB>").replaceAll("\\)", "<RB>"); 
		
		if ( isIncludeLemma )
			str += " (" + this.lemma;		
		
		
		if ( isIncludePOSGeneral ){
			if (this.posGeneral != null && !this.posGeneral.equals("") )
				str += " (" + this.posGeneral;
			else
				str += " (" + this.pos;				
		}
				
		if ( children != null ){
			for ( int i=0; i<children.size(); i++ ){
								
				if ( isIncludeRelName )
					str += " (" + relNamesWithChildren.get(i) + " "; 
				
				if ( children.get(i).NEcategory != null )
					str += " (" + children.get(i).NEcategory;
				
				str += children.get(i).consturctPrintTree( isIncludeWord, isIncludePOS, isIncludeRelName, isIncludeLemma, isIncludePOSGeneral, isIncludePharasalCat);

				if ( children.get(i).NEcategory != null )
					str += ")";				
				
				if ( isIncludeRelName )
					str += ")";				
						
			}
		}
		
		if ( isIncludePOSGeneral ){
			//if (this.posGeneral != null && !this.posGeneral.equals("") )
				str += ")";
		}
		
		if ( isIncludePharasalCat )
			str += ")";
				
		if ( isIncludeLemma )
			str += ")";
		
		if ( isIncludeWord )
			str += ")";
		
		if ( isIncludePOS )
			str += ")";
		
		if ( this.parents == null && this.NEcategory != null )
			str += ")";
		
		return str;
	}

}
