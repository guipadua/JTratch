package ca.concordia.jtratch.pattern;

import java.util.ArrayList;
import java.util.List;

public class CatchBlock extends CommonFeature {
	public String ExceptionType;
	public String ExceptionSuperType;
    public static List<String> MetaKeys;

    public CatchBlock() 
    {
    	//Binding info:
    	OperationFeatures.put("Binded", 0);
    	OperationFeatures.put("RecoveredBinding", -1);
    	
    	//Basic info:
    	MetaInfo.put("ExceptionType", "-exceptiontype");
		OperationFeatures.put("Checked", 0);
    	
		//Try Visitor items:
		OperationFeatures.put("RecoverFlag", 0);
		MetaInfo.put("RecoverFlag", "-recoverflag");
        OperationFeatures.put("InnerCatch", 0);
		OperationFeatures.put("ParentStartLine", 0);
		
		//Method invocation Visitor on the Catch block:
    	OperationFeatures.put("Logged", 0);
		OperationFeatures.put("MultiLog", 0);
		OperationFeatures.put("Abort", 0);
		OperationFeatures.put("Default", 0);
		OperationFeatures.put("OtherInvocation", 0);
		
		MetaInfo.put("Logged", "-logged");
        MetaInfo.put("Abort", "-abort");
        MetaInfo.put("Default", "-default");
        MetaInfo.put("OtherInvocation", "-otherinvocation");
        
		//Other specific visitors:
		OperationFeatures.put("Thrown", 0);
		OperationFeatures.put("Return", 0);
		OperationFeatures.put("Continue", 0);
		MetaInfo.put("Thrown", "-thrown");
        MetaInfo.put("Return", "-return");
        MetaInfo.put("Continue", "-continue");
        
		//Some catch block info
        OperationFeatures.put("EmptyBlock", 0);
		OperationFeatures.put("CatchException", 0);
		MetaInfo.put("TryBlock", "-tryblock");
		MetaInfo.put("CatchBlock", "-catchblock");
        OperationFeatures.put("ParentNodeType", 0);
        MetaInfo.put("ParentNodeType", "-parentnodetype");
        MetaInfo.put("ParentMethodOrType", "-parentmethodortype");
        
		//Finally block items, if existing
		MetaInfo.put("FinallyBlock", "-finallyblock");
        OperationFeatures.put("FinallyThrowing", 0);
		
		//Binding based info:
        MetaInfo.put("TryMethods", "-trymethods");
        OperationFeatures.put("NumMethod", 0);
        MetaInfo.put("TryMethodsBinded", "-trymethodsbinded");
        OperationFeatures.put("NumMethodsNotBinded", 0);
		OperationFeatures.put("NumExceptions", 0);
		OperationFeatures.put("NumSpecificHandler", 0);
    	OperationFeatures.put("NumSubsumptionHandler", 0);
    	OperationFeatures.put("NumSupersumptionHandler",0);
    	OperationFeatures.put("NumOtherHandler",0);
    	
    	//Comments info - not in the Catch Visitor
    	OperationFeatures.put("ToDo", 0);
		
    	/* // Not in Use right now:
        OperationFeatures.put("SetLogicFlag", 0);
        MetaInfo.put("SetLogicFlag", "-setlogicflag");
        OperationFeatures.put("OtherOperation", 0);
        MetaInfo.put("OtherOperation", "-otheroperation");
        */
        
        MetaKeys = new ArrayList<String>(MetaInfo.keySet());
    }
   
}
