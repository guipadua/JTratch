package ca.concordia.jtratch.utility;

import org.eclipse.jdt.core.dom.ASTNode;
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
	public static String getMethodName ( MethodDeclaration method, boolean quotes){
		
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
			
			if (subType.equals(referenceType.getSuperclass())) 
				return true;
			
			return IsSuperType(subType, referenceType.getSuperclass());
	  			
	  }
}
