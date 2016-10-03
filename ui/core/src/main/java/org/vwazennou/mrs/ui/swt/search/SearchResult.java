/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.ui.swt.search;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.vwazennou.mrs.dictionary.Str;
import org.vwazennou.mrs.ui.swt.SWTInterface;

import com.datamininglab.foundation.swt.util.ResourceManager;
import com.datamininglab.foundation.swt.util.SWTUtilities;
import com.datamininglab.foundation.ui.UIUtilities.UIAction;

public abstract class SearchResult<T> implements MouseListener, SelectionListener {
	private static final Color[] SHADE_COLOR = new Color[2];
	private static final int[]   SHADE_PCT   = {100};

	protected SearchInterface parent;
	protected SWTInterface ui;
	protected T obj;
	
	private Composite comp;
	private CLabel    title;
	private ToolBar   tb;
	private boolean   showButtons;
	
	public SearchResult(SearchInterface parent, T obj, boolean showButtons) {
		this(parent.getUI(), obj, showButtons);
		this.parent = parent;
	}
	public SearchResult(SWTInterface ui, T obj, boolean showButtons) {
		this.ui = ui; this.obj = obj; this.showButtons = showButtons;
	}
	
	public void fill(ResourceManager rm, Composite parent) {
		if (SHADE_COLOR[0] == null) {
			SHADE_COLOR[0] = rm.getColor(250, 250, 250);
			SHADE_COLOR[1] = rm.getColor(-50);
		}
		
		title = new CLabel(parent, SWT.NONE);
		title.setText(obj.toString());
		title.setData(SWTInterface.TEXT, obj);
		
		title.setFont(rm.getFont(12, SWT.NORMAL));
		title.setImage(rm.getImage(getIcon()));
		title.setBackground(SHADE_COLOR, SHADE_PCT, true);
		title.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		title.addMouseListener(this);
	
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.verticalIndent = 6;
		title.setLayoutData(gd);
		
		comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		if (showButtons) {
			tb = new ToolBar(parent, SWT.FLAT);
			tb.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
		
		getContent(rm, comp);
	}
	
	protected abstract String getIcon();
	protected abstract void   getContent(ResourceManager rm, Composite comp);
	
	protected void searchItem(ResourceManager rm, UIAction action) {
		searchItem(rm, action, false);
	}
	protected void searchItem(ResourceManager rm, UIAction action, boolean isDefault) {
		searchItem(rm, action, action.getName(), isDefault);
	}
	protected void searchItem(ResourceManager rm, UIAction action, Object text, boolean isDefault) {
		if (isDefault) { title.setData(action); }
		if (!showButtons) { return; }
		
		int style = SWT.PUSH | SWTUtilities.IMAGE | SWTUtilities.TEXT;
		
		ToolItem item = SWTUtilities.item(tb, style, action, rm, this);
		item.setData(SWTInterface.TEXT, text);
		item.setText(text.toString());
	}
	protected void searchSeparator() {
		if (showButtons) { new ToolItem(tb, SWT.SEPARATOR); }
	}
	
	static void label(ResourceManager rm, Composite parent, Str str, String icon,
			Object value, int widthHint, int hspan) {
		
		String s = value == null? "" : value.toString();
		
		Label l = new Label(parent, SWT.NONE);
		if (icon != null && !s.isEmpty()) { l.setImage(rm.getImage(icon)); }
		l.setToolTipText(str.toString());
		l.setData(SWTInterface.TOOLTIP, str);
		l.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		Text t = new Text(parent, SWT.READ_ONLY);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, widthHint < 0, false, hspan, 1);
		gd.widthHint = widthHint;
		t.setLayoutData(gd);
		t.setText(s);
		t.setToolTipText(str.toString());
		t.setData(SWTInterface.TOOLTIP, str);
	}
	
	public void setContentVisible(boolean visible) {
		if (showButtons) {
			SWTUtilities.setVisibleAndIncluded(tb, visible);
		}
		SWTUtilities.setVisibleAndIncluded(comp, visible);
	}
	public boolean isContentVisible() {
		return comp.isVisible();
	}
	
	@Override
	public void mouseDown(MouseEvent me) {
		Event e = new Event();
		e.widget = me.widget;
		widgetSelected(new SelectionEvent(e));
	}
	
	@Override
	public void mouseUp(MouseEvent e) {
		// No actions should be performed for this event
	}
	@Override
	public void mouseDoubleClick(MouseEvent e) {
		// No actions should be performed for this event
	}
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// No actions should be performed for this event
	}
}