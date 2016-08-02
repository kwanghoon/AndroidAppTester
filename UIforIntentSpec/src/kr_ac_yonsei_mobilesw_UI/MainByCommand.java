package kr_ac_yonsei_mobilesw_UI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import com.example.java.GenIntentSpecFromAPK;
import kr_ac_yonsei_mobilesw_Grouping.GroupingResult;
import kr_ac_yonsei_mobilesw_shell.ExecuteShellCommand;

public class MainByCommand{

	public static String PkgName = "";
	private static String appPath;
	private static String TestCodeDir = null;
	private static String osNameStr = "windows";
	private static String genCommand = "gen.exe";
	private static String adbPathString; 
	private static String deviceID = "";
	public static String excelfileName = "";
	private static String randomisfileName = "";
	private static int mode = 0;
	private static int count = 3;
	private static double allTime = 0;
	private static int deviceNum = 1;
	private static Date startTime;
	private static Date endTime;
	public static int StepDone = 0;
	private static boolean isApk = false;
	private static ArrayList<Integer> Represent = null;
	private static boolean InstallFlag = false;
	private static boolean internalFlag = false;

	static {
		adbPathString = Config.getAdbPath();

		if (adbPathString == null) {
			String osName = System.getProperty("os.name");
			String osNameMatch = osName.toLowerCase();
			if(osNameMatch.contains("linux")) {
				adbPathString = "/home/";
				osNameStr = "linux";
				genCommand = "gen_linux";
			} else if(osNameMatch.contains("windows")) {
				adbPathString = "C:/users/";
				osNameStr = "windows";
				genCommand = "gen.exe";
			} else if(osNameMatch.contains("mac os") || osNameMatch.contains("macos") || osNameMatch.contains("darwin")) {
				adbPathString = "/Users/";
				genCommand = "gen_mac";
				osNameStr = "mac";
			} else {
				adbPathString = "C:/users/"; // Windows OS by default
				osNameStr = "windows"; // Windows OS by default
				genCommand = "gen.exe";
			}
		}
	}

	public static void main(String[] args)
	{	
		try {
			if (args.length > 2) {
				appPath = args[args.length - 1];
				for (int i = 0; i < args.length; i += 2) {
					if (args[i].equals("-mode")) {
						mode = Integer.valueOf(args[i + 1]);
					}
					else if (args[i].equals("-count")) {
						count = Integer.valueOf(args[i + 1]);
					}
					else if (args[i].equals("-adbpath")) {
						adbPathString = args[i + 1] + "\\";
						Config.putAdbPath(args[1]);
					}
					else if (args[i].equals("-device")) {
						deviceNum = Integer.valueOf(args[i + 1]);
					}
					else if (args[i].equals("-testcodedir")) {
						TestCodeDir = args[i + 1];
					}
				}
			}
			else if(args.length == 1)
			{
				if(args[0].equals("-device"))
				{
					String cmd = adbPathString + "adb devices";
					try {
						Runtime.getRuntime().exec(cmd);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					return ;
				}
				else if(args[0].equals("-adbpath"))
				{
					System.out.println(adbPathString);
					return ;
				}
				else appPath = args[0];
			}
			else if(args.length == 2)
			{
				if(args[0].equals("-adbpath"))
				{
					Config.putAdbPath(args[1] + "\\");
					return ;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Wrong Command\n-mode DIGIT\n-count DIGIT\n-adbpath PATH\n-device DIGIT\n-testcodedir PATH\n");
			return ;
		}

		MainByCommand main = new MainByCommand();
		main.start();
		return ;
	}
	
	public static int main2(String[] args)
	{	
		try {
			if (args.length > 2) {
				appPath = args[args.length - 1];
				for (int i = 0; i < args.length; i += 2) {
					if (args[i].equals("-mode")) {
						mode = Integer.valueOf(args[i + 1]);
					}
					else if (args[i].equals("-count")) {
						count = Integer.valueOf(args[i + 1]);
					}
					else if (args[i].equals("-adbpath")) {
						adbPathString = args[i + 1] + "\\";
						Config.putAdbPath(args[1]);
					}
					else if (args[i].equals("-device")) {
						deviceNum = Integer.valueOf(args[i + 1]);
					}
					else if (args[i].equals("-testcodedir")) {
						TestCodeDir = args[i + 1];
					}
				}
			}
			else if(args.length == 1)
			{
				if(args[0].equals("-device"))
				{
					String cmd = adbPathString + "adb devices";
					try {
						Runtime.getRuntime().exec(cmd);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					return 1;
				}
				else if(args[0].equals("-adbpath"))
				{
					System.out.println(adbPathString);
					return 1;
				}
				else appPath = args[0];
			}
			else if(args.length == 2)
			{
				if(args[0].equals("-adbpath"))
				{
					Config.putAdbPath(args[1] + "\\");
					return 1;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Wrong Command\n-mode DIGIT\n-count DIGIT\n-adbpath PATH\n-device DIGIT\n-testcodedir PATH\n");
			return 1;
		}

		MainByCommand main = new MainByCommand();
		main.start();
		return 0;
	}

	public void start() 
	{
		try {
			//Step 1: Generate IntentSpec(.is file)
			GenIntentSpec();

			//Step 2: If Apk, Install.
			InstallApk();

			//Step 3: Generate ADBCommand
			GenAdbCommand();

			//Step 4: Test ADBCommand (by BenchCommand.java)
			RunBenchCommand();

			//Step 5: Grouping Result
			GroupingResult();

			//Step 6: Make .randomis file from Grouping
			MakeRepresentRandomisFile();

			//Step 7: Generate JUnit test code
			GenTestCode();

			//Step 8: uninstall apk
			UninstallApk();

			//Wait
			WaitEnd();
			
			int restartCount = BenchStart.getrestartCount();
			String outputStr = "End of Test.\nTotal Time : " + allTime + "sec";
			String outputStr2 = "Rebooting Time : " + restartCount*180 + "sec";
			String outputStr3 = " Real Test Time : " + (allTime - restartCount*180) + "sec";
			System.out.println(outputStr);
			try {
				FileWriter output = new FileWriter(new File("TimeLog.txt"), true);
				output.append(outputStr + "\n" + outputStr2 + "\n" + outputStr3 + "\n\n");
				output.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			System.err.println("Error in Start : ");
			e.printStackTrace();
			return;
		}
	}
	
	public static void WriteLog(String Work)
	{
		double Time = (double)(endTime.getTime() - startTime.getTime())/1000;
		allTime += Time;
		String outputStr = Work + " : Started at " + startTime + ", Finished at " + endTime + ",\t" + Time + "sec";
		System.out.println(outputStr);
		try {
			FileWriter output = new FileWriter(new File("TimeLog.txt"), true);
			output.append(outputStr + "\n");
			output.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public synchronized void GenIntentSpec()
	{
		startTime = new Date();

		GenIntentSpecFromAPK.main(new String[]{"-console", appPath});
		PkgName = GenIntentSpecFromAPK.getPkgName();
		isApk = GenIntentSpecFromAPK.isApk();
		internalFlag = GenIntentSpecFromAPK.existInternal();
		endTime = new Date();
		WriteLog(PkgName + "\nIntentSpec Generation");
		StepDone = 1;
		notifyAll();
	}

	public synchronized void InstallApk() 
	{
		while(StepDone != 1){try {
			wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}} //wait
		
		startTime = new Date();
		
		if(isApk == true)
		{
			String cmd = adbPathString + "adb kill-server";
			try {
				Process p = Runtime.getRuntime().exec(cmd);
				p.waitFor();
				Thread.currentThread().sleep(2000);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			cmd = adbPathString + "adb start-server";

			try {
				Process p = Runtime.getRuntime().exec(cmd);
				p.waitFor();
				Thread.currentThread().sleep(2000);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	

			cmd = adbPathString + "adb install " + appPath;
			int error_code = -1;
			System.out.println(cmd);
			try {
				Process p = Runtime.getRuntime().exec(cmd);
				error_code = p.waitFor();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (error_code == 0) 
			{
				System.out.println("End of APK Installation");
				InstallFlag = true;
			}
			else
			{
				System.out.println("Installation Fail");
				InstallFlag = false;
			}
		}
		
		endTime = new Date();
		WriteLog("Install APK");
		StepDone = 2;
		notifyAll();
	}

	public synchronized void GenAdbCommand()
	{
		while(StepDone != 2){try {
			wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}} // wait 

		startTime = new Date();

		String command = "\"" + System.getProperty("user.dir") 
		+ "/../GenTestsfromIntentSpec/bin/" + genCommand + "\"" + " AdbCommand "
		+ mode + " " + count + " -f " + PkgName + ".is > " + PkgName + ".adbcmds";
		System.out.println("Run: " + command);
		Process p;
		try {
			if ("mac".equals(osNameStr)) {
				p = Runtime.getRuntime().exec(new String[] {"/bin/bash", "-c", command});
			}
			else {
				p =Runtime.getRuntime().exec(new String[]{"cmd.exe", "/y", "/c", command});
			}
			p.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		endTime = new Date();
		WriteLog("AdbCommand Generation");
		StepDone = 3;
		notifyAll();
	}

	public synchronized void RunBenchCommand()
	{
		while(StepDone != 3){try {
			wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}} // wait

		startTime = new Date();

		if(deviceNum == 1)
		{
			BenchCommand.main(new String[]{"-adbcmds", PkgName + ".adbcmds"});
		}
		else
		{
			BenchCommand.main(new String[]{"-device", String.valueOf(deviceNum), "-adbcmds", PkgName + ".adbcmds"});
		}
		excelfileName = BenchCommand.getExcelfileName();
		endTime = new Date();
		WriteLog("Test AdbCommand");
		StepDone = 4;
		notifyAll();
	}

	public synchronized void GroupingResult()
	{
		while(StepDone != 4){try {
			wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}} // wait

		startTime = new Date();
		GroupingResult.main(new String[]{excelfileName, PkgName});
		randomisfileName = GroupingResult.getRandomisfileName();
		Represent = GroupingResult.getRepresent();
		endTime = new Date();
		WriteLog("Grouping Result");
		StepDone = 5;
		notifyAll();
	}

	public synchronized void MakeRepresentRandomisFile()
	{
		while(StepDone != 5) {try {
			wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}} //wait

		try {
			String inputcontent = "";
			ArrayList<String> outputcontent = new ArrayList<String>();
			FileReader input = new FileReader(new File(PkgName + ".randomis"));
			FileWriter output = new FileWriter(new File(randomisfileName));
			Scanner scan = new Scanner(input);
			while (scan.hasNextLine()) {
				inputcontent = scan.nextLine();
			}
			input.close();
			inputcontent = inputcontent.substring(1,inputcontent.length()-1);
			if(inputcontent.length() == 0)
			{
				output.close();
				return;
			}
			while(inputcontent.length() > 0)
			{
				int endindex = findNextArray(inputcontent);
				String content = inputcontent.substring(0, endindex+1);
				if(!content.contains("Internal True"))
				{
					outputcontent.add(content);
				}
				endindex = inputcontent.indexOf("[", endindex);
				if(endindex < 0){
					inputcontent = "";
				}
				else{
					inputcontent = inputcontent.substring(endindex);
				}
			}		
			System.out.println("output : " + outputcontent.size());
			output.write("[");
			for(int i = 0; i < outputcontent.size(); i++)
			{
				for(int j = 0; j < Represent.size(); j++)
				{
					if(i == Represent.get(j))
					{
						output.write(outputcontent.get(i));
						if(j < Represent.size()-1){
							output.write(",");
						}
					}
				}
			}
			output.write("]");
			output.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Thread.currentThread().sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StepDone = 6;
		notifyAll();
	}

	public static int findNextArray(String input)
	{
		int firststart = input.indexOf("["); // first [
		int firstend = input.indexOf("]", firststart+1); // first ]
		int secondstart = input.indexOf("[", firststart+1); // second [
		while(secondstart < firstend && secondstart != -1)
		{
			firstend = input.indexOf("]", firstend+1);
			secondstart = input.indexOf("[", secondstart+1);
		}
		return firstend;
	}

	public synchronized void GenTestCode()
	{
		while(StepDone != 6){try {
			wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}} // wait

		startTime = new Date();
		String testPkgName = PkgName.substring(0,PkgName.lastIndexOf(".")) + ".test";
		GenAndroidTestCodeCommand.main(new String[]{"-output", TestCodeDir, "-package", testPkgName, randomisfileName});
		//		if(internalFlag)
		//		{
		//			GenAndroidTestCodeCommand.main(new String[]{"-output", TestCodeDir, "-package", testPkgName,
		//					"-internal", "Internal_" + PkgName + ".is", "-mode", String.valueOf(mode), randomisfileName});
		//		}
		//		else
		//		{
		//
		//			GenAndroidTestCodeCommand.main(new String[]{"-output", TestCodeDir, "-package", testPkgName, randomisfileName});
		//		}
		endTime = new Date();
		WriteLog("Generate Test Code");
		StepDone = 7;
		notifyAll();
	}

	public synchronized void UninstallApk() {
		while(StepDone != 7) {try {
			wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}} //wait

		startTime = new Date();
		
		if(InstallFlag == true)
		{
			String cmd = adbPathString + "adb kill-server";
			try {
				Runtime.getRuntime().exec(cmd);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			cmd = adbPathString + "adb start-server";

			try {
				Runtime.getRuntime().exec(cmd);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	

			cmd = adbPathString + "adb uninstall " + appPath;
			int error_code = -1;
			System.out.println(cmd);
			try {
				Process p = Runtime.getRuntime().exec(cmd);
				error_code = p.waitFor();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (error_code == 0) 
			{
				System.out.println("End of APK Uninstallation");
			}
			else
			{ 
				System.out.println("Unnstallation Fail");
			}
		}
		
		endTime = new Date();
		WriteLog("Uninstall APK");
		StepDone = 8;
		notifyAll();
	}

	public synchronized void WaitEnd()
	{
		while(StepDone != 8){
			try {
				wait();

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static String getPkgName(){
		return PkgName;
	}
}
