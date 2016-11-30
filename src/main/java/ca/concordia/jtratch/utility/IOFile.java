package ca.concordia.jtratch.utility;

public class IOFile {
	public static String InputFolderPath;
	public static String OutputFolderPath;
	public static String MavenRepo;
    
    public static String CompleteFileNameInput(String tail)
    {
        String [] folderPath;
        folderPath = InputFolderPath.split("/");
    	return (InputFolderPath + "/" + folderPath[folderPath.length - 1] + "_" + tail);
    }
    public static String CompleteFileNameOutput(String tail)
    {
        String [] folderPath;
        folderPath = InputFolderPath.split("/");
    	return (OutputFolderPath + "/" + folderPath[folderPath.length - 1] + "_" + tail);
    }
    static public String DeleteSpace(String str)
    {
        if (str == null || str == "") return str;

        String updatedStr = str.replace("\n", "").replace("\r", "").replace("\t", "")
        .replace("    ", " ").replace("    ", " ").replace("   ", " ")
        .replace("  ", " ");

        return updatedStr;
    }

//    static public String MethodNameExtraction(String str)
//    {
//        try
//        {
//            String methodName = str;
//            try
//            {                  
//                methodName = Regex.Replace(methodName, "<.*>", "");
//                methodName = Regex.Replace(methodName, "{.*}", "");
//                methodName = Regex.Replace(methodName, "\\(.*\\)", "");
//                if (methodName.IndexOf('(') != -1)
//                {
//                    methodName = methodName.Split('(').First();
//                }
//                methodName = DeleteSpace(methodName);
//                methodName = methodName.Replace(" ", "");
//            }
//            catch { }
//            return methodName;
//        }
//        catch
//        {
//            return null;
//        }
//    }
//
//    static public String ShortMethodNameExtraction(String str)
//    {
//        try
//        {
//            String methodName = null;
//            MatchCollection allMatches = Regex.Matches(str, "\\.[a-zA-Z0-9\\s]+\\(");
//            if (allMatches.Count > 1)
//            {
//                methodName = Regex.Replace(allMatches[allMatches.Count - 1].ToString(), "[\\.(\\s]", "");
//            }
//            else
//            {
//                methodName = MethodNameExtraction(str);
//            }
//            if (methodName.IndexOf('.') != -1)
//            {
//                methodName = methodName.Split('.').Last();
//            }
//            else if (methodName == null)
//            {
//                Logger.Log("An API call cannot be extracted by the ShortMethodNameExtraction function:\n" + str);
//            }
//
//            return methodName;
//        }
//        catch
//        {
//            return null;
//        }
//    }
}
