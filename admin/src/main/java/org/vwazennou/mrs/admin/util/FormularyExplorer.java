/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.admin.util;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.hibernate.Session;
import org.vwazennou.mrs.data.Database;
import org.vwazennou.mrs.formulary.Formulary;
import org.vwazennou.mrs.formulary.FormularyEntry;
import org.vwazennou.mrs.formulary.FormularyEntry.FormularyEntryType;

public final class FormularyExplorer {
	private FormularyExplorer() {
		// Prevent initialization
	}
	
	public static void main(String[] args) {
		long start = System.nanoTime();
		Session session = Database.connect();
		final Formulary f = new Formulary(session);
		
		long delta = System.nanoTime() - start;
		System.out.format("Loading formulary took %d s%s", TimeUnit.SECONDS.convert(delta, TimeUnit.NANOSECONDS), System.lineSeparator());
		
		try (Scanner s = new Scanner(System.in)) {
			while (true) {
				System.out.println("Type a treatment query (or exit):");
				String str = s.nextLine();
				if ("exit".equals(str)) { break; }
				
				for (FormularyEntry e : f.query(str, FormularyEntryType.TREATMENT, FormularyEntryType.TREATMENT_ALIAS)) {
					System.out.println(e.getId() + ". " + e);
					printDosages(f, e);
					
					FormularyEntry[] arr = f.getNext(FormularyEntryType.TREATMENT, e);
					if (arr == null) { continue; }
					
					for (int i = 0; i < arr.length; i++) {
						System.out.println("Alias for: " + arr[i].getId() + ". " + arr[i]);
						printDosages(f, arr[i]);
					}
				}
			}
		}
		
		Database.disconnect(session);
		Database.disconnect();
	}
	
	private static void printDosages(Formulary f, FormularyEntry fe) {
		FormularyEntry[] arr = f.getNext(FormularyEntryType.DOSAGE, fe);
		if (arr == null) { return; }
		
		for (int i = 0; i < arr.length; i++) {
			System.out.println("  Dosage: " + arr[i].getId() + ". " + arr[i]);
			FormularyEntry[] farr = f.getNext(FormularyEntryType.FORM, fe, arr[i]);
			for (int j = 0; j < farr.length; j++) {
				System.out.println("    Form: " + farr[j]);
			}
		}
	}
}
