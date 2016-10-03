/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.admin.util;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.vwazennou.mrs.data.Database;
import org.vwazennou.mrs.formulary.FormularyEntry;
import org.vwazennou.mrs.script.Prescription;

public final class UpdateRanks {
	private static TObjectIntHashMap<FormularyEntry> counts;
	
	private UpdateRanks() {
		// Prevent initialization
	}
	
	public static void main(String[] args) {
		System.out.println("Updating formulary ranks...");
		counts = new TObjectIntHashMap<>();
		
		Session s = Database.connect();
		for (Object o : s.createCriteria(Prescription.class).list()) {
			Prescription p = (Prescription) o;
			increment(p.getTreatment());
			increment(p.getDosage());
			increment(p.getForm());
		}
		
		final FormularyEntry[] arr = new FormularyEntry[counts.size()];
		counts.keys(arr);
		
		Arrays.sort(arr, new Comparator<FormularyEntry>() {
			@Override
			public int compare(FormularyEntry o1, FormularyEntry o2) {
				int c1 = counts.get(o1);
				int c2 = counts.get(o2);
				
				int c  = Integer.compare(c2, c1);
				if (c == 0) {
					c = o1.toString().compareToIgnoreCase(o2.toString()); 
				}
				return c;
			}
		});
		
		for (int i = 0; i < arr.length; i++) {
			FormularyEntry fe = arr[i];
			if (StringUtils.equalsIgnoreCase("N/A", fe.getName())) {
				fe.setRank(Integer.MAX_VALUE);
			} else {
				fe.setRank(i + 1);	
			}
		}
		
		Database.disconnect(s);		
		Database.disconnect();
		System.out.println("Done.");
	}
	
	private static void increment(FormularyEntry fe) {
		if (fe != null) { counts.adjustOrPutValue(fe, 1, 1); }
	}
}
