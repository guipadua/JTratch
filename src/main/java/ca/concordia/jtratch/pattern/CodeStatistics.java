package ca.concordia.jtratch.pattern;

import java.util.LinkedHashMap;
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
    public PossibleExceptionsDic PossibleExceptionsBlocks;
    
    //public CallDic APICalls;
    
    private static final Logger logger = LogManager.getLogger(CodeStatistics.class.getName());
    
    public CodeStatistics(List<Tuple<CompilationUnit, TreeStatistics>> codeStatsList)
    {
        TreeStats = codeStatsList;
        CatchBlocks = new CatchDic();
        ThrowsBlocks = new ThrowsDic();
        PossibleExceptionsBlocks = new PossibleExceptionsDic();
        //APICalls = new CallDic();
        CodeStats = new LinkedHashMap<String, Integer>();
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
            if (treetuple.Item2.PossibleExceptionsBlockList != null)
            {
            	PossibleExceptionsBlocks.Add(treetuple.Item2.PossibleExceptionsBlockList);
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
        
        if (CatchBlocks.size() > 0)
        {
        	CodeStats.put("NumBinded",CatchBlocks.NumBinded);
            CodeStats.put("NumMethodsNotBinded",CatchBlocks.NumMethodsNotBinded);
            CodeStats.put("NumLoggedCatchBlock",CatchBlocks.NumLogged);
            CodeStats.put("NumDistinctExceptionTypeCatch",CatchBlocks.size());
            CodeStats.put("NumRecoveredBinding",CatchBlocks.NumRecoveredBinding);
            
        }
        if (ThrowsBlocks.size() > 0)
        {
        	CodeStats.put("NumDistinctExceptionTypeThrows",ThrowsBlocks.size());
        }
        if (PossibleExceptionsBlocks.size() > 0)
        {
        	CodeStats.put("NumDistinctPossibleExceptions",PossibleExceptionsBlocks.size());
        }
            
        
        //CodeStats["NumCallType"] = APICalls.Count;
        //CodeStats["NumAPICall"] = APICalls.NumAPICall;
        //CodeStats["NumLoggedAPICall"] = APICalls.NumLogged;
    }

    public void PrintStatistics()
    {           
        
    	String header = "";
    	String content = "";

        for (Map.Entry<String, Integer> stat : CodeStats.entrySet())
        {
            header += stat.getKey() + "\t";
            content += stat.getValue() + "\t";

            //Logger.Log(stat + ": " + CodeStats[stat]);
        }

        logger.info(header);
        logger.info(content);
        
        if (CatchBlocks.size() > 0)
        {
        	CatchBlocks.PrintToFileCSV();
        }
        
        if (ThrowsBlocks.size() > 0)
        {
        	ThrowsBlocks.PrintToFileCSV();
        }
        
        if (PossibleExceptionsBlocks.size() > 0)
        {
        	PossibleExceptionsBlocks.PrintToFileCSV();
        }
        
        //APICalls.PrintToFile();
    }
}
