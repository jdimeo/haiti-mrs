/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.ui.swt.editors.script;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.widgets.Control;
import org.vwazennou.mrs.script.Directive;
import org.vwazennou.mrs.script.DirectiveBlank.BlankType;
import org.vwazennou.mrs.script.DirectiveText;
import org.vwazennou.mrs.ui.MRSInterface;
import org.vwazennou.mrs.ui.swt.SWTInterface;

import com.datamininglab.viz.gui.swt.util.SWTUtilities;

/**
 * This class dynamically provides the snippets of text in between blanks for directives.  It takes
 * the place of a dictionary <tt>Str</tt> key as a label or text box's {@link SWTInterface#TEXT}
 * object when the UI's text is being refreshed.
 */
public class DirectiveSnippet {
	private MRSInterface ui;
	private BlankType    type;
	private Directive    dir;
	private int          blankIndex;
	private boolean      isTitle;
	private Control      blankControl;
	
	public DirectiveSnippet(MRSInterface ui, Directive dir, BlankType type, int blankIndex) {
		this.ui         = ui;
		this.dir        = dir;
		this.type       = type;
		this.blankIndex = blankIndex;
	}
	
	public void setTitle(boolean isTitle) {
		this.isTitle = isTitle;
	}
	public boolean isTitle() {
		return isTitle;
	}
	
	@Override
	public String toString() {
		DirectiveText dt = ui.getDirectiveText(dir.getCode());
		String s = isTitle? dt.getTitle() : dt.getText();
		
		int from = 0, to = -1;
		for (int i = 0; i <= blankIndex; i++) {
			from = to + 1;
			to = s.indexOf('_', from);
		}
		
		s = to < 0? s.substring(from)
	              : s.substring(from, to);
		return s.trim();
	}
	
	public void setBlankControl(Control c) { blankControl = c; }
	
	public String getText() {
		String ret = SWTUtilities.getText(blankControl);
		if (StringUtils.isEmpty(ret) && type == BlankType.NUMERIC) {
			return "0";
		}
		return ret;
	}
	
	public void setText(String text) {
		SWTUtilities.setText(blankControl, text);
	}
}