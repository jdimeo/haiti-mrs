/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.script;

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

@Entity
@Table(name = "directive_blanks")
public class DirectiveBlank implements Comparable<DirectiveBlank> {
	public enum BlankType {
		TITLE_TEXT, TEXT, NUMERIC, ENUMERATION;
	}
	
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false)
	private int id;

	@ManyToOne @JoinColumn(name = "directive_id", nullable = false)
	private Directive directive;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "blank_type", nullable = false)
	private BlankType type = BlankType.TEXT;
	
	@Column(name = "blank_seq", nullable = false)
	private int blankSeq = -1;
	
	@Column(name = "blank_detail")
	private String detail;
	
	public DirectiveBlank() {
		// Default constructor provided for Hibernate
	}
	public DirectiveBlank(Directive d) { this.directive = d; }
	
	public int       getId()            { return id;        }
	public String    getDetail()        { return detail;    }
	public Directive getDirective()     { return directive; }
	public int       getBlankSequence() { return blankSeq;  }
	public BlankType getType()          { return type;      }

	public void setDirective(Directive d) { this.directive = d;      }
	public void setType(BlankType type)   { this.type      = type;   }
	public void setDetail(String detail)  { this.detail    = detail; }
	public void setBlankSequence(int seq) { this.blankSeq  = seq;    }
	
	@Override
	public int compareTo(DirectiveBlank o) {
		return Integer.compare(getBlankSequence(), o.getBlankSequence());
	}
	
	@Override
	public String toString() { return directive + " - Blank #" + blankSeq; }
}