/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.task.excel;

import java.io.IOException;
import java.util.List;

import jxl.SheetSettings;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.CellFormat;
import jxl.format.Colour;
import jxl.write.DateFormat;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.vwazennou.mrs.data.Database;
import org.vwazennou.mrs.dictionary.Language;
import org.vwazennou.mrs.patient.Patient;
import org.vwazennou.mrs.script.Prescription;
import org.vwazennou.mrs.visit.ClinicTeam;
import org.vwazennou.mrs.visit.ClinicTeam.ClinicType;
import org.vwazennou.mrs.visit.Visit;
import org.vwazennou.mrs.visit.VisitText;
import org.vwazennou.mrs.visit.VisitText.VisitTextType;

import com.datamininglab.foundation.text.UnicodeChars;
import com.datamininglab.foundation.ui.StatusMonitor;

public class DataExport extends ExcelReport {
	private static final int FONT_SIZE  = 9;
	private static final int MAX_VISITS = 6;
	
	public enum SortOrder {
		ID, LAST_NAME, FIRST_NAME
	}
	public enum ExportType {
		FULL_DATA, PATIENTS_ONLY, LADS
	}
	
	// Formats are assigned a workbook-specific ID so we cannot reuse instances across workbooks
	private CellFormat smallFont = new WritableCellFormat(new WritableFont(WritableFont.TAHOMA, FONT_SIZE));
	private CellFormat boldFont  = new WritableCellFormat(new WritableFont(WritableFont.TAHOMA, FONT_SIZE, WritableFont.BOLD));
	private CellFormat dates     = new WritableCellFormat(new DateFormat("yyyy/MM/dd"));
	private WritableCellFormat wrapped    = new WritableCellFormat();
	private WritableCellFormat patientRow = new WritableCellFormat();
	private WritableSheet ws;
	
	private ExportType type;
	private SortOrder order;
	
	private Language currLange;
	public DataExport(Language currLang, ExportType type, SortOrder order) {
		this.currLange = currLang;
		this.type      = type;
		this.order     = order;
	}
	
	@Override
	protected void export(StatusMonitor sm, WritableWorkbook wb) throws IOException, WriteException {
		wrapped.setWrap(true);
		wrapped.setShrinkToFit(true);
		
		patientRow.setBackground(Colour.GRAY_25);
		patientRow.setBorder(Border.TOP, BorderLineStyle.MEDIUM);
		
		ws = wb.createSheet("Medical Records", 0);
	    SheetSettings wss = ws.getSettings();
	    wss.setTopMargin(0.2);
	    wss.setBottomMargin(0.2);
	    wss.setLeftMargin(0.2);
	    wss.setRightMargin(0.2);
	    wss.setFitToPages(true);
	    wss.setFitWidth(1);
	    
	    ws.addCell(new Label(0, 0, "Note: Only prescriptions from the most recent clinic visit are shown", boldFont));
	    ws.mergeCells(0, 0, 11, 0);
	    
		Session s = Database.connect();
		Criteria c = s.createCriteria(Patient.class);
		switch (order) {
			case LAST_NAME:  c.addOrder(Order.asc("lastName"));  break;
			case FIRST_NAME: c.addOrder(Order.asc("firstName")); break;
			default:         c.addOrder(Order.asc("id"));        break;
		}
		List<?> pl = c.list();
		
		if (!sm.isRunning()) {
			Database.disconnect(s);
			return;
		}
		
		int row = 1;
		sm.setSize(pl.size());
		for (Object po : pl) {
			Patient p = (Patient) po;
			if (p.isDeceased()) { continue; }
			
			row = printPatient(p, row);
			
			if (type == ExportType.PATIENTS_ONLY) { continue; }
			
			List<?> vl = s.createCriteria(Visit.class)
			              .add(Restrictions.eq("patient", p))
			              .addOrder(Order.desc("date")).list();
			
			int clinicVisits = 0;
			int allVisits = 0;
			for (Object vo : vl) {
				Visit v = (Visit) vo;
				
				ClinicTeam ct = v.getClinicTeam();
				boolean isNormal = ct != null && ct.getType() == ClinicType.NORMAL;
				if (isNormal) { clinicVisits++; }
				allVisits++;
				
				if (allVisits <= MAX_VISITS || isNormal) {
					row = printVisit(v, row);
				}
				if (clinicVisits == 1) {
					List<?> psl = s.createCriteria(Prescription.class)
					               .add(Restrictions.eq("visit", v))
					               .addOrder(Order.desc("quantity")).list();
					for (Object pso : psl) {
						Prescription ps = (Prescription) pso;
						row = printScript(ps, row);
					}
				}
			}
			s.clear();
			
			sm.setProgress(1L, true);
			if (!sm.isRunning()) { break; }
		}
		Database.disconnect(s);
	}
	
	private int printPatient(Patient p, int row) throws WriteException {
		ws.addCell(new Label(0, row, "#" + p.getId(), boldFont));
		
		String name;
		if (order == SortOrder.FIRST_NAME) {
			name = p.getFirstName() + " " + p.getLastName();
		} else {
			name = p.getLastName() + ", " + p.getFirstName();
		}
		ws.addCell(new Label(1, row, name, boldFont));
		
		if (!StringUtils.isEmpty(p.getAddress())) {
			ws.addCell(new Label(5, row, p.getAddress(), smallFont));
		}
		if (!StringUtils.isEmpty(p.getPhone())) {
			ws.addCell(new Label(9, row, "Ph. " + p.getPhone(), smallFont));
		}
		float age = p.getAge();
		if (!Float.isNaN(age)) {
			ws.addCell(new Label(11, row, "Age: " + Math.round(age)));
		}
		
		if (type != ExportType.PATIENTS_ONLY) {
			ws.getRowView(row).setFormat(patientRow);
		}
		return row + 1;
	}
	
	private int printVisit(Visit v, int row) throws WriteException {
		ws.addCell(new DateTime(0, row, v.getDate(), dates));
		ws.setColumnView(0, 11);
		
		ClinicTeam ct = v.getClinicTeam();
		if (ct != null) {
			ws.addCell(new Label(1, row, ct.toString(), smallFont));
		}
		
		int bg = v.getBloodGlucose();
		if (bg > 0) {
			ws.addCell(new Label(3, row, "BG " + bg + " mg/dl", smallFont));
		}
		int sys = v.getSystolic();
		if (sys > 0) {
			ws.addCell(new Label(5, row, "BP " + v.getSystolic() + "/" + v.getDiastolic() + " mmHg", smallFont));
		}
		float temp = v.getTemperatureC();
		if (temp > 0.0f) {
			ws.addCell(new Label(7, row, String.format("%3.1f%sC", temp, UnicodeChars.DEGREE_SIGN)));
		}
		float bmi = v.getBMI();
		if (!Float.isInfinite(bmi) && !Float.isNaN(bmi)) {
			ws.addCell(new Label(8, row, String.format("BMI %3.2f", bmi)));
		}
		
		VisitText vt = v.getText(VisitTextType.COMMENTS, currLange);
		if (vt != null) {
			Label l = new Label(1, ++row, vt.getText(), smallFont);
			l.setCellFormat(wrapped);
			ws.addCell(l);
			ws.mergeCells(1, row, 11, row);
		}
		return row + 1;
	}
	
	private int printScript(Prescription p, int row) throws WriteException {
		ws.addCell(new jxl.write.Number(1, row, p.getQuantity() < 0? Double.NaN : p.getQuantity()));
		int col = 2;
		if (p.getDosage() != null) {
			ws.addCell(new Label(col, row, p.getDosage().toString(), smallFont));
			col += 2;
		}
		ws.addCell(new Label(col, row, p.getTreatment() + " for " + p.getDiagnosis(), smallFont));
		return row + 1;
	}
}