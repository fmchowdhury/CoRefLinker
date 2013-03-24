package Knowledgebase;

import java.util.ArrayList;

import Objects.DataFile;
import Objects.NPMention;
import Objects.StringSimilarity;
import Objects.NPMention.eHumanMention;
import Structures.Sentence;
import Utility.TextUtility;

public class LingusiticAnalyzer {

	static String regExForTimePrep = "(in|on|at|from|since|till|until)";
	
	/**
	 * 
	 * @param sen
	 * @return
	 */
	public static String getTemporalExpressionDateTime ( NPMention mn, DataFile df ) {
		// containsTemporalExpression
		
		String sen = df.listOfSentences.get(mn.senIndx).text.toLowerCase();
	
		String[] pattern = {
				"[0-9]{1,2}:?[0-9]{1,2}\\s*[ap]\\.?m\\.?", // date time
				"[0-9]{4}[-/][0-9]{1,2}[-/][0-9]{1,2}", // date
				"[0-9]{1,2}[-/][0-9]{1,2}[-/][0-9]{4}", // date
				regExForTimePrep + "\\s+[0-9]{1,2}[-][0-9]{1,2}", // date
				"\\s+[0-9]{1,2}\\s*[ap]\\.?m\\.?", // time
				};
	
		
		for ( int i=0; i<pattern.length; i++ ) {
			ArrayList<String> str = TextUtility.returnMatchedString(sen, pattern[i]);
			if (  str != null && str.size() > 0 )
				return str.get(0).replaceAll(regExForTimePrep, "");
		}
		
		return "";
	}
	
	
	/**
	 * 
	 * @param mn
	 * @param df
	 * @return
	 */
	public static String getTemporalExpressionYearMonth ( NPMention mn, DataFile df ) {
		// containsTemporalExpression
		
		String sen = df.listOfSentences.get(mn.senIndx).text.toLowerCase();
	
		String[] pattern = {
				regExForTimePrep + "\\s+[0-9]{4}",
				"(january|february|march|april|may|june|july|august|september|october|november|december)",
			//	"[^a-zA-Z](day|month|year)(s)?[^a-zA-Z]"
				};
	
		
		for ( int i=0; i<pattern.length; i++ ) {
			ArrayList<String> str = TextUtility.returnMatchedString(sen, pattern[i]);
			if (  str != null && str.size() > 0 ) {
				return str.get(0).replaceAll(regExForTimePrep, "");
			}
		}
		
		return "";
	}
	
	
	/**
	 * 
	 * @param mn
	 * @param df
	 * @return
	 */
	public static ArrayList<String> getPositionCue ( NPMention mn, DataFile df ) {
		
		String[][] arrPositions = new String[][] {{"left", "right", "center"},
				{"upper", "lower"},
				{"small", "medium", "big", "large"}
		};
		
		ArrayList<String> tmpListX = new ArrayList<String>();
		String name =  mn.name.toLowerCase();
		
		for ( int p=0; p<arrPositions.length; p++ ) {
			for ( int i=0; i<arrPositions[p].length; i++ ) {
				if ( name.matches(".*" + arrPositions[p][i] + ".*") )
					tmpListX.add(arrPositions[p][i]);
			}
		}
		
		return tmpListX;
	}
	
	
	
	/**
	 * 
	 * @param np
	 * @param df
	 * @return
	 */
	public static String getQuantityAndFrequency ( NPMention np, DataFile df ) {
	
		if ( np.type.equalsIgnoreCase("treatment") ) {
			
			String str = "";
			
			for ( int i=np.eTokIndxBySpace+1; i<df.listOfSentences.get(np.senIndx).arrWordBySpace.length && i<np.eTokIndxBySpace+6 ; i++ ) 
				str = str + df.listOfSentences.get(np.senIndx).arrWordBySpace[i];
			
			str = str.toLowerCase();
			
			String pattern = "[0-9]+.*(daily|weekly|aday|perweek)";
			if ( str.matches(pattern) ) {
				return TextUtility.returnMatchedString(str, pattern).get(0);
			}
			
			// pattern
			str = "";
			for ( int i=np.eTokIndxBySpace+1; i<df.listOfSentences.get(np.senIndx).arrWordBySpace.length && i<np.eTokIndxBySpace+4 ; i++ ) 
				str = str + " " + df.listOfSentences.get(np.senIndx).arrWordBySpace[i];
			str = str + " "; 
			
			pattern = "([0-9]+.*)*(h\\.s\\.|ac|pc|q|q\\.d\\.|b\\.i\\.d\\.|t\\.i\\.d\\.|q\\.i\\.d\\.|q\\.o\\.d\\.)";
			if ( str.matches(pattern) ){
				return TextUtility.returnMatchedString(str, pattern).get(0);
			}
			
			// for other clues which should appear immediately after
			if ( np.eTokIndxBySpace+2 < df.listOfSentences.get(np.senIndx).arrWordBySpace.length )
				str = df.listOfSentences.get(np.senIndx).arrWordBySpace[np.eTokIndxBySpace+1] + 
					df.listOfSentences.get(np.senIndx).arrWordBySpace[np.eTokIndxBySpace+2];
			
			pattern = "[0-9./]+(mg|ml|unit)";
			if ( str.matches(pattern) ) {
				return TextUtility.returnMatchedString(str, pattern).get(0);
			}
			
		}
				
			/*
			h.s.: at hour of sleep (bedtime)
			ac: before meals
			pc: after meals
			q: every, ie, q 8 h means every 8 hours
			q.d.: every day
			b.i.d.: twice/day
			t.i.d.: three times/day
			q.i.d.: four times/day
			q.o.d.: every other day
			*/
		
		return "";
	}
	
	
	/**
	 * 
	 * @param np
	 * @param df
	 * @return
	 */
	public static String getAdminMode ( NPMention np, DataFile df ) {
		
		if ( np.humanMention != eHumanMention.YES ) {
		//	System.out.println(np.toStringFull());
			ArrayList<Integer> listOfFoundModeIndexes = new ArrayList<Integer>();
			ArrayList<String> listOfFoundModes = new ArrayList<String>();
			String[] modes = new String[] {"osseous", "cerebral", "cerebroventricular", "(oral|mouth)", "buccal", "sublingual",
					"subcutaneous", "(intradermal|intracutaneous)", "muscular", "venous", "(thecal|spinal)", "inhalation", 
					"vaporization", "nebulization", "(topical|epicutaneous)", "rectal", "vaginal", "synovial", "atrticular", 
					"cardiac", "arterial", "conjunctival", "ocular", "aural", "nasal", "respiratory", "urethra", "transdermal", 
					"transmucosal", "peritoneal", "vesical", "vitreal", "cavernous"};
			
			if ( np.senIndx != np.eSenIndx )
				return "";
			
			for ( int i=np.sTokIndxBySpace-5>=0? np.sTokIndxBySpace : 0; 
					i<df.listOfSentences.get(np.senIndx).arrWordBySpace.length && i < np.eTokIndxBySpace+5; i++ )
				for ( int k=0; k<modes.length; k++ )
					if ( df.listOfSentences.get(np.senIndx).arrWordBySpace[i].length() > 3 &&
							df.listOfSentences.get(np.senIndx).arrWordBySpace[i].toLowerCase().matches(".*" + modes[k] + ".*") ) {
						listOfFoundModeIndexes.add(i);
						listOfFoundModes.add(modes[k]);
					}
			
			if ( listOfFoundModes.size() == 1 )
				return listOfFoundModes.get(0);
			else if ( listOfFoundModes.size() > 1 ) {
				int n = 100, ni = 0;
				// find the nearest one
				for ( int i=0; i<listOfFoundModeIndexes.size(); i++ ) {
					int x = Math.abs(np.sTokIndxBySpace-listOfFoundModeIndexes.get(i));
					x = Math.abs(np.eTokIndxBySpace-listOfFoundModeIndexes.get(i)) < x 
						? Math.abs(np.eTokIndxBySpace-listOfFoundModeIndexes.get(i)) : x;
						
					if ( x < n ) {
						n = x;
						ni = i;
					}
						
				}
					
				return listOfFoundModes.get(ni);
			}
		}
		
		return "";			
	}
	
	
	/**
	 * 
	 * @param np
	 * @param df
	 * @return
	 */
	public static String getPhysicalLocation ( NPMention np, DataFile df ) {
		
		if ( np.humanMention != eHumanMention.YES ) {
			
		//	System.out.println(np.toStringFull());
			ArrayList<Integer> listOfFoundOrganIndexes = new ArrayList<Integer>();
			ArrayList<String> listOfFoundOrgans = new ArrayList<String>();
			String[] organs = new String[] {"basal", "medulla", "midbrain", "pons", "cerebellum", "cerebral", 
					"hypothalamus", "limbic", "amygdala", "eye", "pineal", "pituitary", "thyroid", "parathyroid", 
					"heart", "lung", "esophagus", "thymus", "pleura", "adrenal", "appendix", "bladder", "gallbladder", 
					"intestine", "kidney", "liver", "pancreas", "spleen", "stomach", "prostate", "testes", "ovaries", "uterus",
					
					 "back", "head", "forehead", "jaw", "cheek", "chin", "neck", "shoulder", "arm", "elbow", "wrist", "hand", "finger", "thumb", "spine", 
					 "chest", "thorax", "abdomen", "groin", "hip", "buttocks", "leg", "thigh", "knee", "calf", "heel", "ankle", 
					 "foot", "toe", "eye", "ear", "nose", "mouth", "teeth", "tongue", "throat", "adam", "breast", "penis", 
					 "scrotum", "clitoris", "vulva", "navel"};
			
			if ( np.senIndx != np.eSenIndx )
				return "";
			
			
			// checking from right to left since locations usually mentioned after the problem/treatment/test
			for ( int i=np.sTokIndxBySpace+4; i >= 0 && i >= np.eTokIndxBySpace; i-- ) {
				for ( int k=0; i<df.listOfSentences.get(np.senIndx).arrWordBySpace.length && k<organs.length; k++ )
					if ( df.listOfSentences.get(np.senIndx).arrWordBySpace[i].length() >= 3 &&
							df.listOfSentences.get(np.senIndx).arrWordBySpace[i].toLowerCase().matches(".*" + organs[k] + ".*") ) {
						listOfFoundOrganIndexes.add(i);
						listOfFoundOrgans.add(organs[k]);
					}
			}
			
			if ( listOfFoundOrgans.size() == 1 )
				return listOfFoundOrgans.get(0);
			else if ( listOfFoundOrgans.size() > 1 ) {
				int n = 100, ni = 0;
				// find the nearest one
				for ( int i=0; i<listOfFoundOrganIndexes.size(); i++ ) {
					int x = Math.abs(np.sTokIndxBySpace-listOfFoundOrganIndexes.get(i));
					x = Math.abs(np.eTokIndxBySpace-listOfFoundOrganIndexes.get(i)) < x 
						? Math.abs(np.eTokIndxBySpace-listOfFoundOrganIndexes.get(i)) : x;
						
					if ( x < n ) {
						n = x;
						ni = i;
					}
						
				}
					
				return listOfFoundOrgans.get(ni);
			}
		}
		
		return "";			
	}
	
	
	
	
	public void identifyPleonasticPronoun( Sentence sen, NPMention mn){
		
		/**
		 * TODO: For now we skip it 
		 */
		
		/*
		 * Patterns from Qiu et al. LREC 2004
		 * 1. it is Modaladj that S,
		 * 2. it is Modaladj (for NP) to VP,
		 * 3. it is Cogv-ed that S,
		 * 4. it seems / appears / means / follows (that) S,
		 * 5. NP makes / finds it Modaladj (for NP) to VP,
		 * 6. it is time to VP, and
		 * 7. it is thanks to NP that S,
		 * where Modaladj stands for a modal adjective and Cogv-ed stands for the passive participle of a cognitive verb.
		 * 
		 * Also consider syntactic variants -
		 * Syntactic variants of these patterns (it is not /may be Modaladj that..., wouldn’t it be Modaladj..., etc.)
		 */
		
		/*
		 * Rules from Segura-Bedmar et al. BMC Bioinformatics 2010
		 * IT [MODALVERB [NOT]?]? BE [NOT]? [AJD|ADV| VP]* [THAT|WHETHER]
		 * Example: It is not known whether other progestational contraceptives are adequate methods of contraception during acitretin therapy.
		 * IT [MODALVERB [NOT]?]? BE [NOT]? ADJ [FOR np] TO VP
		 * Example: If it is not possible to discontinue the diuretic, the starting dose of trandolapril should be reduced.
		 * IT [MODALVERB [NOT]]? [SEEM|APPEAR|MEAN|FOLLOW] [THAT] *
		 * Example: It does not appear that the SSRIs reduce the effectiveness of a mood stabilizer in these populations
		 */
	}
	/*
	private void identifyPleonasticPronoun(DefaultMutableTreeNode root, NPMention mn){
	    Enumeration enumeration = root.preorderEnumeration();

	    DefaultMutableTreeNode parentNode = null;
	    DefaultMutableTreeNode uncleNode = null;
	    DefaultMutableTreeNode orgPRPNode = null;
	    DefaultMutableTreeNode node = null;
	    DefaultMutableTreeNode NPnode = null;
	    DefaultMutableTreeNode siblingNode = null;
	    DefaultMutableTreeNode PrevSiblingNode = null;
	    DefaultMutableTreeNode nephewNode1 = null;
	    DefaultMutableTreeNode nephewNode2 = null;
	    DefaultMutableTreeNode nephewNode3 = null;
	    boolean isPleonastic = false;

	    while (enumeration.hasMoreElements()) {


	      node = (DefaultMutableTreeNode) enumeration.
	          nextElement();
	      TagWord tagWd = (TagWord) (node.getUserObject());
	      if(tagWd == null){
	        continue;
	      }

	      if( tagWd.getTag().equalsIgnoreCase("PRP") && tagWd.getText().equalsIgnoreCase("it")){
	        isPleonastic = false;

	        NPnode = (DefaultMutableTreeNode) node.getParent();
	        if(NPnode == null){
	          //never happens!
	          Util.errLog("Weird: (PRP it) has no parent");
	          System.exit(0);
	        }

	        parentNode = (DefaultMutableTreeNode) NPnode.getParent();
	        if(parentNode == null){
	          //never happens!
	          Util.errLog("Weird: (PRP it) has no grandparent");
	          System.exit(0);
	        }

	        uncleNode = (DefaultMutableTreeNode) parentNode.getPreviousSibling();

	        siblingNode = (DefaultMutableTreeNode) NPnode.getNextSibling();
	        if ((siblingNode != null) && (siblingNode.getChildCount()>0)) {

	          nephewNode1 = (DefaultMutableTreeNode) siblingNode.getChildAt(0);
	          nephewNode2 = (DefaultMutableTreeNode) nephewNode1.getNextSibling();
	          if(nephewNode2 !=null){
	            nephewNode3 = (DefaultMutableTreeNode) nephewNode2.getNextSibling();
	          }
	        }

	        PrevSiblingNode = (DefaultMutableTreeNode) NPnode.getPreviousSibling();

	        //identify pleonastic pronouns

	        //It is very necessary
	        //It is recommended that
	        if( (siblingNode != null)
	            && (((TagWord)siblingNode.getUserObject()).getTag().equalsIgnoreCase("VP"))
	            && (nephewNode1 != null)
	            && (((TagWord)nephewNode1.getUserObject()).getTag().equalsIgnoreCase("AUX"))
	            && (nephewNode2 != null)
	            &&
	            ( (((TagWord)nephewNode2.getUserObject()).getTag().equalsIgnoreCase("ADJP"))
	              || ((nephewNode3 != null) && (((TagWord)nephewNode3.getUserObject()).getTag().equalsIgnoreCase("ADJP")))
	             )
	            ){
	            DefaultMutableTreeNode adjpNode =  (((TagWord)nephewNode2.getUserObject()).getTag().equalsIgnoreCase("ADJP"))? nephewNode2: nephewNode3;
	            String[] words = ((TagWord)adjpNode.getUserObject()).getContent().split(" ");

	            for(int i = 0; i<words.length;i++){
	              if (ModalAdj.contains(words[i])){
	                  isPleonastic = true;
	                  break; //if
	              }
	            }
	        }

	        //really appreciate it
	        if( (PrevSiblingNode != null)
	            && (((TagWord)PrevSiblingNode.getUserObject()).getTag().startsWith("VB"))){
	            String[] words = ((TagWord)PrevSiblingNode.getUserObject()).getContent().split(" ");

	            for(int i = 0; i<words.length;i++){
	              if (ModalAdj.contains(words[i])){
	                  isPleonastic = true;
	                  break; //if
	              }
	            }
	        }



	        //it may/might be
	        if( (siblingNode != null)
	           && (((TagWord)siblingNode.getUserObject()).getTag().equalsIgnoreCase("VP"))
	           && (nephewNode1 != null)
	           && (((TagWord)nephewNode1.getUserObject()).getTag().equalsIgnoreCase("MD"))
	           && (nephewNode2 != null)
	           && (((TagWord)nephewNode2.getUserObject()).getTag().equalsIgnoreCase("VP"))){

	         if (nephewNode2.getChildCount()>1) {
	           DefaultMutableTreeNode subNode1 = (DefaultMutableTreeNode)nephewNode2.getChildAt(0);
	           DefaultMutableTreeNode subNode2 = (DefaultMutableTreeNode)nephewNode2.getChildAt(1);
	           if(((TagWord)subNode1.getUserObject()).getTag().equalsIgnoreCase("AUX")
	              && ((TagWord)subNode2.getUserObject()).getTag().equalsIgnoreCase("ADJP")){

	             String[] words = ( (TagWord) nephewNode2.getUserObject()).
	                 getContent().split(" ");

	             for (int i = 0; i < words.length; i++) {
	               if (ModalAdj.contains(words[i])) {
	                 isPleonastic = true;
	                 break; //if
	               }
	             }
	           }
	         }
	       }


	        // I will/could appreciate/ believe it
	        if( (siblingNode != null)
	            && (((TagWord)siblingNode.getUserObject()).getTag().equalsIgnoreCase("VB"))
	            && (uncleNode != null)
	            && (((TagWord)uncleNode.getUserObject()).getTag().equalsIgnoreCase("MD"))){

	          String[] words = ( (TagWord) siblingNode.getUserObject()).getContent().split(" ");

	          for (int i = 0; i < words.length; i++) {
	            if (ModalAdj.contains(words[i])) {
	              isPleonastic = true;
	              break; //if
	            }
	          }
	        }

	        //find it important
	        if( (siblingNode != null)
	            && (((TagWord)siblingNode.getUserObject()).getTag().equalsIgnoreCase("ADJP"))){
	          String[] words = ( (TagWord) siblingNode.getUserObject()).getContent().split(" ");

	          for (int i = 0; i < words.length; i++) {
	            if (ModalAdj.contains(words[i])) {
	              isPleonastic = true;
	              break; //if
	            }
	          }
	        }

	        //it is thanks to
	        if( (siblingNode != null)
	            && (((TagWord)siblingNode.getUserObject()).getTag().equalsIgnoreCase("VP"))
	            && (nephewNode1 != null)
	            && (((TagWord)nephewNode1.getUserObject()).getTag().equalsIgnoreCase("AUX"))
	            && (nephewNode2 != null)
	            && (((TagWord)nephewNode2.getUserObject()).getTag().equalsIgnoreCase("NP"))){
	            String[] words = ((TagWord)nephewNode2.getUserObject()).getContent().split(" ");

	            for(int i = 0; i<words.length;i++){
	              if (ModalAdj.contains(words[i])){
	                  isPleonastic = true;
	                  break; //if
	              }
	            }
	        }

	        //it follows that
	        if( (siblingNode != null)
	            && (((TagWord)siblingNode.getUserObject()).getTag().equalsIgnoreCase("VP"))
	            && (nephewNode1 != null)
	            && (((TagWord)nephewNode1.getUserObject()).getTag().startsWith("VB"))
	            && (nephewNode2 != null)
	            && (((TagWord)nephewNode2.getUserObject()).getTag().startsWith("S"))){

	            String word = ((TagWord)nephewNode1.getUserObject()).getContent();
	            if (ModalAdj.contains(word)) {
	              isPleonastic = true;
	            }
	        }


	        //it is time to
	        if ( (siblingNode != null)
	            &&( ( (TagWord) siblingNode.getUserObject()).getTag().equalsIgnoreCase("VP"))
	            && (nephewNode1 != null)
	            &&( ( (TagWord) nephewNode1.getUserObject()).getTag().equalsIgnoreCase("AUX"))
	            && (nephewNode2 != null)
	            &&( ( (TagWord) nephewNode2.getUserObject()).getTag().equalsIgnoreCase("NP"))) {


	         String[] words = ( (TagWord) nephewNode2.getUserObject()).getContent().split(" ");
	         if (ModalAdj.contains(words[0])) {
	           isPleonastic = true;
	         }
	        }

	        tagWd.setPleonastic(isPleonastic);
	        //set parent NP as pleonastic also
	        ((TagWord)NPnode.getUserObject()).setPleonastic(isPleonastic);

	      } //if it's (PRP it)
	    } ///~while

	  }

	*/
}
