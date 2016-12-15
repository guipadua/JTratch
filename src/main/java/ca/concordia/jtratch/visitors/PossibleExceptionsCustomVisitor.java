package ca.concordia.jtratch.visitors;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import ca.concordia.jtratch.ExceptionFlow;
import ca.concordia.jtratch.InvokedMethod;
import ca.concordia.jtratch.CodeAnalyzer;
import ca.concordia.jtratch.utility.ASTUtilities;
import ca.concordia.jtratch.utility.Dic;

public class PossibleExceptionsCustomVisitor extends ASTVisitor{
	private static final Logger logger = LogManager.getLogger(PossibleExceptionsCustomVisitor.class.getName());
	
	private ITypeBinding m_exceptionType;
	
	private CompilationUnit m_compilation;
	
	//store just the method identifier and if binded or not.
    private HashMap<String,Byte> m_invokedMethodsBinded = new HashMap<String,Byte>();
    
    //store the method identifiers of the existing method in the visited declaration with their possible exceptions
    private HashMap<String, HashSet<ExceptionFlow>> m_invokedMethodsPossibleExceptions = new HashMap<String,HashSet<ExceptionFlow>>();
    
    //store just the distinct exceptions that comes from this declaration, without method information
    //TODO: if memory issues, this could be removed and calculated on the fly, no need to store it.
    private HashSet<ExceptionFlow> m_possibleExceptions = new HashSet<ExceptionFlow>();
	
	private int m_nodeMaxLevel = 0;

    private int m_myLevel = 0;
    private HashMap<String, Integer> m_ChildrenNodesLevel = new HashMap<String, Integer>();

    public boolean m_isForAnalysis;
	
	public boolean isM_isForAnalysis() {
		return m_isForAnalysis;
	}

	private void setM_isForAnalysis(boolean m_isForAnalysis) {
		this.m_isForAnalysis = m_isForAnalysis;
	}
	
	/// <summary>
    /// Constructor
    /// </summary>
    /// <param name="p_isForAnalysis">This is know if the found exceptions should be evaluated against the parent try-catch block, if any.</param>
    public PossibleExceptionsCustomVisitor(ITypeBinding p_exceptionType, CompilationUnit p_compilation, boolean p_isForAnalysis, int p_level)
    {
        m_exceptionType = p_exceptionType;
        m_compilation = p_compilation;
        m_isForAnalysis = p_isForAnalysis;
        m_myLevel = p_level;            
    }	
	
	@Override
	public boolean visit(MethodInvocation node) {
		logger.trace("Visiting a AST node of type "+ node.getNodeType() + " at line " + m_compilation.getLineNumber(node.getStartPosition()));
		processNodeAndVisit(node.toString(), node.resolveMethodBinding());
		return super.visit(node);
	}
	@Override
	public boolean visit(ClassInstanceCreation node) {
		logger.trace("Visiting a AST node of type "+ node.getNodeType() + " at line " + m_compilation.getLineNumber(node.getStartPosition()));
		processNodeAndVisit(node.toString(), node.resolveConstructorBinding());
		return super.visit(node);
	}
	
	private void processNodeAndVisit(String p_nodeString, IMethodBinding nodeBindingInfo) 
	{
		//store all possible exceptions from the node being visited
		//these will be validated before being added to the invoked methods list
		HashSet<ExceptionFlow> nodePossibleExceptions = new HashSet<ExceptionFlow>();
        
		// STEP 1: check if such method was already identified before and added to the invoked methods collection
		String nodeString;
		boolean binded;
				
		//nodeBindingInfo is not empty it means it's binded and we have semantic info
		binded = (nodeBindingInfo != null) ? true : false;
		nodeString = binded ? nodeBindingInfo.getKey() : p_nodeString;
		
		//update this declaration (scope being visited) metrics
		m_invokedMethodsBinded.put(nodeString, (byte) (binded ? 1 : 0) );
		if (!m_ChildrenNodesLevel.containsKey(nodeString))
			m_ChildrenNodesLevel.put(nodeString, 1);          
        
        //Check if invoked method is already known - grab data if not known
        if (CodeAnalyzer.InvokedMethods.containsKey(nodeString)){
        	//TODO: if already known, what to do?
        } else
        {
        	if(binded) {
        		CodeAnalyzer.InvokedMethods.put(nodeString, new InvokedMethod(nodeString, true));        		
        		
        		nodePossibleExceptions.addAll(processNodeForCheckedExceptions(nodeBindingInfo));
        		//nodePossibleExceptions.addAll(processNodeForJavaDocSemantic(nodeBindingInfo));
        		
        		collectBindedInvokedMethodDataFromDeclaration(CodeAnalyzer.InvokedMethods.get(nodeString), nodeString, nodeBindingInfo);
        		
        	} else
        	{
        		CodeAnalyzer.InvokedMethods.put(nodeString, new InvokedMethod(nodeString, false));
        	}
        }
        
        //add the invoked method exception to the list of possible exceptions
        nodePossibleExceptions.addAll(CodeAnalyzer.InvokedMethods.get(nodeString).getExceptionFlowSet());
        m_ChildrenNodesLevel.put(nodeString, m_ChildrenNodesLevel.get(nodeString) + CodeAnalyzer.InvokedMethods.get(nodeString).getChildrenMaxLevel());
        
        HashMap<String, HashSet<ExceptionFlow>> nodeAndNodePossibleExceptions = new HashMap<String, HashSet<ExceptionFlow>>();
        HashSet<ExceptionFlow> validNodePossibleExceptions = new HashSet<ExceptionFlow>();

        if (!m_isForAnalysis)
        	validNodePossibleExceptions = nodePossibleExceptions; 
        	//TODO evaluate stuff for when analysis when not - close flows!!! getValidPossibleExceptions(node, nodePossibleExceptions);
        else
        	validNodePossibleExceptions = nodePossibleExceptions;

        m_possibleExceptions.addAll(validNodePossibleExceptions);
        nodeAndNodePossibleExceptions.put(nodeString, validNodePossibleExceptions);
        Dic.MergeDic2(m_invokedMethodsPossibleExceptions, nodeAndNodePossibleExceptions);  
        
        
	}
	private void collectBindedInvokedMethodDataFromDeclaration(InvokedMethod invokedMethod, String nodeString, IMethodBinding nodeBindingInfo) 
	{
		MethodDeclaration nodemDeclar;
		nodemDeclar = (MethodDeclaration) m_compilation.findDeclaringNode(nodeString);
				
		if(nodemDeclar != null){
			invokedMethod.setVisited(true);
			PossibleExceptionsCustomVisitor possibleExceptionsCustomVisitor = new PossibleExceptionsCustomVisitor(m_exceptionType, m_compilation, false, m_myLevel + 1);
            possibleExceptionsCustomVisitor.visit(nodemDeclar);
            
            Dic.MergeDic2(m_invokedMethodsBinded, possibleExceptionsCustomVisitor.m_invokedMethodsBinded);
            
            invokedMethod.getExceptionFlowSet().addAll(GetExceptionsFromXMLSyntax(nodemDeclar));
            invokedMethod.getExceptionFlowSet().addAll(possibleExceptionsCustomVisitor.m_possibleExceptions);
            invokedMethod.setChildrenMaxLevel(possibleExceptionsCustomVisitor.getChildrenMaxLevel());    		
		}		
	}

	
			
		//Dic.MergeDic2(m_possibleExceptions, exceptionTypeNames);
        //m_possibleExceptions.addAll(nodePossibleExceptions);
        //m_invokedMethodsPossibleExceptions.put(nodeString,nodePossibleExceptions);		
	
	
	private HashSet<ExceptionFlow> GetExceptionsFromXMLSyntax(MethodDeclaration nodemDeclar) {
		HashSet<ExceptionFlow> exceptions = new HashSet<ExceptionFlow>();
		
		//nodemDeclar.getJavadoc().
		
		return exceptions;
	}

	private HashSet<ExceptionFlow> processNodeForCheckedExceptions(IMethodBinding nodeBindingInfo)
	{
		HashSet<ExceptionFlow> exceptions = new HashSet<ExceptionFlow>();
		
		for( ITypeBinding type : nodeBindingInfo.getExceptionTypes())
		{
			ExceptionFlow flow = new ExceptionFlow(type, nodeBindingInfo.getKey());
			flow.setIsBindingInfo(true);			
			exceptions.add(flow);
		}
		
		return exceptions;
	}
	private HashSet<ExceptionFlow> processNodeForJavaDocSemantic(IMethodBinding nodeBindingInfo)
	{
		HashSet<ExceptionFlow> exceptions = new HashSet<ExceptionFlow>();
		
		//TODO: review how to get javadoc based on binding info.
		
//		for( ITypeBinding type : nodeBindingInfo.getExceptionTypes())
//		{
//			ExceptionFlow flow = new ExceptionFlow(m_exceptionType, type);
//			flow.setIsBindingInfo(true);
//			
//			exceptions.add(flow);
//		}
		
		return exceptions;
	}
	
	public HashMap<String, Byte> getInvokedMethodsBinded() {
		return m_invokedMethodsBinded;
	}
	
	public HashSet<ExceptionFlow> getDistinctPossibleExceptions() {
		return m_possibleExceptions;
	}
	
	public HashMap<String, HashSet<ExceptionFlow>> getInvokedMethodsHandlerType() {
		return m_invokedMethodsPossibleExceptions;
	}
	
	/// <summary>
    /// To check whether an invocation is a logging statement
    /// </summary>
    
    public int countMetricsForExceptions(String strKey, int intCode){
    	//TODO: Fix return to use the String strKey
    	return 1;
    	//return (int) m_possibleExceptions.values().stream().filter(exception -> exception. intValue() == intCode).count();
    	//return (int) m_possibleExceptions.stream().filter(flow -> flow.getIsBindingInfo()).count();
    }
    
    public int getNumSpecificHandler() {
		return countMetricsForExceptions("HandlerTypeCode",0);
	}
    public int getNumSubsumptionHandler() {
		return countMetricsForExceptions("HandlerTypeCode",1);
	}
    public int getNumSupersumptionHandler() {
		return countMetricsForExceptions("HandlerTypeCode",2);
	}
    public int getNumOtherHandler() {
		return countMetricsForExceptions("HandlerTypeCode",3);
	}
    public int getNumMethodsNotBinded() {	
    	return (int) m_invokedMethodsBinded.values().stream()
    				.filter(value -> value.intValue() == 0)
    				.count();
	}
    public int getNumIsXMLSemantic()
    {
        return countMetricsForExceptions("IsXMLSemantic", 1);
    }
    public int getNumIsXMLSyntax()
    {
        return countMetricsForExceptions("IsXMLSyntax", 1);
    }
    public int getNumIsThrow()
    {
        return countMetricsForExceptions("IsThrow", 1);
    }
    public int getNumIsBindingInfo()
    {
    	return (int) m_possibleExceptions.stream().filter(flow -> flow.getIsBindingInfo()).count();
    	
    	//return countMetricsForExceptions("IsBindingInfo", 1);
    }
    int getChildrenMaxLevel()
    {
        return 1;
    	//TODO: fix to use children nodes level and not hardcoded value
        //return (m_ChildrenNodesLevel.Values.Count > 0) ? m_ChildrenNodesLevel.Values.Max() : 0;
    }
}
