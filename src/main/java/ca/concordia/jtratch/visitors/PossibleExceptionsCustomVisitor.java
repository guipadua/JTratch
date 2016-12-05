package ca.concordia.jtratch.visitors;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import ca.concordia.jtratch.utility.Dic;

public class PossibleExceptionsCustomVisitor extends ASTVisitor{
	private static final Logger logger = LogManager.getLogger(PossibleExceptionsCustomVisitor.class.getName());
	private CompilationUnit tree;
	private ITypeBinding exceptionType;
	
	private Map<String,Integer> m_invokedMethodsBinded = new HashMap<String,Integer>();
	private HashMap<String,HashMap<String,Integer>> m_invokedMethodsPossibleExceptions = new HashMap<String,HashMap<String,Integer>>();
	private HashMap<String,Integer> m_possibleExceptions = new HashMap<String,Integer>();
		
	public PossibleExceptionsCustomVisitor (ITypeBinding exceptionType) {
		this.exceptionType = exceptionType;
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		logger.trace("Visiting a AST node of type "+ node.getNodeType() + " at line " + tree.getLineNumber(node.getStartPosition()));
		processNode(node.toString(), node.resolveMethodBinding());
		return super.visit(node);
	}
	@Override
	public boolean visit(ClassInstanceCreation node) {
		logger.trace("Visiting a AST node of type "+ node.getNodeType() + " at line " + tree.getLineNumber(node.getStartPosition()));
		processNode(node.toString(), node.resolveConstructorBinding());
		return super.visit(node);
	}
	
	private void processNode(String nodeString, IMethodBinding resolveMethodBinding) {
		HashMap<String,Integer> exceptionTypeNames = new HashMap<String,Integer>();
		
		if(resolveMethodBinding != null)
		{
			for( ITypeBinding type : resolveMethodBinding.getExceptionTypes())
			{
				//In case is the same type, it's specific handler type - code: 0
				//In case the catched type is equal a super class of the possible thrown type, it's a subsumption - code: 1
				//In case the possible thrown type is equal a super class of the catched type, it's a supersumption - code: 2
				//In case it's none of the above - most likely tree of unrelated exceptions: code: 3
				if (this.exceptionType.equals(type))
				{
					exceptionTypeNames.put(type.getName(),0);
				}
				else if (IsSuperType(this.exceptionType,type))
				{
					exceptionTypeNames.put(type.getName(),1);
				}	
				else if (IsSuperType(type,this.exceptionType))
				{
					exceptionTypeNames.put(type.getName(),2);
				}	
				else 
				{
					//it can happen when exceptions are not related on the type tree
					exceptionTypeNames.put(type.getName(),3);
				}				
			}
			m_invokedMethodsBinded.put(nodeString, 1);
		} else
		{
			m_invokedMethodsBinded.put(nodeString, 0);			
		}
		
		Dic.MergeDic2(m_possibleExceptions, exceptionTypeNames);
		m_invokedMethodsPossibleExceptions.put(nodeString,exceptionTypeNames);
		
	}

	public void setTree(CompilationUnit cu) {
		tree = cu;
		
	}
		
	public Map<String,Integer> getInvokedMethodsBinded() {
		return m_invokedMethodsBinded;
	}
	
	public Map<String,Integer> getDistinctPossibleExceptions() {
		return m_possibleExceptions;
	}
	
	public HashMap<String, HashMap<String, Integer>> getInvokedMethodsHandlerType() {
		return m_invokedMethodsPossibleExceptions;
	}
	
	/// <summary>
    /// To check whether an invocation is a logging statement
    /// </summary>
    	
	/**
	 * Recursively find if the given subtype is a supertype of the reference type.
	 *  
	 * @param subtype type to evaluate
	 * @param referenceType initial tracing reference to detect the super type
	 */
    private Boolean IsSuperType(ITypeBinding subType, ITypeBinding referenceType) {
		  
			if (subType == null || referenceType == null || referenceType.getQualifiedName().equals("java.lang.Object")) 
				return false;
			
			if (subType.equals(referenceType.getSuperclass())) 
				return true;
			
			return IsSuperType(subType, referenceType.getSuperclass());
	  			
	  }
    
    public int countMetricsForExceptions(int intCode){
    	return (int) m_possibleExceptions.values().stream().filter(exception -> exception.intValue() == intCode).count();
    }
    
    public int getNumSpecificHandler() {
		return countMetricsForExceptions(0);
	}
    public int getNumSubsumptionHandler() {
		return countMetricsForExceptions(1);
	}
    public int getNumSupersumptionHandler() {
		return countMetricsForExceptions(2);
	}
    public int getNumOtherHandler() {
		return countMetricsForExceptions(3);
	}
    public int getNumMethodsNotBinded() {
		
    	return (int) m_invokedMethodsBinded.values().stream()
    				.filter(value -> value.intValue() == 0)
    				.count();
		
	}
    
}
