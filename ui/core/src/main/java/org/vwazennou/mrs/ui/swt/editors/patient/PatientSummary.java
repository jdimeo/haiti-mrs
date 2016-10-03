/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.ui.swt.editors.patient;

import java.awt.Color;
import java.util.Date;
import java.util.Stack;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.vwazennou.mrs.MRSController;
import org.vwazennou.mrs.dictionary.DictionaryEntry.CompoundStr;
import org.vwazennou.mrs.dictionary.Language;
import org.vwazennou.mrs.dictionary.Str;
import org.vwazennou.mrs.patient.Patient;
import org.vwazennou.mrs.ui.MRSActions;
import org.vwazennou.mrs.ui.swt.MRSControls;
import org.vwazennou.mrs.ui.swt.MRSView;
import org.vwazennou.mrs.ui.swt.MRSView.MRSViewable;
import org.vwazennou.mrs.ui.swt.SWTInterface;
import org.vwazennou.mrs.ui.swt.editors.patient.VisitCallback.LabResult;
import org.vwazennou.mrs.ui.swt.editors.patient.VisitCallback.Treatment;
import org.vwazennou.mrs.ui.swt.search.SearchResults;
import org.vwazennou.mrs.visit.Visit;

import com.datamininglab.foundation.awt.ColorPalette;
import com.datamininglab.foundation.awt.icons.IconsMS;
import com.datamininglab.foundation.awt.icons.IconsMed;
import com.datamininglab.foundation.awt.plot.BasePlot.PlotAxes;
import com.datamininglab.foundation.data.field.DataFields.DateField;
import com.datamininglab.foundation.data.field.DataFields.FloatField;
import com.datamininglab.foundation.data.field.DataFields.IntField;
import com.datamininglab.foundation.data.field.DataFields.StringField;
import com.datamininglab.foundation.data.render.DataRenderers.DateRenderer;
import com.datamininglab.foundation.data.render.DataRenderers.StringRenderer;
import com.datamininglab.foundation.swt.controls.data.DataTable;
import com.datamininglab.foundation.swt.plot.scatter.ScatterLinePainter.DefaultLinePainter;
import com.datamininglab.foundation.swt.plot.scatter.ScatterLinePainter.DefaultLinePainter.ScatterLineMode;
import com.datamininglab.foundation.swt.plot.scatter.ScatterPlotWithLegend;
import com.datamininglab.foundation.swt.plot.scatter.ScatterPointPainter.DefaultPointPainter;
import com.datamininglab.foundation.swt.util.ResourceManager;
import com.datamininglab.foundation.swt.util.SWTUtilities;
import com.datamininglab.foundation.text.UnicodeChars;
import com.datamininglab.foundation.ui.UIUtilities.UIAction;
import com.datamininglab.foundation.util.Utilities;

public class PatientSummary extends Composite implements MRSViewable, SelectionListener {
	private static final String DELIM = ", ";
	private static final DateRenderer DATES = new DateRenderer("dd.MM.yyyy", "N/A");
	private static final int COL_WIDTH = 50;
	
	static final StringRenderer<Float> FLOAT_RENDERER = new StringRenderer<Float>() {
		@Override
		public String render(Float in) {
			if (in == 0.0f) { return ""; }
			return Utilities.stringValue(in, 1);
		}
	};
	static final StringRenderer<Integer> INT_RENDERER = new StringRenderer<Integer>() {
		@Override
		public String render(Integer in) { return FLOAT_RENDERER.render(in.floatValue()); }
	};
	
	private SWTInterface ui;
	private Patient p;
	
	private Text dob, children, marital, history, tags;
	private Text comments;
	private Label medsDate;
	private SearchResults meds;
	private DataTable<Visit> vitals;
	private ScatterPlotWithLegend<VisitVital> vitalsGraph;
	private DataTable<LabResult> labs;
	private DataTable<Treatment> medsAll;
	
	private VisitCallback visitCallback;
	
	public PatientSummary(SWTInterface ui, ScrolledComposite parent, Integer style) {
		super(parent, style);
		this.ui = ui;
		ResourceManager rm = ui.getResourceManager();
		
		setLayout(new GridLayout(6, false));
		MRSControls.spacer(this, SWT.DEFAULT, 2);
		
		tags     = setSpan(keyVal(this, Str.TAGS, null, true), 3);	
		dob      = keyVal(this, Str.DOB, null, true);
		marital  = keyVal(this, Str.MARITAL_STATUS, null, false);
		children = keyVal(this, Str.CHILDREN, null, true);
		history  = setSpan(keyVal(this, Str.MEDICAL_HISTORY, null, true), 5);
		
		header(rm, this, Str.CURRENT_MEDICATIONS, IconsMed.PILL2, 6);
		
		medsDate = new Label(this, SWT.NONE);
		medsDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		medsDate.setFont(rm.getFont(12, SWT.NORMAL));
		
		ToolBar tb = new ToolBar(this, SWT.FLAT);
		tb.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
		SWTUtilities.item(tb, SWT.PUSH, MRSActions.EXPAND_ALL, rm, this);
		SWTUtilities.item(tb, SWT.PUSH, MRSActions.COLLAPSE_ALL, rm, this);
		
		meds = new SearchResults(rm, this, SWT.BORDER);
		meds.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 6, 1));
		
		SashForm sash = new SashForm(this, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 6, 1);
		gd.heightHint = 600;
		sash.setLayoutData(gd);
		
		Composite commentsBox = new Composite(sash, SWT.NONE);
		commentsBox.setLayout(SWTUtilities.removeMargins(new GridLayout(1, false)));
		
		header(rm, commentsBox, Str.FOLLOW_UP_COMMENTS, IconsMed.NOTE, 1);
		comments = new Text(commentsBox, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = 100;
		comments.setLayoutData(gd);
		comments.setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		
		Composite vitalsBox = new Composite(sash, SWT.NONE);
		vitalsBox.setLayout(SWTUtilities.removeMargins(new GridLayout(1, false)));
		
		TabFolder vitalsTabs = new TabFolder(vitalsBox, SWT.NONE);
		vitalsTabs.setFont(rm.getFont("bold", SWT.DEFAULT, SWT.DEFAULT));
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = 200;
		vitalsTabs.setLayoutData(gd);
		
		TabItem graphItem = new TabItem(vitalsTabs, SWT.NONE);
		graphItem.setText(Str.VITALS + " " + Str.GRAPH);
		graphItem.setData(SWTInterface.TEXT, new CompoundStr(Str.VITALS, " ", Str.GRAPH));
		graphItem.setImage(rm.getImage(IconsMS.CLIPBOARD_CHECK));
		vitalsGraph = new ScatterPlotWithLegend<>(rm, vitalsTabs, SWT.NONE,
				new ColorPalette(new Color(200, 200, 240), new Color(240, 200, 200)));
		vitalsGraph.getPlot().setPointPainter(new DefaultPointPainter<VisitVital>()
				.setDrawBorder(false));
		vitalsGraph.getPlot().setLinePainter(new DefaultLinePainter<VisitVital>()
				.setColor(rm.getColor(150, 150, 200))
				.setWidth(3)
				.setMode(ScatterLineMode.VISIBLE_UNDER_MOUSE));
		graphItem.setControl(vitalsGraph.getPlot().getControl());
		
		vitalsGraph.setAxis(PlotAxes.X,     VisitVital.DATE,  false);
		vitalsGraph.setAxis(PlotAxes.Y,     VisitVital.VALUE, false);
		vitalsGraph.setAxis(PlotAxes.COLOR, VisitVital.VALUE, false);
		vitalsGraph.setAxis(PlotAxes.SIZE,  vitalsGraph.getPlot().getConstantField(), false);
		vitalsGraph.setAxis(PlotAxes.ICON,  VisitVital.ICON,  false);
		
		TabItem tableItem = new TabItem(vitalsTabs, SWT.NONE);
		tableItem.setText(Str.TABLE.toString());
		tableItem.setData(SWTInterface.TEXT, Str.TABLE);
		vitals = new DataTable<>(vitalsTabs, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, rm);
		tableItem.setControl(vitals.getControl());
		
		vitals.addColumn(new DateField<Visit>(Str.DATE) {
			@Override
			public Date get(Visit row) { return row.getDate(); }
		}, SWT.RESIZE | SWT.DOWN, DATES);
		
		vitals.addColumn(new IntField<Visit>(Str.SYSTOLIC) {
			@Override
			public Integer get(Visit row) { return row.getSystolic(); }
		}, SWT.NONE, INT_RENDERER, COL_WIDTH);
		vitals.addColumn(new IntField<Visit>(Str.DIASTOLIC) {
			@Override
			public Integer get(Visit row) { return row.getDiastolic(); }
		}, SWT.NONE, INT_RENDERER, COL_WIDTH);
		vitals.addColumn(new IntField<Visit>(Str.BLOOD_GLUCOSE) {
			@Override
			public Integer get(Visit row) { return row.getBloodGlucose(); }
		}, SWT.NONE, INT_RENDERER, COL_WIDTH);
		vitals.addColumn(new FloatField<Visit>(Str.HEMOGLOBIN) {
			@Override
			public Float get(Visit row) { return row.getHemoglobin(); }
		}, SWT.NONE, FLOAT_RENDERER, COL_WIDTH);
		vitals.addColumn(new FloatField<Visit>(UnicodeChars.DEGREE_SIGN + "C") {
			@Override
			public Float get(Visit row) { return row.getTemperatureC(); }
		}, SWT.NONE, FLOAT_RENDERER, COL_WIDTH);
		
		vitals.addColumn(new FloatField<Visit>(
				new CompoundStr(Str.WEIGHT, " (kg)")) {
			@Override
			public Float get(Visit row) { return row.getWeightKg(); }
		}, SWT.NONE, FLOAT_RENDERER, COL_WIDTH);
		vitals.addColumn(new FloatField<Visit>(
				new CompoundStr(Str.HEIGHT, " (cm)")) {
			@Override
			public Float get(Visit row) { return row.getHeightCm(); }
		}, SWT.NONE, FLOAT_RENDERER, COL_WIDTH);
		
		vitals.addColumn(new IntField<Visit>(Str.PULSE) {
			@Override
			public Integer get(Visit row) { return row.getPulse(); }
		}, SWT.NONE, INT_RENDERER, COL_WIDTH);
		vitals.addColumn(new IntField<Visit>(Str.RESPIRATION) {
			@Override
			public Integer get(Visit row) { return row.getRespiration(); }
		}, SWT.NONE, INT_RENDERER, COL_WIDTH);
		
		header(rm, vitalsBox, Str.LABS, IconsMed.FLASK2, 1);
		
		labs = new DataTable<>(vitalsBox, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, rm);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
		gd.heightHint = 100;
		labs.getControl().setLayoutData(gd);
		labs.addColumn(new DateField<LabResult>(Str.DATE) {
			@Override
			public Date get(LabResult row) { return row.getDate(); }
		}, SWT.RESIZE | SWT.DOWN, DATES);
		labs.addColumn(new StringField<LabResult>(Str.NAME) {
			@Override
			public String get(LabResult row) { return row.getName(); }
		}, SWT.RESIZE, null);
		labs.addColumn(new StringField<LabResult>(Str.RESULT) {
			@Override
			public String get(LabResult row) { return row.getResult(); }
		}, SWT.RESIZE, null);
		
		header(rm, this, Str.ALL_MEDICATIONS, IconsMed.PILLS2, 6);
		
		medsAll = new DataTable<>(this, SWT.BORDER | SWT.H_SCROLL, rm);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false, 6, 1);
		gd.heightHint = 150;
		medsAll.getControl().setLayoutData(gd);
		
		visitCallback = new VisitCallback(this);
		
		parent.setExpandVertical(true);
		parent.setMinHeight(computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
	}
	
	SWTInterface            getUI()          { return ui; }
	DataTable<Visit>        getVitalsTable() { return vitals; }
	DataTable<LabResult>    getLabs()        { return labs; }
	DataTable<Treatment>    getMedsAll()     { return medsAll; }
	Text                    getComments()    { return comments; }
	SearchResults           getMeds()        { return meds; }
	Label                   getMedsDate()    { return medsDate; }
	Language                getLanguage()    { return ui.getCurrentLanguage(); }
	
	ScatterPlotWithLegend<VisitVital> getVitalsGraph() { return vitalsGraph; }
	
	public void setPatient(Patient p) {
		this.p = p;
		setPatientMetadata();
		
		meds.clear();
		vitals.removeAllRows();
		labs.removeAllRows();
		medsAll.removeAllRows();
		comments.setText("");
		
		MRSController c = ui.getController();
		c.new PerformQuery(p, c.getSearchFields().getParentFields(Visit.class), visitCallback);
	}
	
	private void setPatientMetadata() {
		String dobStr = DATES.render(p.getBirthdate());
		String age = Utilities.stringValue(p.getAge(), 1);
		setText(this.dob, new CompoundStr(dobStr + " (", Str.AGE, ": " + age + ")"));
	
		String childrenStr  = String.valueOf(p.getNumberOfChildren());
		String childAges = p.getChildAges();
		if (!childAges.isEmpty()) {
			childrenStr += " (" + childAges + ")";
		}
		this.children.setText(childrenStr);
		
		setText(marital, p.getMaritalStatus());
		
		Stack<Object> l = new Stack<>();
		for (Str str : Patient.MEDICAL_CONDITIONS) {
			if (p.hasCondition(str)) { l.push(str); l.push(DELIM); }
		}
		if (l.isEmpty()) {
			l.push(Str.NA);
		} else {
			// Remove trailing ", "
			l.pop();
		}
		setText(history, new CompoundStr(l));
		
		l.clear();
		l.push(p.getPatientGroup()); l.push(DELIM);
		if (p.isFollowedByLADS()) {
			l.push(Str.LADS_PATIENT); l.push(DELIM);
		}
		if (p.isChurchMember()) {
			l.push(Str.CHURCH_MEMBER); l.push(DELIM);
		}
		if (p.isDeceased()) {
			l.push(Str.DECEASED); l.push(DELIM);
		}
		l.pop();
		setText(tags, new CompoundStr(l));		
	}
	
	private Text keyVal(Composite parent, Str key, String icon, boolean grabExcessSpace) {
		Label l = new Label(parent, SWT.RIGHT);
		l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		if (icon != null) {
			l.setImage(ui.getResourceManager().getImage(icon));
			l.setData(SWTInterface.TOOLTIP, key);
			l.setToolTipText(key.toString());
		} else {
			l.setData(SWTInterface.TEXT, key);
			l.setText(key.toString());
		}
		
		Text t = new Text(parent, SWT.READ_ONLY);
		t.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, grabExcessSpace, false));
		t.setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		t.setFont(ui.getResourceManager().getFont(12, SWT.NORMAL));
		return t;
	}
	
	private static Text setSpan(Text t, int hspan) {
		((GridData) t.getLayoutData()).horizontalSpan = hspan;
		return t;
	}
	
	private static void setText(Text t, Object o) {
		t.setData(SWTInterface.TEXT, o);
		t.setText(o.toString());
		t.setToolTipText(o.toString());
	}
	
	private static void header(ResourceManager rm, Composite parent, Str text, String icon, int span) {
		Composite c = new Composite(parent, SWT.NONE);
		GridLayout gl = SWTUtilities.removeMargins(new GridLayout(3, false));
		gl.marginTop = 6;
		c.setLayout(gl);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, span, 1));
		
		Label l = new Label(c, SWT.NONE);
		l.setImage(rm.getImage(icon));
		
		l = new Label(c, SWT.NONE);
		l.setFont(rm.getFont("bold", SWT.DEFAULT, SWT.DEFAULT));
		l.setText(text.toString());
		l.setData(SWTInterface.TEXT, text);
		
		l = new Label(c, SWT.SEPARATOR | SWT.HORIZONTAL);
		l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		UIAction action = (UIAction) e.widget.getData();
		if (action == MRSActions.EXPAND_ALL) {
			meds.setExpanded(true);
			pack();
		} else if (action == MRSActions.COLLAPSE_ALL) {
			meds.setExpanded(false);
			pack();
		}
	}
	
	@Override
	public MRSView getView(UIAction action) {
		return new MRSView(this, getParent(), action.getNameObj(), getSaveAction());
	}
	
	@Override
	public boolean hideView(boolean promptToSave) { return true; }
	
	@Override
	public void showView() { ui.setCurrent(p); }
	
	@Override
	public UIAction getSaveAction() { return MRSActions.EDIT_PATIENT; }
	@Override
	public UIAction getNewAction() { return MRSActions.NEW_PATIENT; }
	
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// No actions should be performed for this event
	}
}