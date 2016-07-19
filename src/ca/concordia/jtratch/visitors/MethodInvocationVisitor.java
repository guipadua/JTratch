package ca.concordia.jtratch.visitors;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;

import ca.concordia.jtratch.utility.Config;

public class MethodInvocationVisitor extends ASTVisitor{
	private static final Logger logger = LogManager.getLogger(MethodInvocationVisitor.class.getName());
	private CompilationUnit tree;
	private String originator;
	
	private List<String> loggingStatements = new ArrayList<String>();
	private List<String> abortStatements = new ArrayList<String>();
	private List<String> defaultStatements = new ArrayList<String>();
	private List<String> otherStatements = new ArrayList<String>();
	private List<String> getCauseStatements = new ArrayList<String>();;
	
	public MethodInvocationVisitor (String originator) {
		this.originator = originator;
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		logger.trace("Visiting a AST node of type "+ node.getNodeType() + " at line " + tree.getLineNumber(node.getStartPosition()));
		
		String nodeName = node.getName().toString();
		
		if(this.originator=="catch"){
			
			if (IsLoggingStatement(nodeName))
				loggingStatements.add(node.toString());

			if (IsAbortStatement(nodeName))
				abortStatements.add(node.toString());
			
			if (IsDefaultStatement(nodeName))
				defaultStatements.add(node.toString());
			
			if (IsGetCause(nodeName))
				getCauseStatements.add(node.toString());
			
			if(!IsLoggingStatement(nodeName) && !IsAbortStatement(nodeName) && !IsDefaultStatement(nodeName) && !IsGetCause(nodeName)){
				otherStatements.add(node.toString());
			}
		
		} 
		
		return super.visit(node);
		
	}
	
	public void setTree(CompilationUnit cu) {
		tree = cu;
		
	}
	public List<String> getLoggingStatements() {
		return loggingStatements;
	}
	
	public List<String> getAbortStatements() {
		return abortStatements;
	}
	
	public List<String> getDefaultStatements() {
		return defaultStatements;
	}
	
	public List<String> getOtherStatements() {
		return otherStatements;
	}
	
	public List<String> getGetCauseStatements() {
		return getCauseStatements;
	}
	
	/// <summary>
    /// To check whether an invocation is a logging statement
    /// </summary>
    
	static public boolean IsLoggingStatement(String statement)
    {
        if (statement == null) return false;

        for (String notlogmethod : Config.NotLogMethods)
        {
            if (notlogmethod == "") break;
            if (statement.indexOf(notlogmethod) > -1)
            {
                return false;
            }
        }
        for (String logmethod : Config.LogMethods)
        {
            if (statement.indexOf(logmethod) > -1)
            {
                return true;
            }
        }
        return false;
    }
	
	static public boolean IsAbortStatement(String statement)
    {
        if (statement == null) return false;

//        for (String notlogmethod : Config.NotLogMethods)
//        {
//            if (notlogmethod == "") break;
//            if (statement.indexOf(notlogmethod) > -1)
//            {
//                return false;
//            }
//        }
        for (String abortmethod : Config.AbortMethods)
        {
            if (statement.indexOf(abortmethod) > -1)
            {
                return true;
            }
        }
        return false;
    }
	
	static public boolean IsDefaultStatement(String statement)
	{
        if (statement == null) return false;

//        for (String notlogmethod : Config.NotLogMethods)
//        {
//            if (notlogmethod == "") break;
//            if (statement.indexOf(notlogmethod) > -1)
//            {
//                return false;
//            }
//        }
        for (String defaultmethod : Config.DefaultMethods)
        {
            if (statement.indexOf(defaultmethod) > -1)
            {
                return true;
            }
        }
        return false;
    }
	
	static public boolean IsGetCause(String statement)
    {
        if (statement == null) return false;

        if (statement.indexOf("getCause") > -1)
        {
            return true;
        }
    
        return false;
    }
}
