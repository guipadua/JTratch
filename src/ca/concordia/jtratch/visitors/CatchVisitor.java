package ca.concordia.jtratch.visitors;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TryStatement;

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
    
	catchBlockInfo.ExceptionType = node.getException().getType().toString();
	
    TryStatement tryBlock = (TryStatement) node.getParent();

	Integer startLine = tree.getLineNumber(tryBlock.getStartPosition() + 1);
	Integer endLine = tree.getLineNumber(tryBlock.getStartPosition() + tryBlock.getLength() + 1);
	
	catchBlockInfo.OperationFeatures.put("Line", startLine);
    catchBlockInfo.OperationFeatures.put("LOC", endLine - startLine + 1);
	
    catchBlockInfo.OperationFeatures.put("CatchStart", node.getStartPosition());
    catchBlockInfo.OperationFeatures.put("CatchLength", node.getLength());
	
    catchBlockInfo.FilePath = filePath;
    catchBlockInfo.MetaInfo.put("Line", startLine.toString());
    catchBlockInfo.MetaInfo.put("FilePath", filePath);
    
    
    /*------------------------BEGIN Inner Visitors----------------------*/
    
    //Treatment for TryStatement
    //Collection of data for statements of: Recover
    //Other possible items to be collected: (LogAdvisor do it a bit different)
    //***Remove try-catch-finally block inside for the other analysis!
    TryVisitor tryVisitor = new TryVisitor();
    tryVisitor.setTree(tree);
    node.accept(tryVisitor);
    
    //RecoverFlag - (based on inner try blocks)
    if (!tryVisitor.getTryStatements().isEmpty())
    {
    	catchBlockInfo.MetaInfo.put("RecoverFlag", tryVisitor.getTryStatements().toString());
        catchBlockInfo.OperationFeatures.put("RecoverFlag", 1);
    }
    
    catchBlockInfo.MetaInfo.put("CatchBlock", node.toString());
    
    //Treatment for MethodInvocation
    //Collection of data for statements of: logging, abort, 
    //Other possible items to be collected: throw (check if it can happen and not fall under ThrowStatement type)
    MethodInvocationVisitor methodInvocationVisitor = new MethodInvocationVisitor();
    methodInvocationVisitor.setTree(tree);
    node.accept(methodInvocationVisitor);
    
    //Logging
    if (!methodInvocationVisitor.getLoggingStatements().isEmpty())
    {
    	catchBlockInfo.MetaInfo.put("Logged", methodInvocationVisitor.getLoggingStatements().toString());
        catchBlockInfo.OperationFeatures.put("Logged", 1);
    }
    
    //Abort
    if (!methodInvocationVisitor.getAbortStatements().isEmpty())
    {
    	catchBlockInfo.MetaInfo.put("Abort", methodInvocationVisitor.getAbortStatements().toString());
        catchBlockInfo.OperationFeatures.put("Abort", 1);
    }
    
    //Treatment for ThrowStatement
    //Collection of data for statements of: throw 
    ThrowStatementVisitor throwStatementVisitor = new ThrowStatementVisitor();
    throwStatementVisitor.setTree(tree);
    node.accept(throwStatementVisitor);
    
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
    node.accept(returnStatementVisitor);
    
    //Return
    if (!returnStatementVisitor.getReturnStatements().isEmpty())
    {
    	catchBlockInfo.MetaInfo.put("Return", returnStatementVisitor.getReturnStatements().toString());
        catchBlockInfo.OperationFeatures.put("Return", 1);
    }
        
    /*------------------------END Inner Visitors----------------------*/
  
    //EmptyBlock
    //It counts if only comments on it - use with comment related metrics
    if (node.getBody().statements().isEmpty())
    	catchBlockInfo.OperationFeatures.put("EmptyBlock", 1);
    
    //CatchException
    if (node.getException().getType().toString().equalsIgnoreCase("exception"))
    	catchBlockInfo.OperationFeatures.put("CatchException", 1);
    
  //CatchException
    if (node.getException().getType().toString().equalsIgnoreCase("exception"))
    	catchBlockInfo.OperationFeatures.put("CatchException", 1);
    
    
    
    
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

    if (IsToDo(updatedCatchBlock))
    {
        catchBlockInfo.OperationFeatures["ToDo"] = 1;       
    }

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