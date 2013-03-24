package Structures;


import java.util.ArrayList;

import Utility.*;


public class DependencyTree {

	public ArrayList<Integer> rootIndexes = new ArrayList<Integer>();
	public ArrayList<Boolean> hasRootChildren = new ArrayList<Boolean>();
	public DepTreeNode[] allNodesByWordIndex = new DepTreeNode[0];
	
	/**
	 * 
	 * @param senID
	 * @param wordAndPos
	 * @param origSen
	 */
	public DependencyTree ( String[][] arrWordWithPosByParser, String[] arrAllDepRelations ){
		
		String[][] arrAllSeparatedRelAndArgs = new SyntacticParser().separateRelationAndArgs(arrAllDepRelations);
		boolean isTokensGiven = true;
		
		if ( arrWordWithPosByParser == null || arrWordWithPosByParser.length == 0 ) {
			int totWord = 0;
			isTokensGiven = false;
			
			for ( int i=0; i<arrAllSeparatedRelAndArgs.length; i++ ) {
				int argIndx = Integer.valueOf(arrAllSeparatedRelAndArgs[i][2]);
				if ( argIndx > totWord )
					totWord = argIndx;
				
				argIndx = Integer.valueOf(arrAllSeparatedRelAndArgs[i][4]);
				if ( argIndx > totWord )
					totWord = argIndx;
			}
			
			allNodesByWordIndex = new DepTreeNode[totWord];
		}
		else
			allNodesByWordIndex = new DepTreeNode[arrWordWithPosByParser.length];
			
		int sCharIndex = 0;
		
		for ( int i=0; i<arrWordWithPosByParser.length; i++ ){
			
			allNodesByWordIndex[i] = new DepTreeNode();
			
			if ( isTokensGiven ) {
				allNodesByWordIndex[i].word = arrWordWithPosByParser[i][0];			
				allNodesByWordIndex[i].pos = arrWordWithPosByParser[i][1];	
			}
			
			allNodesByWordIndex[i].startCharIndex = sCharIndex;
			
			sCharIndex = sCharIndex + allNodesByWordIndex[i].word.length();
			allNodesByWordIndex[i].endCharIndex = sCharIndex  - 1;
			
			for ( int j=0; j<Common.arrPosToGeneralPos.length; j++)
				if ( allNodesByWordIndex[i].pos != null
						&& !allNodesByWordIndex[i].pos.equals("")
						&& Common.arrPosToGeneralPos[j][1].equalsIgnoreCase(allNodesByWordIndex[i].pos) )
					allNodesByWordIndex[i].posGeneral = Common.arrPosToGeneralPos[j][0]; 
				
			allNodesByWordIndex[i].wordIndex = i;	
			allNodesByWordIndex[i].lemma = 
				SyntacticParser.getLemma(allNodesByWordIndex[i].word, allNodesByWordIndex[i].pos);
			
			if ( !allNodesByWordIndex[i].lemma.equals("-") )
				allNodesByWordIndex[i].lemma = allNodesByWordIndex[i].lemma.replaceAll("-", "");
		}
		
		populateTree(arrAllSeparatedRelAndArgs, isTokensGiven);
	}
	
	
	/**
	 * 
	 * @param wordsAndNE
	 */
	public void assignNEcategory( String[][] wordsAndNE ){
		for ( int i=0; i<wordsAndNE.length; i++ )
			for ( int k=0; k<allNodesByWordIndex.length; k++ )
				if ( allNodesByWordIndex[k].word.equals(wordsAndNE[i][0]) ){
					allNodesByWordIndex[k].NEcategory = wordsAndNE[i][1];
					break;
				}
	}
	
	
	/**
	 * Thomas et al. 2011 BioNLp
	 * 
	 * @param depType
	 * @return
	 */
	public String generalizeDepType ( String depType ) {
		
		if ( depType.matches(".*subj.*") )
			return "subj";
		else if ( depType.matches(".*obj.*") )
			return "obj";
		else if ( depType.matches(".*prep.*|agent") )
			return "prep";
		else if ( depType.matches("nn|apos") )
			return "nn";
		
		return depType;
	}
	
	
	/**
	 * 
	 * @param allSeparatedRelAndArgs
	 */
	private void populateTree ( String[][] allSeparatedRelAndArgs, boolean isTokensGiven ){
		
		/**
		 * NOTE:
		 * Cotransfection of hGITRL and hGITR in embryonic kidney 293 cells activated the anti-apoptotic transcription factor NF-kappaB, via a pathway that appeared to involve TNFR-associated factor 2 (TRAF2) [7] and NF-kappaB-inducing kinase (NIK) [8].
		 * 
		 * In the above sentence, there is a vice-versa dependency -
		 * nsubj(appeared-22, pathway-20)
		 * rcmod(pathway-20, appeared-22) 
		 */
				
		for ( int i=0; i<allSeparatedRelAndArgs.length; i++ ) {
			
			// allSeparatedRelAndArgs[i][0] = generalizeDepType(allSeparatedRelAndArgs[i][0]); // The result after generalization is lower 
            
			int argOneIndx = Integer.valueOf(allSeparatedRelAndArgs[i][2]) - 1,
				argTwoIndx = Integer.valueOf(allSeparatedRelAndArgs[i][4]) - 1;
			
			// any dependency relation of a word with root is discarded by the following checking
			if ( argOneIndx < 0 || argTwoIndx < 0 )
				continue;
			
			// NOTE: we assume arg2 is child of arg1
			if ( allNodesByWordIndex[argTwoIndx].parentWordIndexes == null )
				allNodesByWordIndex[argTwoIndx].parentWordIndexes = new ArrayList<Integer>();			
			allNodesByWordIndex[argTwoIndx].parentWordIndexes.add(argOneIndx);			
			
			allNodesByWordIndex[argTwoIndx].wordIndex = argTwoIndx;
			
			if ( allNodesByWordIndex[argTwoIndx].relNameWithParents == null )
				allNodesByWordIndex[argTwoIndx].relNameWithParents = new ArrayList<String>();			
			allNodesByWordIndex[argTwoIndx].relNameWithParents.add(allSeparatedRelAndArgs[i][0]);
			
			if ( allNodesByWordIndex[argTwoIndx].childrenWordIndex == null )
				allNodesByWordIndex[argTwoIndx].childrenWordIndex = new ArrayList<Integer>();			
			allNodesByWordIndex[argOneIndx].childrenWordIndex.add(argTwoIndx);
			
			if ( allNodesByWordIndex[argTwoIndx].relNamesWithChildren == null )
				allNodesByWordIndex[argTwoIndx].relNamesWithChildren = new ArrayList<String>();			
			allNodesByWordIndex[argOneIndx].relNamesWithChildren.add(allSeparatedRelAndArgs[i][0]);
			
			allNodesByWordIndex[argOneIndx].wordIndex = argOneIndx;	
			
			if ( !isTokensGiven ) {
				allNodesByWordIndex[argOneIndx].word = allSeparatedRelAndArgs[i][1];
				allNodesByWordIndex[argTwoIndx].word = allSeparatedRelAndArgs[i][3];
			}
		}

		
		for ( int i=0; i<allSeparatedRelAndArgs.length; i++ )
			// only consider the conj_and and conj_or relations
			if ( allSeparatedRelAndArgs[i][0].equalsIgnoreCase("conj_and") || 
					allSeparatedRelAndArgs[i][0].equalsIgnoreCase("conj_or") )
			{
				int argOneIndx = Integer.valueOf(allSeparatedRelAndArgs[i][2]) - 1,
					argTwoIndx = Integer.valueOf(allSeparatedRelAndArgs[i][4]) - 1;
				
				if ( argOneIndx < 0 || argTwoIndx < 0 )
					continue;
				
				/**
				 *  NOTE: we assume arg2 has a conj_and or conj_or with arg1, then
				 *  the parents of arg1 are also parents of arg2  
				 */
				for ( int p1=0; p1<allNodesByWordIndex[argOneIndx].parentWordIndexes.size(); p1++ ) {
					int wiOfParentofArg1 = allNodesByWordIndex[argOneIndx].parentWordIndexes.get(p1); 
					if ( wiOfParentofArg1 != argTwoIndx && !allNodesByWordIndex[argTwoIndx].parentWordIndexes.contains(wiOfParentofArg1) ) {
						allNodesByWordIndex[argTwoIndx].parentWordIndexes.add(wiOfParentofArg1);
						allNodesByWordIndex[argTwoIndx].relNameWithParents.add(allNodesByWordIndex[argOneIndx].relNameWithParents.get(p1));
						
						allNodesByWordIndex[wiOfParentofArg1].childrenWordIndex.add(argTwoIndx);
						allNodesByWordIndex[wiOfParentofArg1].relNamesWithChildren.add(allNodesByWordIndex[argOneIndx].relNameWithParents.get(p1));
					}
				}
				
				/**
				 * Additionally, If the words (i.e. arg1 and arg2) are consecutive verbs, then the children of arg1 would be also the 
				 * children of arg2, and vice versa.
				 */
				if ( allNodesByWordIndex[argOneIndx].pos.toLowerCase().matches("v.*") 
						&& allNodesByWordIndex[argTwoIndx].pos.toLowerCase().matches("v.*")
						&& argTwoIndx - argOneIndx == 2 ) {
					
					for ( int ch=0; ch<allNodesByWordIndex[argOneIndx].childrenWordIndex.size(); ch++ ) {
						int wiOfChild = allNodesByWordIndex[argOneIndx].childrenWordIndex.get(ch); 
						if ( wiOfChild != argTwoIndx && !allNodesByWordIndex[argTwoIndx].childrenWordIndex.contains(wiOfChild) ) {
							allNodesByWordIndex[argTwoIndx].childrenWordIndex.add(wiOfChild);
							allNodesByWordIndex[argTwoIndx].relNamesWithChildren.add(allNodesByWordIndex[argOneIndx].relNamesWithChildren.get(ch));
							
							allNodesByWordIndex[wiOfChild].parentWordIndexes.add(argTwoIndx);
							allNodesByWordIndex[wiOfChild].relNameWithParents.add(allNodesByWordIndex[argOneIndx].relNamesWithChildren.get(ch));
						}
					}
					
					for ( int ch=0; ch<allNodesByWordIndex[argTwoIndx].childrenWordIndex.size(); ch++ ) {
						int wiOfChild = allNodesByWordIndex[argTwoIndx].childrenWordIndex.get(ch); 
						if ( wiOfChild != argOneIndx && !allNodesByWordIndex[argOneIndx].childrenWordIndex.contains(wiOfChild) ) {
							allNodesByWordIndex[argOneIndx].childrenWordIndex.add(wiOfChild);
							allNodesByWordIndex[argOneIndx].relNamesWithChildren.add(allNodesByWordIndex[argTwoIndx].relNamesWithChildren.get(ch));
							
							allNodesByWordIndex[wiOfChild].parentWordIndexes.add(argOneIndx);
							allNodesByWordIndex[wiOfChild].relNameWithParents.add(allNodesByWordIndex[argTwoIndx].relNamesWithChildren.get(ch));
						}
					}
				}
			}

		for ( int i=0; i<allNodesByWordIndex.length; i++ )
			if ( allNodesByWordIndex[i].parentWordIndexes.size() == 0 ) {
				rootIndexes.add(i);
				if ( allNodesByWordIndex[i].childrenWordIndex.size() < 1 )
					hasRootChildren.add(false);
				else
					hasRootChildren.add(true);
			}
		
		/*
		for ( int i=0; i<allNodesByWordIndex.length; i++ ){
			System.out.print(allNodesByWordIndex[i].wordIndex + " [" + allNodesByWordIndex[i].startCharIndex
					+ ", " + allNodesByWordIndex[i].endCharIndex + "]"
					+ allNodesByWordIndex[i].word  + " (");
			for ( int k=0; k<allNodesByWordIndex[i].parentWordIndexes.size(); k++ )
				System.out.print(allNodesByWordIndex[i].parentWordIndexes.get(k) + " ");
				System.out.print(") -> ");
			for ( int k=0; k<allNodesByWordIndex[i].childrenWordIndex.size(); k++ ){
				System.out.print(allNodesByWordIndex[i].childrenWordIndex.get(k) + " ");
			}
			System.out.println();
		}
	 */
		
		for ( int i=0; i<rootIndexes.size(); i++ )
			populateGrandChildList(rootIndexes.get(i), new ArrayList<Integer>());	
		
		for ( int i=0; i<allNodesByWordIndex.length; i++ )
			if ( allNodesByWordIndex[i].allGrandChildrenWordIndex.size() == 0)
				populateGrandChildList(allNodesByWordIndex[i].wordIndex, new ArrayList<Integer>());
	}
	
	
	
	/**
	 * 
	 * @param nodeIndex
	 * @param listAlreadyTraversed
	 * @return
	 */
	private ArrayList<Integer> populateGrandChildList( int nodeIndex, ArrayList<Integer> listAlreadyTraversed ){
		
		ArrayList<Integer> temp = new ArrayList<Integer>();
		listAlreadyTraversed.add(nodeIndex);
			
		if ( allNodesByWordIndex[nodeIndex].childrenWordIndex != null && allNodesByWordIndex[nodeIndex].childrenWordIndex.size() > 0 ){
			for ( int k=0; k<allNodesByWordIndex[nodeIndex].childrenWordIndex.size(); k++ ){
				if ( !listAlreadyTraversed.contains(allNodesByWordIndex[nodeIndex].childrenWordIndex.get(k)) ){
					temp = populateGrandChildList( allNodesByWordIndex[nodeIndex].childrenWordIndex.get(k),
							DataStrucUtility.listCopy(listAlreadyTraversed) );
					
					// if there is grand children
					if ( temp != null ){
						if ( allNodesByWordIndex[nodeIndex].allGrandChildrenWordIndex == null )
							allNodesByWordIndex[nodeIndex].allGrandChildrenWordIndex = temp;
						else
							allNodesByWordIndex[nodeIndex].allGrandChildrenWordIndex.addAll(temp);
					}
				}
			}
			
		}
		
		temp = new ArrayList<Integer>();
		if ( allNodesByWordIndex[nodeIndex].childrenWordIndex != null )
			temp.addAll(allNodesByWordIndex[nodeIndex].childrenWordIndex);
		if ( allNodesByWordIndex[nodeIndex].allGrandChildrenWordIndex != null )
			temp.addAll(allNodesByWordIndex[nodeIndex].allGrandChildrenWordIndex);
		
		if ( temp.size() > 0 )
			return temp;
		else
			return null;
	}
		
	
	/**
	 * 
	 * @param listOfIndexesOfEntityWords
	 * @return
	 */
	public DepTreeNode findHeadAndSubTree ( ArrayList<Integer> listOfIndexesOfEntityWords ){
		
		ArrayList<DepTreeNode> listOfNodes = new ArrayList<DepTreeNode>();
		for ( int i=0; i<listOfIndexesOfEntityWords.size(); i++ ){
			if ( allNodesByWordIndex[listOfIndexesOfEntityWords.get(i)].parentWordIndexes.size() > 0
					|| allNodesByWordIndex[listOfIndexesOfEntityWords.get(i)].childrenWordIndex.size() > 0 ){
				DepTreeNode node = allNodesByWordIndex[listOfIndexesOfEntityWords.get(i)].copy();			
				listOfNodes.add(node);
			}
		}
		
		// Hypothesis: except one of the words, all the others will have their parents inside one of themselves 
		
		/**
		 * The above hypothesis fails in cases like following -
		 * Bone/JJ morphogenetic/JJ protein-2/JJ (/NNP BMP-2/NN )/NNS induces/VBZ bone/NN formation/NN and/CC regeneration/NN in/IN adult/JJ vertebrates/NNS and/CC regulates/VBZ important/JJ developmental/JJ processes/NNS in/IN all/DT animals/NNS ./.
		 * amod()-6, Bone-1) 
		 * amod()-6, morphogenetic-2) 
		 * amod()-6, protein-2-3)
		 * nn()-6, (-4)
		 * nn()-6, BMP-2-5)
		 */
		
		if ( listOfNodes.size() == 0 )
			return null;
		
		DepTreeNode dn = listOfNodes.get(0);
		
		for ( int i=1; i<listOfNodes.size(); i++ ){
			if ( dn.wordIndex != listOfNodes.get(i).wordIndex ){
				DepTreeNode x  = findCommonHead(dn, listOfNodes.get(i), 0);
				if ( x != null )
					dn = x;
			}
		}
		
		return dn;
	}
	
	
	/**
	 * Basic idea:
	 * Find the grandparents which has both the target nodes as grand children.
	 * Then, for each of those grandparents find the minimum number of connecting nodes to the target nodes. 
	 * These nodes together with the target nodes and the particular grandparent form the minimal sub-tree rooted
	 * at that grandparent.
	 * Once all such minimal sub-tree are computed, select the shortest one.
	 * 
	 * @param node1
	 * @param nodeOneTraversed
	 * @param node2
	 * @param nodeTwoTraversed
	 * @return
	 */
	
	
	private ArrayList<Integer> findShortestPathBetweenTwoNodes( int parentWI, ArrayList<Integer> nodeTraversed,
			ArrayList<Integer> shortestPath, int childWI ){
			
		if ( allNodesByWordIndex[parentWI].childrenWordIndex.contains(childWI) ){
			nodeTraversed.add(childWI);
			
			if ( nodeTraversed.size() < shortestPath.size() || shortestPath.size() == 0  )
				return nodeTraversed;
		}
		else{
			
			for ( int i=0; i<allNodesByWordIndex[parentWI].childrenWordIndex.size(); i++ ){
				
				int ic = allNodesByWordIndex[parentWI].childrenWordIndex.get(i);
								
				if ( !nodeTraversed.contains(ic) 
						&& (allNodesByWordIndex[ic].childrenWordIndex.contains(childWI)
						|| allNodesByWordIndex[ic].allGrandChildrenWordIndex.contains(childWI)) && 
						(shortestPath.size() == 0 || shortestPath.size() > nodeTraversed.size() ) ){
					
					ArrayList<Integer> temp = DataStrucUtility.listCopy(nodeTraversed); 
					temp.add(ic);
					temp = findShortestPathBetweenTwoNodes(ic, temp, shortestPath, childWI);
					
					if ( temp.size() < shortestPath.size() || shortestPath.size() == 0 )
						shortestPath = temp;
				}					
			}	
		}
		
		return shortestPath;
	}

	
	/**
	 * 
	 * @param node1
	 * @param node2
	 * @return
	 */
	public DepTreeNode findCommonHead( DepTreeNode node1, DepTreeNode node2, int stageNo ){	
		
		/**
		 * We don't allow a head to be selected between two words directly because of a conj relation.
		 */
		
		if ( node2.parentWordIndexes.contains(node1.wordIndex) && 
				( stageNo > 0 || !node2.relNameWithParents.get(node2.parentWordIndexes.indexOf(node1.wordIndex)).contains("conj"))
			){
			node1.addChild(node2, false);
			return node1;
		}
		else if ( node1.parentWordIndexes.contains(node2.wordIndex) && 
				( stageNo > 0 || !node1.relNameWithParents.get(node1.parentWordIndexes.indexOf(node2.wordIndex)).contains("conj"))
				){
			node2.addChild(node1, true);
			return node2;
		}
				
		ArrayList<Integer> shortestPathForNodeOne = new ArrayList<Integer>(),
								shortestPathForNodeTwo = new ArrayList<Integer>();
		
		int commonHeadIndx = -1;
		
		if ( node1.allGrandChildrenWordIndex.contains(node2.wordIndex) ){
		
			shortestPathForNodeOne.add(node1.wordIndex);
			shortestPathForNodeOne = findShortestPathBetweenTwoNodes(
					node1.wordIndex, shortestPathForNodeOne, 
					new ArrayList<Integer>(), node2.wordIndex);
			
			commonHeadIndx = node1.wordIndex;
		}
		else if ( node2.allGrandChildrenWordIndex.contains(node1.wordIndex) ){
			
			shortestPathForNodeTwo.add(node2.wordIndex);
			shortestPathForNodeTwo = findShortestPathBetweenTwoNodes(
					node2.wordIndex, shortestPathForNodeTwo, 
					new ArrayList<Integer>(), node1.wordIndex);
			
			commonHeadIndx = node2.wordIndex;
		}	
		else {
		
			// Find the grandparents which has both the target nodes as grand children.
			for ( int i=0; i<allNodesByWordIndex.length; i++ ){
				
				if ( ( allNodesByWordIndex[i].childrenWordIndex.contains(node1.wordIndex) ||
						allNodesByWordIndex[i].allGrandChildrenWordIndex.contains(node1.wordIndex) )
						&& 
						(allNodesByWordIndex[i].childrenWordIndex.contains(node2.wordIndex) ||
								allNodesByWordIndex[i].allGrandChildrenWordIndex.contains(node2.wordIndex)) ){
				
					ArrayList<Integer> tempPathForNodeOne = new ArrayList<Integer>(),
								tempPathForNodeTwo = new ArrayList<Integer>();

					tempPathForNodeOne.add(allNodesByWordIndex[i].wordIndex);
					tempPathForNodeTwo.add(allNodesByWordIndex[i].wordIndex);
					
					tempPathForNodeOne = findShortestPathBetweenTwoNodes(
							allNodesByWordIndex[i].wordIndex, tempPathForNodeOne, 
							new ArrayList<Integer>(), node1.wordIndex);
					
					if ( tempPathForNodeOne.size() > 1 ){
						
						tempPathForNodeTwo = findShortestPathBetweenTwoNodes(
							allNodesByWordIndex[i].wordIndex, tempPathForNodeTwo, 
							new ArrayList<Integer>(), node2.wordIndex);
					
						if ( tempPathForNodeTwo.size() > 1 &&
							( commonHeadIndx == -1 ||
							tempPathForNodeOne.size() + tempPathForNodeTwo.size()
							< shortestPathForNodeOne.size() + shortestPathForNodeTwo.size()) ){
						
							commonHeadIndx = allNodesByWordIndex[i].wordIndex; 
						
							shortestPathForNodeOne = tempPathForNodeOne;
							shortestPathForNodeTwo = tempPathForNodeTwo;
						}
					}				
				}
			}
		}
		
		//-- create the sub-tree 
		if ( commonHeadIndx > -1 ){
			DepTreeNode root = allNodesByWordIndex[commonHeadIndx].copy();
			
			if ( commonHeadIndx == node1.wordIndex )
				root = node1;
			else if ( commonHeadIndx == node2.wordIndex )
				root = node2;
			
			DepTreeNode tempNode = root;
			
			for ( int i=1; i<shortestPathForNodeOne.size(); i++ ){
				DepTreeNode newNode;
				
				if ( node1.wordIndex == shortestPathForNodeOne.get(i) )
					newNode = node1;
				else if ( node2.wordIndex == shortestPathForNodeOne.get(i) )
					newNode = node2;
				else
					newNode = allNodesByWordIndex[shortestPathForNodeOne.get(i)].copy();
					
				tempNode.addChild(newNode, false);
				tempNode = newNode;
			}
			
			tempNode = root;
		
			for ( int i=1; i<shortestPathForNodeTwo.size(); i++ ){
				DepTreeNode newNode;
				
				if ( node2.wordIndex == shortestPathForNodeTwo.get(i) )
					newNode = node2;
				else if ( node1.wordIndex == shortestPathForNodeTwo.get(i) )
					newNode = node1;					
				else
					newNode = allNodesByWordIndex[shortestPathForNodeTwo.get(i)].copy();
					
				tempNode.addChild(newNode, false);
				tempNode = newNode;		
			}
			
			return root;
		}
			
		return null;
	}
	
	
	
	/**
	 * 
	 * @param isSimplifyEntity
	 * @param entityName1
	 * @param boundaryEnt1
	 * @param entityName2
	 * @param boundaryEnt2
	 * @param isConsiderNeCat
	 * @param medtType
	 * @return
	 */
	public DepTreeNode findMinimalSubTreeWithEntities ( boolean isSimplifyEntity, String entityName1, int[] boundaryEnt1,
			String entityName2, int[] boundaryEnt2, boolean isConsiderNeCat, int medtType,
			boolean isBlindEntity, int[][] arrBoundariesByWordIndexes ){
		
		ArrayList<Integer> entOneWIndx = null, entTwoWIndx = null;
			
		// Find the words that cover entities and determine the heads of those words.
		entOneWIndx = Common.findEntityWordIndexes( boundaryEnt1, arrBoundariesByWordIndexes);
		entTwoWIndx = Common.findEntityWordIndexes( boundaryEnt2, arrBoundariesByWordIndexes);	
				
		if ( TextUtility.hasOverlappingItems( DataStrucUtility.listToArray(entOneWIndx), 
				DataStrucUtility.listToArray(entTwoWIndx)) )
			return null;
		
		DepTreeNode headOfEnt1 = findHeadAndSubTree( DataStrucUtility.listCopy(entOneWIndx)), 
			headOfEnt2 = findHeadAndSubTree(DataStrucUtility.listCopy(entTwoWIndx));
		
		if ( headOfEnt1 == null || headOfEnt2 == null )
			return null;
			//		/*
		//-- Start only head words of entities
		if ( isSimplifyEntity && headOfEnt1.children != null && headOfEnt1.childrenWordIndex != null 
				&& headOfEnt1.relNamesWithChildren != null ){
			
			headOfEnt1.children.clear();
			headOfEnt1.childrenWordIndex.clear();
			headOfEnt1.relNamesWithChildren.clear();
		}
		
		if ( isSimplifyEntity && headOfEnt2.children != null && headOfEnt2.childrenWordIndex != null && headOfEnt2.relNamesWithChildren != null ){
			headOfEnt2.children.clear();
			headOfEnt2.childrenWordIndex.clear();
			headOfEnt2.relNamesWithChildren.clear();
		}
		//-- End only head words of entities
		//*/
		if ( headOfEnt1.wordIndex == headOfEnt2.wordIndex )
			return null;
		
		headOfEnt1.word = "ENT_T1";
		headOfEnt1.lemma = "ENT_T1";
		headOfEnt1.pos = "ENT_T1";
		
		headOfEnt2.word = "ENT_T2";
		headOfEnt2.lemma = "ENT_T2";
		headOfEnt2.pos = "ENT_T2";		
		
		if ( !isConsiderNeCat ){
			headOfEnt1.NEcategory = "T1";
			headOfEnt2.NEcategory = "T2";
		}
		else {
			headOfEnt1.NEcategory = "T1-" + headOfEnt1.NEcategory;
			headOfEnt2.NEcategory = "T2-" + headOfEnt2.NEcategory;
		}
		
		// Find common parent of the heads found in earlier step.
		DepTreeNode nodeCommonHead = null;
		if ( headOfEnt1.wordIndex < headOfEnt2.wordIndex )
			nodeCommonHead = findCommonHead(headOfEnt1, headOfEnt2, 0);
		else
			nodeCommonHead = findCommonHead(headOfEnt2, headOfEnt1, 0);

		if (  (medtType == 3 || medtType == 4)
				&& nodeCommonHead != null && nodeCommonHead.NEcategory != null
				&& (nodeCommonHead.NEcategory.equals("T1") || nodeCommonHead.NEcategory.equals("T2") ) ){
		
			 nodeCommonHead = extendShortestPathIfEntHeadIsRoot(nodeCommonHead);
		}
		
		if (  (medtType == 2 || medtType == 5)
				&& nodeCommonHead != null && nodeCommonHead.posGeneral.equals("verb") ){
			
			nodeCommonHead = extendShortestPathBySubject(nodeCommonHead);
		}
		
		if ( (medtType == 1 || medtType == 4)
				&& nodeCommonHead != null && 
				(nodeCommonHead.posGeneral == null || !nodeCommonHead.posGeneral.equals("verb") && !nodeCommonHead.posGeneral.equals("adj")) ){
		
			 nodeCommonHead = extendShortestPath(nodeCommonHead);
		}
		
		
		
		
		while ( nodeCommonHead!=null &&  nodeCommonHead.countTotalNodesInSubTree() < 4 &&  nodeCommonHead.parentWordIndexes.size() > 0 ){
			int hi = nodeCommonHead.wordIndex; 
			nodeCommonHead = extendShortestPathIfEntHeadIsRoot(nodeCommonHead);
			if ( hi == nodeCommonHead.wordIndex )
				break;
		}
		
		if ( nodeCommonHead!=null &&  nodeCommonHead.countTotalNodesInSubTree() < 4 ){
			nodeCommonHead = extendShortestPathByAddingChildOfRoot(nodeCommonHead);
		}
		return nodeCommonHead;
	}
	
	
	/**
	 * 
	 * @param node
	 * @return
	 */
	private DepTreeNode extendShortestPathIfEntHeadIsRoot( DepTreeNode node ){
				
		//-- Basically, here we consider the first parent (if there are multiple parents) only
		
		//for ( int p1=0; p1<node.parentWordIndexes.size(); p1++ ){
			//if ( allNodesByWordIndex[node.parentWordIndexes.get(p1)].posGeneral.equals("verb") 
				//	|| allNodesByWordIndex[node.parentWordIndexes.get(p1)].posGeneral.equals("adj")  )
		
		int p1=0;
		if ( p1 < node.parentWordIndexes.size() ) {
				DepTreeNode nodeNew = allNodesByWordIndex[node.parentWordIndexes.get(p1)].copy();
				
				nodeNew.addChild(node, false);
				return nodeNew;
		}
		//}
		
		return node;
	}
	
	/**
	 * 
	 * @param node
	 * @return
	 */
	private DepTreeNode extendShortestPathByAddingChildOfRoot( DepTreeNode node ){
			
		for ( int p1=0; p1<node.childrenWordIndex.size(); p1++ ){
			if ( allNodesByWordIndex[node.childrenWordIndex.get(p1)].pos.matches("V.*|NN.*")  ){
				DepTreeNode nodeNew = allNodesByWordIndex[node.childrenWordIndex.get(p1)].copy();
				node.addChild(nodeNew, false);
			}
		}
			
		return node;
	}
	
	/**
	 * 
	 * @param node
	 * @return
	 */
	private DepTreeNode extendShortestPath( DepTreeNode node ){
	
		for ( int p1=0; p1<node.children.size(); p1++ )
			if ( node.children.get(p1).posGeneral.equals("verb") 
					|| node.children.get(p1).posGeneral.equals("adj")  )
				return node;
			
		for ( int p1=0; p1<node.childrenWordIndex.size(); p1++ ){
			if ( allNodesByWordIndex[node.childrenWordIndex.get(p1)].posGeneral.equals("verb") 
					|| allNodesByWordIndex[node.childrenWordIndex.get(p1)].posGeneral.equals("adj")  ){
				DepTreeNode nodeNew = allNodesByWordIndex[node.childrenWordIndex.get(p1)].copy();
				
				node.addChild(nodeNew, false);
				return node;
			}
		}
		
		for ( int p1=0; p1<node.parentWordIndexes.size(); p1++ ){
			if ( allNodesByWordIndex[node.parentWordIndexes.get(p1)].posGeneral.equals("verb") 
					|| allNodesByWordIndex[node.parentWordIndexes.get(p1)].posGeneral.equals("adj")  ){
				DepTreeNode nodeNew = allNodesByWordIndex[node.parentWordIndexes.get(p1)].copy();
				
				nodeNew.addChild(node, false);
				return nodeNew;
			}
		}
		
		for ( int p1=0; p1<node.parentWordIndexes.size(); p1++ ){
			DepTreeNode nodeNew = allNodesByWordIndex[node.parentWordIndexes.get(p1)].copy();
				
			nodeNew.addChild(node, false);
			nodeNew = extendShortestPath(nodeNew);
			
			if ( nodeNew.posGeneral.equals("verb") 
					|| nodeNew.posGeneral.equals("adj")  )
				return nodeNew;
		}
		
		return node;
	}
	
	
	/**
	 * 
	 * @param node
	 * @return
	 */
	private DepTreeNode extendShortestPathBySubject( DepTreeNode node ){
		
		for ( int p1=0; p1<node.relNamesWithChildren.size(); p1++ )
			if ( node.relNamesWithChildren.get(p1).contains("subj") )
				return node;
			
		int indx = -1; 
		for ( int k=0; k<allNodesByWordIndex.length; k++ )
			if ( allNodesByWordIndex[k].parentWordIndexes != null
					&& (indx=allNodesByWordIndex[k].parentWordIndexes.indexOf(node.wordIndex)) > -1
					&& allNodesByWordIndex[k].relNameWithParents.get(indx).contains("subj") ){

				DepTreeNode nodeNew = allNodesByWordIndex[k].copy();
				
				node.addChild(nodeNew, false);
				return node;
			}

		return node;
	}
	
	
	public int[] boundaryWordIndexOfTree( DepTreeNode dn ) {
		
		int[] bn = new int[]{10000, -1};
		
		ArrayList<Integer> allWords = new ArrayList<Integer>();
		
		if ( dn.parentWordIndexes != null )
			allWords.addAll(dn.parentWordIndexes);
		
		if ( dn.childrenWordIndex != null )
			allWords.addAll(dn.childrenWordIndex);
		
		if ( dn.allGrandChildrenWordIndex != null )
			allWords.addAll(dn.allGrandChildrenWordIndex);
		
		allWords.add(dn.wordIndex);
		
		for ( int i=0; i<allWords.size(); i++ ) {
			if ( allWords.get(i) < bn[0] )
				bn[0] = allWords.get(i);
			if ( allWords.get(i) > bn[1] )
				bn[1] = allWords.get(i);			
		}
		
		return bn;
	}
	
}