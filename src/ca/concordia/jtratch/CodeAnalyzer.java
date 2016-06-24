package ca.concordia.jtratch;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

import ca.concordia.jtratch.pattern.CatchBlock;
import ca.concordia.jtratch.pattern.CodeStatistics;
import ca.concordia.jtratch.pattern.TreeStatistics;
import ca.concordia.jtratch.utility.IOFile;
import ca.concordia.jtratch.utility.Tuple;
import ca.concordia.jtratch.visitors.CatchVisitor;
import ca.concordia.jtratch.visitors.CommentVisitor;

final class CodeAnalyzer {

	private static final Logger logger = LogManager.getLogger(CodeAnalyzer.class.getName());
	
	public static void AnalyzeAllTrees(List<String> sourceFilePathList )
	{
		logger.trace("Running AnalyzeAllTrees.");
		
		String[] sourceFilePaths = sourceFilePathList.toArray(new String [] {});
		
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
	
	public static void AnalyzeAllTreesAndComments(List<String> sourceFilePathList )
	{
		logger.trace("Running AnalyzeAllTreesAndComments.");
		
		List<Tuple<CompilationUnit, TreeStatistics>> codeStatsList = new ArrayList<Tuple<CompilationUnit, TreeStatistics>>();
		
		//TODO: change to parallel stream to make it faster
		sourceFilePathList.stream().forEach(sourceFilePath -> codeStatsList.add(AnalyzeATreeAndComments(sourceFilePath)));
		
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
	
	public static Tuple<CompilationUnit, TreeStatistics> AnalyzeATreeAndComments(String sourceFilePath)
    {
		logger.trace("Processing AST for File:" + sourceFilePath);
		
		Tuple<CompilationUnit, TreeStatistics> codeStatsTuple = new Tuple<CompilationUnit, TreeStatistics>(null, null) ;
		
		Charset charset = Charset.forName("UTF-8");
    	ASTParser parser = ASTParser.newParser(AST.JLS8);
    	StringBuilder fileData = new StringBuilder();
    	
    	try {
			Files.lines(Paths.get(sourceFilePath), charset)
					.forEachOrdered(line -> fileData.append(line + System.getProperty("line.separator")));
			
			parser.setSource(fileData.toString().toCharArray());
			parser.setResolveBindings(false); 
			parser.setStatementsRecovery(true);
			parser.setBindingsRecovery(true);	
			
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			
			final CompilationUnit ast = (CompilationUnit) parser.createAST(null);
			
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
			
			for ( CatchBlock catchblock : stats.CatchBlockList)
			{
				int start = catchblock.OperationFeatures.get("CatchStart");
				int end = start + catchblock.OperationFeatures.get("CatchLength");
				
				String catchString = fileData.substring(start, end);
				if (!catchString.isEmpty())
				{
					//TODO can't guarantee this is really a comment block.
					if (catchString.toLowerCase().contains("todo") || catchString.toLowerCase().contains("fixme")){
						catchblock.OperationFeatures.put("ToDo", 1);
					}
				}
				
			}
			
			//logger.debug("List of catches: "+ catchVisitor.getCatches().toString());
			codeStatsTuple = new Tuple<CompilationUnit, TreeStatistics>(ast,stats);
			
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Illegal Configure File Format.");
		} finally {
			logger.trace("Single Tree read successfully.");
		}
				
		return codeStatsTuple;

    }
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
