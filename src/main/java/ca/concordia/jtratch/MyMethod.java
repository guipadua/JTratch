package ca.concordia.jtratch;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MyMethod {

    public String Name;
    public MethodDeclaration Declaration;
    public HashMap<String, HashMap<String, Byte>> Exceptions = new HashMap<String, HashMap<String, Byte>>();
    public int ChildrenMaxLevel = 0;
    public boolean IsVisited = false;
    
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

	public HashMap<String, HashMap<String, Byte>> getExceptions() {
		return Exceptions;
	}

	public void setExceptions(HashMap<String, HashMap<String, Byte>> exceptions) {
		Exceptions = exceptions;
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
        
