/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.visit;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

import com.datamininglab.commons.hash.DataHashes;
import com.datamininglab.commons.structs.lut.LookupTable;

@Entity
@Table(name = "clinic_teams")
public class ClinicTeam implements MRSMergable {
	public enum ClinicType {
		NORMAL, REFILL, OTHER
	}
	
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false)
	private int id;
	
	@Version @Column(name = "version")
	private int version;
	
	@Column(name = "original_id")
	private long originalId = UNMERGED;

	@ManyToOne @JoinColumn(name = "original_client")
	private Client originalClient;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "clinic_type_id")
	private ClinicType type = ClinicType.NORMAL;
	
	@Column(name = "name", nullable = false)
	private String name;
	
	public ClinicTeam() {
		this("New Clinic");
	}
	public ClinicTeam(String name) {
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
	
	public ClinicType getType()  { return type; }
	@Override
	public String     toString() { return name; }
	
	public void setType(ClinicType type) {
		if (type != null) { this.type = type; }
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ClinicTeam)) { return false; }
		return name.equals(((ClinicTeam) obj).name);
	}
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	public static LookupTable<String, ClinicTeam> getAll(Session session) {
		return new LookupTable<>(ClinicTeam.class, "name", DataHashes.STRING_HASH)
				.setCache(new LUTHibernateCache<ClinicTeam, String>(session)).fill();
	}
}
