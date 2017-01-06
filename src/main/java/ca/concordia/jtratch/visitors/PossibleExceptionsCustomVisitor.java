package ca.concordia.jtratch.visitors;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;

import ca.concordia.jtratch.ClosedExceptionFlow;
import ca.concordia.jtratch.CodeAnalyzer;
import ca.concordia.jtratch.ExceptionFlow;
import ca.concordia.jtratch.InvokedMethod;
import ca.concordia.jtratch.utility.ASTUtilities;
import ca.concordia.jtratch.utility.Dic;

public class PossibleExceptionsCustomVisitor extends ASTVisitor{
	private static final Logger logger = LogManager.getLogger(PossibleExceptionsCustomVisitor.class.getName());
	
	private ITypeBinding m_catchType;
	private String m_catchFilePath;
	private int m_catchStartLine;
	private String m_declaringNodeKey;
	
	private CompilationUnit m_compilation;
	
	//store just the method identifier and if binded or not.
    private HashMap<String,Byte> m_invokedMethodsBinded = new HashMap<String,Byte>();
    
    //store the method identifiers of the existing method in the visited declaration with their possible exceptions
    private HashMap<String, HashSet<ExceptionFlow>> m_invokedMethodsPossibleExceptions = new HashMap<String,HashSet<ExceptionFlow>>();
    
    //store just the distinct exceptions that comes from this declaration, without method information
    //TODO: if memory issues, this could be removed and calculated on the fly, no need to store it.
    private HashSet<ExceptionFlow> m_possibleExceptions = new HashSet<ExceptionFlow>();
    
    private HashSet<ClosedExceptionFlow> ClosedExceptionFlows = new HashSet<ClosedExceptionFlow>();
	
	private int m_nodeMaxLevel = 0;

    private int m_myLevel = 0;
    private HashMap<String, Integer> m_ChildrenNodesLevel = new HashMap<String, Integer>();

    public boolean m_isForAnalysis;
	
	/// <summary>
    /// Constructor
    /// </summary>
    /// <param name="p_isForAnalysis">This is know if the found exceptions should be evaluated against the parent try-catch block, if any.</param>
    public PossibleExceptionsCustomVisitor(	CompilationUnit p_compilation, int p_level, boolean p_isForAnalysis, 
    										String catchFilePath, int catchStartLine, ITypeBinding p_exceptionType){
    	m_compilation = p_compilation;
        m_myLevel = p_level;
        m_isForAnalysis = p_isForAnalysis;
        
        m_catchFilePath = catchFilePath;
        m_catchStartLine = catchStartLine;
        m_catchType = p_exceptionType;        
    }
    
    /// <summary>
    /// Constructor
    /// </summary>
    /// <param name="p_isForAnalysis">This is know if the found exceptions should be evaluated against the parent try-catch block, if any.</param>
    public PossibleExceptionsCustomVisitor(CompilationUnit p_compilation, int p_level, boolean p_isForAnalysis, 
    										String declaringNodeKey)
    {
    	m_compilation = p_compilation;
        m_myLevel = p_level;
        m_isForAnalysis = p_isForAnalysis;
        
        m_declaringNodeKey = declaringNodeKey;
    }
	
	@Override
	public boolean visit(MethodInvocation node) {
		logger.trace("Visiting a AST node of type "+ node.getNodeType() + " at line " + m_compilation.getLineNumber(node.getStartPosition()));
		processNodeAndVisit(node);
		return super.visit(node);
	}
	@Override
	public boolean visit(ClassInstanceCreation node) {
		logger.trace("Visiting a AST node of type "+ node.getNodeType() + " at line " + m_compilation.getLineNumber(node.getStartPosition()));
		processNodeAndVisit(node);
		return super.visit(node);
	}
	@Override
	 public  boolean visit(ThrowStatement node)
     {
		logger.trace("Visiting a AST node of type "+ node.getNodeType() + " at line " + m_compilation.getLineNumber(node.getStartPosition())); 
		
		//Common Features - parent method
        String parentMethodName;
        ITypeBinding exceptionType;
		ExceptionFlow flow;
		HashSet<ExceptionFlow> possibleException;
		HashSet<ExceptionFlow> validNodePossibleExceptions = new HashSet<ExceptionFlow>();
				
		parentMethodName = ASTUtilities.getMethodName(ASTUtilities.findParentMethod(node));
		exceptionType = node.getExpression().resolveTypeBinding();
		if(exceptionType != null)
			flow = new ExceptionFlow(exceptionType, ExceptionFlow.THROW, parentMethodName);
		else
		{
			String exceptionName;
			if (node.getExpression() != null)
				exceptionName = node.getExpression().toString();             
	        else
	        	exceptionName = "!NO_EXCEPTION_DECLARED!";
	        flow = new ExceptionFlow(exceptionName, ExceptionFlow.THROW, parentMethodName);
		}
		possibleException = new HashSet<ExceptionFlow>();
		possibleException.add(flow);
		
		//save only throws that escapes the containing method.
		validNodePossibleExceptions = getValidPossibleExceptions(node, possibleException);
        closePossibleExceptionFlows(validNodePossibleExceptions, "", 0);
        m_possibleExceptions.addAll(validNodePossibleExceptions);        
                
        //false so that it doesn't visit the ClassInstanceCreation when throw new
        return false;
     }
	
	/// <summary>
    /// Validate exceptions and return only valid exceptions.
	/// A valid exception means exceptions that can escape its containing method.
	/// Here we try to close the exception, if it closes the exception doesn't escape the method.
	/// </summary>
    /// <param name="node">the current node that throws the possible exceptions</param>
	/// <param name="possibleException"> the possible exceptions being thrown</param>
	private HashSet<ExceptionFlow> getValidPossibleExceptions(ASTNode node, HashSet<ExceptionFlow> possibleExceptions) 
	{
		if (m_isForAnalysis)
			return possibleExceptions;
		else
		{
			TryStatement parentTry = (TryStatement) ASTUtilities.FindParentTry(node);

	        if (parentTry == null)
	            return possibleExceptions;
	        else
	        {
	        	HashSet<ExceptionFlow> validPossibleExceptions = new HashSet<ExceptionFlow>();
	    		List<CatchClause> catchBlockList = parentTry.catchClauses();
	        	
	        	for(CatchClause catchBlock : catchBlockList)
	        	{
	        		ITypeBinding thrownExceptionType;
	        		ITypeBinding caughtExceptionType;
	        		
	        		caughtExceptionType = catchBlock.getException().getType().resolveBinding();
	        		
	        		for(ExceptionFlow exception : possibleExceptions)
	            	{
	        			thrownExceptionType = exception.getThrownType();
	        			
	        			if(!ClosedExceptionFlow.IsCloseableExceptionFlow(caughtExceptionType, thrownExceptionType))
	        				validPossibleExceptions.add(exception);
	            	}
	        	}
	        	return validPossibleExceptions;
	        }	        
		}
	}

	private void processNodeAndVisit(ASTNode node) 
	{
		int startLine;
		IMethodBinding nodeBindingInfo;
		boolean binded;
		String nodeString;
		//nodePossibleExceptions store all possible exceptions from the node being visited
		//these will be validated (and added to validNodePossibleExceptions) before being added to the invoked methods list
		HashSet<ExceptionFlow> nodePossibleExceptions = new HashSet<ExceptionFlow>();
		HashSet<ExceptionFlow> validNodePossibleExceptions = new HashSet<ExceptionFlow>();
		HashMap<String, HashSet<ExceptionFlow>> nodeAndNodePossibleExceptions = new HashMap<String, HashSet<ExceptionFlow>>();
        
		startLine = m_compilation.getLineNumber(node.getStartPosition());
		nodeBindingInfo = getBindingInfo(node);
		binded = (nodeBindingInfo != null) ? true : false;
		nodeString = binded ? nodeBindingInfo.getKey() : node.toString();
				
		initializeLocalVisitInfo(nodeString, binded);
		
		nodePossibleExceptions.addAll(getPossibleExceptionsFromBindingInfo(nodeBindingInfo, nodeString));
		nodePossibleExceptions.addAll(getCachedPossibleExceptions(nodeString, binded));		
		
        m_ChildrenNodesLevel.put(nodeString, m_ChildrenNodesLevel.get(nodeString) + CodeAnalyzer.InvokedMethods.get(nodeString).getChildrenMaxLevel());

       	//TODO evaluate stuff for when analysis when not - close flows!!! getValidPossibleExceptions(node, nodePossibleExceptions);
       	//TODO combine exceptions that are actually from a same origin
    	
        validNodePossibleExceptions = getValidPossibleExceptions(node, nodePossibleExceptions);
        closePossibleExceptionFlows(validNodePossibleExceptions, nodeString, startLine);
        //TODO: close exception and change type to ClosedExceptionFlow
        m_possibleExceptions.addAll(validNodePossibleExceptions);
        nodeAndNodePossibleExceptions.put(nodeString, validNodePossibleExceptions);
        Dic.MergeDic2(m_invokedMethodsPossibleExceptions, nodeAndNodePossibleExceptions);
        
	}
	
	private void closePossibleExceptionFlows(HashSet<ExceptionFlow> exceptions, String invokedMethodKey, Integer invokedMethodLine) 
	{
		//A visit FOR analysis will let caught exceptions escape the validation because those are the possible exception we want to see for each catch block.
 		//A visit NOT for analysis will go through validation that won't allow exceptions that are caught in a inner scope pass to the outside.
 		//When FOR analysis we also need to close the flows based on the given catch block.
        if (this.m_isForAnalysis)
        {
        	for(ExceptionFlow exception : exceptions)
	    	{
				ClosedExceptionFlow closedExceptionFlow = new ClosedExceptionFlow(exception);
				closedExceptionFlow.closeExceptionFlow(this.m_catchType, exception.getThrownType(), 
						this.m_catchFilePath, this.m_catchStartLine, invokedMethodKey, invokedMethodLine);
				this.ClosedExceptionFlows.add(closedExceptionFlow);			
	    	}
        } 
//        else
//        {
//        	for(ExceptionFlow exception : exceptions)
//	    	{
//				ClosedExceptionFlow closedExceptionFlow = new ClosedExceptionFlow(exception);
//				closedExceptionFlow.closeExceptionFlow(m_catchType, exception.getThrownType(), m_declaringNodeKey);
//				//CodeAnalyzer.AllClosedExceptionFlows.add(closedExceptionFlow);			
//	    	}
//        }
				
	}

	private HashSet<ExceptionFlow> getCachedPossibleExceptions(String nodeString, boolean binded) {
		//Go grab data if method not yet known
        if (!CodeAnalyzer.InvokedMethods.containsKey(nodeString)) {
        	if(binded) {
        		CodeAnalyzer.InvokedMethods.put(nodeString, new InvokedMethod(nodeString, true));        		
        		collectBindedInvokedMethodDataFromDeclaration(CodeAnalyzer.InvokedMethods.get(nodeString), nodeString);        		
        	} 
        	else
        		CodeAnalyzer.InvokedMethods.put(nodeString, new InvokedMethod(nodeString, false));
        }
		//TODO: if already known, what to do?        
        return CodeAnalyzer.InvokedMethods.get(nodeString).getExceptionFlowSet();		
	}

	private void initializeLocalVisitInfo(String nodeString, boolean binded) {
		///update this declaration (scope being visited) metrics
		m_invokedMethodsBinded.put(nodeString, (byte) (binded ? 1 : 0) );
		if (!m_ChildrenNodesLevel.containsKey(nodeString))
			m_ChildrenNodesLevel.put(nodeString, 1);		
	}

	private IMethodBinding getBindingInfo(ASTNode node) 
	{
		if(node.getNodeType() == ASTNode.METHOD_INVOCATION)
			return ((MethodInvocation) node).resolveMethodBinding();
		else if(node.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION)
			return ((ClassInstanceCreation) node).resolveConstructorBinding();
		else
			return null;		
	}

	private void collectBindedInvokedMethodDataFromDeclaration(InvokedMethod invokedMethod, String nodeString) 
	{
		MethodDeclaration nodemDeclar;
		nodemDeclar = (MethodDeclaration) m_compilation.findDeclaringNode(nodeString);
				
		if(nodemDeclar == null && CodeAnalyzer.AllMyMethods.get(nodeString) != null){
			nodemDeclar = CodeAnalyzer.AllMyMethods.get(nodeString).getDeclaration();
		}
		if(nodemDeclar != null){
			invokedMethod.setVisited(true);
			PossibleExceptionsCustomVisitor possibleExceptionsCustomVisitor = new PossibleExceptionsCustomVisitor(m_compilation, m_myLevel + 1, false, nodeString);
			nodemDeclar.accept(possibleExceptionsCustomVisitor);
			
            Dic.MergeDic2(m_invokedMethodsBinded, possibleExceptionsCustomVisitor.m_invokedMethodsBinded);
            //TODO: validate before inserting into storage
            invokedMethod.getExceptionFlowSet().addAll(possibleExceptionsCustomVisitor.m_possibleExceptions);
            invokedMethod.setChildrenMaxLevel(possibleExceptionsCustomVisitor.getChildrenMaxLevel());
            invokedMethod.getExceptionFlowSet().addAll(GetExceptionsFromJavaDoc(nodemDeclar.getJavadoc(), nodeString));            
		}
	}
		//Dic.MergeDic2(m_possibleExceptions, exceptionTypeNames);
        //m_possibleExceptions.addAll(nodePossibleExceptions);
        //m_invokedMethodsPossibleExceptions.put(nodeString,nodePossibleExceptions);	
	
	private HashSet<ExceptionFlow> GetExceptionsFromJavaDoc(Javadoc nodeJavaDoc, String originalNode) 
	{
		HashSet<ExceptionFlow> exceptions = new HashSet<ExceptionFlow>();
		if(nodeJavaDoc != null) 
		{
			HashSet<SimpleName> exceptionNames = new HashSet<SimpleName>();
			List<TagElement> allTagsList; 
			
			allTagsList = nodeJavaDoc.tags();
			allTagsList	.stream()
						.filter(tag -> (tag.getTagName() == TagElement.TAG_THROWS) || (tag.getTagName() == TagElement.TAG_EXCEPTION))
						.forEach(tag -> exceptionNames.add((SimpleName) tag.fragments().get(0)));
			
			//FIXME: fix bug here: org.eclipse.jdt.core.dom.QualifiedName cannot be cast to org.eclipse.jdt.core.dom.SimpleName
			
			Iterator<SimpleName> iter = exceptionNames.iterator();
			
			while (iter.hasNext())
			{
				ITypeBinding type = iter.next().resolveTypeBinding();
				ExceptionFlow flow = new ExceptionFlow(type, ExceptionFlow.JAVADOC_SYNTAX, originalNode);
				exceptions.add(flow);
			}			
		}
	return exceptions;
	}

	private HashSet<ExceptionFlow> getPossibleExceptionsFromBindingInfo(IMethodBinding nodeBindingInfo, String originalNode)
	{
		HashSet<ExceptionFlow> exceptions = new HashSet<ExceptionFlow>();
		if (nodeBindingInfo != null){
			for( ITypeBinding type : nodeBindingInfo.getExceptionTypes())
			{
				ExceptionFlow flow = new ExceptionFlow(type, ExceptionFlow.BINDING_INFO, originalNode);
				exceptions.add(flow);
			}
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
	
	public HashSet<ClosedExceptionFlow> getClosedExceptionFlows() {
		return ClosedExceptionFlows;
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
    
    public boolean isM_isForAnalysis() {
		return m_isForAnalysis;
	}

	private void setM_isForAnalysis(boolean m_isForAnalysis) {
		this.m_isForAnalysis = m_isForAnalysis;
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
