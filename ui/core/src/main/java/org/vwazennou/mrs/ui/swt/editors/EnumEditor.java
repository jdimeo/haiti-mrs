/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.ui.swt.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.vwazennou.mrs.dictionary.Str;
import org.vwazennou.mrs.patient.PatientGroup;
import org.vwazennou.mrs.ui.swt.MRSControls;
import org.vwazennou.mrs.ui.swt.SWTInterface;
import org.vwazennou.mrs.visit.ClinicTeam;

import com.datamininglab.foundation.data.lut.LookupTable;
import com.datamininglab.foundation.swt.util.SWTUtilities;
import com.datamininglab.foundation.ui.UserInterface.MessageType;

public final class EnumEditor<T> implements SelectionListener {
	private LookupTable<T, String> table;
	
	private Shell shell;
	private Combo combo;
	private Text  text;
	private SWTInterface ui;
	private Editor<?, ?, T> editor;
	private EnumFactory<T> factory;
	
	private boolean warnedUser;
	
	private EnumEditor(SWTInterface ui, Str title, Str prompt) {
		this.ui = ui;
		
		shell = new Shell(ui.getResourceManager().getDisplay(), SWT.APPLICATION_MODAL | SWT.TOOL);
		shell.setLayout(new GridLayout(1, false));
		shell.setText(title.toString());

		String promptStr = prompt.toString();
		StringBuilder sb = new StringBuilder();
		int j = 0;
		for (int i = 0; j >= 0; i = j + 1) {
			j = promptStr.indexOf(' ', i + 24);
			if (j < 0) {
				sb.append(promptStr.substring(i));
			} else {
				sb.append(promptStr.substring(i, j)).append('\n');
			}
		}
		new Label(shell, SWT.NONE).setText(sb.toString());

		combo = new Combo(shell, SWT.READ_ONLY);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				text.setVisible(isNewValue());
			}
		});
		
		text = new Text(shell, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		text.setVisible(false);
		SWTUtilities.addReplacePromptListener(text, Str.ENUM_NEW_VAL_HERE);

		Button ok = new Button(shell, SWT.PUSH);
		ok.setText(Str.OK.toString());
		GridData gd = new GridData(SWT.CENTER, SWT.BOTTOM, true, false);
		gd.widthHint = SWTUtilities.DEFAULT_BUTTON_WIDTH;
		ok.setLayoutData(gd);
		shell.setDefaultButton(ok);
		ok.addSelectionListener(this);
	}
	
	private EnumEditor<T> setEditor(Editor<?, ?, T> editor) {
		this.editor = editor;
		return this;
	}
	
	public EnumEditor<T> setFactory(EnumFactory<T> factory) {
		this.factory = factory;
		return this;
	}
	
	private EnumEditor<T> setLookupTable(LookupTable<T, String> table) {
		this.table = table;
		MRSControls.comboItems(combo, table);
		combo.add(Str.ENUM_ADD_NEW.toString());
		if (combo.getSelectionIndex() < 0) { combo.select(0); }
		return this;
	}
	
	private EnumEditor<T> open() {
		shell.pack();
		SWTUtilities.centerShell(shell, shell.getDisplay().getActiveShell());
		shell.setVisible(true);
		combo.setFocus();
		return this;
	}
	
	private boolean isNewValue() {
		return combo.getSelectionIndex() == combo.getItemCount() - 1;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		boolean isNew = isNewValue();
		String str;
		if (isNew) {
			str = text.getText();
		} else {
			str = combo.getText();
		}
		
		if (str.isEmpty()) {
			ui.setMessage(MessageType.WARN, Str.ENUM_SEL_ERROR.toString());
			warnedUser = true;
			return;
		} else if (warnedUser) {
			ui.setMessage(MessageType.DEFAULT, null);
		}
		
		T obj = table.get(str);
		if (obj == null) {
			obj = factory.newInstance(str);
			table.add(obj);
		}
		
		editor.setDefault(obj, isNew);
		shell.dispose();
	}
	
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// No actions should be performed for this event
	}
	
	public static EnumEditor<PatientGroup> showPatientGroupEditor(SWTInterface ui) {
		return new EnumEditor<PatientGroup>(ui, Str.PATIENT_GROUP, Str.PATIENT_GROUP_PROMPT)
				.setEditor(ui.getPatientEditor())
				.setFactory(PGF)
				.setLookupTable(ui.getController().getPatientGroups())
				.open();
	}
	public static EnumEditor<ClinicTeam> showClinicTeamEditor(SWTInterface ui) {
		return new EnumEditor<ClinicTeam>(ui, Str.CLINIC_TEAM, Str.CLINIC_TEAM_PROMPT)
				.setEditor(ui.getVisitEditor())
				.setFactory(CTF)
				.setLookupTable(ui.getController().getClinicTeams())
				.open();
	}
	
	public static void selectIfNull(Combo c, Object obj, Object toSel, boolean isNew) {
		String str = toSel.toString();
		if (isNew) {
			c.add(str);
			c.pack();
		}
		if (obj == null && !SWTUtilities.select(c, str)) {
			throw new IllegalStateException("Could not select " + str + " since it was not "
					+ "found in the existing items nor was it flagged as being new");
		}
		SWTUtilities.selectAndNotify(c);
	}
	
	private interface EnumFactory<T> {
		T newInstance(String name);
	}
	private static final EnumFactory<PatientGroup> PGF = name -> new PatientGroup(name);
	private static final EnumFactory<ClinicTeam> CTF = name -> new ClinicTeam(name);
}