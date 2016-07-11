package ca.concordia.jtratch.pattern;

import java.util.ArrayList;
import java.util.List;

public class CatchBlock extends CommonFeature {
	public String ExceptionType;
	public String ExceptionSuperType;
    public static List<String> MetaKeys;

    public CatchBlock() 
    {
    	OperationFeatures.put("Checked", 0);
    	OperationFeatures.put("Binded", 0);
    	OperationFeatures.put("RecoveredBinding", -1);
    	
		
    	OperationFeatures.put("Logged", 0);
		OperationFeatures.put("MultiLog", 0);
		
		OperationFeatures.put("Abort", 0);
		OperationFeatures.put("Default", 0);
		OperationFeatures.put("Thrown", 0);
		OperationFeatures.put("SetLogicFlag", 0);
		OperationFeatures.put("Return", 0);
		OperationFeatures.put("Continue", 0);
		OperationFeatures.put("NumMethod", 0);
		OperationFeatures.put("NumExceptions", 0);
		 
		OperationFeatures.put("EmptyBlock", 0);
		OperationFeatures.put("ToDo", 0);
		//OperationFeatures.put("LogOnly", 0);
		OperationFeatures.put("CatchException", 0);
		OperationFeatures.put("SpecificHandler", 0);
		OperationFeatures.put("RecoverFlag", 0);
		OperationFeatures.put("OtherOperation", 0);
		
		OperationFeatures.put("FinallyThrowing", 0);
		OperationFeatures.put("InnerCatch", 0);
		OperationFeatures.put("ParentStartLine", 0);
		OperationFeatures.put("ParentNodeType", 0);
		       
        MetaInfo.put("Logged", "-logged");
        MetaInfo.put("Abort", "-abort");
        MetaInfo.put("Default", "-default");
        //MetaInfo.put("CatchException", "-CatchException");
        MetaInfo.put("Thrown", "-thrown");
        MetaInfo.put("SetLogicFlag", "-setlogicflag");
        MetaInfo.put("Return", "-return");
        MetaInfo.put("Continue", "-continue");
        
        MetaInfo.put("RecoverFlag", "-recoverflag");
        MetaInfo.put("OtherOperation", "-otheroperation");
        MetaInfo.put("CatchBlock", "-catchblock");
        MetaInfo.put("TryBlock", "-tryblock");
        MetaInfo.put("FinallyBlock", "-finallyblock");
        MetaInfo.put("ParentNodeType", "-parentnodetype");
           
        
        MetaKeys = new ArrayList<String>(MetaInfo.keySet());
    }
   
}
