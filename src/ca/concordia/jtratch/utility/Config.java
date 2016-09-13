package ca.concordia.jtratch.utility;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ca.concordia.jtratch.pattern.CatchDic;

public class Config {
	static public String[] LogMethods; // "WriteError"
    static public String[] NotLogMethods; // "TraceUtil.If"
    static public Integer LogLevelArgPos; // ="2"
    static public String[] AbortMethods;
	static public String[] DefaultMethods;
    private static final Logger logger = LogManager.getLogger(CatchDic.class.getName());
	
    static public void Load(String FileName)
    {
    	logger.info("Reading Config file...");
    	Charset charset = Charset.forName("UTF-8");
    	Path file = Paths.get(IOFile.CompleteFileNameInput(FileName));
    	
    	List<String> allLines = new ArrayList<String>();
    	try {
			allLines = Files.readAllLines(file, charset);
    	} catch (IOException e) {
			// use the default:
    		logger.warn("File not found: using default hardcoded values.");
    		
    		allLines.add("log,info,warn,error,trace,debug,fatal%	 	LogMethods");
    		allLines.add("println,print% NotLogMethods");
    		allLines.add("0%					LogLevelIndex");
    		allLines.add("abort,exit%	 	AbortMethods");
    		allLines.add("printStackTrace%	 	DefaultMethods");
    			
		} finally {
			Iterator<String> linesIterator = allLines.iterator();
			
			LogMethods = linesIterator.next().split("%")[0].split(",");
			NotLogMethods = linesIterator.next().split("%")[0].split(",");
			LogLevelArgPos = Integer.parseInt(linesIterator.next().split("%")[0]);
			AbortMethods = linesIterator.next().split("%")[0].split(",");
			DefaultMethods = linesIterator.next().split("%")[0].split(",");
			
			logger.info("Config file read succesfully.");
		}	
			
			
		
    }
}
