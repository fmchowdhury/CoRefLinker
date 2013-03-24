package Utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 
 * @author Md. Faisal Mahbub Chowdhury
 *
 */

public class SystemUtility {

	/**
	 * 
	 * @param command
	 * @param outputFileName
	 */
	public static void UnixSystemCall(String command, String outputFileName) {
	    try {
	      String[] cmd = {
	          "/bin/sh",
	          "-c",
	          "ulimit -s unlimited;" + command + "  > " + outputFileName};
	      Process proc = Runtime.getRuntime().exec(cmd);
	      proc.waitFor();
	    }
	    catch (Exception e) {
	      e.printStackTrace();
	      System.err.println("\"Wrong.\"Murmurs Util.java.");
	      System.exit( -1);
	    }
	}

	/**
	 * 
	 * @param command
	 * @return
	 */
	public static String UnixSystemCall(String command) {
	    String output = new String();
	    try {
	      String[] cmd = {
	          "/bin/sh",
	          "-c",
	          //"ulimit -s unlimited;" +
	          command};
	      Process proc = Runtime.getRuntime().exec(cmd);
	      BufferedReader in = new BufferedReader(new InputStreamReader(proc.
	          getInputStream()));
	      String s;
	      while ( (s = in.readLine()) != null) {
	        output += s + "\n";
	      }
	      proc.waitFor();
	      in.close(); //Added by Qiu Long on Nov. 25, 2004
	      proc.destroy();
	    }
	    catch (Exception e) {
	      e.printStackTrace();
	      System.err.println("\"Wrong.\"Murmurs Util.java.");
	      System.exit( -1);
	    }
	 
	    return output;
	}


	/**
	 * 
	 * @param command
	 * @param dataDir
	 * @param inputFile
	 * @param outputFile
	 * @return
	 * @throws IOException 
	 */
	public static void parse(String command, String dataDir, String inputFile,
            String outputFile) throws IOException {

		String output = "";

		try {
			// if parser data dir is not found
			if (!new java.io.File(dataDir).exists()) {
				System.err.println(
						"Can't initialize the parser properly. Please check the path: " +
								command + " " + dataDir);
				System.exit( -1);
			}

		//Old code, only works on unix
		//String[] cmd = {
		//"/bin/sh",
		//"-c",
		//"ulimit -s unlimited;" + command + " " +
		//System.getProperty("parserOption") + dataDir + " " + inputFile +
		//" > " + outputFile};

			Process proc = null;

			if(System.getProperty("os.name").startsWith("Windows")){
			
				String[] cmd = {"cmd", "/c", command + " " + System.getProperty("parserOption") + dataDir + " " + inputFile};
				proc = Runtime.getRuntime().exec(cmd);
			}
			else {
				String[] cmd = {
						"/bin/sh",
						"-c",
						"ulimit -s unlimited;" + command + " " +
								System.getProperty("parserOption") + dataDir + " " + inputFile};
				proc = Runtime.getRuntime().exec(cmd);
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String s;
			
			while ( (s = in.readLine()) != null) {
				output += s + "\n";
			}
			
			proc.waitFor();
			in.close();
			proc.destroy();
		}
		catch (Exception e) {
			System.err.println("Wrong");
		}
		
		FileUtility.writeInFile(outputFile, output, false);
	}
}
