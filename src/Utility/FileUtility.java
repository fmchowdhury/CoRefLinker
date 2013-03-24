package Utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;


public class FileUtility {
	
	/**
	 * Get all the files of a given directory
	 * 
	 * @param dirPath
	 * @return
	 */
	public static File[] getAllFilesFromDirectory( String dirPath ){
		File folder = new File(dirPath + "/");
	    File[] listOfFiles = folder.listFiles();
	    
	    return listOfFiles;
	}
	
	
	/**
	 * 
	 * @param fileName
	 */
	public static void deleteFile(String fileName){
	    // A File object to represent the filename
	    File f = new File(fileName);

	    // Make sure the file or directory exists and isn't write protected
	    if (!f.exists())
	      throw new IllegalArgumentException(
	          "Delete: no such file or directory: " + fileName);

	    if (!f.canWrite())
	      throw new IllegalArgumentException("Delete: write protected: "
	          + fileName);

	    // If it is a directory, make sure it is empty
	    if (f.isDirectory()) {
	      String[] files = f.list();
	      if (files.length > 0)
	        throw new IllegalArgumentException(
	            "Delete: directory not empty: " + fileName);
	    }

	    // Attempt to delete it
	    boolean success = f.delete();

	    if (!success)
	      throw new IllegalArgumentException("Delete: deletion failed");
	}
	
	/**
	 * 
	 * @param fileName
	 * @param lineNo
	 * @return
	 */
	public static String readLineNoX(String fileName, int lineNo) {
		String line = null;
		
		try {
			BufferedReader input =  new BufferedReader(new FileReader(new File(fileName)));
			int curLine = 0;
			
			while (( line = input.readLine()) != null){	    	
				if ( curLine == lineNo )
					break;
				else
					curLine++;	    
			}
		    
			input.close();
		}
		catch (IOException e) {
            e.printStackTrace();
        } 
		
		return line;
	}
	
	/**
	 * Create tmp file with life time up to the application session
	 * @param fileName
	 */
	public static void createTmpFile( String fileName ){
		  File tempFile = null;
		  
		  try {
			  tempFile = File.createTempFile( fileName, ".tmp" );
			  System.out.println("Created temporary file with name " + tempFile.getAbsolutePath());
		  } 
		  
		  catch (IOException ex) {
			  System.err.println("Cannot create temp file: " + ex.getMessage());
		  } 	  
	}
	
	
	/**
	 * Create directory 
	 * @param dirName
	 */
	public static void createDirectory( String dirName ) {
		File file=new File(dirName);		
		if ( !file.exists() ){
			// Create directory named "tmp" inside current directory
		    boolean success = (new File(dirName)).mkdir();
		    if (success)
		      System.out.println("Directory: '" + dirName + "' created");		    
		}
	}
	
	
	/**
	 * Read inputs (e.g. sentences) which are represented by multiple lines. Two inputs must be separated by
	 * a blank line.
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<ArrayList<String>> readAllMultiLineInputs(String fileName) throws IOException {
		
		BufferedReader input =  new BufferedReader(new FileReader(new File(fileName)));
		ArrayList<ArrayList<String>> allTokenList = new ArrayList<ArrayList<String>>();
	      
		ArrayList<String> tokenList = new ArrayList<String>();
		
		String line = null;
		boolean isPrevLineEmpty = false;
		
		while (( line = input.readLine()) != null){
	    	
	    	line = line.trim();
	    	
	    	// new input
	    	if ( !line.isEmpty() ) {
	    		tokenList.add(line);
	    		isPrevLineEmpty = false;
	    	}	
	    	else if ( line.isEmpty() && isPrevLineEmpty ) {
	    		isPrevLineEmpty = false;
	    	}
	    	// end of input reached
	    	else if ( line.isEmpty() )	
	    	{
	    		allTokenList.add(tokenList);
	    		tokenList = new ArrayList<String>();
	    		isPrevLineEmpty = true;
	    	}
	    }
	    
		input.close();
	    return allTokenList;
	}
	
	
	/**
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<ArrayList<String>> readAllMultiLineInputsWithEmptyLines(String fileName) throws IOException {
		
		BufferedReader input =  new BufferedReader(new FileReader(new File(fileName)));
		ArrayList<ArrayList<String>> allTokenList = new ArrayList<ArrayList<String>>();
	    
		String line = null;
		boolean isPrevLineEmpty = false;
		
		while (( line = input.readLine()) != null){
	    	
	    	line = line.trim();
	    	
	    	// new input
	    	if ( !line.isEmpty() ) {
	    		isPrevLineEmpty = false;
	    		ArrayList<String> tokenList = new ArrayList<String>();
	    		tokenList.add(line);
		    	// loop until end of the current input reached
	    		while (( line = input.readLine()) != null && !line.isEmpty())
	    			tokenList.add(line);
	    		
	    		allTokenList.add(tokenList);
	    		isPrevLineEmpty = true;
	    	}	
	    	else if ( line.isEmpty() && isPrevLineEmpty ) 	    		
	    		allTokenList.add(new ArrayList<String>());
	    }
	    
		input.close();
	    return allTokenList;
	}
	
	
	/**
	 * 
	 * @param fileName
	 * @param contents
	 * @param isAppend
	 * @throws IOException
	 */
	public static void writeInFile( String fileName, String contents, boolean isAppend) throws IOException{
		Writer output = new BufferedWriter(new FileWriter(fileName, isAppend));
	    try {
	    	//FileWriter always assumes default encoding is OK!
	    	
	    	output.write( contents );		    
	    }
	    finally {
	      output.close();
	    }	
	}
	
	
	/**
	 * 
	 * @param dirPath
	 * @return
	 */
	public static String[] getFileNamesFromDir( String dirPath ){
		File dir = new File(dirPath);

		return dir.list();
	}
	
	/**
	 * 
	 * @param firstFileName
	 * @param secondFileName
	 * @param outFileName
	 * @throws IOException
	 */
	public static void mergeFiles( String firstFileName, String secondFileName, String outFileName) throws IOException{
		
		BufferedReader input =  new BufferedReader(new FileReader(new File(firstFileName)));	    
	    StringBuilder contents = new StringBuilder("");
	    String line = null;
	    
	    while (( line = input.readLine()) != null)
	    	contents.append(line).append("\n");
		
	    writeInFile(outFileName, contents.toString(), false);
	    
	    input =  new BufferedReader(new FileReader(new File(secondFileName)));
	    contents = new StringBuilder("");
	    
	    while (( line = input.readLine()) != null)
	    	contents.append(line).append("\n");
		
	    writeInFile(outFileName, contents.toString(), true);	    	
	}
	
	
	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public static String readFileContents( String fileName ) {
		String line = null;
		StringBuilder sb = new StringBuilder("");
		
		try {
			BufferedReader input =  new BufferedReader(new FileReader(new File(fileName)));
			
			while (( line = input.readLine()) != null)	    	
				sb.append(line).append("\n");
		    
			input.close();
		}
		catch (IOException e) {
            e.printStackTrace();
        } 
		
		return sb.toString();
	}
	
	
	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public static ArrayList<String> readFileLines( String fileName ) {
		String line = null;
		ArrayList<String> listLines = new ArrayList<String>();
				
		try {
			BufferedReader input =  new BufferedReader(new FileReader(new File(fileName)));
			
			while (( line = input.readLine()) != null)	    	
				listLines.add(line);
		    
			input.close();
		}
		catch (IOException e) {
            e.printStackTrace();
        } 
		
		return listLines;
	}
	
	
	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public static ArrayList<String> readNonEmptyFileLines( String fileName ) {
		String line = null;
		ArrayList<String> listLines = new ArrayList<String>();
				
		try {
			BufferedReader input =  new BufferedReader(new FileReader(new File(fileName)));
			
			while (( line = input.readLine()) != null)
				if ( !( line = line.trim()).isEmpty() )
					listLines.add(line);
		    
			input.close();
		}
		catch (IOException e) {
            e.printStackTrace();
        } 
		
		return listLines;
	}
}
