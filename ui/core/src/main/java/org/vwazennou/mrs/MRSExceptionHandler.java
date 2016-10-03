/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;

import org.vwazennou.mrs.data.Database;
import org.vwazennou.mrs.ui.MRSInterface;

import com.datamininglab.commons.logging.LogContext;

public class MRSExceptionHandler implements UncaughtExceptionHandler {
	private MRSInterface ui;
	
	public MRSExceptionHandler(MRSInterface ui) { this.ui = ui; }
	
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		if (ui != null) { ui.uncaughtException(e); }
		
		LogContext.severe(e, "Critical error");
		
		File dir = Database.getClientDirectory();
		File log = new File(dir, String.format("mrs-error-%F.log", new Date()));
		try (PrintStream ps = new PrintStream(new FileOutputStream(log, true))) {
			ps.print(new Date() + ": ");
			e.printStackTrace(ps);
		} catch (FileNotFoundException e1) {
			LogContext.warning(e1, "Warning: could not log exception to file");
		}
	}
}
