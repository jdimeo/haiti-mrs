/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.patient;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.Session;
import org.vwazennou.mrs.MRSMergable;
import org.vwazennou.mrs.data.Client;

import com.datamininglab.foundation.data.hash.DataHashes;
import com.datamininglab.foundation.data.lut.LookupTable;
import com.datamininglab.foundation.orm.LUTHibernateCache;

@Entity
@Table(name = "patient_groups")
public class PatientGroup implements MRSMergable {
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false)
	private int id;
	
	@Version @Column(name = "version")
	private int version;
	
	@Column(name = "original_id")
	private long originalId = UNMERGED;

	@ManyToOne @JoinColumn(name = "original_client")
	private Client originalClient;
	
	@Column(name = "name", nullable = false)
	private String name;
	
	public PatientGroup() {
		this("New Group");
	}
	public PatientGroup(String name) {
		this.name = name;
	}

	@Override
	public long getId() { return id; }
	@Override
	public Client getOriginalClient() { return originalClient; }
	@Override
	public long getOriginalId() { return originalId; }
	@Override
	public void setOriginalClient(Client c) { originalClient = c; }
	@Override
	public void setOriginalId(long id) { originalId = id; }
	@Override
	public int getVersion() { return version; }
	@Override
	public String toString() { return name; }
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof PatientGroup)) { return false; }
		return name.equals(((PatientGroup) obj).name);
	}
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	public static LookupTable<PatientGroup, String> getAll(Session session) {
		return new LookupTable<>(PatientGroup.class, "name", DataHashes.STRING_HASH)
				.setCache(new LUTHibernateCache<PatientGroup, String>(session)).fill();
	}
}
