/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.ui.swt.editors.patient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.vwazennou.mrs.dictionary.DictionaryEntry.CompoundStr;
import org.vwazennou.mrs.dictionary.Str;
import org.vwazennou.mrs.formulary.FormularyEntry;
import org.vwazennou.mrs.script.Prescription;
import org.vwazennou.mrs.task.Query;
import org.vwazennou.mrs.task.Query.QueryCallback;
import org.vwazennou.mrs.ui.swt.SWTInterface;
import org.vwazennou.mrs.ui.swt.search.PrescriptionResult;
import org.vwazennou.mrs.visit.UrineTest;
import org.vwazennou.mrs.visit.Visit;
import org.vwazennou.mrs.visit.VisitText.VisitTextType;

import com.datamininglab.foundation.awt.icons.IconsERI;
import com.datamininglab.foundation.awt.icons.IconsMS;
import com.datamininglab.foundation.awt.icons.IconsMed;
import com.datamininglab.foundation.data.field.DataField;
import com.datamininglab.foundation.data.field.DataFields.FloatField;
import com.datamininglab.foundation.data.field.DataFields.StringField;
import com.datamininglab.foundation.data.graph.DefaultEdge;
import com.datamininglab.foundation.data.graph.Graph;
import com.datamininglab.foundation.swt.controls.data.DataTable;
import com.datamininglab.foundation.swt.util.SWTUtilities;
import com.datamininglab.foundation.util.Structures.MappedSet;

import gnu.trove.map.hash.TObjectFloatHashMap;

public class VisitCallback implements QueryCallback, Runnable, Comparator<Prescription> {
	private PatientSummary parent;
	
	private static final DataField<Treatment, String> NAME_FIELD =
		new StringField<Treatment>(Str.TREATMENT) {
			@Override
			public String get(Treatment row) { return row.name; }
		};
	private static final Str[] FOLLOW_UP_STR = new Str[] {
		Str.LATER_THIS_WEEK,
		Str.NEXT_CLINIC,
		Str.LADS,
		Str.OUTSIDE_REFERRAL
	};
	
	private Visit[] visits;
	private Prescription[] scripts;
	private Map<String, Treatment> medMap;
	private Object medsDateTxt;
	
	public VisitCallback(PatientSummary parent) {
		this.parent = parent;
	}
	
	@Override
	public void handleResultsFor(Query q) {
		visits = (Visit[]) q.getResults();
		
		medMap = new TreeMap<>();
		Visit curr = null;
		
		Graph<Visit>      visitList = new Graph<>();
		Graph<VisitVital> vitalList = new Graph<>();		
		Graph<LabResult>  labList   = new Graph<>();
		MappedSet<String, String> medDosMap = new MappedSet<>();
		
		List<DataField<VisitVital, ?>> fieldList = new ArrayList<>();
		fieldList.add(VisitVital.DATE);
		fieldList.add(VisitVital.VALUE);
		fieldList.add(VisitVital.ICON);
		vitalList.setFields(fieldList);
		
		VisitVital sys = null, dia = null, bg = null, bmi = null,
		           hgb = null, pul = null, res  = null, tmp = null, wgt = null;		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < visits.length; i++) {
			Visit v = visits[i];
			String date = v.getFormattedDate();
			
			sb.append(date);
			boolean[] fuFlags = new boolean[] {
				v.followUpThis(),
				v.followUpNext(),
				v.followUpLADS(),
				v.followUpReferral()
			};
			for (int j = 0, fu = 0; j < fuFlags.length; j++) {
				if (!fuFlags[j]) { continue; }
				if (fu++ > 0) {
					sb.append(", ");
				} else {
					sb.append(" - ");
				}
				sb.append(FOLLOW_UP_STR[j]);
			}
			sb.append('\n');
			
			String c = ObjectUtils.toString(v.getText(VisitTextType.COMMENTS, parent.getLanguage()));
			if (!StringUtils.isEmpty(c)) { sb.append(c).append('\n'); }
			sb.append('\n');
			
			visitList.getNodes().add(v);
			
			UrineTest u = v.getUrineTestResult();
			if (u != null) {
				labList.getNodes().add(new LabResult(v.getDate(), Str.URINE, u.toString()));
			}
			
			Str mal = v.getMalariaResult();
			if (mal != Str.NA) {
				labList.getNodes().add(new LabResult(v.getDate(), Str.MALARIA, mal));
			}
			
			Str strep = v.getStrepResult();
			if (strep != Str.NA) {
				labList.getNodes().add(new LabResult(v.getDate(), Str.STREP, strep));
			}
			
			Date d = v.getDate();
			if (d.getTime() > Visit.DEFAULT_DATE) {
				sys = newVital(sys, Str.SYSTOLIC,      IconsMed.BLOOD_DROPS,
						d, date, v.getSystolic(),     vitalList);
				dia = newVital(dia, Str.DIASTOLIC,     IconsMed.BLOOD_DROPS,
						d, date, v.getDiastolic(),    vitalList);
				bg  = newVital(bg,  Str.BLOOD_GLUCOSE, IconsMed.SUGAR_CUBE,
						d, date, v.getBloodGlucose(), vitalList);
				hgb = newVital(hgb, Str.HEMOGLOBIN,    IconsMed.FLASK2,
						d, date, v.getHemoglobin(),   vitalList);
				bmi = newVital(bmi, Str.BMI,           IconsERI.CONT,
						d, date, v.getBMI(),          vitalList);
				wgt = newVital(wgt, Str.WEIGHT,        IconsMS.ARROW_DOWN_BLUE,
						d, date, v.getWeightKg(),     vitalList);
				pul = newVital(pul, Str.PULSE,         IconsMed.CARDIOLOGY,
						d, date, v.getPulse(),        vitalList);
				res = newVital(res, Str.RESPIRATION,   IconsMS.CLOCK,
						d, date, v.getRespiration(),  vitalList);
				tmp = newVital(tmp, Str.TEMPERATURE,   IconsMed.TEMPERATURE,
						d, date, v.getTemperatureC(), vitalList);
			}
			
			switch (v.getClinicTeam().getType()) {
				case REFILL: continue;
				case NORMAL:
					if (curr == null) { curr = v; }
					break;
				default: break;
			}
			
			for (Prescription p : v.getPrescriptions()) {
				FormularyEntry tfe = p.getTreatment();
				if (tfe == null) { continue; }
				
				String ts = tfe.toString();
				String ds = p.getModifiedDosage();
				if ("N/A".equals(ds)) { ds = ""; }
				
				medDosMap.add(ts, ds);
				Treatment t = medMap.get(ts);
				if (t == null) {
					t = new Treatment(ts);
					medMap.put(ts, t);
				}
				
				float n = p.getModifiedQuantity();
				t.counts.adjustOrPutValue(date, n, n);
			}
		}
		if (dia != null && sys != null) {
			vitalList.addEdge(new DefaultEdge<>(dia, sys, true));
		}
		
		parent.getVitalsTable().setPlotData(visitList);
		parent.getVitalsGraph().setPlotData(vitalList);
		parent.getLabs().setPlotData(labList);
		
		final String c = sb.toString();
		SWTUtilities.run(parent.getDisplay(), new Runnable() {
			@Override
			public void run() { parent.getComments().setText(c); }
		});
		
		for (Entry<String, Treatment> e : medMap.entrySet()) {
			String d = StringUtils.join(medDosMap.get(e.getKey()), ", ");
			if (!d.isEmpty()) { e.getValue().name += " " + d; }
		}
		
		if (curr == null) {
			medsDateTxt = Str.NA;
			scripts = null;
		} else {
			medsDateTxt = new CompoundStr(Str.PRESCRIBED, " " + curr.getFormattedDate());
			List<Prescription> l = curr.getPrescriptions();
			scripts = l.toArray(new Prescription[l.size()]);
			Arrays.sort(scripts, this);
		}
		SWTUtilities.run(parent.getDisplay(), this);
	}
	
	private static VisitVital newVital(VisitVital prev, Str s, String icon, Date date, String dateStr, float v,
			Graph<VisitVital> list) {
		if (Float.isNaN(v) || Float.isInfinite(v) || v == 0.0f) { return prev; }
		
		VisitVital vv = new VisitVital(s, icon, date, dateStr, v);
		list.getNodes().add(vv);
		if (prev != null) {
			list.addEdge(new DefaultEdge<>(prev, vv, false));
		}
		return vv;
	}
	
	@Override
	public void run() {
		DataTable<Treatment> medsAll = parent.getMedsAll();
		medsAll.removeAllColumns();
		medsAll.addColumn(NAME_FIELD, SWT.RESIZE | SWT.UP, null);
		for (int i = visits.length - 1; i >= 0; i--) {
			if ("LADS".equals(visits[i].getClinicTeam().toString())) { continue; }
			
			medsAll.addColumn(new QtyField(visits[i].getFormattedDate()),
				SWT.RESIZE,	PatientSummary.FLOAT_RENDERER).setAlignment(SWT.RIGHT);
		}
		
		Graph<Treatment> list = new Graph<>();
		for (Treatment t : medMap.values()) { list.getNodes().add(t); }
		medsAll.setPlotData(list);
		
		parent.getMedsDate().setText(medsDateTxt.toString());
		parent.getMedsDate().setData(SWTInterface.TEXT, medsDateTxt);
		if (scripts == null) { return; }
		
		PrescriptionResult[] arr = new PrescriptionResult[scripts.length];
		for (int i = 0; i < scripts.length; i++) {
			arr[i] = new PrescriptionResult(parent.getUI(), scripts[i], false);
		}
		parent.getMeds().setResults(arr);
		parent.getMeds().setExpanded(false);
		
		parent.pack();
	}
	
	// Sort scripts by quantity so long term/chronic meds are generally listed first
	@Override
	public int compare(Prescription o1, Prescription o2) {
		return Float.compare(o2.getQuantity(), o1.getQuantity());
	}
	
	@Override
	public Criterion getFilter(Class<?> c) { return null; }
	@Override
	public Order getOrder(Class<?> c) { return Order.desc("date"); }
	
	static class LabResult implements Comparable<LabResult> {
		private Date date;
		private Object name;
		private Object result;
		
		LabResult(Date date, Object name, Object result) {
			this.date   = date;
			this.name   = name;
			this.result = result;
		}
		
		Date   getDate()   { return date; }
		String getName()   { return name.toString(); }
		String getResult() { return result.toString(); }
		
		@Override
		public int compareTo(LabResult lr) {
			return date.compareTo(lr.date);
		}
	}
	
	static class Treatment implements Comparable<Treatment> {
		private String name;
		private TObjectFloatHashMap<String> counts;
		
		Treatment(String name) {
			this.name   = name;
			this.counts = new TObjectFloatHashMap<>();
		}
		
		@Override
		public int compareTo(Treatment o) {
			return name.compareToIgnoreCase(o.name);
		}
	}
	static class QtyField extends FloatField<Treatment> {
		QtyField(String date) { super(date); }
		@Override
		public Float get(Treatment row) {
			return row.counts.get(getName());
		}
	}
}