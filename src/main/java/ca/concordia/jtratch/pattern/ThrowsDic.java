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
    		csvBW.write("id," + OpFeaturesKey + "ExceptionType,ParentMethod,ParentType,FilePath,StartLine");
    		csvBW.newLine();
    		metaCSVBW.write("id," + metaKey);
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
    
    public void PrintToFileTXT()
    {
    	logger.info("Writing ThrowsBlock features into file...");
    	Charset charset = Charset.forName("UTF-8");
    	Path file = Paths.get(IOFile.CompleteFileNameOutput("ThrowsBlock.txt"));
    	Path fileMeta = Paths.get(IOFile.CompleteFileNameOutput("ThrowsBlock_Meta.txt"));
    	
    	Integer throwsId = 0;
        String metaKey = ThrowsBlock.Splitter;
        
        for ( String meta : ThrowsBlock.MetaKeys)
        {
            metaKey += (meta + ThrowsBlock.Splitter);
        }
        
    	try 
    	(
			BufferedWriter bw = Files.newBufferedWriter(file, charset);
			BufferedWriter metaBW = Files.newBufferedWriter(fileMeta, charset);
		)
    	{
    		metaBW.write(metaKey);
    		metaBW.newLine();
    		metaBW.write("--------------------------------------------------------");
    		metaBW.newLine();
    		metaBW.write( "NumExceptionType: "+ this.keySet().size() + "." );
    		metaBW.newLine();
    	
    		for (Map.Entry<String,ThrowsList> entry : this.entrySet())
    		{
    			metaBW.write("--------------------------------------------------------");
        		metaBW.newLine();
        		ThrowsList throwsList = entry.getValue();
        		metaBW.write( "Exception Type: "+ entry.getKey() + "." );
            		metaBW.newLine();
            		
            	for (ThrowsBlock throwsblock : throwsList)
            	{
            		throwsId++;
            		bw.write("id:" + throwsId + ThrowsBlock.Splitter + throwsblock.PrintFeatures());
            		bw.newLine();
            		metaBW.write("id:" + throwsId + ThrowsBlock.Splitter + throwsblock.PrintMetaInfo());
            		metaBW.newLine();
            	}
            	metaBW.newLine();
            	metaBW.newLine();
            	bw.flush();
            	metaBW.flush();
        	}
    		
    		//Print summary
            metaBW.write("------------------------ Summary -------------------------");
    		metaBW.newLine();
    		metaBW.write("Exception Type" + "\t" +
                    "NumThrows");
    		metaBW.newLine();
    		
    		for (Map.Entry<String,ThrowsList> entry : this.entrySet())
    		{
    			ThrowsList throwsList = entry.getValue();
        		metaBW.write(entry.getKey() + "\t" +
        				throwsList.size());
        		metaBW.newLine();
    		}
    		bw.close();
    		metaBW.close();
    		logger.info("Writing done.");
        	
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
