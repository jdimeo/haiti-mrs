/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.ui.swt.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.vwazennou.mrs.dictionary.DictionaryEntry.CompoundStr;
import org.vwazennou.mrs.dictionary.Str;
import org.vwazennou.mrs.patient.Patient;
import org.vwazennou.mrs.script.Prescription;
import org.vwazennou.mrs.search.SearchField;
import org.vwazennou.mrs.search.SearchFieldRegistry;
import org.vwazennou.mrs.task.Query;
import org.vwazennou.mrs.task.Query.QueryCallback;
import org.vwazennou.mrs.ui.MRSActions;
import org.vwazennou.mrs.ui.swt.MRSControls;
import org.vwazennou.mrs.ui.swt.MRSView;
import org.vwazennou.mrs.ui.swt.MRSView.MRSViewable;
import org.vwazennou.mrs.ui.swt.SWTInterface;
import org.vwazennou.mrs.visit.Visit;

import com.datamininglab.viz.gui.UIAction;
import com.datamininglab.viz.gui.UserInterface.MessageType;
import com.datamininglab.viz.gui.swt.util.ResourceManager;
import com.datamininglab.viz.gui.swt.util.SWTUtilities;

public class SearchInterface extends Composite implements SelectionListener, QueryCallback, Runnable, MRSViewable {
	private static final int MAX_HISTORY = 24;
	private static final Class<?>[] QUERY_TYPE_CLASS = {
		Patient.class, Visit.class, Prescription.class
	};
	private static final Str[]      QUERY_TYPE_STR = {
		Str.PATIENTS, Str.VISITS, Str.PRESCRIPTIONS, Str.CUSTOM
	};
	
	private SWTInterface ui;
	private volatile boolean excludeDeceased;
	
	private SearchResults results;
	private Combo         queryType;
	private Text          queryText;
	private Button        instant;
	private Button[][]    fieldSelectors;
	
	private Object[]             resultObjs;
	private Set<SearchField> selFields;
	
	private List<Query> history;
	private int         historyPtr;
	
	public SearchInterface(SWTInterface ui, Composite parent, int style) {
		super(parent, style);
		this.ui = ui;
		ResourceManager rm = ui.getResourceManager();
		
		selFields = new HashSet<>();
		history = new ArrayList<>();

		GridLayout gl = SWTUtilities.removeMargins(new GridLayout(5, false));
		gl.verticalSpacing = 0;
		setLayout(gl);
		MRSControls.spacer(this, 40, 5);
		
		Label l = new Label(this, SWT.NONE);
		GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		gd.horizontalIndent = 6;
		l.setLayoutData(gd);
		l.setData(SWTInterface.TEXT, Str.FIND);
		
		queryType = new Combo(this, SWT.READ_ONLY);
		queryType.setData(SWTInterface.TEXT, QUERY_TYPE_STR);
		
		queryText = new Text(this, SWT.BORDER);
		queryText.addSelectionListener(this);
		gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gd.widthHint = 128;
		queryText.setLayoutData(gd);
		queryText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (instant.getSelection()) { handle(MRSActions.DO_SEARCH); }
			}
		});
		
		SWTUtilities.button(this, SWTUtilities.IMAGE, MRSActions.DO_SEARCH, rm, this)
			.setData(SWTInterface.TEXT, Str.SEARCH);
		
		ToolBar toolbar = new ToolBar(this, SWT.FLAT);
		toolbar.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false));
		SWTUtilities.item(toolbar, SWT.PUSH, MRSActions.PREVIOUS_SEARCH, rm, this);
		SWTUtilities.item(toolbar, SWT.PUSH, MRSActions.NEXT_SEARCH,     rm, this);
		SWTUtilities.separator(toolbar);
		SWTUtilities.item(toolbar, SWT.PUSH, MRSActions.EXPAND_ALL,      rm, this);
		SWTUtilities.item(toolbar, SWT.PUSH, MRSActions.COLLAPSE_ALL,    rm, this);
		
		final ScrolledComposite sc1 = new ScrolledComposite(this, SWT.V_SCROLL);
		sc1.setExpandHorizontal(true);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false, 5, 1);
		gd.heightHint = 150;
		sc1.setLayoutData(gd);
		
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1);
		gd.verticalIndent = 4;
		new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(gd);
		
		queryType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int n = queryType.getItemCount() - 1;
				int i = queryType.getSelectionIndex();
				
				if (i < n) {
					SWTUtilities.setVisibleAndIncluded(sc1, false);
					selFields.clear();
					for (int j = 0; j < fieldSelectors.length; j++) {
						for (int k = 0; k < fieldSelectors[j].length; k++) {
							Button b = fieldSelectors[j][k];
							if (j == i) {
								SearchField sf = (SearchField) b.getData();
								b.setSelection(sf.isDefault());
								if (sf.isDefault()) { selFields.add(sf); }
							} else {
								b.setSelection(false);	
							}
						}
					}
				} else {
					SWTUtilities.setVisibleAndIncluded(sc1, true);
				}
				SearchInterface.this.layout(true);
			}
		});
		
		Composite custom = new Composite(sc1, SWT.NONE);
		custom.setLayout(new GridLayout(3, false));
		sc1.setContent(custom);
		
		instant = new Button(custom, SWT.CHECK);
		instant.setSelection(true);
		instant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		instant.setData(SWTInterface.TEXT, Str.INSTANT_RESULTS);
		
		final Button deceased = new Button(custom, SWT.CHECK);
		deceased.setSelection(excludeDeceased = true);
		deceased.setData(SWTInterface.TEXT, Str.EXCLUDE_DECEASED);
		deceased.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				excludeDeceased = deceased.getSelection();
			}
		});
		
		MRSControls.label(custom, new CompoundStr(Str.SEARCH_FIELDS, ":"), null, SWT.LEFT)
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		
		int max = 0;
		SearchFieldRegistry sfr = ui.getController().getSearchFields();
		fieldSelectors = new Button[QUERY_TYPE_CLASS.length][];
		for (int i = 0; i < fieldSelectors.length; i++) {
			List<SearchField> list = sfr.getFields(QUERY_TYPE_CLASS[i]);
			fieldSelectors[i] = new Button[list.size()];
			max = Math.max(max, list.size());
		}
		
		SelectionListener buttonSel = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button b = (Button) e.widget;
				SearchField sf = (SearchField) b.getData();
				
				if (b.getSelection()) {
					selFields.add(sf);
				} else {
					selFields.remove(sf);
				}
			}
		};
		
		for (int i = 0; i < max; i++) {
			for (int j = 0; j < fieldSelectors.length; j++) {
				Button b = addFieldButton(sfr, custom, i, j, max);
				if (b != null) { b.addSelectionListener(buttonSel); }
			}
		}
		custom.pack();
		
		results = new SearchResults(rm, this, SWT.NONE);
		results.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 5, 1));
	}
	
	private Button addFieldButton(SearchFieldRegistry sfr, Composite parent, int i, int j, int n) {
		List<SearchField> list = sfr.getFields(QUERY_TYPE_CLASS[j]);
		if (i < list.size()) {
			SearchField sf = list.get(i);
			
			Button b = new Button(parent, SWT.CHECK);
			b.setBackground(parent.getBackground());
			b.setData(sf);
			b.setData(SWTInterface.TEXT, sf.getName());
			
			return fieldSelectors[j][i] = b;
			
		} else if (i == list.size()) {
			Label l = new Label(parent, SWT.NONE);
			l.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, n - i));
			l.setBackground(parent.getBackground());
		}
		return null;
	}

	@Override
	public Criterion getFilter(Class<?> c) {
		if (!c.equals(Patient.class) || !excludeDeceased) { return null; }
		return Restrictions.eq("isDeceased", Str.NO);
	}
	@Override
	public Order getOrder(Class<?> c) {
		if (c.equals(Visit.class)) { return Order.desc("date"); }
		return Order.asc("id");
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		handle((UIAction) e.widget.getData());
	}
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		handle(MRSActions.DO_SEARCH);
	}
	public void handle(UIAction a) {
		if (a == MRSActions.DO_SEARCH) {
			String query = queryText.getText();
			if (query.length() < 2 && NumberUtils.toLong(query, -1L) < 0L) { return; }
			
			performQuery(query, selFields);
			
		} else if (a == MRSActions.PREVIOUS_SEARCH) {
			if (historyPtr < 1) {
				ui.setMessage(MessageType.WARN, Str.ERROR_FIRST_SEARCH.toString());
			} else {
				historyPtr--;
				loadQuery();
			}
		} else if (a == MRSActions.NEXT_SEARCH) {
			if (historyPtr > history.size() - 2) {
				ui.setMessage(MessageType.WARN, Str.ERROR_LAST_SEARCH.toString());
			} else {
				historyPtr++;
				loadQuery();
			}
			
		} else if (a == MRSActions.EXPAND_ALL) {
			results.setExpanded(true);
		} else if (a == MRSActions.COLLAPSE_ALL) {
			results.setExpanded(false);
		} else {
			ui.setMessage(MessageType.ERROR, Str.UNSUPPORTED_ACTION + ": " + a);
		}
	}
	
	protected SWTInterface getUI() { return ui; }
	
	@Override
	public void handleResultsFor(Query q) {
		handleResultsFor(q, true);
	}
	private void handleResultsFor(Query q, boolean isNew) {
		if (isDisposed()) { return; }
		
		resultObjs = q.getResults();
		SWTUtilities.run(getDisplay(), this);
		
		if (isNew) {
			if (history.size() > MAX_HISTORY) { history.remove(0); }
			historyPtr = history.size();
			history.add(q);	
		}
	}
	
	@Override
	public void run() {
		if (resultObjs == null) { return; }
		
		ui.setSubtitle(resultObjs.length + " " + Str.MATCHES);
	
		SearchResult<?>[] arr = new SearchResult<?>[resultObjs.length];
		for (int i = 0; i < resultObjs.length; i++) {
			Object o = resultObjs[i];
			if (o instanceof Patient) {
				// TODO: throws index out of bounds
				arr[i] = new PatientResult(SearchInterface.this, (Patient) o);
			} else if (o instanceof Visit) {
				arr[i] = new VisitResult(this, (Visit) o);
			} else if (o instanceof Prescription) {
				arr[i] = new PrescriptionResult(this, (Prescription) o, true);
			} else {
				throw new IllegalArgumentException("Unrecognized search result type: " +
						resultObjs[i].getClass());
			}
		}
		results.setResults(arr);
	}
	
	protected void performQuery(Object query, Collection<SearchField> fields) {
		clearResults();
		ui.getController().new PerformQuery(query, fields, this);
	}
	
	private void loadQuery() {
		clearResults();
		handleResultsFor(history.get(historyPtr), false);
	}
	
	private void clearResults() {
		results.clear();
	}
	
	@Override
	public MRSView getView(UIAction action) {
		return new MRSView(this, this, Str.SEARCH, getNewAction());
	}
	
	@Override
	public void showView() {
		ui.setCurrent(null);
		queryText.setFocus();
		handle(MRSActions.DO_SEARCH);
	}
	
	@Override
	public boolean hideView(boolean promptToSave) { return true; }
	
	@Override
	public UIAction getSaveAction() { return null; }
	@Override
	public UIAction getNewAction() { return MRSActions.NEW_PATIENT; }
}