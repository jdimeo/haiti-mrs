/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.vwazennou.mrs.dictionary.DictionaryEntry;
import org.vwazennou.mrs.dictionary.Language;
import org.vwazennou.mrs.formulary.FormularyAlias;
import org.vwazennou.mrs.formulary.FormularyEntry;
import org.vwazennou.mrs.formulary.FormularyEntryPart;
import org.vwazennou.mrs.formulary.FormularyTuple;
import org.vwazennou.mrs.patient.Patient;
import org.vwazennou.mrs.patient.PatientGroup;
import org.vwazennou.mrs.script.Directive;
import org.vwazennou.mrs.script.DirectiveBlank;
import org.vwazennou.mrs.script.DirectiveText;
import org.vwazennou.mrs.script.Prescription;
import org.vwazennou.mrs.script.PrescriptionDirective;
import org.vwazennou.mrs.script.PrescriptionDirectiveBlank;
import org.vwazennou.mrs.visit.ClinicTeam;
import org.vwazennou.mrs.visit.UrineTest;
import org.vwazennou.mrs.visit.Visit;
import org.vwazennou.mrs.visit.VisitText;

import com.datamininglab.foundation.orm.SessionFactory;

public final class Database {
	public static final String DB_FILE = "database.db";
	public static final String MASTER_FILE = "master.db";
	
	private static SessionFactory factory;
	public  static SessionFactory getConnection() { return factory; }
	
	public static File getClientDirectory() {
		return new File(Option.DATA_PATH + File.separator + Option.CLIENT_NAME);
	}
	
	private Database() {
		// Prevent initialization
	}
	
	/** For advanced users only! */
	public static SessionFactory connect(File file) {
		Configuration c = new Configuration();
		c.addAnnotatedClass(Client.class);
		c.addAnnotatedClass(ClinicTeam.class);
		c.addAnnotatedClass(DictionaryEntry.class);
		c.addAnnotatedClass(Language.class);
		c.addAnnotatedClass(Patient.class);
		c.addAnnotatedClass(PatientGroup.class);
		c.addAnnotatedClass(UrineTest.class);
		c.addAnnotatedClass(Visit.class);
		c.addAnnotatedClass(VisitText.class);
		
		c.addAnnotatedClass(FormularyAlias.class);
		c.addAnnotatedClass(FormularyEntry.class);
		c.addAnnotatedClass(FormularyEntryPart.class);
		c.addAnnotatedClass(FormularyTuple.class);
		
		c.addAnnotatedClass(Directive.class);
		c.addAnnotatedClass(DirectiveBlank.class);
		c.addAnnotatedClass(DirectiveText.class);
		c.addAnnotatedClass(Prescription.class);
		c.addAnnotatedClass(PrescriptionDirective.class);
		c.addAnnotatedClass(PrescriptionDirectiveBlank.class);
	
		return SessionFactory.connectToSQLite(c, file);
	}
	
	public static Session connect() {
		if (factory == null) {
			File dir = getClientDirectory();
			File db  = new File(dir, DB_FILE);
			if (!db.exists()) {
				dir.mkdirs();
				try {
					Files.copy(Paths.get(Option.DATA_PATH.toString(), MASTER_FILE), Paths.get(db.toURI()));
				} catch (IOException e) {
					System.err.println("Could not copy master database into folder " + dir);
					e.printStackTrace();
				}
			}	
			factory = connect(db);
		}
		return factory.newSession();
	}
	
	public static void disconnect(Session s) {
		if (s != null && factory != null) { factory.endSession(s); }
	}
	public static void disconnect() {
		if (factory != null) { factory.close(); }
		factory = null;
	}
}
