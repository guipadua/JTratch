package ca.concordia.jtratch.pattern;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public class TreeStatistics {
	public LinkedHashMap<String, Integer> CodeStats;
    public List<CatchBlock> CatchBlockList;
    //public List<APICall> APICallList;
	public List<ThrowsBlock> ThrowsBlockList;
	public List<PossibleExceptionsBlock> PossibleExceptionsBlockList;
	
	public List<MethodDeclaration> MethodDeclarationList;	

    public TreeStatistics()
    {
        CodeStats = new LinkedHashMap<String, Integer>();
        
        CodeStats.put("NumFiles", 0);
        
        //From process compilation
        CodeStats.put("NumLOC", 0);
        CodeStats.put("NumCatchBlock", 0);
        
        //From collection of catch blocks per AST - roll-up
        CodeStats.put("NumBinded", 0);
        CodeStats.put("NumNoDeclaration", 0);
        CodeStats.put("NumMethodsNotBinded", 0);
        CodeStats.put("NumLoggedCatchBlock", 0);
        CodeStats.put("NumDistinctExceptionTypeCatch", 0);          
        CodeStats.put("NumRecoveredBinding", 0);          
        
        //From process compilation
      	CodeStats.put("NumPossibleExceptionBlock", 0);
              
      	//From collection of possible exceptions blocks per AST - roll-up
        CodeStats.put("NumDistinctPossibleExceptions", 0);
        
        //From CodeAnalyzer - global method storages:
        CodeStats.put("NumDeclaredMethods", 0);
		CodeStats.put("NumInvokedMethods", 0);
		CodeStats.put("NumInvokedMethodsBinded", 0);
		CodeStats.put("NumInvokedMethodsDeclared", 0);
		CodeStats.put("NumInvokedMethodsExtDocPresent", 0);		
        
		//From process compilation
		CodeStats.put("NumThrowsBlock", 0);        
		
		//From collection of possible exceptions blocks per AST - roll-up
		CodeStats.put("NumDistinctExceptionTypeThrows", 0);
		
        //CodeStats.Add("NumLoggedLOC", 0);
        //CodeStats.Add("NumCall", 0);
        //CodeStats.Add("NumLogging", 0);
        //CodeStats.Add("NumClass", 0);
        //CodeStats.Add("NumLoggedClass", 0);
        //CodeStats.Add("NumMethod", 0);
        //CodeStats.Add("NumExceptions", 0);
        //CodeStats.Add("NumLoggedMethod", 0);
        //CodeStats.Add("NumLoggedFile", 0);
        //CodeStats.Add("NumCallType", 0);
        //CodeStats.Add("NumAPICall", 0);
        //CodeStats.Add("NumLoggedAPICall", 0);
    }
    
    public static <T> void Add(HashMap<T, Integer> dic1, HashMap<T, Integer> dic2)
    {
        for (Map.Entry<T, Integer> entry : dic2.entrySet())
        {
            if (entry.getKey() == null) continue;
            if (dic1.containsKey(entry.getKey()))
            {
                dic1.put(entry.getKey(), dic1.get(entry.getKey()) + entry.getValue());
            }
            else
            {
                dic1.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
