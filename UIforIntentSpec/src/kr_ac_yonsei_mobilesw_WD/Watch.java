package kr_ac_yonsei_mobilesw_WD;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Watch {

	static File Dir;
	static String pid;
	static int argcount;
	static String arguments = "";
	public static void main(String[] args){

		for(argcount = 0; argcount < args.length; argcount++)
		{
			arguments += " " + args[argcount]; 
		}
		
		Dir = new File(args[argcount-1]);		
		startWatchDogLoop();
		while(true)
		{
			try {
				Thread.currentThread().sleep(60000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Process p;
			try {
				p = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/k", "jps"});
				while (p.isAlive()) {
					BufferedReader reader = 
							new BufferedReader(
									new InputStreamReader(p.getInputStream()));
					String temp = "";
					String line = "";

					while (!(temp = reader.readLine()).equals("")) 
					{
						System.out.println(temp);
						line += temp + " ";
					}
					if(line.contains("WatchDogLoop") == false){ // loop가 돌아가지 않는 중
						File Temp_Dir = new File(Dir.getAbsoluteFile() + "\\temp_test");
						if(Temp_Dir.exists() == false) // 테스트가 모두 끝남
						{
							return;
						}
						else // 테스트가 아직 남아있음
						{
							System.out.println("fail\n");
							Temp_Dir.listFiles()[0].delete();
							getpid();
							killcmd();
							startWatchDogLoop();
						}
					}
					else
					{
						System.out.println("success\n");
					}
					p.destroy();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void startWatchDogLoop()
	{
		try {
			Runtime.getRuntime().exec(new String[]{"cmd.exe", "/k", "start \"hwachawatchdog\" start.bat " + arguments});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void getpid()
	{
		try {
			Process p = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/k", "tasklist /fi \"windowtitle eq hwachawatchdog*\" /fo list"});
			while (p.isAlive()) {
				BufferedReader reader = 
						new BufferedReader(
								new InputStreamReader(p.getInputStream()));
				String temp = reader.readLine();
				ArrayList<String> line = new ArrayList<String>();
				while (!(temp = reader.readLine()).equals("")) 
				{
					line.add(temp);
				}
				pid = line.get(1).replace("PID:", "").trim();
				p.destroy();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	public static void killcmd()
	{
		try {
			Runtime.getRuntime().exec(new String[]{"cmd.exe", "/k", "taskkill /pid " + pid});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

