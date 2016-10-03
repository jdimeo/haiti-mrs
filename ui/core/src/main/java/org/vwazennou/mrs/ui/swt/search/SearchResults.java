/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.ui.swt.search;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.datamininglab.foundation.swt.controls.CScrolledComposite;
import com.datamininglab.foundation.swt.util.ResourceManager;

public class SearchResults extends CScrolledComposite {
	private ResourceManager rm;
	private SearchResult<?>[] results;
	
	public SearchResults(ResourceManager rm, Composite parent, int style) {
		super(parent, style | SWT.V_SCROLL);
		this.rm = rm;
		
		setBackground(rm.getColor(-32));
		
		GridLayout gl = new GridLayout(1, false);
		gl.verticalSpacing = gl.marginHeight = 0;
		gl.marginBottom = 6;
		setLayout(gl);
	}
	
	public void setResults(SearchResult<?>... arr) {
		for (int i = 0; i < arr.length; i++) {
			arr[i].fill(rm, this);
		}
		pack();
		controlResized(null);
		results = arr;
	}
	
	public void setExpanded(boolean expanded) {
		if (results == null) { return; }
		for (int i = 0; i < results.length; i++) {
			results[i].setContentVisible(expanded);
		}
		controlResized(null);
	}
	
	public void clear() {
		Control[] items = getChildren();
		for (int i = 0; i < items.length; i++) { items[i].dispose(); }
	}
}
