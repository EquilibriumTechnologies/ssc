package com.eqt.ssc.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.curator.utils.CloseableUtils;

public class Props {
	
	private static Properties props = new Properties();
	
	static {
		InputStream defaultProps = null;
		InputStream customProps = null;
		try {
			defaultProps = Props.class.getResourceAsStream("/ssc-default.properties");
			props.load(defaultProps);
			
			customProps = Props.class.getResourceAsStream("/ssc.properties");
			if(customProps == null)
				throw new FileNotFoundException("cannot find ssc.properties");
			
			props.load(customProps);
		} catch (IOException e) {
			throw new IllegalStateException("cannot load the properties files",e);

		} finally {
			CloseableUtils.closeQuietly(defaultProps);
			CloseableUtils.closeQuietly(customProps);
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
