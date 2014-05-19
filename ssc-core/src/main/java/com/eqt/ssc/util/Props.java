package com.eqt.ssc.util;

import java.io.IOException;
import java.util.Properties;

public class Props {
	
	private static Properties props = new Properties();
	
	static {
		try {
			props.load(Props.class.getResourceAsStream("/ssc.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String getProp(String key) {
		return props.getProperty(key);
	}
	
	public static int getPropInt(String key, String defaultValue) {
		return Integer.parseInt(props.getProperty(key,defaultValue));
	}
	
	
	public static String getProp(String key, String defaultValue) {
		return props.getProperty(key, defaultValue);
	}
	
	public static String getProp(String key, String defaultKey, String defaultValue) {
		if(props.containsKey(key))
			return props.getProperty(key);
		else
			return props.getProperty(defaultKey, defaultValue);
	}

	public static int getPropInt(String key, String defaultKey, String defaultValue) {
		if(props.containsKey(key))
			return Integer.parseInt(props.getProperty(key));
		else
			return Integer.parseInt(props.getProperty(defaultKey, defaultValue));
	}	
}
