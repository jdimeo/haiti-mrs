/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.ui.swt.search;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.vwazennou.mrs.dictionary.DictionaryEntry.CompoundStr;
import org.vwazennou.mrs.dictionary.Str;
import org.vwazennou.mrs.patient.Patient;
import org.vwazennou.mrs.search.SearchFieldRegistry;
import org.vwazennou.mrs.ui.MRSActions;
import org.vwazennou.mrs.visit.Visit;

import com.datamininglab.foundation.awt.icons.IconsMS;
import com.datamininglab.foundation.awt.icons.IconsMed;
import com.datamininglab.foundation.awt.icons.IconsSilk;
import com.datamininglab.foundation.swt.util.ResourceManager;
import com.datamininglab.foundation.ui.UIUtilities.UIAction;

public class PatientResult extends SearchResult<Patient> {
	private static final int COL2_WIDTH = 128;
	
	public PatientResult(SearchInterface parent, Patient obj) {
		super(parent, obj, true);
	}
	
	@Override
	public void getContent(ResourceManager rm, Composite comp) {
		comp.setLayout(new GridLayout(5, false));
		
		SearchResult.label(rm, comp, Str.ADDRESS, IconsMS.HOME,
				addIfNotNull(obj.getAddress(), ", ", obj.getCommunity()), SWT.DEFAULT, 2);
		SearchResult.label(rm, comp, Str.PHONE, IconsMed.PHONE,
				addIfNotNull(obj.getPhone(), "/", obj.getAlternatePhone()), COL2_WIDTH, 1);
		
		int nv = obj.getVisits().size();
		searchItem(rm, MRSActions.NEW_VISIT);
		searchItem(rm, MRSActions.SHOW_VISITS, new CompoundStr(Str.SHOW_VISITS, " (", nv, ")"), false);
		searchSeparator();
		searchItem(rm, MRSActions.SUMMARIZE_PATIENT, true);
		searchItem(rm, MRSActions.EDIT_PATIENT);
		searchItem(rm, MRSActions.DELETE_PATIENT);
	}
	
	@Override
	public String getIcon() {
		return obj.getGender() == Str.FEMALE? IconsSilk.USER_FEMALE : IconsSilk.USER;
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		UIAction a = (UIAction) e.widget.getData();
		if (a == MRSActions.SHOW_VISITS) {
			SearchFieldRegistry sfr = ui.getController().getSearchFields();
			parent.performQuery(obj, sfr.getParentFields(Visit.class));
		} else {
			ui.setCurrent(obj);
			ui.widgetSelected(e);
		}
	}
	
	private static String addIfNotNull(String s1, String delim, String s2) {
		return (s2 == null || s2.isEmpty())? s1 : (s1 + delim + s2);
	}
}