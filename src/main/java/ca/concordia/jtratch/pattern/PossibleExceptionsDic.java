package ca.concordia.jtratch.pattern;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ca.concordia.jtratch.utility.IOFile;

public class PossibleExceptionsDic extends HashMap<String, PossibleExceptionsList> {
	public int NumPossibleExceptions = 0;
    
	private static final Logger logger = LogManager.getLogger(PossibleExceptionsDic.class.getName());
	
    public void Add(List<PossibleExceptionsBlock> possibleExceptionsList)
    {
        for (PossibleExceptionsBlock possibleExceptionsBlock : possibleExceptionsList)
        {
            if (possibleExceptionsBlock == null) continue;
            NumPossibleExceptions++;
            String exception = possibleExceptionsBlock.ExceptionType;
            if (this.containsKey(exception))
            {
                this.get(exception).add(possibleExceptionsBlock);
            }
            else
            {
                //Create a new list for this type.
                this.put(exception, new PossibleExceptionsList());
                this.get(exception).add(possibleExceptionsBlock);
            }
        }
    }

    public void PrintToFileCSV()
    {
    	logger.info("Writing PossibleExceptionsBlock features into file...");
    	Charset charset = Charset.forName("UTF-8");
    	Path fileCSV = Paths.get(IOFile.CompleteFileNameOutput("PossibleExceptionsBlock.csv"));
//    	Path fileMetaCSV = Paths.get(IOFile.CompleteFileNameOutput("PossibleExceptionsBlock_Meta.csv"));
    	
    	Integer possibleExceptionsId = 0;
//        String metaKey = "";
        
//        for ( String meta : PossibleExceptionsBlock.MetaKeys)
//        {
//            metaKey += (meta + ",");
//        }
        
        String OpFeaturesKey = "";
        
        for ( String OpFeature : PossibleExceptionsBlock.OpFeaturesKeys)
        {
        	OpFeaturesKey += (OpFeature + ",");
        }
        
    	try 
    	(
			BufferedWriter csvBW = Files.newBufferedWriter(fileCSV, charset);
    		//BufferedWriter metaCSVBW = Files.newBufferedWriter(fileMetaCSV, charset);
		)
    	{
    		csvBW.write("id," + OpFeaturesKey + "ThrownType,CaughtType,DeclaringMethod,InvokedMethod,InvokedMethodLine,FilePath,StartLine");
    		csvBW.newLine();
//    		metaCSVBW.write("id," + metaKey);
//    		metaCSVBW.newLine();
    		
    		for (Map.Entry<String,PossibleExceptionsList> entry : this.entrySet())
    		{
    			PossibleExceptionsList possibleExceptionsList = entry.getValue();
        		for (PossibleExceptionsBlock possibleExceptionsBlock : possibleExceptionsList)
            	{
            		possibleExceptionsId++;
            		csvBW.write(possibleExceptionsId + "," + possibleExceptionsBlock.PrintFeaturesCSV());
            		csvBW.newLine();
//            		metaCSVBW.write(possibleExceptionsId + "," + possibleExceptionsBlock.PrintMetaInfoCSV());
//            		metaCSVBW.newLine();
            		
            	}
            	csvBW.flush();
//            	metaCSVBW.flush();
    		}
    		
    		csvBW.close();
//    		metaCSVBW.close();
    		logger.info("Writing done.");
        	
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
