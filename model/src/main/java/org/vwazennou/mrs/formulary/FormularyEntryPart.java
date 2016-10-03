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

@Entity
@Table(name = "formulary_entry_parts")
public class FormularyEntryPart implements Comparable<FormularyEntryPart> {
	
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false)
	private int id;
	
	@ManyToOne
	@JoinColumn(name = "entry_id", nullable = false)
	@Cascade(CascadeType.SAVE_UPDATE)
	private FormularyEntry entry;
	
	@ManyToOne
	@JoinColumn(name = "part_id", nullable = false)
	@Cascade(CascadeType.SAVE_UPDATE)
	private FormularyEntry part;
	
	@Column(name = "part_seq", nullable = false)
	private int partSeq = -1;
	
	public FormularyEntryPart() {
		// Default constructor provided for Hibernate
	}
	public FormularyEntryPart(FormularyEntry entry, FormularyEntry part) {
		this.entry = entry;
		this.part  = part;
	}
	
	public int  getId()              { return id;      }
	public int  getPartSequence()    { return partSeq; }
	public FormularyEntry getEntry() { return entry;   }
	public FormularyEntry getPart()  { return part;    }
	
	protected void setPartSequence(int partSeq) {
		this.partSeq = partSeq;
	}

	@Override
	public String toString() { return part.toString(); }
	
	@Override
	public int compareTo(FormularyEntryPart o) {
		return Integer.compare(partSeq, o.partSeq);
	}
}