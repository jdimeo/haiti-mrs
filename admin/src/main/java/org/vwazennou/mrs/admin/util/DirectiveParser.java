/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.admin.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.hibernate.Session;
import org.vwazennou.mrs.data.Database;
import org.vwazennou.mrs.dictionary.Dictionary;
import org.vwazennou.mrs.dictionary.Language;
import org.vwazennou.mrs.script.Directive;
import org.vwazennou.mrs.script.DirectiveTextRegistry;
import org.vwazennou.mrs.script.Directive.DirectiveType;
import org.vwazennou.mrs.script.DirectiveBlank;
import org.vwazennou.mrs.script.DirectiveBlank.BlankType;
import org.vwazennou.mrs.script.DirectiveText;

import com.datamininglab.foundation.data.lut.LookupTable;

public final class DirectiveParser {
	private static final String DELIM       = Pattern.quote("|");
	private static final int    DIR_NUM_COL = 0,
	                            TYPE_COL    = 1,
	                            LANG_COL    = 2,
	                            TITLE_COL   = 3,
	                            TEXT_COL    = 4;
	private static final char   TEXT        = '$',
	                            NUM         = '#',
	                            ENUM        = '[';
	
	private DirectiveParser() {
		// Prevent initialization
	}
	
	public static void parseDirectives(Session session, LookupTable<Directive, Integer> directives) throws FileNotFoundException {
		Dictionary d = new Dictionary(session);
		DirectiveTextRegistry dtr = new DirectiveTextRegistry(session);
		
		Scanner s = new Scanner(new File("data/directives.txt"), "UTF-8");
		while (s.hasNextLine()) {
			String line = s.nextLine().trim();
			if (line.startsWith("//")) { continue; }
			
			String[] arr = line.split(DELIM);
			
			int num;
			try {
				num = Integer.parseInt(arr[DIR_NUM_COL].trim());
			} catch (NumberFormatException ex) {
				System.err.println("Warning: directive number unparseable for line " + line);
				continue;
			}
			
			Directive dir = directives.get(num);
			if (dir == null) {
				dir = new Directive();
				dir.setCode(num);
				if (arr[TYPE_COL].equalsIgnoreCase("A")) {
					dir.setType(DirectiveType.ALERT);	
				} else {
					dir.setType(DirectiveType.INSTRUCTION);
				}
				directives.add(dir);
			}
			
			Language lang     = null;
			String   langName = arr.length > LANG_COL? arr[LANG_COL].trim() : "";
			String   country  = null;
			
			if (!langName.isEmpty()) {
				int i = langName.indexOf(':');
				if (i > 0) {
					country  = langName.substring(i + 1);
					langName = langName.substring(0, i);
				}
				
				lang = d.getOrAddLanguage(langName, country);
			}
			
			String title = arr.length > TITLE_COL? arr[TITLE_COL].trim() : "";
			String text  = arr.length > TEXT_COL?  arr[TEXT_COL].trim()  : "";

			// No language- defining blank structure
			if (lang == null) {
				if (dir.getBlanks().isEmpty()) {
					if (!title.isEmpty()) { parseBlanks(dir, title, true);  }
					if (!text.isEmpty())  { parseBlanks(dir, text,  false); }
				}
				continue;
			}
			
			// Language given- defining a new directive translation
			List<DirectiveBlank> list = dir.getBlanks();
			int correctBlanks = list.size();
			int actualBlanks  = countChar(title, '_') + countChar(text, '_');
			
			if (correctBlanks != actualBlanks) {
				System.err.println("Warning: expected " + correctBlanks + " blanks in " + line + " (only " + actualBlanks + ")");
				continue;
			}
			
			DirectiveText dt = dtr.get(lang, num);
			if (dt == null) {
				dt = new DirectiveText(dir);
				dt.setLanguage(lang);
				session.save(dt);
			}
			dt.setTitle(title);
			dt.setText(text);
		}
		s.close();
	}
	
	private static int countChar(String s, char c) {
		int j = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == c) { j++; }
		}
		return j;
	}
	
	private static void parseBlanks(Directive dir, String s, boolean isTitle) {
		String[] arr = s.split("[\\s]+");
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].isEmpty()) { continue; }
			
			DirectiveBlank db = new DirectiveBlank();
			db.setDetail("");
			dir.addBlank(db);
			
			switch (arr[i].charAt(0)) {
				case NUM:
					db.setType(BlankType.NUMERIC);
					break;
				case ENUM:
					db.setType(BlankType.ENUMERATION);
					db.setDetail(arr[i].substring(1, arr[i].length() - 1));
					break;
				case TEXT: default:
					db.setType(isTitle? BlankType.TITLE_TEXT : BlankType.TEXT);
					break;
			}
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		Session s = Database.connect();
		parseDirectives(s, Directive.getAll(s));
		Database.disconnect(s);
		Database.disconnect();
	}
}