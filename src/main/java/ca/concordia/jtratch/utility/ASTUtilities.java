package ca.concordia.jtratch.utility;

import org.eclipse.jdt.core.dom.ASTNode;
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
}
