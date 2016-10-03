/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.ui.swt.search;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.vwazennou.mrs.dictionary.DictionaryEntry.CompoundStr;
import org.vwazennou.mrs.dictionary.Str;
import org.vwazennou.mrs.script.Prescription;
import org.vwazennou.mrs.search.SearchFieldRegistry;
import org.vwazennou.mrs.ui.MRSActions;
import org.vwazennou.mrs.visit.Visit;
import org.vwazennou.mrs.visit.VisitText.VisitTextType;

import com.datamininglab.commons.icons.eri.IconsMed;
import com.datamininglab.commons.icons.ms.IconsMS;
import com.datamininglab.commons.lang.utf.UnicodeChars;
import com.datamininglab.viz.gui.UIAction;
import com.datamininglab.viz.gui.swt.util.ResourceManager;
import com.datamininglab.viz.gui.swt.util.SWTUtilities;

public class VisitResult extends SearchResult<Visit> {
	private static final int WIDTH = 120;
	private static final int SMALL_WIDTH = 50;
	
	public VisitResult(SearchInterface parent, Visit obj) {
		super(parent, obj, true);
	}
	
	@Override
	protected void getContent(ResourceManager rm, Composite comp) {
		comp.setLayout(new GridLayout(3, false));

		Composite vitals = new Composite(comp, SWT.NONE);
		vitals.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		vitals.setLayout(SWTUtilities.removeMargins(new GridLayout(10, false)));
		
		label(rm, vitals, Str.CLINIC_TEAM, IconsMed.PHYSICIAN,
				obj.getClinicTeam(), WIDTH, 1);
		label(rm, vitals, Str.TEMPERATURE, IconsMed.TEMPERATURE,
				obj.getTemperatureC(),
				UnicodeChars.DEGREE_SIGN + "C ("
		      + String.format("%3.1f", obj.getTemperatureF())
		      + UnicodeChars.DEGREE_SIGN + "F)", WIDTH, 1);
		label(rm, vitals, Str.BLOOD_PRESSURE, IconsMed.BLOOD_DROPS, obj.getSystolic(),
				"/" + obj.getDiastolic() + " mmHg", WIDTH, 1);
		label(rm, vitals, Str.PULSE, IconsMed.CARDIOLOGY,
				obj.getPulse(), "/min", SMALL_WIDTH, 1);
		label(rm, vitals, Str.RESPIRATION, IconsMS.CLOCK,
				obj.getRespiration(), "/min", SMALL_WIDTH, 1);
		
		String notes = ObjectUtils.toString(obj.getText(VisitTextType.COMMENTS, ui.getCurrentLanguage()));
		if (!StringUtils.isEmpty(notes)) {
			label(rm, vitals, Str.COMMENTS, IconsMed.NOTE, notes, SWT.DEFAULT, 9);
		}
		
		int np = obj.getPrescriptions().size();
		searchItem(rm, MRSActions.NEW_SCRIPT);
		searchItem(rm, MRSActions.SHOW_SCRIPTS, new CompoundStr(Str.SHOW_PRESCRIPTIONS, " (", np, ")"), false);
		searchSeparator();
		searchItem(rm, MRSActions.EDIT_VISIT, true);
		searchItem(rm, MRSActions.DELETE_VISIT);
	}
	
	@Override
	protected String getIcon() { return IconsSilk.TABLE; }

	@Override
	public void widgetSelected(SelectionEvent e) {
		UIAction a = (UIAction) e.widget.getData();
		if (a == MRSActions.SHOW_SCRIPTS) {
			SearchFieldRegistry sfr = ui.getController().getSearchFields();
			parent.performQuery(obj, sfr.getParentFields(Prescription.class));
		} else {
			ui.setCurrent(obj);
			ui.widgetSelected(e);
		}
	}
	
	private static void label(ResourceManager rm, Composite c, Str str, String icon,
			Object val, String units, int width, int hspan) {
		boolean showVal = true;
		if (val instanceof Number) {
			showVal = ((Number) val).intValue() > 0;
		}
		if (val instanceof Float) {
			val = String.format("%3.1f", val);
		}
		
		label(rm, c, str, icon, showVal? (val + units) : null, width, hspan);
	}
}