package ca.concordia.jtratch;

import java.util.HashMap;
import java.util.HashSet;

public class InvokedMethod {

    private String Key;
    private HashSet<ExceptionFlow> ExceptionFlowSet = new HashSet<ExceptionFlow>();
    private int ChildrenMaxLevel = 0;
    private boolean Binded = false;
    private boolean Declared = false;
    private boolean ExternalJavadocPresent = false;
    
    public InvokedMethod(String key, boolean binded) {
		super();
		Key = key;
		Binded = binded;
	}
    
    public String getKey() {
		return Key;
	}

	public void setKey(String key) {
		Key = key;
	}
	
	public HashSet<ExceptionFlow> getExceptionFlowSet() {
		return ExceptionFlowSet;
	}
	
	public HashSet<ExceptionFlow> getExceptionFlowSetByType() {
		HashSet<ExceptionFlow> combinedExceptionsSet = new HashSet<ExceptionFlow>();
		HashMap<String, ExceptionFlow> combinedExceptionsTemp = new HashMap<String, ExceptionFlow>();
		
		for(ExceptionFlow exception : ExceptionFlowSet)
		{
			if (!combinedExceptionsTemp.containsKey(exception.getThrownTypeName()))
				combinedExceptionsTemp.put(exception.getThrownTypeName(), exception);
			else
			{
				ExceptionFlow combinedException = combinedExceptionsTemp.get(exception.getThrownTypeName());
				
				//take original method info from the one that is identified as throw
				if(exception.getIsThrow())
					combinedException.setOriginalMethodBindingKey(exception.getOriginalMethodBindingKey());
				
				//boolean flags - do an OR to be true if any is true
				combinedException.setIsBindingInfo(combinedException.getIsBindingInfo() || exception.getIsBindingInfo());
				combinedException.setIsJavadocSemantic(combinedException.getIsJavadocSemantic() || exception.getIsJavadocSemantic());
				combinedException.setIsJavadocSyntax(combinedException.getIsJavadocSyntax() || exception.getIsJavadocSyntax());
				combinedException.setIsThrow(combinedException.getIsThrow() || exception.getIsThrow());
				
				//take deepest level found
				if(exception.getLevelFound() > combinedException.getLevelFound())
					combinedException.setLevelFound(exception.getLevelFound());
				
				//take type that is not null
				if(combinedException.getThrownType() == null && exception.getThrownType() != null)
					combinedException.setThrownType(exception.getThrownType());				
			}
    	}
		
		combinedExceptionsSet.addAll(combinedExceptionsTemp.values());
		
		return combinedExceptionsSet;
	}

	public void setExceptionFlowSet(HashSet<ExceptionFlow> exceptionFlowSet) {
		ExceptionFlowSet = exceptionFlowSet;
	}
    
    public int getChildrenMaxLevel() {
		return ChildrenMaxLevel;
	}

	public void setChildrenMaxLevel(int childrenMaxLevel) {
		ChildrenMaxLevel = childrenMaxLevel;
	}

	public boolean isBinded() {
		return Binded;
	}

	public void setBinded(boolean binded) {
		Binded = binded;
	}

	public boolean isDeclared() {
		return Declared;
	}

	public void setDeclared(boolean declared) {
		Declared = declared;
	}

	public boolean isExternalJavadocPresent() {
		return ExternalJavadocPresent;
	}

	public void setExternalJavadocPresent(boolean externalJavadocPresent) {
		ExternalJavadocPresent = externalJavadocPresent;
	}

	
}
        
