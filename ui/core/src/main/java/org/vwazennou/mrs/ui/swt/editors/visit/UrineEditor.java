/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.ui.swt.editors.visit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.vwazennou.mrs.dictionary.Str;
import org.vwazennou.mrs.ui.swt.MRSControls;
import org.vwazennou.mrs.ui.swt.SWTInterface;
import org.vwazennou.mrs.ui.swt.TextRefreshDelegate;
import org.vwazennou.mrs.visit.UrineTest;
import org.vwazennou.mrs.visit.UrineTest.Blood;
import org.vwazennou.mrs.visit.UrineTest.Glucose;
import org.vwazennou.mrs.visit.UrineTest.Leukocytes;
import org.vwazennou.mrs.visit.UrineTest.Nitrite;
import org.vwazennou.mrs.visit.UrineTest.Protein;

import com.datamininglab.commons.icons.eri.IconsERI;
import com.datamininglab.commons.icons.ms.IconsMS;
import com.datamininglab.viz.gui.UserInterface.MessageType;
import com.datamininglab.viz.gui.swt.util.SWTUtilities;

public class UrineEditor implements SelectionListener {
	private VisitEditor ve;
	
	private SWTInterface ui;
	private Shell        s;
	private Button       close, del;
	
	private Combo leuk, nitrite, protein, blood, glucose;
	
	private boolean warnedUser;
	
	public UrineEditor(SWTInterface ui, Display d) {
		this.ui = ui;
		
		s = new Shell(d, SWT.TOOL | SWT.APPLICATION_MODAL);
		s.setData(SWTInterface.TEXT, Str.URINE);
		s.setLayout(new GridLayout(2, false));
		
		Label l = new Label(s, SWT.NONE);
		l.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		l.setData(SWTInterface.TEXT, Str.URINE);
		
		close = new Button(s, SWT.NONE);
		close.setData(SWTInterface.TOOLTIP, Str.CLOSE);
		close.setImage(ui.getResourceManager().getImage(IconsERI.CLOSE));
		close.addSelectionListener(this);
		
		GridData gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		gd.widthHint = gd.heightHint = 20;
		close.setLayoutData(gd);
		
		leuk    = newCombo(Leukocytes.class);
		nitrite = newCombo(Nitrite.class);
		protein = newCombo(Protein.class);
		blood   = newCombo(Blood.class);
		glucose = newCombo(Glucose.class);
		
		new Label(s, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		Button save = new Button(s, SWT.PUSH);
		save.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false, 2, 1));
		save.setData(SWTInterface.TEXT, Str.SAVE);
		save.setImage(ui.getResourceManager().getImage(IconsMS.SAVE));
		save.addSelectionListener(this);
		
		del = new Button(s, SWT.PUSH);
		del.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false, 2, 1));
		del.setData(SWTInterface.TEXT, Str.REMOVE_RESULTS);
		del.addSelectionListener(this);

		s.setDefaultButton(save);
	}
	
	private Combo newCombo(Class<? extends Enum<?>> c) {
		Combo combo = MRSControls.combo(s, SWT.BORDER, c.getSimpleName(), s, 1,
				SWTUtilities.DEFAULT_BUTTON_WIDTH);
		combo.setData(SWTInterface.TEXT, c.getEnumConstants());
		TextRefreshDelegate.refresh(combo);
		combo.setVisibleItemCount(8);
		return combo;
	}
	
	public void open(VisitEditor ve) {
		this.ve = ve;
		
		UrineTest ut = ve.getUrineTest();
		if (ut == null) {
			SWTUtilities.setVisibleAndIncluded(del, false);
			
			leuk.select(-1);
			nitrite.select(-1);
			protein.select(-1);
			blood.select(-1);
			glucose.select(-1);
		} else {
			SWTUtilities.setVisibleAndIncluded(del, true);
			
			SWTUtilities.select(leuk,    ut.getLeukocytes());
			SWTUtilities.select(nitrite, ut.getNitrite());
			SWTUtilities.select(protein, ut.getProtein());
			SWTUtilities.select(blood,   ut.getBlood());
			SWTUtilities.select(glucose, ut.getGlucose());
		}
		
		Point p = ve.getPopupCoordinates();
		s.pack();
		s.setSize(220, s.getSize().y);
		s.setLocation(p.x - 42, p.y - 8);
		s.setImage(s.getDisplay().getActiveShell().getImage());
		s.setVisible(true);
		s.setActive();
		TextRefreshDelegate.refresh(s);
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.widget == del) {
			ve.deleteUrineTest();
			s.setVisible(false);
			return;
		} else if (e.widget == close) {
			s.setVisible(false);
			return;
		}
		
		UrineTest ut = ve.getUrineTest();
		if (ut == null) { ut = new UrineTest(); }
		
		ut.setLeukocytes(getSelection(Leukocytes.class, leuk));
		ut.setNitrite(getSelection(Nitrite.class, nitrite));
		ut.setProtein(getSelection(Protein.class, protein));
		ut.setBlood(getSelection(Blood.class, blood));
		ut.setGlucose(getSelection(Glucose.class, glucose));
		
		if (warnedUser) {
			warnedUser = false;
		} else {
			ui.setMessage(MessageType.DEFAULT, null);
			ve.setUrineTest(ut);
			s.setVisible(false);
		}
	}
	
	private <T extends Enum<?>> T getSelection(Class<T> c, Combo combo) {
		if (warnedUser) { return null; }
		
		int idx = combo.getSelectionIndex();
		if (idx < 0) {
			ui.setMessage(MessageType.WARN, Str.REENTER_INPUT + " " + c.getSimpleName());
			warnedUser = true;
			return null;
		}
		return c.getEnumConstants()[idx];
	}
	
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// No actions should be performed for this event
	}
}