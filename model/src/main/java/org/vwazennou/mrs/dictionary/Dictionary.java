/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.dictionary;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.hibernate.Session;
import org.vwazennou.mrs.data.Option;

import com.datamininglab.commons.lang.ReflectionUtils;
import com.datamininglab.commons.structs.lut.LookupTable;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.TIntHashSet;

public class Dictionary {
	private Session session;
	private Language language;
	
	private LookupTable<String, Language> languages;
	private TIntObjectHashMap<DictionaryEntry> entries = new TIntObjectHashMap<>();
	private TIntHashSet deleted = new TIntHashSet();
	
	public Dictionary(Session session) {
		this.session = session;
		
		languages = Language.getAll(session);
		setLanguage(languages.get(Option.LANGUAGE.toString()));
		initialize();
	}
	
	private void initialize() {
		List<?> l = session.createCriteria(DictionaryEntry.class).list();
		for (Object o : l) {
			DictionaryEntry de = (DictionaryEntry) o;
			entries.put(de.hashCode(), de);
		}
	}

	/**
	 * Saves any changes that were made to dictionary (edits or deletes) entries since
	 * the last call.
	 */
	public void applyChanges() {
		// Save or delete any edited dictionary entries before replacing them
		entries.forEachEntry(new TIntObjectProcedure<DictionaryEntry>() {
			@Override
			public boolean execute(int key, DictionaryEntry de) {
				if (deleted.contains(key))  { session.delete(de); }
				else if (!de.isTemporary()) { session.saveOrUpdate(de); }
				return true;
			}
		});
		
		session.getTransaction().commit();
		session.beginTransaction();
		entries.clear();
		deleted.clear();
		
		initialize();
	}
	
	/**
	 * Looks up the translated phrase of the key in the current language.
	 * If there is not a translated value defined, or if the current language
	 * is the default language, this returns the default phrase.  If no default
	 * has been defined, this returns <tt>null</tt>.
	 * @param key the language-insensitive phrase key
	 * @return the translated phrase in the current language
	 */
	public String getPhrase(Str key) {
		if (language == null) { return key.getDefault(); }
		
		DictionaryEntry de = entries.get(DictionaryEntry.hash(language, key));
		return de == null? key.getDefault() : de.getWord();
	}
	
	/**
	 * Sets the language of the system to the specified language. This
	 * must be one of the {@linkplain #getSupportedLanguages() supported languages}.
	 * @param language the new system language
	 */
	public void setLanguage(Language language) {
		this.language = language;
		if (language != null) { Option.LANGUAGE.set(language.getName()); }
	}
	
	/**
	 * Gets the current lanuguage of the system.
	 * @return the current language, or {@link Language#DEFAULT} if no language has been set
	 */
	public Language getLanguage() {
		return language;
	}
	
	/**
	 * Gets the language with the given name. If that language is new, the dictionary creates
	 * and saves the new language
	 * @param name the name of the language
	 * @param country the country in which the language is used (only used to create new instances-
	 * can be <tt>null</tt> if you are certain the language exists
	 * @return the language
	 */
	public Language getOrAddLanguage(String name, String country) {
		Language l = languages.get(name);
		if (l == null) {
			l = new Language(name, country);
			languages.add(l);
		}
		return l;
	}
	
	/**
	 * Gets the current locale corresponding to the selected language, or the default locale if
	 * the name of the current language is not one of the constants defined in {@link Locale}.
	 * @return the current locale
	 */
	public Locale getLocale() {
		String s = getLanguage().getName().toUpperCase();
		Field  f = ReflectionUtils.getField(Locale.class, s);
		Locale l = (Locale) ReflectionUtils.get(f, null);
		
		if (l == null) {
			// TODO: Hack to support Kreyol.
			l = "KREYOL".equals(s)? Locale.FRENCH : Locale.getDefault();
		}
		return l;
	}
	
	/**
	 * Gets the set of supported language names. This does not include the default language.
	 * @return all available languages for the system
	 */
	public Collection<Language> getSupportedLanguages() {
		List<Language> ret = languages.values();
		Collections.sort(ret, LANG_SORTER);
		return ret;
	}
	
	/**
	 * Gets all the dictionary entries in the specified language. This will create
	 * and return new, blank entries for each {@link Str} key that does not have a
	 * mapping for <tt>lang</tt>.  
	 * @param lang the language of the entries
	 * @return a set of matching entries
	 */
	public Set<DictionaryEntry> getEntires(final Language lang) {
		final Set<DictionaryEntry> ret = new HashSet<>();
		entries.forEachValue(new TObjectProcedure<DictionaryEntry>() {
			@Override
			public boolean execute(DictionaryEntry de) {
				if (de.getLanguage().equals(lang)) { ret.add(de); }	
				return true;
			}
		});
		
		Str[] keys = Str.values();
		for (int i = 0; i < keys.length; i++) {
			int hash = DictionaryEntry.hash(lang, keys[i]);
			if (!entries.containsKey(hash)) {
				DictionaryEntry de = new DictionaryEntry(keys[i], lang, keys[i].getDefault());
				entries.put(hash, de);
				ret.add(de);
			}
		}
		return ret;
	}
	
	/**
	 * Deletes the entry from the dictionary.  This will not be reflected in the 
	 * database until the next call to {@link #initializeDictionary(Session)}.
	 * @param entry the entry to delete
	 */
	public void deleteEntry(DictionaryEntry entry) {
		deleted.add(entry.hashCode());
	}
	
	private static final Comparator<Language> LANG_SORTER = new Comparator<Language>() {
		@Override
		public int compare(Language o1, Language o2) {
			return Long.compare(o1.getId(), o2.getId());
		}
	};
}
