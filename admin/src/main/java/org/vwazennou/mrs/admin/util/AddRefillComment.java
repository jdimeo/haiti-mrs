/*
 * Copyright (c) 2013 Elder Research, Inc.
 * All rights reserved.
 */
package org.vwazennou.mrs.admin.util;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.vwazennou.mrs.data.Database;
import org.vwazennou.mrs.dictionary.Dictionary;
import org.vwazennou.mrs.visit.ClinicTeam;
import org.vwazennou.mrs.visit.ClinicTeam.ClinicType;
import org.vwazennou.mrs.visit.Visit;
import org.vwazennou.mrs.visit.VisitText.VisitTextType;

public final class AddRefillComment {
	private AddRefillComment() {
		// Prevent initialization
	}
	
	public static void main(String[] args) {
		Session s = Database.connect();
		
		List<?> clinics = s.createCriteria(ClinicTeam.class)
				.add(Restrictions.eq("type", ClinicType.REFILL))
				.list();
		
		List<?> visits = s.createCriteria(Visit.class)
				.add(Restrictions.in("clinicTeam", clinics))
				.list();

		Dictionary d = new Dictionary(s);
		for (Object o : visits) {
			Visit v = (Visit) o;
			v.setText(VisitTextType.COMMENTS, d.getLanguage(), "Pt not seen- automatic refill prescriptions");
		}
		
		Database.disconnect(s);
		Database.disconnect();
	}
}
