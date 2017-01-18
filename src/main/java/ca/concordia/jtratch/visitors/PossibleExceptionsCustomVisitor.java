package ca.concordia.jtratch.visitors;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;

import ca.concordia.jpexjd.MyClass;
import ca.concordia.jpexjd.MyMethod;
import ca.concordia.jpexjd.MyPackage;
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
	//private String m_declaringNodeKey;
	
	private CompilationUnit m_compilation;
	
	//store just the method identifier and if binded or not.
    private HashMap<String,Byte> m_invokedMethodsBinded = new HashMap<String,Byte>();
    
    //store the method identifiers of the existing method in the visited declaration with their possible exceptions
    private HashMap<String, HashSet<ExceptionFlow>> m_invokedMethodsPossibleExceptions = new HashMap<String,HashSet<ExceptionFlow>>();
    
    //store just the distinct exceptions that comes from this declaration, without method information
    //TODO: if memory issues, this could be removed and calculated on the fly, no need to store it.
    private HashSet<ExceptionFlow> m_possibleExceptions = new HashSet<ExceptionFlow>();
    
    private HashSet<ClosedExceptionFlow> closedExceptionFlows = new HashSet<ClosedExceptionFlow>();
	
	private Byte m_nodeMaxLevel = 0;

    private Byte m_myLevel = 0;
    private HashMap<String, Integer> m_ChildrenNodesLevel = new HashMap<String, Integer>();

    public boolean m_isForAnalysis;
	
	/// <summary>
    /// Constructor
    /// </summary>
    /// <param name="p_isForAnalysis">This is know if the found exceptions should be evaluated against the parent try-catch block, if any.</param>
    public PossibleExceptionsCustomVisitor(	CompilationUnit p_compilation, Byte p_level, boolean p_isForAnalysis, 
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
    public PossibleExceptionsCustomVisitor(CompilationUnit p_compilation, Byte p_level, boolean p_isForAnalysis, 
    										String declaringNodeKey)
    {
    	m_compilation = p_compilation;
        m_myLevel = p_level;
        m_isForAnalysis = p_isForAnalysis;
        
        //m_declaringNodeKey = declaringNodeKey;
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
			flow = new ExceptionFlow(exceptionType, ExceptionFlow.THROW, parentMethodName, m_myLevel);
		else
		{
			String exceptionName;
			if (node.getExpression() != null)
				exceptionName = node.getExpression().toString();             
	        else
	        	exceptionName = "!NO_EXCEPTION_DECLARED!";
	        flow = new ExceptionFlow(exceptionName, ExceptionFlow.THROW, parentMethodName, m_myLevel);
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
		nodeBindingInfo = ASTUtilities.getBindingInfo(node);
		binded = (nodeBindingInfo != null) ? true : false;
		nodeString = binded ? nodeBindingInfo.getKey() : node.toString();
				
		initializeLocalVisitInfo(nodeString, binded);
		
		nodePossibleExceptions.addAll(getCachedPossibleExceptions(nodeString, binded, nodeBindingInfo));		
		
        m_ChildrenNodesLevel.put(nodeString, m_ChildrenNodesLevel.get(nodeString) + CodeAnalyzer.InvokedMethods.get(nodeString).getChildrenMaxLevel());

       	//TODO combine exceptions that are actually from a same origin
    	
        validNodePossibleExceptions = getValidPossibleExceptions(node, nodePossibleExceptions);
        closePossibleExceptionFlows(validNodePossibleExceptions, nodeString, startLine);
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
				this.closedExceptionFlows.add(closedExceptionFlow);			
	    	}
        }				
	}

	private HashSet<ExceptionFlow> getCachedPossibleExceptions(String nodeString, boolean binded, IMethodBinding nodeBindingInfo) {
		//Go grab data if method not yet known
        if (!CodeAnalyzer.InvokedMethods.containsKey(nodeString)) {
        	if(binded) {
        		CodeAnalyzer.InvokedMethods.put(nodeString, new InvokedMethod(nodeString, true));        		
        		collectBindedInvokedMethodDataFromDeclaration(CodeAnalyzer.InvokedMethods.get(nodeString), nodeString);
        		collectBindedInvokedMethodDataFromBindingInfo(CodeAnalyzer.InvokedMethods.get(nodeString), nodeBindingInfo, nodeString);
        		collectBindedInvokedMethodDataFromExternalJavadoc(CodeAnalyzer.InvokedMethods.get(nodeString), nodeBindingInfo, nodeString); 
        	} 
        	else
        		CodeAnalyzer.InvokedMethods.put(nodeString, new InvokedMethod(nodeString, false));
        }
		//TODO: if already known, what to do?        
        return CodeAnalyzer.InvokedMethods.get(nodeString).getExceptionFlowSetByType();		
	}

	private void initializeLocalVisitInfo(String nodeString, boolean binded) {
		///update this declaration (scope being visited) metrics
		m_invokedMethodsBinded.put(nodeString, (byte) (binded ? 1 : 0) );
		if (!m_ChildrenNodesLevel.containsKey(nodeString))
			m_ChildrenNodesLevel.put(nodeString, 1);		
	}

	private void collectBindedInvokedMethodDataFromDeclaration(InvokedMethod invokedMethod, String nodeString) 
	{
		MethodDeclaration nodemDeclar;
		nodemDeclar = (MethodDeclaration) m_compilation.findDeclaringNode(nodeString);
				
		if(nodemDeclar == null && CodeAnalyzer.AllMyMethods.get(nodeString) != null){
			nodemDeclar = CodeAnalyzer.AllMyMethods.get(nodeString).getDeclaration();
		}
		if(nodemDeclar != null){
			invokedMethod.setDeclared(true);
			PossibleExceptionsCustomVisitor possibleExceptionsCustomVisitor = new PossibleExceptionsCustomVisitor(m_compilation, (byte) (m_myLevel + 1), false, nodeString);
			nodemDeclar.accept(possibleExceptionsCustomVisitor);
			
            Dic.MergeDic2(m_invokedMethodsBinded, possibleExceptionsCustomVisitor.m_invokedMethodsBinded);
            
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
			HashSet<Object> exceptionNames = new HashSet<Object>();
			List<TagElement> allTagsList; 
			
			allTagsList = nodeJavaDoc.tags();
			allTagsList	.stream()
						.filter(tag -> (tag.getTagName() == TagElement.TAG_THROWS) || (tag.getTagName() == TagElement.TAG_EXCEPTION))
						.forEach(tag -> exceptionNames.add(tag.fragments().get(0)));
			
			Iterator<Object> iter = exceptionNames.iterator();
			
			while (iter.hasNext())
			{
				Object object;
				ITypeBinding type = null;
				
				object = iter.next();
				if(object instanceof SimpleName)
				{
					type = ((SimpleName) object).resolveTypeBinding();					
				}
				else if(object instanceof QualifiedName)
				{
					type = ((QualifiedName) object).resolveTypeBinding();
				}
				else
				{
					logger.warn("Non expected type of javadoc exception was found: " + object.getClass().getName() + "!");
				}
				if(type != null)
				{
					ExceptionFlow flow = new ExceptionFlow(type, ExceptionFlow.JAVADOC_SYNTAX, originalNode, m_myLevel);
					exceptions.add(flow);
				}					
			}			
		}
	return exceptions;
	}

	private void collectBindedInvokedMethodDataFromBindingInfo(InvokedMethod invokedMethod, IMethodBinding nodeBindingInfo, String originalNode)
	{
		HashSet<ExceptionFlow> exceptions = new HashSet<ExceptionFlow>();
		if (nodeBindingInfo != null){
			for( ITypeBinding type : nodeBindingInfo.getExceptionTypes())
			{
				ExceptionFlow flow = new ExceptionFlow(type, ExceptionFlow.BINDING_INFO, originalNode, m_myLevel);
				exceptions.add(flow);
			}
		}
		invokedMethod.getExceptionFlowSet().addAll(exceptions);
	}
	
	private void collectBindedInvokedMethodDataFromExternalJavadoc(InvokedMethod invokedMethod, IMethodBinding nodeBindingInfo, String originalNode)
	{
		HashSet<ExceptionFlow> exceptionFlows = new HashSet<ExceptionFlow>();
		
		String packageName = "";
		String className = "";
		String methodAndSignature  = "";
		
		MyPackage myPackage = null;
		MyClass myClass = null;
		MyMethod myMethod = null;
		Set<String> exceptions = new HashSet<String>();
		
		packageName = nodeBindingInfo.getDeclaringClass().getPackage().getName();
		className = nodeBindingInfo.getDeclaringClass().getName();
		
		myPackage = CodeAnalyzer.externalJavadoc.getPackages().get(packageName);
		if(myPackage != null){
			myClass = myPackage.getClasses().get(className);
			if(myClass != null){
				methodAndSignature = ASTUtilities.getMethodNameAndSignature(nodeBindingInfo, false);
				myMethod = myClass.getMethods().get(methodAndSignature);
				if(myMethod != null){
					invokedMethod.setExternalJavadocPresent(true);
					exceptions = myMethod.getThrowTags().keySet();	
				}							
			}
		}
		
		for( String exceptionSimpleName : exceptions)
		{
			String exceptionNameQualified = null;
			if(myPackage.getClasses().containsKey(exceptionSimpleName))			{
				exceptionNameQualified = packageName + "." + myPackage.getClasses().get(exceptionSimpleName).getName();			
			} else
			{
				Set<String> exceptionOptions = new HashSet<String>();
				CodeAnalyzer.externalJavadoc.getPackages().entrySet().forEach( entry -> {
					if(entry.getValue().getClasses().containsKey(exceptionSimpleName)){
						exceptionOptions.add(entry.getKey() + "." + entry.getValue().getClasses().get(exceptionSimpleName).getName());
						//break;
					}					
				});
				if(exceptionOptions.size() == 1)
					exceptionNameQualified = exceptionOptions.iterator().next();
				else if(exceptionOptions.size() == 0)
					exceptionNameQualified = exceptionSimpleName;
				else if(exceptionOptions.size() > 1)
					exceptionNameQualified = "!EXCEPTION_IN_MULIPLE_PACKAGES!";
			}
				
			ITypeBinding exceptionType = m_compilation.getAST().resolveWellKnownType(exceptionNameQualified);
			if(exceptionType == null)
				exceptionType = ASTUtilities.getType(exceptionNameQualified);
			
			ExceptionFlow flow;
			if(exceptionType != null)
				flow = new ExceptionFlow(exceptionType, ExceptionFlow.JAVADOC_SEMANTIC, originalNode, m_myLevel);
			else
			{
				flow = new ExceptionFlow(exceptionNameQualified, ExceptionFlow.JAVADOC_SEMANTIC, originalNode, m_myLevel);
			}			
			exceptionFlows.add(flow);
		}
		
		invokedMethod.getExceptionFlowSet().addAll(exceptionFlows);
	}
	
	public HashMap<String, Byte> getInvokedMethodsBinded() {
		return m_invokedMethodsBinded;
	}
	
	public HashSet<ClosedExceptionFlow> getClosedExceptionFlows() {
		return closedExceptionFlows;
	}

	public HashSet<ExceptionFlow> getDistinctPossibleExceptions() {
		return m_possibleExceptions;
	}
	
	public HashMap<String, HashSet<ExceptionFlow>> getInvokedMethodsPossibleExceptions() {
		return m_invokedMethodsPossibleExceptions;
	}
	
	public boolean isM_isForAnalysis() {
		return m_isForAnalysis;
	}
	
	public int getNumMethodsNotBinded() {	
    	return (int) m_invokedMethodsBinded.values().stream()
    				.filter(value -> value.intValue() == 0)
    				.count();
	}
    
	public int getNumSpecificHandler() {
    	return (int) getClosedExceptionFlows().stream().filter(flow -> flow.getHandlerTypeCode() == 0).count();
	}
    public int getNumSubsumptionHandler() {
    	return (int) getClosedExceptionFlows().stream().filter(flow -> flow.getHandlerTypeCode() == 1).count();
	}
    public int getNumSupersumptionHandler() {
    	return (int) getClosedExceptionFlows().stream().filter(flow -> flow.getHandlerTypeCode() == 2).count();
	}
    public int getNumOtherHandler() {
    	return (int) getClosedExceptionFlows().stream().filter(flow -> flow.getHandlerTypeCode() == 3).count();
	}
    
    public int getNumIsJavadocSemantic()
    {
    	return (int) m_possibleExceptions.stream().filter(flow -> flow.getIsJavadocSemantic()).count();
    }
    public int getNumIsJavadocSyntax()
    {
    	return (int) m_possibleExceptions.stream().filter(flow -> flow.getIsJavadocSyntax()).count();
    }
    public int getNumIsThrow()
    {
    	return (int) m_possibleExceptions.stream().filter(flow -> flow.getIsThrow()).count();
    }
    public int getNumIsBindingInfo()
    {
    	return (int) m_possibleExceptions.stream().filter(flow -> flow.getIsBindingInfo()).count();
    }
    
    int getChildrenMaxLevel()
    {
        return (m_ChildrenNodesLevel.values().size() > 0) ? Collections.max(m_ChildrenNodesLevel.values()) : 0;
    }
}
