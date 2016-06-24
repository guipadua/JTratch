package ca.concordia.jtratch;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

import ca.concordia.jtratch.pattern.CodeStatistics;
import ca.concordia.jtratch.pattern.TreeStatistics;
import ca.concordia.jtratch.utility.Tuple;
import ca.concordia.jtratch.visitors.CatchVisitor;

final class CodeAnalyzer {

	private static final Logger logger = LogManager.getLogger(CodeAnalyzer.class.getName());
	
	public static void AnalyzeAllTrees(String[] sourceFilePaths)
        {
            
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		
		//set environment assumtion - this being run by folder, and each folder is one single project
		//to run multiple projects, required extra loop basically
		//classpath empty - maybe will be required if desired to combine binary calls together
		//using default encodings
		
		parser.setEnvironment(null, sourceFilePaths, null, false);
		
		//this has to be activated for using bindings, however the setEnvironment above will need the classpath array to work
		parser.setResolveBindings(false); 
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);	
		
		final String[] emptyArray = new String[0];
		
		List<Tuple<CompilationUnit, TreeStatistics>> codeStatsList = new ArrayList<Tuple<CompilationUnit, TreeStatistics>>();
				
		FileASTRequestor fileASTRequestor = new FileASTRequestor() { 
			@Override
			public void acceptAST(String sourceFilePath, CompilationUnit ast) {
				logger.trace("Processing AST for File:" + sourceFilePath);
				TreeStatistics stats = new TreeStatistics();
				CatchVisitor catchVisitor = new CatchVisitor();
				//parser.
				stats.CodeStats.put("NumLOC", ast.toString().split(System.getProperty("line.separator")).length);
				logger.debug("NumLOC: " + stats.CodeStats.get("NumLOC"));
				
				catchVisitor.setTree(ast);
				catchVisitor.setFilePath(sourceFilePath);
				ast.accept(catchVisitor);
				
				stats.CodeStats.put("NumCatchBlock", catchVisitor.getCatchBlockList().size());
				stats.CatchBlockList = catchVisitor.getCatchBlockList();
				
				//logger.debug("List of catches: "+ catchVisitor.getCatches().toString());
				
				codeStatsList.add(new Tuple<CompilationUnit, TreeStatistics>(ast,stats));
	    	}
				
		};
		
		parser.createASTs(sourceFilePaths, null, emptyArray, fileASTRequestor, null);
		
		// statistics
        //int numFiles = treeAndModelDic.Count;
        //var treeNode = treeAndModelDic.Keys
         //   .Select(tree => tree.GetRoot().DescendantNodes().Count());
	
		CodeStatistics allStats = new CodeStatistics(codeStatsList);
		
		// Log statistics
        //Logger.Log("Num of syntax nodes: " + treeNode.Sum());
        //Logger.Log("Num of source files: " + numFiles);
        allStats.PrintSatistics();
        
        }
	
	public static void AnalyzeAllTreesAndComments(String[] sourceFilePaths)
    {
        
//		for (Comment comment : (List<Comment>) ast.getCommentList()) {
//			CommentVisitor commentVisitor = new CommentVisitor();
//		    commentVisitor.setTree(ast);
//		    comment.accept(commentVisitor);
//		}
//	    //ToDo
//	    if (!commentVisitor.getToDoComments().isEmpty())
//	    {
//	    	//catchBlockInfo.OperationFeatures.put("ToDo", 1);
//	    }
	    
    }

}
