package kr_ac_yonsei_mobilesw_UI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import kr_ac_yonsei_mobilesw_shell.ExecuteShellCommand;


public class BenchCommand implements Benchbase {

	private static String adbPathString; 
	private static String deviceID = "";
	private static int deviceNum = 1;
	private static String excelfileName = "";
	
	static {
		adbPathString = Config.getAdbPath();

		if (adbPathString == null) {
			String osName = System.getProperty("os.name");
			String osNameMatch = osName.toLowerCase();
			if(osNameMatch.contains("linux")) {
				adbPathString = "/home/";
			} else if(osNameMatch.contains("windows")) {
				adbPathString = "C:/users/";
			} else if(osNameMatch.contains("mac os") || osNameMatch.contains("macos") || osNameMatch.contains("darwin")) {
				adbPathString = "/Users/";
			} else {
				adbPathString = "C:/users/"; // Windows OS by default
			}
		}
	}
	
	public int StepDone = 0;
	private String adbCommandLog; 
	HashMap<String, String> mapPidToApplicationName = new HashMap<String, String>();
	private int logcatMaximumCount = 5000;
	public int logcatReadStartIndex = 0;
	public String logcatFilter = "";
	public String NowTxtFilter = "";
	private char logLevel = 'V';
	private char nowLogLevel = 'V';
	private boolean isBusy = false;
	public boolean benchStartProcessingFlag = false;
	private final Logger logger = Logger.getLogger(Benchmark.class.getName());
	
	// Input : stdin   => Adb Commands
	//run command : java -cp "bin;lib/*;tool/*" kr_ac_yonsei_mobilesw_UI.BenchCommand -adbpath ADBPATH -device DIGIT < notelist.adbcmds
	public static void main(String[] args) {
		Scanner scan = null;
		boolean adbPathExist = false;
		//adbpath È®ÀÎ
		ArrayList<String> arrList = new ArrayList<String>();
		try{
			for(int i = 0; i < args.length; i+= 2)
			{
				if(args[i].equals("-adbpath"))
				{
					adbPathString = args[i+1] + "\\";
				}
				else if(args[i].equals("-device"))
				{
					deviceNum = Integer.valueOf(args[i+1]);
				}
				else if(args[i].equals("-adbcmds"))
				{
					FileReader input = new FileReader(new File(args[i+1]));
					scan = new Scanner(input);
				}
			}
		}
		catch(Exception e){
			System.out.println("Wrong Command");
			return;
		}
		File f = new File(adbPathString);
		File[] flist = f.listFiles();
		for(File l:flist)
		{
			if(l.getName().contains("adb")) 
			{
				Config.putAdbPath(adbPathString);
				adbPathExist = true;
				break;
			}
		}
		if(adbPathExist == false)
		{
			System.out.println("Need AdbPath.( -adbpath PATH )");
			return;
		}
		if(scan == null)
		{
			scan = new Scanner(System.in);
		}
		while (scan.hasNextLine()) {
			String adbcmd = scan.nextLine();
			if (adbcmd.length() >=3 && adbcmd.startsWith("adb")) {
				arrList.add(adbcmd);
			}
		}
		
		
		BenchCommand benchcommand = new BenchCommand(arrList);
		
		//Step 3-1: ReadDevice
		benchcommand.ReadDevice();
		
		//Step 3-2: ReadLogcat
		benchcommand.ReadLogcat();

		//Step 3-3: Test AdbCommand
		benchcommand.run();		
		
		//Step 3-4: Wait Test
		benchcommand.waitTest();
	}

	public synchronized void waitTest()
	{
		while(StepDone != 3){try {
			wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}}
	}
	public BenchCommand(ArrayList<String> arrList)
	{
		modelLogcat.addColumn("Level");
		modelLogcat.addColumn("Time");
		modelLogcat.addColumn("PID");
		modelLogcat.addColumn("TID");
		modelLogcat.addColumn("Application");
		modelLogcat.addColumn("Tag");
		modelLogcat.addColumn("Text");
		modelLogcat.addColumn("RawMessage");

		modelLogcatView.addColumn("Level");
		modelLogcatView.addColumn("Time");
		modelLogcatView.addColumn("PID");
		modelLogcatView.addColumn("TID");
		modelLogcatView.addColumn("Application");
		modelLogcatView.addColumn("Tag");
		modelLogcatView.addColumn("Text");
		modelLogcatView.addColumn("RawMessage");

		modelAdbCommand.addColumn("Seq");
		modelAdbCommand.addColumn("Command");
		modelAdbCommand.addColumn("Result");

		for(int i=0; i<arrList.size(); i++) {
			modelAdbCommand.addRow(new Object[] { String.valueOf(i+1), arrList.get(i), ""});
		}
	}
	
	public synchronized void run()
	{
		if(modelAdbCommand.getRowCount() < 1)
    	{
    		return;
    	}
    	while(StepDone != 2){try {
			wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}} // wait
    	BenchStart benchStart = new BenchStart();
    	benchStartProcessingFlag = true;
    	benchStart.benchStartProcessingFlag = benchStartProcessingFlag;
    	benchStart.start(BenchCommand.this);
	}
	public void filterEvent(String packageName)
	{
		LogcatLock.lock();

		NowTxtFilter = packageName.trim();
		showLogcat();

		LogcatLock.unlock();
	}

	public void ReadDevice()
	{
		String command = adbPathString + "adb devices -l";
		ExecuteShellCommand.readDevice(BenchCommand.this, command);
	}
	
	@Override
	public synchronized void showDeviceList(String line, int listNum) {
		// TODO Auto-generated method stub
		System.out.println(line + "    " + listNum);
		if(listNum == deviceNum)
		{
			deviceID = line.substring(0, line.indexOf(" "));
			StepDone = 1; 
		}
		notifyAll();
	}
	
	public synchronized void ReadLogcat()
	{
		while(StepDone != 1){try {
			wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}} // wait
		
		if(deviceID.equals(""))
		{
			System.out.println("Wrong device number.");
			Runtime.getRuntime().exit(0);
		}
		String command = adbPathString + "adb -s " + deviceID + " logcat -v threadtime *:V";
		System.out.println(command);
		ExecuteShellCommand.showLogcat(BenchCommand.this, command);
	}

	public void appendTxt_logcat(String str)
	{
		if(str.equals("--------- beginning of /dev/log/system") || str.equals("--------- beginning of /dev/log/main"))
		{
			return;
		}

		Object[] newRow = parseLog(str);
		LogcatLock.lock();

		if(newRow != null)
		{
			modelLogcat.addRow(newRow);

			if(modelLogcat.getRowCount() > logcatMaximumCount)
			{
				modelLogcat.removeRow(0);
				logcatReadStartIndex--;
			}
		}

		//logger.info("modelLogcat => modelLogcat.getRowCount() : " + modelLogcat.getRowCount() + ", logcatMaximumCount : " + logcatMaximumCount + ", logcatReadStartIndex : " + logcatReadStartIndex);

		LogcatLock.unlock();
	}

	public Object[] parseLog(String str)
	{
		Object[] row = null;


		//mapPidToApplicationName
		try
		{
			if(str.indexOf("Start proc ") != -1)
			{
				int idx = str.indexOf("Start proc ");

				if(str.indexOf("pid=") != -1)
				{
					String pid = str.substring(str.indexOf("pid=") + 4, str.indexOf(" ", str.indexOf("pid=") + 4));
					String applicationName = str.substring(str.indexOf("Start proc ") + 11, str.indexOf(" ", str.indexOf("Start proc ") + 11));

					String orgApplicationName = mapPidToApplicationName.get(pid);
					if(orgApplicationName == null)
					{
						mapPidToApplicationName.put(pid, applicationName);						
					}
					else
					{
						mapPidToApplicationName.remove(pid);
						mapPidToApplicationName.put(pid, applicationName);	
					}
				}
			}

			if(str.length() > 33)
			{
				String level = str.substring(31, 32);
				String time = str.substring(0, 18);
				String pid = str.substring(19, 24).trim();
				String tid = str.substring(25, 30).trim();
				int endOfTag = str.indexOf(':', 33);
				String tag = str.substring(33, endOfTag);
				String text = str.substring(endOfTag + 2, str.length());

				String ApplicationName = mapPidToApplicationName.get(pid);
				if(ApplicationName != null)
				{
					str = str + " id:" + ApplicationName + " ";
				}
				int startOfId = str.indexOf(" id:");
				String application = "";
				if(startOfId != -1)
				{
					int endOfId = str.indexOf(' ', startOfId + 5);
					if(endOfId == -1)
					{
						endOfId = str.length();
					}
					//logger.info("btnViewlogcat => parseLog - endOfId : " + endOfId);
					application = str.substring(startOfId + 4, endOfId).trim();
				}

				row = new Object[]{level, time, pid, tid, application, tag, text, str};
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return row;
	}

	public synchronized void showLogcat()
	{
		LogcatLock.lock();

		if(NowTxtFilter.equals("") == false || nowLogLevel != 'V')
		{
			if(logcatFilter.equals(NowTxtFilter) == false || logLevel != nowLogLevel)
			{
				logLevel = nowLogLevel;
				logcatFilter = NowTxtFilter;
				modelLogcatView.setRowCount(0);

				for(int i = 0; i < modelLogcat.getRowCount(); i++)
				{
					char level = modelLogcat.getValueAt(i, 0).toString().charAt(0);

					if(modelLogcat.getValueAt(i, 7).toString().contains(logcatFilter))
					{
						if(logLevel == 'V')
						{
							addRow(i);
						}
						else if(logLevel == 'D'
								&& level != 'V')
						{
							addRow(i);
						}
						else if(logLevel == 'I'
								&& level != 'V'
								&& level != 'D')
						{
							addRow(i);
						}
						else if(logLevel == 'W'
								&& level != 'V'
								&& level != 'D'
								&& level != 'I')
						{
							addRow(i);
						}
						else if(logLevel == 'E'
								&& level != 'V'
								&& level != 'D'
								&& level != 'I'
								&& level != 'W')
						{
							addRow(i);
						}

					}
				}
			}
			else
			{
				for(int i = logcatReadStartIndex; i < modelLogcat.getRowCount(); i++)
				{

					char level = modelLogcat.getValueAt(i, 0).toString().charAt(0);

					if(modelLogcat.getValueAt(i, 7).toString().contains(logcatFilter))
					{
						if(logLevel == 'V')
						{
							addRow(i);
						}
						else if(logLevel == 'D'
								&& level != 'V')
						{
							addRow(i);
						}
						else if(logLevel == 'I'
								&& level != 'V'
								&& level != 'D')
						{
							addRow(i);
						}
						else if(logLevel == 'W'
								&& level != 'V'
								&& level != 'D'
								&& level != 'I')
						{
							addRow(i);
						}
						else if(logLevel == 'E'
								&& level != 'V'
								&& level != 'D'
								&& level != 'I'
								&& level != 'W')
						{
							addRow(i);
						}
					}
				}
			}
		}
		else
		{
			if(logcatFilter.equals(NowTxtFilter) == false || logLevel != nowLogLevel)
			{
				logLevel = nowLogLevel;
				logcatFilter = NowTxtFilter;
				modelLogcatView.setRowCount(0);

				for(int i = 0; i < modelLogcat.getRowCount(); i++)
				{
					addRow(i);
				}
			}
			else
			{
				for(int i = logcatReadStartIndex; i < modelLogcat.getRowCount(); i++)
				{
					addRow(i);
				}
			}
		}

		logcatReadStartIndex = modelLogcat.getRowCount();

		if(modelLogcatView.getRowCount() > logcatMaximumCount)
		{
			modelLogcatView.removeRow(0);
		}

		//logger.info("modelLogcatView => modelLogcatView.getRowCount() : " + modelLogcatView.getRowCount() + ", logcatMaximumCount : " + logcatMaximumCount + ", logcatReadStartIndex : " + logcatReadStartIndex);

		LogcatLock.unlock();
		StepDone = 2;
		notifyAll();
	}

	public void LogcatClear()
	{
		LogcatLock.lock();

		modelLogcat.setRowCount(0);
		modelLogcatView.setRowCount(0);
		logcatReadStartIndex = 0;

		LogcatLock.unlock();
	}

	private void addRow(int row)
	{
		Object[] newRow = new Object[]{modelLogcat.getValueAt(row, 0), modelLogcat.getValueAt(row, 1), modelLogcat.getValueAt(row, 2), modelLogcat.getValueAt(row, 3),
				modelLogcat.getValueAt(row, 4), modelLogcat.getValueAt(row, 5), modelLogcat.getValueAt(row, 6), modelLogcat.getValueAt(row, 7)};

		modelLogcatView.addRow(newRow);
	}

	public void setisBusy(boolean bool)
	{
		isBusy = bool;
	}

	public boolean getisBusy()
	{
		return isBusy;
	}

	@Override
	public void exec(String command)
	{
		if(command.equals("") == true)
		{
			return;
		}

		if(getisBusy() == true)										//Already processing
		{
			JOptionPane.showMessageDialog (null, "Now Processing...");
			return;
		}

		setisBusy(true);

		command = command.replace("adb shell am", "adb -s " + deviceID + " shell am");
		logger.info("analyze => DevicesID : " + deviceID + ", command : " + command);
		command = adbPathString + command;
		executeCommand(command);
		setisBusy(false);
	}

	public void executeCommand(String command)
	{
		Thread worker = new Thread()
		{
			public void run()
			{
				Process p = null;
				try {					
					p = Runtime.getRuntime().exec(command);
					adbCommandLog = ("> " + command);
					while(p.isAlive())
					{
						BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
						String line = "";

						while ((line = reader.readLine())!= null) 
						{
							if(line.equals("") == false)
							{
								adbCommandLog += "\n" + line;
							}
						}					
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

	@Override
	public void setLogcat(String adbCommand, String packageName) {
		// TODO Auto-generated method stub
		LogcatClear();
		filterEvent(packageName);
		exec(adbCommand);
	}

	@Override
	public String[] getAdbCommandLog() {
		// TODO Auto-generated method stub
		return adbCommandLog.split("\n");
	}

	@Override
	public void showResult(int Normal, int Exit, int ErrorExit, int IntentSpecCatchAndNormal, int IntentSpecCatchAndExit,
			int IntentSpecCatchAndErrorExit, int IntentSpecPassAndNormal, int IntentSpecPassAndExit, int IntentSpecPassAndErrorExit,
			int resultCount, int CantAnalyze, boolean flagUseIntentAssertion) {
		// TODO Auto-generated method stub
		System.out.println("Pass\t: " + (Normal+Exit)
				// + "\nPass\t\t: " + Exit 
				+ "\nFail\t: " + ErrorExit

				+ (flagUseIntentAssertion ?

						"\nAssert/F=>Pass\t: " + IntentSpecCatchAndNormal
						+ "\nAssert/F=>Pass\t: " + IntentSpecCatchAndExit
						+ "\nAssert/F=>Fail\t: " + IntentSpecCatchAndErrorExit
						+ "\nAssert/T=>Pass\t: " + IntentSpecPassAndNormal 
						+ "\nAssert/T=>Pass\t: " + IntentSpecPassAndExit 
						+ "\nAssert/T=>Fail\t: " + IntentSpecPassAndErrorExit

						: "")

				+ "\nProgress\t: " + (int)(((double)resultCount / modelAdbCommand.getRowCount()) * 100) + "% (" + (resultCount + "/" + modelAdbCommand.getRowCount() + ")")
				+ "\nAnalysis Failure : " + CantAnalyze );
		
	}

	@Override
	public synchronized void endBenchStart(String fileName) {
		// TODO Auto-generated method stub
		excelfileName = fileName;
		StepDone = 3;
		notifyAll();
	}

	@Override
	public void AdbKillAndStart() {
		// TODO Auto-generated method stub
		exec("adb -s " + deviceID + " kill-server");
		try {
			Thread.currentThread().sleep(2000);
		} catch (InterruptedException e) {
			
		}

		exec("adb -s " + deviceID + " start-server");
		try {
			Thread.currentThread().sleep(3000);
		} catch (InterruptedException e) {

		}
//		ReadDevice();
		StepDone = 1;
		ReadLogcat();
	}

	@Override
	public void RebootDevice() {
		// TODO Auto-generated method stub
		exec("adb -s " + deviceID + " reboot");
		try {
			Thread.currentThread().sleep(180000);
		} catch (InterruptedException e) {
			
		}
//		ReadDevice();
		StepDone = 1;
		ReadLogcat();
	}

	public static String getExcelfileName(){
		return excelfileName;
	}
}

