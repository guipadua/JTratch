package ca.concordia.jtratch.pattern;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.dom.CompilationUnit;

import ca.concordia.jtratch.utility.Dic;
import ca.concordia.jtratch.utility.Tuple;

public class CodeStatistics extends TreeStatistics {
	public List<Tuple<CompilationUnit, TreeStatistics>> TreeStats;
    public CatchDic CatchBlocks;
    public ThrowsDic ThrowsBlocks;
    
    //public CallDic APICalls;
    
    private static final Logger logger = LogManager.getLogger(CodeStatistics.class.getName());
    
    public CodeStatistics(List<Tuple<CompilationUnit, TreeStatistics>> codeStatsList)
    {
        TreeStats = codeStatsList;
        CatchBlocks = new CatchDic();
        ThrowsBlocks = new ThrowsDic();
        //APICalls = new CallDic();
        CodeStats = new HashMap<String, Integer>();
        for (Tuple<CompilationUnit, TreeStatistics> treetuple : codeStatsList)
        {
            if (treetuple == null) continue;
            if (treetuple.Item2.CatchBlockList != null)
            {
                CatchBlocks.Add(treetuple.Item2.CatchBlockList);
            }
            if (treetuple.Item2.ThrowsBlockList != null)
            {
            	ThrowsBlocks.Add(treetuple.Item2.ThrowsBlockList);
            }
            //if (treetuple.Item2.APICallList != null)
            //{
            //    APICalls.Add(treetuple.Item2.APICallList);
            //}
            if (treetuple.Item2.CodeStats != null) 
            {
                Dic.MergeDic1(CodeStats, treetuple.Item2.CodeStats);
            }
        }
        CodeStats.put("NumExceptionTypeCatch",CatchBlocks.size());
        CodeStats.put("NumLoggedCatchBlock",CatchBlocks.NumLogged);
        CodeStats.put("NumExceptionTypeThrows",ThrowsBlocks.size());
        
        CodeStats.put("NumBinded",CatchBlocks.NumBinded);
        CodeStats.put("NumRecoveredBinding",CatchBlocks.NumRecoveredBinding);
        CodeStats.put("NumMethodsNotBinded",CatchBlocks.NumMethodsNotBinded);
        
        //CodeStats["NumCallType"] = APICalls.Count;
        //CodeStats["NumAPICall"] = APICalls.NumAPICall;
        //CodeStats["NumLoggedAPICall"] = APICalls.NumLogged;
    }

    public void PrintSatistics()
    {           
        for (Map.Entry<String, Integer> entry : CodeStats.entrySet())
        {
        	logger.info(entry.getKey() + ": " + entry.getValue());
        }
        CatchBlocks.PrintToFile();
        ThrowsBlocks.PrintToFile();
        
        //APICalls.PrintToFile();
    }
}
