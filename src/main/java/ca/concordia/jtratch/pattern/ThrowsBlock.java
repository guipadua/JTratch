package ca.concordia.jtratch.pattern;

import java.util.ArrayList;
import java.util.List;

public class ThrowsBlock extends CommonFeature {
	public String ExceptionType;
    public static List<String> MetaKeys;

    public ThrowsBlock() 
    {
		
		OperationFeatures.put("NumExceptions", 0);
		OperationFeatures.put("ThrowsException", 0);
		OperationFeatures.put("ThrowsKitchenSink", 0);
		
		MetaInfo.put("ThrowsBlock", "-throwsBlock");
           
        MetaKeys = new ArrayList<String>(MetaInfo.keySet());
    }
   
}
