/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.task.excel;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.vwazennou.mrs.dictionary.Str;

import com.datamininglab.commons.lang.StatusMonitor;
import com.datamininglab.commons.logging.LogContext;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public abstract class ExcelReport {
	public void export(StatusMonitor sm, String file) {
		sm.newTask(Str.EXPORTING + " " + file + "...");
		
		File f = new File(file);
		WorkbookSettings wbs = new WorkbookSettings();
	    wbs.setLocale(Locale.getDefault());
	    
	    WritableWorkbook wb = null;
	    try {
	    	wb = Workbook.createWorkbook(f, wbs);
	    	export(sm, wb);
	    } catch (RowsExceededException ex) {
	    	sm.setError(Str.ERROR_TOO_MANY_ROWS.toString());
	    } catch (WriteException | IOException ex) {
	    	error(sm, ex); return;
	    }
	    
	    try {
	    	wb.write();
    		wb.close();
	    } catch (WriteException | IOException ex) {
	    	error(sm, ex); return;
	    }
    	
	    try {
			Desktop.getDesktop().open(f);
		} catch (IOException e) {
			LogContext.warning(e, "Unable to automatically open exported workbook");
		}
	}
	
	private static void error(StatusMonitor sm, Exception ex) {
		sm.setError(Str.ERROR_EXPORT_TO_EXCEL + ex.getMessage());
	}
	
	protected abstract void export(StatusMonitor sm, WritableWorkbook wb) throws IOException, WriteException;
}
