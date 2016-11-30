/**
 * 
 */
package ca.concordia.jtratch;

//Import log4j classes.
import org.apache.logging.log4j.Logger;

import ca.concordia.jtratch.utility.Config;
import ca.concordia.jtratch.utility.IOFile;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;


/**
 * @author gbp
 * JTratch is a translation of NTratch. (an ExceptionAnalysis tool for C#, based on LogAdvisor)
 * Credits: LogAdvisor - https://github.com/cuhk-cse/LogAdvisor
 * 			Jieming Zhu, Pinjia He, Qiang Fu, Hongyu Zhang, Michael R. Lyu, and Dongmei Zhang, "Learning to Log: Helping Developers Make Informed Logging Decisions," in Proc. of ACM/IEEE ICSE, 2015.
 */
public class JTratch {

	private static final Logger logger = LogManager.getLogger(JTratch.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final String inputMode = args[0];
		final String filePath = args[1];
		final String mavenRepo = args[2];
		
		IOFile.InputFolderPath = filePath;
		IOFile.OutputFolderPath = System.getProperty("user.dir");
		IOFile.MavenRepo = mavenRepo;
		
		long startTime = System.nanoTime();
		
		//Load Config file
		Config.Load("Config.txt");
		
		logger.info("Entering application at " + System.getProperty("user.dir") + " for folder: " + filePath);
        
		System.out.println("Starting JTratch at " + System.getProperty("user.dir") + " for folder: " + filePath + ". See the log folder");
		
		CodeWalker walker = new CodeWalker();
        try {
			walker.LoadByInputMode(inputMode, filePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        long estimatedTime = System.nanoTime() - startTime;
        
		logger.info("Exiting application." + " for folder: " + filePath + "Elapsed Time (ns): " + estimatedTime );
		System.out.println("Exiting JTratch at " + filePath + ". See the log folder");

	}

}
