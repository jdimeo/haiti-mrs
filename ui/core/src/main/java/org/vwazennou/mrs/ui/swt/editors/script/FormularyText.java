/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.ui.swt.editors.script;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.vwazennou.mrs.dictionary.Str;
import org.vwazennou.mrs.formulary.Formulary;
import org.vwazennou.mrs.formulary.FormularyEntry;
import org.vwazennou.mrs.formulary.FormularyEntry.FormularyEntryType;

import com.datamininglab.foundation.swt.util.SWTUtilities;

public class FormularyText {
	private Text text;
	
	public FormularyText(Composite parent, Label label, Str prompt) {
		text = new Text(parent, SWT.BORDER);
		SWTUtilities.addReplacePromptListener(text, prompt);
		
		FormData fd = new FormData();
		fd.top = new FormAttachment(label, 4);
		fd.left = new FormAttachment(0);
		fd.right = new FormAttachment(100);
		text.setLayoutData(fd);
	}
	
	public Text getText() { return text; }
	
	public void setText(FormularyEntry fe) {
		text.setText(fe == null? "" : fe.toString().toLowerCase());
		text.notifyListeners(SWT.Modify, new Event());
		text.notifyListeners(SWT.FocusOut, new Event());
	}
	
	public void setList(FormularyList list, FormularyEntryType... types) {
		text.addKeyListener(new ArrowListener(list.getList()));
		text.addModifyListener(new QueryListener(list, types));
	}

	/** This queries the formulary and refreshes the corresponding list's items with the result */
	private static final class QueryListener implements ModifyListener {
		private FormularyList list;
		private FormularyEntryType[] types;
		
		private QueryListener(FormularyList list, FormularyEntryType... types) {
			this.list  = list;
			this.types = types;
		}
		
		@Override
		public void modifyText(ModifyEvent e) {
			Text      t = (Text) e.widget;
			Formulary f = list.getFormulary();
			
			if (f == null) {
				System.err.println("Error: no formulary; cannot perform search");
				return;
			}
			
			list.setEntries(f.query(t.getText(), types));
			list.refresh();
		}
	}
	
	/** This dispatches up/down key press events (arrow and page) to the list */
	private static class ArrowListener extends KeyAdapter {
		private Control receiver;
		
		ArrowListener(Control reciever) { this.receiver = reciever; }
		
		@Override
		public void keyPressed(KeyEvent e) {
			switch (e.keyCode) {
				case SWT.ARROW_DOWN:
				case SWT.ARROW_UP:
				case SWT.PAGE_DOWN:
				case SWT.PAGE_UP:
					SWTUtilities.propagate(e, SWT.KeyDown, receiver);
					break;
				default:
					break;
			}
		}
	}
}