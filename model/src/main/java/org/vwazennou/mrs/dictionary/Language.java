/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.dictionary;

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

import com.datamininglab.commons.hash.DataHashes;
import com.datamininglab.commons.structs.lut.LookupTable;

@Entity
@Table(name = "languages")
public class Language implements Comparable<Language>, MRSMergable {
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
	
	@Column(name = "country", nullable = false)
	private String country;
	
	public Language() {
		// Default constructor provided for Hibernate
	}
	public Language(String name, String country) {
		this.name    = name;
		this.country = country;
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
	
	public String getCountry() { return country; }
	public String getName()    { return name;    }
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Language)) { return false; }
		
		Language l = (Language) obj;
		return name.equalsIgnoreCase(l.name);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public int compareTo(Language o) {
		return name.compareToIgnoreCase(o.name);
	}
	
	@Override
	public String toString() { return name; }
	
	public static LookupTable<String, Language> getAll(Session session) {
		return new LookupTable<>(Language.class, "name", DataHashes.STRING_HASH)
				.setCache(new LUTHibernateCache<Language, String>(session)).fill();
	}
}