package Objects;

import java.util.ArrayList;

import Knowledgebase.HumanList;
import Structures.DepGraphVertex;
import Structures.DependencyGraph;
import Structures.PhraseStrucTreeNode;
import Structures.PhraseStructureTree;
import Utility.TextUtility;

public class NPMention {

	public static ArrayList<String> listOfNPMentionTypes = new ArrayList<String>();
	
	public static enum eGender {
		MALE, FEMALE, NEUTRAL, UNKNOWN
	}
	
	public static enum eHumanMention {
		YES, NO, UNKNOWN
	}
	
	public static enum ePronoun {
		REFLEXIVE, POSSESSIVE, SUBJECTIVE, RECIPROCAL, PLEONASTIC, UNKNOWN
	}

	public static enum eNOUN {
		COMMON, PROPER, UNKNOWN
	}

	public static enum ePerson {
		FIRST, SECOND, THIRD, UNKNOWN
	}
	
	public static enum eNumber {
		SINGULAR, PLURAL, UNKNOWN
	}
	
	public static enum eClinicalEntity {
		Patient, Doctor, Family, Other, UNKNOWN
	}

	
	public String name = "", type = "", headWord = "", posOfHeadWord = "", lemmaOfHeadWord = "";
	public int senIndx = -1, eSenIndx = -1, sTokIndxBySpace = -1, eTokIndxBySpace = -1, headWordIndexByParser = -1;
	
	public ArrayList<Integer> listOfTokenIndxByParser = new ArrayList<Integer>();
	
	// If the mention is not pronoun then search up to the (grand)parent of the mention which has sub/obj dependency. 
	public ArrayList<Integer> listOfGovernerWordIndexesOfSubjObj = new ArrayList<Integer>();	
	public ArrayList<String> listOfGovernerRelTypeOfSubjObj = new ArrayList<String>();
	
	public boolean isExistential = false, isHeadNP = false, isInADVP = false, isSubject = false, isDeterminer = false, isObject = false;
	public boolean partOfNP = false, isSectionHeading = false;
	
	public eClinicalEntity clinicalEntityType = eClinicalEntity.UNKNOWN;
	
	public eNOUN headNounType = eNOUN.UNKNOWN;
	public eGender genderType = eGender.UNKNOWN;
	public eNumber numberType = eNumber.UNKNOWN;
	public ePerson personType = ePerson.UNKNOWN;
	public ePronoun pronounType = ePronoun.UNKNOWN;
	public eHumanMention humanMention = eHumanMention.UNKNOWN;
	
	/**
	 * 
	 */
	public String toString() {
		//c="patient" 21:0 21:0
		return "c=\"" + name + "\" " + (senIndx+1) + ":" + sTokIndxBySpace + " "
				+ (eSenIndx+1) + ":" + eTokIndxBySpace; 
	}
	
	/**
	 * 
	 */
	public String toStringFull() {
		//c="patient" 21:0 21:0
		return "c=\"" + name + "\" " + (senIndx+1) + ":" + sTokIndxBySpace + " "
				+ (eSenIndx+1) + ":" + eTokIndxBySpace + "||t=\"" + type + "\""; 
	}
	
	/**
	 * 
	 * @param headWordNode
	 */
	private void detectExistential( DepGraphVertex headWordNode ) {
		
		if ( isPronoun() && headWordNode.relNameWithParents.contains("expl") ) 
			isExistential = true;
	}	
	
	
	/**
	 * 
	 * @param df
	 */
	private void isPartOfNP( PhraseStructureTree psgTree ) {
		PhraseStrucTreeNode pstParent = psgTree.getParentPhraseNode(this.listOfTokenIndxByParser); 
		
		if ( pstParent.pos.contains("NP") // NP WHNP 
				&& pstParent.allTerminalNodeIndexesUnderThisNode.size() > this.listOfTokenIndxByParser.size() ) {
			//System.out.println(df.fileId + " " + mn.toStringFull());
			partOfNP = true;
		}
	}
	
	/**
	 * 
	 * @return
	 */
	private void isHumanMention() {
		
		if ( this.type.toLowerCase().matches("people|person") )			
			humanMention = eHumanMention.YES;
		else if ( this.name.toLowerCase().matches("(this|these|those|that|which|what)") )			
			humanMention = eHumanMention.NO;
		else if ( this.type.toLowerCase().matches("pronoun") && HumanList.isHuman(this.name) )			
			humanMention = eHumanMention.YES;
		else if ( !this.type.toLowerCase().matches("people|person|pronoun") || HumanList.isNotHuman(this.name) )
			humanMention = eHumanMention.NO;
		else if ( this.type.toLowerCase().matches("pronoun") && this.isDeterminer )
			humanMention = eHumanMention.NO;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isPreposition() {
		
		if ( this.eTokIndxBySpace == this.sTokIndxBySpace && HumanList.isFoundInDeterminerList(this.name)
				&& this.posOfHeadWord.equalsIgnoreCase("IN") ) 
			return true;
		
		return false;
	}	


	
	/**
	 * 
	 * @return
	 */
	public boolean hasPossesiveCase() {
		
		if ( this.eTokIndxBySpace > this.sTokIndxBySpace && 
				( HumanList.hasPossesiveAdjectives(this.name)
						// NOTE: using 's degrades the performance  slightly 
						// || this.name.contains("'s")
				) ) 
			return true;
		
		return false;
	}	
	
	/**
	 * 
	 * @return
	 */
	public boolean hasOF() {
		
		if ( this.eTokIndxBySpace > this.sTokIndxBySpace && this.name.contains("of") ) 
			return true;
		
		return false;
	}

	/**
	 * 
	 */
	private void detectDeterminer() {
		
		if ( this.eTokIndxBySpace == this.sTokIndxBySpace && HumanList.isFoundInDeterminerList(this.name)
				&& this.posOfHeadWord.toUpperCase().contains("DT") ) // i.e. DT or WDT
			isDeterminer = true;
	}
	
	/**
	 * 
	 * @param dg
	 */
	private void setGovernerWordIndexesOfSubjObj  ( DependencyGraph dg ) {
		listOfGovernerWordIndexesOfSubjObj.clear();
		for ( int p=0; p<dg.allNodesByWordIndex[headWordIndexByParser].relNameWithParents.size(); p++ ) {
			if ( dg.allNodesByWordIndex[headWordIndexByParser].relNameWithParents.get(p).contains("subj")
					|| dg.allNodesByWordIndex[headWordIndexByParser].relNameWithParents.get(p).contains("obj") ) {
				
				listOfGovernerWordIndexesOfSubjObj.add(dg.allNodesByWordIndex[headWordIndexByParser].parentWordIndexes.get(p));
				listOfGovernerRelTypeOfSubjObj.add(dg.allNodesByWordIndex[headWordIndexByParser].relNameWithParents.get(p));
			}
		}
		
		ArrayList<Integer> listConsidered = new ArrayList<Integer>();
		ArrayList<Integer> listParents = dg.allNodesByWordIndex[headWordIndexByParser].parentWordIndexes;		
		/*
		while ( listOfGovernerWordIndexesOfSubjObj.isEmpty() && !listParents.isEmpty() ) {
			
			ArrayList<Integer> listToBeConsidered = new ArrayList<Integer>();
			
			for ( int k=0; k<listParents.size(); k++ ) {
				// if the parent is not checked yet
				if ( !listConsidered.contains(listParents.get(k)) && !listToBeConsidered.contains(listParents.get(k)) ) {
					for ( int x=0; x<dg.allNodesByWordIndex[listParents.get(k)].relNameWithParents.size(); x++ ) {
						// search whether the parent has a subj/obj dependency
						if ( dg.allNodesByWordIndex[listParents.get(k)].relNameWithParents.get(x).contains("subj")
								|| dg.allNodesByWordIndex[listParents.get(k)].relNameWithParents.get(x).contains("obj") ) {
							listOfGovernerWordIndexesOfSubjObj.add(dg.allNodesByWordIndex[listParents.get(k)].parentWordIndexes.get(x));
							listOfGovernerRelTypeOfSubjObj.add(dg.allNodesByWordIndex[listParents.get(k)].relNameWithParents.get(x));
						}
						
					}
					
					listToBeConsidered.addAll(dg.allNodesByWordIndex[listParents.get(k)].parentWordIndexes);
				}
			}
			
			listConsidered.addAll(listParents);
			listParents = listToBeConsidered;
		}	
		*/		
	}
	
	/**
	 * 
	 * @param dg
	 */
	public void setHeadWord ( DependencyGraph dg, PhraseStructureTree psgTree ) {
		
		if ( listOfTokenIndxByParser.size() == 0 ) {
			System.err.println(TextUtility.now() + " Couldn't map " + this.toStringFull() + " to its tokenized parsed output.");
			return;
		}	
		
		int hIndex = listOfTokenIndxByParser.get(listOfTokenIndxByParser.size()-1);
		//-- Heuristics for finding head noun of an NP
		
		if ( listOfTokenIndxByParser.size() > 1 ) {
			ArrayList<Integer> listOfWI = new ArrayList<Integer>();
			
			/*
			 *  Rule 1a: For terms fitting the pattern X of... (where of represents any preposition)	the term X was taken as the head noun.
			 *  Rule 1b: For terms fitting the pattern X , ... 	the term X was taken as the head noun.
			 *  Rule 1c: For terms fitting the pattern X ' or 's ... 	the term X was taken as the head noun.
			 */
			listOfWI.add(listOfTokenIndxByParser.get(0));
			// the 1st word of the NP could be wrongly annotated as preposition by the parser. So, we skip it. 
			for ( int w=1; w<listOfTokenIndxByParser.size(); w++ ) {
				if ( dg.allNodesByWordIndex[listOfTokenIndxByParser.get(w)].pos.equalsIgnoreCase("IN")
						//|| dg.allNodesByWordIndex[listOfTokenIndxByParser.get(w)].word.equals(",")
						|| dg.allNodesByWordIndex[listOfTokenIndxByParser.get(w)].word.equals("'")
						|| dg.allNodesByWordIndex[listOfTokenIndxByParser.get(w)].word.equals("'s"))
					break;
				listOfWI.add(listOfTokenIndxByParser.get(w));	
			}
			
			// Rule 2: For all other terms, the rightmost word was taken as the head noun.
			for ( int i=0; i<listOfWI.size() && listOfWI.size() > 1; i++ ){
				// this to make sure the rightmost alphanumeric word gets selected
				if ( !dg.allNodesByWordIndex[listOfWI.get(i)].word.matches(".*[A-Za-z0-9].*") ) {
					listOfWI.remove(i);
					i--;
					continue;
				}
			}
			
			hIndex = listOfWI.get(listOfWI.size()-1);
		}
		
		headWordIndexByParser = hIndex;
		headWord = dg.allNodesByWordIndex[hIndex].word;
		posOfHeadWord = dg.allNodesByWordIndex[hIndex].pos;
		lemmaOfHeadWord = dg.allNodesByWordIndex[hIndex].lemma;
		
		for ( int p=0; p<dg.allNodesByWordIndex[hIndex].relNameWithParents.size(); p++ ) {
			if ( dg.allNodesByWordIndex[hIndex].relNameWithParents.get(p).contains("subj") ) {
				isSubject = true;
				break;
			}
			else if ( dg.allNodesByWordIndex[hIndex].relNameWithParents.get(p).contains("obj") ) {
				isObject = true;
				break;
			}
		}
		
		isHumanMention();
		
		if ( type.toLowerCase().matches("(person|people)") ) {
			detectClinicalEntityType();
		}
		
		setHeadNounType();
		isPronoun();
		getPerson();
		getGender();
		getNumber(dg, dg.allNodesByWordIndex[hIndex]);
		detectExistential(dg.allNodesByWordIndex[hIndex]);
		detectDeterminer();
		setGovernerWordIndexesOfSubjObj(dg);
		isPartOfNP(psgTree);
	}
	
	/**
	 * 
	 */
	private void detectClinicalEntityType() {
		
		if ( !type.matches("(people|person)") )
			return;
		
	/*	
		if ( name.toLowerCase().matches(".*(pt|patient|you|mr|mrs|ms|male|man|gentleman|female|woman|caucasian" +
						"|infant|baby|newborn|toddler|boy|girl|year.*old|student|secretary).*") ) {
		/*/
		if ( name.toLowerCase().matches("(the\\s)?(pt|pt.|patient)") 
				|| name.toLowerCase().matches("(you|your|yours)")
				|| name.toLowerCase().matches("(mr|mrs|ms)\\.?\\s.+")
			|| name.toLowerCase().matches(".*(male|man|gentleman|female|woman|caucasian" +
					"|infant|baby|newborn|toddler|boy|girl|year.*old|student|secretary).*") ) {
			//		*/
			if ( !name.toLowerCase().matches(".*'s.*[a-zA-Z].*") && !name.toLowerCase().contains(" of ") ) {
				clinicalEntityType = eClinicalEntity.Patient;
				return;
			}
		}
		
		if ( name.toLowerCase().matches("(i|my|myself|we|us|our)")
				|| name.toLowerCase().matches(".+\\s+m\\.?d\\.?") 
				 ||	name.toLowerCase().matches("(.*\\s)*(dr\\.?|drs|doctor|attending|cardiologist|internist|nephrologist|neurologist|pcp|compatible.*doctor" +
					"|primary care physician|physician|primary cardiologist|primary care doctor|primary care provider" +
					"|primary doctor|psychiatrist|surgeon" +
					"|cardiology|hematology).*") ) {
		/*
		if ( name.toLowerCase().matches("(.*\\s)*(m\\.?d\\.?|dr\\.?|i|my|myself|we|our|us" +
				"|drs|doctor|attending|cardiologist|internist|nephrologist|neurologist|pcp|compatible_doctors" +
				"|primary care physician|physician|primary cardiologist|primary care doctor|primary care provider" +
				"|primary doctor|psychiatrist|surgeon" +
				"|cardiology|hematology).*") ) {*/
			if ( !name.toLowerCase().matches(".*'s.*[a-zA-Z].*") && !name.toLowerCase().contains(" of ") ) {
				clinicalEntityType = eClinicalEntity.Doctor;
				return;
			}			
		}
		
		// check family type entity
		if ( this.name.toLowerCase().matches(".*(your|her|'s|his)\\s+(family|father|mother|baby|child|sister|brother|babies|parents).*") 
				|| this.name.toLowerCase().matches(".*parents.*") )			
		    this.clinicalEntityType = eClinicalEntity.Family;
		else if ( this.headWord.toLowerCase().matches("(service|team|dept.|department|unit)") )
		      this.clinicalEntityType = eClinicalEntity.Other;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isHumanName ( DataFile df ) {
		
		if ( type.contains("person") || type.contains("people") ) {
			if ( HumanList.hasHumanTitle(name) )
				return true;
			
			if ( senIndx == eSenIndx
					&& eTokIndxBySpace > sTokIndxBySpace && df.listOfSentences.get(senIndx).arrWordBySpace[sTokIndxBySpace].matches("[A-Z].*")
					&& df.listOfSentences.get(senIndx).arrWordBySpace[sTokIndxBySpace+1].matches("[A-Z].*") )
				return true;
		}
		
		return false;
	}
	
	
	/**
	 * 
	 * @param str
	 */
	public void setNPMentionType ( String str ) {
	
		if ( !TextUtility.isEmptyString(str) ) {
			this.type = str;
			if ( !listOfNPMentionTypes.contains(str) )
				listOfNPMentionTypes.add(str);
		}
	}
	
	/**
	 * 
	 * 
	 */
	private void setHeadNounType () {		    
		
		if ( this.posOfHeadWord.equalsIgnoreCase("NN") )
			headNounType = eNOUN.COMMON;
		else if ( this.posOfHeadWord.equalsIgnoreCase("NNP") )
			headNounType = eNOUN.PROPER;
		else if ( this.posOfHeadWord.equalsIgnoreCase("NNPS") )
			headNounType = eNOUN.PROPER;
		else if ( this.posOfHeadWord.equalsIgnoreCase("NNS") )
			headNounType = eNOUN.COMMON;  
	}
	
	/**
	 * 
	 * @param df
	 * @return
	 */
	public boolean isAllWordsNNP ( DataFile df ) {		    
		
		String[][] tmpArr = df.listOfSentences.get(senIndx).arrWordAndPosByParser;
		
		for ( int i=0; i<listOfTokenIndxByParser.size(); i++ )
			if ( !tmpArr[listOfTokenIndxByParser.get(i)][1].matches("(NNP|NNPS)")  )
				return false;
		
		return true;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isFirstWordPronoun () {		    
		
		if ( isPronoun() )
			return true;
				
		// if more then one tokens then it is not pronoun
		if ( HumanList.isFoundInPronounList(this.name.split("\\s+")[0]) )
			return true;
				
		return false;  
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isFirstWordPronounOrDeterminer () {		    
		
		if ( isPronoun() )
			return true;
				
		// if more then one tokens then it is not pronoun
		if ( HumanList.isFoundInPronounList(this.name.split("\\s+")[0]) || HumanList.isFoundInDeterminerList(this.name.split("\\s+")[0]) )
			return true;
				
		return false;  
	}
	
	/**
	 * 
	 * @param pst
	 * @return
	 */
	public boolean isDemostrativePronoun ( PhraseStructureTree pst ) {
		
		if ( isPronoun() && name.toLowerCase().matches("this|that|these|those") ){
			
			if ( pst.getParentPhraseType(headWordIndexByParser).equalsIgnoreCase("NP") )
				return true;
		}
				
		
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isDemostrativeNP () {
		
		if ( name.split("\\s+")[0].toLowerCase().matches("this|that|these|those") )
			return true;		
		
		return false;
	}
	

	/**
	 * 
	 * @return
	 */
	public boolean isPronoun () {		    
		
		if ( pronounType != ePronoun.UNKNOWN )
			return true;
		
		boolean pronoun = false;
		// if more then one tokens then it is not pronoun
		if ( this.eTokIndxBySpace == this.sTokIndxBySpace && 
				(this.posOfHeadWord.startsWith("PRP") || this.posOfHeadWord.startsWith("WP")
						|| HumanList.isFoundInPronounList(this.name) ) 
						|| isReflexive())
			pronoun = true;
		
		if ( pronoun ) {
			headNounType = eNOUN.UNKNOWN;
			
			if ( this.posOfHeadWord.equalsIgnoreCase("PRP$") || this.posOfHeadWord.equalsIgnoreCase("WP$") )
				pronounType = ePronoun.POSSESSIVE;
		}
		
		return pronoun;  
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isReflexive() {
	    
		  if (this.eTokIndxBySpace == this.sTokIndxBySpace && this.name.indexOf("sel") > 0)
	        return true;

		  return false;
	  }

	
	/**
	 * 
	 * @return
	 */
	public boolean isIt() {
	    
	    if( !this.isPronoun() )
	    	return false;
	    
	    return this.name.toLowerCase().startsWith("it");//it its itself
	  }
	
		
	/**
	 * 
	 * @param str
	 */
	public NPMention ( String str ) {
		
		int x = str.lastIndexOf("\"");
		this.name = str.substring(0, x).trim().replace("c=\"", "");
		
		String[] tmp = str.substring(x+1).trim().split("\\s+");
		String[] boundaries = tmp[0].split(":");
		
		this.senIndx = Integer.valueOf(boundaries[0])-1;
		this.sTokIndxBySpace = Integer.valueOf(boundaries[1]);
				
		boundaries = tmp[1].split(":");
		this.eSenIndx = Integer.valueOf(boundaries[0])-1;
		this.eTokIndxBySpace = Integer.valueOf(boundaries[1]);
	}
	
	/**
	 * 
	 * @param str
	 * @return
	 */
	public static String getTypeFromString ( String str ) {
		str = str.trim().replaceAll("t=|\\\"", "").trim();
		
		if ( str.indexOf("coref") == 0 )
			str = str.replaceFirst("coref\\s+", "");
		
		return str;
	}
	
	/**
	 * 
	 * @param other
	 * @return
	 */
	public boolean isSameMention ( NPMention other ) {
	
		if ( this.senIndx == other.senIndx && this.sTokIndxBySpace == other.sTokIndxBySpace
				&& this.eTokIndxBySpace == other.eTokIndxBySpace )
			return true;		
		/*else if ( this.senIndx == other.senIndx && this.name.equalsIgnoreCase(other.name) ) {
			return true;
		}
		*/
		return false;
	}
	
	
	/**
	 * 
	 * @return
	 */
	private eNumber getNumber( DependencyGraph dg, DepGraphVertex dvHeadWord ) {
	    
		if( this.numberType != eNumber.UNKNOWN )
			return this.numberType;
		
		/*
		 * find the main verb that governs the head, decide the number of that verb....
	 * 		....otherwise decide the number of the head...if the ending of the verb/head is either s and e and has 
	 *      different lemmatized form than surface form...then it is plural
		 */
	    
		if( name.equalsIgnoreCase("you") )
			return this.numberType;
			
		if( name.matches(".*\\s+and\\s+.*") )
    		this.numberType = eNumber.PLURAL;
		else if( HumanList.isPlural(dvHeadWord.word) )
	    	this.numberType = eNumber.PLURAL;
		else if( HumanList.isSingular(dvHeadWord.word) )
		  	this.numberType = eNumber.SINGULAR;
		
		if ( this.numberType == eNumber.UNKNOWN ) {
			int x=0;
			// check whether there is any governor auxiliary verb or singular main verb
			if ( (x=dvHeadWord.relNameWithParents.indexOf("aux")) >= 0
						|| (x=dvHeadWord.relNameWithParents.indexOf("auxpass")) >= 0 ) {
				String str = dg.allNodesByWordIndex[dvHeadWord.parentWordIndexes.get(x)].word;
				if( HumanList.isPlural(str) ) 
					this.numberType = eNumber.PLURAL;
				else if( HumanList.isSingular(str) )
					this.numberType = eNumber.SINGULAR;
				else  if( personType == ePerson.THIRD && str.equals("have") )
					this.numberType = eNumber.PLURAL;
			}
			else if ( (x=dvHeadWord.relNameWithParents.indexOf("nsubj")) >= 0
					|| (x=dvHeadWord.relNameWithParents.indexOf("nsubjpass")) >= 0 ) {
				
				if( HumanList.isSingularVerb(dg.allNodesByWordIndex[dvHeadWord.parentWordIndexes.get(x)].word) ) 
					this.numberType = eNumber.SINGULAR;
				else if( HumanList.isPluralVerb(dg.allNodesByWordIndex[dvHeadWord.parentWordIndexes.get(x)].word) ) 
					this.numberType = eNumber.PLURAL;
				else if( dg.allNodesByWordIndex[dvHeadWord.parentWordIndexes.get(x)].pos.equalsIgnoreCase("VBZ") ) 
					this.numberType = eNumber.SINGULAR;
			}
		}
		
		if ( this.numberType == eNumber.UNKNOWN ) {
			if ( posOfHeadWord.equalsIgnoreCase("NNP") )
			    this.numberType = eNumber.SINGULAR;
			else if ( posOfHeadWord.equalsIgnoreCase("NNPS")
					|| posOfHeadWord.equalsIgnoreCase("NNS") )
				this.numberType = eNumber.PLURAL;
		}
		
	    return this.numberType;
	}

	/**
	 * 
	 * @param df
	 * @return
	 */
	public boolean isPreceededByDigit ( DataFile df ) {
		
		if ( sTokIndxBySpace > 0 && TextUtility.isNumber(df.listOfSentences.get(senIndx).arrWordBySpace[sTokIndxBySpace-1]) )
			return true;
		
		return false;
	}
	
	/**
	 * 
	 * @param df
	 * @return
	 */
	public String followedByDigit ( DataFile df ) {
		
		if ( (eTokIndxBySpace+1 < df.listOfSentences.get(senIndx).arrWordBySpace.length
				&& TextUtility.isNumber(df.listOfSentences.get(senIndx).arrWordBySpace[eTokIndxBySpace+1])) )
			return df.listOfSentences.get(senIndx).arrWordBySpace[eTokIndxBySpace+1];
		
		else if ( eTokIndxBySpace+2 < df.listOfSentences.get(senIndx).arrWordBySpace.length
						&& TextUtility.isNumber(df.listOfSentences.get(senIndx).arrWordBySpace[eTokIndxBySpace+2])
						&& df.listOfSentences.get(senIndx).arrWordBySpace[eTokIndxBySpace+1].equals("-") )
			return df.listOfSentences.get(senIndx).arrWordBySpace[eTokIndxBySpace+2];
		
		return "";
	}
	
	/**
	 * 
	 * @return
	 */
	public eGender getGender() {
	
		/*
		 * use gazzeters, and tokens such as mr. mrs. etc
		 */
		if( this.genderType != eGender.UNKNOWN )
	      return this.genderType;
		
		if ( this.humanMention == eHumanMention.NO )
			this.genderType = eGender.NEUTRAL;
		
		if ( this.name.toLowerCase().matches("[0-9]+\\s+y/?o\\s+(f|female)") ) {
			this.genderType = eGender.FEMALE;
			this.clinicalEntityType = eClinicalEntity.Patient; // TODO: move to appropriate place
		}
		else if ( this.name.toLowerCase().matches("[0-9]+\\s+y/?o\\s+(m|male)") ) {
			this.genderType = eGender.MALE;
			this.clinicalEntityType = eClinicalEntity.Patient; // TODO: move to appropriate place
		}
		else if ( this.clinicalEntityType == eClinicalEntity.Other )
		      this.genderType = eGender.NEUTRAL;
		else if ( eNumber.PLURAL == numberType )
		      this.genderType = eGender.NEUTRAL;
		else if (HumanList.isMale(name))
	      this.genderType = eGender.MALE;	    
	    else if (HumanList.isFemale(name))
	      this.genderType = eGender.FEMALE;
	    	    
	    return genderType;
	}

	/**
	 * 
	 * @return
	 */
	public ePerson getPerson(){

	    if( personType != ePerson.UNKNOWN )
	    	return personType;
	    
	    if (HumanList.isThirdPerson(headWord))
			this.personType = ePerson.THIRD;	    
		else if (HumanList.isFirstPerson(headWord))
			this.personType = ePerson.FIRST;
		else if (HumanList.isSecondPerson(headWord))
			this.personType = ePerson.SECOND;
		else
			this.personType = ePerson.THIRD;
	    
		return personType;
	}

}
