package kr_ac_yonsei_mobilesw_shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import kr_ac_yonsei_mobilesw_UI.BenchAdd;
import kr_ac_yonsei_mobilesw_UI.Benchmark;
import kr_ac_yonsei_mobilesw_UI.InterfaceWithExecution;

public class ExecuteShellCommand {
	public static void executeCommand(Benchmark ui, String command) 
	{		
		Thread worker = new Thread()
		{
			public void run()
			{
				Process p = null;
				try {					
					p = Runtime.getRuntime().exec(command);
					
					ui.appendTxt_adbCommandLog("> " + command);
					
					while(p.isAlive())
					{
						BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
						String line = "";
						
						while ((line = reader.readLine())!= null) 
						{
							if(line.equals("") == false)
							{
								ui.appendTxt_adbCommandLog("\n" + line);
							}
						}
						
						ui.appendTxt_adbCommandLog("\n\n");
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				
				p.destroy();
			}
		};
		
		worker.start();
	}
	
	public static void showLogcat(Benchmark ui,  String command)
	{
		Thread worker = new Thread()
		{
			public void run()
			{
				Process p;
				try {
					p = Runtime.getRuntime().exec(command);
					
					while(p.isAlive())
					{
						BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
						String line = "";
						
						while ((line = reader.readLine())!= null) 
						{
							if(line.equals("") == false)
							{
								ui.appendTxt_logcat(line);
								ui.showLogcat();
							}
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		
		worker.start();
	}
	
	public static void readDevice(Benchmark ui, String command) 
	{		
		Thread worker = new Thread()
		{
			public void run()
			{
				Process p = null;
				try {
					
					for(int i = 0; i < 3; i ++)
					{
						p = Runtime.getRuntime().exec(command);
						
						while(p.isAlive())
						{
							if(i == 2)
							{
								BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
								String line = "";
								
								while ((line = reader.readLine())!= null) 
								{
									if(line.equals("") == false)
									{
										ui.showDeviceList(line);
									}
								}
							}
						}
					}
					
					p.destroy();
					
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		
		worker.start();
	}
		
	public static void executeMakeTestArtifacts(InterfaceWithExecution ui, String command) 
	{		
		Thread worker = new Thread()
		{
			public void run()
			{
				Process p = null;
				try {					
					p = Runtime.getRuntime().exec(command);
					
					//ui.appendTxt_adbCommand("> " + command);
					
					while(p.isAlive())
					{
						BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
						String line = "";
						
						while ((line = reader.readLine())!= null) 
						{
							if(line.equals("") == false)
							{
								//ui.appendTxt_adbCommand(line + "\n");
								ui.appendTxt_testArtifacts(line + "\n");
							}
						}
					}
				}
				catch (Exception e)
				{
					ui.appendTxt_testArtifacts(e.getStackTrace().toString());
				}
				
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
					String line = "";
					boolean flag = false;
					
					while ((line = reader.readLine())!= null) 
					{						
						if (flag == false) {
							ui.appendTxt_testArtifacts("Error: \n");
							flag = true;
						}
						if(line.equals("") == false)
						{
							ui.appendTxt_testArtifacts(line + "\n");
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				p.destroy();
				
				ui.done_testArtifacts();
			}
		};
		
		worker.start();
	}
	
	public static void executeImportIntentSpecCommand(InterfaceWithExecution ui, String command) 
	{		
		Thread worker = new Thread()
		{
			public void run()
			{
				Process p = null;
				try {					
					p = Runtime.getRuntime().exec(command);
					
					//ui.appendTxt_adbCommand("> " + command);
					
					while(p.isAlive())
					{
						BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
						String line = "";
						
						while ((line = reader.readLine())!= null) 
						{
							if(line.equals("") == false)
							{
								ui.appendTxt_intentSpec(line + "\n");
							}
						}
					}
					
					ui.done_intentSpec();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
					String line = "";
					boolean flag = false;
					
					while ((line = reader.readLine())!= null) 
					{
						if (flag == false) {
							ui.appendTxt_intentSpec("Error: \n");
							flag = true;
						}
						if(line.equals("") == false)
						{
							ui.appendTxt_intentSpec(line + "\n");
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				p.destroy();
			}
		};
		
		worker.start();
	}
	
}
