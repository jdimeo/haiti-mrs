/*
 * Copyright (c) 2015 Elder Research, Inc.
 * All rights reserved.
 */
package org.vwazennou.mrs.admin.util;

import java.util.List;

import org.hibernate.Session;
import org.vwazennou.mrs.data.Database;
import org.vwazennou.mrs.visit.Visit;

public final class DeleteBlankVisits {
	private DeleteBlankVisits() {
		// Prevent initialization
	}
	
	public static void main(String[] args) {
		Session s = Database.connect();
		
		System.out.println("Finding blank visits...");
		List<Visit> l = s.createCriteria(Visit.class).list();
		int deleted = 0;
		for (Visit v : l) {
			if (v.getDateInMillis() < 0L
			 && v.getBloodGlucose() == 0
			 && v.getDiastolic() == 0
			 && v.getHeightCm() == 0.0f
			 && v.getHemoglobin() == 0.0f
			 && v.getPulse() == 0
			 && v.getRespiration() == 0
			 && v.getSystolic() == 0
			 && v.getTemperatureC() == 0.0f
			 && v.getWeightKg() == 0.0f) {
				System.out.println("Deleting visit " + v);
				s.delete(v);
				deleted++;
			}
		}
		System.out.println("Deleted " + deleted + " visit(s)");
		
		Database.disconnect(s);
		Database.disconnect();
		
	}
}
