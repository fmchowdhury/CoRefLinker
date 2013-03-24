package Objects;

import java.util.ArrayList;

import Knowledgebase.HumanList;
import Utility.DataStrucUtility;
import Utility.FileUtility;

public class StringSimilarity {

	public boolean NoMatch = false, 
	ExactStringMatch = false,
	BothNNP = false,
	FullMatcheWithoutDetAndTitle = false,
	AntContainsAnph = false,
	AnphContainsAnt = false,
	HeadWordMatches = false,
	EqualNoWordWithoutDetAndTitleButNotFullMatch = false;
	
	final static ArrayList<String> listOfPrepositions = new ArrayList<String>();
	
	/**
	 * 
	 * @param ant
	 * @param anph
	 * @param df
	 */
	public StringSimilarity ( NPMention ant, NPMention anph, DataFile df ) {
		detectSimilarity(ant, anph, df);
	}
	
	
	/**
	 * 
	 * @param words
	 * @return
	 */
	private ArrayList<String> removePrepositions ( ArrayList<String> words ) {
		
		String str = "";
		for ( int i=0; i<words.size(); i++ )
		  str = str + " " + words.get(i);
		str = " " + str.trim().toLowerCase() + " ";
		
		if ( listOfPrepositions.isEmpty() ) {
			  ArrayList<String> listOfLines = FileUtility.readNonEmptyFileLines("kb/prep_list");
			  for ( int i=0; i<listOfLines.size(); i++ )
				  listOfPrepositions.add(" " + listOfLines.get(i).replaceAll("\\s+", "").trim() + " ");
			  
			  // sort the strings by size in descended order
			  for ( int i=0; i<listOfLines.size()-1; i++ )
				  for ( int k=i+1; k<listOfLines.size(); k++ ) {
					  if ( listOfPrepositions.get(i).length() < listOfPrepositions.get(k).length() ) {
						  String w = listOfPrepositions.get(i);
						  listOfPrepositions.set(i, listOfPrepositions.get(k));
						  listOfPrepositions.set(k, w);
					  }
				  }
		  }
		
		for ( int i=0; i<listOfPrepositions.size(); i++ )
			str = str.replaceAll( listOfPrepositions.get(i), " ");
			
		words = DataStrucUtility.arrayToList(str.trim().split("\\s+"));
		
		return words;
	}
	
	/**
	 * 
	 * @param ant
	 * @param anph
	 * @param df
	 */
	public void detectSimilarity ( NPMention ant, NPMention anph, DataFile df ) {	
		
		String[] tmp = ant.name.split("\\s+");
		ArrayList<String> antWords = new ArrayList<String>();
		for ( int x=0; x<tmp.length; x++ )
			if ( !HumanList.isFoundInDeterminerList(tmp[x]) && !HumanList.hasHumanTitle(tmp[x])
					&& !HumanList.isFoundInPersonalPronounList(tmp[x])
					)
				antWords.add(tmp[x].toLowerCase());
		
		tmp = anph.name.split("\\s+");
		ArrayList<String> anphWords = new ArrayList<String>();
		for ( int x=0; x<tmp.length; x++ )
			if ( !HumanList.isFoundInDeterminerList(tmp[x]) && !HumanList.hasHumanTitle(tmp[x]) 
					&& !HumanList.isFoundInPersonalPronounList(tmp[x])
					)
				anphWords.add(tmp[x].toLowerCase());
		
	//	anphWords = removePrepositions(anphWords);
	//	antWords = removePrepositions(antWords);
		
		boolean isNoMatch = true;
		int totMatch = 0;
		for ( int x=0; x<anphWords.size(); x++ )
			if ( antWords.contains(anphWords.get(x)) ) {
				isNoMatch = false;
				totMatch++;
				break;
			}
		
		if ( isNoMatch ) {
			NoMatch = true;
			return;
		}
		/*
		if ( anph.type.equalsIgnoreCase("test") && ant.type.equalsIgnoreCase("test") 
				&& ant.name.equalsIgnoreCase(anph.name) )
			listOfFeatVal.add(addFeature("BothNPareTestAndHasFullMatch"));
		*/
		
		if ( anph.isAllWordsNNP(df) && ant.isAllWordsNNP(df) )
			BothNNP = true;
		
		if ( ant.name.equalsIgnoreCase(anph.name) )
			ExactStringMatch = true;
		
		if ( anphWords.size() == antWords.size() && anphWords.size() == totMatch )
			FullMatcheWithoutDetAndTitle = true;
		
		if ( antWords.size() != anphWords.size() && antWords.containsAll(anphWords) )		
			AntContainsAnph = true;
		
		if ( antWords.size() != anphWords.size() && anphWords.containsAll(antWords) ) 		
			AnphContainsAnt = true;
		
		if ( ant.headWord.equalsIgnoreCase(anph.headWord) ) 
			HeadWordMatches = true;
		
		if ( anphWords.size() == antWords.size() && anphWords.size() != totMatch ) 
			EqualNoWordWithoutDetAndTitleButNotFullMatch = true;
		
		
		/*
		if ( anphWords.get(0).equalsIgnoreCase(antWords.get(0)) ) {
			listOfFeatVal.add(addFeature(prefFeat + "StringMatchAtFirstWord"));
			if ( !nnp.isEmpty() )
				listOfFeatVal.add(addFeature(prefFeat + nnp + "StringMatchAtFirstWord"));
		}
		
		if ( anphWords.get(anphWords.size()-1).equalsIgnoreCase(antWords.get(antWords.size()-1)) ) {
			listOfFeatVal.add(addFeature(prefFeat + "StringMatchAtLastWord"));
			if ( !nnp.isEmpty() )
				listOfFeatVal.add(addFeature(prefFeat + nnp + "StringMatchAtLastWord"));
		}*/
		
	}
	
	/**
	 * 
	 * @param antIndex
	 * @param anphIndex
	 * @param df
	 */
	public StringSimilarity ( int antIndex, int anphIndex, DataFile df ) {
		
		NPMention ant = df.listOfMentions[antIndex], anph = df.listOfMentions[anphIndex];
		detectSimilarity(ant, anph, df);
	}
}
