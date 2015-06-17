package com.example.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream.GetField;
import java.net.URL;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class GenIntentSpecFromAPK {
	private static enum Type { All, Activity, Service, BroadcastReceiver };
	
	private static Type requestType;
	
	private static ArrayList<IntentFilter> intentfiltersforActivity = new ArrayList<IntentFilter>();
	private static ArrayList<IntentFilter> intentfiltersforService = new ArrayList<IntentFilter>();
	private static ArrayList<IntentFilter> intentfiltersforReceiver = new ArrayList<IntentFilter>();
	private static int context;
	private static int count = 0;

	private static void find(String manifestpath) {
		intentfiltersforActivity.clear();
		intentfiltersforService.clear();
		intentfiltersforReceiver.clear();

		try {

			FileInputStream url = new FileInputStream(manifestpath);

			XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = parserFactory.newPullParser();
			parser.setInput(url, "utf-8");
			String name = null, manifest = null, typ = null, cmp = null, cmptype = null;

			ArrayList<String> action = new ArrayList<String>();
			ArrayList<String> category = new ArrayList<String>();

			int parserEvent = parser.getEventType();
			int action_count = 0;
			int category_count = 0;

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
					break;
				case XmlPullParser.TEXT:
					break;
				case XmlPullParser.END_TAG:
					if (name.equals("intent-filter")) {
						for (int i = 0; i < action_count; i++) { 
							String t_category = "";
							for (int m = 0; m < category_count; m++) { 
								if (m > 0) {    // when multiple categories exist 
									t_category += ", ";
								}
								t_category += category.get(m);
							}

							if (context == 1) {
								IntentFilter filter = new IntentFilter(cmp,action.get(i), t_category);
								intentfiltersforActivity.add(filter);
							} else if (context == 2) {
								IntentFilter filter = new IntentFilter(cmp,action.get(i), t_category);
								intentfiltersforService.add(filter);
							} else {
								IntentFilter filter = new IntentFilter(cmp,action.get(i), t_category);
								intentfiltersforReceiver.add(filter);
							}

						}
						action_count = 0;
						category_count = 0;
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
		
		for (int i = 0; i < intentfilters.size(); i++) {
			
			System.out.println(intentfilters.get(i));
			if (i != intentfilters.size() - 1) {
				System.out.println(" || ");
			}
			count++;
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

	public static void main(String[] args) {

		requestType = Type.All;
		String apkfile;
		
		if (args.length >= 1) {		
		
			apkfile = "\"" + args[0] + "\"";	// ' ' in the file name
			
			try {
				String currentWorkingDir = new File("").getAbsolutePath();
				if (DEBUG) System.out.println("CWD: " + currentWorkingDir);
				
				String command = currentWorkingDir + "/../GenIntentSpecfromAPK/lib/" + apktool + " d -f " + apkfile;
				
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
					
					
					Process p = Runtime.getRuntime().exec(command);
					
					while (p.isAlive()) {
						BufferedReader reader = 
								new BufferedReader(
										new InputStreamReader(p.getInputStream()));
						String line = "";
						
						while ((line = reader.readLine())!= null) 
						{
							if(line.equals("") == false)
							{
								if (DEBUG) System.out.println(line);
							}
						}
		
					}
					
					find(xmlfile);
			
					clearFolders(new File(outputDir));
				}
				
			} catch (IOException e) {
				e.printStackTrace();
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
}
