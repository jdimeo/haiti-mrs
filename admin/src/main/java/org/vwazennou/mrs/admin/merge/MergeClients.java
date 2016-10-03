/*
 * Copyright (c) 2015 Elder Research, Inc.
 * All rights reserved.
 */
package org.vwazennou.mrs.admin.merge;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.vwazennou.mrs.MRSMergable;
import org.vwazennou.mrs.data.Client;
import org.vwazennou.mrs.data.Database;
import org.vwazennou.mrs.data.Option;
import org.vwazennou.mrs.dictionary.DictionaryEntry;
import org.vwazennou.mrs.dictionary.Language;
import org.vwazennou.mrs.patient.Patient;
import org.vwazennou.mrs.patient.PatientGroup;
import org.vwazennou.mrs.script.Prescription;
import org.vwazennou.mrs.visit.ClinicTeam;
import org.vwazennou.mrs.visit.UrineTest;
import org.vwazennou.mrs.visit.Visit;
import org.vwazennou.mrs.visit.VisitText;

import com.datamininglab.commons.logging.LogContext;

public class MergeClients {
	public enum MergableTable {
		CLINIC_TEAM(ClinicTeam.class),
		DICT_ENTRY(DictionaryEntry.class),
		LANGUAGE(Language.class),
		PAT_GROUP(PatientGroup.class),
		PATIENT(Patient.class),
		VISIT(Visit.class),
		URINE_TEST(UrineTest.class),
		VISIT_TEXT(VisitText.class),
		SCRIPT(Prescription.class);
		
		private Class<? extends MRSMergable> c;
		private MergableTable(Class<? extends MRSMergable> c) { this.c = c; }
		@Override
		public String toString() { return c.getSimpleName(); }
		public Class<? extends MRSMergable> getTableClass() { return c; }
	}
	
	public static void main(String[] args) {
		new MergeClients().merge();
	}
	
	public void merge() {
		if (!Option.isDataPathValid()) {
			System.err.println("Error: required data files could not be found. Exiting.");
			return;
		}
		
		File parentDir = new File(Option.DATA_PATH.toString());
		MergeClient master = new MergeClient(new File(parentDir, Database.MASTER_FILE), Client.UNKNOWN.toString());
		
		List<MergeClient> clients = new ArrayList<>();
		for (File childDir : parentDir.listFiles()) {
			if (childDir.isDirectory()) {
				MergeClient mc = new MergeClient(new File(childDir, Database.DB_FILE), childDir.getName()); 
				clients.add(mc);
				master.saveClient(mc.getClient());
			}
		}
		
		for (MergableTable mt : MergableTable.values()) {
			MergeData masterData = master.getData(mt);
			for (MergeClient client : clients) {
				LogContext.info("Merging %s for client %s", mt, client);
				MergeData clientData = client.getData(mt);
				
				clientData.checkOldBaseline(masterData);
				clientData.merge(masterData);
				clientData.saveNew(master.getSession());
			}
		}
		
		master.close(true);
		for (MergeClient client : clients) { client.close(false); }
		
		// Reload master data to print stats after the fact (inefficient but
		// more of a true test instead of just using current state of master)
		new MergeClient(new File(parentDir, Database.MASTER_FILE), Client.UNKNOWN.toString());
	}
}
