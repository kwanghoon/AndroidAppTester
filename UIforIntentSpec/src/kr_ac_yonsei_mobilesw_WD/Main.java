package kr_ac_yonsei_mobilesw_WD;

import java.io.*;
import kr_ac_yonsei_mobilesw_UI.*;

/*
 * apk�� ���ִ� ������ �Ű������� ����
 * apk���� temp_test�� ���� ����� �� �ȿ� ����
 * temp_test ���� �ȿ� �ִ� apk�� �ϳ��� ����
 * tasklist jsp ��ɾ ���� java.exe �Ǵ� Java(TM) Platform SE binary Ȯ��, �������� �ڹ� ���μ��� ��� ȹ��
 * 10�а������� Ȯ���Ͽ� task ��Ͽ� ������ ���з� ����,
 * ������ apk�� Failed ������ ����� �̵�
 * ���ο� apk ����
 * ������ ��� �ؽ�Ʈ�� ���
 */
public class Main {

	static File Dir; // apk������ ����ִ� ����
	static File Temp_Dir; // �׽�Ʈ������ ������ apk�� ���� ����
	static File[] Filelist;
	static File[] Temp_File;

	public static void main(String[] args)
	{
		System.out.println(args[0]);
		Dir = new File(args[0]);

		MakeDummyFolder(args); // �������� ����
		CopyAPK(); // apk���� ����
		//RunTest(); // �׽�Ʈ ����
		Watch(); // ����

		Temp_Dir.delete();
	}

	public static void MakeDummyFolder(String[] args)
	// apk���� ���ο� temp_test ���� ����
	{
		String Dirpath = args[0] + "\\temp_test";
		Filelist = Dir.listFiles();
		Temp_Dir = new File(Dirpath);
		Temp_Dir.mkdirs();
	}

	public static void CopyAPK()
	// temp_test������ apk���� ����
	{
		InputStream instream; // ���� ����
		OutputStream outstream; // ������ ��ġ

		// ���� ���ϵ��� ������!
		try {
			for(int i = 0; i < Filelist.length; i++)
			{
				instream = new FileInputStream(Filelist[i]);
				outstream = new FileOutputStream(new File(Temp_Dir.getAbsolutePath() + "\\" + Filelist[i].getName()));
				byte[] buffer = new byte[1024];
				int length;

				while((length = instream.read(buffer)) > 0){
					outstream.write(buffer, 0, length);
				}
				instream.close();
				outstream.close();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void RunTest()
	{
		Thread worker = new Thread()
		{
			public void run()
			{
				while(Temp_Dir.listFiles().length != 0)
				{
					String cmd = "java -cp \"bin;lib/*;tool/*;\" kr_ac_yonsei_mobilesw_UI.MainByCommand -mode 0 -count 1 -device 1 ";
					Temp_File = Temp_Dir.listFiles();
					cmd += Temp_File[0].getAbsolutePath();
					System.out.println(cmd);
					Process p;
					try {
						p = Runtime.getRuntime().exec(cmd);
						while (p.isAlive()) {
							BufferedReader reader = 
									new BufferedReader(
											new InputStreamReader(p.getInputStream()));
							String line = "";

							while ((line = reader.readLine())!= null) 
							{
								System.out.println(line);
							}
						}
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		worker.start();
	}

	public static void Watch()
	// jps ��ɾ� ���
	{
		Thread worker = new Thread()
		{
			public void run()
			{
				while(Temp_Dir.listFiles().length != 0){
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
								line = temp;
							}
							if(line.contains("MainByCommand") == false){
//								Temp_File[0].delete();
//								RunTest();
								System.out.println("false:");
							}
							p.destroy();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						Thread.currentThread().sleep(60000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		};
		worker.start();

	}
}
