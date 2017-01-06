package ca.concordia.jtratch;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MyMethod {

    private String Name;
    private MethodDeclaration Declaration;
    
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
}
        
