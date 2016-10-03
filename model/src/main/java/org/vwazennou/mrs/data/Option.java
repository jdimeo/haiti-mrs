/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.apache.commons.lang3.SystemUtils;

import com.datamininglab.commons.logging.LogContext;

public enum Option {
	CLIENT_NAME(null),
	CLIENT_ID(null),
	DATA_PATH(SystemUtils.USER_HOME + File.separator
	        + "Dropbox"             + File.separator
	        + "Haiti LADS"          + File.separator
	        + "Data"                + File.separator),
	LANGUAGE("English");
	
	private static final String PREF_DIR = ".hmrs";
	
	private Object defval;
	private Object val;
	Option(Object o) { defval = o; }
	
	@Override
	public String toString() {
		parse(); return val.toString();
	}
	
	public boolean isOn() {
		if (parse()) { val = Boolean.parseBoolean(val.toString()); }
		return (Boolean) val;
	}
	public long longValue() {
		if (parse()) { val = Long.valueOf(val.toString()); }
		return (Long) val;
	}
	
	private boolean parse() {
		if (val != null) { return false; }
		
		val = props.getProperty(name());
		if (val == null) {
			val = defval; return false;
		}
		return true;
	}
	
	public void set(Object value) { val = value; }
	
	private static File configFile;
	private static Properties props = new Properties();
	static {
		CLIENT_ID.defval = System.currentTimeMillis();
		CLIENT_NAME.defval = SystemUtils.USER_NAME + "-" + CLIENT_ID.defval;
		
		Path userDir = Paths.get(SystemUtils.USER_HOME);
		Path hmrsDir = userDir.resolve(PREF_DIR);
		configFile = hmrsDir.resolve("config.ini").toFile();

		try {
			Files.createDirectories(hmrsDir);
			Files.setAttribute(hmrsDir, "dos:hidden", true);
		} catch (IOException e) {
			LogContext.warning(e, "Error creating config dir");
		}
		
		try (FileInputStream fis = new FileInputStream(configFile)) {
			props.load(fis);
		} catch (IOException | UnsupportedOperationException | NoSuchElementException e) {
			LogContext.warning(e, "Could not load configuration");
		}
	}
	
	/**
	 * Checks if the the current data path (either the default data path or the one
	 * specified by the user in the config file) is valid (i.e. exists, and contains
	 * all the required files).
	 * @return if the data path is valid
	 */
	public static boolean isDataPathValid() { 
		File dir = new File(Option.DATA_PATH.toString());
		if (!dir.exists()) { return false; }
		
		return new File(dir, Database.MASTER_FILE).exists();
	}
	
	/** Saves the current option set to disk in a configuration file. */
	public static void store() {
		Option[] arr = Option.values();
		for (int i = 0; i < arr.length; i++) {
			props.setProperty(arr[i].name(), arr[i].toString());
		}
		try (FileOutputStream fos = new FileOutputStream(configFile)) {
			props.store(fos, "Haiti MRS configuration file");
		} catch (IOException e) {
			LogContext.warning(e, "Could not save configuration");
		}
	}
}
