package ca.concordia.jpexjd;

import java.util.HashMap;

public class MyInterface {
	private String name;
	private HashMap<String, MyMethod> methods = new HashMap<String, MyMethod>();
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MyInterface(String name) {
		this.name = name;		
	}
	
	public HashMap<String, MyMethod> getMethods() {
		return methods;
	}
}
