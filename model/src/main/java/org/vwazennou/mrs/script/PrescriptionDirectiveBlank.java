/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.script;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity @Table(name = "prescription_directive_blanks")
public class PrescriptionDirectiveBlank implements Comparable<PrescriptionDirectiveBlank> {
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false)
	private long id;
	
	@ManyToOne @JoinColumn(name = "prescription_directive_id")
	private PrescriptionDirective prescriptionDirective;
	
	@ManyToOne @JoinColumn(name = "directive_blank_id")
	private DirectiveBlank blank;
	
	@Column(name = "value")
	private String value;
	
	public PrescriptionDirectiveBlank() {
		// Default constructor provided for Hibernate
	}
	public PrescriptionDirectiveBlank(PrescriptionDirective pd, DirectiveBlank db) {
		this.prescriptionDirective = pd;
		this.blank = db;
		pd.getBlanks().add(this);
	}
	
	public long                  getId()                    { return id;                    }
	public PrescriptionDirective getPrescriptionDirective() { return prescriptionDirective; }
	public DirectiveBlank        getBlank()                 { return blank;                 }
	public String                getValue()                 { return value;                 }

	public void setPrescriptionDirective(PrescriptionDirective pd) { prescriptionDirective = pd; }
	public void setDirectiveBlank(DirectiveBlank db)               { blank = db;                 }
	public void setValue(String val)                               { value = val;                }

	@Override
	public int compareTo(PrescriptionDirectiveBlank o) {
		return Integer.compare(blank.getBlankSequence(), o.blank.getBlankSequence());
	}
	
	@Override
	public String toString() {
		return prescriptionDirective + " - " + blank + " - " + value;
	}
}