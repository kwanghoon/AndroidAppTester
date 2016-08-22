package com.example.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream.GetField;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import kr_ac_yonsei_mobilesw_UI.BenchCommand;
import kr_ac_yonsei_mobilesw_UI.Config;
import kr_ac_yonsei_mobilesw_UI.JavaCommand;


public class GenIntentSpecFromAPK {
	private static String osNameStr="windows";
	private static String adbPathString ="";
	private static boolean Apkflag = false;
	private static boolean internalFlag = false;
	
	static {
		String osName = System.getProperty("os.name");
		String osNameMatch = osName.toLowerCase();

		if(osNameMatch.contains("linux")) {
			osNameStr = "linux";
		} else if(osNameMatch.contains("windows")) {
			osNameStr = "windows";
		} else if(osNameMatch.contains("mac os") || osNameMatch.contains("macos") || osNameMatch.contains("darwin")) {
			osNameStr = "mac";
		}else {
			osNameStr = "windows"; // Windows OS by default
		}	
	}

	static {
		adbPathString = Config.getAdbPath();

		if (adbPathString == null) {
			String osName = System.getProperty("os.name");
			String osNameMatch = osName.toLowerCase();
			if(osNameMatch.contains("linux")) {
				adbPathString = "/home/";
			} else if(osNameMatch.contains("windows")) {
				adbPathString = "C:/users/";
			} else if(osNameMatch.contains("mac os") || osNameMatch.contains("macos") || osNameMatch.contains("darwin")) {
				adbPathString = "/Users/";
			} else {
				adbPathString = "C:/users/"; // Windows OS by default
			}
		}
	}
	
	private static enum Type { All, Activity, Service, BroadcastReceiver };

	private static Type requestType;
	private static boolean console = false;
	private static ArrayList<IntentFilter> intentfiltersforActivity = new ArrayList<IntentFilter>();
	private static ArrayList<IntentFilter> intentfiltersforService = new ArrayList<IntentFilter>();
	private static ArrayList<IntentFilter> intentfiltersforReceiver = new ArrayList<IntentFilter>();
	private static int context;
	private static int count = 0;
	private static String manifest = null;

	private static void find(String manifestpath) {
		intentfiltersforActivity.clear();
		intentfiltersforService.clear();
		intentfiltersforReceiver.clear();

		try {

			FileInputStream url = new FileInputStream(manifestpath);		
			XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = parserFactory.newPullParser();
			parser.setInput(url, "utf-8");
			String name = null, typ = null, cmp = null, cmptype = null;
			Boolean exported = null;
			boolean hasExportedAttr = false;
			boolean hasIntentFilter = false;

			ArrayList<String> action = new ArrayList<String>();
			ArrayList<String> category = new ArrayList<String>();

			int parserEvent = parser.getEventType();
			int action_count = 0;
			int category_count = 0;
			boolean componentwithoutintentfilter = true;

			while (parserEvent != XmlPullParser.END_DOCUMENT) {

				name = parser.getName();
				switch (parserEvent) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:

					if ("manifest".equals(name)) {
						manifest = parser.getAttributeValue(null, "package");
						if (DEBUG) System.out.println(manifest);
						if (DEBUG) System.out.println("");
					}
					if ("activity".equals(name) || "receiver".equals(name) || "service".equals(name)) {
						componentwithoutintentfilter = true;

						exported = null;
						hasExportedAttr = false;
						hasIntentFilter = false;

						if ("activity".equals(name)) {
							context = 1;
							cmptype = "Activity ";
						} else if ("receiver".equals(name)) {
							context = 3;
							cmptype = "BroadcastReceiver ";
						} else {
							context = 2;
							cmptype = "Service ";
						}

						cmp = parser.getAttributeValue(null, "android:name");
						if (cmp.substring(0, 1).equals(".")) { // android:name =.MyActivity
							cmp = manifest + "/" + cmp;
						} else if (cmp.startsWith(manifest)) { // android:name =com.example.java.MyActitvity 
							cmp = manifest + "/" + cmp;
						} else if (cmp.contains(".")) { 		// android:name =com.example.java.MyActivity
							cmp = manifest + "/" + cmp;
						} else { 								// android:name = MyActivity
							cmp = manifest + "/" + "." + cmp;
						}

						cmp = cmptype + cmp;

						String b = parser.getAttributeValue(null, "android:exported");
						if(b == null){
							hasExportedAttr = false;
						}
						else if(b.compareToIgnoreCase("true") == 0){
							hasExportedAttr = true;
							exported = true;
						}
						else if(b.compareToIgnoreCase("false") == 0){
							hasExportedAttr = true;
							exported = false;	
						}
						else{
							hasExportedAttr = false;
						}
					}

					if ("action".equals(name)) {
						action.add(parser.getAttributeValue(null,"android:name"));
						action_count++;
					}
					if ("category".equals(name)) {
						category.add(parser.getAttributeValue(null,"android:name"));
						category_count++;
					}
					if ("data".equals(name)) {
						typ = parser.getAttributeValue(null, "android:mimeType");
					}
					if ("intent-filter".equals(name)) {
						hasIntentFilter = true;
						if(hasExportedAttr == false){
							exported = true;
						}
					}
					break;
				case XmlPullParser.TEXT:
					break;
				case XmlPullParser.END_TAG:
					if ("intent-filter".equals(name)) {
						componentwithoutintentfilter = false;

						for (int i = 0; i < action_count; i++) { 
							String t_category = "";
							for (int m = 0; m < category_count; m++) { 
								if (m > 0) {    // when multiple categories exist 
									t_category += ", ";
								}
								t_category += category.get(m);
							}

							if (context == 1) {
								IntentFilter filter = new IntentFilter(cmp,action.get(i), t_category, exported);
								intentfiltersforActivity.add(filter);
							} else if (context == 2) {
								IntentFilter filter = new IntentFilter(cmp,action.get(i), t_category, exported);
								intentfiltersforService.add(filter);
							} else {
								IntentFilter filter = new IntentFilter(cmp,action.get(i), t_category, exported);
								intentfiltersforReceiver.add(filter);
							}

						}
						action_count = 0;
						category_count = 0;
					}

					if ("activity".equals(name) || "receiver".equals(name) || "service".equals(name)) {
						if (componentwithoutintentfilter == true) {

							if(hasExportedAttr == false){					
								if(hasIntentFilter == true){								
									exported = true;
								}
								else{
									exported = false;
								}
							}				

							// Found a component declaration without any intent filters.


							String t_category = "";
							String t_action  = "";
							if (context == 1) {
								IntentFilter filter = new IntentFilter(cmp, t_action, t_category, exported);
								intentfiltersforActivity.add(filter);
							} else if (context == 2) {
								IntentFilter filter = new IntentFilter(cmp, t_action, t_category, exported);
								intentfiltersforService.add(filter);
							} else {
								IntentFilter filter = new IntentFilter(cmp, t_action, t_category, exported);
								intentfiltersforReceiver.add(filter);
							}
						}
					}
					break;
				case XmlPullParser.END_DOCUMENT:
					break;
				}

				parserEvent = parser.next();
			}

			url.close();  // MUST close the XML file! 
			// Otherwise, it can't be deleted later.

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		ArrayList<IntentFilter> intentfilters = new ArrayList<IntentFilter>();

		if (requestType == Type.All || requestType == Type.Activity) {
			for (IntentFilter i : intentfiltersforActivity) {
				intentfilters.add(i);
			}

			if (DEBUG) System.out.println("Activity");
			if (DEBUG) System.out.println(intentfiltersforActivity.size());
		}

		if (requestType == Type.All || requestType == Type.Service) {
			for (IntentFilter i : intentfiltersforService) {
				intentfilters.add(i);
			}

			if (DEBUG) System.out.println("Service");
			if (DEBUG) System.out.println(intentfiltersforService.size());
		}

		if (requestType == Type.All || requestType == Type.BroadcastReceiver) {
			for (IntentFilter i : intentfiltersforReceiver) {
				intentfilters.add(i);
			}

			if (DEBUG) System.out.println("Receiver");
			if (DEBUG) System.out.println(intentfiltersforReceiver.size());
		}
		try {
			FileWriter output = new FileWriter(new File(manifest + ".is"));
//			FileWriter InternalOutput = new FileWriter(new File("Internal_" + manifest + ".is"));
			
			for (int i = 0; i < intentfilters.size(); i++) {
				if(console){
					output.write(intentfilters.get(i).toString());
//					if( intentfilters.get(i).isInternal()){
//						if(internalFlag == false){
//							internalFlag = true;
//							InternalOutput.write(intentfilters.get(i).toStringIfInternal());
//						}
//						else{
//							InternalOutput.write("\n || \n");
//							InternalOutput.write(intentfilters.get(i).toStringIfInternal());
//						}
//					}
				}
				System.out.println(intentfilters.get(i));
				if (i != intentfilters.size() - 1) {
					if(console){
						output.write("\n || \n");
					}
					System.out.println(" || ");
				}
				count++;
			}
//			InternalOutput.close();
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		//		if (requestType == Type.All || requestType == Type.Activity) {
		//			for (int i = 0; i < intentfiltersforActivity.size(); i++) {
		//	
		//				System.out.println(intentfiltersforActivity.get(i));
		//				if (i != intentfiltersforActivity.size() - 1) {
		//					System.out.println(" || ");
		//				}
		//				count++;
		//			}
		//			
		//			if (DEBUG) System.out.println("Activity");
		//			if (DEBUG) System.out.println(intentfiltersforActivity.size());
		//		}
		//		
		//		if (requestType == Type.All || requestType == Type.Service) {
		//			for (int i = 0; i < intentfiltersforService.size(); i++) {
		//	
		//				System.out.println(intentfiltersforService.get(i));
		//				if (i != intentfiltersforService.size() - 1) {
		//					System.out.println(" || ");
		//				}
		//				count++;
		//			}
		//			
		//			if (DEBUG) System.out.println("Service");
		//			if (DEBUG) System.out.println(intentfiltersforService.size());
		//		}
		//	
		//		if (requestType == Type.All || requestType == Type.BroadcastReceiver) {
		//			for (int i = 0; i < intentfiltersforReceiver.size(); i++) {
		//	
		//				System.out.println(intentfiltersforReceiver.get(i));
		//				if (i != intentfiltersforReceiver.size() - 1) {
		//					System.out.println(" || ");
		//				}
		//				count++;
		//			}
		//			
		//			if (DEBUG) System.out.println("Receiver");
		//			if (DEBUG) System.out.println(intentfiltersforReceiver.size());
		//		}

		if (DEBUG) System.out.println("Total count = " +count);
	}

	/*
	 *  INPUT: A path to an APK file
	 *      
	 *  OUTPUT: Intent Specification
	 */
	private final static boolean DEBUG = false;

	private static String apktool;

	static {
		String osName = System.getProperty("os.name");
		String osNameMatch = osName.toLowerCase();
		if(osNameMatch.contains("linux")) {
			apktool = "apktool_linux";
		} else if(osNameMatch.contains("windows")) {
			apktool = "apktool_win.bat";
		} else if(osNameMatch.contains("mac os") || osNameMatch.contains("macos") || osNameMatch.contains("darwin")) {
			apktool = "apktool_mac";
		}else {
			apktool = "apktool_win.bat"; // Windows OS by default
		}
	}

	//java -cp "bin;lib/*;tool/*" com.example.java.GenIntentSpecFromAPK -ui(-console) C:\NotesList
	//java -cp "bin;lib/*;tool/*" com.example.java.GenIntentSpecFromAPK -ui(-console) C:\NotesList\src\main\AndroidManifest.xml
	//java -cp "bin;lib/*;tool/*" com.example.java.GenIntentSpecFromAPK -ui(-console) C:\NotesList\NoteList.apk

	public static void main(String[] args) {

		requestType = Type.All;
		String apkfile = "";
		
		if (args.length >= 1) {		
			try {
				if(args[0].equals("-ui"))
				{
					console = false;
				}
				else if(args[0].equals("-console"))
				{
					console = true;
				}
				else
				{
					System.out.println("Wrong Command");
					return;
				}
				apkfile = "\"" + args[1] + "\"";	// ' ' in the file name
				File f = new File(args[1]); // project dir
				if(f.isDirectory())
				{
					apkfile = "\"" + findManifest(f) + "\"";
					if(apkfile.equals("\"\""))
					{
						apkfile = "\"" + findApk(f) + "\"";
					}
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				String currentWorkingDir = new File("").getAbsolutePath();
				if (DEBUG) System.out.println("CWD: " + currentWorkingDir);

				String command = currentWorkingDir + "/tool/" + apktool + " d -f " + apkfile;

				if (DEBUG) System.out.println("CMD: " + command);



				
				String filenamewithExt = new File(apkfile).getName();

				if (DEBUG) System.out.println("NAMEEXT: " + filenamewithExt);

				String filename = filenamewithExt.substring(0, filenamewithExt.lastIndexOf('.'));

				if (DEBUG) System.out.println("NAME: " + filename);

				String outputDir = currentWorkingDir + "/" + filename;

				if (DEBUG) System.out.println("OUTPUT: " + outputDir);

				String xmlfile;

				if (filenamewithExt.startsWith("AndroidManifest.xml")) {
					xmlfile = apkfile.substring(1, apkfile.length()-1);

					find(xmlfile);
				}
				else {
					xmlfile = outputDir + "/AndroidManifest.xml";

					if (DEBUG) System.out.println("XML: " + xmlfile);

					Process p;

					if ("mac".equals(osNameStr)) {
						p = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", command });
					}
					else {
						p = Runtime.getRuntime().exec(command);
					}

					while (p.isAlive()) {
						BufferedReader reader = 
								new BufferedReader(
										new InputStreamReader(p.getErrorStream()));
						String line = "";

						while ((line = reader.readLine())!= null) 
						{
							if(line.equals("") == false)
							{
								/*if (DEBUG)*/ System.out.println(line);
							}
						}
					}

					find(xmlfile);					
					clearFolders(new File(outputDir));
					Apkflag = true;
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (StringIndexOutOfBoundsException e){
				e.printStackTrace();
				return;
			}
		}
	}

	public static void clearFolders(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files!=null) {
				for (File subfile : files) {
					clearFolders(subfile);
				}
			}
		}
		boolean flag = file.delete();
		if(DEBUG) System.out.println("Delete " + file.getAbsolutePath() + " " + flag);
	}

	public static String findManifest(File f)
	{
		String name = "";
		File[] filelists = f.listFiles();
		for(File e:filelists)
		{
			if(e.isDirectory() && !e.getName().equals("debug"))
			{
				name = findManifest(e);
				if(name.endsWith("AndroidManifest.xml"))
				{
					return name;
				}
			}
			else if(e.getName().equals("AndroidManifest.xml"))
			{
				return e.getAbsolutePath();
			}
		}
		return name;
	}

	public static String findApk(File f)
	{
		String name = "";
		File[] filelists = f.listFiles();
		for(File e:filelists)
		{
			if(e.isDirectory())
			{
				name = findManifest(e);
				if(name.endsWith(".apk"))
				{
					return name;
				}
			}
			else if(e.getName().endsWith(".apk"))
			{
				return e.getAbsolutePath();
			}
		}
		return name;
	}
	
	public static boolean isApk()
	{
		return Apkflag;
	}
	
	public static String getPkgName()
	{
		return manifest;
	}
	
	public static boolean existInternal()
	{
		return internalFlag;
	}
}