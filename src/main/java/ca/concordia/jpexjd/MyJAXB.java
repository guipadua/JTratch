package ca.concordia.jpexjd;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.github.markusbernhardt.xmldoclet.xjc.Root;

public class MyJAXB {

	private Root root;
	private MyRoot myRoot = new MyRoot();
	
	public MyJAXB(String jreJsonFilePath) throws JAXBException
	{
		JAXBContext jc = JAXBContext.newInstance("com.github.markusbernhardt.xmldoclet.xjc");
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		this.root= (Root)
			    unmarshaller.unmarshal(new File(jreJsonFilePath));		
	}

	public Root getRoot() {
		return this.root;
	}
	
	public MyRoot getMyRoot() {
		
		root.getPackage().forEach(packag -> {
			MyPackage myPackage = new MyPackage(packag.getName());			
			packag.getClazz().forEach(clazz -> {
				MyClass myClass = new MyClass(clazz.getName());
				clazz.getMethod().forEach(method -> {
					MyMethod myMethod = new MyMethod(method.getName() + method.getSignature());
					method.getTag().forEach( tag -> {
						
						if(tag.getName().contains("throws")){
							MyThrowTag myThrowTag = new MyThrowTag(tag.getText(), true);
							myMethod.getThrowTags().put(myThrowTag.getName(), myThrowTag);
						}						
					});
					myClass.getMethods().put(myMethod.getNameAndSignature(), myMethod);
				});
				
				clazz.getConstructor().forEach(method -> {
					MyMethod myMethod = new MyMethod(method.getName() + method.getSignature());
					method.getTag().forEach( tag -> {
						
						if(tag.getName().contains("throws")){
							MyThrowTag myThrowTag = new MyThrowTag(tag.getText(), true);
							myMethod.getThrowTags().put(myThrowTag.getName(), myThrowTag);
						}						
					});
					myClass.getMethods().put(myMethod.getNameAndSignature(), myMethod);
				});
				
				myPackage.getClasses().put(myClass.getName(), myClass);
			});
			
			packag.getInterface().forEach(interf -> {
				MyInterface myInterface = new MyInterface(interf.getQualified());
				interf.getMethod().forEach(method -> {
					MyMethod myMethod = new MyMethod(method.getName() + method.getSignature());
					method.getTag().forEach( tag -> {
						
						if(tag.getName().contains("throws")){
							MyThrowTag myThrowTag = new MyThrowTag(tag.getText(), true);
							myMethod.getThrowTags().put(myThrowTag.getName(), myThrowTag);
						}						
					});
					myInterface.getMethods().put(myMethod.getNameAndSignature(), myMethod);
				});
				myPackage.getInterfaces().put(myInterface.getName(), myInterface);
			});
			myRoot.getPackages().put(myPackage.getName(), myPackage);
		});
		
		return myRoot;
	}
	
}
