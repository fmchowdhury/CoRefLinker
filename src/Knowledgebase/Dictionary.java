package Knowledgebase;

import java.util.ArrayList;

import Objects.DataFile;
import Objects.NPMention;
import Objects.NPMention.eClinicalEntity;
import Objects.NPMention.eHumanMention;
import Structures.Sentence;

public class Dictionary {

	public static ArrayList<String> gListOfTreatmentVerbs = new ArrayList<String>(),
		gListOfPatientVerbs = new ArrayList<String>(), gListOfProblemVerbs = new ArrayList<String>(),
		gListOfDoctorVerbs = new ArrayList<String>(), gListOfTestVerbs = new ArrayList<String>(),
		gListOfPeopleVerbs = new ArrayList<String>();
	
	/**
	 * 
	 * @param df
	 */
	public static void collectGoverningVerbs ( DataFile df ) {
		/**
		 * If a mention is subject/object, then find it's governing word and add in the
		 * corresponding list if it is not modal or BE verb
		 */
		for ( int ch=0; ch<df.listOfChains.size(); ch++ ) {
			
			boolean isPatientChain = false, isDoctorChain = false;
			String corefType = df.listOfChains.get(ch).corefType;
			
			if ( corefType.equalsIgnoreCase("people") || corefType.equalsIgnoreCase("person") ) {
				for ( int m=0; m<df.listOfChains.get(ch).listOfMentionIndexes.size(); m++ ) {
					int anphIndx = df.listOfChains.get(ch).listOfMentionIndexes.get(m);
					NPMention anph = df.listOfMentions[anphIndx];
				
					if ( anph.clinicalEntityType == eClinicalEntity.Patient  ) {
						isPatientChain = true;
						break;
					}
					else if ( anph.clinicalEntityType == eClinicalEntity.Doctor ) {
						isDoctorChain = true;
						break;
					}
						
				}				
				
				if ( !isPatientChain && !isDoctorChain && df.listOfChains.get(ch).listOfMentionIndexes.size() > 7 )
					isPatientChain = true;
			}
			
			for ( int m=0; m<df.listOfChains.get(ch).listOfMentionIndexes.size(); m++ ) {
			
				int anphIndx = df.listOfChains.get(ch).listOfMentionIndexes.get(m);
				NPMention anph = df.listOfMentions[anphIndx];
				Sentence curSen = df.listOfSentences.get(anph.senIndx);
				
		
				for ( int p=0; p<anph.listOfGovernerWordIndexesOfSubjObj.size(); p++ ) {
					if ( !curSen.depGraph.allNodesByWordIndex[anph
				      .listOfGovernerWordIndexesOfSubjObj.get(p)].pos.equalsIgnoreCase("MD") ) {
					
						String lemma = curSen.depGraph.allNodesByWordIndex[anph
										  .listOfGovernerWordIndexesOfSubjObj.get(p)].lemma.toLowerCase()
										  + "|" + anph.listOfGovernerRelTypeOfSubjObj.get(p);
					
						if ( corefType.equalsIgnoreCase("test") && !gListOfTestVerbs.contains(lemma) )
							gListOfTestVerbs.add(lemma);
						else if ( corefType.equalsIgnoreCase("problem") && !gListOfProblemVerbs.contains(lemma) )
							gListOfProblemVerbs.add(lemma);
						else if ( corefType.equalsIgnoreCase("treatment") && !gListOfTreatmentVerbs.contains(lemma) )
							gListOfTreatmentVerbs.add(lemma);
						else if ( corefType.equalsIgnoreCase("people") || corefType.equalsIgnoreCase("person") ) {
							gListOfPeopleVerbs.add(lemma);
							if ( isPatientChain && !gListOfPatientVerbs.contains(lemma) )
								gListOfPatientVerbs.add(lemma);
							else if ( isDoctorChain && !gListOfDoctorVerbs.contains(lemma) )
								gListOfDoctorVerbs.add(lemma);
						}
					}
				}
			}
		}
	}
	
	
	public static void filterGoverningVerbLists () {
		
		for ( int w=0; w<gListOfDoctorVerbs.size(); w++ ) {
			if ( gListOfPatientVerbs.contains(gListOfDoctorVerbs.get(w)) ) {
				gListOfPatientVerbs.remove(gListOfDoctorVerbs.get(w));
				gListOfDoctorVerbs.remove(w);
			}
		}
		
		int f=0;
		f++;
	}
}
