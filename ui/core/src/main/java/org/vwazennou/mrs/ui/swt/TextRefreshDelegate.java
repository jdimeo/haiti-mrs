/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.ui.swt;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

import com.datamininglab.foundation.swt.util.SWTUtilities;
import com.datamininglab.foundation.ui.UIUtilities;
import com.datamininglab.foundation.util.ReflectionUtils;

public final class TextRefreshDelegate implements Runnable {
	public static void refreshThreadSafe(Widget w) { new TextRefreshDelegate(null, w, true); }
	public static void refresh(Widget w) { new TextRefreshDelegate(null, w, false); }
	
	private TextRefreshDelegate parent;
	private boolean async;
	private Widget w;
	private volatile int children;
	
	private TextRefreshDelegate(TextRefreshDelegate parent, Widget w, boolean async) {
		this.w      = w;
		this.parent = parent;
		this.async  = async;
		if (parent != null) { parent.children++; }
		
		if (async) {
			SWTUtilities.run(w.getDisplay(), this);
		} else {
			run();
		}
	}
	
	@Override
	public void run() {
		if (w.isDisposed()) { return; }
		
		Object o = w.getData(SWTInterface.TOOLTIP);
		if (o != null) {
			ReflectionUtils.invoke(w, "setToolTipText", o.toString());
		}
		
		o = w.getData(SWTInterface.TEXT);
		if (o != null) {
			if (w instanceof Combo) {
				refreshCombo((Combo) w, o);
			} else {
				refreshText(w, o);
			}
		}
		
		// Recurse child controls (if applicable)
		recurseChildren("getChildren");
		// Recurse child items (if applicable)
		recurseChildren("getItems");

		if (parent != null) {
			parent.children--;
			if (parent.children == 0 && parent.w instanceof Composite) {
				((Composite) parent.w).layout(true);
			}
		}
	}
	
	private static void refreshText(Widget w, Object text) {
		String s = text.toString();
		Object mref = w.getData(SWTInterface.MNEUMONIC);
		if (mref != null) {
			Object prev = ReflectionUtils.invoke(w, "getText");
			if (prev != null) { UIUtilities.freeMnemonic(mref, prev.toString()); }
			s = UIUtilities.getMnemonic(mref, s, null);
		}
		ReflectionUtils.invoke(w, "setText", s);
	}
	
	private static void refreshCombo(Combo c, Object text) {
		Object[] arr = (Object[]) text;
		int selected = c.getSelectionIndex();
		c.removeAll();
		for (int i = 0; i < arr.length; i++) {
			c.add(arr[i].toString());
		}
		SWTUtilities.selectAndNotify(c, selected < 0? 0 : selected);
	}
	
	private void recurseChildren(String childMethod) {
		Object[] arr = (Object[]) ReflectionUtils.invokeIfExists(w, childMethod);
		if (arr == null) { return; }
		
		for (Object child : arr) {
			if (child instanceof Widget) {
				new TextRefreshDelegate(this, (Widget) child, async);
			}
		}
	}
}
