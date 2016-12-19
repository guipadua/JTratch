package ca.concordia.jtratch;

import org.eclipse.jdt.core.dom.ITypeBinding;

import ca.concordia.jtratch.utility.ASTUtilities;

public class ExceptionFlow {
	
	//Thrown Exception Type info
	private String thrownTypeName;
	private ITypeBinding thrownType;
	
	//Original method info
	//Assumption: all declared methods are successfully binded.
	//Store where this exception was found. 
	//TODO: If found by different ways, the value will be based on precedence, according to this order:
	//			Throw -> deepest level found -> others...
	private String originalMethodBindingKey = "";
	private Byte LevelFound = (byte) 0;
	
	private Boolean isBindingInfo = false;	
	private Boolean isJavadocSemantic = false;
	private Boolean isJavadocSyntax = false;
	private Boolean isThrow = false;
		
	//Catch info
	private ITypeBinding catchedType;
	private Byte HandlerTypeCode = (byte) -9;
	private String catchFilePath;
	private Integer catchStartLine;
	
	public static final String BINDING_INFO = "binding_info";
	public static final String JAVADOC_SEMANTIC = "javadoc_semantic";
	public static final String JAVADOC_SYNTAX = "javadoc_syntax";
	public static final String THROW = "throw";
	
	public ExceptionFlow(ITypeBinding thrownType, String originType, String originalMethodBindingKey) {
		this.setType(thrownType);
		this.setOriginalMethodBindingKey(originalMethodBindingKey);
		this.setOriginFlag(originType);
				
		//HandlerTypeCode = GetHandlerTypeCode(catchedType, thrownType);
	}
	
	//TODO: constructor with find type based on name - or just ignore this, no binding, no fun.
	
	public ExceptionFlow(String exceptionName, String originType, String originalMethodBindingKey) {
		this.setThrownTypeName(exceptionName);
		this.setOriginalMethodBindingKey(originalMethodBindingKey);
		this.setOriginFlag(originType);
	}

	public String getThrownTypeName() {
		return thrownTypeName;
	}

	public void setThrownTypeName(String thrownTypeName) {
		this.thrownTypeName = thrownTypeName;
	}

	public ITypeBinding getCatchedType() {
		return catchedType;
	}

	public void setCatchedType(ITypeBinding catchedType) {
		this.catchedType = catchedType;
		this.setHandlerTypeCode(calculateHandlerTypeCode(this.catchedType, this.thrownType));
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

	public ITypeBinding getType() {
		return thrownType;
	}

	public void setType(ITypeBinding thrownType) {
		this.thrownType = thrownType;
	}

	public Byte getLevelFound() {
		return LevelFound;
	}

	public void setLevelFound(Byte levelFound) {
		LevelFound = levelFound;
	}

	public Boolean getIsBindingInfo() {
		return isBindingInfo;
	}

	public void setIsBindingInfo(Boolean isBindingInfo) {
		this.isBindingInfo = isBindingInfo;
	}

	public Boolean getIsJavadocSemantic() {
		return isJavadocSemantic;
	}

	public void setIsJavadocSemantic(Boolean isJavadocSemantic) {
		this.isJavadocSemantic = isJavadocSemantic;
	}

	public Boolean getIsJavadocSyntax() {
		return isJavadocSyntax;
	}

	public void setIsJavadocSyntax(Boolean isJavadocSyntax) {
		this.isJavadocSyntax = isJavadocSyntax;
	}

	public Boolean getIsThrow() {
		return isThrow;
	}

	public void setIsThrow(Boolean isThrow) {
		this.isThrow = isThrow;
	}

	public Byte getHandlerTypeCode() {
		return HandlerTypeCode;
	}
	
	//should be calculated using calculateHandlerTypeCode
	private void setHandlerTypeCode(Byte handlerTypeCode) {
		HandlerTypeCode = handlerTypeCode;
	}

	public String getOriginalMethodBindingKey() {
		return originalMethodBindingKey;
	}

	public void setOriginalMethodBindingKey(String originalMethodBindingKey) {
		this.originalMethodBindingKey = originalMethodBindingKey;
	}
	
	public void setOriginFlag(String originType)
	{
		switch(originType) {
			case BINDING_INFO: 		this.setIsBindingInfo(true);
									break;
			case JAVADOC_SEMANTIC: 	this.setIsJavadocSemantic(true);
									break;
			case JAVADOC_SYNTAX: 	this.setIsJavadocSyntax(true);
									break;
			case THROW: 			this.setIsThrow(true);
									break;
			default:				break;
	}
	}
	
	public byte calculateHandlerTypeCode(ITypeBinding catchedType, ITypeBinding thrownType)
    {
    	byte handlerTypeCode = -9;
    	
    	if (catchedType != null)
        {
        	if (thrownType != null)
            {
                //In case is the same type, it's specific handler type - code: 0
                //In case the catched type is equal a super class of the possible thrown type, it's a subsumption - code: 1
                //In case the possible thrown type is equal a super class of the catched type, it's a supersumption - code: 2
                //In case it's none of the above - most likely tree of unrelated exceptions: code: 3
                if (catchedType.equals(thrownType))
                {
                    handlerTypeCode = 0;                        
                }
                else if (ASTUtilities.IsSuperType(catchedType, thrownType))
                {
                    handlerTypeCode = 1;                        
                }
                else if (ASTUtilities.IsSuperType(thrownType, catchedType))
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
}
