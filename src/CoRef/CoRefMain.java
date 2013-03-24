package CoRef;

import Knowledgebase.UmlsMetaThesaurus;
import Utility.FileUtility;

/**
 * 
 * @author Md. Faisal Mahbub Chowdhury
 *
 */
public class CoRefMain {
	
	public static boolean clusterChains = true;
	
	public static UmlsMetaThesaurus objUmlsMetaThesaurus = new UmlsMetaThesaurus();
	
	public static void main ( String[] args ) throws Exception {
	
		String testDataDir = "", trainDataDir = "";
		
		for ( int i=0; i<args.length; i++ ) {
			if ( args[i].equalsIgnoreCase("-testdata") )
				testDataDir = args[i+1];
			else if ( args[i].equalsIgnoreCase("-traindata") )
				trainDataDir = args[i+1];
			else if ( args[i].equalsIgnoreCase("-cluster") )
				clusterChains = true;
		}
		
		if ( clusterChains ) {
			FileUtility.createDirectory(testDataDir + "/chains_out");
			
			new Resolver().extractAnaphoraAndAntecedents(testDataDir + "/predict", 
				testDataDir + "/pairs_out", 
				testDataDir + "/predict", 
				testDataDir + "/concepts", 
				testDataDir + "/chains_out",
				testDataDir + "/docs",
				testDataDir + "/parse_full"
				);
		}
		else {
			generateInstancesForTrainAndTest(trainDataDir, testDataDir);
		}
	}
	
	
	/**
	 * 
	 * @param parentTrainingDir
	 * @param parentTestDir
	 * @throws Exception
	 */
	public static void generateInstancesForTrainAndTest ( String parentTrainingDir, String parentTestDir ) throws Exception { 
		
		String[] args = new String[] { 
				"-chainTrainF",
				parentTrainingDir + "/chains",
				"-conceptTrainF",
				parentTrainingDir + "/concepts",
				"-pairTrainF",
				parentTrainingDir + "/pairs",
				"-textTrainF",
				parentTrainingDir + "/docs",
				"-inFileForTrainer",
				"tmp/train.in",
				"-parseTrainF",
				parentTrainingDir + "/parse_full",
				
				"-chainTestF",
				parentTestDir + "/chains",
				"-conceptTestF",
				parentTestDir + "/concepts",
				"-pairTestF",
				parentTestDir + "/pairs",
				"-textTestF",
				parentTestDir + "/docs",
				"-inFolderForPrediction",
				parentTestDir + "/predict",
				"-parseTestF",
				parentTestDir + "/parse_full"};
		
		String dirConcept = "", dirChain = "", dirPair = "", dirText = "", inFileForTrainer = "", dirParse = "",
				inFolderForPrediction = ""; 
		
		for ( int i=0; i<args.length; i++ ) {
			
			if ( args[i].equals("-chainTrainF") ) {
				dirChain = args[i+1].trim();
				i++;
			}
			
			else if ( args[i].equals("-conceptTrainF") ) {
				dirConcept = args[i+1].trim();
				i++;
			}

			else if ( args[i].equals("-pairTrainF") ) {
				dirPair = args[i+1].trim();
				i++;
			}

			else if ( args[i].equals("-textTrainF") ) {
				dirText = args[i+1].trim();
				i++;
			}
			
			else if ( args[i].equals("-parseTrainF") ) {
				dirParse = args[i+1].trim();
				i++;
			}
			
			else if ( args[i].equals("-inFileForTrainer") ) {
				inFileForTrainer = args[i+1].trim();
				i++;
			}
		}
		
		//FileUtility.createDirectory(dirParse + "_full");
	//	new Preprocessor().createCompleteParseOutput(dirParse, dirParse + "_full");
		//dirParse = dirParse + "_full";
		
		new Trainer().createInputForTrainer(dirChain, dirConcept, dirPair, dirText, dirParse, inFileForTrainer);
		

		for ( int i=0; i<args.length; i++ ) {
			
			if ( args[i].equals("-chainTestF") ) {
				dirChain = args[i+1].trim();
				i++;
			}
			
			else if ( args[i].equals("-conceptTestF") ) {
				dirConcept = args[i+1].trim();
				i++;
			}

			else if ( args[i].equals("-pairTestF") ) {
				dirPair = args[i+1].trim();
				i++;
			}

			else if ( args[i].equals("-textTestF") ) {
				dirText = args[i+1].trim();
				i++;
			}
			
			else if ( args[i].equals("-parseTestF") ) {
				dirParse = args[i+1].trim();
				i++;
			}
			
			else if ( args[i].equals("-inFolderForPrediction") ) {
				inFolderForPrediction = args[i+1].trim();
				i++;
			}
		}
		
	//	FileUtility.createDirectory(dirParse + "_full");
	//	new Preprocessor().createCompleteParseOutput(dirParse, dirParse + "_full");
	//	dirParse = dirParse + "_full";
		
		FileUtility.createDirectory(dirPair + "_out");
		
		new Resolver().createInputForPrediction(dirChain, dirConcept, dirPair, dirText, dirParse, inFolderForPrediction, dirPair + "_out");
	}
	

}
