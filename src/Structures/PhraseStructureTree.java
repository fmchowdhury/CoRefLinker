package Structures;

import java.util.ArrayList;

import Utility.*;

public class PhraseStructureTree {

	PhraseStrucTreeNode root = new PhraseStrucTreeNode();	
	ArrayList<int[]> listOfBoundariesByWordIndexes = new ArrayList<int[]>();
	ArrayList<String> listOfNodesString = new ArrayList<String>();
	ArrayList<int[]> listOfWordAndNodeIndexes = new ArrayList<int[]>();
	
	
	
	public PhraseStructureTree() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * 
	 * @param pst
	 * @return
	 */
	public String[][] getWordAndPosFromParseTree ( String pst ){
		return readParseTreeString(pst, null);
	}
	
	
	/**
	 * 
	 * @param wordIndex
	 * @return
	 */
	public PhraseStrucTreeNode getParentPhraseNode ( ArrayList<Integer> wordIndexes ) {
	
		ArrayList<Integer> listOfNodeIndexes = new ArrayList<Integer>();
		
		for ( int i=0; i<listOfWordAndNodeIndexes.size(); i++ ) {
			
			if ( wordIndexes.contains(listOfWordAndNodeIndexes.get(i)[0]) )
				listOfNodeIndexes.add(listOfWordAndNodeIndexes.get(i)[1]);			
		}
		

		if ( listOfNodeIndexes.size() > 0 )
			return getParentPhraseNode(root, listOfNodeIndexes);
		
		return null;
	}
	
	/**
	 * 
	 * @param wordIndex
	 * @return
	 */
	public String getParentPhraseType ( int wordIndex ) {
		
		for ( int i=0; i<listOfWordAndNodeIndexes.size(); i++ ) {
			
			if ( listOfWordAndNodeIndexes.get(i)[0] == wordIndex ) {
				PhraseStrucTreeNode parentNode = getParentNode(root, listOfWordAndNodeIndexes.get(i)[1]);
				return parentNode.pos;
			}			
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param curNode
	 * @param nodeIndex
	 * @return
	 */
	private PhraseStrucTreeNode getParentNode ( PhraseStrucTreeNode curNode, int nodeIndex ) {
			
		if ( curNode.allTerminalNodeIndexesUnderThisNode.contains(nodeIndex) ){
			// TODO: is the following line correct ???
			if ( curNode.listOfChildren.contains(nodeIndex) )
				return curNode;
					
			for ( int i=0; i<curNode.listOfChildren.size(); i++ ){
				if ( curNode.listOfChildren.get(i).allTerminalNodeIndexesUnderThisNode.contains(nodeIndex) ){
					curNode = getParentNode(curNode.listOfChildren.get(i), nodeIndex);
					
					if ( curNode != null )
						return curNode;
				}
			}	
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param curNode
	 * @param nodeIndex
	 * @return
	 */
	private PhraseStrucTreeNode getParentPhraseNode ( PhraseStrucTreeNode curNode, ArrayList<Integer> nodeIndexes ) {
		
		if ( curNode.allTerminalNodeIndexesUnderThisNode.containsAll(nodeIndexes) ){
					
			for ( int i=0; i<curNode.listOfChildren.size(); i++ ){
				
				PhraseStrucTreeNode tmpNode = curNode.listOfChildren.get(i);
				
				if ( !tmpNode.pos.matches("(NNP|RP|PRP|RP|VBP|WP|SYM)") && (tmpNode.pos.endsWith("P") || tmpNode.pos.endsWith("ROOT") || tmpNode.pos.startsWith("S"))
					&&	TextUtility.isEmptyString(tmpNode.word) 
						&& tmpNode.allTerminalNodeIndexesUnderThisNode.containsAll(nodeIndexes) ){
					tmpNode = getParentPhraseNode(tmpNode, nodeIndexes);
					
					if ( tmpNode != null )
						curNode = tmpNode;
				}
			}
			
			return curNode;
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param pst
	 * @param arrWordWithPosByParser
	 * @return
	 */
	private String[][] readParseTreeString ( String pst, String[][] arrWordWithPosByParser ){
		String[] temp = pst.replaceAll("\\s+", " ").replaceAll("\\)\\s+\\)", "))").split("\\(");
		ArrayList<String> tagAndWords = new ArrayList<String>();
		
		for ( int i=0; i<temp.length; i++ ){
			temp[i] = temp[i].trim();
			
			if ( temp[i].isEmpty() )
				continue;
			
			String[] str = temp[i].replaceAll("\\)", ")#@%#").split("#@%#"); 
			for ( int k=0; k<str.length; k++ )
				if ( !(str[k]=str[k].trim()).isEmpty() )
					tagAndWords.add(str[k]);
		}
		
		PhraseStrucTreeNode node = new PhraseStrucTreeNode(), prevNode = null;
		ArrayList<String[]> listTemp = new ArrayList<String[]>();
		
		int sCharIndex = 0, wi = 0, ni=0;
		for ( int ii=0; ii<tagAndWords.size(); ii++ ){
		
			String[] str = tagAndWords.get(ii).split("\\s+");
			node = new PhraseStrucTreeNode();
			node.pos = str[0];
			node.nodeIndex = ni;
		
			if ( tagAndWords.get(ii).contains(")") ){
				
				if ( tagAndWords.get(ii).equals(")") ){
					if ( prevNode == null )
						break;
					
					prevNode = prevNode.parent;
					continue;
				}
				
				str[1] = str[1].replace(")", "");
				node.startCharIndex = sCharIndex;
				
				// if the tokenized words are provided
				if ( arrWordWithPosByParser != null && arrWordWithPosByParser.length > 0 )
					node.word = arrWordWithPosByParser[wi][0];
				else {
					str[1] = ParseOutputUtility.reconstructOrigTokensFromPrasedToken(str[1]);					
					node.word = str[1];
					listTemp.add( new String[]{str[1], str[0]});
				}
					
				node.wordIndexByParser = wi;
				listOfWordAndNodeIndexes.add(new int[]{ wi, ni});
				wi++;
								
				sCharIndex = sCharIndex + node.word.length();
				node.endCharIndex = sCharIndex  - 1;				
								
				node.lemma = SyntacticParser.getLemma(node.word, node.pos);
				
				if ( !node.lemma.equalsIgnoreCase(node.word) ) 
					node.lemma = node.lemma.trim();
				
				prevNode.addChild(node);
			}	
			else if ( prevNode == null ){
				
				prevNode = node;
				root = prevNode;
			}
			else{
				prevNode.addChild(node);
				prevNode = node;
			}
			
			ni++;
			listOfBoundariesByWordIndexes.add(new int[]{ node.startCharIndex, node.endCharIndex });
			if ( node.word != null )
				listOfNodesString.add( node.pos + " " + node.word);
			else
				listOfNodesString.add( node.pos );
		}
		
		root = populateTerminalChildrenList(root);
		return DataStrucUtility.listToArrayOfString(listTemp); 
	}
	
	/**
	 * 
	 * @param pst
	 * @param arrWordWithPosByParser
	 */
	public PhraseStructureTree ( String pst, String[][] arrWordWithPosByParser ){
		  readParseTreeString(pst, arrWordWithPosByParser);
	}
	
	
	/**
	 * 
	 * @param currentNode
	 * @return
	 */
	private PhraseStrucTreeNode populateTerminalChildrenList( PhraseStrucTreeNode currentNode ){
		
		if ( currentNode.listOfChildren == null ){
			currentNode.allTerminalNodeIndexesUnderThisNode.add(currentNode.nodeIndex);
			return currentNode;
		}
		
		for ( int i=0; i<currentNode.listOfChildren.size(); i++ ){
			PhraseStrucTreeNode child = populateTerminalChildrenList(currentNode.listOfChildren.get(i));
			
			currentNode.listOfChildren.set(i, child);
			
			if ( currentNode.listOfChildren.get(i).allTerminalNodeIndexesUnderThisNode.size() > 0 )
				currentNode.allTerminalNodeIndexesUnderThisNode.addAll(
						currentNode.listOfChildren.get(i).allTerminalNodeIndexesUnderThisNode);
		}
		
		return currentNode;
	}
	
		
	
	
	
	/**
	 * 
	 * @param curNode
	 * @param listEntNodeIndexes
	 * @return
	 */
	private PhraseStrucTreeNode findPetRootWithAllNodesOfEntities ( PhraseStrucTreeNode curNode, ArrayList<Integer> listEntNodeIndexes ){
		
		if ( curNode.allTerminalNodeIndexesUnderThisNode.containsAll(listEntNodeIndexes) ){
			for ( int i=0; i<curNode.listOfChildren.size(); i++ ){
				if ( curNode.listOfChildren.get(i).allTerminalNodeIndexesUnderThisNode.containsAll(listEntNodeIndexes) ){
					curNode = findPetRootWithAllNodesOfEntities(curNode.listOfChildren.get(i), listEntNodeIndexes);
					
					if ( curNode != null )
						break;
				}
			}
			
			return curNode;	
		}
		
		return null;
	}
	
	
	private boolean hasTerminalNodeOverlap ( PhraseStrucTreeNode curNode, ArrayList<Integer> listEntNodeIndexes ){
		
		for ( int i=0; i<listEntNodeIndexes.size(); i++ ){
			if ( curNode.allTerminalNodeIndexesUnderThisNode.contains(listEntNodeIndexes.get(i)) )		
			return true;	
		}
		
		return false;
	}

	private PhraseStrucTreeNode pruneLeft ( PhraseStrucTreeNode curNode, ArrayList<Integer> listEntNodeIndexes ){

		PhraseStrucTreeNode temp =  new PhraseStrucTreeNode();
		
		for ( int i=0; curNode.listOfChildren!= null && i<curNode.listOfChildren.size(); i++ ){
			if ( hasTerminalNodeOverlap ( curNode.listOfChildren.get(i), listEntNodeIndexes ) ){
				temp = pruneLeft(curNode.listOfChildren.get(i), listEntNodeIndexes );
				
				if (temp != null )
					curNode.listOfChildren.set(i, temp);
				break;
			}
			else{
				curNode.listOfChildren.remove(i);
				i--;
			}
		}
		
		return curNode;
	}
	
	
	private PhraseStrucTreeNode pruneRight ( PhraseStrucTreeNode curNode, ArrayList<Integer> listEntNodeIndexes ){

		PhraseStrucTreeNode temp =  new PhraseStrucTreeNode();
		
		if ( curNode.listOfChildren != null )
			for ( int i=curNode.listOfChildren.size()-1; i>=0; i-- ){
				if ( hasTerminalNodeOverlap ( curNode.listOfChildren.get(i), listEntNodeIndexes ) ){
					temp = pruneRight(curNode.listOfChildren.get(i), listEntNodeIndexes );
					
					if (temp != null )
						curNode.listOfChildren.set(i, temp);
					break;
				}
				else{
					curNode.listOfChildren.remove(i);
				}
			}
		
		return curNode;
	}
	

	private PhraseStrucTreeNode updateEntityCategory( PhraseStrucTreeNode curNode,
			ArrayList<Integer> listEntityNodeIndexes, String neCat ){
				
		if ( curNode.allTerminalNodeIndexesUnderThisNode.containsAll(listEntityNodeIndexes) ){
			int i = 0;
			
			for ( i=0; curNode.listOfChildren != null && i<curNode.listOfChildren.size(); i++ ){
				if ( curNode.listOfChildren.get(i).allTerminalNodeIndexesUnderThisNode.containsAll(listEntityNodeIndexes) ){
					
					PhraseStrucTreeNode temp = updateEntityCategory(curNode.listOfChildren.get(i), listEntityNodeIndexes, neCat); 
								
					if ( curNode != null ){
						curNode.listOfChildren.set(i, temp);
	
						break;
					}
				}
			}
			
			if ( curNode.listOfChildren == null || i == curNode.listOfChildren.size() )
				curNode.NEcategory = neCat;
		}
	
		return curNode;
	}
	
	
	/**
	 * 
	 * @param entityName1
	 * @param boundaryEnt1
	 * @param entityName2
	 * @param boundaryEnt2
	 * @param isConsiderNeCat
	 * @return
	 */
	public PhraseStrucTreeNode findPathEnclosedTreeWithEntities ( String entityName1, int[] boundaryEnt1,
			String entityName2, int[] boundaryEnt2, boolean isConsiderNeCat ){
		
		ArrayList<Integer> listNodeIndexForBothEntities = Common.findEntityWordIndexes( boundaryEnt1, listOfBoundariesByWordIndexes);
		ArrayList<Integer> temp = Common.findEntityWordIndexes( boundaryEnt2, listOfBoundariesByWordIndexes);
		
		if ( TextUtility.hasOverlappingItems( DataStrucUtility.listToArray(listNodeIndexForBothEntities), 
				DataStrucUtility.listToArray(temp)) )
			return null;
		else {
		
			if ( !isConsiderNeCat ){
				root = updateEntityCategory(root, listNodeIndexForBothEntities, "T1");
				root = updateEntityCategory(root, temp, "T2");
			}
			
			listNodeIndexForBothEntities.addAll(temp);
		}
		
		
		PhraseStrucTreeNode petRoot = findPetRootWithAllNodesOfEntities( root, listNodeIndexForBothEntities);
		
		// Prune left part of PET
		petRoot = pruneLeft(petRoot, listNodeIndexForBothEntities);
		
		// Prune right part of PET
		petRoot = pruneRight(petRoot, listNodeIndexForBothEntities);
		
		return petRoot;
	}
	
	
}