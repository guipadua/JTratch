package ca.concordia.jtratch.visitors;

import java.util.ArrayList;
import java.util.HashSet;
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

import ca.concordia.jtratch.ClosedExceptionFlow;
import ca.concordia.jtratch.pattern.CatchBlock;
import ca.concordia.jtratch.pattern.PossibleExceptionsBlock;
import ca.concordia.jtratch.utility.ASTUtilities;

public class CatchVisitor extends ASTVisitor {
  List<CatchBlock> catches = new ArrayList<CatchBlock>();
  List<PossibleExceptionsBlock> possibleExceptionsList = new ArrayList<PossibleExceptionsBlock>();
  private static final Logger logger = LogManager.getLogger(CatchVisitor.class.getName());
  private static CompilationUnit tree;
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
    	int kind = ASTUtilities.findKind(exceptionTypeBinding, tree);
    	catchBlockInfo.OperationFeatures.put("Kind", kind);
    	catchBlockInfo.OperationFeatures.put("Checked", (kind == 1 || kind == 3) ? 1 : 0);
    } else 
    {	
    	catchBlockInfo.ExceptionType = exceptionType.getType().toString();
    	catchBlockInfo.OperationFeatures.put("Binded", 0);
    }
    
    //Basic info:
    //catchBlockInfo.MetaInfo.put("ExceptionType", catchBlockInfo.ExceptionType);    
	
    //Try info:
    TryStatement tryStatement = (TryStatement) node.getParent();
    catchBlockInfo.MetaInfo.put("TryBlock", tryStatement.getBody().toString());
    catchBlockInfo.OperationFeatures.put("ParentNodeType", ASTUtilities.findParent(tryStatement).getNodeType());
    catchBlockInfo.MetaInfo.put("ParentNodeType", ASTUtilities.findParent(tryStatement).getClass().getName());
    
    //Common Features - try/catch block
	Integer tryStartLine = tree.getLineNumber(tryStatement.getBody().getStartPosition() + 1);
	Integer tryEndLine = tree.getLineNumber(tryStatement.getBody().getStartPosition() + tryStatement.getBody().getLength() + 1);
	Integer tryCount = ASTUtilities.countLines(tryStatement.getBody());
	
	catchBlockInfo.OperationFeatures.put("TryStartLine", tryStartLine);
	catchBlockInfo.OperationFeatures.put("TryEndLine", tryEndLine);
	catchBlockInfo.OperationFeatures.put("TryLOC", tryCount);
	
	catchBlockInfo.MetaInfo.put("TryBlock", tryStartLine.toString());
    
    Integer catchStartLine = tree.getLineNumber(node.getBody().getStartPosition() + 1);
	Integer catchEndLine = tree.getLineNumber(node.getBody().getStartPosition() + node.getBody().getLength() + 1);
	Integer catchCount = ASTUtilities.countLines(node.getBody());
	
	catchBlockInfo.OperationFeatures.put("CatchStartLine", catchStartLine);
	catchBlockInfo.OperationFeatures.put("CatchEndLine", catchEndLine);
	catchBlockInfo.OperationFeatures.put("CatchLOC", catchCount);
    
    catchBlockInfo.OperationFeatures.put("CatchStart", node.getStartPosition());
    catchBlockInfo.OperationFeatures.put("CatchLength", node.getBody().getLength());
	
    catchBlockInfo.FilePath = filePath;
    catchBlockInfo.StartLine = catchStartLine;
    //catchBlockInfo.MetaInfo.put("FilePath", filePath);
    //catchBlockInfo.MetaInfo.put("StartLine", catchStartLine.toString());
    
    //Common Features - parent type
    catchBlockInfo.ParentType = ASTUtilities.findParentType(tryStatement);
    //catchBlockInfo.MetaInfo.put("ParentType", catchBlockInfo.ParentType);
    
    //Common Features - parent method
    ASTNode parentNode = ASTUtilities.findParentMethod(tryStatement);
    int parentStartPosition = parentNode.getStartPosition();
    int parentLength = parentNode.getLength();
    
    String parentMethodName = new String();
    if(parentNode.getNodeType() == ASTNode.METHOD_DECLARATION)
    {
    	MethodDeclaration parentMethod = (MethodDeclaration) parentNode;
    	parentStartPosition = parentMethod.getBody().getStartPosition();
    	parentLength = parentMethod.getBody().getLength();
    	parentNode = parentMethod.getBody();
    	parentMethodName = ASTUtilities.getMethodNameWithoutBinding(parentMethod, true);
    	//TODO: review this to use a different method to get the name from ast utilities
    	
    } else if (parentNode.getNodeType() == ASTNode.INITIALIZER) {
    	parentMethodName = "!NAME_NA!"; //name not applicable
    } else
    	parentMethodName = "!UNEXPECTED_KIND!";
    
    catchBlockInfo.ParentMethod = parentMethodName;
    //catchBlockInfo.MetaInfo.put("ParentMethod", parentMethodName);
    

    Integer parentMethodStartLine = tree.getLineNumber(parentStartPosition + 1);
	Integer parentMethodEndLine = tree.getLineNumber(parentStartPosition + parentLength + 1);
	Integer parentMethodLOC = ASTUtilities.countLines(parentNode);
	
	//Common Features
	catchBlockInfo.OperationFeatures.put("MethodStartLine", parentMethodStartLine);
	catchBlockInfo.OperationFeatures.put("MethodEndLine", parentMethodEndLine);	
    catchBlockInfo.OperationFeatures.put("MethodLOC", parentMethodLOC);
    
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
    
    if(catchBlockInfo.OperationFeatures.get("Binded") == 1) 
    {
    	PossibleExceptionsCustomVisitor tryPossibleExceptionsCustomVisitor = new PossibleExceptionsCustomVisitor(tree, (byte) 0, true,
    																											filePath, catchStartLine, exceptionTypeBinding);
    	tryStatement.getBody().accept(tryPossibleExceptionsCustomVisitor);
    	
    	/*
		 * Process for possible exceptions
		 */		
		getExceptionFlows(this.possibleExceptionsList, tryPossibleExceptionsCustomVisitor.getClosedExceptionFlows());		
        
        catchBlockInfo.MetaInfo.put("TryMethodsAndExceptions", tryPossibleExceptionsCustomVisitor.getInvokedMethodsPossibleExceptions().toString());
        
        catchBlockInfo.OperationFeatures.put("NumDistinctMethods", tryPossibleExceptionsCustomVisitor.getInvokedMethodsPossibleExceptions().size());
        catchBlockInfo.MetaInfo.put("TryMethodsBinded",tryPossibleExceptionsCustomVisitor.getInvokedMethodsBinded().toString());
        catchBlockInfo.OperationFeatures.put("NumMethodsNotBinded",tryPossibleExceptionsCustomVisitor.getNumMethodsNotBinded()); 
        
        catchBlockInfo.MetaInfo.put("DistinctExceptions", tryPossibleExceptionsCustomVisitor.getDistinctPossibleExceptions().toString());
        catchBlockInfo.OperationFeatures.put("NumDistinctExceptions", tryPossibleExceptionsCustomVisitor.getDistinctPossibleExceptions().size());
        
    	catchBlockInfo.OperationFeatures.put("NumSpecificHandler", tryPossibleExceptionsCustomVisitor.getNumSpecificHandler());
    	catchBlockInfo.OperationFeatures.put("NumSubsumptionHandler", tryPossibleExceptionsCustomVisitor.getNumSubsumptionHandler());
    	catchBlockInfo.OperationFeatures.put("NumSupersumptionHandler", tryPossibleExceptionsCustomVisitor.getNumSupersumptionHandler());
    	catchBlockInfo.OperationFeatures.put("NumOtherHandler", tryPossibleExceptionsCustomVisitor.getNumOtherHandler());
    	
    	catchBlockInfo.OperationFeatures.put("MaxLevel", tryPossibleExceptionsCustomVisitor.getChildrenMaxLevel());
    	catchBlockInfo.OperationFeatures.put("NumIsBindingInfo", tryPossibleExceptionsCustomVisitor.getNumIsBindingInfo());
    	catchBlockInfo.OperationFeatures.put("NumIsDocSemantic", tryPossibleExceptionsCustomVisitor.getNumIsJavadocSemantic());
    	catchBlockInfo.OperationFeatures.put("NumIsDocSyntax", tryPossibleExceptionsCustomVisitor.getNumIsJavadocSyntax());
    	catchBlockInfo.OperationFeatures.put("NumIsThrow", tryPossibleExceptionsCustomVisitor.getNumIsThrow());
    	
    }
    
    //FinallyThrowing
    Block finallyBlock = tryStatement.getFinally();
    if (finallyBlock != null)
    {
    	catchBlockInfo.MetaInfo.put("FinallyBlock", finallyBlock.toString());
    	
    	ThrowStatementVisitor throwStatementVisitorFinally = new ThrowStatementVisitor(exceptionType.getName());
    	throwStatementVisitorFinally.setTree(tree);
    	updatedCatchBlock.accept(throwStatementVisitorFinally);
    	finallyBlock.accept(throwStatementVisitorFinally);
    	
    	PossibleExceptionsCustomVisitor finallyPossibleExceptionsCustomVisitor = new PossibleExceptionsCustomVisitor(tree, (byte) 0, true,
																														filePath, catchStartLine, exceptionTypeBinding);
    	finallyBlock.accept(finallyPossibleExceptionsCustomVisitor);
        
    	//FinallyThrowing
    	if (! throwStatementVisitorFinally.getThrowStatements().isEmpty() 
    			|| finallyPossibleExceptionsCustomVisitor.getDistinctPossibleExceptions().size() > 0
    		)
    		catchBlockInfo.OperationFeatures.put("FinallyThrowing", 1);
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


public List<PossibleExceptionsBlock> getPossibleExceptionsList() {
	return possibleExceptionsList;
}

private static void getExceptionFlows(List<PossibleExceptionsBlock> possibleExceptionsList, HashSet<ClosedExceptionFlow> closedExceptionFlows) {
	
	closedExceptionFlows.forEach(flow -> 
		{
			PossibleExceptionsBlock possibleExceptionsBlockInfo = new PossibleExceptionsBlock();

			possibleExceptionsBlockInfo.ExceptionType = flow.getThrownTypeName();
			possibleExceptionsBlockInfo.CaughtType = flow.getCaughtTypeName();			
	    	possibleExceptionsBlockInfo.DeclaringMethod = flow.getOriginalMethodBindingKey();
	    	possibleExceptionsBlockInfo.InvokedMethod = flow.getInvokedMethodKey();
	    	possibleExceptionsBlockInfo.InvokedMethodLine = flow.getInvokedMethodLine();
			possibleExceptionsBlockInfo.FilePath = flow.getCatchFilePath();
			possibleExceptionsBlockInfo.StartLine = flow.getCatchStartLine();
			
			//possibleExceptionsBlockInfo.MetaInfo.put("FilePath", flow.getCatchFilePath());
			//possibleExceptionsBlockInfo.MetaInfo.put("StartLine", flow.getCatchStartLine().toString());
	        
			int kind = ASTUtilities.findKind(flow.getThrownType(), tree);
			possibleExceptionsBlockInfo.OperationFeatures.put("Kind", kind);
			
			possibleExceptionsBlockInfo.OperationFeatures.put("IsBindingInfo", flow.getIsBindingInfo() ? 1 : 0);
			possibleExceptionsBlockInfo.OperationFeatures.put("IsDocSemantic", flow.getIsJavadocSemantic() ? 1 : 0);
			possibleExceptionsBlockInfo.OperationFeatures.put("IsDocSyntax", flow.getIsJavadocSyntax() ? 1 : 0);
			possibleExceptionsBlockInfo.OperationFeatures.put("IsThrow", flow.getIsThrow() ? 1 : 0);
			possibleExceptionsBlockInfo.OperationFeatures.put("LevelFound", (int) flow.getLevelFound());
	        
			possibleExceptionsBlockInfo.OperationFeatures.put("HandlerTypeCode", (int) flow.getHandlerTypeCode());			
			
	        //possibleExceptionsBlockInfo.MetaInfo.put("PossibleExceptionsBlock", node.toString());
	    					        
			possibleExceptionsList.add(possibleExceptionsBlockInfo);
	        
	        logger.trace("throws block info registered.");
		});		
}
  
} 