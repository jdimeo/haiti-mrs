/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.ui.swt;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.vwazennou.mrs.HaitiMRS;
import org.vwazennou.mrs.MRSController;
import org.vwazennou.mrs.data.Option;
import org.vwazennou.mrs.dictionary.Dictionary;
import org.vwazennou.mrs.dictionary.Language;
import org.vwazennou.mrs.dictionary.Str;
import org.vwazennou.mrs.patient.Patient;
import org.vwazennou.mrs.script.Prescription;
import org.vwazennou.mrs.task.excel.DataExport.ExportType;
import org.vwazennou.mrs.task.excel.DataExport.SortOrder;
import org.vwazennou.mrs.ui.MRSActions;
import org.vwazennou.mrs.ui.MRSInterface;
import org.vwazennou.mrs.ui.swt.editors.DictionaryEditor;
import org.vwazennou.mrs.ui.swt.editors.EnumEditor;
import org.vwazennou.mrs.ui.swt.editors.TabularData;
import org.vwazennou.mrs.ui.swt.editors.patient.PatientEditor;
import org.vwazennou.mrs.ui.swt.editors.patient.PatientSummary;
import org.vwazennou.mrs.ui.swt.editors.script.PrescriptionEditor;
import org.vwazennou.mrs.ui.swt.editors.visit.VisitEditor;
import org.vwazennou.mrs.ui.swt.search.SearchInterface;
import org.vwazennou.mrs.visit.Visit;

import com.datamininglab.commons.geo.CountryDatabase;
import com.datamininglab.commons.geo.CountryMetadata;
import com.datamininglab.commons.icons.eri.IconsFlags;
import com.datamininglab.commons.lang.ReflectionUtils;
import com.datamininglab.commons.lang.Utilities;
import com.datamininglab.commons.logging.LogContext;
import com.datamininglab.foundation.data.validation.DataValidator;
import com.datamininglab.viz.gui.UIAction;
import com.datamininglab.viz.gui.swt.controls.StatusBar;
import com.datamininglab.viz.gui.swt.dialog.InputBox;
import com.datamininglab.viz.gui.swt.dialog.OptionBox;
import com.datamininglab.viz.gui.swt.util.ResourceManager;
import com.datamininglab.viz.gui.swt.util.SWTUtilities;
import com.datamininglab.viz.gui.swt.util.SWTValidator;
import com.datamininglab.viz.gui.util.CountryFlags;

public class SWTInterface extends MRSInterface implements SelectionListener {
	private static final Object UI_READY  = new Object();
	public  static final String MNEUMONIC = "MNEUMONIC";
	public  static final String TEXT      = "TEXT";
	public  static final String TOOLTIP   = "TOOLTIP";
	public  static final String RESULT    = "RESULT";
	public  static final Object INVALID   = Double.NaN;
	
	private static final DateFormat DF_MONTH = new SimpleDateFormat("yyyy-MM");
	
	private static CountryDatabase countryData = new CountryDatabase();
	
	private Display display;
	private Shell shell, splash;
	private ResourceManager rm;
	private StatusBar status;
	
	private Label title, subtitle;
	private Button close;
	private Button[] topActions;
	private SearchInterface search;
	
	private volatile PatientEditor      patientEditor;
	private volatile PatientSummary     patientSummary;
	private volatile VisitEditor        visitEditor;
	private volatile PrescriptionEditor scriptEditor;
	private volatile DictionaryEditor   dictEditor;
	private volatile TabularData          dataTable;
	
	private MenuItem newItem, saveItem;
	private MenuItem lang;
	private Menu langMenu;
	private MenuItem[] langItems;
	
	private Color normal;
	private Color error;
	private Set<Text> invalid;
	
	private LinkedList<MRSView> views;
	
	public SWTInterface(MRSController c) { super(c); }
	
	@Override
	public void initialize() {
		display = new Display();
		rm = new ResourceManager(display);
		
		// Create and show splash screen
		splash = Splash.open(null, rm, SWT.TOOL, "Initializing interface...");
		
		// Check valid data path
		changeDataPath(splash, true);
		
		// Open DB connection, fill dictionary, lookup tables, etc.
		MRSController c = getController().initialize();
		
		// Fields used by data validation
		invalid = new HashSet<>();
		error   = rm.getColor(255, 200, 200);
		normal  = rm.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		
		// Root shell
		shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setData(TEXT, Str.APP_TITLE);
		shell.setSize(700, 600);
		try {
			shell.setImages(rm.loadImages(HaitiMRS.class, "/hmrs.ico"));
		} catch (IOException e) {
			LogContext.warning(e, "Error loading application icon");
			shell.setImage(rm.getImage(IconsFlags.FLAG_HAITI));
		}
		shell.setLayout(new FormLayout());
		
		// Menu
		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);
		createMenus(menu);
		
		// Toolbar on top
		//CoolBar bar = new CoolBar(shell, SWT.NONE);
		//createToolbars(bar);
		
		// Main application area- stacked view
		final Composite main = new Composite(shell, SWT.NONE);
		main.setLayout(SWTUtilities.removeMargins(new GridLayout(1, false)));
		
		Canvas titleBox = new Canvas(main, SWT.NONE);
		titleBox.setLayout(new GridLayout(3, false));
		titleBox.setBackground(rm.getColor(24));
		titleBox.setForeground(rm.getColor(31, 73, 125));
		SWTUtilities.exclude(titleBox);
		
		titleBox.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				Point p = ((Control) e.widget).getSize();
				p.x -= 1; p.y -= 1;
				e.gc.setLineWidth(2);
				e.gc.setLineCap(SWT.CAP_ROUND);
				e.gc.drawLine(0, p.y, p.x, p.y);
				e.gc.drawLine(p.x, 0, p.x, p.y);
			}
		});
		
		close = SWTUtilities.button(titleBox, SWTUtilities.IMAGE, MRSActions.CLOSE, rm, this);
		close.setBackground(titleBox.getBackground());
		close.setLayoutData(new GridData(20, 20));
		
		title = new Label(titleBox, SWT.NONE);
		title.setFont(rm.getFont(11, SWT.BOLD));
		title.setBackground(titleBox.getBackground());
		
		subtitle = new Label(titleBox, SWT.NONE);
		subtitle.setBackground(titleBox.getBackground());
		
		topActions = new Button[0];
		
		titleBox.pack();
		titleBox.setLocation(0, 0);
		
		Composite stack = new Composite(main, SWT.NONE);
		stack.setLayout(new StackLayout());
		stack.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		search = new SearchInterface(this, stack, SWT.NONE);
		search.pack();
		
		patientEditor  = newEditor(stack, PatientEditor.class);
		patientSummary = newEditor(stack, PatientSummary.class);
		visitEditor    = newEditor(stack, VisitEditor.class);
		scriptEditor   = newEditor(stack, PrescriptionEditor.class);
		dictEditor     = new DictionaryEditor(this, stack, SWT.NONE, getController().getDictionary());
		dataTable      = new TabularData(this, stack, SWT.NONE);
		
		patientEditor.setCities(c.getUniqueCities());
		patientEditor.setCommunities(c.getUniqueCommunities());
		visitEditor.setStandardPrescriptions(c.getStandardScripts());
		visitEditor.setProivders(c.getUniqueProviders());
		
		views = new LinkedList<>();
		titleBox.moveAbove(null);
		handle(MRSActions.SHOW_SEARCH);
		
		// Status bar on bottom
		status = new StatusBar(getController(), shell, SWT.NONE);
	
		// Layout
		FormData fd = new FormData();
		fd.left   = new FormAttachment(0);
		fd.right  = new FormAttachment(100);
		fd.bottom = new FormAttachment(100);
		status.getComposite().setLayoutData(fd);
		
		fd = new FormData();
		fd.top    = new FormAttachment(0);
		fd.left   = new FormAttachment(0);
		fd.right  = new FormAttachment(100);
		fd.bottom = new FormAttachment(status.getComposite());
		main.setLayoutData(fd);
		
		// Set interface with default language
		refreshText();
		
		// Close the splash screen
		splash.dispose();
	}
	
	@Override
	public void start() {
		// Open and poll for events until the window is closed
		SWTUtilities.centerShell(shell);
		SWTUtilities.open(shell);
		display.dispose();
		Option.store();
		
		synchronized (UI_READY) {
			UI_READY.notifyAll();
		}
	}
	
	public ResourceManager getResourceManager() { return rm; }
	
	// It is possible for another thread to access our editors before they've been initialized.
	// Wait for the start() method to finish before returning from any of these getters
	public PatientEditor getPatientEditor() {
		checkUI(); return patientEditor;
	}
	public VisitEditor getVisitEditor() {
		checkUI(); return visitEditor;
	}
	public PrescriptionEditor getPrescriptionEditor() {
		checkUI(); return scriptEditor;
	}
	public DictionaryEditor getDictionaryEditor() {
		checkUI(); return dictEditor;
	}
	
	private void checkUI() {
		// Dictionary editor is the last editor to be initialized
		if (dictEditor == null) { Utilities.wait(UI_READY, 1L, TimeUnit.MINUTES); }
	}
	
	@Override
	public void refreshDictionaryEntries() {
		getDictionaryEditor().refreshEntries();
	}
	
	private <T extends Composite> T newEditor(Composite parent, Class<T> c) {
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		T t = ReflectionUtils.newInstance(c, ReflectionUtils.arg(this),
				ReflectionUtils.arg(sc), ReflectionUtils.arg(SWT.NONE));
		sc.setContent(t); sc.setExpandHorizontal(true);
		sc.setMinWidth(t.computeSize(SWT.DEFAULT, 1200).x);
		t.pack();
		return t;
	}
	
	private void createMenus(Menu parent) {
		Menu file = menu(parent, Str.FILE);
		newItem  = SWTUtilities.item(file, SWT.PUSH, MRSActions.NEW_PATIENT, rm, this);
		saveItem = SWTUtilities.item(file, SWT.PUSH, MRSActions.SAVE_PATIENT, rm, this);
		saveItem.setEnabled(false);
		SWTUtilities.item(file, SWT.PUSH, MRSActions.CLOSE,              rm, this);
		SWTUtilities.separator(file);
		SWTUtilities.item(file, SWT.PUSH, MRSActions.EXIT,               rm, this);
		
		Menu tools = menu(parent, Str.TOOLS);
		SWTUtilities.item(tools, SWT.PUSH, MRSActions.SHOW_SEARCH,       rm, this);
		SWTUtilities.item(tools, SWT.PUSH, MRSActions.SHOW_DATA_TABLE,   rm, this);
		SWTUtilities.separator(tools);
		SWTUtilities.item(tools, SWT.PUSH, MRSActions.EXPORT_PT_IDX,     rm, this);
		SWTUtilities.item(tools, SWT.PUSH, MRSActions.EXPORT_TO_EXCEL,   rm, this);
		SWTUtilities.item(tools, SWT.PUSH, MRSActions.EXPORT_MED_REPORT, rm, this);
		SWTUtilities.item(tools, SWT.PUSH, MRSActions.EXPORT_DIR_REPORT, rm, this);
		SWTUtilities.separator(tools);
		SWTUtilities.item(tools, SWT.PUSH, MRSActions.SET_DATA_PATH,     rm, this);
		SWTUtilities.item(tools, SWT.PUSH, MRSActions.SET_CLINIC_TEAM,   rm, this);
		SWTUtilities.item(tools, SWT.PUSH, MRSActions.SET_PATIENT_GROUP, rm, this);
		
		lang = new MenuItem(tools, SWT.CASCADE);
		lang.setText(Str.LANGUAGE.toString());
		lang.setData(TEXT, Str.LANGUAGE);
		langMenu = new Menu(lang);
		lang.setMenu(langMenu);
		refreshLanguageMenu();
		
		SWTUtilities.item(tools, SWT.PUSH, MRSActions.EDIT_DICTIONARY,   rm, this);
		
		Menu help = menu(parent, Str.HELP);
		SWTUtilities.item(help,  SWT.PUSH, MRSActions.ABOUT,             rm, this);
	}
	
	private Menu menu(Menu parent, Str text) {
		Menu m = SWTUtilities.menu(parent, null);
		MenuItem mi = m.getParentItem();
		mi.setData(TEXT, text);
		mi.setData(MNEUMONIC, shell);
		return m;
	}
	
	public void refreshLanguageMenu() {
		for (int i = 0; langItems != null && i < langItems.length; i++) {
			langItems[i].dispose();
		}
		
		SelectionListener langSel = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MenuItem mi = (MenuItem) e.widget;
				
				for (int i = 0; i < langItems.length; i++) {
					if (langItems[i] != mi) { langItems[i].setSelection(false); }
				}
				
				lang.setImage(mi.getImage());
				getController().getDictionary().setLanguage((Language) mi.getData());
				refreshText();
			}
		};
		
		Dictionary d = getController().getDictionary();
		for (Language l : d.getSupportedLanguages()) {
			CountryMetadata[] cm = countryData.find(l.getCountry());
			String flag = CountryFlags.getFlagIcon(cm == null? "" : cm[0].getName());
			
			MenuItem mi = new MenuItem(langMenu, SWT.CHECK);
			mi.setData(l);
			mi.setText(l.getName());
			mi.setImage(rm.getImage(flag));
			mi.addSelectionListener(langSel);
			
			if (d.getLanguage() == l) {
				mi.setSelection(true);
				lang.setImage(mi.getImage());
			}
		}
		langItems = langMenu.getItems();
	}
	
	@Override
	public boolean confirm(Str action, Object o) {
		MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_WARNING);
		String s = action.toString().toLowerCase();
		mb.setText(Str.CONFIRM + " " + s);
		mb.setMessage(Str.CONFIRM_PROMPT + s + "?\n" + o);
		return mb.open() == SWT.YES;
	}
	
	@Override
	public void setMessage(MessageType type, String message) {
		if (status == null) { return; }
		status.setMessage(type, message);
	}
	
	@Override
	public void setStatus(String message, long progress, long size, boolean cancelable) {
		if (status == null) { return; }
		status.setProgress(message, progress, size);
		status.setCancelEnabled(cancelable);
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		handle((UIAction) e.widget.getData());
	}
	private void handle(UIAction a) {
		if (a == null) { return; }
		
		if (a == MRSActions.CLOSE) {
			popView(true, true);
			
		} else if (a == MRSActions.SHOW_SEARCH) {
			pushView(search.getView(a));
			
		} else if (a == MRSActions.NEW_PATIENT) {
			getPatientEditor().createNew(null);
			pushView(getPatientEditor().getView(a));
			
		} else if (a == MRSActions.EDIT_PATIENT) {
			getPatientEditor().set((Patient) getCurrent());
			pushView(getPatientEditor().getView(a));
			
		} else if (a == MRSActions.SAVE_PATIENT) {
			if (popView(false, true)) { getPatientEditor().set(null); }
			
		} else if (a == MRSActions.SUMMARIZE_PATIENT) {
			patientSummary.setPatient((Patient) getCurrent());
			pushView(patientSummary.getView(a));
			
		} else if (a == MRSActions.NEW_VISIT) {
			getVisitEditor().createNew((Patient) getCurrent());
			pushView(getVisitEditor().getView(a));
			
		} else if (a == MRSActions.EDIT_VISIT) {
			getVisitEditor().set((Visit) getCurrent());
			pushView(getVisitEditor().getView(a));
			
		} else if (a == MRSActions.SAVE_VISIT) {
			if (popView(false, true)) { getVisitEditor().set(null); }
			
		} else if (a == MRSActions.NEW_SCRIPT) {
			getPrescriptionEditor().createNew((Visit) getCurrent());
			pushView(getPrescriptionEditor().getView(a));
			
		} else if (a == MRSActions.EDIT_SCRIPT) {
			getPrescriptionEditor().set((Prescription) getCurrent());
			pushView(getPrescriptionEditor().getView(a));
			
		} else if (a == MRSActions.SAVE_SCRIPT) {
			if (popView(false, true)) { getPrescriptionEditor().set(null); }
			
		} else if (a == MRSActions.EDIT_DICTIONARY) {
			pushView(getDictionaryEditor().getView(a));
			
		} else if (a == MRSActions.SHOW_DATA_TABLE) {
			pushView(dataTable.getView(a));
			
		} else if (a == MRSActions.SAVE_DICTIONARY) {
			if (popView(false, true)) { getController().new SaveDictionary(); }
			
		} else if (a == MRSActions.SET_PATIENT_GROUP) {
			EnumEditor.showPatientGroupEditor(this);
		} else if (a == MRSActions.SET_CLINIC_TEAM) {
			EnumEditor.showClinicTeamEditor(this);
			
		} else if (a == MRSActions.EXPORT_TO_EXCEL) {
			FileDialog fd = new FileDialog(shell, SWT.SAVE);
			fd.setFilterExtensions(new String[] {"*.xls", "*.*"});
			fd.setFilterNames(new String[] {"Excel 97-2003 Workbook (*.xls)", "All Files (*.*)"});
			fd.setText(a.getName());
			fd.setOverwrite(true);
			fd.setFileName("workbook.xls");
			
			String file = fd.open();
			if (file == null) { return; }
			
			OptionBox options = new OptionBox(shell);
			options.setImage(rm.getImage(a.getImage()));
			options.setText(a.toString());
			options.setMessages(Str.EXPORT.toString(), Str.PATIENT_SORT.toString());
			options.setOptions(new String[] {
				Str.ALL_DATA.toString(), Str.PATIENTS.toString(), Str.LADS_REPORT.toString()
			}, new String[] {
				Str.ID.toString(), Str.LAST_NAME.toString(), Str.FIRST_NAME.toString()
			});
			boolean[][] ret = options.open(0, 0);
			if (ret == null) { return; }
			
			ExportType type = null;
			for (int i = 0; i < ret[0].length; i++) {
				if (ret[0][i]) {
					type = ExportType.values()[i]; break;
				}
			}
			SortOrder sort = null;
			for (int i = 0; i < ret[1].length; i++) {
				if (ret[1][i]) {
					sort = SortOrder.values()[i]; break;
				}
			}
			
			getController().new ExportToExcel(file, type, sort);
		
		} else if (a == MRSActions.EXPORT_MED_REPORT) {
			FileDialog fd = new FileDialog(shell, SWT.SAVE);
			fd.setFilterExtensions(new String[] {"*.xls", "*.*"});
			fd.setFilterNames(new String[] {"Excel 97-2003 Workbook (*.xls)", "All Files (*.*)"});
			fd.setText(a.getName());
			fd.setOverwrite(true);
			fd.setFileName("workbook.xls");
			
			String file = fd.open();
			if (file != null) {
				getController().new ExportMedReport(file);
			}
			
		} else if (a == MRSActions.EXPORT_PT_IDX) {
			FileDialog fd = new FileDialog(shell, SWT.SAVE);
			fd.setFilterExtensions(new String[] {"*.pdf", "*.*"});
			fd.setFilterNames(new String[] {"Adobe PDF Files (*.pdf)", "All Files (*.*)"});
			fd.setText(a.getName());
			fd.setOverwrite(true);
			fd.setFileName("Patient Index " + DF_MONTH.format(new Date()) + ".pdf");
			
			String file = fd.open();
			if (file != null) {
				getController().new ExportPatientIndex(file);
			}
			
		} else if (a == MRSActions.EXPORT_DIR_REPORT) {
			getController().new ExportDirectiveReport();
			
		} else if (a == MRSActions.ABOUT) {
			Splash.open(shell, rm, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL, null);

		} else if (a == MRSActions.SET_DATA_PATH) {
			changeDataPath(shell, false);
			
		} else if (a == MRSActions.EXIT) {
			shell.dispose();
			
		} else {
			setMessage(MessageType.ERROR, Str.UNSUPPORTED_ACTION + ": " + a);
		}
	}
	
	private boolean popView(boolean notify, boolean setUnderneathView) {
		if (views.size() == 1) {
			display.beep(); return false;
		}
		
		MRSView v = views.peek();
		if (v != null && !v.getProducer().hideView(notify)) { return false; }
		
		views.pop();
		if (setUnderneathView) { setView(); }
		return true;
	}
	
	private void pushView(MRSView newView) {
		// Handle special case of leaving the patient editor- if they are creating
		// a new visit, they usually expect it to return to search after they save/
		// exit the visit. However, they do not want to leave the visit editor as
		// they add prescriptions, etc.
		if (!views.isEmpty() && views.peek().getProducer() instanceof PatientEditor) {
			popView(false, false);
		}
		
		views.push(newView);
		setView();
	}
	
	private void setView() {
		MRSView v = views.peek();
		UIAction[] actions = v.getActions();
		
		decorate(newItem, v.getProducer().getNewAction());
		decorate(saveItem, v.getProducer().getSaveAction());
		
		for (Button b : topActions) { b.dispose(); }
		
		Composite titleBox = title.getParent();
		GridLayout gl = (GridLayout) titleBox.getLayout();
		gl.numColumns = actions.length + 3;
		
		topActions = new Button[actions.length];
		for (int i = 0; i < actions.length; i++) {
			Button b = SWTUtilities.button(titleBox, SWT.PUSH | SWTUtilities.IMAGE | SWTUtilities.TEXT, actions[i], rm, this);
			b.setBackground(titleBox.getBackground());
			b.setData(TEXT, actions[i].getNameObj());
			b.setData(TOOLTIP, actions[i].getDescriptionObj());
			topActions[i] = b;
		}
		titleBox.pack();
		
		boolean base = views.size() == 1;
		SWTUtilities.setVisibleAndIncluded(close, !base);
		
		this.title.setText(v.getTitle().toString());
		this.title.setData(SWTInterface.TEXT, v.getTitle());
		
		SWTUtilities.setTopControl(v.getControl());
		v.getProducer().showView();
		
		// Clear any data validation messages on this view
		if (status != null) { status.setMessage(MessageType.DEFAULT, null); }
	}
	
	private void decorate(MenuItem item, UIAction action) {
		item.setEnabled(action != null);
		if (action != null) { SWTUtilities.decorate(item, action, rm); }
	}
	
	public boolean handleViewChange(boolean promptToSave, Object o) {
		int result = showSaveDialog(promptToSave, o);
		
		if (result == SWT.CANCEL) {
			return false;
		}
		if (result == SWT.YES) {
			if (o == Str.DICTIONARY) {
				getController().new SaveDictionary();
			} else {
				getController().new SaveObjects(o);
			}
		}
		return true;
	}
	
	private int showSaveDialog(boolean user, Object o) {
		if (!user && invalid.isEmpty()) { return SWT.YES; }
		
		int style = SWT.ICON_WARNING;
		String message;
		if (user) {
			style |= SWT.YES | SWT.NO | SWT.CANCEL;
			
			message = Str.CONFIRM_SAVE.toString() + "\n" + o;
			if (!invalid.isEmpty()) {
				message += "\n" + Str.INVALID_DATA.toString();
			}
		} else {
			style |= SWT.OK | SWT.CANCEL;
			
			message = Str.INVALID_DATA + System.lineSeparator()
			        + Str.CONFIRM_PROMPT + Str.EXIT.toString().toLowerCase() + "?";
		}
		
		MessageBox mb = new MessageBox(shell, style);
		mb.setText(title.getText());
		mb.setMessage(message);
		
		return mb.open();
	}
	
	private void changeDataPath(Shell parent, boolean atStartup) {
		boolean valid = Option.isDataPathValid();
		if (valid && atStartup) { return; }
		
		do {
			// TODO: Externalise in a way that doesn't rely on the database, since
			// this needs to display in the local language before the data is loaded
			String msg = "Please select the folder containing the MRS data files:";
			if (!valid) {
				msg = "The specified data folder does not contain all the required files." + System.lineSeparator() + msg;
			}
			if (!atStartup) {
				msg += System.lineSeparator() + "NOTE: You must restart the program for this to take effect.";
			}
			
			InputBox box = new InputBox(parent, InputBox.FILE_DIALOG);
			box.setMessage(msg);
			box.setText(Str.APP_TITLE + " - " + Str.SET_DATA_PATH);
			box.setImage(display.getSystemImage(valid? SWT.ICON_QUESTION : SWT.ICON_WARNING));
			box.setIcon(rm.getImage(MRSActions.SET_DATA_PATH.getImage()));
			String path = box.open(Option.DATA_PATH.toString());
			if (path == null) {
				if (atStartup) { display.dispose(); }
				return;
			}
			Option.DATA_PATH.set(path);
			valid = Option.isDataPathValid();
		} while (!valid);
	}
	
	@Override
	public void setCurrent(Object currentObject) {
		super.setCurrent(currentObject);
		setSubtitle(currentObject);
	}
	
	public void setSubtitle(Object subtitle) {
		this.subtitle.setText(ObjectUtils.toString(subtitle, ""));
		this.subtitle.getParent().pack();
	}
	
	public class MRSValidator<T> extends SWTValidator<T> {
		public MRSValidator(DataValidator<String, T> v, Text t) {
			super(v, t);
			t.setData(RESULT, INVALID);
		}
		
		@Override
		public void validated(Text t, boolean valid, T result) {
			if (valid) {
				invalid.remove(t);
				t.setBackground(normal);
				t.setData(RESULT, result);
			} else {
				invalid.add(t);
				t.setBackground(error);
				t.setData(RESULT, INVALID);
			}
			if (invalid.isEmpty()) {
				setMessage(MessageType.DEFAULT, null);
			} else {
				StringBuilder sb = new StringBuilder(Str.REENTER_INPUT.toString()).append(' ');
				boolean first = true;
				for (Text d : invalid) {
					if (first) {
						first = false;
					} else {
						sb.append(", ");
					}
					sb.append(d.getData(SWTInterface.TOOLTIP));
				}
				setMessage(MessageType.DEFAULT, sb.toString());
			}
		}
	}
	
	private void refreshText() {
		Display.setAppName(Str.APP_TITLE.toString());
		status.setText(Str.READY.toString() + ".", Str.CANCEL.toString());
		TextRefreshDelegate.refreshThreadSafe(shell.getMenuBar());
		TextRefreshDelegate.refreshThreadSafe(shell);
		getDictionaryEditor().refreshEntries();
	}
	
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		if (topActions.length > 0) { SWTUtilities.selectAndNotify(topActions[0]); }
	}
	
	@Override
	public void uncaughtException(final Throwable e) {
		SWTUtilities.run(display, new Runnable() { @Override
		public void run() {
			MessageBox mb = new MessageBox(shell == null? splash : shell, SWT.ICON_ERROR | SWT.OK);
			mb.setText(Str.ERROR + e.getMessage());
			mb.setMessage(Str.ERROR_MESSAGE.toString());
			mb.open();
		} });
	}
}