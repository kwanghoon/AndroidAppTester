package kr_ac_yonsei_mobilesw_UI;

import java.util.concurrent.locks.ReentrantLock;

import javax.swing.table.DefaultTableModel;

public interface Benchbase {

	DefaultTableModel modelAdbCommand = new DefaultTableModel();	
	DefaultTableModel modelLogcatView = new DefaultTableModel();
	DefaultTableModel modelLogcat  = new DefaultTableModel();;
	ReentrantLock LogcatLock = new ReentrantLock();
	
	public void showDeviceList(String line, int listNum);
	public void appendTxt_logcat(String line);
	public void showLogcat();
	public void exec(String string);
	public void setLogcat(String adbCommand, String packageName);
	public String[] getAdbCommandLog();
	public void showResult(int Normal, int Exit, int ErrorExit, int IntentSpecCatchAndNormal, int IntentSpecCatchAndExit,
			int IntentSpecCatchAndErrorExit, int IntentSpecPassAndNormal, int IntentSpecPassAndExit, int IntentSpecPassAndErrorExit,
			int resultCount, int CantAnalyze, boolean flagUseIntentAssertion);
	public void endBenchStart(String fileName);
	public void AdbKillAndStart();
	public void RebootDevice();

}
