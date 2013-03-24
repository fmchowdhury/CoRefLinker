package Structures;

import java.util.ArrayList;
import java.util.Hashtable;

public class FeatureList {

	public static Hashtable<String, Integer> globalListOfFeatureNamesAndIndex = new Hashtable<String, Integer>();
	public static ArrayList<String> globalListOfFeatureNames = new ArrayList<String>();
	public static ArrayList<Integer> globalListOfFeatureIndexes = new ArrayList<Integer>();
	
	public static String prefForAntecedent = "antec", prefForAnaphora = "anph";
}
