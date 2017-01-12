package ca.concordia.jtratch;

import org.eclipse.jdt.core.dom.ITypeBinding;

import ca.concordia.jtratch.utility.ASTUtilities;

public class ClosedExceptionFlow extends ExceptionFlow {
	
	//Catch info
	private String caughtTypeName = "";
	private ITypeBinding caughtType = null;
	private Byte handlerTypeCode = (byte) -9;
		
	private String catchFilePath = "";
	private Integer catchStartLine = 0;
	private String declaringNodeKey = "";
	
	private String invokedMethodKey = "";
	private Integer invokedMethodLine = 0;
	
	
	public ClosedExceptionFlow(String exceptionName, String originType, String originalMethodBindingKey, Byte levelFound) {
		super(exceptionName, originType, originalMethodBindingKey, levelFound);
		// TODO Auto-generated constructor stub
	}

	public ClosedExceptionFlow(ITypeBinding thrownType, String originType, String originalMethodBindingKey, Byte levelFound) {
		super(thrownType, originType, originalMethodBindingKey, levelFound);
		// TODO Auto-generated constructor stub
	}
	
	public ClosedExceptionFlow(ExceptionFlow exception) {
		super(exception);		
	}

	public ITypeBinding getCaughtType() {
		return caughtType;
	}

	public void setCaughtType(ITypeBinding caughtType) {
		this.caughtType = caughtType;
		if(caughtType != null)
			this.setCaughtTypeName(caughtType.getQualifiedName());
	}

	public String getDeclaringNodeKey() {
		return declaringNodeKey;
	}

	public void setDeclaringNodeKey(String declaringNodeKey) {
		this.declaringNodeKey = declaringNodeKey;
	}

	public String getInvokedMethodKey() {
		return invokedMethodKey;
	}

	public void setInvokedMethodKey(String invokedMethodKey) {
		this.invokedMethodKey = invokedMethodKey;
	}

	public Integer getInvokedMethodLine() {
		return invokedMethodLine;
	}

	public void setInvokedMethodLine(Integer invokedMethodLine) {
		this.invokedMethodLine = invokedMethodLine;
	}

	public String getCatchFilePath() {
		return catchFilePath;
	}

	public void setCatchFilePath(String catchFilePath) {
		this.catchFilePath = catchFilePath;
	}

	public Integer getCatchStartLine() {
		return catchStartLine;
	}

	public void setCatchStartLine(Integer catchStartLine) {
		this.catchStartLine = catchStartLine;
	}
	
	public Byte getHandlerTypeCode() {
		return handlerTypeCode;
	}
	
	//should be calculated using calculateHandlerTypeCode
	public void setHandlerTypeCode(Byte handlerTypeCode) {
		this.handlerTypeCode = handlerTypeCode;
	}
	
	public String getCaughtTypeName() {
		return caughtTypeName;
	}

	public void setCaughtTypeName(String caughtTypeName) {
		this.caughtTypeName = caughtTypeName;
	}
	//TODO: method that calculate handlerType even if no binding
	public static byte calculateHandlerTypeCode(ITypeBinding caughtType, ITypeBinding thrownType)
    {
    	byte handlerTypeCode = -9;
    	
    	if (caughtType != null)
        {
        	if (thrownType != null)
            {
                //In case is the same type, it's specific handler type - code: 0
                //In case the caught type is equal a super class of the possible thrown type, it's a subsumption - code: 1
                //In case the possible thrown type is equal a super class of the caught type, it's a supersumption - code: 2
                //In case it's none of the above - most likely tree of unrelated exceptions: code: 3
                if (caughtType.isEqualTo(thrownType))
                {
                    handlerTypeCode = 0;                        
                }
                else if (ASTUtilities.IsSuperType(caughtType, thrownType))
                {
                    handlerTypeCode = 1;                        
                }
                else if (ASTUtilities.IsSuperType(thrownType, caughtType))
                {
                    handlerTypeCode = 2;                        
                }
                else
                {
                    //it can happen when exceptions are not related on the type tree
                    handlerTypeCode = 3;                        
                }
            }
        	else
                handlerTypeCode = -8;
        }
        return handlerTypeCode;
    }
	
	public void closeExceptionFlow(ITypeBinding caughtType, ITypeBinding thrownType, 
			String catchFilePath, int catchStartLine, String invokedMethodKey, Integer invokedMethodLine)
	{
		if(this.getCaughtTypeName() != null && this.getCaughtTypeName() != "") { return;}
		
		byte handlerTypeCodeToEvaluate = ClosedExceptionFlow.calculateHandlerTypeCode(caughtType, thrownType);
		
		this.setHandlerTypeCode(handlerTypeCodeToEvaluate);
		this.setCaughtType(caughtType);
		this.setCatchFilePath(catchFilePath);
		this.setCatchStartLine(catchStartLine);
		this.setInvokedMethodKey(invokedMethodKey);
		this.setInvokedMethodLine(invokedMethodLine);
	}
	
	public void closeExceptionFlow(ITypeBinding caughtType, ITypeBinding thrownType, String declaringNodeKey)
	{
		if((this.getDeclaringNodeKey() != null && this.getDeclaringNodeKey() != "")){return;}
		
		byte handlerTypeCodeToEvaluate = ClosedExceptionFlow.calculateHandlerTypeCode(caughtType, thrownType);
		
		this.setHandlerTypeCode(handlerTypeCodeToEvaluate);
		this.setCaughtType(caughtType);
		this.setDeclaringNodeKey(declaringNodeKey);
	
	}
	
	public static boolean IsCloseableExceptionFlow(ITypeBinding caughtType, ITypeBinding thrownType)
	{
		byte handlerTypeCodeToEvaluate = ClosedExceptionFlow.calculateHandlerTypeCode(caughtType, thrownType);
		
		//0: SPECIFIC, 1: SUBSUMPTION - the only two possible ways to really catch and could close the flow
		if ((handlerTypeCodeToEvaluate == 0 || handlerTypeCodeToEvaluate == 1))
			return true;
		else
			return false;
	}

	
	
}
