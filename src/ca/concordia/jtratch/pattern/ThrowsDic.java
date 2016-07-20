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

    public void PrintToFile()
    {
    	logger.info("Writing ThrowsBlock features into file...");
    	Charset charset = Charset.forName("UTF-8");
    	Path file = Paths.get(IOFile.CompleteFileName("ThrowsBlock.txt"));
    	Path fileMeta = Paths.get(IOFile.CompleteFileName("ThrowsBlock_Meta.txt"));
    	Path fileCSV = Paths.get(IOFile.CompleteFileName("ThrowsBlock.csv"));
    	
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
			BufferedWriter csvBW = Files.newBufferedWriter(fileCSV, charset);
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
            		bw.write("ID:" + throwsId + ThrowsBlock.Splitter + throwsblock.PrintFeatures(throwsblock.ExceptionType));
            		bw.newLine();
            		metaBW.write("ID:" + throwsId + ThrowsBlock.Splitter + throwsblock.PrintMetaInfo());
            		metaBW.newLine();
            		csvBW.write(throwsId + "," + throwsblock.PrintCSV(throwsblock.ExceptionType));
            		csvBW.newLine();
            		
            	}
            	metaBW.newLine();
            	metaBW.newLine();
            	bw.flush();
            	metaBW.flush();
            	csvBW.flush();            	
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
    		csvBW.close();
    		logger.info("Writing done.");
        	
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
