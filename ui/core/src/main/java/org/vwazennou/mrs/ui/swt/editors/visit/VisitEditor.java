/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.ui.swt.editors.visit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.text.WordUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.vwazennou.mrs.MRSController;
import org.vwazennou.mrs.dictionary.DictionaryEntry.CompoundStr;
import org.vwazennou.mrs.dictionary.Str;
import org.vwazennou.mrs.patient.Patient;
import org.vwazennou.mrs.script.Prescription;
import org.vwazennou.mrs.script.PrescriptionDirective;
import org.vwazennou.mrs.script.PrescriptionDirectiveBlank;
import org.vwazennou.mrs.task.Query;
import org.vwazennou.mrs.task.Query.QueryCallback;
import org.vwazennou.mrs.ui.MRSActions;
import org.vwazennou.mrs.ui.MRSInterface;
import org.vwazennou.mrs.ui.swt.MRSControls;
import org.vwazennou.mrs.ui.swt.MRSView;
import org.vwazennou.mrs.ui.swt.MRSView.MRSViewable;
import org.vwazennou.mrs.ui.swt.SWTInterface;
import org.vwazennou.mrs.ui.swt.TextRefreshDelegate;
import org.vwazennou.mrs.ui.swt.editors.Editor;
import org.vwazennou.mrs.ui.swt.editors.EnumEditor;
import org.vwazennou.mrs.visit.ClinicTeam;
import org.vwazennou.mrs.visit.UrineTest;
import org.vwazennou.mrs.visit.Visit;
import org.vwazennou.mrs.visit.VisitText.VisitTextType;

import com.datamininglab.commons.hash.HashUtils;
import com.datamininglab.commons.lang.utf.UnicodeChars;
import com.datamininglab.foundation.data.validation.DataValidators.ChainedValidator;
import com.datamininglab.foundation.data.validation.DataValidators.IntegralValidator;
import com.datamininglab.foundation.data.validation.DataValidators.NumericValidator;
import com.datamininglab.foundation.data.validation.DataValidators.RangeValidator;
import com.datamininglab.viz.gui.UIAction;
import com.datamininglab.viz.gui.UserInterface.MessageType;
import com.datamininglab.viz.gui.swt.controls.DatePicker;
import com.datamininglab.viz.gui.swt.util.ResourceManager;
import com.datamininglab.viz.gui.swt.util.SWTUtilities;

import gnu.trove.map.hash.TIntLongHashMap;
import oracle.jdbc.driver.CRC64;

public class VisitEditor extends Composite implements SelectionListener, Editor<Patient, Visit, ClinicTeam>, QueryCallback, Runnable, MRSViewable {
	private SWTInterface ui;
	private ClinicTeam   defaultTeam;
	private Visit        visit;

	private MutableInt  numScripts;
	private java.util.List<Prescription> scripts = new ArrayList<>();
	
	private Composite   scriptControls, ladsControls;
	private DatePicker  date;
	private Label       scriptDir, scriptTitle, providerLbl;
	private Label       untransComments, untransCommentsLADS, untransEducation;
	private List        scriptList;
	private Text        systolic, diastolic, pulse, glucose, hemoglobin, stdQty;
	private Text        respiration, temperature, height, weight, urine;
	private Text        comments, commentsLads, education;
	private Combo       malaria, strep, team, stdScripts, provider;
	private Button      pregnant, nursing, fupThis, fupNext, fupLADS, fupRefer, showUrine;
	private Button      understandsEd, takingMeds, outOfMeds;
	private UrineEditor urineEditor;
	
	private boolean warnedUser;
	private int     stdCursor;
	private TIntLongHashMap textHashes = new TIntLongHashMap();
	
	public VisitEditor(final SWTInterface ui, ScrolledComposite parent, Integer style) {
		super(parent, style);
		this.ui = ui;
		
		GridLayout layout = new GridLayout(11, false);
		layout.horizontalSpacing = 6;
		setLayout(layout);
		
		ResourceManager rm = ui.getResourceManager();

		MRSControls.spacer(this, SWT.DEFAULT, 4);
		team = MRSControls.combo(this, SWT.BORDER | SWT.READ_ONLY, Str.CLINIC_TEAM, this, 2, 60);
		MRSControls.comboItems(team, ui.getController().getClinicTeams());
		
		team.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (isLADSVisit()) {
					SWTUtilities.setTopControl(ladsControls);
					providerLbl.setData(SWTInterface.TEXT, Str.LADS_NAME);
				} else {
					SWTUtilities.setTopControl(scriptControls);
					providerLbl.setData(SWTInterface.TEXT, Str.PROVIDER);
				}
				TextRefreshDelegate.refresh(providerLbl);
			}
		});
		
		MRSControls.label(this, Str.DATE, this, SWT.RIGHT);
		date = new DatePicker(rm, this, SWT.NONE, true, true);
		MRSControls.label(date, Str.DAY,   null, SWT.CENTER);
		MRSControls.label(date, Str.MONTH, null, SWT.CENTER);
		MRSControls.label(date, Str.YEAR,  null, SWT.CENTER);
		date.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 3));
		
		providerLbl = MRSControls.label(this, Str.PROVIDER, this, SWT.RIGHT);
		providerLbl.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false, 1, 2));
		provider = new Combo(this, SWT.BORDER);
		provider.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false, 3, 2));
		SWTUtilities.addAutoCompleteListeners(provider);
		
		// TODO: Validate that provider is not numeric (common if vitals are 
		// accidentally entered into the provider spot). Currently SWTValidator only
		// supports Text, not Combo.
		
		MRSControls.label(this, Str.MATERNITY, this, SWT.RIGHT).setLayoutData(
				new GridData(SWT.FILL, SWT.TOP, false, false, 1, 2));
		pregnant = MRSControls.check(this, Str.PREGNANT, this);
		pregnant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		
		nursing = MRSControls.check(this, Str.NURSING, this);
		nursing.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		
		MRSControls.separator(rm, this, Str.VITALS, SWT.BOLD | SWT.TOP, 11);
		
		weight = MRSControls.text(this, SWT.NONE, Str.WEIGHT, "kg", this, 1, true, ui);
		ui.new MRSValidator<>(new ChainedValidator<>(
				new NumericValidator(true),
				new RangeValidator<>(true, 1.0, 500.0)), weight);
		
		temperature = MRSControls.text(this, SWT.NONE, Str.TEMPERATURE,
				UnicodeChars.DEGREE_SIGN + "C", this, 1, true, ui);
		// +/- deg C from normal temp.
		ui.new MRSValidator<>(new ChainedValidator<>(
				new NumericValidator(true),
				new RangeValidator<>(true, 32.0, 42.0, 89.6, 107.6)), temperature); 
		
		new Label(this, SWT.NONE).setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
		
		pulse = MRSControls.text(this, SWT.BORDER, Str.PULSE, "/min", this, 1, true, ui);
		ui.new MRSValidator<>(new ChainedValidator<>(
				new IntegralValidator(true),
				new RangeValidator<>(true, 1L, 250L)), pulse);
		
		height = MRSControls.text(this, SWT.BORDER, Str.HEIGHT, "cm", this, 1, true, ui);
		ui.new MRSValidator<>(new ChainedValidator<>(
				new NumericValidator(true),
				new RangeValidator<>(true, 1.0, 250.0)), height);
		
		systolic = MRSControls.text(this, SWT.BORDER, Str.BLOOD_PRESSURE, null, this, 1, true, ui);
		ui.new MRSValidator<>(new ChainedValidator<>(
				new IntegralValidator(true),
				new RangeValidator<>(true, 40L, 240L)), systolic);
		
		Label l = new Label(this, SWT.NONE);
		l.setText("/"); l.setFont(rm.getFontBold());
		
		diastolic = MRSControls.text(this, SWT.BORDER, null, "mmHg", this, 1, false, ui);
		diastolic.setData(SWTInterface.TOOLTIP, Str.BLOOD_PRESSURE);
		ui.new MRSValidator<>(new ChainedValidator<>(
				new IntegralValidator(true),
				new RangeValidator<>(true, 20L, 150L)), diastolic);
		
		respiration = MRSControls.text(this, SWT.BORDER, Str.RESPIRATION, "/min", this, 1, true, ui);
		ui.new MRSValidator<>(new ChainedValidator<>(
				new IntegralValidator(true),
				new RangeValidator<>(true, 6L, 75L)), respiration);
		
		MRSControls.separator(rm, this, Str.LABS, SWT.BOLD, 11);
		glucose = MRSControls.text(this, SWT.BORDER, Str.BLOOD_GLUCOSE, "mg/dl", this, 1, true, ui);
		ui.new MRSValidator<>(new ChainedValidator<>(
				new IntegralValidator(true),
				new RangeValidator<>(true, 10L, 600L)), glucose);
		
		malaria = MRSControls.combo(this, SWT.BORDER, Str.MALARIA,
		                            this, 3, SWTUtilities.DEFAULT_BUTTON_WIDTH);
		malaria.setData(SWTInterface.TEXT, Visit.MALARIA_RESULTS);

		MRSControls.label(this, Str.URINE, this, SWT.RIGHT);
		urine = new Text(this, SWT.BORDER | SWT.READ_ONLY);
			urine.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
			
		hemoglobin = MRSControls.text(this, SWT.BORDER, Str.HEMOGLOBIN, "g/dl", this, 1, true, ui);
		ui.new MRSValidator<>(new ChainedValidator<>(
				new NumericValidator(true),
				new RangeValidator<>(true, 5.0, 20.0)), hemoglobin);
		
		strep = MRSControls.combo(this, SWT.BORDER, Str.STREP,
		                          this, 3, SWTUtilities.DEFAULT_BUTTON_WIDTH);
		strep.setData(SWTInterface.TEXT, Visit.STREP_RESULTS);
		
		new Label(this, SWT.NONE);
		showUrine = SWTUtilities.button(this, SWTUtilities.TEXT, MRSActions.SHOW_URINE_EDITOR, rm, this);
			showUrine.setData(SWTInterface.TEXT, Str.ADD_RESULTS);
			showUrine.setData(SWTInterface.MNEUMONIC, this);
			showUrine.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 2, 1));
		
		Composite stack = new Composite(this, SWT.NONE);
		stack.setLayout(new StackLayout());
		stack.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 11, 1));
		
		scriptControls = new Composite(stack, SWT.NONE);
		scriptControls.setLayout(SWTUtilities.removeMargins(new GridLayout(5, false)));
		
		numScripts = new MutableInt();
		CompoundStr cs = new CompoundStr(Str.PRESCRIPTIONS, " (", numScripts, ")");
		scriptTitle = MRSControls.separator(rm, scriptControls, cs, SWT.BOLD, 11);
		
		Button other = SWTUtilities.button(scriptControls, SWTUtilities.TEXT | SWTUtilities.IMAGE,
				MRSActions.NEW_SCRIPT, rm, this);
			other.setData(SWTInterface.TEXT, Str.NEW_PRESCRIPTION);
			other.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
			
		ToolBar scriptTools = new ToolBar(scriptControls, SWT.FLAT);
			scriptTools.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false));
			SWTUtilities.item(scriptTools, SWT.PUSH, MRSActions.EDIT_SCRIPT,   rm, this);
			SWTUtilities.item(scriptTools, SWT.PUSH, MRSActions.DELETE_SCRIPT, rm, this);
		
		new Label(scriptControls, SWT.SEPARATOR | SWT.VERTICAL).setLayoutData(
				new GridData(SWT.CENTER, SWT.FILL, false, false, 1, 5));
		
		MRSControls.label(scriptControls, Str.STANDARD_SCRIPTS, this, SWT.LEFT).setLayoutData(
				new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 2, 1));
		
		scriptList = new List(scriptControls, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 4);
			gd.heightHint = gd.widthHint = 100;
			scriptList.setLayoutData(gd);		
		
		stdScripts = new Combo(scriptControls, SWT.BORDER);
			stdScripts.setData(MRSActions.ADD);
			gd = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1);
			gd.widthHint = 250;
			stdScripts.setLayoutData(gd);
			stdScripts.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					VisitEditor.this.widgetSelected(e);
				}
			});
			
		stdScripts.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				boolean override;
				switch (e.keyCode) {
					case SWT.ARROW_DOWN: case SWT.ARROW_UP:
					case SWT.ARROW_LEFT: case SWT.ARROW_RIGHT:
						return;
					case SWT.DEL: case SWT.BS:
						override = true; break;
					default:
						override = false; break;
				}
				
				String txt = stdScripts.getText().toLowerCase();
				if (stdCursor <= txt.length()) {
					txt = txt.substring(stdCursor);
				}
				if (txt.length() < 2) { return; }
				
				String[] arr = stdScripts.getItems();
				int i = 0, j = -1;
				for (i = 0; i < arr.length && !override; i++) {
					j = arr[i].toLowerCase().indexOf(txt);
					if (j >= 0) {
						stdCursor = j;
						stdScripts.select(i);
						break;
					}
				}
				
				if (j < 0) {
					stdScripts.deselectAll();
				} else {
					stdScripts.setSelection(new Point(j + txt.length(), arr[i].length()));
				}
				SWTUtilities.selectAndNotify(stdScripts);
			}
		});
			
		new Label(scriptControls, SWT.NONE).setData(SWTInterface.TEXT, Str.QUANTITY);
		
		scriptDir = new Label(scriptControls, SWT.WRAP);
			scriptDir.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 3));
			
		stdQty = new Text(scriptControls, SWT.BORDER);
			stdQty.setEnabled(false);
			stdQty.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
			stdQty.setData(SWTInterface.TOOLTIP, Str.QUANTITY);
			ui.new MRSValidator<>(new NumericValidator(true), stdQty);
			
		final Button add = SWTUtilities.button(scriptControls, SWT.LEFT | SWTUtilities.IMAGE | SWTUtilities.TEXT,
				MRSActions.ADD, rm, this);
			add.setData(SWTInterface.TEXT, Str.ADD);
			add.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			
		stdScripts.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = stdScripts.getSelectionIndex();
				if (index < 0) {
					scriptDir.setText("");
					scriptDir.setToolTipText("");
					stdQty.setText("");
					stdQty.setEnabled(false);
					return;
				}
				
				Prescription p = ui.getController().getStandardScripts()[index];
				float q = p.getQuantity();
				if (q <= 0.0f) {
					stdQty.setText("");
					stdQty.setEnabled(true);
				} else {
					stdQty.setText(String.valueOf(q));
					stdQty.setEnabled(false);
				}
				stdQty.notifyListeners(SWT.KeyUp, new Event());
				
				String dir = VisitEditor.this.ui.getDirectiveText(p);
				scriptDir.setText(dir);
				scriptDir.setToolTipText(dir);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				SWTUtilities.selectAndNotify(add);
			}
		});
		stdQty.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				SWTUtilities.selectAndNotify(add);
			}
		});
			
		MRSControls.separator(rm, scriptControls, Str.FOLLOW_UP_COMMENTS, SWT.BOLD, 5);
		Label followUp = new Label(scriptControls, SWT.NONE);
			followUp.setData(SWTInterface.TEXT, new CompoundStr(Str.FOLLOW_UP, "..."));
			followUp.setData(SWTInterface.MNEUMONIC, this);
			followUp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		MRSControls.label(scriptControls, Str.COMMENTS, this, SWT.LEFT)
			.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 4, 1));
		
		fupThis = MRSControls.check(scriptControls, Str.LATER_THIS_WEEK, this);
			
		comments = new Text(scriptControls, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER | SWT.WRAP);
			comments.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 4));
			comments.addTraverseListener(SWTUtilities.getTraverseListener());
			comments.addSelectionListener(ui);
			
		fupNext  = MRSControls.check(scriptControls, Str.NEXT_CLINIC, this);
		fupLADS  = MRSControls.check(scriptControls, Str.LADS, this);
		fupRefer = MRSControls.check(scriptControls, Str.OUTSIDE_REFERRAL, this);
		
		MRSControls.spacer(scriptControls, SWT.DEFAULT, 1);
		untransComments = newUntranslatedAlert(scriptControls, 4);
		
		ladsControls = new Composite(stack, SWT.NONE);
		ladsControls.setLayout(SWTUtilities.removeMargins(new GridLayout(1, false)));
		
		MRSControls.separator(rm, ladsControls, Str.EDUCATION, SWT.BOLD, 1);
		MRSControls.label(ladsControls, Str.EDUCATION_GIVEN, this, SWT.LEFT);
		education = new Text(ladsControls, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER | SWT.WRAP);
			gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			gd.heightHint = 80;
			education.setLayoutData(gd);
			education.addTraverseListener(SWTUtilities.getTraverseListener());
			education.addSelectionListener(ui);
		untransEducation = newUntranslatedAlert(ladsControls, 1);
		understandsEd = MRSControls.check(ladsControls, Str.EDUCATION_UNDERSTOOD, this);
		
		MRSControls.separator(rm, ladsControls, Str.COMPLIANCE, 1, SWT.BOLD);
		takingMeds = MRSControls.check(ladsControls, Str.TAKING_MEDS, this);
		outOfMeds  = MRSControls.check(ladsControls, Str.FINISHED_MEDS, this);
		MRSControls.label(ladsControls, Str.COMMENTS, this, SWT.LEFT);
		commentsLads = new Text(ladsControls, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER | SWT.WRAP);
			commentsLads.setLayoutData(gd);
			commentsLads.addTraverseListener(SWTUtilities.getTraverseListener());
			commentsLads.addSelectionListener(ui);
		untransCommentsLADS = newUntranslatedAlert(ladsControls, 1);
		
		ladsControls.pack();
		scriptControls.pack();
		SWTUtilities.setTopControl(scriptControls);
		
		// TODO: Tab order
		/*setTabList(new Control[] {
			
		});*/
		
		urineEditor = new UrineEditor(ui, getDisplay());
	}
	
	private static Label newUntranslatedAlert(Composite parent, int hspan) {
		Label l = MRSControls.label(parent, Str.UNTRANSLATED_TEXT, null, SWT.LEFT | SWT.WRAP);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, false, false, hspan, 1);
		gd.widthHint = 100;
		// TODO: This doesn't work when the text wraps, but if we don't specify one the label takes up too much vertical space
		gd.heightHint = 20;
		l.setLayoutData(gd);
		l.setForeground(l.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
		l.setBackground(l.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		return l;
	}
	
	public void setStandardPrescriptions(final Prescription[] arr) {
		stdScripts.setData(SWTInterface.TEXT, arr);
		TextRefreshDelegate.refresh(stdScripts);		
	}
	
	public void setProivders(Collection<String> providers) {
		provider.removeAll();
		for (String s : providers) { provider.add(s); }
	}
	
	@Override
	public void createNew(Patient parent) {
		if (parent == null) {
			throw new IllegalStateException("A visit must be associated with a patient");
		}
		
		set(new Visit(parent));
		
		if (defaultTeam == null) {
			EnumEditor.showClinicTeamEditor(ui);
		} else {
			SWTUtilities.select(team, defaultTeam.toString());
		}
		date.selectToday();
	}
	
	@Override
	public Visit get() {
		if (visit == null) { return null; }
		
		visit.setClinicTeam(ui.getController().getClinicTeams().get(team.getText()));
		
		String p = WordUtils.capitalizeFully(StringUtils.stripToNull(provider.getText()));
		visit.setProvider(p);
		if (provider.getSelectionIndex() < 0 && p != null) { provider.add(p); }
		
		int i = Math.max(0, malaria.getSelectionIndex());
		visit.setMalariaResult(Visit.MALARIA_RESULTS[i]);
		
		i = Math.max(0, strep.getSelectionIndex());
		visit.setStrep(Visit.STREP_RESULTS[i]);

		visit.setDate(date.getSelection());
		
		visit.setSystolic(getInt(systolic));
		visit.setDiastolic(getInt(diastolic));
		visit.setPulse(getInt(pulse));
		visit.setBloodGlucose(getInt(glucose));
		visit.setHemoglobin(getFloat(hemoglobin));
		visit.setRespiration(getInt(respiration));
		visit.setTemperature(getFloat(temperature));
		visit.setHeight(getFloat(height));
		visit.setWeight(getFloat(weight));
		
		visit.setNursing(nursing.getSelection());
		visit.setPregnant(pregnant.getSelection());
		
		visit.setFollowUpThis(fupThis.getSelection());
		visit.setFollowUpNext(fupNext.getSelection());
		visit.setFollowUpLADS(fupLADS.getSelection());
		visit.setFollowUpReferral(fupRefer.getSelection());
		
		Text c = isLADSVisit()? this.commentsLads : this.comments;
		setTextIfChanged(VisitTextType.COMMENTS, c.getText());
		
		if (isLADSVisit()) {
			setTextIfChanged(VisitTextType.LADS_EDUCATION, education.getText());
			
			visit.setTakingMeds(takingMeds.getSelection()? Str.YES : Str.NO);
			visit.setOutOfMeds(outOfMeds.getSelection()? Str.YES : Str.NO);
			visit.setUnderstandsEducation(understandsEd.getSelection()? Str.YES : Str.NO);
		}
		return visit;
	}
	
	private void setTextIfChanged(VisitTextType type, String text) {
		long prevHash = textHashes.get(type.ordinal());
		long currHash = HashUtils.getCRC64(text);
		if (currHash != prevHash) {
			visit.setText(type, ui.getCurrentLanguage(), text);
		}
	}
	
	@Override
	public void set(Visit v) {
		visit = v;
		if (v == null) { return; }
		
		// Don't allow the clinic team to change on existing instances
		team.setEnabled(v.getId() == 0L);
		SWTUtilities.select(team, v.getClinicTeam());
		SWTUtilities.selectAndNotify(team);
		
		provider.setText(StringUtils.defaultString(v.getProvider()));
		
		SWTUtilities.select(malaria, v.getMalariaResult());
		SWTUtilities.select(strep,   v.getStrepResult());
		updateUrineTest(v.getUrineTestResult());
		
		Date d = v.getDate();
		date.setSelection(d.getTime() == Visit.DEFAULT_DATE? new Date() : d);
		
		setInt(systolic,      v.getSystolic());
		setInt(diastolic,     v.getDiastolic());
		setInt(pulse,         v.getPulse());
		setInt(glucose,       v.getBloodGlucose());
		setFloat(hemoglobin,  v.getHemoglobin());
		setInt(respiration,   v.getRespiration());
		setFloat(temperature, v.getTemperatureC());
		setFloat(height,      v.getHeightCm());
		setFloat(weight,      v.getWeightKg());
		
		nursing.setSelection(v.isNursing());
		pregnant.setSelection(v.isPregnant());
		
		fupThis.setSelection(v.followUpThis());
		fupNext.setSelection(v.followUpNext());
		fupLADS.setSelection(v.followUpLADS());
		fupRefer.setSelection(v.followUpReferral());
		
		setDynamicText(v, VisitTextType.COMMENTS, comments, commentsLads);
		setUntranslatedAlert(v, VisitTextType.COMMENTS, untransComments, untransCommentsLADS);
		
		setDynamicText(v, VisitTextType.LADS_EDUCATION, education);
		setUntranslatedAlert(v, VisitTextType.LADS_EDUCATION, untransEducation);
			
		takingMeds.setSelection(v.isTakingMeds() == Str.YES);
		outOfMeds.setSelection(v.isOutOfMeds() == Str.YES);
		understandsEd.setSelection(v.isUnderstandingEducation() == Str.YES);
		
		stdScripts.deselectAll();
	}
	
	private void setDynamicText(Visit v, VisitTextType type, Text... arr) {
		DynamicVisitText dvt = new DynamicVisitText(ui, v, type, textHashes);
		String text = dvt.toString();
		for (Text t : arr) {
			t.setText(text);
			t.setData(SWTInterface.TEXT, dvt);
		}
	}
	
	private void setUntranslatedAlert(Visit v, VisitTextType type, Label... arr) {
		boolean untranslated = v.hasAnyText() && !v.hasText(type, ui.getCurrentLanguage());
		for (Label l : arr) { l.setVisible(untranslated); }
	}
	
	private boolean isLADSVisit() {
		return "LADS".equals(team.getText());
	}
	
	// Asynchronously load this visit's prescriptions if it's persistent
	public void refreshPrescriptions() {
		scripts = new ArrayList<>();
		SWTUtilities.run(getDisplay(), this);
		
		MRSController c = ui.getController();
		if (visit != null && visit.getId() > 0L) {
			c.new PerformQuery(visit, c.getSearchFields().getParentFields(Prescription.class), this);
		}
	}
	
	private static void setInt(Text t, int i) {
		t.setText(i == 0? "" : String.valueOf(i));
		// Trigger validation
		t.notifyListeners(SWT.KeyUp, new Event());
	}
	private static void setFloat(Text t, float f) {
		t.setText(f == 0? "" : String.valueOf(f));
		// Trigger validation
		t.notifyListeners(SWT.KeyUp, new Event());
	}
	private static int getInt(Text t) {
		Object o = t.getData(SWTInterface.RESULT);
		if (o == null || o == SWTInterface.INVALID) { return 0; }
		return ((Long) o).intValue();
	}
	private static float getFloat(Text t) {
		Object o = t.getData(SWTInterface.RESULT);
		if (o == null || o == SWTInterface.INVALID) { return 0.0f; }
		return ((Double) o).floatValue();
	}
	
	@Override
	public Criterion getFilter(Class<?> c) { return null; }
	@Override
	public Order getOrder(Class<?> c) { return Order.asc("id"); }
	
	@Override
	public void handleResultsFor(Query q) {
		Object[] results = q.getResults();
		for (int i = 0; i < results.length; i++) {
			Prescription p = (Prescription) results[i];
			// Replace possible proxy with our initialized instance
			p.setVisit(visit);
			scripts.add(p);
		}
		SWTUtilities.run(getDisplay(), this);
	}
	
	@Override
	public void run() {
		scriptList.removeAll();
		for (Prescription p : scripts) { scriptList.add(p.toString()); }
	
		numScripts.setValue(scripts.size());
		TextRefreshDelegate.refresh(scriptTitle);
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		UIAction a = (UIAction) e.widget.getData();
		if (a == MRSActions.SHOW_URINE_EDITOR) {
			urineEditor.open(this);
		} else if (a == MRSActions.ADD) {
			int idx = stdScripts.getSelectionIndex();
			if (idx < 0) {
				ui.setMessage(MessageType.WARN, Str.ERROR_MUST_SELECT + Str.ADD.toString());
				return;
			}
			
			Double qty = (Double) stdQty.getData(SWTInterface.RESULT);
			if (qty == null || qty == SWTInterface.INVALID) {
				ui.setMessage(MessageType.WARN, Str.ERROR_MUST_ENTER_VAL + Str.ADD.toString());
				return;
			}
			
			Prescription[] arr = ui.getController().getStandardScripts();
			Prescription oldp = arr[idx];
			Prescription newp = new Prescription(visit);
			
			newp.setDiagnosis(oldp.getDiagnosisCode(), oldp.getDiagnosis());
			newp.setDosage(arr[idx].getDosage());
			newp.setForm(arr[idx].getForm());
			newp.setQuantityModifier(arr[idx].getQuantityModifier());
			newp.setTreatment(arr[idx].getTreatment());
			newp.setQuantity(qty.floatValue());
			
			for (PrescriptionDirective oldpd : oldp.getDirectives()) {
				PrescriptionDirective newpd = newp.addDirective(oldpd.getDirective());
				newpd.setQualifier(oldpd.getQualifier());
				
				for (PrescriptionDirectiveBlank oldpdb : oldpd.getBlanks()) {
					PrescriptionDirectiveBlank newpdb = new PrescriptionDirectiveBlank(newpd, oldpdb.getBlank());
					newpdb.setValue(oldpdb.getValue());
				}
			}
			
			scripts.add(newp);
			// Make sure all transient objects are persistent
			ui.getController().new SaveObjects(visit.getPatient(), visit, newp);
			
			stdScripts.deselectAll();
			stdQty.setText("");
			stdQty.notifyListeners(SWT.KeyUp, new Event());
			run();
			
		} else if (a == MRSActions.NEW_SCRIPT) {
			ui.widgetSelected(e);
			
		} else if (a == MRSActions.EDIT_SCRIPT || a == MRSActions.DELETE_SCRIPT) {
			Prescription script = getSelectedScript();
			if (script == null) {
				ui.setMessage(MessageType.WARN, Str.SELECT_PRESCRIPTION.toString());
				warnedUser = true;
				return;
			}
			
			if (warnedUser) {
				ui.setMessage(MessageType.DEFAULT, null);
				warnedUser = false;
			}
			
			if (a == MRSActions.EDIT_SCRIPT) {
				ui.setCurrent(script);
				ui.widgetSelected(e);
			} else if (ui.confirm(Str.DELETE_PRESCRIPTION, script)) {
				script.getVisit().getPrescriptions().remove(script);
				scripts.remove(script);
				run();
				ui.getController().new DeleteObject(script);
			}
		}
	}
	
	private Prescription getSelectedScript() {
		int idx = scriptList.getSelectionIndex();
		return idx < 0? null : scripts.get(idx);
	}
	
	public Point getPopupCoordinates() { return urine.toDisplay(0, 0); }
	public UrineTest getUrineTest() { return visit.getUrineTestResult(); }
	
	public void setUrineTest(UrineTest ut) {
		visit.setUrineTestResult(ut);
		updateUrineTest(ut);
	}
	
	public void deleteUrineTest() {
		updateUrineTest(null);
		
		UrineTest ut = getUrineTest();
		if (ut == null) { return; }
		
		visit.setUrineTestResult(null);
		if (ut.getId() == 0L) { return; }
		
		ui.getController().new DeleteObject(ut);
	}
	
	private void updateUrineTest(UrineTest ut) {
		if (ut == null) {
			urine.setData(SWTInterface.TEXT, Str.NA);
			urine.setText(Str.NA.toString());
			showUrine.setData(SWTInterface.TEXT, Str.ADD_RESULTS);
			showUrine.setText(Str.ADD_RESULTS.toString());			
		} else {
			urine.setData(SWTInterface.TEXT, null);
			urine.setText(ut.toString());
			showUrine.setData(SWTInterface.TEXT, Str.VIEW_RESULTS);
			showUrine.setText(Str.VIEW_RESULTS.toString());
		}
		showUrine.pack();
	}
	
	@Override
	public void setDefault(ClinicTeam obj, boolean isNew) {
		defaultTeam = obj;
		if (visit != null) {
			EnumEditor.selectIfNull(team, visit.getClinicTeam(), obj, isNew);
		}
	}
	
	@Override
	public MRSView getView(UIAction action) {
		return new MRSView(this, getParent(), action.getNameObj(), MRSActions.SAVE_VISIT);
	}
	
	@Override
	public boolean hideView(boolean promptToSave) {
		return ui.handleViewChange(promptToSave, get());
	}
	
	@Override
	public void showView() {
		refreshPrescriptions();
		provider.setFocus();
		ui.setCurrent(visit);
		
		// Don't need to verify patient is saved because they are either editing
		// an existing patient, or coming from the new patient editor, which
		// always closes and saves. See SWTInterface.pushView()
	}
	
	@Override
	public UIAction getSaveAction() { return MRSActions.SAVE_VISIT; }
	@Override
	public UIAction getNewAction() { return MRSActions.NEW_SCRIPT; }
	
	@Override
	public void widgetDefaultSelected(SelectionEvent e) { widgetSelected(e); }
	
	public static class DynamicVisitText {
		private MRSInterface ui;
		private TIntLongHashMap textHashes;
		private Visit v;
		private VisitTextType type;
		
		public DynamicVisitText(MRSInterface ui, Visit v, VisitTextType type, TIntLongHashMap textHashes) {
			this.ui = ui; this.v = v; this.type = type; this.textHashes = textHashes;
		}
		@Override
		public String toString() {
			String s = Objects.toString(v.getText(type, ui.getCurrentLanguage()));
			textHashes.put(type.ordinal(), HashUtils.getCRC64(s));
			return s;
		}
	}
}