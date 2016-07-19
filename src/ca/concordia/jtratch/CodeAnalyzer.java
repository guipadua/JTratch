package ca.concordia.jtratch;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

import ca.concordia.jtratch.pattern.CatchBlock;
import ca.concordia.jtratch.pattern.CodeStatistics;
import ca.concordia.jtratch.pattern.TreeStatistics;
import ca.concordia.jtratch.utility.IOFile;
import ca.concordia.jtratch.utility.Tuple;
import ca.concordia.jtratch.visitors.CatchVisitor;
import ca.concordia.jtratch.visitors.MethodDeclarationVisitor;

final class CodeAnalyzer {

	private static final Logger logger = LogManager.getLogger(CodeAnalyzer.class.getName());
	
	public static void AnalyzeAllTrees(List<String> sourceFilePathList )
	{
		logger.trace("Running AnalyzeAllTrees.");
		
		String[] sourceFilePaths = sourceFilePathList.toArray(new String [] {});
		String[] sourceFolder = { IOFile.FolderPath }; 
		
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		
		//set environment assumtion - this being run by folder, and each folder is one single project
		//to run multiple projects, required extra loop basically
		//classpath empty - maybe will be required if desired to combine binary calls together
		//using default encodings
		
		parser.setResolveBindings(true); 
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		parser.setBindingsRecovery(true);
		//parser.setStatementsRecovery(true);
		
		Map options = JavaCore.getOptions();
		parser.setCompilerOptions(options);
		
		String unitName = "bogus_unit_name";
		parser.setUnitName(unitName);
 
		parser.setEnvironment(getClassJarList(), sourceFolder, null, true);
		
		final String[] emptyArray = new String[0];
		
		List<Tuple<CompilationUnit, TreeStatistics>> codeStatsList = new ArrayList<Tuple<CompilationUnit, TreeStatistics>>();
				
		FileASTRequestor fileASTRequestor = new FileASTRequestor() { 
			@Override
			public void acceptAST(String sourceFilePath, CompilationUnit ast) {
				
				codeStatsList.add(processCompilation(ast, sourceFilePath, new StringBuilder()));
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
		
		sourceFilePathList.parallelStream().forEach(sourceFilePath -> codeStatsList.add(AnalyzeATreeAndComments(sourceFilePath)));
		
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
		Charset charset = Charset.forName("UTF-8");
    	StringBuilder fileData = new StringBuilder();
    	CompilationUnit ast = null;
    	
		try {
			Files.lines(Paths.get(sourceFilePath), charset)
					.forEachOrdered(line -> fileData.append(line + System.getProperty("line.separator")));
			ast = getCUFromPath(sourceFilePath, fileData.toString().toCharArray());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return processCompilation(ast, sourceFilePath, fileData);

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

	private static Tuple<CompilationUnit, TreeStatistics> processCompilation(CompilationUnit ast, String sourceFilePath, StringBuilder fileData) {
		logger.info("Processing AST for File:" + sourceFilePath);
		logger.info("Is Binding Recovery Activated? : " + ast.getAST().hasBindingsRecovery());
		
		TreeStatistics stats = new TreeStatistics();
		stats.CodeStats.put("NumLOC", ast.toString().split(System.getProperty("line.separator")).length);
		logger.info("NumLOC: " + stats.CodeStats.get("NumLOC"));
		
		/*
		 * Visiting Catch blocks (and its parent try from the inside)
		 */
		CatchVisitor catchVisitor = new CatchVisitor();
		catchVisitor.setTree(ast);
		catchVisitor.setFilePath(sourceFilePath);
		ast.accept(catchVisitor);
		
		stats.CodeStats.put("NumCatchBlock", catchVisitor.getCatchBlockList().size());
		stats.CatchBlockList = catchVisitor.getCatchBlockList();
		
		logger.info("Catch blocks visited successfully - number of blocks on this file is:" + stats.CatchBlockList.size() );
			
		/*
		 * Based on the catch list, finding if there are todo, fixme inside them
		 */
		
		if (fileData.length() == 0)
		{
			Charset charset = Charset.forName("UTF-8");
	    	
			try {
				Files.lines(Paths.get(sourceFilePath), charset)
						.forEachOrdered(line -> fileData.append(line + System.getProperty("line.separator")));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		for ( CatchBlock catchblock : stats.CatchBlockList)
		{
			int start = catchblock.OperationFeatures.get("Start");
			int end = start + catchblock.OperationFeatures.get("Length");
			
			String catchString = fileData.substring(start, end);
			
			catchblock.MetaInfo.put("CatchBlock", catchString );
			
			if (!catchString.isEmpty())
			{
				//TODO can't guarantee this is really a comment block.
				if (catchString.toLowerCase().contains("todo") || catchString.toLowerCase().contains("fixme")){
					catchblock.OperationFeatures.put("ToDo", 1);
				}
			}
		}
		
		/*
		 * //Visiting method declarations for throw analysis
		 */
		MethodDeclarationVisitor methodDeclarationVisitor = new MethodDeclarationVisitor();
		methodDeclarationVisitor.setTree(ast);
		methodDeclarationVisitor.setFilePath(sourceFilePath);
		ast.accept(methodDeclarationVisitor);
		
		stats.CodeStats.put("NumThrowsBlock", methodDeclarationVisitor.getThrowsBlockList().size());
		stats.ThrowsBlockList = methodDeclarationVisitor.getThrowsBlockList();
		
		logger.info("Throws blocks visited successfully - number of blocks on this file is: " + stats.ThrowsBlockList.size() );
		
		
		logger.info("Single Tree read successfully.");
		return new Tuple<CompilationUnit, TreeStatistics>(ast,stats);
		
	}

	private static CompilationUnit getCUFromPath(String sourceFilePath, char[] fileCharData) throws IOException {
		
		ASTParser parser = ASTParser.newParser(AST.JLS8);
    	
		parser.setResolveBindings(true); 
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		parser.setBindingsRecovery(true);
		//parser.setStatementsRecovery(true);
		
		Map options = JavaCore.getOptions();
		parser.setCompilerOptions(options);
		
		String unitName = Paths.get(sourceFilePath).getFileName().toString();
		parser.setUnitName(unitName);
 
		String[] sourceFolder = { IOFile.FolderPath }; 
		
		parser.setEnvironment(getClassJarList(), sourceFolder, null, true);
		parser.setSource(fileCharData);
		
		return (CompilationUnit) parser.createAST(null);
		
	}
	
	private static String[] getClassJarList(){
		
		List<String> classFileJarsList = new ArrayList<String>();
		
		try {
			classFileJarsList = Files	.walk(Paths.get(IOFile.FolderPath))
										.map(String::valueOf)
										.filter(line -> line.endsWith(".jar"))
										.collect(Collectors.toList());
			logger.info("Class list with own jars only: " + classFileJarsList.size());
			
//			//Obtain the list of JARs from the local JVM - NOT NEEDED if setEnvironment parameter to include current VM is TRUE.
//			classFileJarsList.addAll(Files	.walk(Paths.get("/Library/Java/JavaVirtualMachines/jdk1.8.0_92.jdk/Contents/Home/jre/lib"))
//										.map(String::valueOf)
//										.filter(line -> line.endsWith(".jar"))
//										.collect(Collectors.toList()));
//			logger.info("Class list with JRE lib: " + classFileJarsList.size());
//			
			File mavenRepo = new File(IOFile.MavenRepo);
			
			if (mavenRepo.exists())
			{
				//TODO: this is getting the whole local maven repository. It could actually be done using the classpath file to only use the proper classes.
				classFileJarsList.addAll(Files	.walk(mavenRepo.toPath())
												.map(String::valueOf)
												.filter(line -> line.endsWith(".jar"))
												.collect(Collectors.toList()));
				logger.info("Class list with M2_REPO: " + classFileJarsList.size());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
			
		String[] classFileJars = classFileJarsList.toArray(new String [0]);// toArray(new String [] {});
		
		return classFileJars;
		
	}
	    
}
