package ca.concordia.jpexjd;

import java.util.HashMap;

public class MyRoot {
	//String name;
	HashMap<String, MyPackage> packages = new HashMap<String, MyPackage>();
	
	public HashMap<String, MyPackage> getPackages() {
		return packages;
	}
	

	public MyRoot()
	{
		
	}
}
