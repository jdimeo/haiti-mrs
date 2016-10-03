/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs;

import org.vwazennou.mrs.ui.MRSInterface;
import org.vwazennou.mrs.ui.swt.SWTInterface;

public final class HaitiMRS {
	public static final String[] URLS = {
		"http://vwazennou.org",
		"http://gracenetwork.org"
	};
	
	public static final String VERSION;
	static {
		String v = HaitiMRS.class.getPackage().getImplementationVersion();
		VERSION = v == null? "Live Build" : v;
	}
	
	public static final String CONTACT = "haiti.mrs@gmail.com";
	
	private HaitiMRS() {
		// Prevent initialization
	}
	
	public static void main(String[] args) {
		MRSInterface ui = new SWTInterface(new MRSController());
		Thread.setDefaultUncaughtExceptionHandler(new MRSExceptionHandler(ui));
		ui.getController().start();
	}
}