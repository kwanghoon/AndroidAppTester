package kr_ac_yonsei_mobilesw_UI;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Scanner;

public class JavaCommand {
	
	private static String classpath;
	
	static {
		String osName = System.getProperty("os.name");
		String osNameMatch = osName.toLowerCase();
		String userDir = System.getProperty("user.dir");
		
		if(osNameMatch.contains("linux")) {
			classpath = userDir + "/bin:" + userDir + "/lib/*:" + userDir + "/tool/*";
		} else if(osNameMatch.contains("windows")) {
			classpath = userDir + "/bin;" + userDir + "/lib/*;" + userDir + "/tool/*";
		} else if(osNameMatch.contains("mac os") || osNameMatch.contains("macos") || osNameMatch.contains("darwin")) {
			classpath = userDir + "/bin:" + userDir + "/lib/*:" + userDir + "/tool/*";
		}else {
			classpath = userDir + "/bin;" + userDir + "/lib/*;" + userDir + "/tool/*"; // Windows OS by default
		}	
	}
	
	public static String javaCmd() {
		return "java -cp \"" + classpath +"\" ";
	}
	
	//
	public static String buildIntentSpecParam(String filename, String spec) {
		File param_is = new File(filename);
		String currentWorkingDir = param_is.getAbsolutePath();
		
		try {
			FileWriter fw = new FileWriter(param_is);
			Scanner scan = new Scanner(new StringReader(spec));
			while (scan.hasNextLine()) {
				String line = scan.nextLine();
				fw.write(line + "\n");
			}
			fw.close();
			scan.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return currentWorkingDir;
	}

}
