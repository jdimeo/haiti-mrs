/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.ui.swt;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.vwazennou.mrs.HaitiMRS;
import org.vwazennou.mrs.dictionary.Str;

import com.datamininglab.commons.logging.LogContext;
import com.datamininglab.foundation.awt.icons.IconsFlags;
import com.datamininglab.foundation.swt.util.ResourceManager;
import com.datamininglab.foundation.swt.util.SWTUtilities;
import com.datamininglab.foundation.text.UnicodeChars;

public class Splash extends Shell {
	public static Shell open(Shell parent, ResourceManager rm, int style, String message) {
		final Shell s = (parent == null)? new Shell(rm.getDisplay(), style)
		                                : new Shell(parent, style);
		s.setText(Str.APP_TITLE.toString());
		s.setImage(rm.getImage(IconsFlags.FLAG_HAITI));
		s.setLayout(new GridLayout(2, false));

		ImageData id = null;
		try (InputStream is = Splash.class.getResourceAsStream("/splash.png")) {
			id = new ImageLoader().load(is)[0];	
		} catch (IOException e) {
			LogContext.warning(e, "Cannot load splash screen image");
		}
		
		Label img = new Label(s, SWT.TRANSPARENT);
		if (id != null) {
			img.setImage(rm.getImage(id));
			GridData gd = new GridData(id.width, id.height);
			gd.verticalSpan = (message == null)? 9 : 7;
			img.setLayoutData(gd);
		}
		
		Label label = new Label(s, SWT.NONE);
		label.setText(Str.APP_TITLE.toString().replaceFirst("[\\s]", "\n"));
		label.setFont(rm.getFont(20, SWT.BOLD));
		label.setForeground(rm.getColor(100, 100, 100));
		
		label = new Label(s, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		label.setFont(rm.getFont(12, SWT.NORMAL));
		if (message != null) { label.setText(message); }
		
		new Label(s, SWT.NONE).setText("Version " + HaitiMRS.VERSION);
		new Label(s, SWT.NONE).setText(UnicodeChars.COPYRIGHT_SIGN + "2009-2017 Grace Network, Vwazen Nou");
		for (String url : HaitiMRS.URLS) { link(s, url, url); }
		link(s, HaitiMRS.CONTACT, "mailto:" + HaitiMRS.CONTACT);
		
		if (message == null) {
			new Label(s, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(
					new GridData(SWT.FILL, SWT.CENTER, true, false));
			
			Button b = new Button(s, SWT.PUSH);
			b.setText(Str.OK.toString());
			GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
			gd.widthHint = SWTUtilities.DEFAULT_BUTTON_WIDTH;
			b.setLayoutData(gd);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) { s.dispose(); }
			});
			s.setDefaultButton(b);
		}
		
		s.pack();
		SWTUtilities.centerShell(s);
		s.setVisible(true);
		return s;
	}
	
	private static void link(Composite parent, String linkText, String linkTarget) {
		Link l = new Link(parent, SWT.NONE);
		l.setText("<a href=\"" + linkTarget + "\">" + linkText + "</a>");
		l.addSelectionListener(SWTUtilities.getHyperlinkSelectionListener());
	}
}
