package kr_ac_yonsei_mobilesw_UI;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

public class Config {
	private static final String configFile = "config.properties";
	
	private static final String keyAdbPath = "adbpath";
	private static final String keyImportPath = "importpath";
	
	public static void putAdbPath(String adbPath) {
		put(keyAdbPath, adbPath);
	}
	
	public static String getAdbPath() {
		return get(keyAdbPath);
	}
	
	public static void putImportPath(String importPath) {
		put(keyImportPath, importPath);
	}
	
	public static String getImportPath() {
		return get(keyImportPath);
	}
	
	public static void put(String key, String adbPath) {
		Properties prop = readAll();
		OutputStream output = null;
		
		try {
			output = new FileOutputStream(configFile);
			
			prop.setProperty(key, adbPath);
			
			prop.store(output, null);
		}
		catch(IOException io) {
		}
		finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	public static String get(String key) {
		Properties prop = readAll();
		return prop.getProperty(key);
	}
	
	public static Properties readAll() {
		Properties prop = new Properties();
		InputStream input = null;
		
		try {
			input = new FileInputStream(configFile);
			
			prop.load(input);
		}
		catch(IOException ex) {
		}
		finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}	
		}
		return prop;
	}
}
