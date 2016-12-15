package ca.concordia.jtratch;

import org.eclipse.jdt.core.dom.ITypeBinding;

import ca.concordia.jtratch.utility.ASTUtilities;

public class ExceptionFlow {
	
	//Thrown Exception Type info
	private String thrownTypeName;
	private ITypeBinding thrownType;
	
	//Original method info
	//Assumption: all declared methods are successfully binded.
	private String originalMethodBindingKey;
	private Byte LevelFound = (byte) 0;
	
	private Boolean IsBindingInfo = false;	
	private Boolean IsXMLSemantic = false;
	private Boolean IsXMLSyntax = false;
	private Boolean IsThrow = false;
		
	//Catch info
	private ITypeBinding catchedType;
	private Byte HandlerTypeCode = (byte) -9;
	private String catchFilePath;
	private Integer catchStartLine;
		
	public ExceptionFlow(ITypeBinding thrownType, String originalMethodBindingKey ) {
		this.setType(thrownType);
		this.setOriginalMethodBindingKey(originalMethodBindingKey);
		
		//HandlerTypeCode = GetHandlerTypeCode(catchedType, thrownType);
	}
//	public ExceptionFlow(ITypeBinding thrownType, ITypeBinding catchedType) {
//		this.thrownType = thrownType;
//		this.setCatchedType(catchedType);
//		HandlerTypeCode = GetHandlerTypeCode(catchedType, thrownType);
//	}
	
	//TODO: constructor with find type based on name - or just ignore this, no binding, no fun.
	
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
		return IsBindingInfo;
	}

	public void setIsBindingInfo(Boolean isBindingInfo) {
		IsBindingInfo = isBindingInfo;
	}

	public Boolean getIsXMLSemantic() {
		return IsXMLSemantic;
	}

	public void setIsXMLSemantic(Boolean isXMLSemantic) {
		IsXMLSemantic = isXMLSemantic;
	}

	public Boolean getIsXMLSyntax() {
		return IsXMLSyntax;
	}

	public void setIsXMLSyntax(Boolean isXMLSyntax) {
		IsXMLSyntax = isXMLSyntax;
	}

	public Boolean getIsThrow() {
		return IsThrow;
	}

	public void setIsThrow(Boolean isThrow) {
		IsThrow = isThrow;
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
