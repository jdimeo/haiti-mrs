/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.dictionary;

import java.util.Collection;

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

import org.apache.commons.lang3.BitField;
import org.apache.commons.lang3.StringUtils;
import org.vwazennou.mrs.MRSMergable;
import org.vwazennou.mrs.data.Client;

import com.datamininglab.foundation.data.field.DataFields.StringField;

@Entity
@Table(name = "dictionary")
public class DictionaryEntry implements Comparable<DictionaryEntry>, MRSMergable {
	static Dictionary globalDict;
	public static void setGlobalDictionary(Dictionary d) {
		globalDict = d;
	}
	
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false)
	private int id;
	
	@Version @Column(name = "version")
	private int version;
	
	@Column(name = "original_id")
	private long originalId = UNMERGED;

	@ManyToOne @JoinColumn(name = "original_client")
	private Client originalClient;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "word_id", nullable = false)
	private Str key = Str.NA;
	
	@ManyToOne
	@JoinColumn(name = "language_id", nullable = false)
	private Language lang;

	@Column(name = "word", nullable = false)
	private String word;
	
	private transient boolean temp;
	
	public DictionaryEntry() {
		// Default constructor provided for Hibernate
	}
	public DictionaryEntry(Str key, Language lang, String word) {
		this.key  = key;
		this.lang = lang;
		this.word = word;
		
		// The dictionary creates temporary entries to display to the user via this constructor
		temp = true;
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
	
	public Str      getKey()      { return key;  }
	public Language getLanguage() { return lang; }
	public String   getWord()     { return word; }
	public boolean  isTemporary() { return temp; }
	
	public void setKey(Str key) {
		this.key = key; 
	}
	public void setLanguage(Language lang) {
		this.lang = lang;
	}
	public void setWord(String word) {
		this.word = word;
		
		// The user has now edited this entry, so it is no longer temporary
		temp = false;
	}
	
	public static class KeyField extends StringField<DictionaryEntry> {
		public KeyField() { super(null); }
		
		@Override
		public String get(DictionaryEntry row) {
			if (globalDict == null) {
				return row.getKey().getDefault();
			}
			return globalDict.getPhrase(row.getKey());
		}
		@Override
		public String getName() {
			if (globalDict == null || globalDict.getLanguage() == null) {
				return "Default";
			}
			return globalDict.getLanguage().getName();
		}
	}
	
	public static class ValueField extends StringField<DictionaryEntry> {
 		public ValueField() { super(null); }
 		
		private Language lang;
		@Override
		public String get(DictionaryEntry row) {
			return row.isTemporary()? "-" : row.getWord();
		}
		@Override
		public void set(DictionaryEntry row, String word) {
			row.setWord(word);
		}
		@Override
		public String getName() {
			return lang == null? "Translated phrase" : lang.getName();
		}
		public void setLanguage(Language l) { lang = l; }
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DictionaryEntry)) { return false; }
		
		DictionaryEntry de = (DictionaryEntry) obj;
		return key == de.key && lang.equals(de.lang);
	}
	
	@Override
	public int hashCode() { return hash(lang, key); }
	
	@Override
	public int compareTo(DictionaryEntry o) { return key.compareTo(o.key); }
	
	@Override
	public String toString() { return key + " > " + word; }
	
	private static final BitField STR_ORD = new BitField(0xFFFF0000);
	private static final BitField LANG_ID = new BitField(0x0000FFFF);
	public static int hash(Language lang, Str key) {
		return LANG_ID.setValue(STR_ORD.setValue(0, key.ordinal()), (int) lang.getId());
	}
	
	public static class CompoundStr {
		private Object[] parts;
		
		public CompoundStr(Object... parts) {
			this.parts = parts;
		}
		public CompoundStr(Collection<?> c) {
			this(c.toArray(new Object[c.size()]));
		}
		
		@Override
		public String toString() {
			return StringUtils.join(parts);
		}
	}
} 