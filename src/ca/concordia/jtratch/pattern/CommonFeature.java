package ca.concordia.jtratch.pattern;

import java.util.HashMap;
import java.util.Map;

import ca.concordia.jtratch.utility.IOFile;

public class CommonFeature {

	public Map<String, Integer> OperationFeatures;
    //public Map<String, Integer> TextFeatures; //TextFeatures is only on the API call
	public String FilePath;
    public Map<String, String> MetaInfo;

    public static final String Splitter = "\t";

    public CommonFeature()
    {
    	OperationFeatures = new HashMap<String, Integer>();
        MetaInfo = new HashMap<String, String>();

        OperationFeatures.put("Line", 0);
        OperationFeatures.put("LOC", 0);
        OperationFeatures.put("Start", 0);
        OperationFeatures.put("Length", 0);
        
        MetaInfo.put("FilePath", "-filepath");
        MetaInfo.put("Line", "-line");
        
        
    }
    public String PrintFeatures(String type) 
    {
        //List<String> features = new ArrayList<String>();
        
        String features = "";
        
        for (Map.Entry<String, Integer> entry : OperationFeatures.entrySet()) 
        {
        	features += (entry.getKey() + ":" + entry.getValue() + Splitter);
		}
        features += (type + Splitter);
        
        //TextFeatures.forEach((key,value) -> features += (key + ":" + value + Splitter));
        
        return features;
    }

    public String PrintCSV(String type)
    {
        String csv = "";
        csv += (FilePath + ",");
        for (Map.Entry<String, Integer> entry : OperationFeatures.entrySet())
        {
            csv += (entry.getKey() + ":" + entry.getValue() + ",");
        }
        csv += (type);
        
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
