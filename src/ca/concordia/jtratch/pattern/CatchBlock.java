package ca.concordia.jtratch.pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ca.concordia.jtratch.utility.IOFile;

public class CatchBlock extends CommonFeature {
	public String ExceptionType;
    public String FilePath;
    public static List<String> MetaKeys;

    public CatchBlock() 
    {
        //OperationFeatures.put("Abort", 0); 
        OperationFeatures.put("EmptyBlock", 0);
        OperationFeatures.put("ToDo", 0);
        OperationFeatures.put("LogOnly", 0);
        OperationFeatures.put("CatchException", 0);
        OperationFeatures.put("RecoverFlag", 0);
        OperationFeatures.put("OtherOperation", 0);
        MetaInfo.put("RecoverFlag", "-recoverflag");
        MetaInfo.put("OtherOperation", "-otheroperation");
        MetaInfo.put("CatchBlock", "-catchblock");
        MetaKeys = new ArrayList<String>(MetaInfo.keySet());
    }

    public String PrintFeatures() 
    {
        //List<String> features = new ArrayList<String>();
        
        String features = "";
        
        for (Map.Entry<String, Integer> entry : OperationFeatures.entrySet()) 
        {
        	features += (entry.getKey() + ":" + entry.getValue() + Splitter);
		}
        features += (ExceptionType + Splitter);
        
        //TextFeatures.forEach((key,value) -> features += (key + ":" + value + Splitter));
        
        return features;
    }

    public String PrintCSV()
    {
        String csv = "";
        csv += (FilePath + ",");
        for (Map.Entry<String, Integer> entry : OperationFeatures.entrySet())
        {
            csv += (entry.getKey() + ":" + entry.getValue() + ",");
        }
        csv += (ExceptionType);
        
        return csv;
    }

    public String PrintMetaInfo()
    {
        String metaInfo = "";
        for (Map.Entry<String, String> entry : MetaInfo.entrySet())
        {
            metaInfo += (IOFile.DeleteSpace(entry.getValue()) + Splitter);
        }
        return metaInfo;
    }
}
