/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.admin.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.vwazennou.mrs.data.Client;
import org.vwazennou.mrs.data.Database;
import org.vwazennou.mrs.formulary.Formulary;
import org.vwazennou.mrs.formulary.FormularyEntry;
import org.vwazennou.mrs.formulary.FormularyEntry.FormularyEntryType;
import org.vwazennou.mrs.script.Directive;
import org.vwazennou.mrs.script.DirectiveBlank;
import org.vwazennou.mrs.script.DirectiveBlank.BlankType;
import org.vwazennou.mrs.script.Prescription;
import org.vwazennou.mrs.script.PrescriptionDirective;
import org.vwazennou.mrs.script.PrescriptionDirectiveBlank;

import com.datamininglab.foundation.data.lut.LookupTable;
import com.datamininglab.foundation.ui.StatusListener.DefaultStatusListener;
import com.datamininglab.foundation.ui.StatusMonitor;

public final class PrescriptionParser {
	private static final String DELIM = Pattern.quote("|");
	
	private static Formulary formulary;
	private static LookupTable<Directive, Integer> directives;
	
	private PrescriptionParser() {
		// Prevent initialization
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		StatusMonitor sm = new StatusMonitor("ScriptParser").addListener(new DefaultStatusListener());
		
		Session session = Database.connect();
		directives = Directive.getAll(session);
		
		formulary = new Formulary(session);
		
		List<?> l = session.createCriteria(Prescription.class)
		                   .add(Restrictions.isNull("visit")).list();
		sm.newTask("Deleting " + l.size() + " existing standard prescriptions...");
		for (Object o : l) { session.delete(o); }
		
		sm.newTask("Parsing directives...");
		DirectiveParser.parseDirectives(session, directives);
		
		sm.newTask("Adding standard prescriptions...");
		Scanner s = new Scanner(new File("data/standard.scripts.txt"));
		s.nextLine(); // Eat headers
		while (s.hasNextLine()) {
			String[] line    = s.nextLine().split(DELIM);
			String   dx      = line[0];
			String   qty     = line[1];
			String   med     = line[2];
			String   dos     = line[3];
			String   form    = line[4];
			
			int semi = form.indexOf(';');
			if (semi > 0) { form = form.substring(0, semi); }
			
			Prescription p = new Prescription();
			try {
				p.setQuantity(Float.parseFloat(qty));
			} catch (NumberFormatException ex) {
				p.setQuantity(-1.0f);
			}
			p.setTreatment(find(med, FormularyEntryType.TREATMENT));
			p.setDosage(find(dos, FormularyEntryType.DOSAGE));
			p.setForm(find(form, FormularyEntryType.FORM));
			p.setDiagnosis(dx);
			p.setOriginalClient(Client.UNKNOWN);
			session.save(p);
			
			parseDirectiveBlanks(session, parseDirectives(session, p, line[5]), line[6]);
		}
		s.close();
		
		Database.disconnect(session);
		Database.disconnect();
		sm.setFinished();
	}
	
	private static PrescriptionDirective parseDirectives(Session session, Prescription p, String dirStr) {
		PrescriptionDirective ret = null;
		String[] arr = dirStr.split(",");
		
		for (int i = 0; i < arr.length; i++) {
			if ("N/A".equals(arr[i])) { continue; }
			
			int n = NumberUtils.toInt(arr[i], -1);
			if (n < 0) {
				System.err.println("Warning: ignoring unparseable directive number " + arr[i]);
				continue;
			}
			
			Directive d = directives.get(n);
			if (d == null) {
				System.err.println("Warning: no directive with code " + n);
				continue;
			}
			
			PrescriptionDirective pd = p.addDirective(d);
			session.save(pd);
			
			if (i == 0) { ret = pd; }
		}
		return ret;
	}
	
	private static void parseDirectiveBlanks(Session session, PrescriptionDirective pd, String details) {
		if (pd == null) { return; }
		
		String[] arr = details.split(",");
		int i = 0;
		for (DirectiveBlank blank : pd.getDirective().getBlanks()) {
			PrescriptionDirectiveBlank pdb = new PrescriptionDirectiveBlank(pd, blank);
			if (blank.getType() == BlankType.TITLE_TEXT) {
				pdb.setValue(pd.getPrescription().getDiagnosis());
			} else {
				if (i >= arr.length) {
					System.err.println("Warning: not enough details provided for directive " +
							pd.getDirective().getCode() + " for " + pd.getPrescription());
				} else {
					pdb.setValue(arr[i++]);
				}
			}
			session.save(pdb);
		}
	}
	
	private static FormularyEntry find(String s, FormularyEntryType t) {
		FormularyEntry[] arr = formulary.query(s, t);
		return arr.length == 0? null : arr[0];
	}
}
