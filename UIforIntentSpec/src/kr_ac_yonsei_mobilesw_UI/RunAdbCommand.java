package kr_ac_yonsei_mobilesw_UI;

import java.util.ArrayList;
import java.util.Scanner;

public class RunAdbCommand {

	// Input : stdin   => Adb Commands
	public static void main(String[] args) {
		ArrayList<String> arrList = new ArrayList<String>();
		
		Scanner scan = new Scanner(System.in);
		while (scan.hasNextLine()) {
			String adbcmd = scan.nextLine();
			if (adbcmd.length() >=3 && adbcmd.startsWith("adb")) {
				arrList.add(adbcmd);
			}
		}
		
		String[] arr = new String[arrList.size()+1];
		int i = 0;
		arr[i] = "-EXIT_ON_CLOSE"; i++;
		for (; i< arr.length; i++) {
			arr[i] = arrList.get(i-1);
		}

//		for (String s : arr) {
//			System.out.println("> " + s);
//		}
		
		Benchmark.main(arr);
	}

}
