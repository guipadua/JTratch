package ca.concordia.jtratch.visitors;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

import ca.concordia.jtratch.pattern.ThrowsBlock;
import ca.concordia.jtratch.utility.ASTUtilities;

public class MethodDeclarationVisitor extends ASTVisitor{
	List<ThrowsBlock> throwsList = new ArrayList<ThrowsBlock>();
	List<MethodDeclaration> methodDeclarationList = new ArrayList<MethodDeclaration>();
	
	private static final Logger logger = LogManager.getLogger(MethodDeclarationVisitor.class.getName());
	private CompilationUnit tree;
	private String filePath;
		
	@Override
	public boolean visit(MethodDeclaration node) {
		logger.trace("Visiting a AST node of type "+ node.getNodeType() + " at line " + tree.getLineNumber(node.getStartPosition()));
		
		//collect method declarations
		methodDeclarationList.add(node);		
		
		//Process for thrown exception types - throws analysis
		int size = node.thrownExceptionTypes().size();
		if (size != 0)
		{
			logger.trace("There are thrown exceptions - size is: " + size);
			evaluateThrownExceptionTypes(node, size);
			
		} else
			logger.trace("There are NO thrown exceptions - size is: " + size);
		
		return super.visit(node);
		
	}
	
	public void evaluateThrownExceptionTypes (MethodDeclaration node, int size) {
		
		List<Type> typeList = node.thrownExceptionTypes();
		
		//node.thrownExceptionTypes().stream(). forEach(type -> typeList.add(type.toString()));
		
		typeList.forEach( type ->
				{
			    	ThrowsBlock throwsBlockInfo = new ThrowsBlock();
			    	throwsBlockInfo.ExceptionType = type.toString();
			    	
			    	ITypeBinding exceptionTypeBinding = type.resolveBinding();
			    	
			    	//Binding info:
			        if(exceptionTypeBinding != null)
			        {
			        	throwsBlockInfo.ExceptionType = exceptionTypeBinding.getQualifiedName();
			        	throwsBlockInfo.OperationFeatures.put("Binded", 1);
			        	throwsBlockInfo.OperationFeatures.put("RecoveredBinding", exceptionTypeBinding.isRecovered() ? 1 : 0 );
			        	int kind = ASTUtilities.findKind(exceptionTypeBinding, tree);
			        	throwsBlockInfo.OperationFeatures.put("Kind", kind);
			        } else 
			        {	
			        	throwsBlockInfo.ExceptionType = type.toString();
			        	throwsBlockInfo.OperationFeatures.put("Binded", 0);
			        }
			    	
			    	Integer startLine = tree.getLineNumber(node.getStartPosition() + 1);
			    	Integer endLine = tree.getLineNumber(node.getStartPosition() + node.getLength() + 1);
			    	
			    	throwsBlockInfo.OperationFeatures.put("Line", startLine);
			    	throwsBlockInfo.OperationFeatures.put("LOC", endLine - startLine + 1);
			    	
			    	throwsBlockInfo.OperationFeatures.put("Start", node.getStartPosition());
			    	throwsBlockInfo.OperationFeatures.put("Length", node.getLength());
			    	
			    	throwsBlockInfo.FilePath = filePath;
			    	throwsBlockInfo.StartLine = startLine;
			    	//throwsBlockInfo.MetaInfo.put("FilePath", filePath);
			    	//throwsBlockInfo.MetaInfo.put("StartLine", startLine.toString());
			        
			        //NumExceptions
			    	throwsBlockInfo.OperationFeatures.put("NumExceptions", size);
			    	
			    	//ThrowsException
			        if (type.toString().equalsIgnoreCase("exception"))
			        	throwsBlockInfo.OperationFeatures.put("ThrowsException", 1);
			        
			        //ThrowsKitchenSink
			        if (size > 1)
			        	throwsBlockInfo.OperationFeatures.put("ThrowsKitchenSink", 1);
			        
			        //ThrowsBlock
			        throwsBlockInfo.MetaInfo.put("ThrowsBlock", node.toString());
			    					        
			        throwsList.add(throwsBlockInfo);
			        
			        logger.trace("throws block info registered.");
			    
			    }
				
				);
	}
	
	public void setTree(CompilationUnit cu) {
		tree = cu;
	}
	public List<ThrowsBlock> getThrowsBlockList() {
		return throwsList;
	}
	public List<MethodDeclaration> getMethodDeclarationList() {
		return methodDeclarationList;
	}
	
	public void setFilePath(String sourceFilePath) {
		filePath = sourceFilePath;
	}
	
	
}
