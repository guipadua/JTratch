package ca.concordia.jtratch.pattern;

import java.util.LinkedHashMap;
import java.util.Map;

import ca.concordia.jtratch.utility.IOFile;

public class CommonFeature {

	public LinkedHashMap<String, Integer> OperationFeatures;
    //public Map<String, Integer> TextFeatures; //TextFeatures is only on the API call
	public String FilePath;
	public Integer StartLine;
	public String ExceptionType;
	public String ParentType;
	public String ParentMethod;
	
    public LinkedHashMap<String, String> MetaInfo;

    public static final String Splitter = "\t";

    public CommonFeature()
    {
    	OperationFeatures = new LinkedHashMap<String, Integer>();
        MetaInfo = new LinkedHashMap<String, String>();

        OperationFeatures.put("TryStartLine", 0);
        OperationFeatures.put("TryEndLine", 0);
        OperationFeatures.put("TryLOC", 0);
        OperationFeatures.put("CatchStartLine", 0);
        OperationFeatures.put("CatchEndLine", 0);
        OperationFeatures.put("CatchLOC", 0);
        OperationFeatures.put("CatchStart", 0);
        OperationFeatures.put("CatchLength", 0);
        OperationFeatures.put("MethodStartLine", 0);
        OperationFeatures.put("MethodEndLine", 0);
        OperationFeatures.put("MethodLOC", 0);
        
//        MetaInfo.put("FilePath", "'-filepath");
//        MetaInfo.put("StartLine", "'-startline");
//        MetaInfo.put("ExceptionType", "'-exceptiontype");
//        MetaInfo.put("ParentType", "'-parenttype");
//        MetaInfo.put("ParentMethod", "'-parentmethod");
        
        MetaInfo.put("TryLine", "'-tryline");
        
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
        features += (ParentMethod + Splitter);
        features += (ParentType + Splitter);
        features += (FilePath + Splitter);
        features += (StartLine + Splitter);
        
        //TextFeatures.forEach((key,value) -> features += (key + ":" + value + Splitter));
        
        return features;
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

    public String PrintFeaturesCSV()
    {
        String csv = "";
        
        for (Map.Entry<String, Integer> entry : OperationFeatures.entrySet())
        {
            csv += (entry.getValue() + ",");
        }
        csv += '"' + (ExceptionType.replace(String.valueOf('"'),"") + '"' + ",");
        csv += '"' + (ParentMethod.replace(String.valueOf('"'),"") + '"' + ",");
        csv += '"' + (ParentType.replace(String.valueOf('"'),"") + '"' + ",");
        csv += '"' + (FilePath.replace(String.valueOf('"'),"") + '"' + ",");
        csv += (StartLine);
        
        return csv;
    }
    public String PrintMetaInfoCSV()
    {
        String csv = "";
        
        for (Map.Entry<String, String> entry : MetaInfo.entrySet())
        {
            csv += '"' + (entry.getValue().replace(String.valueOf('"'),"") + '"' + ",");
        }
        csv += '"' + (ExceptionType.replace(String.valueOf('"'),"") + '"' + ",");
        csv += '"' + (ParentMethod.replace(String.valueOf('"'),"") + '"' + ",");
        csv += '"' + (ParentType.replace(String.valueOf('"'),"") + '"' + ",");
        csv += '"' + (FilePath.replace(String.valueOf('"'),"") + '"' + ",");
        csv += (StartLine);
        
        return csv;
    }
    

    
}
