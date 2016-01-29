package Demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import logger.LogSetup;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class UseFileParser {
	static File fileTraversal = null;
	static Logger logger = Logger.getRootLogger();
	static LogSetup log = null;
	public static void main(String[] args) {
		try {
			BufferedReader br = new BufferedReader ( new FileReader ("D:/work/TUM/study/IDP/RemainingFiles.txt"));
			log = new LogSetup("D:/work/TUM/study/IDP/RemainingFiles.log", Level.INFO);
			
			UseFileParser usFile = new UseFileParser();
			
		    
			String tempFileName = "";
			while ((tempFileName = br.readLine())!=null)
			{
				 try {			            
			           fileTraversal = new File(tempFileName); 
			           System.out.println("Started.." + fileTraversal);
			           ExecutorService executor = Executors.newSingleThreadExecutor();
			           Future<String> future = executor.submit(usFile.new Task1());
			           System.out.println(future.get(60, TimeUnit.SECONDS));
			           executor.shutdownNow();
			            //logger.info("Finished!");
			        } catch (TimeoutException e) {
			        	System.out.println(tempFileName + "|Timed out.");
			            System.out.println("Terminated!");
			        }
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	class Task1 implements Callable<String> {
	    @Override
	    public String call() throws Exception {
	    	
//			File f = new File(tempFileName);
			FileParserCore fpcore = new FileParserCore(logger);
			fpcore.parseFileCore(fileTraversal);
	        return fileTraversal + " Task completed..... !!!!!";
	    }
	}
	
	
	
}






