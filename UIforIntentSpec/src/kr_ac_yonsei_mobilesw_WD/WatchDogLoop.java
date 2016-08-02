package kr_ac_yonsei_mobilesw_WD;

import java.io.*;
import java.util.ArrayList;

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
public class WatchDogLoop {

	static File Dir; // apk������ ����ִ� ����
	static File Temp_Dir; // �׽�Ʈ������ ������ apk�� ���� ����
	static File[] Filelist;
	static File[] Temp_File;
	static int exitvalue;
	static int argcount;
	static ArrayList<String> arguments = new ArrayList<String>();
	
	public static void main(String[] args)
	{
		for(argcount = 0; argcount < args.length; argcount++)
		{
			arguments.add(args[argcount]);
		}
		Dir = new File(args[argcount-1]); 
		
		MakeDummyFolder(); // �������� ����, apk���� ����
		
		RunTest(); // �׽�Ʈ ����
		Temp_Dir.delete();
	}

	public static void MakeDummyFolder()
	// apk���� ���ο� temp_test ���� ����
	{
		String Dirpath = Dir.getAbsolutePath() + "\\temp_test";
		Filelist = Dir.listFiles();
		Temp_Dir = new File(Dirpath);
		if(Temp_Dir.exists() == false)
		{
			Temp_Dir.mkdirs();
			CopyAPK(); // apk���� ����
		}
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

		while(Temp_Dir.listFiles().length != 0)
		{
			Temp_File = Temp_Dir.listFiles();
			arguments.remove(argcount-1);
			arguments.add(Temp_File[0].getAbsolutePath());
			exitvalue = MainByCommand.main2(arguments.toArray(new String[0]));

			if(exitvalue == 0) // ����
			{
				try {
					FileWriter output = new FileWriter(new File(Dir.getAbsolutePath() + "\\successlist.txt"), true);
					output.append(Temp_File[0].getName() + "\n");
					output.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			Temp_File[0].delete();
		}
	}
}
