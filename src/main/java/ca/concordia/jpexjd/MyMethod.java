package ca.concordia.jpexjd;

import java.util.HashMap;

public class MyMethod {
	
	private String nameAndSignature;
	String return_type;
	
	private HashMap<String, MyThrowTag> throwTags = new HashMap<String, MyThrowTag>();
	
	public HashMap<String, MyThrowTag> getThrowTags() {
		return throwTags;
	}

	public MyMethod(String methodName) {
		this.setNameAndSignature(methodName.replace(" ", "").replace("...", "[]"));		
	}

	public String getNameAndSignature() {
		return nameAndSignature;
	}

	public void setNameAndSignature(String nameAndSignature) {
		this.nameAndSignature = nameAndSignature;
	}
}
