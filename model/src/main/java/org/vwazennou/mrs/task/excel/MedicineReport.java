/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.task.excel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.vwazennou.mrs.data.Database;
import org.vwazennou.mrs.formulary.Formulary;
import org.vwazennou.mrs.formulary.FormularyEntry;
import org.vwazennou.mrs.script.Prescription;
import org.vwazennou.mrs.visit.ClinicTeam;
import org.vwazennou.mrs.visit.Visit;

import com.datamininglab.commons.hash.HashUtils;
import com.datamininglab.commons.lang.StatusMonitor;

import gnu.trove.map.TLongFloatMap;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongFloatHashMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.procedure.TLongObjectProcedure;
import gnu.trove.set.hash.TLongHashSet;
import jxl.CellReferenceHelper;
import jxl.CellView;
import jxl.format.CellFormat;
import jxl.format.Colour;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class MedicineReport extends ExcelReport implements TLongObjectProcedure<TLongFloatMap> {
	private static final int FONT_SIZE = 9;
	
	// Formats are assigned a workbook-specific ID so we cannot reuse instances across workbooks
	private CellFormat num1dp   = new WritableCellFormat(new NumberFormat("0.0"));
	private CellFormat num2dp   = new WritableCellFormat(new NumberFormat("0.00"));
	private CellFormat boldFont = new WritableCellFormat(new WritableFont(WritableFont.TAHOMA, FONT_SIZE, WritableFont.BOLD));
	private CellView   wideCol  = new CellView();
	
	private StatusMonitor sm;
	private Formulary formulary;
	private ClinicTeam[] allTeams;
	private List<MedRow> rowQueue;
	private TLongObjectHashMap<String> medHashes =
		new TLongObjectHashMap<>();

	public MedicineReport(Formulary formulary) {
		this.formulary = formulary;
		wideCol.setSize(6000);
	}
	
	@Override
	protected void export(StatusMonitor sm, WritableWorkbook wb) throws IOException, WriteException {
		this.sm = sm;
		
		Session s = Database.connect();
		TLongHashSet patientIDs     = new TLongHashSet();
		TLongIntMap  perTeamVisits  = new TLongIntHashMap();
		TLongIntMap  perTeamReturns = new TLongIntHashMap();
		
		TLongObjectMap<TLongFloatMap> medCounts = new TLongObjectHashMap<>();
		
		List<?> l = s.createCriteria(ClinicTeam.class).list();
		allTeams = new ClinicTeam[l.size()];
		int i = 0;
		for (Object o : l) {
			allTeams[i++] = (ClinicTeam) o;
		}
		
		l = s.createCriteria(Visit.class)
		     .addOrder(Order.asc("date")).list();
		sm.setSize(l.size());
		sm.setProgress(0L, false);
		
		for (Object o : l) {
			if (!sm.isRunning()) { break; }
			
			Visit v = (Visit) o;
			
			ClinicTeam team = v.getClinicTeam();
			long teamId = team == null? -1L : team.getId();
			
			long pid = v.getPatient().getId();
			boolean isReturn = patientIDs.contains(pid);
			patientIDs.add(pid);
			
			perTeamVisits.adjustOrPutValue(teamId, 1, 1);
			if (isReturn) {
				perTeamReturns.adjustOrPutValue(teamId, 1, 1);
			}
			
			for (Prescription p : v.getPrescriptions()) {
				float q = p.getQuantity();
				if (q <= 0.0f) { continue; }
				
				FormularyEntry[] tuple = new FormularyEntry[] {
					p.getTreatment(), p.getDosage(), p.getForm()		
				};
				
				StringBuilder sb = new StringBuilder();
				
				long hash = 0L;
				for (i = 0; i < tuple.length; i++) {
					FormularyEntry fe = formulary.getEntry(tuple[i]);
					if (fe == null) { continue; }
					
					hash = HashUtils.buildHash(hash, fe.getId());
					sb.append(' ').append(fe.toString());
				}
				
				medHashes.put(hash, sb.toString().trim());
				
				TLongFloatMap map = medCounts.get(hash);
				if (map == null) {
					map = new TLongFloatHashMap();
					medCounts.put(hash, map);
				}
				map.adjustOrPutValue(teamId, q, q);
			}
			
			sm.setProgress(1L, true);
		}
		
		Database.disconnect(s);
		if (!sm.isRunning()) { return; }
		
		WritableSheet ws = wb.createSheet("Stats", 0);
		ws.setColumnView(0, wideCol);
		
		int row = 0;
		ws.addCell(new Label(0, row++, "Total patients"));
		ws.addCell(new jxl.write.Number(1, 0, patientIDs.size()));
		ws.addCell(new Label(0, row++, "Total visits"));
		ws.addCell(new jxl.write.Number(1, 1, l.size()));
		ws.addCell(new Label(0, ++row, "Per team:", boldFont));
		ws.addCell(new Label(1, row, "Return patients"));
		ws.addCell(new Label(2, row++, "All visits"));
		
		for (ClinicTeam ct : allTeams) {
			ws.addCell(new Label(0, row, ct.toString()));
			ws.addCell(new jxl.write.Number(1, row,   perTeamReturns.get(ct.getId())));
			ws.addCell(new jxl.write.Number(2, row++, perTeamVisits.get(ct.getId())));
		}
		
		WritableSheet ws100 = wb.createSheet("Medicines (per patient)", 1);
		ws100.setColumnView(0, wideCol);
		
		WritableCellFormat yellow = new WritableCellFormat();
		yellow.setBackground(Colour.YELLOW2);
		ws100.addCell(new Label(0, 0, "Expected visits this clinic:", yellow));
		ws100.addCell(new jxl.write.Number(1, 0, 100, yellow));
		ws100.addCell(new Label(2, 0, "Visits:", boldFont));
		
		String nVisitsCell = CellReferenceHelper.getCellReference(1, 0);
		
		ws100.addCell(new Label(0, 1, "Medicine", boldFont));
		ws100.addCell(new Label(1, 1, "Total for expected visits", boldFont));
		
		WritableSheet wsAll = wb.createSheet("Medicines (total)", 2);
		
		wsAll.setColumnView(0, wideCol);
		wsAll.addCell(new Label(0, 0, "Medicine", boldFont));
		wsAll.addCell(new Label(1, 0, "Total", boldFont));
		wsAll.addCell(new Label(2, 0, "Average (teams > 0)", boldFont));
		
		int col = 3;
		for (ClinicTeam ct : allTeams) {
			wsAll.addCell(new Label(col, 0, ct.toString()));
			ws100.addCell(new Label(col, 1, ct.toString()));
			ws100.addCell(new jxl.write.Number(col, 0, perTeamVisits.get(ct.getId())));
			col++;
		}
		
		rowQueue = new ArrayList<>();
		if (!medCounts.forEachEntry(this)) { return; }
		
		Collections.sort(rowQueue);
		
		row = 1;
		for (MedRow mr : rowQueue) {
			ws100.addCell(new Label(0, row + 1, mr.med));
			wsAll.addCell(new Label(0, row,     mr.med));
			
			wsAll.addCell(new jxl.write.Number(1, row, mr.total, num1dp));
			wsAll.addCell(new jxl.write.Number(2, row, mr.avg,   num1dp));
			
			float avgPerPatient = mr.total / l.size();
			ws100.addCell(new Formula(1, row + 1,
					String.format("%s*%8.7f", nVisitsCell, avgPerPatient), num2dp));
			
			for (i = 0; i < mr.vals.length; i++) {
				col = i + 3;
				wsAll.addCell(new jxl.write.Number(col, row, mr.vals[i]));
				
				String teamCountCell = CellReferenceHelper.getCellReference(col, 0);
				String totalCell = "'Medicines (total)'!" + CellReferenceHelper.getCellReference(col, row);
				ws100.addCell(new Formula(col, row + 1, totalCell + "/" + teamCountCell, num2dp));
			}
			row++;
		}
	}
	
	@Override
	public boolean execute(long a, TLongFloatMap b) {
		MedRow row = new MedRow();
		row.med  = medHashes.get(a);
		row.vals = new float[allTeams.length];
		
		float total = 0.0f;
		int nonZeroTeams = 0;
		
		for (int i = 0; i < allTeams.length; i++) {
			float q = b.get(allTeams[i].getId());
			total += q;
			
			row.vals[i] = q;
			if (q > 0.0f) { nonZeroTeams++; }
		}
		
		row.total = total;
		row.avg   = total / nonZeroTeams;
		
		rowQueue.add(row);
		return sm.isRunning();
	}
	
	private static class MedRow implements Comparable<MedRow> {
		private String  med;
		private float   total;
		private float   avg;
		private float[] vals;
		
		@Override
		public int compareTo(MedRow o) {
			return med.compareToIgnoreCase(o.med);
		}
	}
}
