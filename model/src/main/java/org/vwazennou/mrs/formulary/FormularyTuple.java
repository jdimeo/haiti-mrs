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


@Entity
@Table(name = "formulary_tuples")
public class FormularyTuple {
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false)
	private int id;
	
	@ManyToOne
	@JoinColumn(name = "treatment_id", nullable = false)
	private FormularyEntry treatment;
	
	@ManyToOne
	@JoinColumn(name = "dosage_id", nullable = false)
	private FormularyEntry dosage;
	
	@ManyToOne
	@JoinColumn(name = "form_id")
	private FormularyEntry form;
	
	public FormularyTuple() {
		// Default constructor provided for Hibernate
	}
	public FormularyTuple(FormularyEntry treatment, FormularyEntry dosage, FormularyEntry form) {
		if (treatment == null || dosage == null) {
			System.out.println("huh?");
		}
		
		this.treatment = treatment;
		this.dosage    = dosage;
		this.form      = form;
	}
	
	public int            getId()        { return id;        }
	public FormularyEntry getTreatment() { return treatment; }
	public FormularyEntry getDosage()    { return dosage;    }
	public FormularyEntry getForm()      { return form;      }
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof FormularyTuple)) { return false; }
		
		FormularyTuple ft = (FormularyTuple) obj;
		return getTreatment().equals(ft.getTreatment())
		    && getDosage().equals(ft.getDosage())
		    && getForm().equals(ft.getForm());
	}
	
	@Override
	public int hashCode() {
		return (getTreatment().hashCode() * 31 + getDosage().hashCode()) * 31 + getForm().hashCode();
	}
}
