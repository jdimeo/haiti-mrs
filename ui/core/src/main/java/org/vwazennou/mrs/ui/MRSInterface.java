/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.ui;

import org.vwazennou.mrs.MRSController;
import org.vwazennou.mrs.dictionary.Language;
import org.vwazennou.mrs.dictionary.Str;
import org.vwazennou.mrs.script.DirectiveText;
import org.vwazennou.mrs.script.Prescription;

import com.datamininglab.foundation.ui.UserInterface;

public abstract class MRSInterface implements UserInterface {
	private MRSController c;
	private Object currentObject;
    
	public MRSInterface(MRSController c) {
		this.c = c;
        c.setInterface(this);
    }
    
	public MRSController getController() {
		return c;
	}
	public Language getCurrentLanguage() {
		return c.getDictionary().getLanguage();
	}
	public DirectiveText getDirectiveText(int directiveNum) {
		return c.getDirectiveText().get(getCurrentLanguage(), directiveNum);
	}
	public String getDirectiveText(Prescription p) {
		return c.getDirectiveText().getString(p, getCurrentLanguage());
	}
	public String getDirectiveText(Prescription p, int from, int to) {
		return c.getDirectiveText().getString(p, getCurrentLanguage(), from, to);
	}
	
	public void setCurrent(Object currentObject) {
		this.currentObject = currentObject;
	}
	public Object getCurrent() {
		return currentObject;
	}
	
	public abstract void refreshDictionaryEntries();
	public abstract boolean confirm(Str action, Object o);
	public abstract void uncaughtException(Throwable e);
}
