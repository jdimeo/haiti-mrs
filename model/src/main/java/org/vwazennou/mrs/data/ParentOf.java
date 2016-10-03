/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.data;

import java.util.Collection;

import org.vwazennou.mrs.formulary.Formulary;
import org.vwazennou.mrs.visit.UrineTest;
import org.vwazennou.mrs.visit.Visit;
import org.vwazennou.mrs.visit.VisitText;

import gnu.trove.procedure.TObjectProcedure;

public interface ParentOf<T> {
	Collection<T> getChildren();
	void inflateChildren(Formulary f);
	void unpersistChildCollection();
	
	static boolean recurse(Formulary f, Object o, TObjectProcedure<Object> callback) {
		if (!callback.execute(o)) { return false; }
		
		if (o instanceof ParentOf<?>) {
			ParentOf<?> po = (ParentOf<?>) o;
			if (f != null) { po.inflateChildren(f); }
			for (Object child : po.getChildren()) {
				if (!recurse(f, child, callback)) { return false; }
			}
		}
		
		if (o instanceof Visit) {
			Visit v = (Visit) o;
			
			UrineTest ut = v.getUrineTestResult();
			if (ut != null && !recurse(f, ut, callback)) { return false; }
			
			for (VisitText vt : v.getText()) {
				if (!recurse(f, vt, callback)) { return false; }
			}
		}
		return true;
	}
}