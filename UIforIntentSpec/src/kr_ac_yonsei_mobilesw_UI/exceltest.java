package kr_ac_yonsei_mobilesw_UI;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class exceltest {
	public static void main(String[] args){
	
		MainByCommand m = new MainByCommand();
		m.PkgName = "vStudio.Android.Camera360";
		m.excelfileName = "vStudio.Android.Camera360LOG_2016_04_30_00_08_06.xlsx";
		m.StepDone = 4;
		m.GroupingResult();
		m.MakeRepresentRandomisFile();

		//Step 7: Generate JUnit test code
		m.GenTestCode();
	}
	
}
