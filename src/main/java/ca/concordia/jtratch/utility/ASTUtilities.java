package ca.concordia.jtratch.utility;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class ASTUtilities {
	public static ASTNode findParent (ASTNode node){
		  
		  int parentNodeType = node.getParent().getNodeType();
		  
		  if(!(parentNodeType == ASTNode.BLOCK))
			  return node.getParent();
		  
		  return findParent(node.getParent());
	  }
	  
	public static ASTNode findParentMethod (ASTNode node){
		  
		  int parentNodeType = node.getParent().getNodeType();
		  
		  if(parentNodeType == ASTNode.METHOD_DECLARATION )
		  {
			  return node.getParent();		  
		  }
		  if(parentNodeType == ASTNode.INITIALIZER)
		  {
			  return node.getParent();
		  }
		  if(parentNodeType == ASTNode.TYPE_DECLARATION)
		  {
			  return node.getParent();
		  }
		  
		  return findParentMethod(node.getParent());
	  }
	  
	public static String findParentType (ASTNode node){
		  
		  int parentNodeType = node.getParent().getNodeType();
		  
		  if(parentNodeType == ASTNode.TYPE_DECLARATION)
		  {
			  TypeDeclaration type = (TypeDeclaration) node.getParent();
			  if(type.resolveBinding() != null)
				  return type.resolveBinding().getQualifiedName();
			  else		  
				  return type.getName().getFullyQualifiedName();
		  }
		  
		  return findParentType(node.getParent());
	  }
	
	public static String getMethodName(ASTNode node) {
		
		String methodName;
		if(node.getNodeType() == ASTNode.METHOD_DECLARATION)
        {
        	MethodDeclaration parentMethod = (MethodDeclaration) node;
        	IMethodBinding parentMethodBinding = parentMethod.resolveBinding();
        	
        	methodName = (parentMethodBinding !=null) 	? 
        						parentMethodBinding.getKey() 	: 
        							ASTUtilities.getMethodNameWithoutBinding(parentMethod, false);        	
        	
        } else if (node.getNodeType() == ASTNode.INITIALIZER) {
        	methodName = "!NAME_NA!"; //name not applicable
        } else
        	methodName = "!UNEXPECTED_KIND!";
		
		return methodName;
	}

	public static String getMethodNameWithoutBinding ( MethodDeclaration method, boolean quotes){
		
		String methodName = new String();
				
		methodName = ((quotes) ? "\"" : "") + method.getName().toString();
		methodName += "(";
    	
    	for(Object param : method.parameters())
    	{
    		SingleVariableDeclaration svParam = (SingleVariableDeclaration) param;
    		methodName+= svParam.getType().toString() + ",";
    	}
    	methodName += ")" + ((quotes) ? "\"" : "");
    	
    	methodName = methodName.replace(",)",")");
		
		return methodName;
		
	}
	
	public static String getMethodNameAndSignature ( IMethodBinding nodeBindingInfo, boolean quotes){
		
		String methodName = new String();
				
		methodName = ((quotes) ? "\"" : "") + nodeBindingInfo.getName().toString();
		methodName += "(";
    	
		for(ITypeBinding param : nodeBindingInfo.getParameterTypes())
    	{
    		//SingleVariableDeclaration svParam = (SingleVariableDeclaration) param;
    		methodName+= param.getQualifiedName() + ",";
    	}
    	methodName += ")" + ((quotes) ? "\"" : "");
    	
    	methodName = methodName.replace(",)",")");
		
		return methodName;
		
	}
	
	/**
	 * Recursively find if the given subtype is a supertype of the reference type.
	 *  
	 * @param subtype type to evaluate
	 * @param referenceType initial tracing reference to detect the super type
	 */
    public static Boolean IsSuperType(ITypeBinding subType, ITypeBinding referenceType) {
		  
			if (subType == null || referenceType == null || referenceType.getQualifiedName().equals("java.lang.Object")) 
				return false;
			
			if (subType.isEqualTo(referenceType.getSuperclass())) 
				return true;
			
			return IsSuperType(subType, referenceType.getSuperclass());
	  			
	  }

	public static ASTNode FindParentTry(ASTNode node) {
		//if reach method, constructor and class stop because went too far
        if (	node.getNodeType() == ASTNode.METHOD_DECLARATION || 
        		node.getNodeType() == ASTNode.ENUM_DECLARATION || 
        		node.getNodeType() == ASTNode.TYPE_DECLARATION
        	)
            return null;
        
        //if reached catch clause means it can still pop out of the try statement. A catch clause is also a child node of a try statement =(
        //null here so that it doesnt accuse as parent try
        //check if there are catch blocks with throw statements
        if (node.getNodeType() == ASTNode.CATCH_CLAUSE)
            return null;

        if (node.getNodeType() == ASTNode.TRY_STATEMENT)
            return node;

        return FindParentTry(node.getParent());
	}
	
	public static Integer findKind(ITypeBinding exceptionType, ASTNode tree) {
		if(exceptionType == null)
			return -9;
		if(exceptionType.isEqualTo(tree.getAST().resolveWellKnownType("java.lang.RuntimeException")))
			return 0;
		else if (exceptionType.isEqualTo(tree.getAST().resolveWellKnownType("java.lang.Exception")))
			return 1;
		else if (exceptionType.isEqualTo(tree.getAST().resolveWellKnownType("java.lang.Error")))
			return 2;
		else if (exceptionType.isEqualTo(tree.getAST().resolveWellKnownType("java.lang.Throwable")))
			return 3;
		else if (exceptionType.isEqualTo(tree.getAST().resolveWellKnownType("java.lang.Object")))
			return -1;
		else
			return findKind(exceptionType.getSuperclass(), tree);
	}
	
	public static IMethodBinding getBindingInfo(ASTNode node) 
	{
		if(node.getNodeType() == ASTNode.METHOD_INVOCATION)
			return ((MethodInvocation) node).resolveMethodBinding();
		else if(node.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION)
			return ((ClassInstanceCreation) node).resolveConstructorBinding();
		else
			return null;		
	}
	public static ITypeBinding getType(String name) {
    	StringBuilder fileData = new StringBuilder();
		CompilationUnit cu = null;
		Set<ITypeBinding> exceptionType = new HashSet<ITypeBinding>();
			
		fileData.append("package test;"+ System.getProperty("line.separator"));
		fileData.append("public class Type {"+ System.getProperty("line.separator"));
		fileData.append("	public Type(){"+ System.getProperty("line.separator"));
		fileData.append("		" + name  + " x;"+ System.getProperty("line.separator"));
		fileData.append("	}"+ System.getProperty("line.separator"));
		fileData.append("}"+ System.getProperty("line.separator"));
	
		ASTParser parser = ASTParser.newParser(AST.JLS8);
	
		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
	
		parser.setBindingsRecovery(true);
		// parser.setStatementsRecovery(true);
	
		Map options = JavaCore.getOptions();
		parser.setCompilerOptions(options);
	
		parser.setUnitName("Type.java");
	
		String[] empty = {  };
	
		parser.setEnvironment(empty, empty, null, true);
		parser.setSource(fileData.toString().toCharArray());
	
		cu = (CompilationUnit) parser.createAST(null);
		
		cu.accept(new ASTVisitor() 
		{
			public boolean visit(VariableDeclarationStatement node) 
			{  
				//System.out.println(mycatch.toString());
				exceptionType.add(node.getType().resolveBinding());
				return super.visit(node);
			}
		});
		return exceptionType.iterator().next();
	}
	
	//This will count the lines of code in the flattened AST. Check NaiveASTFlattener to see all that is done for flattening.
	//The flattening was initially developed for debugging purposes only.
	public static Integer countLines(ASTNode node){
		return node.toString().length() - node.toString().replace("\n", "").length() - 2;
	}
}
