package CoRef;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import Knowledgebase.HumanList;
import Objects.Chain;
import Objects.DataFile;
import Objects.NPMention;
import Objects.Pair;
import Objects.StringSimilarity;
import Objects.NPMention.*;
import Structures.DependencyGraph;
import Utility.DataStrucUtility;
import Utility.FileUtility;
import Utility.TextUtility;

public class Resolver {

	ArrayList<Integer> listOfNPMentionToIgnore = new ArrayList<Integer>();
	
	public void createInputForPrediction ( String dirChain, String dirConcept, 
			String dirPair, String dirText, 
			String dirParse, String inFolderForPredUsingML, String outFolderForPairPred ) throws Exception {
		
		File[] fileNames = FileUtility.getAllFilesFromDirectory(dirText);
		
		if ( !TextUtility.isEmptyString(inFolderForPredUsingML) )
			FileUtility.createDirectory( inFolderForPredUsingML );
		
		for ( int i=0; i<fileNames.length; i++ ) {
			
			if ( !fileNames[i].getName().endsWith(".txt") )
				continue;
			
		//	System.out.println("Reading test file " + fileNames[i].getCanonicalPath());
			
			DataFile df = new DataFile();
			df.readAllSentences( fileNames[i].getCanonicalPath(), 
					dirConcept+"/"+fileNames[i].getName()+".con", 
					dirChain+"/"+fileNames[i].getName()+".chains", 
					dirPair+"/"+fileNames[i].getName()+".pairs", 
					dirParse+"/"+fileNames[i].getName()+".parsed",
					true); 
			
		//	df = Trainer.setPatientOrDcotorTypeUsingGoverningVerb(df);
			df.arrOfNoOfCandidatesForMentions = new int[df.listOfMentions.length];
			
			FileUtility.writeInFile( inFolderForPredUsingML + "/"+fileNames[i].getName()+".test", 
					createTestInstances(df, outFolderForPairPred + "/"+fileNames[i].getName()+".test.pairs.pred",
							inFolderForPredUsingML + "/"+fileNames[i].getName()+".test.pairs"), false);
		}
	}

	/**
	 * 
	 * @param df
	 * @return
	 * @throws IOException 
	 */
	private String createTestInstances ( DataFile df, String finalPredOutputFile, String predPairForClassificationFileName ) throws IOException {
		
		StringBuilder sb = new StringBuilder();
		
		FileUtility.writeInFile(finalPredOutputFile, "", false);
		listOfNPMentionToIgnore = new ArrayList<Integer>();
		ArrayList<Integer> listOfNPMentionIndexesResolved = new ArrayList<Integer>(); // applyRules(finalPredOutputFile, df);
		FileUtility.writeInFile( predPairForClassificationFileName, "", false);
		
		for ( int m2=1; m2<df.listOfMentions.length; m2++ ) {
			
			df.arrOfNoOfCandidatesForMentions[m2] = 0;
				
			for ( int m1=m2-1; m1>=0; m1-- ) {
							
				if ( m2==m1 || listOfNPMentionIndexesResolved.contains(m2) || listOfNPMentionToIgnore.contains(m2) )
					continue;
				else if ( PairFilter.isSkipPairs( df.listOfMentions[m1], df.listOfMentions[m2], df, m1, m2) )
					continue;

				else if ( PairFilter.doesExceedSentenceWindow( df.listOfMentions[m1], df.listOfMentions[m2]) )
				{	
					if ( !df.listOfMentions[m2].isPronoun() && !df.listOfMentions[m2].isFirstWordPronoun()
							&& !HumanList.isFoundInDeterminerList(df.listOfMentions[m2].name) 
						&& df.listOfMentions[m1].name.equalsIgnoreCase(df.listOfMentions[m2].name)
								&& !listOfNPMentionIndexesResolved.contains(m2))
							; // consider as a candidate pair
					else
					 continue;
				}
				
				/* 
				 * For any pronoun type male/female, create train/test instance of maximum 3
				 * from the closest antecedents which must be persons, and will not mismatch in gender and number.
				 */
				
				if ( df.listOfMentions[m2].isPronoun() && df.listOfMentions[m2].genderType != eGender.UNKNOWN
						&& df.arrOfNoOfCandidatesForMentions[m2] >=3 )
					break;
				
				// if a pronoun of type male/female
				if ( df.listOfMentions[m2].isPronoun() && df.listOfMentions[m2].genderType != eGender.UNKNOWN ) {
						// if there is no gender mismatch
						if ( (df.listOfMentions[m1].genderType == eGender.UNKNOWN
								|| df.listOfMentions[m1].genderType == df.listOfMentions[m2].genderType)
						// if there is no number mismatch		
						&& (df.listOfMentions[m1].numberType == eNumber.UNKNOWN
								|| df.listOfMentions[m1].numberType == df.listOfMentions[m2].numberType)
						)
							; // do nothing
						else
							continue;
				}
								
				int polarity = -1;
				
				for ( int p=0; p<df.listOfChains.size(); p++ )
					if ( df.listOfChains.get(p).listOfMentionIndexes.contains(m1) 
							&& df.listOfChains.get(p).listOfMentionIndexes.contains(m2) ) {
						polarity = 1;
						break;
					}
				
				df.arrOfNoOfCandidatesForMentions[m2]++;
				
				sb.append(polarity).append( Trainer.createFeatureValues(df.listOfMentions[m1], df.listOfMentions[m2], 
						m1, m2, df))
				.append("\n");
				
				FileUtility.writeInFile( predPairForClassificationFileName, new Pair().toString(df.listOfMentions[m1], 
						df.listOfMentions[m2]) + "\n", true);
			}
		}
		
		return sb.toString();
	}
	

	/**
	 * 
	 * @param listOfAllMentions
	 * @param finalPredOutputFile
	 * @return
	 * @throws IOException 
	 */
	private ArrayList<Integer> applyRules ( String finalPredOutputFile,
			DataFile df) throws IOException {
		
		ArrayList<Integer> listOfNPMentionIndexesResolved = new ArrayList<Integer>();
		
		for ( int m2=1; m2<df.listOfMentions.length; m2++ ) {
			for ( int m1=m2-1; m1>=0; m1-- ) {
				if ( df.listOfMentions[m1].senIndx == df.listOfMentions[m2].senIndx &&
						detectCoreferentsUsingDependency(
						df.listOfSentences.get(df.listOfMentions[m2].senIndx).depGraph, 
						df.listOfMentions[m1], df.listOfMentions[m2], 
						listOfNPMentionIndexesResolved, m2, finalPredOutputFile, df.fileId) )
					break;
			}
		}	
	
		return listOfNPMentionIndexesResolved;
	}
	
	
	/**
	 * 
	 * @param dg
	 * @param ant
	 * @param anph
	 * @param listOfNPMentionIndexesResolved
	 * @param anphIndex
	 * @return
	 * @throws IOException 
	 */
	private boolean detectCoreferentsUsingDependency ( DependencyGraph dg, NPMention ant, NPMention anph,
			ArrayList<Integer> listOfNPMentionIndexesResolved, int anphIndex, String finalPredOutputFile, String fileId ) throws IOException {
		
		/*
		 * A referent of the head of an NP is the relative word introducing the relative clause modifying the NP.
		 * “I saw the book which you bought”		ref (book, which)
		 *
		 * An appositional modifier of an NP is an NP immediately to the right of the first NP that serves 
		 * to define or modify that NP. It includes parenthesized examples.
		 * “Sam, my brother” 		“Bill (John’s cousin)”
		 * appos(Sam, brother)		appos(Bill, cousin)
		 */
		
		//System.out.println(fileId + " " + ant.toStringFull() + "  " + anph.toStringFull());
		
		for ( int i=0; i<dg.allNodesByWordIndex[ant.headWordIndexByParser].relNameWithParents.size();
				i++ ) {
			if ( (dg.allNodesByWordIndex[ant.headWordIndexByParser].relNameWithParents.get(i).equals("ref") 
				|| dg.allNodesByWordIndex[ant.headWordIndexByParser].relNameWithParents.get(i).equals("appos"))
				&& dg.allNodesByWordIndex[ant.headWordIndexByParser].parentWordIndexes.get(i)==anph.headWordIndexByParser
				) {
		
				//System.out.println(fileId + " " + ant.toStringFull() + "  " + anph.toStringFull());
				
				writePredPair(ant, anph, listOfNPMentionIndexesResolved, anphIndex, finalPredOutputFile);
				return true;
			}			
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param ant
	 * @param anph
	 * @param listOfNPMentionIndexesResolved
	 * @param anphIndex
	 * @param finalPredOutputFile
	 * @throws IOException
	 */
	private void writePredPair ( NPMention ant, NPMention anph,
			ArrayList<Integer> listOfNPMentionIndexesResolved, int anphIndex,
			String finalPredOutputFile ) throws IOException {
		
		// write in the output file that ant and m2 are coreferents
		FileUtility.writeInFile(finalPredOutputFile, new Pair().toString(ant,anph) + "\n", true);
		
		//if ( !listOfNPMentionIndexesResolved.contains(anphIndex) )
			//listOfNPMentionIndexesResolved.add(anphIndex);		
	}
	
	
	/**
	 * 
	 * @param dirPredPairForClassificationFiles
	 * @param dirFinalPredOutputFiles
	 * @param dirMlPrecitionResultFiles
	 * @param dirConceptFiles
	 * @param dirPredChainFiles
	 * @param dirSenFiles
	 * @throws Exception 
	 */
	public void extractAnaphoraAndAntecedents ( String dirPredPairForClassificationFiles,
			String dirFinalPredOutputFiles, String dirMlPrecitionResultFiles, String dirConceptFiles,
			String dirPredChainFiles, String dirSenFiles, String dirParsedDataFiles ) throws Exception {
		
		new Resolver().extractPositiveInstanceUsingMLoutput(dirPredPairForClassificationFiles, dirFinalPredOutputFiles, dirMlPrecitionResultFiles);
				
		new Resolver().createChainsFromPairs( dirConceptFiles, dirFinalPredOutputFiles, dirSenFiles, dirPredChainFiles, dirParsedDataFiles);		
	}
	
	/**
	 * 
	 * @param predPairForClassificationFileName
	 * @param finalPredOutputFile
	 * @param mlPrecitionResultFile
	 * @throws IOException
	 */
	private void extractPositiveInstanceUsingMLoutput( String dirPredPairForClassificationFiles,
			String dirFinalPredOutputFiles, String dirMlPrecitionResultFiles) throws IOException {
		
		File[] fileNames = FileUtility.getAllFilesFromDirectory(dirPredPairForClassificationFiles);
		String predPairForClassificationFileName, finalPredOutputFile, mlPrecitionResultFile;
		
		for ( int f=0; f<fileNames.length; f++ ) {
			if ( fileNames[f].getName().endsWith(".pairs") ) {
				predPairForClassificationFileName = fileNames[f].getCanonicalPath();
				mlPrecitionResultFile = dirMlPrecitionResultFiles + "/" + fileNames[f].getName().replace(".pairs", ".stat.in");
				finalPredOutputFile = dirFinalPredOutputFiles + "/" + fileNames[f].getName() + ".pred";
				
				ArrayList<String> listMLprecitions = FileUtility.readNonEmptyFileLines(mlPrecitionResultFile);
				ArrayList<String> listCandidatePairs = FileUtility.readNonEmptyFileLines(predPairForClassificationFileName);
				
				for ( int i=0; i<listMLprecitions.size(); i++ ) {
				
					if ( listMLprecitions.get(i).contains(" 1 ") ) {
						String[] tmp = listMLprecitions.get(i).split("\\s+");
						boolean polarity = false;
						if ( (tmp[0].equals("0.0") && tmp[2].equals("0"))
								|| tmp[0].equals("1.0") && tmp[2].equals("1") )
							polarity = true;
					
						FileUtility.writeInFile(finalPredOutputFile, listCandidatePairs.get(i) + "||" 
								+ tmp[tmp.length-1] + "||" + polarity + "\n", true);
					}
				}
			}
		}
	}
	
	
	/**
	 * 
	 * @param inFile
	 * @param outFile
	 * @throws Exception 
	 */
	private void createChainsFromPairs ( String dirConceptFiles, String dirPredPairsFiles, String dirSentenceFiles,
			String dirPredChainFiles, String dirParsedDataFiles ) throws Exception {
		
		File[] fileNames = FileUtility.getAllFilesFromDirectory(dirConceptFiles);
		
		double PROBABILITY_THRESHOLD_SCORE = 4.0;
		
		for ( int f=0; f<fileNames.length; f++ ) {
			//System.out.println(fileNames[f]);
			
			String senFileName = dirSentenceFiles + "/" + fileNames[f].getName().replace(".con", ""),
				npMentionFileName = fileNames[f].getCanonicalPath(),
				pairFileName = dirPredPairsFiles + "/" + fileNames[f].getName().replace(".con", ".test.pairs.pred"),
				parsedDataFileName = dirParsedDataFiles+"/"+fileNames[f].getName().replace(".con", ".parsed") ,
				
				outFile = dirPredChainFiles + "/" + fileNames[f].getName().replace(".con", ".chains.pred");
			
			DataFile df = new DataFile();
			df.fileId = fileNames[f].getName();
			
			df.readAllSentences(senFileName, npMentionFileName, null, pairFileName, parsedDataFileName, true);
			df.listOfChains = new ArrayList<Chain>();

			ArrayList<ArrayList<Integer>> listOfAntecendentsPerMentions = new ArrayList<ArrayList<Integer>>();
			ArrayList<ArrayList<Double>> listOfScorePerAntecendentsPerMentions = new ArrayList<ArrayList<Double>>();
			
			for ( int m=0; m<df.listOfMentions.length; m++ ) {
				listOfAntecendentsPerMentions.add(new ArrayList<Integer>());
				listOfScorePerAntecendentsPerMentions.add(new ArrayList<Double>());
			}
			
			// 0.6 for treatment 0.7 for test and problem 0.8 for person
			// create list of all antecedents per mention
			for ( int p=0; p<df.listOfPairs.size(); p++ ) {
				if ( df.listOfPairs.get(p).probabScore < 0.9 ) 
					continue;
				
				// if the anaphora does not contain the antecedent already
				if ( !listOfAntecendentsPerMentions.get(df.listOfPairs.get(p).anaphoraMenitonIndex)
						.contains(df.listOfPairs.get(p).antecedentMenitonIndex) ) {
					
					listOfAntecendentsPerMentions.get(df.listOfPairs.get(p).anaphoraMenitonIndex)
						.add(df.listOfPairs.get(p).antecedentMenitonIndex);
				
					listOfScorePerAntecendentsPerMentions.get(df.listOfPairs.get(p).anaphoraMenitonIndex)
					.add(df.listOfPairs.get(p).probabScore);
				}
			}
					
			// We consider the closest match for creating chains
			// If {A,C} and {B,C} then keep only {B,C}
		
			df.listOfPairs = new ArrayList<Pair>();
			for ( int m=0; m<listOfAntecendentsPerMentions.size(); m++ ) {
				if ( listOfAntecendentsPerMentions.get(m).size() > 0 ) {
					
					ArrayList<Integer> tmpListOfSelectedAnt = new ArrayList<Integer>();
					
					// select the pairs for which the probability value exceeds a certain threshold
					for ( int a=0; a<listOfAntecendentsPerMentions.get(m).size() && listOfAntecendentsPerMentions.get(m).size() > 1; a++ ) {
						if ( listOfScorePerAntecendentsPerMentions.get(m).get(a) >= PROBABILITY_THRESHOLD_SCORE ) {		
							Pair newPair = new Pair();
							newPair.antecedentMenitonIndex = listOfAntecendentsPerMentions.get(m).get(a);
							newPair.anaphoraMenitonIndex = m;
							newPair.getType(df);							
							
							df.listOfPairs.add(newPair);
							tmpListOfSelectedAnt.add(newPair.antecedentMenitonIndex);
						}
					}
					
					// sort
					listOfAntecendentsPerMentions.set(m, DataStrucUtility.sortAndRemDuplicate(listOfAntecendentsPerMentions.get(m)));
					
					// select the closest one (which is the last element in the sorted list) as antecedent if not already added
					if ( !tmpListOfSelectedAnt.contains(listOfAntecendentsPerMentions.get(m).get(listOfAntecendentsPerMentions.get(m).size()-1)) ) {
						Pair newPair = new Pair();						
						newPair.antecedentMenitonIndex = listOfAntecendentsPerMentions.get(m).get(listOfAntecendentsPerMentions.get(m).size()-1);
						newPair.anaphoraMenitonIndex = m;
						newPair.getType(df);
					
						df.listOfPairs.add(newPair);
					}
				}
			}
			
			
			// check for cataphora
			for ( int m2=0; m2<df.listOfMentions.length-1; m2++ ) {
				if ( df.listOfMentions[m2].eTokIndxBySpace > df.listOfMentions[m2].sTokIndxBySpace
						&& df.listOfMentions[m2].isFirstWordPronounOrDeterminer() && df.listOfMentions[m2].humanMention == eHumanMention.NO ) {
					for ( int ant=m2+1; ant<df.listOfMentions.length; ant++ ) {
						if ( df.listOfMentions[m2].senIndx == df.listOfMentions[ant].senIndx
								// either the mentions are consecutive or there is a comma between them
								&& 
								    (df.listOfMentions[m2].eTokIndxBySpace == df.listOfMentions[ant].sTokIndxBySpace -1
								    		|| (df.listOfMentions[m2].eTokIndxBySpace == df.listOfMentions[ant].sTokIndxBySpace -2
								    			 && df.listOfSentences.get(df.listOfMentions[ant].senIndx).arrWordBySpace[df.listOfMentions[ant].sTokIndxBySpace-1]
								    			      .equals(",") )
									)
								&& !df.listOfMentions[ant].isFirstWordPronounOrDeterminer()
								&& df.listOfMentions[ant].type.equalsIgnoreCase(df.listOfMentions[m2].type)
								&& df.listOfMentions[ant].type.matches("(people|person)")
								 && df.listOfMentions[m2].humanMention != eHumanMention.NO ) {
					
							Pair newPair = new Pair();
							newPair.antecedentMenitonIndex = m2;
							newPair.anaphoraMenitonIndex = ant;
							newPair.getType(df);
									
							df.listOfPairs.add(newPair);
							break;
						}
					}
				}
			} 
			
			
			// check for patient/the patient
			for ( int ant=0; ant<df.listOfMentions.length-1; ant++ ) {
				for ( int m2=ant+1; m2<df.listOfMentions.length; m2++ ) {
					
					if ( df.listOfMentions[m2].name.toLowerCase().matches("(the patient|patient)")
							&& df.listOfMentions[ant].name.toLowerCase().matches("(the patient|patient)") ) {
						
						boolean add = true;
						for ( int p=0; p<df.listOfPairs.size(); p++ )
							if ( df.listOfPairs.get(p).antecedentMenitonIndex == ant &&
									df.listOfPairs.get(p).anaphoraMenitonIndex == m2 ) {
								add = false;
								break;
							}
						
						if ( add ) {					
							Pair newPair = new Pair();
							newPair.antecedentMenitonIndex = ant;
							newPair.anaphoraMenitonIndex = m2;
							newPair.getType(df);
									
							df.listOfPairs.add(newPair);
							break;
						}
					}
				}
			}
						
					
			// create chains
			ArrayList<ArrayList<Integer>> listOfTempChainsForMentions = new ArrayList<ArrayList<Integer>>();
			
			for ( int m=0; m<df.listOfMentions.length; m++ ) {
				listOfTempChainsForMentions.add(new ArrayList<Integer>());
				listOfTempChainsForMentions.get(m).add(m);
			}
					
			for ( int p=0; p<df.listOfPairs.size(); p++ ) {
				
				if ( !listOfTempChainsForMentions.get(df.listOfPairs.get(p).anaphoraMenitonIndex)
						.contains(df.listOfPairs.get(p).antecedentMenitonIndex) )
						listOfTempChainsForMentions.get(df.listOfPairs.get(p).anaphoraMenitonIndex)
						.add(df.listOfPairs.get(p).antecedentMenitonIndex);					
			}
			
			for ( int c=0; c<listOfTempChainsForMentions.size(); c++ ) {
				// remove chains that has no coreferent
				if ( listOfTempChainsForMentions.get(c).size() < 2 ) {
					listOfTempChainsForMentions.remove(c);
					c--;
				}
			}
				
			// merge chains if they share some common mentions
			for ( int c=0; c<listOfTempChainsForMentions.size(); c++ ) {
				if ( listOfTempChainsForMentions.get(c).size() > 1 ) {
					for ( int ant=0; ant<listOfTempChainsForMentions.size(); ant++ ) {
						
						if ( ant != c && listOfTempChainsForMentions.get(ant).size() > 1
								& DataStrucUtility.hasCommonItems(listOfTempChainsForMentions.get(ant), 
										listOfTempChainsForMentions.get(c)) ) {
						
							listOfTempChainsForMentions.get(c).addAll(listOfTempChainsForMentions.get(ant));
							listOfTempChainsForMentions.remove(ant);
							ant=-1;
						}
					}
				}
			}
					
			for ( int c=0; c<listOfTempChainsForMentions.size(); c++ ) {
				// remove chains that has no coreferent
				if ( listOfTempChainsForMentions.get(c).size() < 2 ) {
					listOfTempChainsForMentions.remove(c);
					c--;
				}
				else {				
					Chain newChain = new Chain();
					newChain.listOfMentionIndexes = DataStrucUtility.sortAndRemDuplicate(listOfTempChainsForMentions.get(c));
					newChain.listOfMentionIndexes = DataStrucUtility.sort(newChain.listOfMentionIndexes);
					// set type
					for ( int x=0; x<newChain.listOfMentionIndexes.size(); x++ ) {
						if ( !df.listOfMentions[newChain.listOfMentionIndexes.get(x)].type.toLowerCase().matches("(pronoun|none|other)") ) {
							newChain.corefType = df.listOfMentions[newChain.listOfMentionIndexes.get(x)].type;
							break;
						}
					}
					
					df.listOfChains.add(newChain);
				}						
			}
			
			// merge chains
			for ( int c=0; c<df.listOfChains.size()-1; c++ ) {
				for ( int oc=c+1; oc<df.listOfChains.size(); oc++ ) {
					if ( df.listOfChains.get(c).corefType.matches("(person|people)") &&
							df.listOfChains.get(c).corefType.equals(df.listOfChains.get(oc).corefType) ) {
						
						boolean merge = false;
						if ( df.listOfChains.get(c).listOfMentionIndexes.size() > 7 && df.listOfChains.get(oc).listOfMentionIndexes.size() > 7 )
							merge = true;
						// merge two chains if both of them have length > 7
						
						if ( merge ) {
							df.listOfChains.get(c).listOfMentionIndexes.addAll(df.listOfChains.get(oc).listOfMentionIndexes);
							df.listOfChains.get(c).listOfMentionIndexes = DataStrucUtility.sortAndRemDuplicate(df.listOfChains.get(c).listOfMentionIndexes);
							df.listOfChains.get(c).listOfMentionIndexes = DataStrucUtility.sort(df.listOfChains.get(c).listOfMentionIndexes);
							df.listOfChains.remove(oc);
							oc--;
						}
					}
				}
			}
				
					
			
			StringBuilder sb = new StringBuilder();
			// write output
			for ( int c=0; c<df.listOfChains.size(); c++ ) {
				Chain curChain = df.listOfChains.get(c);
				
				if ( curChain.corefType.isEmpty() )
					System.out.println("Empty coref for chain.");
				else
					sb.append(curChain.toString(df).toLowerCase()).append("\n");
			}
			
			FileUtility.writeInFile(outFile, sb.toString(), false);
		}
	}
}