package kr_ac_yonsei_mobilesw_WD;

import java.io.*;
import java.util.ArrayList;

import kr_ac_yonsei_mobilesw_UI.*;

/*
 * apk가 모여있는 폴더를 매개변수로 받음
 * apk들을 temp_test를 새로 만들어 그 안에 복사
 * temp_test 폴더 안에 있는 apk를 하나씩 실행
 * tasklist jsp 명령어를 통해 java.exe 또는 Java(TM) Platform SE binary 확인, 실행중인 자바 프로세스 목록 획득
 * 10분간격으로 확인하여 task 목록에 없으면 실패로 간주,
 * 실패한 apk는 Failed 폴더를 만들어 이동
 * 새로운 apk 실행
 * 성공한 목록 텍스트로 출력
 */
public class WatchDogLoop {

	static File Dir; // apk파일이 들어있는 폴더
	static File Temp_Dir; // 테스트용으로 복사한 apk를 넣을 폴더
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
		
		MakeDummyFolder(); // 더미폴더 생성, apk파일 복사
		
		RunTest(); // 테스트 시작
		Temp_Dir.delete();
	}

	public static void MakeDummyFolder()
	// apk폴더 내부에 temp_test 폴더 생성
	{
		String Dirpath = Dir.getAbsolutePath() + "\\temp_test";
		Filelist = Dir.listFiles();
		Temp_Dir = new File(Dirpath);
		if(Temp_Dir.exists() == false)
		{
			Temp_Dir.mkdirs();
			CopyAPK(); // apk파일 복사
		}
	}

	public static void CopyAPK()
	// temp_test폴더에 apk파일 복사
	{
		InputStream instream; // 원본 파일
		OutputStream outstream; // 복사할 위치

		// 원본 파일들을 복사함!
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

			if(exitvalue == 0) // 성공
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
