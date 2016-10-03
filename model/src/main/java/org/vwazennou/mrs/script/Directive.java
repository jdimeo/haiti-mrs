/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.script;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.annotations.Cascade;

import com.datamininglab.commons.hash.DataHashes;
import com.datamininglab.commons.structs.lut.LookupTable;

@Entity @Table(name = "directives")
public class Directive implements Comparable<Directive> {
	public enum DirectiveType { INSTRUCTION, ALERT }

	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false)
	private int id;
	
	@Column(name = "code", nullable = false)
	private int code;
	
	@Column(name = "type_id")
	@Enumerated(EnumType.ORDINAL)
	private DirectiveType type = DirectiveType.INSTRUCTION;
	
	@OneToMany(mappedBy = "directive", cascade = CascadeType.ALL, orphanRemoval = true)
	@Cascade(org.hibernate.annotations.CascadeType.ALL) @OrderBy("blankSeq")
	private List<DirectiveBlank> blanks = new ArrayList<>();
	
	public int           getId()   { return id;   }
	public int           getCode() { return code; }
	public DirectiveType getType() { return type; }
	
	public void setType(DirectiveType type) { this.type = type; }
	public void setCode(int code)           { this.code = code; }
	
	public List<DirectiveBlank> getBlanks() { return blanks; }
	
	public void addBlank(DirectiveBlank db) {
		db.setDirective(this);
		db.setBlankSequence(blanks.size());
		blanks.add(db);
	}
	
	@Override
	public String toString() { return "Directive " + code; }
	
	@Override
	public int compareTo(Directive o) {
		return Integer.compare(getCode(), o.getCode());
	}
	
	public static LookupTable<Integer, Directive> getAll(Session session) {
		return new LookupTable<Directive, Integer>(Directive.class, "code", DataHashes.INT_HASH)
				.setCache(new LUTHibernateCache<Directive, Integer>(session)).fill();
	}
}