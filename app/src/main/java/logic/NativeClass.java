package logic;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/*
 * Implementation of NBIS resources
 *
 * There are many NIST functions available if you copy them in.
 * This doesn't work well with restricted users. You need to copy them in as each user
 * since users each have different applications spaces.
 *
 * Of particular interest are the following NIST apps:
 *
 * Bozorth3 Matcher
 * NFIQ 1.0 Fingerprint quality analyizer
 * MINDTCT Fingerprint Extractor
 */

public class NativeClass {
    /*
	public static int bozorth(String databaseTemplate){
		
		//TODO make return type int and test.
		//int score = 0;
		String score = null;
		
		try {
			FileOutputStream out = new FileOutputStream("data/data/com.biometrac.core/nbis/search/database.xyt");
			out.write(databaseTemplate.getBytes());
			out.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try{	
			Process bozorth3 = Runtime.getRuntime().exec("/data/data/com.biometrac.core/nbis/bin/bozorth3 /data/data/biometrac.bmtcamera/nbis/search/probe.xyt /data/data/biometrac.bmtcamera/nbis/search/database.xyt");
		    
		    BufferedReader reader = new BufferedReader(
		            new InputStreamReader(bozorth3.getInputStream(), Charset.defaultCharset()), 10);
		    int read;
		    char[] buffer = new char[10];
		    StringBuffer output = new StringBuffer();
		    while ((read = reader.read(buffer)) > 0) {
		        output.append(buffer, 0, read);
		    }
		    reader.close();
		    bozorth3.waitFor();
		    score = output.toString();
		    
		    
		} catch (IOException e) {
		    throw new RuntimeException(e);
		} catch (InterruptedException e) {
		    throw new RuntimeException(e);
		}
		String output = score.replaceAll("[^0-9]+", "");
		score = output.trim();
		
		return Integer.parseInt(score);
	}
	
	public static void jpegtran(String input, String output){
		
		NativeSetup.changePermission(input);
		
		try {
			Process jpegConvert = Runtime.getRuntime().exec("/data/data/com.biometrac.core/nbis/bin/jpegtran -grayscale -flip horizontal -outfile "+output+" "+input);
		 	jpegConvert.waitFor();		    
		} catch (IOException e) {
		    throw new RuntimeException(e);
		} catch (InterruptedException e) {
		    throw new RuntimeException(e);
		}
	}
	
	
	public static String mindtct(String fingerName){
			
			String imagePath = "/data/data/com.biometrac.core/nbis/img/"+fingerName+"2.jpg";
			String templatePath ="/data/data/com.biometrac.core/nbis/template/";
			String ext = ".xyt";
			
			//Changes Permission to ensure operation
			String outputString = "error!";
			
			try {
			    Process mindtct = Runtime.getRuntime().exec("/data/data/com.biometrac.core/nbis/bin/mindtct " +imagePath +" " +templatePath+fingerName);
			    mindtct.waitFor();
			    
			    BufferedReader reader = new BufferedReader(
			            new InputStreamReader(new FileInputStream(templatePath+fingerName+ext)));
			    int read;
			    char[] buffer = new char[1024];
			    StringBuffer output = new StringBuffer();
			    while ((read = reader.read(buffer)) > 0) {
			        output.append(buffer, 0, read);
			    }
			    reader.close();
			    outputString = output.toString();
			    
			} catch (IOException e) {
			    throw new RuntimeException(e);
			} catch (InterruptedException e) {
			    throw new RuntimeException(e);
			}catch(RuntimeException e2){
				
			}
			return outputString;
		}
	
	
	public static String nfiq(String imagePath, Boolean verbose){
		
		//Changes Permission to ensure operation
		NativeSetup.changePermission(imagePath);
		
		String verboseFlag = "";
		String outputString = "nfiq error!";
		
		//Verbose or Standard (1-5) output
		if(verbose = true){
			verboseFlag = "-v ";
		}
		
		try {
		    Process nfiq = Runtime.getRuntime().exec("/data/data/com.biometrac.core/nbis/bin/nfiq "+verboseFlag +imagePath);
		    			    
		    BufferedReader reader = new BufferedReader(
		            new InputStreamReader(nfiq.getInputStream()));
		    int read;
		    char[] buffer = new char[4096];
		    StringBuffer output = new StringBuffer();
		    while ((read = reader.read(buffer)) > 0) {
		        output.append(buffer, 0, read);
		    }
		    reader.close();
		    nfiq.waitFor();
		    outputString =  output.toString();
		} catch (IOException e) {
		    throw new RuntimeException(e);
		} catch (InterruptedException e) {
		    throw new RuntimeException(e);
		}
		return outputString;
	}
	*/
}
