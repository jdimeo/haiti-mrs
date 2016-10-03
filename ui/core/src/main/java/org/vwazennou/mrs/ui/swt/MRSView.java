/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.ui.swt;

import org.eclipse.swt.widgets.Control;

import com.datamininglab.viz.gui.UIAction;

public class MRSView {
	private MRSViewable parent;
	private Object      title;
	private Control     control;
	private UIAction[]  actions;
	
	/**
	 * Creates a new view.
	 * @param v the viewable that produced this view
	 * @param c the control that should be shown in the main interface
	 * @param t the title of the view
	 * @param a optional actions to put in the top header of the view
	 */
	public MRSView(MRSViewable v, Control c, Object t, UIAction... a) {
		parent  = v;
		control = c;
		title   = t;
		actions = a;
	}
	
	public MRSViewable getProducer() { return parent;  }
	public Object      getTitle()    { return title;   }
	public Control     getControl()  { return control; }
	public UIAction[]  getActions()  { return actions; }
	
	public interface MRSViewable {
		/**
		 * Creates a new view.
		 * @param action the action that requested this view
		 * @return the view
		 */
		MRSView getView(UIAction action);
		
 		/**
		 * Gets the save action associated with this view.
		 * @return the save action
		 */
		UIAction getSaveAction();
		
		/**
		 * Gets the object creation action associated with this view.
		 * @return the new action
		 */
		UIAction getNewAction();
		
		/**
		 * This method is called when a view produced by this
		 * viewable is set as the current view of the interface.
		 */
		void showView();
		
		/**
		 * This method is called when a view produced by this
		 * viewable is about to no longer be the current view
		 * of the interface.
		 * @param promptToSave if the user may be about to possibly lose unsaved results
		 * and should be given the opportunity to cancel the view change
		 * @return if the view change should continue
		 */
		boolean hideView(boolean promptToSave);
	}
}