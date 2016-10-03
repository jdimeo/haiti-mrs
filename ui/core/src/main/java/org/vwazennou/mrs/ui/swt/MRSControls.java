/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.ui.swt;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.vwazennou.mrs.dictionary.Str;

import com.datamininglab.foundation.data.lut.LookupTable;
import com.datamininglab.foundation.swt.util.ResourceManager;
import com.datamininglab.foundation.swt.util.SWTUtilities;

public final class MRSControls {
	private MRSControls() {
		// Prevent initialization
	}
	
	public static Label label(Composite parent, Object string, Composite mneumonicRoot, int align) {
		Label l = new Label(parent, align);
		l.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		l.setData(SWTInterface.TEXT, string);
		l.setText(string.toString());
		if (mneumonicRoot == null) {
			l.setForeground(l.getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
		} else {
			l.setData(SWTInterface.MNEUMONIC, mneumonicRoot);
		}
		return l;
	}
	
	public static void spacer(Composite parent, int height, int hspan) {
		Label l = new Label(parent, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, hspan, 1);
		if (height > 0) { gd.heightHint = height; }
		l.setLayoutData(gd);
	}
	
	public static Label separator(ResourceManager rm, Composite parent, Object title, int style, int hspan) {
		Label l = SWTUtilities.addSeperator(rm, parent, "Placeholder", style, hspan);
		l.setData(SWTInterface.TEXT, title);
		return l;
	}
	
	public static Button check(Composite parent, Str string, Composite mneumonicRoot) {
		Button b = new Button(parent, SWT.CHECK);
		b.setData(SWTInterface.TEXT, string);
		b.setData(SWTInterface.MNEUMONIC, mneumonicRoot);
		return b;
	}
	
	public static Text text(Composite parent, int style, Object text,
			Composite mneumonicRoot, int hspan, boolean fillHoriz) {
		return text(parent, style, text, null, mneumonicRoot, hspan, fillHoriz, null);
	}
			
	public static Text text(Composite parent, int style, Object text, String unit,
			Composite mneumonicRoot, int hspan, boolean fillHoriz, SWTInterface ui) {
		if (text != null) { label(parent, text, mneumonicRoot, SWT.RIGHT); }
		
		Text t = new Text(parent, SWT.BORDER | style);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, fillHoriz, false, hspan, 1);
		gd.widthHint = 24;
		t.setLayoutData(gd);
		t.setData(SWTInterface.TOOLTIP, text);
		if (ui != null) { t.addSelectionListener(ui); }
		
		if (unit != null) { label(parent, unit, null, SWT.LEFT); }
		return t;
	}
	
	public static Combo combo(Composite parent, int style, Object string, Composite mneumonicRoot, int hspan, int width) {
		label(parent, string, mneumonicRoot, SWT.RIGHT);
		
		Combo c = new Combo(parent, style);
		GridData data = new GridData(width < 0? SWT.FILL : SWT.LEFT, SWT.FILL, false, false, hspan, 1);
		if (width > 0) { data.widthHint = width; }
		c.setLayoutData(data);
		SWTUtilities.addAutoCompleteListeners(c);
		return c;
	}
	
	public static <T> void comboItems(Combo combo, LookupTable<T, String> table) {
		List<String> keys = table.getKeys();
		Collections.sort(keys);
		for (String s : keys) { combo.add(s); }
	}
}
