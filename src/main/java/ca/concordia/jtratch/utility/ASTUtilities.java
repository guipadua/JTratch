package ca.concordia.jtratch.utility;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

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
}
