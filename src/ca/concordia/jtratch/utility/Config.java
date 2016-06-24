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
    static public Integer AssertConditionIndex;
    static public Boolean Orthogonal;
	static public String[] AbortMethods;
    private static final Logger logger = LogManager.getLogger(CatchDic.class.getName());
	
    static public void Load(String FileName)
    {
    	logger.trace("Reading Config file...");
    	Charset charset = Charset.forName("UTF-8");
    	Path file = Paths.get(IOFile.CompleteFileName(FileName));
    	
    	List<String> allLines = new ArrayList<String>();
    	try {
			allLines = Files.readAllLines(file, charset);
			
			Iterator<String> linesIterator = allLines.iterator();
			
			LogMethods = linesIterator.next().split("%")[0].split(",");
			NotLogMethods = linesIterator.next().split("%")[0].split(",");
			LogLevelArgPos = Integer.parseInt(linesIterator.next().split("%")[0]);
			String temp = linesIterator.next().split("%")[0];
			
			if (temp.equals("O"))
				Orthogonal = true;
			else if (temp.equals("N"))
				Orthogonal = false;
			else throw new IOException();
			AssertConditionIndex = Integer.parseInt(linesIterator.next().split("%")[0]);
			AbortMethods = linesIterator.next().split("%")[0].split(",");
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Illegal Configure File Format.");
		} finally {
			logger.trace("Config file read succesfully.");
		}
    }
}
