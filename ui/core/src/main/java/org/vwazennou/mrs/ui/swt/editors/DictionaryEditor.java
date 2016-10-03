/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.ui.swt.editors;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.vwazennou.mrs.dictionary.Dictionary;
import org.vwazennou.mrs.dictionary.DictionaryEntry;
import org.vwazennou.mrs.dictionary.DictionaryEntry.KeyField;
import org.vwazennou.mrs.dictionary.DictionaryEntry.ValueField;
import org.vwazennou.mrs.dictionary.Language;
import org.vwazennou.mrs.dictionary.Str;
import org.vwazennou.mrs.ui.MRSActions;
import org.vwazennou.mrs.ui.swt.MRSControls;
import org.vwazennou.mrs.ui.swt.MRSView;
import org.vwazennou.mrs.ui.swt.MRSView.MRSViewable;
import org.vwazennou.mrs.ui.swt.SWTInterface;
import org.vwazennou.mrs.ui.swt.TextRefreshDelegate;

import com.datamininglab.foundation.awt.icons.IconsMS;
import com.datamininglab.foundation.swt.controls.data.DataTable;
import com.datamininglab.foundation.swt.dialog.InputBox;
import com.datamininglab.foundation.swt.util.ResourceManager;
import com.datamininglab.foundation.swt.util.SWTUtilities;
import com.datamininglab.foundation.ui.UIUtilities.UIAction;

public class DictionaryEditor extends Composite implements SelectionListener, MRSViewable {
	private static final int DEF_COL_WIDTH = 200;
	
	private Dictionary dictionary;
	private SWTInterface ui;
	private Combo lang;
	private DataTable<DictionaryEntry> table;
	private ValueField valField;
	private Set<Control> addLangControls;
	
	public DictionaryEditor(final SWTInterface ui, Composite parent, int style, Dictionary d) {
		super(parent, style);
		this.ui = ui;
		this.dictionary = d;
		
		addLangControls = new HashSet<>();
		setLayout(new GridLayout(2, false));		
		
		lang = MRSControls.combo(this, SWT.BORDER, Str.LANGUAGE,
		                         this, 1, SWTUtilities.DEFAULT_BUTTON_WIDTH);
		lang.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		lang.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (lang.getSelectionIndex() == lang.getItemCount() - 1) {
					setAddLanguageVisible(true);
				} else {
					setAddLanguageVisible(false);
					
					Language l = getSelectedLanguage();
					if (l != null) {
						valField.setLanguage(l);
						ui.getController().new RefreshDictionary(l, table);
					}
				}
			}
		});
		SWTUtilities.addAutoCompleteListeners(lang);
		((GridData) getChildren()[0].getLayoutData()).grabExcessHorizontalSpace = true;
		
		final Text name = MRSControls.text(this, SWT.NONE, Str.NAME, this, 1, false);
		name.addFocusListener(SWTUtilities.getSelectAllTextListener());
		final Text ctry = MRSControls.text(this, SWT.NONE, Str.COUNTRY, this, 1, false);
		ctry.addFocusListener(SWTUtilities.getSelectAllTextListener());
		
		final Button add = new Button(this, SWT.PUSH);
		add.setData(SWTInterface.TEXT, Str.OK);
		GridData gd = new GridData(SWT.RIGHT, SWT.FILL, false, false, 2, 1);
		gd.widthHint = SWTUtilities.DEFAULT_BUTTON_WIDTH;
		add.setLayoutData(gd);
		add.setEnabled(false);
		add.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String n = name.getText();
				String c = ctry.getText();
				refreshLanguages(dictionary.getOrAddLanguage(n, c));
				ui.refreshLanguageMenu();
			}
		});
		
		Control[] children = getChildren();
		for (int i = 2; i < children.length; i++) {
			addLangControls.add(children[i]);
		}
		
		name.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				add.setEnabled(!name.getText().isEmpty()); 
			}
		});
		
		valField = new ValueField();
		table = new DataTable<>(this, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.MULTI, ui.getResourceManager());
		table.addColumn(new KeyField(), SWT.LEFT | SWT.READ_ONLY, null, DEF_COL_WIDTH);
		table.addColumn(valField, SWT.LEFT | SWT.RESIZE, null, DEF_COL_WIDTH);
		table.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		ToolBar toolbar = new ToolBar(this, SWT.NONE);
		toolbar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		ResourceManager rm = ui.getResourceManager();
		SWTUtilities.item(toolbar, SWT.PUSH, MRSActions.EDIT_TRANSLATION, rm, this);
		SWTUtilities.item(toolbar, SWT.PUSH, MRSActions.DELETE_PHRASE,    rm, this);
		
		refreshLanguages(null);
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		UIAction a = (UIAction) e.widget.getData();
		
		if (a == MRSActions.EDIT_TRANSLATION) {
			for (DictionaryEntry de : table.getSelectedRows()) {
				InputBox ib = new InputBox(lang.getShell(), SWT.NONE);
				ib.setText(Str.EDIT_TRANSLATION.toString());
				ib.setIcon(ui.getResourceManager().getImage(IconsMS.GLOBE));
				ib.setMessage(de.getKey().toString());
				
				String result = ib.open(de.getWord());
				if (result != null) { de.setWord(result); }
			}
			refreshEntries();
		} else if (a == MRSActions.DELETE_PHRASE) {
			for (DictionaryEntry de : table.getSelectedRows()) {
				dictionary.deleteEntry(de);
			}
			ui.getController().new RefreshDictionary(getSelectedLanguage(), table);
		}
	}
	
	private void setAddLanguageVisible(boolean visible) {
		for (Control c : addLangControls) {
			SWTUtilities.setVisibleAndIncluded(c, visible);
		}
		layout(true);
	}
	
	private void refreshLanguages(Language newLang) {
		Collection<Language> set = dictionary.getSupportedLanguages();
		Language[] arr = set.toArray(new Language[set.size()]);
		
		Object[] items = new Object[arr.length + 1];
		int selected = 0;		
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == newLang) { selected = i; }
			items[i] = arr[i].getName();
		}
		items[arr.length] = Str.ADD_LANGUAGE;
		
		lang.setData(SWTInterface.TEXT, items);
		lang.setData(arr);
		
		TextRefreshDelegate.refresh(lang);
		if (newLang != null) { SWTUtilities.selectAndNotify(lang, selected); }
	}
	
	public void refreshEntries() {
		table.refreshFast();
	}
	
	private Language getSelectedLanguage() {
		Language[] langs = (Language[]) lang.getData();
		int index = lang.getSelectionIndex();
		if (index == langs.length || index < 0) { return null; }
		return langs[index];
	}
	
	@Override
	public MRSView getView(UIAction action) {
		return new MRSView(this, this, action.getNameObj(), MRSActions.SAVE_DICTIONARY);
	}
	
	@Override
	public boolean hideView(boolean promptToSave) {
		return ui.handleViewChange(promptToSave, Str.DICTIONARY);
	}
	
	@Override
	public UIAction getSaveAction() { return MRSActions.SAVE_DICTIONARY; }
	@Override
	public UIAction getNewAction() { return null; }
	
	@Override
	public void showView() {
		ui.setSubtitle("");
	}
	
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// No actions should be performed for this event
	}
}