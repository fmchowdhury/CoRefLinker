package Knowledgebase;

import java.util.ArrayList;
import java.util.Hashtable;

import Utility.FileUtility;

public class HumanList {

	final static String[] maleList = new String("he him himself his").split(" ");
	  final static String[] femaleList = new String("she her herself").split(" ");
	  final static String[] thirdPersonList = new String("he him himself his she her herself they them their themselves it its itself").split(" ");
	  final static String[] secondPersonList = new String("you your yourself yourselves").split(" ");
	  final static String[] firstPersonList = new String("i me my myself we us our ourselves").split(" ");
	  final static String[] list = new String(
	      "i me myself my we us ourselves our they them themselves their").split(" ");

	  final static String[] listPluralVerb = new String("are have were do").split(" ");
	  final static String[] listSingularVerb = new String("is am has was does").split(" ");

	  final static String[] singularPronounList = new String(
		      "i me myself my he him himself his she her herself it its itself").split(" ");
	  final static String[] pluralPronounList = new String(
	      "we us ourselves our they them themselves their").split(" ");
	  
	  final static String[] detList = new String(
		      "this these those the all none either neither that which what some many few any both a an").split(" ");
	  	  
	  final static String[] wholeList = new String("he him himself his she her herself"
	                                              +" i me myself my we us ourselves our you your yourself").split(" ");
	  final static String[] complementList = new String("it its itself").split(" ");
	  final static String[] auxZList = new String("is does has was").split(" ");
	  
	  final static String[] maleTitleList = new String("Mr.").split(" ");
	  final static String[] femaleTitleList = new String("Mrs. Miss Ms.").split(" ");
	  
	  
	  final static String[] listPersonalPronoun = new String[] {"i", "me", "myself", "mine", "my", "we", "us", "ourselves", "ourself", "ours", "our", "you", "yourself", "yours", "your", "thou", "thee", "thyself", "thine", "thy", "thine", 
		  	"yourselves", "he", "him", "himself", "hisself", "his", "she", "her", "herself", "hers",
		  	"it", "itself", "its", "one", "oneself", 
		  	"one's", "they", "them", "themself", "themselves", "theirself", "theirselves", "theirs", "their"};
	  
	  final static String[] listOfPossessiveAdjectives = "my your his her its our their whose".split("\\s+"); 

	  final static String[] listStopWords = 
		  "a about above across after again against all almost alone along already also although always among an and another any anybody anyone anything anywhere are area areas around as ask asked asking asks at away b back backed backing backs be became because become becomes been before began behind being beings best better between big both but by c came can cannot case cases certain certainly clear clearly come could d did differ different differently do does done down down downed downing downs during e each early either end ended ending ends enough even evenly ever every everybody everyone everything everywhere f face faces fact facts far felt few find finds first for four from full fully further furthered furthering furthers g gave general generally get gets give given gives go going good goods got great greater greatest group grouped grouping groups h had has have having he her here herself high high high higher highest him himself his how however i if important in interest interested interesting interests into is it its itself j just k keep keeps kind knew know known knows l large largely last later latest least less let lets like likely long longer longest m made make making man many may me member members men might more most mostly mr mrs much must my myself n necessary need needed needing needs never new new newer newest next no nobody non noone not nothing now nowhere number numbers o of off often old older oldest on once one only open opened opening opens or order ordered ordering orders other others our out over p part parted parting parts per perhaps place places point pointed pointing points possible present presented presenting presents problem problems put puts q quite r rather really right right room rooms s said same saw say says second seconds see seem seemed seeming seems sees several shall she should show showed showing shows side sides since small smaller smallest so some somebody someone something somewhere state states still still such sure t take taken than that the their them then there therefore these they thing things think thinks this those though thought thoughts three through thus to today together too took toward turn turned turning turns two u under until up upon us use used uses v very w want wanted wanting wants was way ways we well wells went were what when where whether which while who whole whose why will with within without work worked working works would x y year years yet you young younger youngest your yours z"
		  .split("\\s+");

	  final static int numberOfNameToCheck = -1; //3000; //check only the first xx most common first names, respectively
	//  final static Hashtable maleNameTb = getNameTb(System.getProperty("dataPath") + File.separator +"male_first.txt",numberOfNameToCheck);
/*
	  final static Hashtable maleNameTb = getNameTb(System.getProperty("dataPath") + File.separator +"MostCommonMaleFirstNamesInUS.mongabay.txt",numberOfNameToCheck);
	  final static Hashtable femaleNameTb = getNameTb(System.getProperty("dataPath") + File.separator +"female_first.txt",numberOfNameToCheck);
	  final static Hashtable humanOccupationTb = getNameTb(System.getProperty("dataPath") + File.separator +"personTitle.txt");
	  final static Hashtable lastNameTb = getNameTb(System.getProperty("dataPath") + File.separator +"name_last.txt");
*/
	  
	  

	  public HumanList() {

		  
	  }
	  
	  /**
	   * 
	   * @param str
	   * @return
	   */
	  public static boolean hasPossesiveAdjectives ( String str ) {
		  		  
		  String[] words = str.toLowerCase().split("\\s+");
		  if ( words.length > 1 )
			  for ( int i=0; i<words.length; i++ )
				  if ( contains(listOfPossessiveAdjectives, words[i]) )
				  	return true;
				  	
		  return false;
	  }

	  public static boolean isStopWord ( String word ) {
		  return contains(listStopWords, word.toLowerCase());  
	  }

	  public static boolean isFoundInPronounList ( String wd ) {
		  wd = wd.toLowerCase();
		  return contains(firstPersonList, wd) || contains(secondPersonList, wd)
				  || contains(thirdPersonList, wd);
	  }
	  
	  public static boolean isFoundInPersonalPronounList ( String wd ) {
		  wd = wd.toLowerCase();
		  return contains(listPersonalPronoun, wd);
	  }
	  
	  
	  public static boolean isPluralVerb ( String wd ) {
		  wd = wd.toLowerCase();
		  return contains(listPluralVerb, wd);
	  }
	  
	  public static boolean isSingularVerb ( String wd ) {
		  wd = wd.toLowerCase();
		  return contains(listSingularVerb, wd);
	  }

	  public static boolean isFoundInDeterminerList ( String wd ) {
		  wd = wd.toLowerCase();
		  return contains(detList, wd);
	  }
	  
	  public static boolean isMale(String wd) {
		  String[] str = wd.toLowerCase().split("\\s+");
		  
		  for ( int i=0; i<str.length; i++ )				  
			  if ( contains(maleList, str[i]) || contains(maleTitleList, str[i]) )
				  return true;
	    
		  return false;
	    
	    // TODO: read from gazzeters
	  //People's name should start with a capital letter
	    //|| (wd.matches("[A-Z][a-z]*") && contains(maleNameTb,wd)) ;
	  }
	  
	  
	  public static boolean hasHumanTitle(String wd) {
		  String[] str = wd.toLowerCase().split("\\s+");
		  
		  for ( int i=0; i<str.length; i++ )				  
			  if ( contains(femaleTitleList, str[i]) || contains(maleTitleList, str[i]) ||
					 str[i].equalsIgnoreCase("dr.") || str[i].equalsIgnoreCase("m.d.")
					 || str[str.length-1].equalsIgnoreCase("m.d.") )
				  return true;
	    
		  return false;
	  }

	  public static boolean isFemale(String wd) {
		  String[] str = wd.toLowerCase().split("\\s+");
		  
		  for ( int i=0; i<str.length; i++ )				  
			  if ( contains(femaleList, str[i]) || contains(femaleTitleList, str[i]) )
				  return true;
	    
		  return false;
	    // TODO: read from gazzeters
		  //People's name should start with a capital letter
	    //|| (wd.matches("[A-Z][a-z]*") && contains(femaleNameTb,wd));		  
	  }

	  public static boolean isHuman(String wd) {
	   
		  if ( isMale(wd) || isFemale(wd) || isFirstPerson(wd) || isSecondPerson(wd)
				  ||  wd.toLowerCase().matches("whose|who"))
			  return true;
		  
		  return false;
	  }

	  public static boolean isNotHuman(String wd) {
		  wd = wd.toLowerCase();		  
		  return contains(complementList, wd) || wd.matches("this|that|these|those|which");
	  }

	  public static boolean isPlural(String wd){
		  wd = wd.toLowerCase();
		  return contains(pluralPronounList,wd);
	  }


	  public static boolean isSingular(String wd){
		  wd = wd.toLowerCase();
		  return contains(singularPronounList,wd) || isMale(wd) || isFemale(wd);
	  }

	  public static boolean isThirdPerson(String wd){
		  wd = wd.toLowerCase();
		  return contains(thirdPersonList,wd);
	  }

	  public static boolean isSecondPerson(String wd) {
		  wd = wd.toLowerCase();
		  return contains(secondPersonList, wd);
	  }


	  public static boolean isFirstPerson(String wd){
		  wd = wd.toLowerCase();
		  return contains(firstPersonList,wd);
	  }


	  //public static boolean isHumanTitle(String wd){
	    //return contains(humanTitleTb,wd.toLowerCase());
	  //}

	public static boolean contains(String[] list, String str) {
			str = str.toLowerCase();
		  return contains(list,str,false);
	  }

	  public static boolean contains(String[] list, String str, boolean caseSensitive) {
		  boolean contain = false;

	    if(caseSensitive){ //make this a outer check for efficiency's sake
	      for (int i = 0; i < list.length; i++) {
	         if (list[i].equals(str)) {
	           contain = true;
	           break;
	         }
	        }
	    }else{
	      for (int i = 0; i < list.length; i++) {
	        if (list[i].equalsIgnoreCase(str)) {
	          contain = true;
	          break;
	        }
	     }
	    }

	    return contain;
	  }

	  public static boolean contains(Hashtable tb, String wd){
		  wd = wd.toLowerCase();
		  
	    return tb.containsKey(wd);
	  }

	  private static String[] retriveList(String listFile){
	    return FileUtility.readFileContents(listFile).split("\\s+");
	  }

	  private static Hashtable getNameTb(String listFile){
	    return getNameTb(listFile,-1);
	  }

	  private static Hashtable getNameTb(String listFile,int range){
	     String[] nameArray = retriveList(listFile);
	     Hashtable tb = new Hashtable();

	     if(nameArray.length <=0){
	       System.err.println(listFile +" not found. Please download the latest data files. \n System quit.");
	       System.exit(0);
	     }

	     if(nameArray != null){
	        int stopAt;
	        if(range == -1){
	          stopAt = nameArray.length;
	        }else{
	          stopAt = Math.min(range,nameArray.length);
	        }
	        for (int i = 0; i<stopAt; i++){
	          String name = nameArray[i].substring(0,1);
	          if(nameArray[i].length()>1){
	            name += nameArray[i].substring(1).toLowerCase();
	          }
	          tb.put(name,name);
	        }
	     }
	     return tb;
	  }



}

