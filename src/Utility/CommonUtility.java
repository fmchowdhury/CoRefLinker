package Utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class CommonUtility {
	

	public static String EOLmarker= "\n\n THIS IS END OF LINE .";//" EOS .";
	public static void addExtraSpaceBeforeSenEnding( String fileName ) throws Exception {
		String line = null;
		StringBuilder sb = new StringBuilder();
		
		try {
			BufferedReader input =  new BufferedReader(new FileReader(new File(fileName)));
			
			while (( line = input.readLine()) != null){
				if ( !( line = line.trim()).isEmpty() ){
					String endChar = line.substring(line.length() -1);
					if ( endChar.matches("[.?!]") )
						line = line.substring(0, line.length() -1).trim() + " " + endChar;
					else
						line = line + " .";
				}
			
				if ( !line.trim().isEmpty() )
					line = line + EOLmarker;
				
				sb.append(line + "\n");
			}
			
			input.close();
		}
		catch (IOException e) {
            e.printStackTrace();
        } 
		
		FileUtility.writeInFile(fileName, sb.toString(), false);
	}

		
	
	public static void calculateCrossFoldResult_full( String evaluationResultFileName ){
		
		ArrayList<String> outLines = FileUtility.readFileLines(evaluationResultFileName);
		
		double TP = 0, FP = 0, FN = 0;
		
		for ( int i=0; i<outLines.size(); i++ ){
			if ( outLines.get(i).contains("TP:") ) {

				String str = outLines.get(i) + " ";
				int x = str.indexOf("TP:") + 3;
				TP += Double.valueOf(str.substring(x, str.indexOf(" ", x)));
				
				x = outLines.get(i).indexOf("FP:") + 3;
				FP += Double.valueOf(str.substring(x, str.indexOf(" ", x)));
				
				x = outLines.get(i).indexOf("FN:") + 3;
				FN += Double.valueOf(str.substring(x, str.indexOf(" ", x)));	
			}
		}
		
		System.out.println(( TP + FN));
		if ( TP < 1 || (TP+FN) < 1 || (TP+FP) < 1 ){
			System.out.println(0);
			return;
		}

		//FN=FN+52;
		
		System.out.println("TP: " + TP + "\n" + "FP: " + FP + "\n" + "FN: " + FN);
		
		double recall = TextUtility.roundTwoDecimals(TP*100/(TP+FN)), precision = TextUtility.roundTwoDecimals(TP*100/(TP+FP)); 
		System.out.println("P: " + precision);
		System.out.println("R: " + recall);
		System.out.println("F: " + TextUtility.roundTwoDecimals(2*precision*recall/(precision+recall)));
		
		FN=1000-TP;
		
		System.out.println("TP: " + TP + "\n" + "FP: " + FP + "\n" + "FN: " + FN);
		
		recall = TextUtility.roundTwoDecimals(TP*100/(TP+FN));
		precision = TextUtility.roundTwoDecimals(TP*100/(TP+FP)); 
		double TN = 7009 - FP;
		double FPR = FP / (FP + TN); // false positive rate (FPR)
		//double FDR = FP / (FP + TP); // false discovery rate (FDR)
		//double ACC = (TP + TN) / (TP + FN + FP + TN); 
	    
		System.out.println("TN: " + TN);
		System.out.println("P: " + precision);
		System.out.println("R: " + recall);
		System.out.println("F: " + TextUtility.roundTwoDecimals(2*precision*recall/(precision+recall)));
		System.out.println("FPR: " + TextUtility.roundTwoDecimals(FPR*100));
		//System.out.println("FDR: " + Utility.roundTwoDecimals(FDR*100));
		//System.out.println("ACC: " + Utility.roundTwoDecimals(ACC*100));
		
		//calculateAUC("base.stat.in");
	}

	
	public static void calculateCrossFoldResult( String evaluationResultFileName ){
		
		ArrayList<String> outLines = FileUtility.readFileLines(evaluationResultFileName);
		
		double TP = 0, FP = 0, FN = 0;
		
		for ( int i=0; i<outLines.size(); i++ ){
			if ( outLines.get(i).contains("TP:") ) {

				String str = outLines.get(i) + " ";
				int x = str.indexOf("TP:") + 3;
				TP += Double.valueOf(str.substring(x, str.indexOf(" ", x)));
				
				x = outLines.get(i).indexOf("FP:") + 3;
				FP += Double.valueOf(str.substring(x, str.indexOf(" ", x)));
				
				x = outLines.get(i).indexOf("FN:") + 3;
				FN += Double.valueOf(str.substring(x, str.indexOf(" ", x)));	
			}
		}
		
		if ( TP < 1 || (TP+FN) < 1 || (TP+FP) < 1 ){
			System.out.println(0);
			return;
		}

		double recall = TextUtility.roundTwoDecimals(TP*100/(TP+FN)), precision = TextUtility.roundTwoDecimals(TP*100/(TP+FP)); 
		System.out.println((long)(TextUtility.roundTwoDecimals(2*precision*recall/(precision+recall))*100));
	}

	
	
	public static void calculateCrossFoldResult_oth( String evaluationResultFileName ){
		
		ArrayList<String> outLines = FileUtility.readFileLines(evaluationResultFileName);
		
		double TP = 0, FP = 0, FN = 0, f1 =0;
		
		for ( int i=0; i<outLines.size(); i++ ){
			if ( outLines.get(i).contains("c	tp	fp	fn	total	prec	recall	F1")
					|| outLines.get(i).contains("c\ttp\tfp\tfn\ttotal\tprec\trecall\tF1") ) {

				String[] str = outLines.get(i+1).split("\\s+");
				TP += Double.valueOf(str[1]);
				
				FP += Double.valueOf(str[2]);
				
				FN += Double.valueOf(str[3]);
				f1 += Double.valueOf(str[7]);
			}
		}
		

		//FN = 1000- TP;
		
		System.out.println("TP: " + TP + "\n" + "FP: " + FP + "\n" + "FN: " + FN);
		
		double recall = TextUtility.roundTwoDecimals(TP*100/(TP+FN)), precision = TextUtility.roundTwoDecimals(TP*100/(TP+FP));
			System.out.println("P: " + precision);
			System.out.println("R: " + recall);
			System.out.println("F: " + TextUtility.roundTwoDecimals(2*precision*recall/(precision+recall)));
			
			System.out.println(f1/10);
	}
	
	
	/**
	 * 
	 * @param predWithProbabFile
	 */
	public static void calculateAUC( String predWithProbabFile ) {
		
		ArrayList<String> allLines = FileUtility.readNonEmptyFileLines(predWithProbabFile);
		ArrayList<Double> listPosWithDecVal = new ArrayList<Double>(), 
		listNegWithDecVal = new ArrayList<Double>();
		
		for ( int i=0; i<allLines.size(); i++ ) {
			if ( allLines.get(i).trim().length() > 0 ) {
				String[] temp = allLines.get(i).trim().split("\\s+");
				
				if ( (temp[2].equals("1") && temp[0].equals("1.0")) || (temp[2].equals("0") && temp[0].equals("0.0")) )
					listPosWithDecVal.add(Double.valueOf(temp[3]));
				else
					listNegWithDecVal.add(Double.valueOf(temp[3]));
			}
		}
		
		double auc = 0;
		for ( int p=0; p<listPosWithDecVal.size(); p++ ) {
			for ( int n=0; n<listNegWithDecVal.size(); n++ ) {
				if ( listPosWithDecVal.get(p) > listNegWithDecVal.get(n) )
					auc++;
			}
		}		 
				
		auc = (auc / listPosWithDecVal.size()) / listNegWithDecVal.size();
		
		System.out.println("AUC = " + auc);
	}
	
}

