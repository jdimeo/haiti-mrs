/*
 * Copyright (c) 2015 Elder Research, Inc.
 * All rights reserved.
 */
package org.vwazennou.mrs.data;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.Session;

import com.datamininglab.foundation.data.hash.DataHashes;
import com.datamininglab.foundation.data.lut.LookupTable;
import com.datamininglab.foundation.orm.LUTHibernateCache;

@Entity
@Table(name = "clients")
public class Client {
	public static final Client UNKNOWN = new Client(1L).setName("Unknown/Historical");
	
	@Id @Column(nullable = false)
	private long id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "last_merge_date")
	private long lastMerged;
	
	@Column(name = "last_modify_date")
	private long lastModified;

	public Client() {
		// Default constructor provided for Hibernate
	}
	public Client(Long id) {
		this.id = id;
	}
	
	public Client setName(String name) {
		this.name = name;
		return this;
	}
	public void markMerged() {
		lastMerged = System.currentTimeMillis();
	}
	public void markModified() {
		lastModified = System.currentTimeMillis();
	}
	
	public long   getId()               { return id; }
	public String getName()             { return name; }
	public Date   getLastMergedDate()   { return new Date(lastMerged); }
	public Date   getLastModifiedDate() { return new Date(lastModified); }
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Client)) { return false; }
		
		Client c = (Client) obj;
		return id == c.id;
	}
	
	@Override
	public int hashCode() { return (int) id; }
	
	@Override
	public String toString() { return name; }
	
	public static LookupTable<Client, Long> getAll(Session session) {
		return new LookupTable<Client, Long>(Client.class, "id", DataHashes.INTEGER_HASH)
				.setCache(new LUTHibernateCache<Client, Long>(session)).fill();
	}
}
