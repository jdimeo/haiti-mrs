/*
 * Copyright (c) 2015 Elder Research, Inc.
 * All rights reserved.
 */
package org.vwazennou.mrs.admin.merge;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.hibernate.Session;
import org.vwazennou.mrs.admin.merge.MergeClients.MergableTable;
import org.vwazennou.mrs.data.Client;
import org.vwazennou.mrs.data.Database;
import org.vwazennou.mrs.script.Prescription;
import org.vwazennou.mrs.visit.ClinicTeam;
import org.vwazennou.mrs.visit.Visit;

import com.datamininglab.commons.logging.LogContext;
import com.datamininglab.foundation.data.distribution.CategoricalDistribution;
import com.datamininglab.foundation.data.distribution.DataBinSettings;
import com.datamininglab.foundation.orm.SessionFactory;

class MergeClient {
	private File f;
	private List<Client> clients;
	private Client client;
	private SessionFactory sf;
	private Session s;
	
	private Map<MergableTable, MergeData> data;

	MergeClient(File f, String clientName) {
		this.f = f;
		sf = Database.connect(f);
		s = sf.newSession();
		
		// Find the client based on the client name
		clients = s.createCriteria(Client.class).list();
		for (Client c : clients) {
			if (clientName.equals(c.getName())) {
				client = c;
				break;
			}
		}
		if (client == null) {
			throw new IllegalStateException("Client " + clientName + " not found");
		}
		
		data = new HashMap<>();
		for (MergableTable mt : MergableTable.values()) {
			data.put(mt, new MergeData(this, mt).loadAll(s));
		}
		printStats();
	}
	
	File getFile() { return f; }
	Session getSession() { return s; }
	Client getClient() { return client; }
	MergeData getData(MergableTable table) { return data.get(table); }
	
	void saveClient(Client client) {
		client.markMerged();
		for (Client c : clients) {
			if (c.getId() == client.getId()) { return; }
		}
		s.save(client);
	}

	void close(boolean commit) {
		if (commit) {
			s.flush();
			s.getTransaction().commit();
		} else {
			// Avoid any errors because we changed object IDs, etc.
			s.getTransaction().rollback();
			s.clear();
		}
		sf.endSession(s);
		sf.close();
	}
	
	void printStats() {
		LogContext.info("Client %s:", client);
		
		MergeData p = data.get(MergableTable.PATIENT);
		LogContext.info("Patients: %,d new, %,d existing", p.getNew().size(), p.getExisting().size());
		
		MergeData v = data.get(MergableTable.VISIT);
		Collection<Visit> vNew = v.getNew(), vExisting = v.getExisting();
		LogContext.info("Visits: new");
		aggregateByClinic(vNew).print(System.out, Integer.MAX_VALUE, DataBinSettings.ALPHA);
		LogContext.info("Visits: existing");
		aggregateByClinic(vExisting).print(System.out, Integer.MAX_VALUE, DataBinSettings.ALPHA);
		
		MergeData ps = data.get(MergableTable.SCRIPT);
		Collection<Prescription> psNew = ps.getNew(), psExisting = ps.getExisting();
		LogContext.info("Prescriptions: new");
		aggregateByClinic(CollectionUtils.collect(psNew, GET_VISIT)).print(System.out, Integer.MAX_VALUE, DataBinSettings.ALPHA);
		LogContext.info("Prescriptions: existing");
		aggregateByClinic(CollectionUtils.collect(psExisting, GET_VISIT)).print(System.out, Integer.MAX_VALUE, DataBinSettings.ALPHA);
	
		MergeData u = data.get(MergableTable.URINE_TEST);
		LogContext.info("Urine Tests: %,d new, %,d existing", u.getNew().size(), u.getExisting().size());
	}
	
	private static CategoricalDistribution<Void, ClinicTeam> aggregateByClinic(Collection<Visit> visits) {
		CategoricalDistribution<Void, ClinicTeam> ret = new CategoricalDistribution<>();
		for (Visit v : visits) {
			if (v == null) { continue; }
			ret.increment(v.getClinicTeam());
		}
		return ret;
	}
	
	@Override
	public String toString() { return client.toString(); }
	
	private static final Transformer<Prescription, Visit> GET_VISIT = new Transformer<Prescription, Visit>() {
		@Override
		public Visit transform(Prescription input) { return input.getVisit(); }
	};
}
