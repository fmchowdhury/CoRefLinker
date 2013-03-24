package Utility;

import java.util.ArrayList;

import Objects.NPMention;


public class DataStrucUtility {

	/**
	 * 
	 * @param a
	 * @param n
	 */
	public static String[] resizeArrayToNitems( String[] a, int n){
		
		if ( a.length == n )
			return a;
		
		String[] b = new String[n];
		
		for ( int i=0; i<n; i++ )
			b[i] = a[i];
		
		return b;
	}
	
	/**
	 * 
	 * @param a
	 * @param n
	 */
	public static int[] resizeArrayToNitems( int[] a, int n){
		
		if ( a.length == n )
			return a;
		
		int[] b = new int[n];
				
		for ( int i=0; i<n; i++ )
			b[i] = a[i];
		
		return b;
	}
	
	public static String[] listToStringArray ( ArrayList<String> temp ){
		if ( temp != null )
			return (String []) temp.toArray (new String [temp.size()]);
		
		return new String [0];
	}
	
	public static String[][] listToArrayOfString ( ArrayList<String[]> temp ){
		if ( temp != null )
			return (String [][]) temp.toArray (new String [temp.size()][]);
		
		return new String [0][];
	}
	
	public static int[][] listToArrayOfInt ( ArrayList<int[]> temp ){
		if ( temp != null )
			return (int [][]) temp.toArray (new int [temp.size()][]);
		
		return new int [0][];
	}
	
	public static int[] listToArray ( ArrayList<Integer> temp ){
		
		if ( temp == null )
			return new int [0];
		                
		Integer[] arrInteger = (Integer []) temp.toArray (new Integer [temp.size()]);
		int[] arrInt = new int[arrInteger.length];
		
		for ( int i=0; i<arrInteger.length; i++ )
			arrInt[i] = arrInteger[i];
		
		return arrInt;
	}
	
	public static double[] listToArray ( ArrayList<Double> temp ){
		
		if ( temp == null )
			return new double [0];
		                
		Double[] arrDbl = (Double []) temp.toArray (new Double [temp.size()]);
		double[] tmparr = new double[arrDbl.length];
		
		for ( int i=0; i<arrDbl.length; i++ )
			tmparr[i] = arrDbl[i];
		
		return tmparr;
	}
	
	public static ArrayList<String> arrayToList ( String[] temp ){
		ArrayList<String> tempList = new ArrayList<String>();
		
		for ( int i=0; i<temp.length; i++ )
			tempList.add(temp[i]);
		
		return tempList;
	}
	
	public static ArrayList<String[]> arrayToList ( String[][] temp ){
		ArrayList<String[]> tempList = new ArrayList<String[]>();
		
		for ( int i=0; i<temp.length; i++ )
			tempList.add(temp[i]);
		
		return tempList;
	}
	
	public static ArrayList<Integer> arrayToList ( int[] temp ){
		ArrayList<Integer> tempList = new ArrayList<Integer>();
		
		for ( int i=0; i<temp.length; i++ )
			tempList.add(temp[i]);
		
		return tempList;
	}

	public static ArrayList<Double> arrayToList ( double[] temp ){
		ArrayList<Double> tempList = new ArrayList<Double>();
		
		for ( int i=0; i<temp.length; i++ )
			tempList.add(temp[i]);
		
		return tempList;
	}

	public static ArrayList<Integer> listCopy( ArrayList<Integer> sourceList ){
		ArrayList<Integer> targetList = new ArrayList<Integer>();
		
		for ( int i=0; i<sourceList.size(); i++ )
			targetList.add(sourceList.get(i));
		
		return targetList;
	}
	
	
	/**
	 * 
	 * @param listStr
	 * @param separator
	 * @return
	 */
	public static StringBuilder arrayListToStringBuilder ( ArrayList<String> listStr, String separator ){
		
		StringBuilder sb = new StringBuilder();
		for ( int i=0; i<listStr.size(); i++ )
			sb.append(listStr.get(i)).append(separator);		
		
		return sb;
	}
	
	
	/**
	 * 
	 * @param listOne
	 * @param listTwo
	 * @return
	 */
	public static boolean hasListOneAllElementsOfListTwo ( ArrayList<String> listOne, ArrayList<String> listTwo ) {
		ArrayList<Integer> listIndexOfElementMatches = new ArrayList<Integer>();
		
		for ( int k=0; k<listTwo.size(); k++ ) {
			int r=0;
			
			for ( ; r<listOne.size(); r++ ) {
				if ( listOne.get(r).equals(listTwo.get(k))
						// the following constrain is for special case such as [nn,nn,nsubj], 
						// i.e. when a list has duplicate elements
						&& !listIndexOfElementMatches.contains(r) ) {
					listIndexOfElementMatches.add(r);
					break;
				}
			}
		
			// if the k-th element of list 2 is not found in list 1
			if ( r == listOne.size() )
				break;
		}
		
		// if all elements of list 2 is found in list 1
		if ( listIndexOfElementMatches.size() == listTwo.size() ) {
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * 
	 * @param listSource
	 * @param listOther
	 */
	public static void removeItemsOfListOneWhichAreAlreadyInListTwo ( ArrayList<Integer> listSource, ArrayList<Integer> listOther ) {
				
		for ( int v=0; v<listSource.size(); v++ ) {
			
			if ( listOther.size() > 0 && listOther.contains(listSource.get(v)) ){
				listSource.remove(v);
				v--;
			}
		}
	}
	
	/**
	 * This method is updated at 12-Apr-2012
	 * 
	 * @param listSource
	 * @param listOther
	 * @return
	 */
	public static ArrayList<Integer> getCommonItems ( ArrayList<Integer> listSource, ArrayList<Integer> listOther ) {
		
		ArrayList<Integer> listCommon = new ArrayList<Integer>();
		
		for ( int v=0; v<listSource.size() && listOther.size() > 0; v++ ) {
			
			if ( !listCommon.contains(listSource.get(v)) && listOther.contains(listSource.get(v)) )
				listCommon.add(listSource.get(v));
		}
		
		return listCommon;
	}
	
	
	public static boolean hasCommonItems ( ArrayList<Integer> listSource, ArrayList<Integer> listOther ) {
		
		for ( int v=0; v<listSource.size() && listOther.size() > 0; v++ ) {
			
			if ( listOther.contains(listSource.get(v)) )
				return true;
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param listSource
	 * @param listOther
	 * @return
	 */
	public static ArrayList<Integer> getUnCommonItems ( ArrayList<Integer> listSource, ArrayList<Integer> listOther ) {
		
		ArrayList<Integer> listCommon = getCommonItems(listSource, listOther);
		ArrayList<Integer> listUnCommon = new ArrayList<Integer>();
		
		listUnCommon.addAll(listSource);
		listUnCommon.addAll(listOther);
		
		for ( int v=0; v<listCommon.size(); v++ ) {
			
			if ( listUnCommon.contains(listCommon.get(v)) )
				listUnCommon.remove(listCommon.get(v));
		}
		
		return listUnCommon;
	}
	
	
	/**
	 * 
	 * @param listSource
	 * @param listOther
	 * @return
	 */
	public static ArrayList<Integer> getCommonItems ( int[] listSource, int[] listOther ) {
		
		ArrayList<Integer> listCommon = new ArrayList<Integer>();
		
		for ( int v=0; v<listSource.length; v++ ) {
			for ( int s=0; s < listOther.length; s++ ) {			
				if ( !listCommon.contains(listSource[v]) && listOther[s] == listSource[v] )
					listCommon.add(listSource[v]);
			}
		}
		
		return listCommon;
	}
	
	/**
	 * 
	 * @param listSource
	 * @param listOther
	 * @return
	 */
	public static ArrayList<String> getCommonStringItems ( ArrayList<String> listSource, ArrayList<String> listOther ) {
		
		ArrayList<String> listCommon = new ArrayList<String>();
		
		for ( int v=0; v<listSource.size() && listOther.size() > 0; v++ ) {
			
			if ( !listCommon.contains(listSource.get(v)) && listOther.contains(listSource.get(v)) )
				listCommon.add(listSource.get(v));
		}
		
		return listCommon;
	}
	
	/**
	 * 
	 * @param listSource
	 * @param listOther
	 * @return
	 */
	public static boolean hasCommonStringItems ( ArrayList<String> listSource, ArrayList<String> listOther ) {
		
		for ( int v=0; v<listSource.size() && listOther.size() > 0; v++ ) {
			
			if ( listOther.contains(listSource.get(v)) )
				return true;
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param listOfItems
	 */
	public static ArrayList<Integer> removeDuplicateItems ( ArrayList<Integer> listOfItems ) {
		
		for ( int v=0; v<listOfItems.size()-1; v++ ) {
			for ( int x=v+1; x<listOfItems.size(); x++ ) {
				if ( listOfItems.get(v) == listOfItems.get(x) ) {
					listOfItems.remove(x);
					x--;
				}
			}
		}
		
		return listOfItems;
	}
	
	/**
	 * 
	 * @param listOfIntegers
	 * @return
	 */
	public static ArrayList<Integer> sort ( ArrayList<Integer> listOfIntegers ) {
		
		return arrayToList(sort(DataStrucUtility.listToArray(listOfIntegers)));
	}
	
	/**
	 * 
	 * @param arrOfInt
	 * @return
	 */
	public static int[] sort ( int[] arrOfInt ) {
		
		for ( int i=0; i<arrOfInt.length-1; i++ ) {
			for ( int k=i+1; k<arrOfInt.length; k++ ) {
				
				if ( arrOfInt[i] > arrOfInt[k] ) {
					int x = arrOfInt[i];
					arrOfInt[i] = arrOfInt[k];
					arrOfInt[k] = x;
				}
			}
		}
		
		return arrOfInt;
	}

	/**
	 * 
	 * @param listOfIntegers
	 * @return
	 */
	public static ArrayList<Integer> sortAndRemDuplicate ( ArrayList<Integer> listOfIntegers ) {
				
		for ( int i=0; i<listOfIntegers.size(); i++ ) {
			int k = -1;
			while ( (k=listOfIntegers.lastIndexOf(listOfIntegers.get(i))) != i ) {
				listOfIntegers.remove(k);
			}
		}
		
		return sort(listOfIntegers);
	}
	
	
	public static int[] sortAndRemDuplicate ( int[] arrOfInt ) {
		
		return listToArray(sortAndRemDuplicate(DataStrucUtility.arrayToList(arrOfInt)));
	}
	
	
	
	/**
	 * 
	 * @param arrOfInt
	 * @param item
	 * @return
	 */
	public static boolean contains( int[] arrOfInt, int item ) {
		
		for ( int i=0; i<arrOfInt.length; i++ )
			if ( arrOfInt[i] == item )
				return true;
		
		return false;
	}
	
	/**
	 * 
	 * @param arrOfInt
	 * @param item
	 * @return
	 */
	public static int[] add( int[] arrOfInt, int item ) {
		
		ArrayList<Integer> tmp = DataStrucUtility.arrayToList(arrOfInt);
		tmp.add(item);
		
		return listToArray(tmp);
	}
	
	public static int[] addAll( int[] arrOfInt, int[] newItems ) {
		
		ArrayList<Integer> tmp = DataStrucUtility.arrayToList(arrOfInt);
		ArrayList<Integer> tmp2 = DataStrucUtility.arrayToList(newItems);
		tmp.addAll(tmp2);
		
		return listToArray(tmp);
	}

	public static double[] add( double[] arrOfDbl, double item ) {
		
		ArrayList<Double> tmp = DataStrucUtility.arrayToList(arrOfDbl);
		tmp.add(item);
		
		return listToArray(tmp);
	}
}
