/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.formulary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.Session;
import org.hibernate.annotations.Cascade;

import com.datamininglab.foundation.util.CRC64;
import com.datamininglab.foundation.util.HashUtils;
import com.datamininglab.foundation.util.Utilities;

@Entity
@Table(name = "formulary_entries")
public class FormularyEntry implements Comparable<FormularyEntry> {
	private static final String NUM_REGEX = ",.\\d";
	private static final Pattern NUM_IN_STR = Pattern.compile(String.format(
		"([^%s]*)([%s]+)([^%s]*)", NUM_REGEX, NUM_REGEX, NUM_REGEX));
	
	public enum FormularyEntryType {
		FORM, ROUTE_OF_ADMINISTRATION, DOSAGE, TREATMENT, TREATMENT_ALIAS
	}
	
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false)
	private int id;
	
	@Column(name = "name")
	private String name;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "type_id", nullable = false)
	private FormularyEntryType type = FormularyEntryType.TREATMENT;
	
	@Column(name = "rank", nullable = false)
	private int rank;
	
	// We manage this ourselves for performance, so we can load all parts in one bulk query
	private transient List<FormularyEntryPart> parts = new ArrayList<>();
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "entry")
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	private Set<FormularyAlias> aliases = new HashSet<>();
	
	private transient String toString;
	
	public FormularyEntry() {
		// Default constructor provided for Hibernate
	}
	public FormularyEntry(FormularyEntryType type, String name) {
		this.type = type;
		this.name = name;
	}
	
	public int                getId()   { return id;   }
	public String             getName() { return name; }
	public FormularyEntryType getType() { return type; }
	public int                getRank() { return rank; }
	
	private Set<FormularyAlias> getAliases() { return aliases; }
	
	public boolean isCompound() { return getName() == null; }
	
	/**
	 * Scans the entry name for the first digit or decimal, and then replaces that part of the name
	 * with the extracted number multiplied by the parameter.  This is generally used to modify a
	 * dosage for pill fragments.
	 * @param multiplier the coefficient by which to multiply the number
	 * @return the name of this entry, with the first number adjusted by the multiplier, or the name
	 * unmodified if a number was not found in the name
	 */
	public String adjustFirstNumber(float multiplier) {
		String s = toString();
		Matcher m = NUM_IN_STR.matcher(toString());
		if (!m.matches()) { return s; }
		
		float f = NumberUtils.toFloat(m.group(2), Float.NaN);
		if (Float.isNaN(f)) { return s; }
		
		f *= multiplier;
		return m.group(1) + Utilities.stringValue(f, 2) + m.group(3);
	}
	
	public List<FormularyEntry> getCompoundParts() {
		List<FormularyEntry> ret = new ArrayList<>();
		for (FormularyEntryPart p : parts) { ret.add(p.getPart()); }
		return ret;
	}
	public void addPart(FormularyEntryPart part) {
		if (part.getPartSequence() < 0) {
			part.setPartSequence(parts.size());
		}
		parts.add(part);
		toString = null;
	}
	public void sortParts() {
		Collections.sort(parts);
	}
	public void saveParts(Session s) {
		for (FormularyEntryPart p : parts) { s.saveOrUpdate(p); }
	}
	
	public boolean addAlias(FormularyEntry alias) {
		if (alias.isCompound()) {
			boolean ret = false;
			for (FormularyEntryPart fp : alias.parts) {
				ret |= addAlias(fp.getPart());
			}
			return ret;
		}
		return getAliases().add(new FormularyAlias(this, alias));
	}
	
	public void setRank(int rank) { this.rank = rank; }
	
	@Override
	public int compareTo(FormularyEntry o) {
		return Integer.compare(rank, o.rank);
	}
	
	@Override
	public String toString() {
		if (toString != null) { return toString; }
		
		toString = isCompound()? StringUtils.join(parts, "; ") : getName();
		return toString;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof FormularyEntry)) {
			return false;
		}
		
		FormularyEntry fe = (FormularyEntry) obj;
		return getType().equals(fe.getType())
		    && toString().equals(fe.toString());
	}
	
	@Override
	public int hashCode() {
		return (int) (getType().ordinal() * HashUtils.HASH_COEFF + CRC64.getCRC64(toString()));
	}
}