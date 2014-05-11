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
	
	public static String getProp(String key, String defaultValue) {
		return props.getProperty(key, defaultValue);
	}
}
