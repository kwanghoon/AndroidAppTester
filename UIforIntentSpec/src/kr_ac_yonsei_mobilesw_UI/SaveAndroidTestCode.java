package kr_ac_yonsei_mobilesw_UI;

import java.util.Scanner;

import javax.swing.JFrame;

public class SaveAndroidTestCode {

	// Input : args[0] => test package name
	//         args[1] => output dir
	//         stdin   => Android Test code prefixed by # and a file name
	public static void main(String[] args) {
			if (args.length >= 2) {
				String pkg = args[0];
				String output = args[1];
				
				StringBuilder sb = new StringBuilder();
				Scanner scan = new Scanner(System.in);
				while (scan.hasNextLine()) {
					String line = scan.nextLine();
					sb.append(line + "\n");
				}
				
				String testcode = sb.toString();
				GenAndroidTestCodeUI.saveAndroidTestCode(pkg, output, testcode, new JFrame(), true);
			}
	}

}
