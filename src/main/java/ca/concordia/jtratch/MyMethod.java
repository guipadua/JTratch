package ca.concordia.jtratch;

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MyMethod {

    private String Name;
    private MethodDeclaration Declaration;
    private HashSet<ExceptionFlow> ExceptionFlowSet = new HashSet<ExceptionFlow>();
    private int ChildrenMaxLevel = 0;
    private boolean IsVisited = false;
    
    public MyMethod(String Name, MethodDeclaration Declaration)
    {
        this.Name = Name;
        this.Declaration = Declaration;            
    }

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public MethodDeclaration getDeclaration() {
		return Declaration;
	}

	public void setDeclaration(MethodDeclaration declaration) {
		Declaration = declaration;
	}

	public int getChildrenMaxLevel() {
		return ChildrenMaxLevel;
	}

	public void setChildrenMaxLevel(int childrenMaxLevel) {
		ChildrenMaxLevel = childrenMaxLevel;
	}

	public boolean isIsVisited() {
		return IsVisited;
	}

	public void setIsVisited(boolean isVisited) {
		IsVisited = isVisited;
	}
}
        
