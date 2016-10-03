/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.visit;

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
import javax.persistence.Version;

import org.vwazennou.mrs.MRSField;
import org.vwazennou.mrs.MRSField.MRSFieldType;
import org.vwazennou.mrs.MRSMergable;
import org.vwazennou.mrs.data.Client;
import org.vwazennou.mrs.dictionary.Language;
import org.vwazennou.mrs.dictionary.Str;

@Entity
@Table(name = "visits_text")
public class VisitText implements MRSMergable {
	public enum VisitTextType {
		COMMENTS,
		LADS_EDUCATION
	}
	
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false)
	private long id;
	
	@Version @Column(name = "version")
	private int version;
	
	@Column(name = "original_id")
	private long originalId = UNMERGED;

	@ManyToOne @JoinColumn(name = "original_client")
	private Client originalClient;
	
	@ManyToOne @JoinColumn(name = "visit_id")
	private Visit visit;
	
	@ManyToOne @JoinColumn(name = "language_id")	
	private Language lang;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "text_type_id")
	private VisitTextType type = VisitTextType.COMMENTS;
	
	// TODO: Make this searchable from Visit
	@Column(name = "text")
	@MRSField(name = Str.COMMENTS, type = MRSFieldType.STRING)
	private String text;

	public VisitText() {
		// Default constructor provided for Hibernate
	}
	public VisitText(Visit v, Language l, VisitTextType t, String s) {
		visit = v; lang = l; type = t; text = s;
	}

	@Override
	public long getId() { return id; }
	@Override
	public Client getOriginalClient() { return originalClient; }
	@Override
	public long getOriginalId() { return originalId; }
	@Override
	public void setOriginalClient(Client c) { originalClient = c; }
	@Override
	public void setOriginalId(long id) { originalId = id; }
	@Override
	public int getVersion() { return version; }
	
	public Visit         getVisit()    { return visit; }
	public Language      getLanguage() { return lang; }
	public VisitTextType getType()     { return type; }
	public String        getText()     { return text; }
	@Override
	public String        toString()    { return getText(); }
	
	public void setVisit(Visit visit) { this.visit = visit; }
	public void setText(String text) { this.text = text; }
}