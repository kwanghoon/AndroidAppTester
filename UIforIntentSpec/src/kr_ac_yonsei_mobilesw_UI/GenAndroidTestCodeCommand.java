package kr_ac_yonsei_mobilesw_UI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import kr_ac_yonsei_mobilesw_shell.ExecuteShellCommand;

public class GenAndroidTestCodeCommand implements InterfaceWithExecution{

	private static String outputdir = null;
	private static String pkgName = "com.example.android.test";
	private static String genCommand = "";
	private static String IntentSpec = "";
	private static String InternalIS = null;
	private static int mode = 0;
	private static String osNameStr = "";
	
	static {
		String osName = System.getProperty("os.name");
		String osNameMatch = osName.toLowerCase();
		if(osNameMatch.contains("linux")) {
			genCommand = "gen_linux";
			osNameStr = "linux";
		} else if(osNameMatch.contains("windows")) {
			genCommand = "gen.exe";
			osNameStr = "windows";
		} else if(osNameMatch.contains("mac os") || osNameMatch.contains("macos") || osNameMatch.contains("darwin")) {
			genCommand = "gen_mac";
			osNameStr = "mac";
		}else {
			genCommand = "gen.exe"; // Windows OS by default
			osNameStr = "windows";
		}
	}
	
	private String TestCode = "";
	private int StepDone = 0;
	
// -output DIR -package PKGNAME IntentSpecFile
	public static void main(String[] args)
	{
		try{
			IntentSpec = args[args.length-1];
			for(int i = 0; i < args.length; i+= 2)
			{
				if(args[i].equals("-output"))
				{
					outputdir = args[i+1];
				}
				else if(args[i].equals("-package"))
				{
					pkgName = args[i+1];
				}
				else if(args[i].equals("-internal"))
				{
					InternalIS = args[i+1];
				}
				else if(args[i].equals("-mode"))
				{
					mode = Integer.valueOf(args[i+1]);
				}
			}	
		}
		catch(Exception e){
			System.out.println("Wrong Command");
			return;
		}

		if(outputdir == null)
		{
			outputdir = System.getProperty("user.dir") + "/test/" + pkgName + "/";
		}
//		MakeTestArtifacts(command);
		
		GenAndroidTestCodeCommand GATCC = new GenAndroidTestCodeCommand(); 
		
		// Step 1: Make testcode from .randomis file
		GATCC.MakeTestCodeFromRandomis();
		
		// Step 2: Make testcode from Internal intentspec
		GATCC.MakeTestCodeFromInternalIS();
		
		// Step 3: save testcode
		GATCC.saveAndroidTestCode();
	}
	
	public synchronized void MakeTestCodeFromRandomis()
	{
		String command = System.getProperty("user.dir") + "/../GenTestsfromIntentSpec/bin/" + genCommand + 
				" AndroidTestCodeFromRandomis 0 3 -ftmp " + IntentSpec + " " + pkgName;
		System.out.println("Run : " + command);
		ExecuteShellCommand.executeMakeTestArtifacts(GenAndroidTestCodeCommand.this, command);
	}
	
	public synchronized void MakeTestCodeFromInternalIS()
	{
		while(StepDone != 1){
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(InternalIS != null)
		{
			String command = System.getProperty("user.dir") + "/../GenTestsfromIntentSpec/bin/" + genCommand + 
					" AndroidTestCode " + mode + " 1 -f " + InternalIS + " " + pkgName;
			System.out.println("Run : " + command);
			ExecuteShellCommand.executeMakeTestArtifacts(GenAndroidTestCodeCommand.this, command);
		}
		else
		{
			StepDone = 2;
			notifyAll();
		}
	}
	
	public synchronized void saveAndroidTestCode() {

		while(StepDone != 2)
		{
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// Create the directory
		String pkgdir = pkgName.replaceAll("\\.", "/") + "/";
		String outdir = outputdir + "/";
		String dirtosave = outdir + pkgdir;

		File f_dirtosave = new File(dirtosave);
		if (f_dirtosave.exists() == false) {
			f_dirtosave.mkdirs();
		}

		// Write Java files
		BufferedWriter bw = null;
		try {
			Scanner scan = new Scanner(new StringReader(TestCode));
			String pkgpathClzDotJava = null;

			// Find '#'
			while (scan.hasNextLine()) {
				String line = scan.nextLine();

				if (line.charAt(0) == '#') {
					pkgpathClzDotJava = line.substring(1);
					break;
				}
			}

			while (scan.hasNextLine()) {				

				if (pkgpathClzDotJava == null) break; // End of text

				// Save the file
				File f_filetosave = new File(outdir + pkgpathClzDotJava);
				pkgpathClzDotJava = null;

				bw = new BufferedWriter(new FileWriter(f_filetosave));

				while (scan.hasNextLine()) {
					String line = scan.nextLine();

					if (line.charAt(0) == '#') {
						pkgpathClzDotJava = line.substring(1);
						break;
					}
					else {
						bw.write(line + "\n");
					}
				}

				bw.close();
			}

			scan.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void appendTxt_testArtifacts(String str) {
		// TODO Auto-generated method stub
		TestCode += str;
	}

	@Override
	public synchronized void done_testArtifacts(boolean fail) {
		// TODO Auto-generated method stub
		StepDone++;
		notifyAll();
	}

	@Override
	public void appendTxt_intentSpec(String str) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void done_intentSpec() {
		// TODO Auto-generated method stub
		
	}
}
