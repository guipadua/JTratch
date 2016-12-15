package ca.concordia.jtratch;

import java.util.HashSet;

public class InvokedMethod {

    private String Key;
    private HashSet<ExceptionFlow> ExceptionFlowSet = new HashSet<ExceptionFlow>();
    private int ChildrenMaxLevel = 0;
    private boolean Visited = false;
    private boolean Binded = false;
    
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

	public void setExceptionFlowSet(HashSet<ExceptionFlow> exceptionFlowSet) {
		ExceptionFlowSet = exceptionFlowSet;
	}
    
    public int getChildrenMaxLevel() {
		return ChildrenMaxLevel;
	}

	public void setChildrenMaxLevel(int childrenMaxLevel) {
		ChildrenMaxLevel = childrenMaxLevel;
	}

	public boolean isVisited() {
		return Visited;
	}

	public void setVisited(boolean Visited) {
		this.Visited = Visited;
	}

	public boolean isBinded() {
		return Binded;
	}

	public void setBinded(boolean binded) {
		Binded = binded;
	}

	
}
        
