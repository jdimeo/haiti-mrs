/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.ui.swt.editors.script;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.vwazennou.mrs.formulary.Formulary;
import org.vwazennou.mrs.formulary.FormularyEntry;
import org.vwazennou.mrs.formulary.FormularyEntry.FormularyEntryType;
import org.vwazennou.mrs.ui.swt.SWTInterface;

import com.datamininglab.viz.gui.swt.util.SWTUtilities;

public class FormularyList implements FocusListener {
	private SWTInterface ui;
	private Control label;
	private List list;
	private FormularyEntry[] entries;
	
	public FormularyList(SWTInterface ui, Composite parent, Control label) {
		this(ui, parent, label, label);
	}
	public FormularyList(SWTInterface ui, Composite parent, Control label, Control top) {
		this.ui    = ui;
		this.label = label;
		list = new List(parent, SWT.BORDER | SWT.V_SCROLL);
		
		FormData fd = new FormData();
		fd.top   = new FormAttachment(top, 4);
		fd.left  = new FormAttachment(0);
		fd.right = fd.bottom = new FormAttachment(100); 
		list.setLayoutData(fd);
		list.addFocusListener(this);
	}
	
	@Override
	public void focusGained(FocusEvent e) {
		list.setBackground(e.display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		label.setFont(ui.getResourceManager().getFont("bold", SWT.DEFAULT, SWT.DEFAULT));
	}
	@Override
	public void focusLost(FocusEvent e) {
		list.setBackground(e.display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		label.setFont(ui.getResourceManager().getFont("default", SWT.DEFAULT, SWT.DEFAULT));
	}
	
	public List      getList()      { return list; }
	public Formulary getFormulary() { return ui.getController().getFormulary(); }
	
	public void chain(FormularyEntryType type, FormularyList... prev) {
		prev[prev.length - 1].list.addSelectionListener(new ChainListener(type, this, prev));
	}
	
	public boolean select(FormularyEntry fe) {
		for (int i = 0; entries != null && i < entries.length; i++) {
			if (entries[i] == fe) {
				SWTUtilities.selectAndNotify(list, i);
				return true;
			}
		}
		return false;
	}
	
	public FormularyEntry getSelection() {
		int idx = list.getSelectionIndex();
		if (idx < 0) { return null; }
		return entries[idx];
	}
	
	public void setEntries(FormularyEntry[] entries) {
		this.entries = entries;
	}
	
	public void refresh() {
		list.removeAll();
		if (entries != null) {
			int n = entries.length;
			for (int i = 0; i < n; i++) {
				list.add(entries[i].toString());
			}
			if (n > 0) { list.select(0); }
		}
		SWTUtilities.selectAndNotify(list);
	}
	
	/** This cascades a user's selection from left to right */
	private static class ChainListener implements SelectionListener {
		private FormularyEntryType nextType;
		private FormularyList      nextList;
		private FormularyList[]    lists;
		
		ChainListener(FormularyEntryType nextType, FormularyList nextList, FormularyList... lists) {
			this.nextType  = nextType;
			this.nextList  = nextList;
			this.lists     = lists;
		}
		
		@Override
		public void widgetSelected(SelectionEvent e) {
			FormularyEntry[] selected = new FormularyEntry[lists.length];
			for (int i = 0; i < lists.length; i++) {
				selected[i] = lists[i].getSelection();
				if (selected[i] == null) {
					selected = null; break;
				}
			}
			
			if (selected != null) {
				Formulary f = nextList.ui.getController().getFormulary();
				nextList.setEntries(f.getNext(nextType, selected));
			}
			nextList.refresh();
		}
		
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// No actions should be performed for this event
		}
	}
}