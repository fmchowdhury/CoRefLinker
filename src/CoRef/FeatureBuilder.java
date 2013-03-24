package CoRef;

import java.util.ArrayList;
import Objects.DataFile;
import Objects.NPMention;
import Objects.StringSimilarity;
import Objects.NPMention.eClinicalEntity;
import Objects.NPMention.eGender;
import Objects.NPMention.eHumanMention;
import Objects.NPMention.eNumber;
import Structures.DependencyGraph;
import Structures.FeatureList;
import Utility.DataStrucUtility;
import Utility.TextUtility;

public class FeatureBuilder {
	
	static boolean LEXICAL = true,
		GRAMMATICAL = true,
		CONTEXTUAL = true,
		SEMANTIC = true;

	
	/**
	 * 
	 * @param mn
	 * @param pref
	 * @return
	 */
	public static ArrayList<Double[]> createFeatureValuesForNPMention ( NPMention mn, String pref, DataFile df, String prefFeat ) {
		
		ArrayList<Double[]> listOfFeatVal = new ArrayList<Double[]>();
				
		if ( mn.type.equalsIgnoreCase("treatment") && CONTEXTUAL )
			if ( df.listOfSentences.get(mn.senIndx).text.toLowerCase().matches(".*\\s+[0-9]{1,2}\\s+[hr|hour].*")
				|| df.listOfSentences.get(mn.senIndx).text.toLowerCase().matches(".*(morning|afternoon|evening|night).*")
				) {
				listOfFeatVal.add( FeatureBuilder.addFeature(prefFeat + pref + "TreatmentAndContainsTimePeriod" ));
			}
		
		
		if ( mn.hasPossesiveCase() && GRAMMATICAL )
			listOfFeatVal.add( FeatureBuilder.addFeature(prefFeat + pref + "HasPossesiveCase" ));
		
		// type
		if ( SEMANTIC )
			listOfFeatVal.add( FeatureBuilder.addFeature(prefFeat + pref + "Type-" + mn.type.toLowerCase()) );
		
		// head word feature
		if ( GRAMMATICAL )
			listOfFeatVal.add( FeatureBuilder.addFeature(prefFeat + pref + "Head-" + mn.headWord.toLowerCase()) );
		
		// subject
		if ( mn.isSubject && GRAMMATICAL ) {
			//listOfFeatVal.add( FeatureBuilder.addFeature(prefFeat + pref + "Subject") );
			ArrayList<String> listTemp = new ArrayList<String>();
			for ( int i=0; i<mn.listOfGovernerRelTypeOfSubjObj.size(); i++ )
				if ( mn.listOfGovernerRelTypeOfSubjObj.get(i).contains("subj")
						&& !listTemp.contains(mn.listOfGovernerRelTypeOfSubjObj.get(i)) ) {
					listTemp.add(mn.listOfGovernerRelTypeOfSubjObj.get(i));
					listOfFeatVal.add( FeatureBuilder.addFeature(prefFeat + pref + mn.listOfGovernerRelTypeOfSubjObj.get(i)) );
				}			
		}
		
		if ( mn.isObject && GRAMMATICAL ) {
		
			ArrayList<String> listTemp = new ArrayList<String>();
			for ( int i=0; i<mn.listOfGovernerRelTypeOfSubjObj.size(); i++ )
				if ( mn.listOfGovernerRelTypeOfSubjObj.get(i).contains("obj")
						&& !listTemp.contains(mn.listOfGovernerRelTypeOfSubjObj.get(i)) ) {
					listTemp.add(mn.listOfGovernerRelTypeOfSubjObj.get(i));
					listOfFeatVal.add( FeatureBuilder.addFeature(prefFeat + pref + mn.listOfGovernerRelTypeOfSubjObj.get(i)) );
				}
		}
		
		// pronoun
		if ( mn.isPronoun() && GRAMMATICAL ) 
			listOfFeatVal.add( FeatureBuilder.addFeature(prefFeat + pref + "Pronoun") );		
		
		// Reflexive
		if ( mn.isReflexive() && GRAMMATICAL ) 
			listOfFeatVal.add( FeatureBuilder.addFeature(prefFeat + pref + "Reflexive") );
				
		return listOfFeatVal;
	}
	
	
	/**
	 * 
	 * @param ant
	 * @param anph
	 * @param prefFeat
	 * @param df
	 * @return
	 */
	public static ArrayList<Double[]> addFeatureForStringSimilarity( NPMention ant, NPMention anph, String prefFeat, DataFile df ) {
		
		ArrayList<Double[]> listOfFeatVal = new ArrayList<Double[]>();
		
		if ( ant.isPronoun() || anph.isPronoun() || !LEXICAL )
			return listOfFeatVal;
				
		StringSimilarity objSM = new StringSimilarity(ant, anph, df);
		
		
		if ( objSM.NoMatch ) {
			//listOfFeatVal.add(FeatureBuilder.addFeature("NoStringSimilarity"));
			return listOfFeatVal;
		}
		

		//listOfFeatVal.addAll(FeatureBuilder.addFeatureForPositionCue(ant, anph, prefFeat, df));
		
		String nnp = objSM.BothNNP ? "BothNNP" : "";
		
		if (objSM.ExactStringMatch ) {
			listOfFeatVal.add(FeatureBuilder.addFeature(prefFeat + "ExactStringMatch"));
			if ( objSM.BothNNP )
				listOfFeatVal.add(FeatureBuilder.addFeature(prefFeat + nnp + "ExactStringMatch"));
		}
		else if ( objSM.FullMatcheWithoutDetAndTitle ) {
			listOfFeatVal.add(FeatureBuilder.addFeature(prefFeat + "FullMatcheWithoutDet"));
			if ( objSM.BothNNP )
				listOfFeatVal.add(FeatureBuilder.addFeature(prefFeat + nnp + "FullMatcheWithoutDet"));
		}
		else if ( objSM.AntContainsAnph ) {			
			listOfFeatVal.add(FeatureBuilder.addFeature(prefFeat + "AntContainsAnph"));
			if ( objSM.BothNNP )
				listOfFeatVal.add(FeatureBuilder.addFeature(prefFeat + nnp + "AntContainsAnph"));
		}		
		else if ( objSM.AnphContainsAnt ) {			
			listOfFeatVal.add(FeatureBuilder.addFeature(prefFeat + "AnphContainsAnt"));
			if ( objSM.BothNNP )
				listOfFeatVal.add(FeatureBuilder.addFeature(prefFeat + nnp + "AnphContainsAnt"));
		}
		else if ( objSM.HeadWordMatches ) {
			listOfFeatVal.add(FeatureBuilder.addFeature(prefFeat + "HeadWordMatches"));
			if ( objSM.BothNNP )
				listOfFeatVal.add(FeatureBuilder.addFeature(prefFeat + nnp + "HeadWordMatches"));
		}
		
		if ( objSM.EqualNoWordWithoutDetAndTitleButNotFullMatch ) {
			listOfFeatVal.add(FeatureBuilder.addFeature(prefFeat + "EqualWordWithoutDetButNotFullMatch"));
			if ( objSM.BothNNP )
				listOfFeatVal.add(FeatureBuilder.addFeature(prefFeat + nnp + "EqualWordWithoutDetButNotFullMatch"));
		}
		
		return listOfFeatVal;
	}
	
	/**
	 * 
	 * @param ant
	 * @param anph
	 * @param df
	 * @param prefFeat
	 * @return
	 */
	public static Double[] addFeatureForPositionCue ( NPMention ant, NPMention anph, DataFile df, String prefFeat ) {
		
		if ( !CONTEXTUAL )
			return null;
		
		String str = ContextualClueMatcher.comparePositionCue(ant, anph, df);
		
		return getMatchFeatureFromCue(str, "PositionCue", prefFeat, true);
	}
	
	/**
	 * 
	 * @param ant
	 * @param anph
	 * @param df
	 * @param prefFeat
	 * @return
	 */
	public static Double[] addFeatureForTreatmentFreqAndQuantity ( NPMention ant, NPMention anph, DataFile df, String prefFeat ) {
		
		if ( !CONTEXTUAL )
			return null;
		
		String str = ContextualClueMatcher.compareTreatmentFreqAndQuantity(ant, anph, df);
		
		return getMatchFeatureFromCue(str, "TreatmentFreqAndQuantity", prefFeat, false);
	}
	
	
	
	/**
	 * 
	 * @param ant
	 * @param anph
	 * @param df
	 * @param prefFeat
	 * @return
	 */
	public static Double[] addFeatureForDrugAdminMode ( NPMention ant, NPMention anph, DataFile df, String prefFeat ) {
		
		if ( !CONTEXTUAL )
			return null;
		
		String str = ContextualClueMatcher.compareDrugAdminMode(ant, anph, df);
		
		return getMatchFeatureFromCue(str, "DrugAdminMode", prefFeat, false);
	}
	
	/**
	 * 
	 * @param ant
	 * @param anph
	 * @param df
	 * @param prefFeat
	 * @param matchDateTime
	 * @return
	 */
	public static Double[] addFeatureForTemporalExpression ( NPMention ant, NPMention anph, DataFile df, String prefFeat, boolean matchDateTime ) {
		
		if ( !CONTEXTUAL )
			return null;
		
		String str = "", featName = "DateTime";
		
		if ( matchDateTime )
			str = ContextualClueMatcher.compareDateTime(ant, anph, df);
		else {
			str = ContextualClueMatcher.compareYearMonth(ant, anph, df);
			featName = "YearMonth";
		}
		
		return getMatchFeatureFromCue(str, featName, prefFeat, false);
	}

	/**
	 * 
	 * @param ant
	 * @param anph
	 * @param df
	 * @param prefFeat
	 * @return
	 */
	public static Double[] addFeatureForPhysicalLocation ( NPMention ant, NPMention anph, DataFile df, String prefFeat ) {
		
		if ( !CONTEXTUAL )
			return null;
		
		String str = ContextualClueMatcher.comparePhysicalLocation(ant, anph, df);
		
		return getMatchFeatureFromCue(str, "PhysicalLocation", prefFeat, false);
	}
	
	
	/**
	 * 
	 * @param cue
	 * @param featureName
	 * @param featurePref
	 * @param getFeatForSingleNonEmptSet
	 * @return
	 */
	private static Double[] getMatchFeatureFromCue ( String cue, String featureName, String featurePref, boolean getFeatForSingleNonEmptSet ) {
		
		if ( TextUtility.isEmptyString(cue) )
			return null;
		else if ( cue.equalsIgnoreCase("Y") )
			return addFeature(featurePref + "MatchFor" + featureName);
		else if ( cue.equalsIgnoreCase("N") )
			return addFeature(featurePref + "MismatchFor" + featureName);
		else if ( getFeatForSingleNonEmptSet ) {
			if ( cue.equalsIgnoreCase("1") )		 
				return addFeature(featurePref + "AntContains" + featureName);
			else if ( cue.equalsIgnoreCase("2") )
				return addFeature(featurePref + "AnphContains" + featureName);
		}
		
		return null;
	}
	

	
	/**
	 * 
	 * @param ant
	 * @param anph
	 * @param prefFeat
	 * @return
	 */
	public static Double[] addFeatureForNumber( NPMention ant, NPMention anph, String prefFeat ) {
		
		if ( !GRAMMATICAL )
			return null;
		
		if ( ant.numberType == eNumber.UNKNOWN || anph.numberType == eNumber.UNKNOWN  )
			return null;
		
		if ( ant.numberType == anph.numberType )
			return addFeature(prefFeat + "SimilarNumber");
		
		return addFeature(prefFeat + "NumberMismatch");
	}

	
	
	/**
	 * 
	 * @param mn
	 * @param prefFeat
	 * @return
	 */
	public static Double[] addFeatureForPartOfNP( NPMention mn, String prefFeat ) {
		
		if ( !GRAMMATICAL )
			return null;
				
		if ( mn.partOfNP )
			return addFeature(prefFeat + "PartOfNP");
		
		return null;
	}

	/**
	 * 
	 * @param ant
	 * @param anph
	 * @param prefFeat
	 * @return
	 */
	public static ArrayList<Double[]> addFeatureForBeingPronoun( NPMention ant, NPMention anph, String prefFeat ) {
		
		ArrayList<Double[]> listOfFeatVal = new ArrayList<Double[]>();
		
		if ( !GRAMMATICAL )
			return listOfFeatVal;
				
		if ( anph.isPronoun() && ant.isPronoun() )
			listOfFeatVal.add(FeatureBuilder.addFeature(prefFeat + "BothPronouns"));
		else if ( !anph.isPronoun() && ant.isPronoun() )
			listOfFeatVal.add(FeatureBuilder.addFeature(prefFeat + "AntPronounAnphNot"));
		
		return listOfFeatVal;
	}


	/**
	 * 
	 * @param ant
	 * @param anph
	 * @param prefFeat
	 * @return
	 */
	public static ArrayList<Double[]> addFeatureForUMLSmatches( NPMention ant, NPMention anph, String prefFeat ) {
		
		ArrayList<Double[]> listOfFeatVal = new ArrayList<Double[]>();
		
		if ( !SEMANTIC )
			return listOfFeatVal;
				
		if ( ant.humanMention == eHumanMention.NO 
				&& anph.humanMention == eHumanMention.NO &&  ant.type.equals(anph.type) ) {
			
			String res = "";
			if (  (res=CoRefMain.objUmlsMetaThesaurus.hasCommonUmlsCUIs(ant, anph)).equalsIgnoreCase("Y") ) {
					listOfFeatVal.add(FeatureBuilder.addFeature(prefFeat + "AliasAccordingToCommonUmlsCUI"));
					//Pair.print(ant, anph, df);
			}
			else if ( res.equalsIgnoreCase("N") ) {
				listOfFeatVal.add(FeatureBuilder.addFeature(prefFeat + "NoCommonUmlsCUI"));
				//Pair.print(ant, anph, df);
			}
		}
		
		return listOfFeatVal;
	}
	
	
	/**
	 * 
	 * @param ant
	 * @param anph
	 * @param prefFeat
	 * @param df
	 * @return
	 */
	public static ArrayList<Double[]> addFeatureForHumanName( NPMention ant, NPMention anph, String prefFeat, DataFile df ) {
		
		ArrayList<Double[]> listOfFeatVal = new ArrayList<Double[]>();
		
		if ( !SEMANTIC )
			return listOfFeatVal;
				
		boolean bothHumanName = false;
		if ( anph.isHumanName(df) ) {
			bothHumanName = true;
			if ( prefFeat.isEmpty()  )
				listOfFeatVal.add(FeatureBuilder.addFeature( FeatureList.prefForAnaphora + "HumanName"));
		}
			
		if ( ant.isHumanName(df) ) {
			if ( bothHumanName )
				listOfFeatVal.add(FeatureBuilder.addFeature(prefFeat + "BothHumanName"));
			
			listOfFeatVal.add(FeatureBuilder.addFeature(prefFeat + FeatureList.prefForAntecedent + "HumanName"));
		}
		
		return listOfFeatVal;
	}
	
	
	/**
	 * 
	 * @param ant
	 * @param anph
	 * @param prefFeat
	 * @param df
	 * @return
	 */
	public static ArrayList<Double[]> addFeatureForClinicalPersonEntityType( NPMention ant, NPMention anph, String prefFeat, DataFile df ) {
		
		ArrayList<Double[]> listOfFeatVal = new ArrayList<Double[]>();
		
		if ( !SEMANTIC )
			return listOfFeatVal;
				
		if ( ant.clinicalEntityType == eClinicalEntity.Patient && anph.clinicalEntityType == eClinicalEntity.Patient )
			listOfFeatVal.add(FeatureBuilder.addFeature(prefFeat + "BothPaitentsOfClinicalText"));
		
		listOfFeatVal.add(FeatureBuilder.addFeature(prefFeat + FeatureList.prefForAntecedent + ant.clinicalEntityType));
		listOfFeatVal.add(FeatureBuilder.addFeature(prefFeat + FeatureList.prefForAnaphora + anph.clinicalEntityType));
		
		return listOfFeatVal;
	}
	
	/**
	 * 
	 * @param ant
	 * @param anph
	 * @param prefFeat
	 * @param df
	 * @return
	 */
	public static ArrayList<Double[]> addFeatureForDigitsFollowed( NPMention ant, NPMention anph, String prefFeat, DataFile df ) {
		
		ArrayList<Double[]> listOfFeatVal = new ArrayList<Double[]>();
		
		if ( !CONTEXTUAL )
			return listOfFeatVal;
				
		if ( anph.type.toLowerCase().matches("(test|procedure|laboratoryortestresult|indicatorreagentdiagnosticaid|treatment)")
				&& anph.type.toLowerCase().matches("(test|procedure|laboratoryortestresult|indicatorreagentdiagnosticaid|treatment)") ) {
			
			String digAnph = anph.followedByDigit(df);
			String digAntec = anph.followedByDigit(df);
			
			if ( !digAnph.isEmpty() ) {
				if ( prefFeat.isEmpty()  )
					listOfFeatVal.add(FeatureBuilder.addFeature( FeatureList.prefForAnaphora + "FollowedByDigit"));
			}
				
			if ( !digAntec.isEmpty() ) {
				if ( digAnph.equals(digAntec) )
					listOfFeatVal.add(FeatureBuilder.addFeature(prefFeat + "BothFollowedByUnEqualDigit"));
				else
					listOfFeatVal.add(FeatureBuilder.addFeature(prefFeat + "BothFollowedBySameDigits"));
				
				listOfFeatVal.add(FeatureBuilder.addFeature(prefFeat + FeatureList.prefForAntecedent + "FollowedByDigit"));
			}
		}
		
		return listOfFeatVal;
	}
	
	
	/**
	 * 
	 * @param ant
	 * @param anph
	 * @param prefFeat
	 * @return
	 */
	public static Double[] addFeatureForSameSyntacticHead( NPMention ant, NPMention anph, String prefFeat ) {
		
		if ( !GRAMMATICAL )
			return null;
		
		if ( anph.senIndx == ant.senIndx &&
				DataStrucUtility.getCommonItems(anph.listOfGovernerWordIndexesOfSubjObj, 
						ant.listOfGovernerWordIndexesOfSubjObj).size() > 0 )
			return FeatureBuilder.addFeature(prefFeat + "HasSameSyntacticHead");
		
		return null;
	}
	
	
	/**
	 * 
	 * @param ant
	 * @param anph
	 * @param prefFeat
	 * @return
	 */
	public static Double[] addFeatureForGender( NPMention ant, NPMention anph, String prefFeat ) {
		
		if ( !GRAMMATICAL )
			return null;
		
		if ( ant.genderType == eGender.UNKNOWN || anph.genderType == eGender.UNKNOWN  )
			return null;
		
		if ( ant.genderType == anph.genderType )
			return addFeature(prefFeat + "SimilarGender");
		
		return addFeature(prefFeat + "GenderMismatch");
	}

	/**
	 * 
	 * @param mn
	 * @param pref
	 * @param df
	 * @param prefFeat
	 * @return
	 */
	public static Double[] addFeatureForDemonstrativePronounAnaphora ( NPMention mn, String pref, DataFile df, String prefFeat ) {
		
		if ( !GRAMMATICAL )
			return null;
		
		// Demonstrative
		if ( mn.isDemostrativePronoun( df.listOfSentences.get(mn.senIndx).psgTree ) ) 
			return addFeature(prefFeat + pref + "DemonstrativePronoun");
				
		return null;
	}
	
	/**
	 * 
	 * @param mn
	 * @param pref
	 * @param prefFeat
	 * @return
	 */
	public static Double[] addFeatureForDemonstrativeNPAnaphora ( NPMention mn, String pref, String prefFeat ) {
		
		if ( !GRAMMATICAL )
			return null;
		
		// Demonstrative
		if ( mn.isDemostrativeNP() ) 
			return addFeature(prefFeat + pref + "DemonstrativeNP");
				
		return null;
	}
	
	
	/**
	 * 
	 * @param mentionIndxDiff
	 * @param prefFeat
	 * @return
	 */
	public static Double[] addFeatureForClosestAnphCand ( int mentionIndxDiff, String prefFeat ) {
		
		if ( !CONTEXTUAL )
			return null;
		
		if ( mentionIndxDiff == 1 ) {
			return addFeature(prefFeat + "ClosestAnphCand");
		}
		
		return null;
	}

	/**
	 * 
	 * @param pref
	 * @param prefFeat
	 * @return
	 */
	public static Double[] addFeatureForFirstNPInCurSen ( String pref, String prefFeat ) {
		
		if ( !GRAMMATICAL )
			return null;
		
		return addFeature(prefFeat + pref+"FirstNPInCurSen");
	}
		
	
	/**
	 * 
	 * @param ant
	 * @param anph
	 * @param dg
	 * @param prefFeat
	 * @return
	 */
	private static Double[] addFeatureForUnknownDependency( NPMention ant, NPMention anph, DependencyGraph dg, String prefFeat ) {
		
		if ( !GRAMMATICAL )
			return null;
		
		// TODO: copy the dep relation comments from stanford parser manual
		for ( int i=0; i<dg.allNodesByWordIndex[anph.headWordIndexByParser].relNameWithParents.size(); i++ ) {
			if ( dg.allNodesByWordIndex[anph.headWordIndexByParser].relNameWithParents.get(i).equals("dep")
				&& dg.allNodesByWordIndex[anph.headWordIndexByParser].parentWordIndexes.get(i)==ant.headWordIndexByParser
				) {
		
				return addFeature(prefFeat+"antDEPanph");			
			}
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param senDist
	 * @param prefFeat
	 * @return
	 */
	public static Double[] addFeatureForSenDistance ( int senDist, String prefFeat ) {
		
		if ( !CONTEXTUAL )
			return null;
		
		// NOTE: using 1 / (senDist+1) degrades performance
		return addFeatureWithValue(prefFeat + "senDist", senDist+1);
	}
	
	/**
	 * 
	 * @param closestSemanticallySimilarAntecedentIndex
	 * @param antIndex
	 * @param prefFeat
	 * @return
	 */
	public static Double[] addFeatureForSemanticallyClosestAnt ( int closestSemanticallySimilarAntecedentIndex, 
			int antIndex, String prefFeat ) {
		
		if ( !CONTEXTUAL )
			return null;
		
		if ( closestSemanticallySimilarAntecedentIndex == antIndex )
			return addFeature(prefFeat + "ClosestSemanticallyAnt");
		
		return null;
	}
	
	
	/**
	 * 
	 * @param listOfFeatVal
	 * @param featName
	 */
	private static Double[] addFeature ( String featName ) {
		
		return addFeatureWithValue(featName, 1);
	}
	
	/**
	 * 
	 * @param featName
	 * @param value
	 * @return
	 */
	private static Double[] addFeatureWithValue ( String featName, double value ) {
		
		if ( !FeatureList.globalListOfFeatureNamesAndIndex.containsKey(featName) )
			FeatureList.globalListOfFeatureNamesAndIndex.put(featName,
				FeatureList.globalListOfFeatureNamesAndIndex.size());
		
		if ( !FeatureList.globalListOfFeatureNames.contains(featName) ) {
			FeatureList.globalListOfFeatureNames.add(featName);
			FeatureList.globalListOfFeatureIndexes.add(FeatureList.globalListOfFeatureIndexes.size());
		}
	
		return new Double[]{ Double.valueOf(FeatureList.globalListOfFeatureNames.indexOf(featName)), value};
	}
	
	
	/**
	 * 
	 * @param ant
	 * @param anph
	 * @param df
	 * @param prefFeat
	 * @return
	 */
	private static Double[] addFeatureForSameEntityInDuplicatedSentences (  NPMention ant, NPMention anph, DataFile df, String prefFeat ) {
		
		if ( !LEXICAL )
			return null;
		
		if (  ant.senIndx != anph.senIndx
				&& df.listOfSentences.get(ant.senIndx).arrWordBySpace.length > 5
				&& df.listOfSentences.get(ant.senIndx).text.toLowerCase().replaceAll("\\s+", "").equals(
				df.listOfSentences.get(anph.senIndx).text.toLowerCase().replaceAll("\\s+", ""))
				&& ant.name.equalsIgnoreCase(anph.name) 
			) {
			return addFeature(prefFeat + "SentenceRepetitionAndSameName");
		}
	
		return null;
	}
}
