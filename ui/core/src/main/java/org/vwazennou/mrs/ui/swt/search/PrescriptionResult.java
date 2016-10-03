/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.ui.swt.search;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.vwazennou.mrs.script.Prescription;
import org.vwazennou.mrs.ui.MRSActions;
import org.vwazennou.mrs.ui.swt.SWTInterface;

import com.datamininglab.commons.icons.eri.IconsMed;
import com.datamininglab.viz.gui.swt.util.ResourceManager;

public class PrescriptionResult extends SearchResult<Prescription> {
	public PrescriptionResult(SearchInterface si, Prescription obj, boolean showButtons) {
		super(si, obj, showButtons);
	}
	public PrescriptionResult(SWTInterface ui, Prescription obj, boolean showButtons) {
		super(ui, obj, showButtons);
	}
	
	@Override
	protected void getContent(ResourceManager rm, Composite comp) {
		comp.setLayout(new FillLayout());
		
		Label l = new Label(comp, SWT.WRAP);
		l.setText(ui.getDirectiveText(obj));
		
		searchItem(rm, MRSActions.EDIT_SCRIPT);
		searchItem(rm, MRSActions.DELETE_SCRIPT);
	}
	
	@Override
	protected String getIcon() { return IconsMed.PILL2; }
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		setContentVisible(!isContentVisible());
	}
}