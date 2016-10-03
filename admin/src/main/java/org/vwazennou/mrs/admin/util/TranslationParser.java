/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.admin.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.vwazennou.mrs.data.Database;
import org.vwazennou.mrs.dictionary.Dictionary;
import org.vwazennou.mrs.dictionary.DictionaryEntry;
import org.vwazennou.mrs.dictionary.Language;
import org.vwazennou.mrs.dictionary.Str;

import com.datamininglab.commons.lang.Utilities;

public final class TranslationParser {
	private static final String FILE = "data/translation.tsv";
	private static final String DELIM = "\t";
	private static final int FIRST_ROW = 3;
	
	private TranslationParser() {
		// Prevent initialization
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		Session s = Database.connect();
		Dictionary d = new Dictionary(s);
		
		Language kreyol  = d.getOrAddLanguage("Kreyol",  "Haiti");
		Language french  = d.getOrAddLanguage("French",  "France");
		Language spanish = d.getOrAddLanguage("Spanish", "Spain");
		
		Set<DictionaryEntry> kreyolEntries  = d.getEntires(kreyol);
		Set<DictionaryEntry> frenchEntries  = d.getEntires(french);
		Set<DictionaryEntry> spanishEntries = d.getEntires(spanish);
		
		try (Scanner scan = new Scanner(new File(FILE))) {
			for (int i = 0; scan.hasNextLine(); i++) {
				String line = scan.nextLine();
				if (i < FIRST_ROW) { continue; }
				
				String[] arr  = line.split(DELIM);
				for (int j = 0; j < arr.length; j++) {
					arr[j] = StringUtils.stripToNull(arr[j]);
				}
				
				Str entry = Utilities.valueOf(Str.class, arr[0], null);
				if (entry == null) { continue; }
				
				if (arr.length > 2) { setText(entry, kreyol,  arr[2], kreyolEntries);  }
				if (arr.length > 3) { setText(entry, french,  arr[3], frenchEntries);  }
				if (arr.length > 4) { setText(entry, spanish, arr[4], spanishEntries); }
			}
		}
		
		d.applyChanges();
		Database.disconnect(s);
		Database.disconnect();
	}
	
	private static void setText(Str key, Language l, String text, Set<DictionaryEntry> set) {
		if (text == null) { return; }
		
		for (DictionaryEntry de : set) {
			if (de.getLanguage().equals(l) && de.getKey().equals(key)) {
				de.setWord(text);
				break;
			}
		}
	}
}
