package ca.concordia.jtratch.pattern;

import java.util.HashMap;
import java.util.Map;

public class CommonFeature {

	public Map<String, Integer> OperationFeatures;
    //public Map<String, Integer> TextFeatures; //TextFeatures is only on the API call
    public Map<String, String> MetaInfo;

    public static final String Splitter = "\t";

    public CommonFeature()
    {
    	OperationFeatures = new HashMap<String, Integer>();
        MetaInfo = new HashMap<String, String>();

        OperationFeatures.put("Line", 0);
        OperationFeatures.put("LOC", 0);
        OperationFeatures.put("CatchStart", 0);
        OperationFeatures.put("CatchLength", 0);
        
        OperationFeatures.put("Logged", 0);
        OperationFeatures.put("Abort", 0);
        OperationFeatures.put("Thrown", 0);
        OperationFeatures.put("SetLogicFlag", 0);
        OperationFeatures.put("Return", 0);
        OperationFeatures.put("NumMethod", 0);
        OperationFeatures.put("NumExceptions", 0);
        MetaInfo.put("FilePath", "-filepath");
        MetaInfo.put("Line", "-line");
        MetaInfo.put("Logged", "-logged");
        MetaInfo.put("Abort", "-abort");
        //MetaInfo.put("CatchException", "-CatchException");
        MetaInfo.put("Thrown", "-thrown");
        MetaInfo.put("SetLogicFlag", "-setlogicflag");
        MetaInfo.put("Return", "-return");
    }
	
}
