/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.ui.swt.editors.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.vwazennou.mrs.dictionary.DictionaryEntry.CompoundStr;
import org.vwazennou.mrs.dictionary.Str;
import org.vwazennou.mrs.formulary.FormularyEntry;
import org.vwazennou.mrs.formulary.FormularyEntry.FormularyEntryType;
import org.vwazennou.mrs.script.Directive;
import org.vwazennou.mrs.script.DirectiveBlank;
import org.vwazennou.mrs.script.Prescription;
import org.vwazennou.mrs.script.Prescription.DiagnosisCode;
import org.vwazennou.mrs.script.PrescriptionDirective;
import org.vwazennou.mrs.script.PrescriptionDirectiveBlank;
import org.vwazennou.mrs.ui.MRSActions;
import org.vwazennou.mrs.ui.swt.MRSControls;
import org.vwazennou.mrs.ui.swt.MRSView;
import org.vwazennou.mrs.ui.swt.MRSView.MRSViewable;
import org.vwazennou.mrs.ui.swt.SWTInterface;
import org.vwazennou.mrs.ui.swt.editors.Editor;
import org.vwazennou.mrs.visit.Visit;

import com.datamininglab.commons.lang.Utilities;
import com.datamininglab.foundation.data.validation.DataValidators.NumericValidator;
import com.datamininglab.viz.gui.UIAction;
import com.datamininglab.viz.gui.UserInterface.MessageType;
import com.datamininglab.viz.gui.swt.util.ResourceManager;
import com.datamininglab.viz.gui.swt.util.SWTUtilities;

public class PrescriptionEditor extends Composite implements Editor<Visit, Prescription, Object>, SelectionListener, MRSViewable {
	private SWTInterface ui;
	private Prescription prescription;
	private List<Directive> dirList;
	
	private FormularyText trtText;
	private FormularyList trtList, genList, dosList, frmList;
	private Text          quantity, diagnosis, scriptSummary;
	private Label         dirError, qtyForm, dirTop, dirBottom;
	private Combo         dirNum, dxCode, qtyMod;
	private RowData       hrData;
	private GridData      dirData;
	private StackLayout   stack;
	private Button        prevDir, nextDir, delDir, addSaveDir;
	
	private List<Composite>          dirBoxes;
	private List<Text>               dirTitles;
	private List<DirectiveSnippet[]> dirSnippets;

	private int dirIdx;
	private PrescriptionDirective editing;
	
	public PrescriptionEditor(SWTInterface ui, ScrolledComposite parent, Integer style) {
		super(parent, style);
		this.ui = ui;
		ResourceManager rm = ui.getResourceManager();
		
		dirList = ui.getController().getDirectives().getValues();
		Collections.sort(dirList);
		
		setLayout(new GridLayout(5, false));
		MRSControls.spacer(this, 32, 5);
		
		new Label(this, SWT.NONE).setData(SWTInterface.TEXT, Str.DIAGNOSIS);
		
		dxCode = new Combo(this, SWT.BORDER);
		dxCode.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
		
		DiagnosisCode[] codes = Prescription.getDiagnosisCodes();
		Object[] items = new Object[codes.length + 1];
		System.arraycopy(codes, 0, items, 0, codes.length);
		items[codes.length] = new CompoundStr(Str.OTHER, "...");
		dxCode.setData(SWTInterface.TEXT, items);
		
		dxCode.addModifyListener(e -> {
			String text = dxCode.getText();
			if (text.isEmpty()) { return; }
			
			DiagnosisCode[] arr = Prescription.getDiagnosisCodes();
			for (int i = 0; i < arr.length; i++) {
				if (text.equals(arr[i].toString())) {
					diagnosis.setText(arr[i].getDiagnosis().toString());
					return;
				}
				
				if (text.equalsIgnoreCase(arr[i].getCode())) {
					SWTUtilities.selectAndNotify(dxCode, i);
					return;
				}
			}
			if (text.length() < 3) { return; }
			
			text = text.toLowerCase();
			for (int i = 0; i < arr.length; i++) {
				String dx = arr[i].getDiagnosis().toString().toLowerCase();
				if (dx.startsWith(text)) {
					SWTUtilities.selectAndNotify(dxCode, i);
					dxCode.setSelection(new Point(
							arr[i].getCode().length() + 3 + text.length(),
							arr[i].toString().length()));
					return;
				}
			}
		});
		dxCode.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Str dxCodeVal = getSelectedDiagnosis();
				String dx = (dxCodeVal == null)? diagnosis.getText() : dxCodeVal.toString();
				for (Text t : dirTitles) { t.setText(dx); }
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		new Label(this, SWT.NONE).setData(SWTInterface.TEXT, Str.DIAGNOSIS_SPECIFIC);
		diagnosis = new Text(this, SWT.BORDER);
		diagnosis.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		diagnosis.addFocusListener(SWTUtilities.getSelectAllTextListener());
		
		SashForm autoPicks = new SashForm(this, SWT.HORIZONTAL);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 5, 1);
		gd.heightHint = 120;
		autoPicks.setLayoutData(gd);
		
		Composite c = new Composite(autoPicks, SWT.NONE);
		c.setLayout(new FormLayout());
		Label trtLbl = label(c, Str.TREATMENT);
		trtText = new FormularyText(c, trtLbl, Str.SEARCH);
		trtList = new FormularyList(ui, c, trtLbl, trtText.getText());
		trtText.setList(trtList, FormularyEntryType.TREATMENT, FormularyEntryType.TREATMENT_ALIAS);
		
		c = new Composite(autoPicks, SWT.NONE);
		c.setLayout(new FormLayout());
		genList = new FormularyList(ui, c, label(c, Str.GENERIC_TREATMENT));
		
		c = new Composite(autoPicks, SWT.NONE);
		c.setLayout(new FormLayout());
		dosList = new FormularyList(ui, c, label(c, Str.DOSAGE));
		
		c = new Composite(autoPicks, SWT.NONE);
		c.setLayout(new FormLayout());
		frmList = new FormularyList(ui, c, label(c, Str.FORM));
		
		genList.chain(FormularyEntryType.TREATMENT, trtList);
		dosList.chain(FormularyEntryType.DOSAGE,    genList);
		frmList.chain(FormularyEntryType.FORM,      genList, dosList);
		
		frmList.getList().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) { refreshSummary(); }
		});
		
		SelectionListener giveQuantityFocus = new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) { quantity.setFocus(); }
		};
		trtText.getText().addSelectionListener(giveQuantityFocus);
		trtList.getList().addSelectionListener(giveQuantityFocus);
		genList.getList().addSelectionListener(giveQuantityFocus);
		dosList.getList().addSelectionListener(giveQuantityFocus);
		frmList.getList().addSelectionListener(giveQuantityFocus);
		
		FocusListener refreshSummary = new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) { refreshSummary(); }
		};
		
		new Label(this, SWT.NONE).setData(SWTInterface.TEXT, Str.QUANTITY);		
		quantity = new Text(this, SWT.BORDER);
		quantity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		quantity.setData(SWTInterface.TOOLTIP, Str.QUANTITY);
		ui.new MRSValidator<>(new NumericValidator(false), quantity);
		quantity.addFocusListener(SWTUtilities.getSelectAllTextListener());
		quantity.addFocusListener(refreshSummary);
		
		qtyMod = new Combo(this, SWT.READ_ONLY);
		qtyMod.setData(SWTInterface.TOOLTIP, Str.QTY_MODIFIER);
		qtyMod.setData(SWTInterface.TEXT, Prescription.QUANTITY_MODIFIERS);
		qtyMod.addFocusListener(refreshSummary);
		qtyMod.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshSummary();
			}
		});
		
		qtyForm = new Label(this, SWT.NONE);
		qtyForm.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

		scriptSummary = new Text(this, SWT.READ_ONLY | SWT.BORDER | SWT.WRAP);
		scriptSummary.setBackground(getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		scriptSummary.setFont(rm.getFont(10, SWT.NORMAL));
		gd = new GridData(SWT.FILL, SWT.FILL, true, false, 5, 1);
		gd.heightHint = 2 * scriptSummary.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		scriptSummary.setLayoutData(gd);
		scriptSummary.addFocusListener(refreshSummary);
		
		MRSControls.separator(rm, this, Str.DIRECTIVES, SWT.BOLD, 5);
		
		dirTop = new Label(this, SWT.WRAP);
		dirTop.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 5, 1));
		
		prevDir = SWTUtilities.button(this, SWTUtilities.IMAGE, MRSActions.PREV_DIRECTIVE, rm, this);
		prevDir.setData(SWTInterface.TOOLTIP, MRSActions.PREV_DIRECTIVE.getDescriptionObj());
		
		Label l = new Label(this, SWT.NONE);
		l.setData(SWTInterface.TEXT, Str.DIRECTIVE);
		l.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false));
		
		c = new Composite(this, SWT.BORDER);
		c.setLayoutData(dirData = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 3));
		dirData.heightHint = dirData.widthHint = 30;
		c.setLayout(stack = new StackLayout());
		hrData = new RowData(SWT.DEFAULT, SWT.DEFAULT);
		
		delDir = SWTUtilities.button(this, SWTUtilities.IMAGE, MRSActions.DELETE_DIRECTIVE, rm, this);
		delDir.setData(SWTInterface.TOOLTIP, MRSActions.DELETE_DIRECTIVE.getDescriptionObj());
		
		dirNum = new Combo(this, SWT.BORDER);
		dirNum.setData(MRSActions.ADD);
		dirNum.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		dirNum.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectDirective(false);
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				int idx = dirNum.getSelectionIndex();
				if (idx < 0) {
					selectDirective(true);
				} else {
					PrescriptionEditor.this.widgetSelected(e);
				}
			}
		});
		dirNum.addTraverseListener(e -> selectDirective(true));
		dirNum.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				selectDirective(true);
			}
		});
		
		nextDir = SWTUtilities.button(this, SWTUtilities.IMAGE, MRSActions.NEXT_DIRECTIVE, rm, this);
		nextDir.setData(SWTInterface.TOOLTIP, MRSActions.NEXT_DIRECTIVE.getDescriptionObj());
		nextDir.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
		
		addSaveDir = SWTUtilities.button(this, SWTUtilities.TEXT | SWTUtilities.IMAGE, MRSActions.ADD, rm, this);
		addSaveDir.setData(SWTInterface.TEXT, MRSActions.ADD.getNameObj());
		addSaveDir.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

		dirBoxes    = new ArrayList<>(dirList.size());
		dirTitles   = new ArrayList<>(dirList.size());
		dirSnippets = new ArrayList<>(dirList.size());
		for (Directive d : dirList) {
			dirNum.add(String.valueOf(d.getCode()));
			
			Composite dc = new Composite(c, SWT.NONE);
			dc.setLayout(new RowLayout());
			dirBoxes.add(dc);
			
			dirSnippets.add(fillDirective(dc, d));
		}
		
		dirError = new Label(c, SWT.NONE);
		dirError.setData(SWTInterface.TEXT, Str.SELECT_DIRECTIVE);
		SWTUtilities.setTopControl(dirError);
		
		parent.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) { layoutDirective(false); }
		});
		
		dirBottom = new Label(this, SWT.WRAP);
		dirBottom.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 5, 1));
	}
	
	private void refreshDirectives() {
		String top = ui.getDirectiveText(prescription, -1, dirIdx);
		String bot = ui.getDirectiveText(prescription, dirIdx + 1, Integer.MAX_VALUE);
		
		int n = prescription.getDirectives().size();
		prevDir.setVisible(dirIdx > 0);
		nextDir.setVisible(dirIdx < n);
		delDir.setVisible(dirIdx < n);
		
		ResourceManager rm = ui.getResourceManager();
		if (dirIdx < n) {
			SWTUtilities.decorate(addSaveDir, SWTUtilities.IMAGE | SWTUtilities.TEXT, MRSActions.SAVE_DIRECTIVE, rm);
			editing = prescription.getDirectives().get(dirIdx);
			dirNum.setEnabled(false);
		} else {
			SWTUtilities.decorate(addSaveDir, SWTUtilities.IMAGE | SWTUtilities.TEXT, MRSActions.ADD,  rm);
			editing = null;
			dirNum.setEnabled(true);
		}
		refreshEditingDirective();
		
		if (dirIdx < n - 1) {
			SWTUtilities.decorate(nextDir, SWTUtilities.IMAGE, MRSActions.NEXT_DIRECTIVE, rm);
		} else {
			SWTUtilities.decorate(nextDir, SWTUtilities.IMAGE, MRSActions.NEW_DIRECTIVE,  rm);
		}

		GridData gd = (GridData) dirTop.getLayoutData(); 
		if (dirIdx > 0) {
			dirTop.setText(top);
			dirTop.setData(SWTInterface.TEXT, top);
			
			gd.exclude = false;
			SWTUtilities.setDefaultHeight(dirTop);
			dirTop.setVisible(true);
		} else {
			gd.exclude = true;
			dirTop.setVisible(false);
		}
		
		dirBottom.setText(bot);
		dirBottom.setData(SWTInterface.TEXT, bot);
		SWTUtilities.setDefaultHeight(dirBottom);
		
		pack();
	}
	
	private void refreshEditingDirective() {
		if (editing == null) {
			SWTUtilities.selectAndNotify(dirNum, -1);
			return;
		}
		
		Directive d = editing.getDirective();
		dirNum.setText(String.valueOf(d.getCode()));
		selectDirective(true);
		
		int selIdx = dirNum.getSelectionIndex();
		for (PrescriptionDirectiveBlank pdb : editing.getBlanks()) {
			DirectiveSnippet ds = dirSnippets.get(selIdx)[pdb.getBlank().getBlankSequence()];
			ds.setText(pdb.getValue());
		}
	}
	
	@Override
	public void createNew(Visit parent) {
		if (parent == null) { throw new IllegalStateException("A prescription must be associated with a visit"); }
		
		set(new Prescription(parent));
	}
	
	@Override
	public Prescription get() {
		return get(prescription);
	}
	private Prescription get(Prescription p) {
		if (p == null) { return null; }
		
		FormularyEntry frm = frmList.getSelection();
		FormularyEntry dos = dosList.getSelection();
		FormularyEntry gen = genList.getSelection();
		Double qty = (Double) quantity.getData(SWTInterface.RESULT);
		
		p.setDiagnosis(getSelectedDiagnosis(), diagnosis.getText());
		p.setDosage(dos);
		p.setForm(frm);
		p.setTreatment(gen);
		p.setQuantity(qty == null? Float.NaN : qty.floatValue());
		
		int modIdx = qtyMod.getSelectionIndex();
		p.setQuantityModifier(Prescription.QUANTITY_MODIFIERS[modIdx]);
		
		return p;
	}
	
	private Str getSelectedDiagnosis() {
		DiagnosisCode[] codes = Prescription.getDiagnosisCodes();
		int i = dxCode.getSelectionIndex();
		return (i >= 0 && i < codes.length)? codes[i].getDiagnosis() : null;
	}
	
	@Override
	public void set(Prescription t) {
		prescription = t;
		if (t == null) { return; }
		
		if (Float.isNaN(t.getQuantity())) {
			quantity.setText("");
		} else {
			quantity.setText(String.valueOf(t.getQuantity()));
			quantity.notifyListeners(SWT.KeyUp, new Event());
		}
		
		Str qm = t.getQuantityModifier();
		for (int i = 0; i < Prescription.QUANTITY_MODIFIERS.length; i++) {
			if (Prescription.QUANTITY_MODIFIERS[i] == qm) {
				qtyMod.select(i); break;
			}
		}
		
		trtText.setText(t.getTreatment());
		trtList.select(t.getTreatment());
		dosList.select(t.getDosage());
		frmList.select(t.getForm());
		
		Str code = t.getDiagnosisCode();
		DiagnosisCode[] codes = Prescription.getDiagnosisCodes();
		int i = 0;
		for (; i < codes.length; i++) {
			if (codes[i].getDiagnosis() == code) { break; }
		}
		
		// If the code is "Other.." we need to set the custom diagnosis
		// first for when it gets copied to directive titles
		diagnosis.setText(t.getDiagnosis());
		
		// Select the code and let it trigger refreshing the custom diagnosis box and all the title
		// boxes for the directives
		SWTUtilities.selectAndNotify(dxCode, i);
		
		// If the code is custom, we need to now reset it since it was changed
		// by triggering the code selection logic
		diagnosis.setText(t.getDiagnosis());
		
		dirIdx = t.getDirectives().size();
	}
	
	private void selectDirective(boolean parseText) {
		if (parseText) {
			String s = dirNum.getText();
			int i = ArrayUtils.indexOf(dirNum.getItems(), s);
			if (i >= 0) {
				dirNum.select(i);
			} else {
				dirNum.deselectAll();
			}
		}
		
		int idx = dirNum.getSelectionIndex();
		SWTUtilities.setTopControl(idx < 0? dirError : dirBoxes.get(idx));
		layoutDirective(parseText);
	}
	
	// TODO: this logic only works for one title blank
	private DirectiveSnippet[] fillDirective(Composite parent, Directive dir) {
		DirectiveSnippet[] ret = null;
		
		int blank = 0;
		if (dir.getBlanks().isEmpty()) {
			// Use a dummy blank for the title, then the text will be handled below
			fillDirective(parent, new DirectiveBlank(dir), blank, true).setTitle(true);
			// Insert the horizontal rule that separates the title and text
			new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(hrData);
		} else {
			ret = new DirectiveSnippet[dir.getBlanks().size()];
			
			// Iterate through each blank, inserting a horizontal rule after the title blank
			int i = 0;
			for (DirectiveBlank db : dir.getBlanks()) {
				DirectiveSnippet ds = fillDirective(parent, db, blank++, false);
				ret[i++] = ds;
				
				if (ds.isTitle()) {
					// Reset blank sequence since we're switching to the directive's text
					blank = 0;
					new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(hrData);
				}
			}
		}
		
		// Account for any trailing text after the last blank (or the entire text if no blanks)
		Label l = new Label(parent, SWT.WRAP);
		l.setData(SWTInterface.TEXT, new DirectiveSnippet(ui, dir, null, blank));
		l.setLayoutData(new RowData());
		
		return ret;
	}
	
	private DirectiveSnippet fillDirective(Composite parent, DirectiveBlank db, int blank, boolean dummy) {
		DirectiveSnippet ds = new DirectiveSnippet(ui, db.getDirective(), db.getType(), blank);
		
		Label l = new Label(parent, SWT.WRAP);
		l.setData(SWTInterface.TEXT, ds);
		l.setLayoutData(new RowData());
		
		if (dummy) { return ds; }
		
		Control c;
		RowData rd = new RowData();
		switch (db.getType()) {
			case TITLE_TEXT:
				c = new Text(parent, SWT.BORDER);
				c.addFocusListener(SWTUtilities.getSelectAllTextListener());
				ds.setTitle(true);
				rd.width = 140;
				dirTitles.add((Text) c);
				break;
			case NUMERIC:
				c = new Text(parent, SWT.BORDER);
				c.addFocusListener(SWTUtilities.getSelectAllTextListener());
				ui.new MRSValidator<>(new NumericValidator(true), (Text) c);
				rd.width = 40;
				break;
			case ENUMERATION:
				Combo combo = new Combo(parent, SWT.BORDER | SWT.READ_ONLY);
				String[] items    = db.getDetail().split(",");
				Object[] itemsStr = new Str[items.length];
				for (int j = 0; j < items.length; j++) {
					Str str = Utilities.valueOf(Str.class, items[j], null);
					itemsStr[j] = str == null? items[j] : str;
				}
				combo.setData(SWTInterface.TEXT, itemsStr);
				c = combo;
				break;
			case TEXT: default:
				c = new Text(parent, SWT.BORDER);
				c.addFocusListener(SWTUtilities.getSelectAllTextListener());
				rd.width = 100;
				break;
		}
		c.setLayoutData(rd);
		ds.setBlankControl(c);
		return ds;
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		UIAction a = (UIAction) e.widget.getData();
		if (a == MRSActions.ADD || a == MRSActions.SAVE_DIRECTIVE) {
			int idx = dirNum.getSelectionIndex();
			if (idx < 0) {
				ui.setMessage(MessageType.WARN, Str.ERROR_DIR_NUM.toString());
				return;
			}
			
			if (editing == null) {
				Directive dir = dirList.get(idx);
				PrescriptionDirective pd = prescription.addDirective(dir);
				
				for (DirectiveBlank db : dir.getBlanks()) {
					PrescriptionDirectiveBlank pdb = new PrescriptionDirectiveBlank(pd, db);
					DirectiveSnippet ds = dirSnippets.get(idx)[db.getBlankSequence()]; 
					pdb.setValue(ds.getText());
				}
			} else {
				for (PrescriptionDirectiveBlank pdb : editing.getBlanks()) {
					int i = pdb.getBlank().getBlankSequence();
					pdb.setValue(dirSnippets.get(idx)[i].getText());
				}
			}
			
			dirIdx = prescription.getDirectives().size();
		} else if (a == MRSActions.PREV_DIRECTIVE) {
			dirIdx = Math.max(dirIdx - 1, 0);
		} else if (a == MRSActions.NEXT_DIRECTIVE || a == MRSActions.NEW_DIRECTIVE) {
			dirIdx = Math.min(dirIdx + 1, prescription.getDirectives().size());
		} else if (a == MRSActions.DELETE_DIRECTIVE) {
			if (!ui.confirm(Str.DELETE_DIRECTIVE, editing)) { return; }
			
			prescription.getDirectives().remove(editing);
			dirIdx = prescription.getDirectives().size();
		} else {
			ui.setMessage(MessageType.ERROR, Str.UNSUPPORTED_ACTION + ": " + a);
		}
		refreshDirectives();
	}
	
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// No actions should be performed for this event
	}
	
	private void layoutDirective(boolean giveFocusToFirstBlank) {
		Composite dirBox = stack.topControl.getParent();
		Point size = dirBox.getSize();
		hrData.width = size.x - 10;

		if (stack.topControl instanceof Composite) {
			Composite c = (Composite) stack.topControl;
			Control[] children = c.getChildren();
			
			boolean firstBlank = true;
			for (int i = 0; i < children.length; i++) {
				Control child = children[i];
				
				if (giveFocusToFirstBlank && firstBlank && child instanceof Text) {
					SWTUtilities.run(getDisplay(), new FocusForcer(child));
					firstBlank = false;
				}
				
				if (!(child instanceof Label)) { continue; }
				
				RowData rd = (RowData) child.getLayoutData();
				if (rd == null || rd == hrData) { continue; }
			
				Point p = child.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				rd.width = p.x > hrData.width? hrData.width : SWT.DEFAULT;
			}
			c.layout();
		}
		
		Point p = dirBox.computeSize(size.x, SWT.DEFAULT);
		dirData.heightHint = p.y;
		
		p = computeSize(getSize().x, SWT.DEFAULT);
		setSize(p);
	}
	
	@Override
	public void setDefault(Object obj, boolean isNew) {
		// There are no enums associated with prescriptions
	}
	
	private static Label label(Composite parent, Str label) {
		Label l = new Label(parent, SWT.NONE);
		l.setData(SWTInterface.TEXT, label);
		
		FormData fd = new FormData();
		fd.top = fd.left = new FormAttachment(0);
		fd.right = new FormAttachment(100);
		l.setLayoutData(fd);
		
		return l;
	}
	
	private void refreshSummary() {
		Prescription temp = get(new Prescription());

		FormularyEntry form = temp.getForm();
		qtyForm.setText(form == null? Str.NA.toString()
		              : WordUtils.capitalize(form.toString()));
		
		String s = temp.toString();
		scriptSummary.setText(s);
		scriptSummary.setToolTipText(s);
	}
	
	@Override
	public MRSView getView(UIAction action) {
		return new MRSView(this, getParent(), action.getNameObj(), MRSActions.SAVE_SCRIPT);
	}
	
	@Override
	public boolean hideView(boolean promptToSave) { 
		return ui.handleViewChange(promptToSave, get());
	}
	
	@Override
	public void showView() {
		refreshSummary();
		refreshDirectives();
		dxCode.setFocus();
		ui.setCurrent(prescription);
		
		Visit v = prescription.getVisit();
		ui.getController().new SaveObjects(v.getPatient(), v);
		ui.setSubtitle(v.toString());
	}
	
	@Override
	public UIAction getSaveAction() { return MRSActions.SAVE_SCRIPT; }
	@Override
	public UIAction getNewAction() { return null; }
	
	private static class FocusForcer implements Runnable {
		private Control c;
		FocusForcer(Control c) { this.c = c; }
		
		@Override
		public void run() { c.forceFocus(); }
	}
}