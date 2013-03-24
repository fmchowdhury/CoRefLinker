package Structures;

import java.util.ArrayList;

public class DependencyGraph {
	
	public DepGraphVertex[] allNodesByWordIndex = new DepGraphVertex[0];
	
	
	/**
	 * 
	 * @param senID
	 * @param wordAndPos
	 * @param origSen
	 */
	public DependencyGraph ( String[][] arrWordAndPosByParser, String[] listAllDepRelations ){
		
		DependencyTree dt = new DependencyTree(arrWordAndPosByParser, listAllDepRelations);
				
		allNodesByWordIndex = new DepGraphVertex[dt.allNodesByWordIndex.length];
		
		for ( int i=0; i<dt.allNodesByWordIndex.length; i++ ){
			
			allNodesByWordIndex[i] = new DepGraphVertex();
			allNodesByWordIndex[i].word = dt.allNodesByWordIndex[i].word;
			allNodesByWordIndex[i].pos = dt.allNodesByWordIndex[i].pos;
			allNodesByWordIndex[i].startCharIndex = dt.allNodesByWordIndex[i].startCharIndex;
						
			allNodesByWordIndex[i].endCharIndex = dt.allNodesByWordIndex[i].endCharIndex;
			allNodesByWordIndex[i].pharasalCat = dt.allNodesByWordIndex[i].pharasalCat;
			allNodesByWordIndex[i].posGeneral = dt.allNodesByWordIndex[i].posGeneral; 
				
			allNodesByWordIndex[i].wordIndex = dt.allNodesByWordIndex[i].wordIndex;	
			allNodesByWordIndex[i].lemma = dt.allNodesByWordIndex[i].lemma;
			
			allNodesByWordIndex[i].parentWordIndexes.addAll(dt.allNodesByWordIndex[i].parentWordIndexes);
			allNodesByWordIndex[i].relNameWithParents.addAll(dt.allNodesByWordIndex[i].relNameWithParents);
			
			allNodesByWordIndex[i].childrenWordIndex.addAll(dt.allNodesByWordIndex[i].childrenWordIndex);
			allNodesByWordIndex[i].relNamesWithChildren.addAll(dt.allNodesByWordIndex[i].relNamesWithChildren);
		}
	}
		

	
	/**
	 * 
	 * @param listOfWIndx
	 * @return
	 */
	public int findHeadFromListOfWordIndexes ( ArrayList<Integer> listOfWIndx ) {
	
		// Hypothesis: except one of the words, all the others will have their parents inside one of themselves 
		ArrayList<Integer> entWIndx = new ArrayList<Integer>();
		entWIndx.addAll(listOfWIndx);
		
		for ( int i=0; i<entWIndx.size(); i++ ){
						
			for ( int k=0; k<entWIndx.size(); k++ ){
				if ( i!=k && allNodesByWordIndex[entWIndx.get(i)].childrenWordIndex.contains(entWIndx.get(k)) ) {
					entWIndx.remove(k);
					k--;
				}
				else if ( i!=k && allNodesByWordIndex[entWIndx.get(i)].parentWordIndexes.contains(entWIndx.get(k)) ) {
					entWIndx.remove(i);
					i--;
					break;
				}	
			}
		}
		
		ArrayList<Integer> listWIforRemoval = new ArrayList<Integer>();
		// If there remain multiple words, then we discard those words which are children of the parent(s) of other word(s)
		for ( int i=0; i<entWIndx.size() && entWIndx.size() > 1 ; i++ ){
			 
			if ( allNodesByWordIndex[entWIndx.get(i)].parentWordIndexes != null ) {
				// for each parent p of entWIndx.get(i)
				for ( int p=0; p< allNodesByWordIndex[entWIndx.get(i)].parentWordIndexes.size(); p++ ){					
					for ( int j=0; j<entWIndx.size(); j++ ){
						// if p has a child like entWIndx.get(j) 
						if ( i!=j && !listWIforRemoval.contains(entWIndx.get(j)) 
								&& allNodesByWordIndex[allNodesByWordIndex[entWIndx.get(i)]
						         .parentWordIndexes.get(p)].childrenWordIndex.contains(entWIndx.get(j)) ) {
							// then add p in entWIndx
							entWIndx.add(allNodesByWordIndex[entWIndx.get(i)].parentWordIndexes.get(p));
							
							listWIforRemoval.add(entWIndx.get(j));
							listWIforRemoval.add(entWIndx.get(i));
						}
					}
				}
			}
		}
		
		for ( int i=0; i<listWIforRemoval.size(); i++ )
			entWIndx.remove( (Integer) listWIforRemoval.get(i));
		
		// whether any words from the initial list left
		for ( int i=0; i<listOfWIndx.size(); i++ )
			// if found remove all the other words which were added later 
			if ( listOfWIndx.contains(entWIndx.get(i)) ) {
				for ( int k=0; k<entWIndx.size(); k++ )
					if ( !listOfWIndx.contains(entWIndx.get(k)) ) {
						entWIndx.remove(k);
						k--;
					}
				break;
			}
		
		for ( int i=0; i<entWIndx.size() && entWIndx.size() > 1; i++ ){
			// this to make sure the rightmost alphanumeric word gets selected
			if ( !allNodesByWordIndex[entWIndx.get(i)].word.matches(".*[A-Za-z0-9].*") ) {
				entWIndx.remove(i);
				i--;
				continue;
			}
		}
			
		// NOTE: we assume there is one word left. If there remain more than one words, we assume the last one of them is head
		int head = -1;				
		for ( int i=0; i<entWIndx.size(); i++ ){
			if ( head < entWIndx.get(i) )
				head = entWIndx.get(i);
		}
		
		if ( head < 0 && entWIndx.size() > 0 )
			head = entWIndx.get(0);
		
		if ( head == -1 )
			head=-1;
		
		return head;
	}
				

	/**
	 * 
	 * @param listOne
	 * @param listTwo
	 * @return
	 */
	public ArrayList<Integer> getCommonItems ( ArrayList<Integer> listOne, ArrayList<Integer> listTwo ) {
		
		ArrayList<Integer> listCommon = new ArrayList<Integer>();
	
		for ( int v=0; v<listOne.size(); v++ ) {
			
			if ( listTwo.size() > 0 && listTwo.contains(listOne.get(v)) )
				listCommon.add(listOne.get(v));
		}
			
			
		return listCommon;
	}

	
	/**
	 * Basic idea:
	 * Find the grandparents which have both the target nodes as grand children.
	 * Then, for each of those grandparents find the minimum number of connecting nodes to the target nodes. 
	 * These nodes together with the target nodes and the particular grandparent form the minimal sub-tree rooted
	 * at that grandparent.
	 * Once all such minimal sub-tree are computed, select the shortest one.
	 * 
	 * @param source
	 * @param dest
	 * @param listOfNodesAlreadyVisited
	 * @return
	 */
	public char findNodesBetCommonParentAndEntPairs( int source, int dest,
			ArrayList<Integer> listOfNodesAlreadyVisited, ArrayList<Integer> listOfLeastCommonParent ) {
	
		/**
		 * C - dest is child of source
		 * P - dest is parent of source
		 * M - none of them is parent/child of the other
		 * N - no path detected 
		 */
		
		//System.out.println(source + " " + dest);
		listOfNodesAlreadyVisited.add(source);
		
		if ( !listOfNodesAlreadyVisited.contains(dest) ) {
			if ( allNodesByWordIndex[source].childrenWordIndex.contains(dest) ) {
				listOfNodesAlreadyVisited.add(dest);
				return 'C';
			}
				 
			else if ( allNodesByWordIndex[source].parentWordIndexes.contains(dest) ) {
				listOfNodesAlreadyVisited.add(dest);
				return 'P';
			}
				
			else {
				for ( int p=0; p<allNodesByWordIndex[source].parentWordIndexes.size(); p++ ) {
					int parent = allNodesByWordIndex[source].parentWordIndexes.get(p);
					if (  !listOfNodesAlreadyVisited.contains(parent) ) {
						char dir = findNodesBetCommonParentAndEntPairs( parent, 
								dest, listOfNodesAlreadyVisited, listOfLeastCommonParent);
						
						if ( dir != 'N' ) {
							if ( dir == 'C' ) {
								listOfLeastCommonParent.add(parent);
								return 'M';
							}
							else if ( dir == 'P' )
								return 'P';
							else if ( dir == 'M' )
								return 'M';
						}
					}
				}
				
				for ( int c=0; c<allNodesByWordIndex[source].childrenWordIndex.size(); c++ ) {
					int child = allNodesByWordIndex[source].childrenWordIndex.get(c);
					if ( !listOfNodesAlreadyVisited.contains(child) ) {
						char dir = findNodesBetCommonParentAndEntPairs( child, 
								dest, listOfNodesAlreadyVisited, listOfLeastCommonParent);				
						
						if ( dir != 'N' ) {		
							if ( dir == 'C' )
								return 'C';
							else if ( dir == 'P' ) {
								listOfLeastCommonParent.add(source);
								return 'M';
							}
							else if ( dir == 'M' )
								return 'M';
						}
					}
				}
			}
		}	
				
		listOfNodesAlreadyVisited.remove((Integer) source);
		 
		return 'N';
	}

}
