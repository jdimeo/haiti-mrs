/*
 * Copyright (c) 2015 Elder Research, Inc.
 * All rights reserved.
 */
package org.vwazennou.mrs.admin.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.vwazennou.mrs.data.Database;
import org.vwazennou.mrs.visit.ClinicTeam;

public final class MergeClinics {
	private MergeClinics() {
		// Prevent initialization
	}
	
	public static void main(String[] args) {
		try (Scanner s = new Scanner(System.in)) {
			while (true) {
				Session session = Database.connect();
				List<?> allClinics = session.createCriteria(ClinicTeam.class).list();
				Collections.sort(allClinics, new Comparator<Object>() {
					@Override
					public int compare(Object o1, Object o2) {
						return String.CASE_INSENSITIVE_ORDER.compare(o1.toString(), o2.toString());
					}
				});
				
				int i = 0;
				for (Object o : allClinics) {
					Query q = session.createQuery("select count(*) from Visit where clinicTeam = :ct");
					q.setParameter("ct", o);
					System.out.format("%2d: %s (%,d visits)%n", i++, o, q.uniqueResult());
				}
				System.out.println("Type two clinic numbers above (separated by space) to merge (or type exit). The first will be deleted:");
				
				String input = s.nextLine();
				if (StringUtils.equalsIgnoreCase(input, "exit")) {
					Database.disconnect(session);
					break;
				}
				
				int idx1 = NumberUtils.toInt(StringUtils.substringBefore(input, " ").trim(), -1);
				int idx2 = NumberUtils.toInt(StringUtils.substringAfter(input, " ").trim(), -1);
				if (idx1 < 0 || idx2 < 0 || idx1 >= allClinics.size() || idx2 >= allClinics.size()) {
					System.err.println("Invalid input");
					Database.disconnect(session);
					continue;
				}
				
				Object ct1 = allClinics.get(idx1);
				Object ct2 = allClinics.get(idx2);
				System.out.println("Merging " + ct1 + " into " + ct2 + "...");
				Query q = session.createQuery("update Visit set clinicTeam = :ct2 where clinicTeam = :ct1");
				q.setParameter("ct1", ct1);
				q.setParameter("ct2", ct2);
				System.out.format("%,d visits merged%n", q.executeUpdate());
				session.delete(ct1);
				Database.disconnect(session);
			}
			Database.disconnect();
		}
	}
}
