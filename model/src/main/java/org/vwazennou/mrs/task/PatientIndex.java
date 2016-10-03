/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.task;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.vwazennou.mrs.data.Database;
import org.vwazennou.mrs.dictionary.Str;
import org.vwazennou.mrs.patient.Patient;

import com.datamininglab.commons.logging.LogContext;
import com.datamininglab.foundation.ui.StatusMonitor;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class PatientIndex {
	private String file;
	
	public PatientIndex(String file) {
		this.file = file;
	}
	
	public void generate(StatusMonitor sm) {
		sm.newTask(Str.EXPORTING + " " + file + "...");
		
        Document document = new Document();
        try (FileOutputStream fos = new FileOutputStream(file)) {
			PdfWriter.getInstance(document, fos);
			document.setMargins(30.0f, 30.0f, 10.0f, 10.0f);
	        document.open();
	        generate(sm, document);
	        document.close();
		} catch (IOException | DocumentException e) {
			LogContext.warning(e, "Error creating PDF report");
			sm.setError("Error generating patient index: " + e.getMessage());
			return;
		}

        try {
			Desktop.getDesktop().open(new File(file));
		} catch (IOException e) {
			LogContext.warning(e, "Unable to automatically open exported PDF");
		}
	}
	
	// TODO: Use Str
	private static void generate(StatusMonitor sm, Document d) throws DocumentException {
		d.addTitle("Haiti MRS Patient Index");
		
		PdfPTable table = new PdfPTable(5);
		table.setWidthPercentage(100.0f);
		table.setWidths(new float[] {10.0f, 10.0f, 30.0f, 35.0f, 15.0f});
		table.setHeaderRows(1);
		
		Phrase p = new Phrase();
		Font font = p.getFont();
		font.setSize(8.0f);
		
		Font bold = new Font(font);
		bold.setStyle(Font.BOLD);

		table.addCell("ID");
		table.addCell("Visits");
		table.addCell("Name");
		table.addCell("Address");
		table.addCell("Phone");
		
		PdfPCell cell = new PdfPCell(new Phrase("All patients- Surname first"));
		cell.setColspan(5);
		cell.setBackgroundColor(new BaseColor(100, 100, 100));
		table.addCell(cell);
		
		Session s = Database.connect();
		for (Object o : s.createCriteria(Patient.class)
		                  .addOrder(Order.asc("lastName").ignoreCase())
		                  .list()) {
			writePatient(table, (Patient) o, font, bold, true);
			if (!sm.isRunning()) { return; }
		}
		
		d.newPage();
		
		cell = new PdfPCell(new Phrase("All patients- First name first"));
		cell.setColspan(5);
		cell.setBackgroundColor(new BaseColor(100, 100, 100));
		table.addCell(cell);
		
		for (Object o : s.createCriteria(Patient.class)
                         .addOrder(Order.asc("firstName").ignoreCase())
                         .list()) {
			writePatient(table, (Patient) o, font, bold, false);
			if (!sm.isRunning()) { return; }
		}
		
		d.add(table);
		Database.disconnect(s);
	}
	
	private static void writePatient(PdfPTable table, Patient p, Font f, Font b, boolean lastFirst) {
		int visits = p.getVisits().size();
		if (visits == 0) { return; }
		
		PdfPCell cell = new PdfPCell(new Phrase("#" + p.getId(), b));
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(cell);
		
		cell = new PdfPCell(new Phrase(String.valueOf(visits), f));
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(cell);
		
		if (lastFirst) {
			table.addCell(new Phrase(p.getLastName() + ", " + p.getFirstName(), f));
		} else {
			table.addCell(new Phrase(p.getFirstName() + " " + p.getLastName(), f));
		}
		table.addCell(new Phrase(p.getAddress(), f));
		table.addCell(new Phrase(p.getPhone(), f));
	}
}
