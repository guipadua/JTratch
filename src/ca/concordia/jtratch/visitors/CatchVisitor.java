package ca.concordia.jtratch.visitors;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import ca.concordia.jtratch.pattern.CatchBlock;

public class CatchVisitor extends ASTVisitor {
  List<CatchBlock> catches = new ArrayList<CatchBlock>();
  private static final Logger logger = LogManager.getLogger(CatchVisitor.class.getName());
  private CompilationUnit tree;
  private String filePath;
  
@Override
public boolean visit(CatchClause node) {
	
	logger.trace("Visiting a AST node of type "+ node.getNodeType() + " at line " + tree.getLineNumber(node.getStartPosition()));
	CatchBlock catchBlockInfo = new CatchBlock();
   
	SingleVariableDeclaration exceptionType = node.getException();
	ITypeBinding exceptionTypeBinding = exceptionType.getType().resolveBinding();
	
	//Binding info:
    if(exceptionTypeBinding != null)
    {
    	catchBlockInfo.ExceptionType = exceptionTypeBinding.getQualifiedName();
    	catchBlockInfo.OperationFeatures.put("Binded", 1);
    	catchBlockInfo.OperationFeatures.put("RecoveredBinding", exceptionTypeBinding.isRecovered() ? 1 : 0 );
    	int kind = findKind(exceptionTypeBinding);
    	catchBlockInfo.OperationFeatures.put("Kind", kind);
    	catchBlockInfo.OperationFeatures.put("Checked", (kind == 1 || kind == 3) ? 1 : 0);
    } else 
    {	
    	catchBlockInfo.ExceptionType = exceptionType.getType().toString();
    	catchBlockInfo.OperationFeatures.put("Binded", 0);
    }
    
    //Basic info:
    catchBlockInfo.MetaInfo.put("ExceptionType", catchBlockInfo.ExceptionType);
    
	
    //Try info:
    TryStatement tryStatement = (TryStatement) node.getParent();
    catchBlockInfo.MetaInfo.put("TryBlock", tryStatement.getBody().toString());
    catchBlockInfo.OperationFeatures.put("ParentNodeType", findParent(tryStatement).getNodeType());
    catchBlockInfo.MetaInfo.put("ParentNodeType", findParent(tryStatement).getClass().getName());
    
    //Common Features - try/catch block
	Integer tryStartLine = tree.getLineNumber(tryStatement.getStartPosition() + 1);
	Integer tryEndLine = tree.getLineNumber(tryStatement.getStartPosition() + tryStatement.getLength() + 1);
	
	catchBlockInfo.OperationFeatures.put("TryLine", tryStartLine);
	catchBlockInfo.MetaInfo.put("TryLine", tryStartLine.toString());
    catchBlockInfo.OperationFeatures.put("TryLOC", tryEndLine - tryStartLine + 1);
	
    Integer catchStartLine = tree.getLineNumber(node.getStartPosition() + 1);
	Integer catchEndLine = tree.getLineNumber(node.getStartPosition() + node.getLength() + 1);
	
	catchBlockInfo.OperationFeatures.put("CatchLine", catchStartLine);
	catchBlockInfo.OperationFeatures.put("CatchLOC", catchEndLine - catchStartLine + 1);
    
    catchBlockInfo.OperationFeatures.put("CatchStart", node.getStartPosition());
    catchBlockInfo.OperationFeatures.put("CatchLength", node.getLength());
	
    catchBlockInfo.FilePath = filePath;
    catchBlockInfo.MetaInfo.put("FilePath", filePath);
    
    //Common Features - parent type
    catchBlockInfo.ParentType = findParentType(tryStatement);
    catchBlockInfo.MetaInfo.put("ParentType", catchBlockInfo.ParentType);
    
    //Common Features - parent method
    ASTNode parentNode = findParentMethod(tryStatement);
    
    String parentMethodName = new String();
    if(parentNode.getNodeType() == ASTNode.METHOD_DECLARATION)
    {
    	MethodDeclaration parentMethod = (MethodDeclaration) parentNode;
    	parentMethodName = "\"" + parentMethod.getName().toString();
    	parentMethodName += "(";
    	
    	for(Object param : parentMethod.parameters())
    	{
    		SingleVariableDeclaration svParam = (SingleVariableDeclaration) param;
    		parentMethodName+= svParam.getType().toString() + ",";
    	}
    	parentMethodName += ")" + "\"";
    	
    	parentMethodName = parentMethodName.replace(",)",")");
    	
    } else if (parentNode.getNodeType() == ASTNode.INITIALIZER) {
    	parentMethodName = "!initializer!"; //no name
    } else
    	parentMethodName = "review this!";
    
    catchBlockInfo.ParentMethod = parentMethodName;
    catchBlockInfo.MetaInfo.put("ParentMethod", parentMethodName);
       
    Integer parentMethodStartLine = tree.getLineNumber(parentNode.getStartPosition() + 1);
	Integer parentMethodEndLine = tree.getLineNumber(parentNode.getStartPosition() + parentNode.getLength() + 1);
	
	//Common Features
	catchBlockInfo.OperationFeatures.put("MethodLine", parentMethodStartLine);
    catchBlockInfo.OperationFeatures.put("MethodLOC", parentMethodEndLine - parentMethodStartLine + 1);
    
    /* ---------------------------
     * BEGIN CatchClause node Inner Visitors
     * Inner visitors might modify this parent node.
     * Example: TryVisitor will remove the inner try so that its content doesn't affect the other metrics.
     *  
     */
    
    //Treatment for TryStatement
    //Collection of data for statements of: Recover
    //Other possible items to be collected: (LogAdvisor do it a bit different)
    //***Remove try-catch-finally block inside for the other analysis!
    
    AST updatedAST = AST.newAST(AST.JLS8);
    CatchClause updatedCatchBlock = (CatchClause) ASTNode.copySubtree(updatedAST, node);
    
    TryVisitor tryVisitor = new TryVisitor();
    tryVisitor.setTree(tree);
    updatedCatchBlock.accept(tryVisitor);
    
    //RecoverFlag - (based on inner try blocks)
    if (!tryVisitor.getTryStatements().isEmpty())
    {
    	catchBlockInfo.MetaInfo.put("RecoverFlag", tryVisitor.getTryStatements().toString());
        catchBlockInfo.OperationFeatures.put("RecoverFlag", 1);
    }
    
    /*
     * Flagging inner catch
     * CatchClause node type is 12
     * CatchClause(12) is a child of a TryStatement (54), which is a child of a Block (8), which we wanna know the parent.
     * If 12, then it's an inner catch
     */
    if (IsInnerCatch(node.getParent()))
    {
    	catchBlockInfo.OperationFeatures.put("InnerCatch", 1);
    	catchBlockInfo.OperationFeatures.put("ParentTryStartLine", tree.getLineNumber(node.getParent().getParent().getParent().getParent().getStartPosition() + 1));
    }
        
    //Treatment for MethodInvocation
    //Collection of data for statements of: logging, abort, 
    //Other possible items to be collected: throw (check if it can happen and not fall under ThrowStatement type)
    MethodInvocationVisitor catchMethodInvocationVisitor = new MethodInvocationVisitor("catch");
    catchMethodInvocationVisitor.setTree(tree);
    updatedCatchBlock.accept(catchMethodInvocationVisitor);
    
    //Logging
    if (!catchMethodInvocationVisitor.getLoggingStatements().isEmpty())
    {
    	//Logged
    	catchBlockInfo.MetaInfo.put("Logged", catchMethodInvocationVisitor.getLoggingStatements().toString());
        catchBlockInfo.OperationFeatures.put("Logged", 1);
        
        //MultiLog
        if (catchMethodInvocationVisitor.getLoggingStatements().size() > 1)
        	catchBlockInfo.OperationFeatures.put("MultiLog", 1);
    }
    
    //Abort
    if (!catchMethodInvocationVisitor.getAbortStatements().isEmpty())
    {
    	catchBlockInfo.MetaInfo.put("Abort", catchMethodInvocationVisitor.getAbortStatements().toString());
        catchBlockInfo.OperationFeatures.put("Abort", 1);
    }
    
    //Default (example: eclipse IDE default: printStackTrace )
    if (!catchMethodInvocationVisitor.getDefaultStatements().isEmpty())
    {
    	catchBlockInfo.MetaInfo.put("Default", catchMethodInvocationVisitor.getDefaultStatements().toString());
        catchBlockInfo.OperationFeatures.put("Default", 1);
    }
    
    //GetCause
    if (!catchMethodInvocationVisitor.getGetCauseStatements().isEmpty())
    {
    	catchBlockInfo.MetaInfo.put("GetCause", catchMethodInvocationVisitor.getGetCauseStatements().toString());
        catchBlockInfo.OperationFeatures.put("GetCause", 1);
    }
    
    //Other
    if (!catchMethodInvocationVisitor.getOtherStatements().isEmpty())
    {
    	catchBlockInfo.MetaInfo.put("OtherInvocation", catchMethodInvocationVisitor.getOtherStatements().toString());
        catchBlockInfo.OperationFeatures.put("OtherInvocation", 1);
    }
    
    //Treatment for ThrowStatement
    //Collection of data for statements of: throw 
    ThrowStatementVisitor throwStatementVisitor = new ThrowStatementVisitor(exceptionType.getName());
    throwStatementVisitor.setTree(tree);
    updatedCatchBlock.accept(throwStatementVisitor);
    
    //Thrown
    if (!throwStatementVisitor.getThrowStatements().isEmpty())
    {
    	catchBlockInfo.MetaInfo.put("Thrown", throwStatementVisitor.getThrowStatements().toString());
        catchBlockInfo.OperationFeatures.put("NumThrown", throwStatementVisitor.getThrowStatements().size());
        catchBlockInfo.OperationFeatures.put("NumThrowNew", throwStatementVisitor.getNumThrowNew());
        catchBlockInfo.OperationFeatures.put("NumThrowWrapCurrentException", throwStatementVisitor.getNumThrowWrapCurrentException());
    }
    
    //updatedCatchBlock.getException().
    
    //Treatment for ReturnStatement
    //Collection of data for statements of: throw 
    ReturnStatementVisitor returnStatementVisitor = new ReturnStatementVisitor();
    returnStatementVisitor.setTree(tree);
    updatedCatchBlock.accept(returnStatementVisitor);
    
    //Return
    if (!returnStatementVisitor.getReturnStatements().isEmpty())
    {
    	catchBlockInfo.MetaInfo.put("Return", returnStatementVisitor.getReturnStatements().toString());
        catchBlockInfo.OperationFeatures.put("Return", 1);
    }
    
    //Treatment for ContinueStatement
    //Collection of data for statements of: throw 
    ContinueStatementVisitor continueStatementVisitor = new ContinueStatementVisitor();
    continueStatementVisitor.setTree(tree);
    updatedCatchBlock.accept(continueStatementVisitor);
    
    //Continue
    if (!continueStatementVisitor.getContinueStatements().isEmpty())
    {
    	catchBlockInfo.MetaInfo.put("Continue", continueStatementVisitor.getContinueStatements().toString());
        catchBlockInfo.OperationFeatures.put("Continue", 1);
    }
        
    /* 
     * END CatchClause node Inner Visitors
     * ---------------------------*/
  
    //EmptyBlock
    //It counts if only comments on it - use with comment related metrics
    if (updatedCatchBlock.getBody().statements().isEmpty())
    	catchBlockInfo.OperationFeatures.put("EmptyBlock", 1);
    
    //CatchException
    if (updatedCatchBlock.getException().getType().toString().equalsIgnoreCase("exception"))
    	catchBlockInfo.OperationFeatures.put("CatchException", 1);
    
    //FinallyThrowing
    Block finallyBlock = tryStatement.getFinally();
    if (finallyBlock != null)
    {
    	catchBlockInfo.MetaInfo.put("FinallyBlock", finallyBlock.toString());
    	
    	ThrowStatementVisitor throwStatementVisitorFinally = new ThrowStatementVisitor(exceptionType.getName());
    	throwStatementVisitorFinally.setTree(tree);
    	updatedCatchBlock.accept(throwStatementVisitorFinally);
    	finallyBlock.accept(throwStatementVisitorFinally);
    	
    	PossibleExceptionsCustomVisitor finallyPossibleExceptionsCustomVisitor = new PossibleExceptionsCustomVisitor(exceptionTypeBinding);
    	finallyPossibleExceptionsCustomVisitor.setTree(tree);
    	finallyBlock.accept(finallyPossibleExceptionsCustomVisitor);
        
    	//FinallyThrowing
    	if (! throwStatementVisitorFinally.getThrowStatements().isEmpty() 
    			|| finallyPossibleExceptionsCustomVisitor.getNumPossibleExceptions() > 0)
    		catchBlockInfo.OperationFeatures.put("FinallyThrowing", 1);
    }
    
    if(catchBlockInfo.OperationFeatures.get("Binded") == 1) 
    {
    	PossibleExceptionsCustomVisitor tryPossibleExceptionsCustomVisitor = new PossibleExceptionsCustomVisitor(exceptionTypeBinding);
    	tryPossibleExceptionsCustomVisitor.setTree(tree);
        tryStatement.getBody().accept(tryPossibleExceptionsCustomVisitor);
        
        catchBlockInfo.MetaInfo.put("TryMethods", tryPossibleExceptionsCustomVisitor.getInvokedMethodsHandlerType().toString());
        catchBlockInfo.OperationFeatures.put("NumMethod", tryPossibleExceptionsCustomVisitor.getInvokedMethodsHandlerType().size());
        
        catchBlockInfo.MetaInfo.put("TryMethodsBinded",tryPossibleExceptionsCustomVisitor.getInvokedMethodsBinded().toString());
        
        catchBlockInfo.OperationFeatures.put("NumMethodsNotBinded",tryPossibleExceptionsCustomVisitor.getNumMethodsNotBinded()); 
        
        catchBlockInfo.OperationFeatures.put("NumExceptions", tryPossibleExceptionsCustomVisitor.getNumPossibleExceptions());
        
    	catchBlockInfo.OperationFeatures.put("NumSpecificHandler", tryPossibleExceptionsCustomVisitor.getNumSpecificHandler());
    	catchBlockInfo.OperationFeatures.put("NumSubsumptionHandler", tryPossibleExceptionsCustomVisitor.getNumSubsumptionHandler());
    	catchBlockInfo.OperationFeatures.put("NumSupersumptionHandler", tryPossibleExceptionsCustomVisitor.getNumSupersumptionHandler());
    	catchBlockInfo.OperationFeatures.put("NumOtherHandler", tryPossibleExceptionsCustomVisitor.getNumOtherHandler());
    } 
    /*
    *Pending analysis still to be implemented - c# code:
    *Some will potentially need resolve binding = true
    
    var setLogicFlag = FindSetLogicFlagIn(updatedCatchBlock);
    if (setLogicFlag != null)
    {
        catchBlockInfo.MetaInfo["SetLogicFlag"] = setLogicFlag.ToString();
        catchBlockInfo.OperationFeatures["SetLogicFlag"] = 1;
    }

    //var otherOperation = HasOtherOperation(updatedCatchBlock, model);
    //if (otherOperation != null)
    //{
    //    catchBlockInfo.MetaInfo["OtherOperation"] = otherOperation.ToString();
    //    catchBlockInfo.OperationFeatures["OtherOperation"] = 1;
    //}
	
	*/
    
    catches.add(catchBlockInfo);
	return super.visit(node);
  }

   
  private Integer findKind(ITypeBinding exceptionType) {
	if(exceptionType.equals(tree.getAST().resolveWellKnownType("java.lang.RuntimeException")))
		return 0;
	else if (exceptionType.equals(tree.getAST().resolveWellKnownType("java.lang.Exception")))
		return 1;
	else if (exceptionType.equals(tree.getAST().resolveWellKnownType("java.lang.Error")))
		return 2;
	else if (exceptionType.equals(tree.getAST().resolveWellKnownType("java.lang.Throwable")))
		return 3;
	else if (exceptionType.equals(tree.getAST().resolveWellKnownType("java.lang.Object")))
		return -1;
	else
		return findKind(exceptionType.getSuperclass());
}

  private ASTNode findParent (ASTNode node){
	  
	  int parentNodeType = node.getParent().getNodeType();
	  
	  if(!(parentNodeType == ASTNode.BLOCK))
		  return node.getParent();
	  
	  return findParent(node.getParent());
  }
  
  private ASTNode findParentMethod (ASTNode node){
	  
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
  
  private String findParentType (ASTNode node){
	  
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
  
  private boolean IsInnerCatch (ASTNode node){
	  
	  int parentNodeType = node.getNodeType();
	  
	  //Stop condition that tells it's not an inner catch - finding a parent method before a inner catch
	  if(parentNodeType == ASTNode.METHOD_DECLARATION || parentNodeType == ASTNode.TYPE_DECLARATION)
		  return false;
	  
	  if((parentNodeType == ASTNode.CATCH_CLAUSE))
		  return true;
	  
	  return IsInnerCatch(node.getParent());
	  
  }
  
public void setTree (CompilationUnit cu){
	  tree = cu;
  }

public void setFilePath(String sourceFilePath) {
	filePath = sourceFilePath;
	
}

public List<CatchBlock> getCatchBlockList() {
	return catches;
}


public void setFileData(StringBuilder fileData) {
	// TODO Auto-generated method stub
	
}
  
} 