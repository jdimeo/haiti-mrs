/*
 * Copyright (c) 2015 Elder Research, Inc.
 * All rights reserved.
 */
package org.vwazennou.mrs.admin.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.FetchMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.vwazennou.mrs.data.Database;
import org.vwazennou.mrs.visit.ClinicTeam;
import org.vwazennou.mrs.visit.Visit;

public final class CompareTeamsWithDates {
	private static final DateFormat[] FORMATS = {
		new SimpleDateFormat("MMM ''yy"), new SimpleDateFormat("MMMM ''yy")
	};
	
	private CompareTeamsWithDates() {
		// Prevent initialization
	}
	
	public static void main(String[] args) {
		Session s = Database.connect();
		
		ClinicTeam lads = (ClinicTeam) s.createCriteria(ClinicTeam.class)
				.add(Restrictions.eq("name", "LADS")).uniqueResult();
		
		List<ClinicTeam> allTeams = s.createCriteria(ClinicTeam.class).list();
		
		ScrollableResults sr = s.createCriteria(Visit.class)
				.setFetchMode("cinicTeam", FetchMode.JOIN)
				.add(Restrictions.ne("clinicTeam", lads)).scroll();
		
		try (Scanner scan = new Scanner(System.in)) {
			while (sr.next()) {
				Visit v = (Visit) sr.get(0);
				
				String c = v.getClinicTeam().toString();
				if (StringUtils.countMatches(c, " ") < 2) {
					// Don't try to reconcile old clinics with no church in the name
					continue;
				}
				c = StringUtils.substringAfter(c, " ");
				
				Date minDate = null;
				for (DateFormat fmt : FORMATS) {
					try {
						minDate = fmt.parse(c);
						break;
					} catch (ParseException e) {
						// Try the next format
					}
				}
				
				if (minDate == null) {
					// Can't figure out date of clinic
					continue;
				}
				
				Date maxDate = DateUtils.addMonths(minDate, 1);
				if (v.getDate().before(minDate) || v.getDate().after(maxDate)) {
					System.out.println(String.format("Visit #%d on %s does not appear to be during clinic %s", v.getId(), v.getDate(), v.getClinicTeam()));
					System.out.println("Assign a new clinic, or hit any key to take no action:");
					int i = 0;
					for (ClinicTeam t : allTeams) {
						System.out.println(String.format("%2d. %s", ++i, t));
					}
					
					
					if (!scan.hasNextLine()) { continue; }
					
					int idx = NumberUtils.toInt(scan.nextLine(), 0) - 1;
					if (idx >= 0) { v.setClinicTeam(allTeams.get(idx)); }
				}
			}
		}
		
		Database.disconnect(s);
		Database.disconnect();
	}
}
