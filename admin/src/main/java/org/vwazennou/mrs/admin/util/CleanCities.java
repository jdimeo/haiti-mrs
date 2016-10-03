/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.admin.util;

import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.vwazennou.mrs.data.Database;
import org.vwazennou.mrs.patient.Patient;

public final class CleanCities {
	private static final String FIELD = "community";
	private static final String COL = "address2";
	
	private CleanCities() {
		// Prevent initialization
	}
	
	public static void main(String[] args) {
		Session s = Database.connect();
		
		try (Scanner scanner = new Scanner(System.in)) {
			String in = null;
			while (!StringUtils.equalsIgnoreCase(in, "exit")) {
				System.out.println("Unique values of " + FIELD + ":");
				List<?> l = s.createCriteria(Patient.class)
						.setProjection(Projections.distinct(Projections.property(FIELD)))
						.list();
				int i = 0;
				for (Object o : l) {
					System.out.println(i++ + ": " + o);
				}
				
				System.out.println("Type number then new value or exit:");
				in = scanner.nextLine();
				int sp = in.indexOf(' ');
				if (sp < 0) { continue; }
				
				int idx = NumberUtils.toInt(in.substring(0, sp), -1);
				if (idx < 0) {
					System.err.println("Unparseable number " + in);
					continue;
				}
				
				String oldVal = l.get(idx).toString();
				String newVal = in.substring(sp + 1);
				int affected = s.createSQLQuery("update patients set " + COL + " = ? where " + COL + " = ?")
						.setString(0, newVal).setString(1, oldVal).executeUpdate();
				System.out.println("Changed " + affected + " rows");
				
				Database.disconnect(s);
				s = Database.connect();
			}
		}
		
		Database.disconnect(s);
		Database.disconnect();
		
	}
}
