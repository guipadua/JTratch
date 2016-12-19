package ca.concordia.jtratch.pattern;

import java.util.ArrayList;
import java.util.List;

public class ThrowsBlock extends CommonFeature {
	public static List<String> MetaKeys;
    public static List<String> OpFeaturesKeys;

    public ThrowsBlock() 
    {
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
   
}
