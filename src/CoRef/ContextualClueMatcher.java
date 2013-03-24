package CoRef;

import java.util.ArrayList;

import Knowledgebase.LingusiticAnalyzer;
import Objects.DataFile;
import Objects.NPMention;
import Objects.NPMention.eClinicalEntity;
import Objects.NPMention.eHumanMention;
import Utility.DataStrucUtility;

public class ContextualClueMatcher {


	/**
	 * 
	 * @param ant
	 * @param anph
	 * @param df
	 * @param prefFeat
	 * @return
	 */
	public static boolean areProblemsOfDifferentPersons ( NPMention ant, NPMention anph, DataFile df, int antIndex, int anphIndex ) {
		
		if ( ant.type.toLowerCase().matches("(problem|signorsymptom|diseaseorsyndrome)") 
				&& anph.type.toLowerCase().matches("(problem|signorsymptom|diseaseorsyndrome)") ) {
			int x=-1, y=-1;
			for ( int i=antIndex-1; i>=0; i-- ) {
				if ( df.listOfMentions[i].senIndx != ant.senIndx )
					break;
				if ( df.listOfMentions[i].type.matches("(person|people)") ) {
					x = i;
					break;
				}
			}
			
			for ( int i=anphIndex-1; i>=0; i-- ) {
				if ( df.listOfMentions[i].senIndx != anph.senIndx )
					break;
				if ( df.listOfMentions[i].type.matches("(person|people)") ) {
					y = i;
					break;
				}
			}
			
			if ( x > -1 && y > -1 && df.listOfMentions[x].clinicalEntityType != df.listOfMentions[y].clinicalEntityType
					&& (df.listOfMentions[x].clinicalEntityType == eClinicalEntity.Patient || df.listOfMentions[x].clinicalEntityType == eClinicalEntity.Family)
					&& (df.listOfMentions[y].clinicalEntityType == eClinicalEntity.Patient || df.listOfMentions[y].clinicalEntityType == eClinicalEntity.Family) 
					) {
				//if ( df.containsPair(df, ant, anph) )
					//System.out.println(df.fileId + "   " + ant.toString() + "   " + df.listOfMentions[x).clinicalEntityType + "   " 
						//	+ anph.toString() + "   " + df.listOfMentions[y).clinicalEntityType + "   =>  " + df.containsPair(df, ant, anph));
				return true;
			}
		}
				
		return false;
	}

	
	/**
	 * 
	 * @param ant
	 * @param anph
	 * @param df
	 * @return
	 */
	public static String compareDrugAdminMode ( NPMention ant, NPMention anph, DataFile df ) {
		
		if ( ant.humanMention != eHumanMention.YES && anph.humanMention != eHumanMention.YES ) {
		
			String str = LingusiticAnalyzer.getAdminMode(ant, df).toLowerCase();
			String w = LingusiticAnalyzer.getAdminMode(anph, df).toLowerCase();
			
			return compareValuesForMentionPair(str, w);
		}
		
		return "";
	}
	
	/**
	 * 
	 * @param ant
	 * @param anph
	 * @param df
	 * @return
	 */
	public static String comparePhysicalLocation ( NPMention ant, NPMention anph, DataFile df ) {
		
		if ( ant.humanMention != eHumanMention.YES && anph.humanMention != eHumanMention.YES ) {
		
			String str = LingusiticAnalyzer.getPhysicalLocation(ant, df).toLowerCase();
			String w = LingusiticAnalyzer.getPhysicalLocation(anph, df).toLowerCase();
			
			return compareValuesForMentionPair(str, w);
		}
		
		return "";
	}
	
	
	public static String compareDateTime ( NPMention ant, NPMention anph, DataFile df ) {
		
		if ( ant.humanMention != eHumanMention.YES && anph.humanMention != eHumanMention.YES ) {
		
			String str = LingusiticAnalyzer.getTemporalExpressionDateTime(ant, df).toLowerCase();
			String w = LingusiticAnalyzer.getTemporalExpressionDateTime(anph, df).toLowerCase();
			
			return compareValuesForMentionPair(str, w);
		}
		
		return "";
	}
	
	/**
	 * 
	 * @param ant
	 * @param anph
	 * @param df
	 * @return
	 */
	public static String comparePositionCue ( NPMention ant, NPMention anph, DataFile df ) {
		
		//if ( ant.type.equalsIgnoreCase("treatment") && anph.type.equalsIgnoreCase("treatment") ) 
		{
		
			ArrayList<String> str = LingusiticAnalyzer.getPositionCue(ant, df);
			ArrayList<String> w = LingusiticAnalyzer.getPositionCue(anph, df);
			
			return compareValuesForMentionPair(str, w);
		}
		
		// return "";		
	}
	
	/**
	 * 
	 * @param valForCandAntec
	 * @param valForActiveMention
	 * @return
	 */
	private static String compareValuesForMentionPair ( String valForCandAntec, String valForActiveMention ) {
		
		valForCandAntec = valForCandAntec.trim();
		valForActiveMention = valForActiveMention.trim();
		
		if ( valForCandAntec.isEmpty() && valForActiveMention.isEmpty() ) 
			return "";
		
		if (  !valForCandAntec.isEmpty() && !valForActiveMention.isEmpty() ) {
			if ( (valForCandAntec.contains(valForActiveMention) || valForActiveMention.contains(valForCandAntec)) )
				return "Y";
			else
				return "N";
		}
		else if ( !valForCandAntec.isEmpty() )
			return "1";
		else
			return "2";
	}
	
	/**
	 * 
	 * @param str
	 * @param w
	 * @return
	 */
	public static String compareValuesForMentionPair ( ArrayList<String> valForCandAntec, 
			ArrayList<String> valForActiveMention ) {
			
			if ( valForCandAntec.isEmpty() && valForActiveMention.isEmpty() ) 
				return "";
			
			if (  !valForCandAntec.isEmpty() && !valForActiveMention.isEmpty() ) {
				if ( DataStrucUtility.hasCommonStringItems(valForCandAntec, valForActiveMention) )
					return "Y";
				else
					return "N";
			}
			else if ( !valForCandAntec.isEmpty() )
				return "1";
			else
				return "2";
	}
	
	/**
	 * 
	 * @param ant
	 * @param anph
	 * @param df
	 * @return
	 */
	public static String compareTreatmentFreqAndQuantity ( NPMention ant, NPMention anph, DataFile df ) {
		
		if ( ant.type.equalsIgnoreCase("treatment") && anph.type.equalsIgnoreCase("treatment") ) {
		
			String str = LingusiticAnalyzer.getQuantityAndFrequency(ant, df).toLowerCase();
			String w = LingusiticAnalyzer.getQuantityAndFrequency(anph, df).toLowerCase();
			
			return compareValuesForMentionPair(str, w);
		}
		
		return "";
	}


	/**
	 * 
	 * @param ant
	 * @param anph
	 * @param df
	 * @return
	 */
	public static String compareYearMonth ( NPMention ant, NPMention anph, DataFile df ) {
		
		if ( ant.humanMention != eHumanMention.YES && anph.humanMention != eHumanMention.YES ) {
		
			String str = LingusiticAnalyzer.getTemporalExpressionYearMonth(ant, df).toLowerCase();
			String w = LingusiticAnalyzer.getTemporalExpressionYearMonth(anph, df).toLowerCase();
			
			return compareValuesForMentionPair(str, w);
		}
		
		return "";
	}

}
