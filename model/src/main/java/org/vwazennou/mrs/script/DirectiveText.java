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

import org.vwazennou.mrs.dictionary.Language;

@Entity @Table(name = "directive_text")
public class DirectiveText {
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false)
	private int id;
	
	@ManyToOne @JoinColumn(name = "directive_id")
	private Directive directive;
	
	@JoinColumn(name = "language_id") @ManyToOne
	private Language language;
	
	@Column(name = "title", nullable = false)
	private String title = "";
	
	@Column(name = "text", nullable = false)
	private String text = "";
	
	public DirectiveText() {
		// Default constructor provided for Hibernate
	}
	public DirectiveText(Directive d) { directive = d; }
	
	public int       getId()        { return id;        }
	public Directive getDirective() { return directive; }
	public Language  getLanguage()  { return language;  }
	public String    getText()      { return text;      }
	public String    getTitle()     { return title;     }
	
	public void setLanguage(Language lang) { this.language = lang;  }
	public void setText(String text)       { this.text     = text;  }
	public void setTitle(String title)     { this.title    = title; }
}