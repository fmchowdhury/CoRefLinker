package Knowledgebase;

import java.util.Arrays;
import java.util.HashSet;

public class ModalAdjectives {

	  private static final HashSet adj = new HashSet(Arrays.asList(
	      ("announced necessary possible certain likely important good useful "
	       +"advisable convenient sufficient economical easy desirable difficult legal perfect "
	       +"unnecessary impossible uncertain unlikely unimportant bad useless "
	       +"inadvisable inconvenient insufficient uneconomical hard undesirable illegal imperfect "
	       +"better best easier easiest worse worst harder hardest "
	       +"recommended think believe know known anticipate assume expect "
	       +"appreciate correct clear follows nice understood thanks time").split(" ")));

	  public static boolean contains(String word){
	    return adj.contains(word);
	  }
  
}
