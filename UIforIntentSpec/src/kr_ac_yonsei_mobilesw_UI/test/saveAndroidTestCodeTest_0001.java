package kr_ac_yonsei_mobilesw_UI.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

import javax.swing.JFrame;

import kr_ac_yonsei_mobilesw_UI.Benchmark;
import kr_ac_yonsei_mobilesw_UI.GenAndroidTestCodeUI;

import org.junit.Test;

public class saveAndroidTestCodeTest_0001 {

	@Test
	public void test() throws FileNotFoundException {
		
		File file = new File("./src/kr_ac_yonsei_mobilesw_UI/test/" + "saveAndroidTestCodeTest_0001.txt");

		Scanner scan = new Scanner(new FileReader(file));
		StringBuffer sb = new StringBuffer();
		while (scan.hasNext()) {
			sb.append(scan.nextLine() + "\n");
		}
		GenAndroidTestCodeUI.saveAndroidTestCode("com.example.android.test", "D:/TEMP", 
				sb.toString(), new JFrame(), true);
	}

}
