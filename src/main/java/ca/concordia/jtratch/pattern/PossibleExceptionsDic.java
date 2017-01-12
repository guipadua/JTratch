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
    	Path fileMetaCSV = Paths.get(IOFile.CompleteFileNameOutput("PossibleExceptionsBlock_Meta.csv"));
    	
    	Integer possibleExceptionsId = 0;
        String metaKey = "";
        
        for ( String meta : PossibleExceptionsBlock.MetaKeys)
        {
            metaKey += (meta + ",");
        }
        
        String OpFeaturesKey = "";
        
        for ( String OpFeature : PossibleExceptionsBlock.OpFeaturesKeys)
        {
        	OpFeaturesKey += (OpFeature + ",");
        }
        
    	try 
    	(
			BufferedWriter csvBW = Files.newBufferedWriter(fileCSV, charset);
    		BufferedWriter metaCSVBW = Files.newBufferedWriter(fileMetaCSV, charset);
		)
    	{
    		csvBW.write("id," + OpFeaturesKey + "ThrownType,CaughtType,DeclaringMethod,InvokedMethod,InvokedMethodLine,CatchFilePath,CatchStartLine");
    		csvBW.newLine();
    		metaCSVBW.write("id," + metaKey);
    		metaCSVBW.newLine();
    		
    		for (Map.Entry<String,PossibleExceptionsList> entry : this.entrySet())
    		{
    			PossibleExceptionsList possibleExceptionsList = entry.getValue();
        		for (PossibleExceptionsBlock possibleExceptionsBlock : possibleExceptionsList)
            	{
            		possibleExceptionsId++;
            		csvBW.write(possibleExceptionsId + "," + possibleExceptionsBlock.PrintFeaturesCSV());
            		csvBW.newLine();
            		metaCSVBW.write(possibleExceptionsId + "," + possibleExceptionsBlock.PrintMetaInfoCSV());
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
    	Path file = Paths.get(IOFile.CompleteFileNameOutput("PossibleExceptionsBlock.txt"));
    	Path fileMeta = Paths.get(IOFile.CompleteFileNameOutput("PossibleExceptionsBlock_Meta.txt"));
    	//Path fileCSV = Paths.get(IOFile.CompleteFileNameOutput("PossibleExceptionsBlock.csv"));
    	//Path fileMetaCSV = Paths.get(IOFile.CompleteFileNameOutput("PossibleExceptionsBlock_Meta.csv"));
    	
    	Integer possibleExceptionsId = 0;
        String metaKey = PossibleExceptionsBlock.Splitter;
        
        for ( String meta : PossibleExceptionsBlock.MetaKeys)
        {
            metaKey += (meta + PossibleExceptionsBlock.Splitter);
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
    	
    		for (Map.Entry<String,PossibleExceptionsList> entry : this.entrySet())
    		{
    			metaBW.write("--------------------------------------------------------");
        		metaBW.newLine();
        		PossibleExceptionsList possibleExceptionsList = entry.getValue();
        		metaBW.write( "Exception Type: "+ entry.getKey() + "." );
            		metaBW.newLine();
            		
            	for (PossibleExceptionsBlock possibleExceptionsBlock : possibleExceptionsList)
            	{
            		possibleExceptionsId++;
            		bw.write("id:" + possibleExceptionsId + ThrowsBlock.Splitter + possibleExceptionsBlock.PrintFeatures());
            		bw.newLine();
            		metaBW.write("id:" + possibleExceptionsId + ThrowsBlock.Splitter + possibleExceptionsBlock.PrintMetaInfo());
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
    		
    		for (Map.Entry<String,PossibleExceptionsList> entry : this.entrySet())
    		{
    			PossibleExceptionsList possibleExceptionsList = entry.getValue();
        		metaBW.write(entry.getKey() + "\t" +
        				possibleExceptionsList.size());
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
