/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.ui.swt.editors;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.vwazennou.mrs.MRSController.QueryAllCallback;
import org.vwazennou.mrs.dictionary.DictionaryEntry.CompoundStr;
import org.vwazennou.mrs.dictionary.Str;
import org.vwazennou.mrs.patient.Patient;
import org.vwazennou.mrs.script.Prescription;
import org.vwazennou.mrs.ui.swt.MRSControls;
import org.vwazennou.mrs.ui.swt.MRSView;
import org.vwazennou.mrs.ui.swt.MRSView.MRSViewable;
import org.vwazennou.mrs.ui.swt.SWTInterface;
import org.vwazennou.mrs.visit.ClinicTeam;
import org.vwazennou.mrs.visit.Visit;

import com.datamininglab.foundation.awt.icons.IconsMS;
import com.datamininglab.foundation.awt.icons.IconsMed;
import com.datamininglab.foundation.data.DataList;
import com.datamininglab.foundation.data.field.DataFields.BaseEnumField;
import com.datamininglab.foundation.data.field.DataFields.DateField;
import com.datamininglab.foundation.data.field.DataFields.FloatField;
import com.datamininglab.foundation.data.field.DataFields.IntField;
import com.datamininglab.foundation.data.field.DataFields.LongField;
import com.datamininglab.foundation.data.field.DataFields.ObjectField;
import com.datamininglab.foundation.data.field.DataFields.StringField;
import com.datamininglab.foundation.data.render.DataRenderers.DateRenderer;
import com.datamininglab.foundation.data.render.DataRenderers.FloatRenderer;
import com.datamininglab.foundation.data.render.DataRenderers.IntRenderer;
import com.datamininglab.foundation.data.render.DataRenderers.LongRenderer;
import com.datamininglab.foundation.data.render.DataRenderers.StringRenderer;
import com.datamininglab.foundation.swt.controls.data.DataTable;
import com.datamininglab.foundation.swt.util.ResourceManager;
import com.datamininglab.foundation.swt.util.SWTUtilities;
import com.datamininglab.foundation.text.UnicodeChars;
import com.datamininglab.foundation.ui.UIUtilities.UIAction;
import com.datamininglab.foundation.util.Utilities;

public class TabularData extends Composite implements MRSViewable, SelectionListener {
	private static final String NULL = "-";
	private static final StringRenderer<String>  STRING_RENDERER = new StringRenderer<>(NULL);
	private static final StringRenderer<Long>    LONG_RENDERER   = new LongRenderer(true, NULL);
	private static final StringRenderer<Integer> INT_RENDERER    = new IntRenderer(true, NULL);
	private static final FloatRenderer           FLOAT_RENDERER  = new FloatRenderer("2.1", NULL);
	private static final DateRenderer            DATE_RENDERER   = new DateRenderer("dd/MM/yyyy", NULL) {
		@Override
		public String render(Date in) {
			if (in == null || in.getTime() == Utilities.DATE_1900_1_1) { in = null; }
			return super.render(in);
		}
	};
	
	private SWTInterface ui;
	
	private Combo type, clinic;
	private DataTable<Patient>      patients;
	private DataTable<Visit>        visits;
	private DataTable<Prescription> scripts;
	
	public TabularData(SWTInterface ui, Composite parent, int style) {
		super(parent, style);
		this.ui = ui;
		setLayout(new GridLayout(6, false));
		
		MRSControls.spacer(this, 0, 1);
		type = MRSControls.combo(this, SWT.BORDER | SWT.READ_ONLY, Str.TABLE, this, 1, 0);
		type.setData(SWTInterface.TEXT, new Str[] {
			Str.PATIENTS, Str.VISITS, Str.PRESCRIPTIONS	
		});
		
		clinic = MRSControls.combo(this, SWT.BORDER | SWT.READ_ONLY, Str.CLINIC_TEAM, this, 1, 0);
		MRSControls.comboItems(clinic, ui.getController().getClinicTeams());
		
		Button b = new Button(this, SWT.PUSH);
		b.setData(SWTInterface.TEXT, Str.REFRESH);
		b.addSelectionListener(this);
		ResourceManager rm = ui.getResourceManager();
		b.setImage(rm.getImage(IconsMS.PAGE_REFRESH));
		
		Composite stack = new Composite(this, SWT.NONE);
		stack.setLayout(new StackLayout());
		stack.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 6, 1));

		initPatientsTable(stack, rm);
		initVisitsTable(stack, rm);
		initScriptsTable(stack, rm);
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		ClinicTeam ct = clinic.getSelectionIndex() < 0? null
				: ui.getController().getClinicTeams().get(clinic.getText());
		switch (type.getSelectionIndex()) {
			case 0:
				queryAll(Patient.class, null, patients);
				break;
			case 1:
				queryAll(Visit.class, ct == null? null : Restrictions.eq("clinicTeam", ct), visits);
				break;
			case 2:
				queryAll(Prescription.class, null, scripts);
				break;
			default: break;
		}
	}
	
	private <T> void queryAll(Class<T> c, Criterion crit, final DataTable<T> table) {
		SWTUtilities.setTopControl(table.getControl());
		ui.getController().new QueryAll<>(c, crit, new QueryAllCallback<T>() {
			@Override
			public void handleResults(final List<T> list) {
				table.setData(new DataList<>(list));
				SWTUtilities.run(getDisplay(), new Runnable() {
					@Override
					public void run() {
						ui.setSubtitle(INT_RENDERER.render(list.size()) + " " + Str.MATCHES);
					}
				});
			}
		});
	}
	
	private void initPatientsTable(Composite parent, ResourceManager rm) {
		patients = new DataTable<>(parent, DataTable.RANK_COLUMN | SWT.BORDER, rm);
		patients.addColumn(new LongField<Patient>(Str.PATIENT_ID) {
			@Override
			public Long get(Patient row) { return row.getId(); }
		}, SWT.RIGHT | SWT.READ_ONLY, LONG_RENDERER, 50);
		patients.addColumn(new IntField<Patient>(Str.VISITS) {
			@Override
			public Integer get(Patient row) { return row.getVisits().size(); }
		}, SWT.RIGHT, INT_RENDERER, 50);
		
		patients.addColumn(new BaseEnumField<Patient, Str>(Str.GENDER) {
			@Override
			public Str get(Patient row) { return row.getGender(); }
			@Override
			public Str[] getEnumConstants() { return Str.values(); }
		}, SWT.CENTER | SWT.READ_ONLY, new StringRenderer<Str>() {
			@Override
			public String icon(Str in) {
				switch (in) {
					case MALE:   return IconsMed.CHILD_MALE_DARK;
					case FEMALE: return IconsMed.CHILD_FEMALE_DARK;
					default:     return IconsMS.QUESTION_PURPLE;
				}
			}
		}, 60);
		patients.addColumn(new ObjectField<Patient, Boolean>(Str.CHURCH_MEMBER) {
			@Override
			public Boolean get(Patient row) { return row.isChurchMember(); }
		}, SWT.CENTER | SWT.READ_ONLY, new StringRenderer<Boolean>() {
			@Override
			public String render(Boolean in) { return in? "Yes" : NULL; }
			@Override
			public String icon(Boolean in) { return in? IconsMed.CHURCH : null; }
		}, 50);
		patients.addColumn(new ObjectField<Patient, Boolean>(Str.LADS_PATIENT) {
			@Override
			public Boolean get(Patient row) { return row.isFollowedByLADS(); }
		}, SWT.CENTER | SWT.READ_ONLY, new StringRenderer<Boolean>() {
			@Override
			public String render(Boolean in) { return in? "Yes" : NULL; }
			@Override
			public String icon(Boolean in) { return in? IconsMed.NURSE_MALE_DARK : null; }
		}, 50);
		
		patients.addColumn(new StringField<Patient>(Str.LAST_NAME) {
			@Override
			public String get(Patient row) { return row.getLastName(); }
		}, SWT.READ_ONLY, STRING_RENDERER, 100);
		patients.addColumn(new StringField<Patient>(Str.FIRST_NAME) {
			@Override
			public String get(Patient row) { return row.getFirstName(); }
		}, SWT.READ_ONLY, STRING_RENDERER, 100);
		patients.addColumn(new DateField<Patient>(Str.DOB) {
			@Override
			public Date get(Patient row) { return row.getBirthdate(); }
		}, SWT.READ_ONLY, DATE_RENDERER, 80);
		
		patients.addColumn(new StringField<Patient>(Str.ADDRESS) {
			@Override
			public String get(Patient row) { return row.getAddress(); }
		}, SWT.READ_ONLY, STRING_RENDERER, 120);
		patients.addColumn(new StringField<Patient>(Str.COMMUNITY) {
			@Override
			public String get(Patient row) { return row.getCommunity(); }
		}, SWT.READ_ONLY, STRING_RENDERER, 100);
		patients.addColumn(new StringField<Patient>(Str.PHONE) {
			@Override
			public String get(Patient row) { return row.getPhone(); }
		}, SWT.READ_ONLY, STRING_RENDERER, 100);
		patients.addColumn(new StringField<Patient>(Str.ALTERNATE_PHONE) {
			@Override
			public String get(Patient row) { return row.getAlternatePhone(); }
		}, SWT.READ_ONLY, STRING_RENDERER, 100);
		patients.addColumn(new StringField<Patient>(Str.E_MAIL) {
			@Override
			public String get(Patient row) { return row.getEmail(); }
		}, SWT.READ_ONLY, STRING_RENDERER, 100);
		
		patients.addColumn(new IntField<Patient>(Str.CHILDREN) {
			@Override
			public Integer get(Patient row) { return row.getNumberOfChildren(); }
		}, SWT.READ_ONLY, INT_RENDERER, 40);
		patients.addColumn(new StringField<Patient>(Str.NOTES) {
			@Override
			public String get(Patient row) { return row.getNotes(); }
		}, SWT.READ_ONLY, STRING_RENDERER, 200);
	}
	
	private void initVisitsTable(Composite parent, ResourceManager rm) {
		visits = new DataTable<>(parent, DataTable.RANK_COLUMN | SWT.BORDER, rm);
		visits.addColumn(new LongField<Visit>(Str.ID) {
			@Override
			public Long get(Visit row) { return row.getId(); }
		}, SWT.RIGHT, LONG_RENDERER, 50);
		visits.addColumn(new LongField<Visit>(Str.PATIENT_ID) {
			@Override
			public Long get(Visit row) { return row.getPatient().getId(); }
		}, SWT.RIGHT | SWT.READ_ONLY, LONG_RENDERER, 50);
		visits.addColumn(new StringField<Visit>(Str.NAME) {
			@Override
			public String get(Visit row) {
				Patient p = row.getPatient();
				return p.getLastName() + ", " + p.getFirstName();
			}
		}, SWT.READ_ONLY, STRING_RENDERER, 120);
		visits.addColumn(new DateField<Visit>(Str.DATE) {
			@Override
			public Date get(Visit row) { return row.getDate(); }
		}, SWT.READ_ONLY, DATE_RENDERER, 80);
		visits.addColumn(new StringField<Visit>(Str.CLINIC_TEAM) {
			@Override
			public String get(Visit row) { return ObjectUtils.toString(row.getClinicTeam(), NULL); }
		}, SWT.LEFT, STRING_RENDERER, 80);
		visits.addColumn(new IntField<Visit>(Str.PRESCRIPTIONS) {
			@Override
			public Integer get(Visit row) { return row.getPrescriptions().size(); }
		}, SWT.RIGHT, INT_RENDERER, 50);
		
		visits.addColumn(new IntField<Visit>(Str.SYSTOLIC) {
			@Override
			public Integer get(Visit row) { return row.getSystolic(); }
		}, SWT.RIGHT | SWT.READ_ONLY, INT_RENDERER, 50);
		visits.addColumn(new IntField<Visit>(Str.DIASTOLIC) {
			@Override
			public Integer get(Visit row) { return row.getDiastolic(); }
		}, SWT.RIGHT | SWT.READ_ONLY, INT_RENDERER, 50);
		visits.addColumn(new IntField<Visit>(Str.PULSE) {
			@Override
			public Integer get(Visit row) { return row.getPulse(); }
		}, SWT.RIGHT | SWT.READ_ONLY, INT_RENDERER, 50);
		visits.addColumn(new IntField<Visit>(Str.BLOOD_GLUCOSE) {
			@Override
			public Integer get(Visit row) { return row.getBloodGlucose(); }
		}, SWT.RIGHT | SWT.READ_ONLY, INT_RENDERER, 50);
		visits.addColumn(new FloatField<Visit>(Str.HEMOGLOBIN) {
			@Override
			public Float get(Visit row) { return row.getHemoglobin(); }
		}, SWT.RIGHT | SWT.READ_ONLY, FLOAT_RENDERER, 50);
		visits.addColumn(new FloatField<Visit>(new CompoundStr(Str.TEMPERATURE, " (" + UnicodeChars.DEGREE_SIGN + "C)")) {
			@Override
			public Float get(Visit row) { return row.getTemperatureC(); }
		}, SWT.RIGHT | SWT.READ_ONLY, FLOAT_RENDERER, 50);
		visits.addColumn(new FloatField<Visit>(new CompoundStr(Str.HEIGHT, " (cm)")) {
			@Override
			public Float get(Visit row) { return row.getHeightCm(); }
		}, SWT.RIGHT | SWT.READ_ONLY, FLOAT_RENDERER, 50);
		visits.addColumn(new FloatField<Visit>(new CompoundStr(Str.WEIGHT, " (kg)")) {
			@Override
			public Float get(Visit row) { return row.getWeightKg(); }
		}, SWT.RIGHT | SWT.READ_ONLY, FLOAT_RENDERER, 50);
	}
	
	private void initScriptsTable(Composite parent, ResourceManager rm) {
		scripts = new DataTable<>(parent, DataTable.RANK_COLUMN | SWT.BORDER, rm);
		scripts.addColumn(new LongField<Prescription>(Str.PATIENT_ID) {
			@Override
			public Long get(Prescription row) {
				return row.getVisit() == null? null : row.getVisit().getPatient().getId();
			}
		}, SWT.RIGHT | SWT.READ_ONLY, LONG_RENDERER, 50);
		scripts.addColumn(new StringField<Prescription>(Str.NAME) {
			@Override
			public String get(Prescription row) {
				if (row.getVisit() == null) { return null; }
				
				Patient p = row.getVisit().getPatient();
				return p.getLastName() + ", " + p.getFirstName();
			}
		}, SWT.READ_ONLY, STRING_RENDERER, 120);
		scripts.addColumn(new DateField<Prescription>(Str.DATE) {
			@Override
			public Date get(Prescription row) {
				return row.getVisit() == null? null : row.getVisit().getDate();
			}
		}, SWT.READ_ONLY, DATE_RENDERER, 80);
		
		scripts.addColumn(new FloatField<Prescription>(Str.QUANTITY) {
			@Override
			public Float get(Prescription row) { return row.getQuantity() < 0.0f? Float.NaN : row.getQuantity(); }
		}, SWT.READ_ONLY, FLOAT_RENDERER, 50);
		scripts.addColumn(new StringField<Prescription>(Str.TREATMENT) {
			@Override
			public String get(Prescription row) { return ObjectUtils.toString(row.getTreatment()); }
		}, SWT.READ_ONLY, STRING_RENDERER, 120);
		scripts.addColumn(new StringField<Prescription>(Str.DOSAGE) {
			@Override
			public String get(Prescription row) { return ObjectUtils.toString(row.getDosage()); }
		}, SWT.READ_ONLY, STRING_RENDERER, 60);
		scripts.addColumn(new StringField<Prescription>(Str.FORM) {
			@Override
			public String get(Prescription row) { return ObjectUtils.toString(row.getForm()); }
		}, SWT.READ_ONLY, STRING_RENDERER, 60);
		scripts.addColumn(new StringField<Prescription>(Str.DIAGNOSIS) {
			@Override
			public String get(Prescription row) { return row.getDiagnosis(); }
		}, SWT.READ_ONLY, STRING_RENDERER, 120);
	}

	@Override
	public MRSView getView(UIAction action) {
		return new MRSView(this, this, Str.DATA_TABLE);
	}

	@Override
	public void showView() { ui.setCurrent(null); }
	
	@Override
	public boolean hideView(boolean promptToSave) { return true; }
	
	@Override
	public UIAction getSaveAction() { return null; }
	@Override
	public UIAction getNewAction() { return null; }
	
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}
}