package ca.concordia.jtratch.visitors;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Type;

import ca.concordia.jtratch.pattern.ThrowsBlock;

public class MethodDeclarationVisitor extends ASTVisitor{
	List<ThrowsBlock> throwsList = new ArrayList<ThrowsBlock>();
	private static final Logger logger = LogManager.getLogger(MethodDeclarationVisitor.class.getName());
	private CompilationUnit tree;
	private String filePath;
		
	@Override
	public boolean visit(MethodDeclaration node) {
		logger.trace("Visiting a AST node of type "+ node.getNodeType() + " at line " + tree.getLineNumber(node.getStartPosition()));
		//ThrowsBlock throwsBlockInfo = new ThrowsBlock();
		
		//throws.add(node.toString());
		int size = node.thrownExceptionTypes().size();
		
		if (size != 0)
		{
			List<Type> typeList = new ArrayList<Type>();
			typeList = node.thrownExceptionTypes();
			
			typeList.forEach( type ->
					{
				    	ThrowsBlock throwsBlockInfo = new ThrowsBlock();
				    	throwsBlockInfo.ExceptionType = type.toString();
				    					    	
				    	Integer startLine = tree.getLineNumber(node.getStartPosition() + 1);
				    	Integer endLine = tree.getLineNumber(node.getStartPosition() + node.getLength() + 1);
				    	
				    	throwsBlockInfo.OperationFeatures.put("Line", startLine);
				    	throwsBlockInfo.OperationFeatures.put("LOC", endLine - startLine + 1);
				    	
				    	throwsBlockInfo.OperationFeatures.put("Start", node.getStartPosition());
				    	throwsBlockInfo.OperationFeatures.put("Length", node.getLength());
				    	
				    	throwsBlockInfo.FilePath = filePath;
				    	throwsBlockInfo.MetaInfo.put("Line", startLine.toString());
				    	throwsBlockInfo.MetaInfo.put("FilePath", filePath);
				    	
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
				    
				    }
					
					);
		    
		}
		
		return super.visit(node);
		
	}
	public void setTree(CompilationUnit cu) {
		tree = cu;
	}
	public List<ThrowsBlock> getThrowsBlockList() {
		return throwsList;
	}
	public void setFilePath(String sourceFilePath) {
		filePath = sourceFilePath;
	}
	
	
}
