package kr_ac_yonsei_mobilesw_UI;

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

}
