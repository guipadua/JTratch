package ca.concordia.jtratch.pattern;

import java.util.ArrayList;
import java.util.List;

public class PossibleExceptionsBlock extends CommonFeature {
	public static List<String> MetaKeys;
    public static List<String> OpFeaturesKeys;

    public PossibleExceptionsBlock() 
    {
		OperationFeatures.put("IsBindingInfo", 0);
		OperationFeatures.put("IsJavadocSemantic", 0);
		OperationFeatures.put("IsJavadocSyntax", 0);
		OperationFeatures.put("IsThrow", 0);
		
		//MetaInfo.put("PossibleExceptionsBlock", "'-PossibleExceptionsBlock");
           
        MetaKeys = new ArrayList<String>(MetaInfo.keySet());
        OpFeaturesKeys = new ArrayList<String>(OperationFeatures.keySet());        
    }
   
}
