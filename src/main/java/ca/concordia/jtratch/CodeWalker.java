package ca.concordia.jtratch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
//Import log4j classes.
import org.apache.logging.log4j.Logger;

public class CodeWalker {

	private static final Logger logger = LogManager.getLogger(CodeWalker.class.getName());
	
	public void LoadByInputMode(String inputMode, String filePath) throws IOException
    {
        logger.trace("Input mode: " + inputMode);
        switch (inputMode)
        {
            case "ByFolder":
            	logger.trace("Before javadoc xml load");
            	CodeAnalyzer.getJavadocFromExternalXML();
            	logger.trace("After javadoc xml load");
            	
            	logger.trace("Before method 1 - createASTs");
                CodeAnalyzer.AnalyzeAllTrees(LoadByFolder(filePath));
                logger.trace("After method 1 - createASTs");
                
                logger.trace("------------------------------------------------------");
                                
//                logger.trace("Before method 2 - createAST");
//                CodeAnalyzer.AnalyzeAllTreesAndComments(LoadByFolder(filePath));
//                logger.trace("After method 2 - createAST");
                
        		break;
            case "ByTxtFile":
                LoadByTxtFile(filePath);
                break;
            default:
                logger.error("Invalid input mode. (Select ByFolder/ByTxtFile)");
                System.in.read();
                return;
        }
    }

    public static List<String> LoadByFolder(String folderPath)
    {
    	logger.info("Loading from folder: " + folderPath);
        
    	List<String> sourceFilePaths = new ArrayList<String>();
    	
    		
			try {
				sourceFilePaths = Files	.walk(Paths.get(folderPath))
										.map(String::valueOf)
										.filter(line -> !line.contains("/test/"))
										.filter(line -> !line.contains("/tests/"))
										.filter(line -> !line.contains(".tests"))
										.filter(line -> line.endsWith(".java"))
										.collect(Collectors.toList());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    		logger.info("Loading " + sourceFilePaths.size() + " *.java files.");
            
    		    		
	
    	
    	return sourceFilePaths;
    	
    	/*
    	logger.info("Loading from folder: " + folderPath);
        IEnumerable<String> FileNames = Directory.EnumerateFiles(folderPath, "*.cs",
            SearchOption.AllDirectories);
        int numFiles = FileNames.Count();
        logger.info("Loading " + numFiles + " *.cs files.");
        // parallelization
        var treeAndModelList = FileNames.AsParallel()
            .Select(fileName => LoadSourceFile(fileName))
            .ToList();

        var treeAndModelDic = new Dictionary<SyntaxTree, SemanticModel>();
        foreach (var treeAndModel in treeAndModelList)
        {
            treeAndModelDic.Add(treeAndModel.Item1, treeAndModel.Item2);
        }
        var compilation = BuildCompilation(treeAndModelDic.Keys.ToList());

        CodeAnalyzer.AnalyzeAllTrees(treeAndModelDic, compilation);
        */
    }
    
   
    public static void LoadByTxtFile(String folderPath)
    {
    	logger.error("LoadByTxtFile not implemented!");
    }
}
