package ca.concordia.jpexjd;

import java.util.HashMap;

public class MyPackage {
		private String name;
		
		HashMap<String, MyClass> classes = new HashMap<String, MyClass>() ;
		HashMap<String, MyInterface> interfaces = new HashMap<String, MyInterface>() ;
		
		public MyPackage(String name) {
			this.name = name;		
		}
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
		
		public HashMap<String, MyClass> getClasses() {
			return classes;
		}
		public HashMap<String, MyInterface> getInterfaces() {
			return interfaces;
		}		
}
