package ca.concordia.jtratch;

import org.eclipse.jdt.core.dom.ITypeBinding;

public class ExceptionFlow {
	
	public static final String BINDING_INFO = "binding_info";
	public static final String JAVADOC_SEMANTIC = "javadoc_semantic";
	public static final String JAVADOC_SYNTAX = "javadoc_syntax";
	public static final String THROW = "throw";
	
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
	
	public ExceptionFlow(ITypeBinding thrownType, String originType, String originalMethodBindingKey, Byte levelFound) {
		this.setThrownType(thrownType);
		this.setOriginFlag(originType);
		this.setOriginalMethodBindingKey(originalMethodBindingKey);
		this.setLevelFound(levelFound);
	}
	
	public ExceptionFlow(String exceptionName, String originType, String originalMethodBindingKey, Byte levelFound) {
		this.setThrownTypeName(exceptionName);
		this.setOriginFlag(originType);
		this.setOriginalMethodBindingKey(originalMethodBindingKey);
		this.setLevelFound(levelFound);
	}
	
	public ExceptionFlow(ExceptionFlow exceptionFlow) {
		if(exceptionFlow.getThrownType() != null)
			this.setThrownType(exceptionFlow.getThrownType());
		this.setThrownTypeName(exceptionFlow.getThrownTypeName());
		this.setOriginalMethodBindingKey(exceptionFlow.getOriginalMethodBindingKey());
		this.setLevelFound(exceptionFlow.getLevelFound());
		
		this.setIsBindingInfo(exceptionFlow.getIsBindingInfo());
		this.setIsJavadocSemantic(exceptionFlow.getIsJavadocSemantic());
		this.setIsJavadocSyntax(exceptionFlow.getIsJavadocSyntax());
		this.setIsThrow(exceptionFlow.getIsThrow());
	}

	public String getThrownTypeName() {
		return thrownTypeName;
	}

	public void setThrownTypeName(String thrownTypeName) {
		this.thrownTypeName = thrownTypeName;
	}

	public ITypeBinding getThrownType() {
		return thrownType;
	}

	public void setThrownType(ITypeBinding thrownType) {
		this.thrownType = thrownType;
		this.setThrownTypeName(thrownType.getQualifiedName());
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
	
	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(getThrownTypeName());
		
		return sb.toString();		
	}
	
}
