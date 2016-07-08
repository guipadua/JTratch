package ca.concordia.jtratch.visitors;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;

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
//    node.getAST() getException().resolveBinding()
//	
//    node.getAST().hasBindingsRecovery()
//
//    node.getAST().hasResolvedBindings()
//    node.getException().getType().
   
	if ( node.getException().getType().resolveBinding() != null || node.getException().resolveBinding() != null)
	{
		logger.trace("exception succesfully binded");
	}
	
    SingleVariableDeclaration exceptionType = node.getException();
	
    if(exceptionType.getType().resolveBinding() != null)
    	catchBlockInfo.ExceptionType = exceptionType.getType().resolveBinding().getQualifiedName();
    else
    	catchBlockInfo.ExceptionType = exceptionType.getType().toString();
	
    catchBlockInfo.OperationFeatures.put("Checked", IsChecked(exceptionType));
	
    TryStatement tryStatement = (TryStatement) node.getParent();

	Integer startLine = tree.getLineNumber(tryStatement.getStartPosition() + 1);
	Integer endLine = tree.getLineNumber(tryStatement.getStartPosition() + tryStatement.getLength() + 1);
	
	catchBlockInfo.OperationFeatures.put("Line", startLine);
    catchBlockInfo.OperationFeatures.put("LOC", endLine - startLine + 1);
	
    catchBlockInfo.OperationFeatures.put("Start", node.getStartPosition());
    catchBlockInfo.OperationFeatures.put("Length", node.getLength());
	
    catchBlockInfo.FilePath = filePath;
    catchBlockInfo.MetaInfo.put("Line", startLine.toString());
    catchBlockInfo.MetaInfo.put("FilePath", filePath);
    
    
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
    CatchClause updatedCatchBlock = (CatchClause) node.copySubtree(updatedAST, node);
    
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
    if (node.getParent().getParent().getParent().getNodeType() == 12)
    {
    	catchBlockInfo.OperationFeatures.put("InnerCatch", 1);
    	catchBlockInfo.OperationFeatures.put("ParentStartLine", tree.getLineNumber(node.getParent().getParent().getParent().getParent().getStartPosition() + 1));
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
    	catchBlockInfo.MetaInfo.put("Default", catchMethodInvocationVisitor.getAbortStatements().toString());
        catchBlockInfo.OperationFeatures.put("Default", 1);
    }
    
    //Treatment for ThrowStatement
    //Collection of data for statements of: throw 
    ThrowStatementVisitor throwStatementVisitor = new ThrowStatementVisitor();
    throwStatementVisitor.setTree(tree);
    updatedCatchBlock.accept(throwStatementVisitor);
    
    //Thrown
    if (!throwStatementVisitor.getThrowStatements().isEmpty())
    {
    	catchBlockInfo.MetaInfo.put("Thrown", throwStatementVisitor.getThrowStatements().toString());
        catchBlockInfo.OperationFeatures.put("Thrown", 1);
    }
    
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
    
    catchBlockInfo.MetaInfo.put("TryBlock", tryStatement.getBody().toString());
    catchBlockInfo.MetaInfo.put("ParentNodeType", tryStatement.getParent().getParent().getClass().getName());
    catchBlockInfo.OperationFeatures.put("ParentNodeType", tryStatement.getParent().getParent().getNodeType());
    
    //ASTNode.nodeClassForType(nodeType)
    
    //FinallyThrowing
    if (tryStatement.getFinally() != null)
    {
    	catchBlockInfo.MetaInfo.put("FinallyBlock", tryStatement.getFinally().toString());
    	ThrowStatementVisitor throwStatementVisitorFinally = new ThrowStatementVisitor();
    	throwStatementVisitorFinally.setTree(tree);
    	updatedCatchBlock.accept(throwStatementVisitorFinally);
    	tryStatement.getFinally().accept(throwStatementVisitorFinally);
    	
    	//FinallyThrowing
    	if (! throwStatementVisitorFinally.getThrowStatements().isEmpty())
    		catchBlockInfo.OperationFeatures.put("FinallyThrowing", 1);
    }
    	
    //if(updatedCatchBlock.getAST().hasResolvedBindings())
	{
	    MethodInvocationVisitor tryMethodInvocationVisitor = new MethodInvocationVisitor("try");
	    tryMethodInvocationVisitor.setTree(tree);
	    tryStatement.getBody().accept(tryMethodInvocationVisitor);
	    
	    //SpecificHandler
	    if (tryMethodInvocationVisitor.getExceptionTypes().contains(updatedCatchBlock.getException().getType().toString()))
	    	catchBlockInfo.OperationFeatures.put("SpecificHandler", 1);
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

    //if (IsLogOnly(updatedCatchBlock, model))
    //{
    //    catchBlockInfo.OperationFeatures["LogOnly"] = 1;
    //}

    var variableAndComments = GetVariablesAndComments(tryBlock.Block);
    //var containingMethod = GetContainingMethodName(tryBlock, model);
    //var methodNameList = GetAllInvokedMethodNamesByBFS(tryBlock.Block, treeAndModelDic, compilation);

    //var methodAndExceptionList = GetAllInvokedMethodNamesAndExceptionsByBFS(tryBlock.Block, treeAndModelDic, compilation);

    //foreach (var methodname in methodNameList)
    //{
    //    //var method = new MethodDeclarationSyntax();
    //    //method.
    //}

    //catchBlockInfo.OperationFeatures["NumMethod"] = methodAndExceptionList[0].Count;
    //catchBlockInfo.OperationFeatures["NumExceptions"] = methodAndExceptionList[1].Count;
    //catchBlockInfo.TextFeatures = methodAndExceptionList[0];
    //if (containingMethod != null)
    //{
    //    MergeDic<String>(ref catchBlockInfo.TextFeatures,
    //        new Dictionary<String, int>() { { containingMethod, 1 } });
    //}
    MergeDic<String>(ref catchBlockInfo.TextFeatures,
            new Dictionary<String, int>() { { "##spliter##", 0 } }); // to seperate methods and variables
    MergeDic<String>(ref catchBlockInfo.TextFeatures, variableAndComments);
    
    return catchBlockInfo;
	
	*/
    
    catches.add(catchBlockInfo);
	return super.visit(node);
  }

   
  private Integer IsChecked(SingleVariableDeclaration exceptionType) {
	  if (exceptionType.resolveBinding() != null)
	  {
		  return findExceptionSuperType(exceptionType.resolveBinding().getType().getSuperclass());
	  } else if (exceptionType.getType().resolveBinding() != null)
	  {
		  return findExceptionSuperType(exceptionType.getType().resolveBinding().getSuperclass());
	  }	  
	  
	return null;
}

  private Integer findExceptionSuperType(ITypeBinding typeBinding) {
		
	  if (typeBinding == null) { return null; }
	  
	  if (typeBinding.getQualifiedName().equals("java.lang.RuntimeException"))
	  {
		  return 0;
	  } else if (typeBinding.getQualifiedName().equals("java.lang.Exception"))
	  {
		  return 1;
	  }
	  else
	  {
		  return findExceptionSuperType(typeBinding.getSuperclass());
	  }
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