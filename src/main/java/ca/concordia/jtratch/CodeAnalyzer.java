package ca.concordia.jtratch;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import ca.concordia.jpexjd.MyJAXB;
import ca.concordia.jpexjd.MyRoot;
import ca.concordia.jtratch.pattern.CatchBlock;
import ca.concordia.jtratch.pattern.CodeStatistics;
import ca.concordia.jtratch.pattern.TreeStatistics;
import ca.concordia.jtratch.utility.ASTUtilities;
import ca.concordia.jtratch.utility.Dic;
import ca.concordia.jtratch.utility.IOFile;
import ca.concordia.jtratch.utility.Tuple;
import ca.concordia.jtratch.visitors.CatchVisitor;
import ca.concordia.jtratch.visitors.MethodDeclarationVisitor;

public final class CodeAnalyzer {

	private static final Logger logger = LogManager.getLogger(CodeAnalyzer.class.getName());

	public static HashMap<String, MyMethod> AllMyMethods = new HashMap<String, MyMethod>();
	
	//InvokedMethods:
	//Store the methods that were invoked at least once during evaluation.
	// Each method will be stored only once and will contain a list of possible exceptions (ExceptionFlow - open exception)
	public static HashMap<String, InvokedMethod> InvokedMethods = new HashMap<String, InvokedMethod>();
	
	public static MyRoot externalJavadoc = new MyRoot();
	
	public static void AnalyzeAllTrees(List<String> sourceFilePathList )
	{
		logger.trace("Running AnalyzeAllTrees.");
		
		String[] sourceFilePaths = sourceFilePathList.toArray(new String [] {});
		String[] sourceFolder = { IOFile.InputFolderPath }; 
		
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		Map options = JavaCore.getOptions();		
		
		String unitName = "bogus_unit_name";
		final String[] emptyArray = new String[0];
		String[] classJarList = getClassJarList();
		
		//set environment assumtion - this being run by folder, and each folder is one single project
		//to run multiple projects, required extra loop basically
		//classpath empty - maybe will be required if desired to combine binary calls together
		//using default encodings
		
		//start as resolve binding false - very costly and not required for getting method declarations
		parser.setResolveBindings(true); 
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setBindingsRecovery(true);
		parser.setCompilerOptions(options);
		parser.setUnitName(unitName); 
		parser.setEnvironment(classJarList, sourceFolder, null, true);
				
		List<Tuple<CompilationUnit, TreeStatistics>> codeStatsFromMethodsList = new ArrayList<Tuple<CompilationUnit, TreeStatistics>>();
		List<HashMap<String, MyMethod>> allMethodDeclarations = new ArrayList<HashMap<String, MyMethod>>();
		
		FileASTRequestor fileASTRequestorDeclarations = new FileASTRequestor() { 
			@Override
			public void acceptAST(String sourceFilePath, CompilationUnit ast) {
				
				logger.warn("Problems found in this AST: " + printProblems(ast.getProblems()) + "." );
				
				Tuple<CompilationUnit, TreeStatistics> codeStats = getAllMethodDeclarations(ast, sourceFilePath);
				HashMap<String, MyMethod> astMethodDeclarations = new HashMap<String, MyMethod>();
				
				codeStatsFromMethodsList.add(codeStats);
				
				for ( MethodDeclaration method : codeStats.Item2.MethodDeclarationList)
				{
					String methodDeclaration = ASTUtilities.getMethodName(method);
					//method.
					if (methodDeclaration != null && !astMethodDeclarations.containsKey(methodDeclaration))
                    {
						astMethodDeclarations.put(methodDeclaration, new MyMethod(methodDeclaration, method));
                    }
				}				
				allMethodDeclarations.add(astMethodDeclarations);
			}			
		};
		
		logger.info("Parser settings Ready - Method Declarations Processing starting...");
		//parse first to collect all method declarations - this is the first implementation approach (another possibility is to use: CompilationUnit.findDeclaringNode
		parser.createASTs(sourceFilePaths, null, emptyArray, fileASTRequestorDeclarations, null);
		
		for ( HashMap<String, MyMethod> methoddeclar : allMethodDeclarations)
		{
			Dic.MergeDic2( AllMyMethods, methoddeclar);
		}
		
		logger.info("Cached all method declarations: " + AllMyMethods.size());
		
		
		CodeStatistics allStatsFromMethods = new CodeStatistics(codeStatsFromMethodsList);
		// Log statistics
        //Logger.Log("Num of syntax nodes: " + treeNode.Sum());
        //Logger.Log("Num of source files: " + numFiles);
		allStatsFromMethods.PrintStatistics();     
		
		//resolve binding true - required for type comparison
        parser.setResolveBindings(true); 
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setBindingsRecovery(true);
		parser.setCompilerOptions(options);
		parser.setUnitName(unitName); 
		parser.setEnvironment(classJarList, sourceFolder, null, true);
		
		List<Tuple<CompilationUnit, TreeStatistics>> codeStatsFromCatchsList = new ArrayList<Tuple<CompilationUnit, TreeStatistics>>();
		
		FileASTRequestor fileASTRequestorStats = new FileASTRequestor() { 
			@Override
			public void acceptAST(String sourceFilePath, CompilationUnit ast) {
				codeStatsFromCatchsList.add(processCompilation(ast, sourceFilePath, new StringBuilder()));
			}	
		};
		
		logger.info("Parser settings Ready - Catch Blocks Processing starting...");
		parser.createASTs(sourceFilePaths, null, emptyArray, fileASTRequestorStats, null);
		// statistics
        //int numFiles = treeAndModelDic.Count;
        //var treeNode = treeAndModelDic.Keys
         //   .Select(tree => tree.GetRoot().DescendantNodes().Count());
		CodeStatistics allStatsFromCatchs = new CodeStatistics(codeStatsFromCatchsList);
		allStatsFromCatchs.CodeStats.put("NumFiles", sourceFilePathList.size());
		allStatsFromCatchs.CodeStats.put("NumDeclaredMethods", AllMyMethods.size());
		allStatsFromCatchs.CodeStats.put("NumInvokedMethods", InvokedMethods.size());
		allStatsFromCatchs.CodeStats.put("NumInvokedMethodsBinded", (int) InvokedMethods.values().stream().filter( method -> method.isBinded()).count());
		allStatsFromCatchs.CodeStats.put("NumInvokedMethodsDeclared", (int) InvokedMethods.values().stream().filter( method -> method.isDeclared()).count());
		allStatsFromCatchs.CodeStats.put("NumInvokedMethodsExtDocPresent", (int) InvokedMethods.values().stream().filter( method -> method.isExternalJavadocPresent()).count());
		// Log statistics
        //Logger.Log("Num of syntax nodes: " + treeNode.Sum());
        //Logger.Log("Num of source files: " + numFiles);
		allStatsFromCatchs.PrintStatistics();       
	}

	public static void AnalyzeAllTreesAndComments(List<String> sourceFilePathList) {
		logger.trace("Running AnalyzeAllTreesAndComments.");

		List<Tuple<CompilationUnit, TreeStatistics>> codeStatsList = new ArrayList<Tuple<CompilationUnit, TreeStatistics>>();

		sourceFilePathList.parallelStream()
				.forEach(sourceFilePath -> codeStatsList.add(AnalyzeATreeAndComments(sourceFilePath)));

		// statistics
		// int numFiles = treeAndModelDic.Count;
		// var treeNode = treeAndModelDic.Keys
		// .Select(tree => tree.GetRoot().DescendantNodes().Count());

		CodeStatistics allStats = new CodeStatistics(codeStatsList);

		// Log statistics
		// Logger.Log("Num of syntax nodes: " + treeNode.Sum());
		// Logger.Log("Num of source files: " + numFiles);
		allStats.PrintStatistics();

	}

	public static Tuple<CompilationUnit, TreeStatistics> AnalyzeATreeAndComments(String sourceFilePath) {
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

	// for (Comment comment : (List<Comment>) ast.getCommentList()) {
	// CommentVisitor commentVisitor = new CommentVisitor();
	// commentVisitor.setTree(ast);
	// comment.accept(commentVisitor);
	// }
	// //ToDo
	// if (!commentVisitor.getToDoComments().isEmpty())
	// {
	// //catchBlockInfo.OperationFeatures.put("ToDo", 1);
	// }
	private static Tuple<CompilationUnit, TreeStatistics> getAllMethodDeclarations(CompilationUnit ast,
			String sourceFilePath) {

		TreeStatistics stats = new TreeStatistics();

		/*
		 * //Visiting method declarations for storage and throw analysis
		 */
		MethodDeclarationVisitor methodDeclarationVisitor = new MethodDeclarationVisitor();
		methodDeclarationVisitor.setTree(ast);
		methodDeclarationVisitor.setFilePath(sourceFilePath);
		ast.accept(methodDeclarationVisitor);

		stats.MethodDeclarationList = methodDeclarationVisitor.getMethodDeclarationList();
		logger.info("Method Declarations visited successfully - number of methods on this file is: "
				+ stats.MethodDeclarationList.size());

		stats.CodeStats.put("NumThrowsBlock", methodDeclarationVisitor.getThrowsBlockList().size());
		stats.ThrowsBlockList = methodDeclarationVisitor.getThrowsBlockList();
		logger.info("Throws blocks collected - number of blocks on this file is: " + stats.ThrowsBlockList.size());

		return new Tuple<CompilationUnit, TreeStatistics>(ast, stats);

	}

	private static Tuple<CompilationUnit, TreeStatistics> processCompilation(CompilationUnit ast, String sourceFilePath,
			StringBuilder fileData) {
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
		
		stats.CodeStats.put("NumPossibleExceptionBlock", catchVisitor.getPossibleExceptionsList().size());
		stats.PossibleExceptionsBlockList = catchVisitor.getPossibleExceptionsList();

		logger.info(
				"Catch blocks visited successfully - number of blocks on this file is:" + stats.CatchBlockList.size());

		/*
		 * Based on the catch list, finding if there are todo, fixme inside them
		 */

		if (fileData.length() == 0) {
			Charset charset = Charset.forName("UTF-8");

			try {
				Files.lines(Paths.get(sourceFilePath), charset)
						.forEachOrdered(line -> fileData.append(line + System.getProperty("line.separator")));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		for (CatchBlock catchblock : stats.CatchBlockList) {
			int start = catchblock.OperationFeatures.get("CatchStart");
			int end = start + catchblock.OperationFeatures.get("CatchLength");

			String catchString = fileData.substring(start, end);

			catchblock.MetaInfo.put("CatchBlock", catchString);

			if (!catchString.isEmpty()) {
				// TODO can't guarantee this is really a comment block.
				if (catchString.toLowerCase().contains("todo") || catchString.toLowerCase().contains("fixme")) {
					catchblock.OperationFeatures.put("ToDo", 1);
				}
			}
		}
		
		logger.info("Single Tree read successfully.");
		return new Tuple<CompilationUnit, TreeStatistics>(ast, stats);

	}
	
	private static CompilationUnit getCUFromPath(String sourceFilePath, char[] fileCharData) throws IOException {

		ASTParser parser = ASTParser.newParser(AST.JLS8);

		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		parser.setBindingsRecovery(true);
		// parser.setStatementsRecovery(true);

		Map options = JavaCore.getOptions();
		parser.setCompilerOptions(options);

		String unitName = Paths.get(sourceFilePath).getFileName().toString();
		parser.setUnitName(unitName);

		String[] sourceFolder = { IOFile.InputFolderPath };

		parser.setEnvironment(getClassJarList(), sourceFolder, null, true);
		parser.setSource(fileCharData);

		return (CompilationUnit) parser.createAST(null);

	}

	private static String[] getClassJarList() {

		List<String> classFileJarsList = new ArrayList<String>();

		try {
			classFileJarsList = Files.walk(Paths.get(IOFile.InputFolderPath)).map(String::valueOf)
					.filter(line -> line.endsWith(".jar")).collect(Collectors.toList());
			logger.info("Class list with own jars only: " + classFileJarsList.size());

//			// //Obtain the list of JARs from the local JVM - NOT NEEDED if
//			// setEnvironment parameter to include current VM is TRUE.
//			 classFileJarsList.addAll(Files
//			 .walk(Paths.get("/Library/Java/JavaVirtualMachines/jdk1.8.0_92.jdk/Contents/Home/jre/lib"))
//			 .map(String::valueOf)
//			 .filter(line -> line.endsWith(".jar"))
//			 .collect(Collectors.toList()));
//			 logger.info("Class list with JRE lib: " +
//			 classFileJarsList.size());
			
			File mavenRepo = new File(IOFile.MavenRepo);

			if (mavenRepo.exists()) {
				// TODO: this is getting the whole local maven repository. It
				// could actually be done using the classpath file to only use
				// the proper classes.
				classFileJarsList.addAll(Files.walk(mavenRepo.toPath()).map(String::valueOf)
						.filter(line -> line.endsWith(".jar")).collect(Collectors.toList()));
				logger.info("Class list with M2_REPO: " + classFileJarsList.size());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String[] classFileJars = classFileJarsList.toArray(new String[0]);// toArray(new
																			// String
																			// []
																			// {});

		return classFileJars;

	}
	
	static void getJavadocFromExternalXML()
	{
		String jreXMLFilePath = "/Users/gbp/javadoc/javadoc.xml";
		//String jreXMLFilePath = "/Users/gbp/javadoc/filesys.xml";		
		
		//JAXB
		MyJAXB myJAXB = null;
		try {
			myJAXB = new MyJAXB(jreXMLFilePath);
			externalJavadoc = myJAXB.getMyRoot();
		} catch (JAXBException e) {
			logger.error("External javadoc failure: " + e.getMessage());
		}		
	}
	
	private static String printProblems(IProblem[] iproblems) {
		StringBuilder problems = new StringBuilder();
		
		if(iproblems.length > 0 ){
			for(IProblem problem : iproblems) {
				problems.append("Message:" + problem.getMessage() + 
								" at: " + new String(problem.getOriginatingFileName()) + 
								" " + problem.getSourceLineNumber() + "" +
								" Error:" + (problem.isError() ? "Y" : "N"));
			}						
		}
		
		return problems.toString();
	}

}
