/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.ui.swt.editors.patient;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import javax.naming.ldap.PagedResultsControl;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.vwazennou.mrs.dictionary.Str;
import org.vwazennou.mrs.patient.Patient;
import org.vwazennou.mrs.patient.PatientGroup;
import org.vwazennou.mrs.ui.MRSActions;
import org.vwazennou.mrs.ui.swt.MRSControls;
import org.vwazennou.mrs.ui.swt.MRSView;
import org.vwazennou.mrs.ui.swt.MRSView.MRSViewable;
import org.vwazennou.mrs.ui.swt.SWTInterface;
import org.vwazennou.mrs.ui.swt.editors.Editor;
import org.vwazennou.mrs.ui.swt.editors.EnumEditor;

import com.datamininglab.viz.gui.UIAction;
import com.datamininglab.viz.gui.swt.controls.DatePicker;
import com.datamininglab.viz.gui.swt.util.ResourceManager;
import com.datamininglab.viz.gui.swt.util.SWTUtilities;


public class PatientEditor extends Composite implements Editor<Object, Patient, PatientGroup>, MRSViewable {
	private SWTInterface ui;
	private PatientGroup defaultGroup;
	private Patient      patient;
	
	private Combo      dobFormat;
	private DatePicker dobPicker;
	private Control[]  dobControls;
	
	private Text     lastName, firstName, address, phone, altPhone, email;
	private Text     childAges, medications, notes;
	private Combo    community, city, gender, group, maritalStatus;
	private Spinner  birthYear, ageYears, ageMonths, children;
	private Button   churchMember, ladsPatient, deceased;
	private Button[] conditionChecks;
	
	public PatientEditor(SWTInterface ui, ScrolledComposite parent, Integer style) {
		super(parent, style);
		this.ui = ui;
		ResourceManager rm = ui.getResourceManager();
		
		GridLayout layout = new GridLayout(5, false);
		layout.horizontalSpacing = 10;
		setLayout(layout);
		MRSControls.spacer(this, SWT.DEFAULT, 3);
		
		group = MRSControls.combo(this, SWT.BORDER | SWT.READ_ONLY, Str.PATIENT_GROUP, this, 1, 0);
		MRSControls.comboItems(group, ui.getController().getPatientGroups());
		
		MRSControls.separator(rm, this, Str.NAME, SWT.BOLD, 5);
		lastName  = MRSControls.text(this, SWT.BORDER, Str.LAST_NAME,       null, this, 2, true, ui);
		firstName = MRSControls.text(this, SWT.BORDER, Str.FIRST_NAME,      null, this, 1, true, ui);
		
		MRSControls.separator(rm, this, Str.CONTACT_INFORMATION, SWT.BOLD, 5);
		address   = MRSControls.text(this,  SWT.BORDER, Str.ADDRESS,         null, this, 2, false, ui);
		phone     = MRSControls.text(this,  SWT.BORDER, Str.PHONE,           null, this, 1, false, ui);
		community = MRSControls.combo(this, SWT.BORDER, Str.COMMUNITY,             this, 2, SWT.DEFAULT);
		altPhone  = MRSControls.text(this,  SWT.BORDER, Str.ALTERNATE_PHONE, null, this, 1, false, ui);
		city      = MRSControls.combo(this, SWT.BORDER, Str.CITY,                  this, 2, SWT.DEFAULT);
		email     = MRSControls.text(this,  SWT.BORDER, Str.E_MAIL,          null, this, 1, false, ui);
		
		SWTUtilities.addAutoCompleteListeners(city);
		
		MRSControls.separator(rm, this, Str.OTHER_INFORMATION, SWT.BOLD, 3);
		MRSControls.separator(rm, this, Str.MEDICAL_HISTORY, SWT.BOLD, 2);
		
		dobFormat = new Combo(this, SWT.READ_ONLY);
		dobFormat.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		dobFormat.setData(SWTInterface.TEXT, new Str[] {
			Str.DOB, Str.BIRTH_YEAR, Str.AGE, Str.NA
		});
		
		dobControls = new Control[4];
		Composite dobStack = new Composite(this, SWT.NONE); {
			dobStack.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 2, 1));
			dobStack.setLayout(new StackLayout());
			
			dobPicker = new DatePicker(rm, dobStack, SWT.NONE, true, true);
			MRSControls.label(dobPicker, Str.DAY,   null, SWT.CENTER);
			MRSControls.label(dobPicker, Str.MONTH, null, SWT.CENTER);
			MRSControls.label(dobPicker, Str.YEAR,  null, SWT.CENTER);
			
			dobControls[0] = dobPicker;
			
			Composite c = new Composite(dobStack, SWT.NONE);
			c.setLayout(SWTUtilities.removeMargins(new GridLayout(1, false)));
			birthYear = new Spinner(c, SWT.BORDER);
			birthYear.setValues(dobPicker.getMaxYear(), dobPicker.getMinYear(),
			                    dobPicker.getMaxYear(), 0, 1, 10);
			dobControls[1] = c;
			
			c = new Composite(dobStack, SWT.NONE);
			c.setLayout(SWTUtilities.removeMargins(new GridLayout(2, false)));
			ageYears = new Spinner(c, SWT.BORDER);
			ageYears.setValues(0, 0, 999, 0, 1, 10);
			ageMonths = new Spinner(c, SWT.BORDER);
			ageMonths.setValues(0, 0, 11, 0, 1, 1);
			MRSControls.label(c, Str.YEARS,  null, SWT.CENTER);
			MRSControls.label(c, Str.MONTHS, null, SWT.CENTER);
			dobControls[2] = c;
			
			dobControls[3] = MRSControls.label(dobStack, Str.NO_DOB, null, SWT.LEFT);
		}
		
		dobFormat.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SWTUtilities.setTopControl(dobControls[dobFormat.getSelectionIndex()]);
			}
		});
		
		ScrolledComposite sc = new ScrolledComposite(this, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL); {
			GridData data = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 5);
			data.heightHint = 0;
			sc.setLayoutData(data);
			sc.setExpandHorizontal(true);
			sc.setShowFocusedControl(true);
			
			Composite c = new Composite(sc, SWT.NONE);
			c.setBackground(rm.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
			c.setLayout(new GridLayout(1, false));
			sc.setContent(c);
			
			Str[] mcs = Patient.MEDICAL_CONDITIONS;
			conditionChecks = new Button[mcs.length];
			int maxWidth = 0;
			for (int i = 0; i < mcs.length; i++) {
				Button b = new Button(c, SWT.CHECK);
				b.setData(SWTInterface.TEXT, mcs[i]);
				b.setBackground(c.getBackground());
				maxWidth = Math.max(maxWidth, b.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
				conditionChecks[i] = b;
			}
			sc.setMinWidth(maxWidth + 6);
			c.setSize(c.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}
		
		gender = MRSControls.combo(this, SWT.BORDER, Str.GENDER,
		                           this, 1, SWTUtilities.DEFAULT_BUTTON_WIDTH);
		gender.setData(SWTInterface.TEXT, Patient.GENDERS);
		
		churchMember = MRSControls.check(this, Str.CHURCH_MEMBER, this);
		
		maritalStatus = MRSControls.combo(this, SWT.BORDER, Str.MARITAL_STATUS,
		                                  this, 1, SWTUtilities.DEFAULT_BUTTON_WIDTH);
		maritalStatus.setData(SWTInterface.TEXT, Patient.MARITAL_STATUSES);
		
		ladsPatient = MRSControls.check(this, Str.LADS_PATIENT, this);
		
		MRSControls.label(this, Str.CHILDREN, this, SWT.RIGHT);
		children = new Spinner(this, SWT.BORDER);
		children.setValues(0, 0, 50, 0, 1, 5);
		GridData data = new GridData(SWT.LEFT, SWT.FILL, true, false);
		data.widthHint = SWTUtilities.DEFAULT_BUTTON_WIDTH;
		children.setLayoutData(data);
		
		deceased = MRSControls.check(this, Str.DECEASED, this);
		
		childAges = MRSControls.text(this, SWT.BORDER, Str.CHILDREN_AGES, null, this, 2, false, ui);
		new Label(this, SWT.NONE);
		MRSControls.label(this, Str.CHILDREN_AGES_HELP, null, SWT.CENTER).setLayoutData(
				new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		
		Label l = new Label(this, SWT.LEFT);
		l.setData(SWTInterface.TEXT, Str.MEDICATIONS);
		l.setData(SWTInterface.MNEUMONIC, this);
		l.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		notes = MRSControls.text(this, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP, Str.NOTES, null, this, 2, false, ui);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd.heightHint = 75;
		notes.setLayoutData(gd);
		notes.addTraverseListener(SWTUtilities.getTraverseListener());
		
		medications = new Text(this, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		medications.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		medications.addTraverseListener(SWTUtilities.getTraverseListener());
		medications.addSelectionListener(ui);
		
		// Tab order
		/*setTabList(new Control[] {
			lastName, firstName,
			address, community, city,
			phone, altPhone, email,
			dobFormat, dobStack,
			gender, maritalStatus, children, childAges,
			churchMember, ladsPatient, deceased,
			sc, medications, notes
		});*/
	}

	public void setCities(Collection<String> cities) {
		city.removeAll();
		for (String s : cities) { city.add(s); }
	}
	
	public void setCommunities(Collection<String> communities) {
		community.removeAll();
		for (String s : communities) { community.add(s); }
	}
	
	// Parameter parent is always null, since patients don't have parent objs
	@Override
	public void createNew(Object parent) {
		set(new Patient());
			
		if (defaultGroup == null) {
			EnumEditor.showPatientGroupEditor(ui);
		} else {
			SWTUtilities.select(group, defaultGroup.toString());
		}
	}
	
	@Override
	public void setDefault(PatientGroup obj, boolean isNew) {
		defaultGroup = obj;
		if (patient != null) {
			EnumEditor.selectIfNull(group, patient.getPatientGroup(), obj, isNew);
		}
	}
	
	@Override
	public void set(Patient p) {
		patient = p;
		if (p == null) { return; }
		
		lastName.setText(p.getLastName());
		firstName.setText(p.getFirstName());
		address.setText(p.getAddress());
		community.setText(p.getCommunity());
		phone.setText(p.getPhone());
		altPhone.setText(p.getAlternatePhone());
		city.setText(p.getCity());
		email.setText(p.getEmail());
		
		SWTUtilities.select(group, p.getPatientGroup());
		
		dobPicker.setSelection(p.getBirthdate());
		birthYear.setSelection(dobPicker.getSelection(Calendar.YEAR));
		
		float age = p.getAge();
		ageYears.setSelection((int) age);
		ageMonths.setSelection((int) ((age - ageYears.getSelection()) * 12.0f));
		
		SWTUtilities.selectAndNotify(dobFormat, p.getBirthdateInMillis() == Patient.DEFAULT_DOB? dobFormat.getItemCount() - 1 : 0);
		
		gender.select(ArrayUtils.indexOf(Patient.GENDERS, p.getGender()));
		maritalStatus.select(ArrayUtils.indexOf(Patient.MARITAL_STATUSES, p.getMaritalStatus()));
		
		children.setSelection(p.getNumberOfChildren());
		childAges.setText(p.getChildAges());
		
		churchMember.setSelection(p.isChurchMember());
		ladsPatient.setSelection(p.isFollowedByLADS());
		deceased.setSelection(p.isDeceased());
		
		for (int i = 0; i < Patient.MEDICAL_CONDITIONS.length; i++) {
			Str condition = Patient.MEDICAL_CONDITIONS[i];
			conditionChecks[i].setSelection(p.hasCondition(condition));
		}
		
		medications.setText(p.getMedications());
		notes.setText(p.getNotes());
	}
	
	@Override
	public Patient get() {
		if (patient == null) { return null; }
		
		patient.setLastName(lastName.getText());
		patient.setFirstName(firstName.getText());
		patient.updateFullName();
		patient.setAddress(address.getText());
		patient.setCommunity(community.getText());
		patient.setPhone(phone.getText());
		patient.setAlternatePhone(altPhone.getText());
		patient.setCity(city.getText());
		patient.setEmail(email.getText());
		
		switch (dobFormat.getSelectionIndex()) {
			case 0:
				patient.setBirthdate(dobPicker.getSelection());
				break;
			case 1:
				patient.setBirthYear(birthYear.getSelection());
				break;
			case 2:
				float m = ageMonths.getSelection() / 12.0f;
				patient.setAge(ageYears.getSelection() + m);
				break;
			default:
				patient.setBirthdate(new Date(Patient.DEFAULT_DOB));
				break;
		}
		
		patient.setPatientGroup(ui.getController().getPatientGroups().get(group.getText()));
		
		int i = Math.max(0, gender.getSelectionIndex());
		patient.setGender(Patient.GENDERS[i]);
		
		i = Math.max(0, maritalStatus.getSelectionIndex());
		patient.setMaritalStatus(Patient.MARITAL_STATUSES[i]);
		
		patient.setNumberOfChildren(children.getSelection());
		patient.setChildAges(childAges.getText());
		
		patient.setChurchMember(churchMember.getSelection());
		patient.setFollowedByLADS(ladsPatient.getSelection());
		patient.setDeceased(deceased.getSelection());
		
		for (i = 0; i < conditionChecks.length; i++) {
			patient.setCondition(Patient.MEDICAL_CONDITIONS[i], conditionChecks[i].getSelection());
		}
		
		patient.setMedications(medications.getText());
		patient.setNotes(notes.getText());
		
		return patient;
	}
	
	@Override
	public MRSView getView(UIAction action) {
		return new MRSView(this, getParent(), action.getNameObj(), getSaveAction(), getNewAction());
	}
	
	@Override
	public boolean hideView(boolean promptToSave) {
		return ui.handleViewChange(promptToSave, get());
	}
	
	@Override
	public UIAction getSaveAction() { return MRSActions.SAVE_PATIENT; }
	@Override
	public UIAction getNewAction() { return MRSActions.NEW_VISIT; }
	
	@Override
	public void showView() {
		lastName.setFocus();
		ui.setCurrent(patient);
	}
}