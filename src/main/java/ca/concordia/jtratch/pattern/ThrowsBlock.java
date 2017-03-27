package ca.concordia.jtratch.pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ThrowsBlock extends CommonFeature {
	public static List<String> MetaKeys;
    public static List<String> OpFeaturesKeys;

    public ThrowsBlock() 
    {
    	OperationFeatures.put("Binded", -9);
    	OperationFeatures.put("RecoveredBinding", -9);
    	OperationFeatures.put("Kind", -9);
    	
    	OperationFeatures.put("Line", 0);
		OperationFeatures.put("LOC", 0);
		OperationFeatures.put("Start", 0);
		OperationFeatures.put("Length", 0);
		
		OperationFeatures.put("NumExceptions", 0);
		OperationFeatures.put("ThrowsException", 0);
		OperationFeatures.put("ThrowsKitchenSink", 0);
		
		MetaInfo.put("ThrowsBlock", "-throwsBlock");
           
        MetaKeys = new ArrayList<String>(MetaInfo.keySet());
        OpFeaturesKeys = new ArrayList<String>(OperationFeatures.keySet());
    }
    
    @Override
    public String PrintFeaturesCSV()
    {
        String csv = "";
        
        for (Map.Entry<String, Integer> entry : OperationFeatures.entrySet())
        {
            csv += (entry.getValue() + ",");
        }
        csv += '"' + (ExceptionType.replace(String.valueOf('"'),"") + '"' + ",");
        csv += '"' + (FilePath.replace(String.valueOf('"'),"") + '"' + ",");
        csv += (StartLine);
        
        return csv;
    }
    
    @Override
    public String PrintMetaInfoCSV()
    {
        String csv = "";
        
        for (Map.Entry<String, String> entry : MetaInfo.entrySet())
        {
        	csv += '"' + (entry.getValue().replace(String.valueOf('"'),"") + '"' + ",");
        }
        csv += '"' + (ExceptionType.replace(String.valueOf('"'),"") + '"' + ",");
        csv += '"' + (FilePath.replace(String.valueOf('"'),"") + '"' + ",");
        csv += (StartLine);
        
        return csv;
    }
}
