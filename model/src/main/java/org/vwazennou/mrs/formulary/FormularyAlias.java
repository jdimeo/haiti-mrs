/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.formulary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.vwazennou.mrs.formulary.FormularyEntry.FormularyEntryType;

@Entity
@Table(name = "formulary_aliases")
public class FormularyAlias {
	
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false)
	private int id;
	
	@ManyToOne
	@JoinColumn(name = "entry_id", nullable = false)
	@Cascade(CascadeType.SAVE_UPDATE)
	private FormularyEntry entry;
	
	@ManyToOne
	@JoinColumn(name = "alias_id", nullable = false)
	@Cascade(CascadeType.SAVE_UPDATE)
	private FormularyEntry alias;
	
	public FormularyAlias() {
		// Default constructor provided for Hibernate
	}
	public FormularyAlias(FormularyEntry entry, FormularyEntry alias) {
		if (entry.equals(alias)) {
			throw new IllegalArgumentException("You should not be creating an alias that is equivalent to the entry");
		}
		
		if (entry.getType() != FormularyEntryType.TREATMENT
		 || alias.getType() != FormularyEntryType.TREATMENT_ALIAS) {
			throw new IllegalArgumentException("The entry argument must be type TREATMENT and the alias type TREATMENT_ALIAS");
		}
		
		this.entry = entry;
		this.alias = alias;
	}
	
	public int            getId()    { return id;    }
	public FormularyEntry getEntry() { return entry; }
	public FormularyEntry getAlias() { return alias; }
	
	@Override
	public String toString() { return alias.getName(); }
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof FormularyAlias)) { return false; }
		
		FormularyAlias fa = (FormularyAlias) obj;
		return getEntry().equals(fa.getEntry())
		    && getAlias().equals(fa.getAlias());
	}
	
	@Override
	public int hashCode() {
		return getEntry().hashCode() * 31 + getAlias().hashCode();
	}
}
