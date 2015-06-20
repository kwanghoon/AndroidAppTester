package kr_ac_yonsei_mobilesw_UI.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Scanner;

import kr_ac_yonsei_mobilesw_UI.InterfaceWithExecution;
import kr_ac_yonsei_mobilesw_parser.MalformedIntentException;
import kr_ac_yonsei_mobilesw_parser.ParserOnIntent;
import kr_ac_yonsei_mobilesw_shell.ExecuteShellCommand;

import org.junit.Test;

public class AdbCommandGenerationTest {
	private static String osNameStr="windows";
	private static String genCommand="gen.exe";
	
	static {
		String osName = System.getProperty("os.name");
		String osNameMatch = osName.toLowerCase();
		
		if(osNameMatch.contains("linux")) {
			osNameStr = "linux";
			genCommand = "gen_linux";
		} else if(osNameMatch.contains("windows")) {
			osNameStr = "windows";
			genCommand = "gen.exe";
		} else if(osNameMatch.contains("mac os") || osNameMatch.contains("macos") || osNameMatch.contains("darwin")) {
			osNameStr = "mac";
			genCommand = "gen_mac";
		}else {
			osNameStr = "windows"; // Windows OS by default
			genCommand = "gen.exe"; // Windows OS by default
		}	
	}

	// Put APK files or AndroidManifest.xml files into the following directory.
	private static String pathToTestAPKorAndroidManifestFiles = 
			"./src/kr_ac_yonsei_mobilesw_UI/test/files/";
	
	private boolean stop = false;
	
	// Adb Command Generation Test
	@Test
	public void test() {
		File file = new File(pathToTestAPKorAndroidManifestFiles);
		
		File[] files = file.listFiles();
		
		System.out.println("Total " + files.length + " files");
		
		
		
		if (files!=null) {
			try {
				int count = 0;
				for (File subfile : files) {
					count = count + 1;
					if (stop) {
						fail("Something wrong in Adb command generation.");
						break;
					}
					
					String command = "java -cp \"" 
							+ System.getProperty("user.dir") + "/bin;" 
							+ System.getProperty("user.dir") + "/lib/*\" " 
							+ "com.example.java.GenIntentSpecFromAPK " 
							+ "\"" + subfile.getAbsolutePath() + "\""; // ' ' in the file name
					
					System.out.println(command);
					
					Process p;
					
					if ("mac".equals(osNameStr)) {
						p = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", command });
					}
					else {
						p = Runtime.getRuntime().exec(command);
					}
					
					// Stdout Messages
					StringBuffer sb = new StringBuffer();
					
					while(p.isAlive())
					{
						BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
						String line = "";
						
						while ((line = reader.readLine())!= null) 
						{
							if(line.equals("") == false)
							{
								//ui.appendTxt_adbCommand(line + "\n");
								System.out.println(line);
								sb.append(line + " ");
							}
						}
					}
					
					// Error Messages
					BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
					String line = "";
					boolean flag = false;
					
					while ((line = reader.readLine())!= null) 
					{						
						if (flag == false) {
							System.err.println("Error: \n");
							flag = true;
						}
						if(line.equals("") == false)
						{
							System.err.println(line + "\n");
						}
					}
					
					p.destroy();
					
					if (flag) {
						throw new IOException("There is some error message.");
					} else {
						
						// Generate ADB Commands
						InterfaceWithExecution ie = new InterfaceWithExecution() {

							@Override
							public void appendTxt_testArtifacts(String str) {
								System.out.println(str);
							}

							@Override
							public void done_testArtifacts(boolean isfail_flag) {
								if(isfail_flag) {
									stop = true;
								}
							}

							@Override
							public void appendTxt_intentSpec(String str) {
							}

							@Override
							public void done_intentSpec() {
							}
							
						};
						
						String genAdbCommand = System.getProperty("user.dir") 
								+ "/../GenTestsfromIntentSpec/bin/" + genCommand + " AdbCommand "; 
						
						
						String intentSpec = sb.toString();
						String path = buildIntentSpecParam("param"+count+".is", intentSpec);
						
						intentSpec = " -f " + path;
						
						//if(intentSepc.charAt(0) != '"')
						//{
							//intentSepc = "\"" + intentSepc;
						//}
						//if(intentSepc.charAt(intentSepc.length() - 1) != '"')
						//{
							//intentSepc = intentSepc + "\""; 
						//}
						
						genAdbCommand = genAdbCommand + " 0 " + " " +
										    " 3 " + " " +
										    intentSpec + " ";
						
						System.out.println("RUN: " + genAdbCommand);
						
						ExecuteShellCommand.executeMakeTestArtifacts(ie, genAdbCommand);
					}
				}
				
			} catch (IOException e) {
				fail(e.getStackTrace().toString());
			} 
			
		}
		
		if (stop) 
			fail("Something wrong...");
	}
	
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
