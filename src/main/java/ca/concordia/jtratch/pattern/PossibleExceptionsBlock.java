package ca.concordia.jtratch.pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PossibleExceptionsBlock extends CommonFeature {
	public String CaughtType;
	public String InvokedMethod;
	public Integer InvokedMethodLine;
	public String DeclaringMethod;
	
	public static List<String> MetaKeys;
    public static List<String> OpFeaturesKeys;

    public PossibleExceptionsBlock() 
    {
    	OperationFeatures.put("Kind", 0);
    	OperationFeatures.put("IsBindingInfo", 0);
		OperationFeatures.put("IsDocSemantic", 0);
		OperationFeatures.put("IsDocSyntax", 0);
		OperationFeatures.put("IsThrow", 0);
		OperationFeatures.put("HandlerTypeCode", 0);
		OperationFeatures.put("LevelFound", 0);
		
		//MetaInfo.put("PossibleExceptionsBlock", "'-PossibleExceptionsBlock");
           
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
        csv += '"' + (CaughtType.replace(String.valueOf('"'),"") + '"' + ",");
        csv += '"' + (DeclaringMethod.replace(String.valueOf('"'),"") + '"' + ",");
        csv += '"' + (InvokedMethod.replace(String.valueOf('"'),"") + '"' + ",");
        csv += (InvokedMethodLine + ",");
        csv += '"' + (FilePath.replace(String.valueOf('"'),"") + '"' + ",");
        csv += (StartLine);
        
        return csv;
    }
   
}
