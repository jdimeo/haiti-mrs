/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.script;

import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.vwazennou.mrs.data.ParentOf;
import org.vwazennou.mrs.formulary.Formulary;

@Entity @Table(name = "prescription_directives")
public class PrescriptionDirective implements Comparable<PrescriptionDirective>, ParentOf<PrescriptionDirectiveBlank> {
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false)
	private long id;
	
	@ManyToOne @JoinColumn(name = "prescription_id")
	private Prescription prescription;
	
	@ManyToOne @JoinColumn(name = "directive_id")
	private Directive directive;
	
	@Column(name = "directive_seq")
	private int directiveSeq;
	
	@Column(name = "qualifier")
	private String qualifier;
	
	@OneToMany(mappedBy = "prescriptionDirective", cascade = CascadeType.ALL, orphanRemoval = true)
	@Cascade(org.hibernate.annotations.CascadeType.ALL) @Sort(type = SortType.NATURAL)
	private SortedSet<PrescriptionDirectiveBlank> blanks = new TreeSet<>();
	
	public PrescriptionDirective() {
		// Default constructor provided for Hibernate
	}
	public PrescriptionDirective(Prescription p, Directive d) {
		this.prescription = p;
		this.directive    = d;
		p.getDirectives().add(this);
	}
	
	public void setPrescription(Prescription p) { prescription = p;    }
	public void setDirective(Directive d)       { directive    = d;    }
	public void setDirectiveSequence(int seq)   { directiveSeq = seq;  }
	public void setQualifier(String q)          { qualifier    = q;    } 
	
	public long         getId()                 { return id;           }
	public Directive    getDirective()          { return directive;    }
	public int          getDirectiveSequence()  { return directiveSeq; }
	public Prescription getPrescription()       { return prescription; }
	public String       getQualifier()          { return qualifier;    }
	
	public Set<PrescriptionDirectiveBlank> getBlanks() { return blanks; }
	
	@Override
	public Collection<PrescriptionDirectiveBlank> getChildren() {
		return getBlanks();
	}
	
	@Override
	public void inflateChildren(Formulary f) {
		for (PrescriptionDirectiveBlank pdb : getBlanks()) { pdb.getId(); }
	}
	
	@Override
	public void unpersistChildCollection() {
		blanks = new TreeSet<>(getBlanks());
	}
	
	@Override
	public int compareTo(PrescriptionDirective o) {
		return Integer.compare(directiveSeq, o.directiveSeq);
	}
	
	@Override
	public String toString() {
		return prescription + " - Directive #" + directiveSeq;
	}
}