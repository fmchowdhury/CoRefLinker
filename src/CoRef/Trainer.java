package CoRef;

import java.io.File;
import java.util.ArrayList;

import Objects.DataFile;
import Objects.NPMention;
import Objects.NPMention.eGender;
import Objects.NPMention.eHumanMention;
import Objects.NPMention.eNumber;
import Structures.FeatureList;
import Utility.FileUtility;

public class Trainer {
	
	/**
	 * 
	 * @param dirChain
	 * @param dirConcept
	 * @param dirPair
	 * @param dirText
	 * @param dirParse
	 * @param inFileForTrainer
	 * @throws Exception
	 */
	public void createInputForTrainer ( String dirChain, String dirConcept, 
			String dirPair, String dirText, 
			String dirParse, String inFileForTrainer ) throws Exception {
		
		File[] fileNames = FileUtility.getAllFilesFromDirectory(dirText);
		StringBuilder sb = new StringBuilder();
		
		FeatureList.globalListOfFeatureNamesAndIndex.put("$EMPTY$", 0);
		FeatureList.globalListOfFeatureNames.add("$EMPTY$");
		FeatureList.globalListOfFeatureIndexes.add(0);
		
		/*
		 
		  NOTE: Exploitation of patient/doctor/treatment/test/problem specific governing verbs was not helpful. 
		 
		for ( int i=0; i<fileNames.length; i++ ) {
			
			if ( !fileNames[i].getName().endsWith(".txt") )
				continue;
			
			DataFile df = new DataFile();
			df.readAllSentences( fileNames[i].getCanonicalPath(), 
					dirConcept+"/"+fileNames[i].getName()+".con", 
					dirChain+"/"+fileNames[i].getName()+".chains", 
					dirPair+"/"+fileNames[i].getName()+".pairs", 
					dirParse+"/"+fileNames[i].getName()+
					(CoRefMain.useCharniacParserOutput ? ".parsed.bllip.complete" : ".parsed.2.0.1"),
					true); 
			
			Dictionary.collectGoverningVerbs(df);
		}
		
		Dictionary.filterGoverningVerbLists();
		*/
		
		for ( int i=0; i<fileNames.length; i++ ) {
			
			if ( !fileNames[i].getName().endsWith(".txt") )
				continue;
			
		//	System.out.println("Reading training file " + fileNames[i].getCanonicalPath());
			
			DataFile df = new DataFile();
			df.readAllSentences( fileNames[i].getCanonicalPath(), 
					dirConcept+"/"+fileNames[i].getName()+".con", 
					dirChain+"/"+fileNames[i].getName()+".chains", 
					dirPair+"/"+fileNames[i].getName()+".pairs", 
					dirParse+"/"+fileNames[i].getName()+".parsed",
					true); 
			
			df.arrOfNoOfCandidatesForMentions = new int[df.listOfMentions.length];
			
			sb.append(createTrainingInstanceByClosestAntecedent(df));
		}
		
		FileUtility.writeInFile(inFileForTrainer, sb.toString(), false);
	}
	
	
	/**
	 * 
	 * @param df
	 * @return
	 */
	private String createTrainingInstanceByClosestAntecedent ( DataFile df ) {
		
		StringBuilder sb = new StringBuilder();
		
		for ( int c=0; c<df.listOfChains.size(); c++ ) {
			
			ArrayList<Integer> listOfMentionIndexesInChain = df.listOfChains.get(c).listOfMentionIndexes;
			for ( int m=1; m<listOfMentionIndexesInChain.size(); m++ ) {
				
				df.arrOfNoOfCandidatesForMentions[m]=0;
				int a = m-1;
				
				/**
				 * RULES for skipping 
				 */
				
				/*
				 * Select the closest preceding antecedent of m to create a positive instance. (Soon et al. 1999, 2001)				 * 
				 * If m is non-pronominal, then select the closest preceding non-pronominal antecedent. (Ng and Cardie 2002)
				 * 
				 * All the remaining mentions in between the selected closest preceding antecedent and m 
				 * should be used to create negative instances. (Soon et al. 1999, 2001)
				 */

				// If m is non-pronominal
				if ( !df.listOfMentions[listOfMentionIndexesInChain.get(m)].isPronoun() ) {
					for ( ; a>=0; a-- ) {
						if ( !df.listOfMentions[listOfMentionIndexesInChain.get(a)].isPronoun() )
							break;
					}
				}
							
				if ( a < 0 )
					a = 0;
				
				if ( listOfMentionIndexesInChain.get(m) == listOfMentionIndexesInChain.get(a) )
					continue;
				
				sb.append( "1 ").append( createFeatureValues( df.listOfMentions[listOfMentionIndexesInChain.get(a)], 
						df.listOfMentions[listOfMentionIndexesInChain.get(m)], listOfMentionIndexesInChain.get(a),
						listOfMentionIndexesInChain.get(m), df))
							.append("\n");
				
				// create negative instances
				for ( int n=listOfMentionIndexesInChain.get(a)+1; n<listOfMentionIndexesInChain.get(m); n++ ) {
					
					if ( listOfMentionIndexesInChain.get(m) != n 
							&& !PairFilter.doesExceedSentenceWindow( df.listOfMentions[n], 
									df.listOfMentions[listOfMentionIndexesInChain.get(m)]) 
							&& !PairFilter.isSkipPairs( df.listOfMentions[n], 
								df.listOfMentions[listOfMentionIndexesInChain.get(m)], df, n, listOfMentionIndexesInChain.get(m)) ) {
						sb.append( "-1 ").append( createFeatureValues( df.listOfMentions[n], 
									df.listOfMentions[listOfMentionIndexesInChain.get(m)], n,
									listOfMentionIndexesInChain.get(m), df)).append("\n");
					}
				}
			}
		}
		
		return sb.toString();
	}
	

	/**
	 * 
	 * @param ant
	 * @param anph
	 * @return
	 */
	public static String createFeatureValues ( NPMention ant, NPMention anph, int antIndex,
			int anphIndex, DataFile df ) {
		
		int closestSemanticallySimilarAntecedentIndex = getClosestSemanticallySimilarAntecedentIndex(anphIndex, df);
		
		ArrayList<Double[]> listOfFeatVal = collectAllFeatureValues(ant, anph, antIndex, anphIndex, df, "", closestSemanticallySimilarAntecedentIndex);
				
		for ( int i=0; i<listOfFeatVal.size(); i++ ) {
			if ( listOfFeatVal.get(i) == null ) {
				listOfFeatVal.remove(i);
				i--;
			}			
		}
		
		// sort the features according to their index
		for ( int i=0; i<listOfFeatVal.size()-1; i++ ) {
			for ( int k=i+1; k<listOfFeatVal.size(); k++ ) {
		
				if ( listOfFeatVal.get(i)[0] > listOfFeatVal.get(k)[0] ) {
					Double[] tmp = listOfFeatVal.get(i);
					listOfFeatVal.set(i, listOfFeatVal.get(k));
					listOfFeatVal.set(k, tmp);					
				}
			}
		}
		
		StringBuilder sb = new StringBuilder();
		
		for ( int i=0; i<listOfFeatVal.size(); i++ )
			 sb.append(" ").append( listOfFeatVal.get(i)[0].intValue()).append(":").append(listOfFeatVal.get(i)[1]);
			//sb.append(" ").append( FeatureList.globalListOfFeatureNames.get(listOfFeatVal.get(i)[0].intValue())).append(":").append(listOfFeatVal.get(i)[1]);
		
		return sb.toString();
	}
	
	
	

	
	
	
	/**
	 * 
	 * @param anphIndex
	 * @param df
	 * @return
	 */
	private static int getClosestSemanticallySimilarAntecedentIndex ( int anphIndex, DataFile df ) {
		
		int antIndex = anphIndex-1;
		for ( ; antIndex>=0; antIndex-- ) {
			if ( 
					// NOTE: if the skipped pair are not taken in consideration then the results decreases
					//!isSkipPairs(antIndex, anphIndex, df) 
					//&&
					(
						(df.listOfMentions[antIndex].humanMention == eHumanMention.YES && 
								df.listOfMentions[anphIndex].humanMention == eHumanMention.YES) 
						|| df.listOfMentions[antIndex].type.equals(df.listOfMentions[anphIndex].type)
					) )
				return antIndex;
		}
		
		return -1;
	}
	
	/**
	 * 
	 * @param ant
	 * @param anph
	 * @param antIndex
	 * @param anphIndex
	 * @param df
	 * @param prefFeat
	 * @return
	 */
	public static ArrayList<Double[]> collectAllFeatureValues ( NPMention ant, NPMention anph, int antIndex,
			int anphIndex, DataFile df, String prefFeat, int closestSemanticallySimilarAntecedentIndex ) {
		
		if ( antIndex == anphIndex )
			return new ArrayList<Double[]>();
		
		ArrayList<Double[]> listOfFeatVal = FeatureBuilder.createFeatureValuesForNPMention(ant, FeatureList.prefForAntecedent, df, prefFeat);
		
		// is closest semantically
		
		
		listOfFeatVal.add(FeatureBuilder.addFeatureForSemanticallyClosestAnt(closestSemanticallySimilarAntecedentIndex, antIndex, prefFeat));			
		
		listOfFeatVal.add(FeatureBuilder.addFeatureForSenDistance(Math.abs(ant.senIndx - anph.senIndx), prefFeat));
		
		if ( ant.humanMention != eHumanMention.YES && anph.humanMention != eHumanMention.YES && ant.type.equals(anph.type) 
				&& !ant.type.toLowerCase().matches("(pronoun|none)") ) {
			if ( ant.type.toLowerCase().matches("treatment") )
				listOfFeatVal.add( FeatureBuilder.addFeatureForTreatmentFreqAndQuantity(ant, anph, df, prefFeat));
		
		
			//if ( ant.type.toLowerCase().equalsIgnoreCase("treatment") && ant.type.equals(anph.type) )	
				listOfFeatVal.add( FeatureBuilder.addFeatureForDrugAdminMode(ant, anph, df, prefFeat));		
		
			// match date/time	
			if ( ant.type.toLowerCase().matches("(test|procedure|laboratoryortestresult|indicatorreagentdiagnosticaid)") )		
				listOfFeatVal.add( FeatureBuilder.addFeatureForTemporalExpression(ant, anph, df, prefFeat, true));
			
			// match year/month
			listOfFeatVal.add( FeatureBuilder.addFeatureForTemporalExpression(ant, anph, df, prefFeat, false));
			
			listOfFeatVal.add( FeatureBuilder.addFeatureForPhysicalLocation(ant, anph, df, prefFeat));
			
			if ( ant.type.toLowerCase().matches("(test|procedure|laboratoryortestresult|indicatorreagentdiagnosticaid|problem|signorsymptom|diseaseorsyndrome)") )	
				listOfFeatVal.add( FeatureBuilder.addFeatureForPositionCue(ant, anph, df, prefFeat));
		}
		
		listOfFeatVal.add(FeatureBuilder.addFeatureForNumber(ant, anph, prefFeat));
		listOfFeatVal.add(FeatureBuilder.addFeatureForGender(ant, anph, prefFeat));
		
		listOfFeatVal.addAll(FeatureBuilder.addFeatureForBeingPronoun(ant, anph, prefFeat));
		
		listOfFeatVal.addAll(FeatureBuilder.addFeatureForUMLSmatches(ant, anph, prefFeat));
		
		listOfFeatVal.add(FeatureBuilder.addFeatureForSameSyntacticHead(ant, anph, prefFeat));
				
		
		if ( antIndex == 0 
				|| df.listOfMentions[antIndex].senIndx != df.listOfMentions[antIndex-1].senIndx )
			listOfFeatVal.add(FeatureBuilder.addFeatureForFirstNPInCurSen(FeatureList.prefForAntecedent, prefFeat));
		
		listOfFeatVal.addAll(FeatureBuilder.addFeatureForStringSimilarity( ant, anph, prefFeat, df ));
		
		if ( prefFeat.isEmpty()  ) {			
			
			listOfFeatVal.add(FeatureBuilder.addFeatureForDemonstrativeNPAnaphora( anph, FeatureList.prefForAnaphora, prefFeat));
			listOfFeatVal.add(FeatureBuilder.addFeatureForDemonstrativePronounAnaphora( anph, FeatureList.prefForAnaphora, df, prefFeat));
			
			listOfFeatVal.addAll(FeatureBuilder.createFeatureValuesForNPMention(anph, FeatureList.prefForAnaphora, df, ""));
			if ( df.listOfMentions[anphIndex].senIndx != df.listOfMentions[anphIndex-1].senIndx )
				listOfFeatVal.add(FeatureBuilder.addFeatureForFirstNPInCurSen(FeatureList.prefForAnaphora, ""));
		}
				
		listOfFeatVal.addAll(FeatureBuilder.addFeatureForHumanName(ant, anph, prefFeat, df));		
				
		listOfFeatVal.addAll(FeatureBuilder.addFeatureForDigitsFollowed(ant, anph, prefFeat, df));
		
		listOfFeatVal.addAll(FeatureBuilder.addFeatureForClinicalPersonEntityType(ant, anph, prefFeat, df));
		
		
	//	if ( ant.senIndx == anph.senIndx )
		//	listOfFeatVal.add(FeatureBuilder.addFeatureForUnknownDependency(ant, anph, df.listOfSentences.get(ant.senIndx).depGraph, prefFeat));
				
		return listOfFeatVal;
	}
	
	
	
	
	/**
	 * 
	 * @param df
	 * @param antIndex
	 * @param anphIndex
	 * @return
	 */
	private static ArrayList<Double[]> addFeatureForOtherMentionsInBetween( DataFile df, int antIndex, int anphIndex,
			NPMention ant, NPMention anph) {
		
		//must match type, gender, number
		
		for ( int m=anphIndex-1; m > antIndex; m++ ) {
			
			if ( (df.listOfMentions[m].type.equals(ant.type) || df.listOfMentions[m].type.equals(anph.type))
				&& (df.listOfMentions[m].numberType == ant.numberType || df.listOfMentions[m].numberType == anph.numberType
						|| df.listOfMentions[m].numberType == eNumber.UNKNOWN)
					&& (df.listOfMentions[m].genderType == ant.genderType || df.listOfMentions[m].genderType == anph.genderType
								|| df.listOfMentions[m].genderType == eGender.UNKNOWN)		) {
				
				int closestSemanticallySimilarAntecedentIndex = getClosestSemanticallySimilarAntecedentIndex(anphIndex, df);
				
				return collectAllFeatureValues(df.listOfMentions[m], anph, m, anphIndex, df, "Other", closestSemanticallySimilarAntecedentIndex);
			}
		}
		
		return new ArrayList<Double[]>();
	}
}
