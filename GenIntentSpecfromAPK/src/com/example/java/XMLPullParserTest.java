package com.example.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream.GetField;
import java.net.URL;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class XMLPullParserTest {
	static ArrayList<intentfilter> intentfiltersforActivity = new ArrayList<intentfilter>();
	static ArrayList<intentfilter> intentfiltersforService = new ArrayList<intentfilter>();
	static ArrayList<intentfilter> intentfiltersforReceiver = new ArrayList<intentfilter>();
	static int context;
	static int count = 0;

	static void find(String manifestpath) {
		intentfiltersforActivity.clear();
		intentfiltersforService.clear();
		intentfiltersforReceiver.clear();

		try {

			FileInputStream url = new FileInputStream(manifestpath);

			XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = parserFactory.newPullParser();
			parser.setInput(url, "utf-8");
			String name = null, manifest = null, typ = null, cmp = null;

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
						System.out.println(manifest);
						System.out.println("");
					}
					if ("activity".equals(name) || "receiver".equals(name) || "service".equals(name)) {
						if ("activity".equals(name)) {
							context = 1;
						} else if ("receiver".equals(name)) {
							context = 3;
						} else {
							context = 2;
						}

						cmp = parser.getAttributeValue(null, "android:name");
						if (cmp.substring(0, 1).equals(".")) { // android:name =.MyActivity
							cmp = manifest + "/" + cmp;
						} else if (cmp.startsWith(manifest)) { // android:name =com.example.java.MyActity 패키지명 일
							cmp = manifest + "/" + cmp;
						} else if (cmp.contains(".")) { 		// android:name =com.example.java.MyActivity패키지명다
							cmp = manifest + "/" + cmp;
						} else { 								// android:name = MyActivity
							cmp = manifest + "/" + "." + cmp;
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
					break;
				case XmlPullParser.TEXT:
					break;
				case XmlPullParser.END_TAG:
					if (name.equals("intent-filter")) {
						for (int i = 0; i < action_count; i++) { 
							String t_category = "";
							for (int m = 0; m < category_count; m++) { 
								if (m > 0) {    //시작할때에는 , 를 안찍기 위해서 
									t_category += ", ";
								}
								t_category += category.get(m);
							}

							if (context == 1) {
								intentfilter filter = new intentfilter(cmp,action.get(i), t_category);
								intentfiltersforActivity.add(filter);
							} else if (context == 2) {
								intentfilter filter = new intentfilter(cmp,action.get(i), t_category);
								intentfiltersforService.add(filter);
							} else {
								intentfilter filter = new intentfilter(cmp,action.get(i), t_category);
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

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < intentfiltersforActivity.size(); i++) {

			System.out.println(intentfiltersforActivity.get(i));
			if (i != intentfiltersforActivity.size() - 1) {
				System.out.println(" || ");
			}
			count++;
		}
		
		for (int i = 0; i < intentfiltersforService.size(); i++) {

			System.out.println(intentfiltersforService.get(i));
			if (i != intentfiltersforService.size() - 1) {
				System.out.println(" || ");
			}
			count++;
		}
	
		for (int i = 0; i < intentfiltersforReceiver.size(); i++) {

			System.out.println(intentfiltersforReceiver.get(i));
			if (i != intentfiltersforReceiver.size() - 1) {
				System.out.println(" || ");
			}
			count++;
		}
		System.out.println("Activity");
		System.out.println(intentfiltersforActivity.size());
		System.out.println("Service");
		System.out.println(intentfiltersforService.size());
		System.out.println("Receiver");
		System.out.println(intentfiltersforReceiver.size());
	}

	public static void main(String[] args) {

		//xml파일이 있는 경로
		String filePath;
		filePath ="/Users/Yoonsungbin/Desktop/apk/apk모음/카카오톡4.8.2/AndroidManifest.xml"; 
		find(filePath);

		System.out.println("Total count = " +count);

	}
}
