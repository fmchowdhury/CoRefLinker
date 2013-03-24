package Objects;

public class NP {

	
	
	 /*Type*/
	  public final static int DEF = 1; //definite NP;
	  public final static int PLEO = 2; //pleonastic pronoun;
	  public final static int PRON = 3; //other pronoun;
	  public final static int INDEF = 4; //indefinite NP;
	  

	  //Indicates whether this NP is part of a "NNX (NNX)+" combination
	  //The probability of such a NP being a good antecedent of an anaphor plumbs.
	  boolean hasNNXsibling = false;
	  
	  //distance between the beinning of the sentence and the first word in UNIT;
	  int offset;
	  
	  public void setHasNNXsibling(boolean b){
	    hasNNXsibling = b;
	  }

	  public boolean hasNNXsibling(){
	    return hasNNXsibling;
	  }
	  

	  

	  
	  
}
