package CoRef;

import Objects.DataFile;
import Objects.NPMention;
import Objects.StringSimilarity;
import Objects.NPMention.eClinicalEntity;
import Objects.NPMention.eGender;
import Objects.NPMention.eHumanMention;
import Objects.NPMention.eNumber;

public class PairFilter {

	/**
	 * 
	 * @param antIndex
	 * @param anphIndex
	 * @param df
	 * @return
	 */
	public static boolean isSkipPairs( int antIndex, int anphIndex, DataFile df) {
		return isSkipPairs( df.listOfMentions[antIndex], df.listOfMentions[anphIndex], df, antIndex, anphIndex);
	}
	
	
	/**
	 * 
	 * @param ant
	 * @param anph
	 * @return
	 */
	public static boolean doesExceedSentenceWindow ( NPMention ant, NPMention anph ) {
	
		if ( Math.abs(ant.senIndx - anph.senIndx) > 5 )
			return true;
		
		return false;
	}
	
	
	/**
	 * 
	 * @param ant
	 * @param anph
	 * @return
	 */
	public static boolean isSkipPairs ( NPMention ant, NPMention anph, DataFile df, int antIndex, int anphIndex ) {
		
		if ( ant.humanMention != eHumanMention.UNKNOWN && anph.humanMention != eHumanMention.UNKNOWN
				&& anph.humanMention != ant.humanMention )
			return true;
		
		if ( ant.humanMention == eHumanMention.NO && anph.humanMention == eHumanMention.NO ) {
			if ( !anph.type.equalsIgnoreCase(ant.type) && !ant.type.toLowerCase().matches("(pronoun|none)")
				&& !anph.type.toLowerCase().matches("(pronoun|none)")
			) 
			return true;
		}
			
		/*
		if ( df.listOfSentences.get(ant.senIndx).text.toLowerCase().replaceAll("\\s+", "").equals(
				df.listOfSentences.get(anph.senIndx).text.toLowerCase().replaceAll("\\s+", ""))
			&& anph.name.equalsIgnoreCase(anph.name)	 ) 
			return false; 
		*/
		
		//if (  isSameEntityInDuplicatedSentences(ant, anph, df) )
		//	return false;
		
		if ( ContextualClueMatcher.areProblemsOfDifferentPersons(ant, anph, df, antIndex, anphIndex) )
			return true;
		
		StringSimilarity objSem = new StringSimilarity(ant, anph, df);
		
		// If anph is non-pronominal but ant is pronominal
		if ( !anph.isPronoun() && ant.isPronoun() && objSem.NoMatch ) 
			return true;
		
		if ( !anph.isFirstWordPronounOrDeterminer() && ant.isFirstWordPronounOrDeterminer() && objSem.NoMatch ) 
			return true; 
		/*
		if ( compareTreatmentFreqAndQuantity(ant, anph, df).equalsIgnoreCase("N") ) {
		//	System.out.println( df.fileId + "   " + ant.toString() + "   " +  anph.toString() + "    "
			//		+ df.containsPair(df, ant, anph));
			return true;
		}
			
		/*
		if ( !objSem.NoMatch && comparePhysicalLocation(ant, anph, df).equalsIgnoreCase("N") ) {
			//if ( df.containsPair(df, ant, anph) )
				System.out.println( df.fileId + "   " + ant.toString() + "   " +  anph.toString() + "    "
						+  df.containsPair(df, ant, anph));
			return true;
		}
			
		
		
		if ( anph.type.toLowerCase().matches("(treatment)") &&
				ant.type.toLowerCase().matches("(treatment)") &&
				compareDrugAdminMode(ant, anph, df).equalsIgnoreCase("N") ) {
				//System.out.println( df.fileId + "   " + ant.toString() + "   " +  anph.toString() + "    "
				//		+ df.containsPair(df, ant, anph));
				return true;
			}
		/*/
		
		if ( anph.isDeterminer && anph.sTokIndxBySpace > 2 && ant.senIndx != anph.senIndx )
			return true;
		
		if ( anph.isDeterminer && anph.partOfNP )
			return true;
		/*
		if ( containDifferentPositionCue(ant, anph, df) )
			return true;
		*/
		/* 
		 * if anaphora is determiner and preceded by comma (,) or CC
		 * and there are more than 4 words preceding it and the sentence indexes 
		 * of the anaphora and antecedent is same  
		 * /
		if ( anph.isDeterminer && anph.sTokIndxBySpace > 4 && anph.senIndx == ant.senIndx ) {
		
			for ( int w=0; w<df.listOfSentences.get(anph.senIndx).arrWordAndPosByParser.length
					&& w<anph.sTokIndxBySpace && !anph.listOfTokenIndxByParser.contains(w); w++ ) {
			
				if ( df.listOfSentences.get(anph.senIndx).arrWordAndPosByParser[w][1].equalsIgnoreCase("CC")
						|| df.listOfSentences.get(anph.senIndx).arrWordAndPosByParser[w][0].equals(",") )
					;//return true;
			}
		}
		*/
		
		if ( ant.genderType != eGender.UNKNOWN && anph.genderType != eGender.UNKNOWN
				&& anph.genderType != ant.genderType )
			return true;
		
		if ( ant.numberType != eNumber.UNKNOWN && anph.numberType != eNumber.UNKNOWN
				&& anph.numberType != ant.numberType )
			return true;
		
		if ( ant.posOfHeadWord.equals("CC") || anph.posOfHeadWord.equals("CC") )
			return true;
		
		//if ( ant.name.length() == 1 || anph.name.length() == 1 )
		//	return true;
		
		
		if ( anph.clinicalEntityType != eClinicalEntity.UNKNOWN && ant.clinicalEntityType != eClinicalEntity.UNKNOWN 
				&& ant.clinicalEntityType != anph.clinicalEntityType )
			return true;
		
		if ( ant.isSectionHeading || anph.isSectionHeading )
			return true;
	
	//	if ( anph.humanMention == eHumanMention.YES && anph.isAllWordsNNP(df) && ant.isAllWordsNNP(df) && !isOneNPConatiansOtherNP(ant, anph) )
		//	return true;
		
		// NOTE: The following rule HAS considerable effect if left out
		// Example: Mr. Anders states that 2 weeks prior to his presentation he hurt his left knee .
		if ( ant.isPreposition() || anph.isPreposition() )
			return true;
		
		return false;
	}

}
