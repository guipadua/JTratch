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

public class CatchDic extends HashMap<String, CatchList> {
	public int NumCatch = 0;
	public int NumBinded = 0;
	public int NumRecoveredBinding = 0;
	public int NumMethodsNotBinded = 0;
	public int NumLogged = 0;
    public int NumThrown = 0;
    public int NumLoggedAndThrown = 0;
    public int NumLoggedNotThrown = 0;
    private static final Logger logger = LogManager.getLogger(CatchDic.class.getName());
	
    public void Add(List<CatchBlock> catchList)
    {
        for (CatchBlock catchBlock : catchList)
        {
            if (catchBlock == null) continue;
            NumCatch++;
            String exception = catchBlock.ExceptionType;
            if (this.containsKey(exception))
            {
                this.get(exception).add(catchBlock);
            }
            else
            {
                //Create a new list for this type.
                this.put(exception, new CatchList());
                this.get(exception).add(catchBlock);
            }

            //Update Statistics
            if (catchBlock.OperationFeatures.get("Logged") == 1)
            {
                this.get(exception).NumLogged++;
                NumLogged++;
                if (catchBlock.OperationFeatures.get("NumThrown") > 0)
                {
                    this.get(exception).NumLoggedAndThrown++;
                    NumLoggedAndThrown++;
                }
                else
                {
                    this.get(exception).NumLoggedNotThrown++;
                    NumLoggedNotThrown++;
                }
            }
            if (catchBlock.OperationFeatures.get("NumThrown") > 0)
            {
                this.get(exception).NumThrown++;
                NumThrown++;
            }
            if (catchBlock.OperationFeatures.get("Binded") == 1)
            {
                this.get(exception).NumBinded++;
                NumBinded++;
            }
            if (catchBlock.OperationFeatures.get("RecoveredBinding") == 1)
            {
                this.get(exception).NumRecoveredBinding++;
                NumRecoveredBinding++;
            }
            if (catchBlock.OperationFeatures.get("NumMethodsNotBinded") > 0)
            {
                this.get(exception).NumMethodsNotBinded++;
                NumMethodsNotBinded++;
            }
        }
    }

    public void PrintToFileCSV()
    {
    	logger.info("Writing CatchBlock features into file...");
    	Charset charset = Charset.forName("UTF-8");
    	Path fileCSV = Paths.get(IOFile.CompleteFileNameOutput("CatchBlock.csv"));
    	Path fileMetaCSV = Paths.get(IOFile.CompleteFileNameOutput("CatchBlock_Meta.csv"));
    	
    	Integer catchId = 0;
        String metaKey = "";
        
        for ( String meta : CatchBlock.MetaKeys)
        {
            metaKey += (meta + ",");
        }
        
        String OpFeaturesKey = "";
        
        for ( String OpFeature : CatchBlock.OpFeaturesKeys)
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
    		metaCSVBW.write("id," + metaKey + "ExceptionType,ParentMethod,ParentType,FilePath,StartLine");
    		metaCSVBW.newLine();
    		
    		for (Map.Entry<String,CatchList> entry : this.entrySet())
    		{
    			CatchList CatchList = entry.getValue();
        		for (CatchBlock catchBlock : CatchList)
            	{
            		catchId++;
            		csvBW.write(catchId + "," + catchBlock.PrintFeaturesCSV());
            		csvBW.newLine();
            		metaCSVBW.write(catchId + "," + catchBlock.PrintMetaInfoCSV());
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
