package kr_ac_yonsei_mobilesw_WD;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Watch {

	public static void main(String[] args){

		File Temp_Dir = new File(args[0]);
		//		while(Temp_Dir.listFiles().length != 0)
		//		{
		//			Process p;
		//			try {
		//				p = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/k", "jps"});
		//				while (p.isAlive()) {
		//					BufferedReader reader = 
		//							new BufferedReader(
		//									new InputStreamReader(p.getInputStream()));
		//					String temp = "";
		//					String line = "";
		//
		//					while (!(temp = reader.readLine()).equals("")) 
		//					{
		//						System.out.println(temp);
		//						line = temp;
		//					}
		//					if(line.contains("MainByCommand") == false){
		//						
		//						System.out.println("false:");
		//					}
		//					p.destroy();
		//				}
		//			} catch (IOException e) {
		//				// TODO Auto-generated catch block
		//				e.printStackTrace();
		//			}
		//			try {
		//				Thread.currentThread().sleep(60000);
		//			} catch (InterruptedException e1) {
		//				// TODO Auto-generated catch block
		//				e1.printStackTrace();
		//			}
		//		}
		try {
			String cmd = "java -cp \"bin;lib/*;tool/*;\" kr_ac_yonsei_mobilesw_WD.Main";
			File[] Temp_File = Temp_Dir.listFiles();
			cmd += Temp_File[0].getAbsolutePath();
			Process p = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", "start " + cmd});

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
