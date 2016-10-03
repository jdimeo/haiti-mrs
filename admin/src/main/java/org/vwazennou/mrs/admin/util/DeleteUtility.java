/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.admin.util;

import java.io.Serializable;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.Session;
import org.hibernate.TypeMismatchException;
import org.hibernate.criterion.Restrictions;
import org.vwazennou.mrs.data.Database;
import org.vwazennou.mrs.visit.ClinicTeam;
import org.vwazennou.mrs.visit.Visit;

public final class DeleteUtility {
	private DeleteUtility() {
		// Prevent initialization
	}
	
	public static void main(String[] args) {
		try (Scanner s = new Scanner(System.in)) {
			while (true) {
				System.out.println("Enter a class name followed by IDs to delete (or type exit):");
				String input = s.nextLine();
				if (StringUtils.equalsIgnoreCase(input, "exit")) { break; }
				
				String className = StringUtils.substringBefore(input, " ").trim();
				String[] ids = StringUtils.substringAfter(input, " ").split("[\\s,]+");
				if (ids.length < 1) {
					System.err.println("Invalid list of IDs");
					continue;
				}
				
				Session session = Database.connect();
				Class<?> c;
				try {
					c = Class.forName(className);
				} catch (ClassNotFoundException e) {
					try {
						c = Class.forName("org.vwazennou.mrs." + className);
					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
						continue;	
					}
				}
				
				for (String id : ids) {
					try {
						delete(session, c, NumberUtils.toLong(id, -1L));
					} catch (TypeMismatchException e) {
						delete(session, c, NumberUtils.toInt(id, -1));
					}
				}
				Database.disconnect(session);
			}
		}
		System.out.println("Shutting down...");
		Database.disconnect();
	}
	
	private static void delete(Session s, Class<?> c, Serializable id) {
		Object o = s.get(c, id);
		if (o == null) {
			System.err.println("Not found!");
		} else {
			if (o instanceof ClinicTeam) {
				System.out.println("Deleting all visits of " + o + "...");
				List<?> l = s.createCriteria(Visit.class)
						.add(Restrictions.eq("clinicTeam", o)).list();
				for (Object v : l) { s.delete(v); }
			}
			System.out.println("Deleting " + o + "...");
			s.delete(o);
		}
	}
}
