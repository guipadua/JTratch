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

public class ThrowsDic extends HashMap<String, ThrowsList> {
	public int NumThrows = 0;
    
	private static final Logger logger = LogManager.getLogger(ThrowsDic.class.getName());
	
    public void Add(List<ThrowsBlock> throwsList)
    {
        for (ThrowsBlock throwsBlock : throwsList)
        {
            if (throwsBlock == null) continue;
            NumThrows++;
            String exception = throwsBlock.ExceptionType;
            if (this.containsKey(exception))
            {
                this.get(exception).add(throwsBlock);
            }
            else
            {
                //Create a new list for this type.
                this.put(exception, new ThrowsList());
                this.get(exception).add(throwsBlock);
            }
        }
    }

    public void PrintToFileCSV()
    {
    	logger.info("Writing ThrowsBlock features into file...");
    	Charset charset = Charset.forName("UTF-8");
    	Path fileCSV = Paths.get(IOFile.CompleteFileNameOutput("ThrowsBlock.csv"));
    	Path fileMetaCSV = Paths.get(IOFile.CompleteFileNameOutput("ThrowsBlock_Meta.csv"));
    	
    	Integer throwsId = 0;
        String metaKey = "";
        
        for ( String meta : ThrowsBlock.MetaKeys)
        {
            metaKey += (meta + ",");
        }
        
        String OpFeaturesKey = "";
        
        for ( String OpFeature : ThrowsBlock.OpFeaturesKeys)
        {
        	OpFeaturesKey += (OpFeature + ",");
        }
        
    	try 
    	(
			BufferedWriter csvBW = Files.newBufferedWriter(fileCSV, charset);
    		BufferedWriter metaCSVBW = Files.newBufferedWriter(fileMetaCSV, charset);
		)
    	{
    		csvBW.write("id," + OpFeaturesKey + "ExceptionType,FilePath,StartLine");
    		csvBW.newLine();
    		metaCSVBW.write("id," + metaKey + "ExceptionType,FilePath,StartLine");
    		metaCSVBW.newLine();
    		
    		for (Map.Entry<String,ThrowsList> entry : this.entrySet())
    		{
    			ThrowsList throwsList = entry.getValue();
        		for (ThrowsBlock throwsBlock : throwsList)
            	{
            		throwsId++;
            		csvBW.write(throwsId + "," + throwsBlock.PrintFeaturesCSV());
            		csvBW.newLine();
            		metaCSVBW.write(throwsId + "," + throwsBlock.PrintMetaInfoCSV());
            		metaCSVBW.newLine();
            		
            	}
            	csvBW.flush();
            	metaCSVBW.flush();
    		}
    		
    		csvBW.close();
    		metaCSVBW.close();
    		logger.info("Writing done.");
        	
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
