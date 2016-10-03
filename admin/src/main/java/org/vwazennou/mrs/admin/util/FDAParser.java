package org.vwazennou.mrs.admin.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.hibernate.Session;
import org.vwazennou.mrs.data.Database;
import org.vwazennou.mrs.formulary.FormularyEntry;
import org.vwazennou.mrs.formulary.FormularyEntry.FormularyEntryType;
import org.vwazennou.mrs.formulary.FormularyEntryPart;
import org.vwazennou.mrs.formulary.FormularyTuple;

import com.datamininglab.commons.lang.Utilities;

public final class FDAParser {
	private static final int FORM_COL      = 2;
	private static final int DOSAGE_COL    = 3;
	private static final int DRUGNAME_COL  = 7;
	private static final int TREATMENT_COL = 8;
	
	private static final String[] FILE_LIST = {
		"data/product.fda.txt",
		"data/product.mrs.txt" // Custom treatments specific to our clinics
	};
	
	private static final String[] ALIAS_BLACKLIST = {
		" IN ", " W/", " WITH ",
	    "FOR INJECTION",
	    "(MICRONIZED)",
	    "(COPACKAGED)",
	    "AND DEXTROSE",
	    "STARTER PACK",
	    "PRESERVATIVE FREE",
	    "SULFITE FREE",
	    "DYE FREE",
	    "COMBINATION PACK",
	    "KIT"
	};
	
	private static Map<String, FormularyEntry> map;
	private static Set<FormularyTuple> tuples;
	
	private FDAParser() {
		// Prevent initialization
	}
	
	public static void parseFDA(Session session) throws FileNotFoundException {
		map    = new HashMap<>();
		tuples = new HashSet<>(); 
		
		for (String file : FILE_LIST) {
			try (Scanner s = new Scanner(new File(file))) {
				s.nextLine(); // headers
				while (s.hasNextLine()) {
					String line = s.nextLine().toUpperCase();
					String[] arr = split(line, "\t");
					
					if (arr[DOSAGE_COL].isEmpty() || "0".equals(arr[DOSAGE_COL])) {
						arr[DOSAGE_COL] = "N/A";
					}
					
					String[] frmArr = split(arr[FORM_COL], ";");
					FormularyEntry frm = (frmArr.length == 0)? null
						               : buildEntry(FormularyEntryType.FORM,      frmArr[0]);
					FormularyEntry dos = buildEntry(FormularyEntryType.DOSAGE,    arr[DOSAGE_COL]);
					FormularyEntry trt = buildEntry(FormularyEntryType.TREATMENT, arr[TREATMENT_COL]);
					
					tuples.add(new FormularyTuple(trt, dos, frm));
					
					if (frmArr.length > 1) {
						String[] roaArr = frmArr[1].split(", ?");
						for (int i = 0; i < roaArr.length; i++) {
							buildEntry(FormularyEntryType.ROUTE_OF_ADMINISTRATION, roaArr[i]);
						}
					}
					
					String name = arr[DRUGNAME_COL];
					if (name.equals(arr[TREATMENT_COL])) { continue; }
					
					name = name.replace('-', ' ');			
					for (int i = 0; i < ALIAS_BLACKLIST.length; i++) {
						int j = name.indexOf(ALIAS_BLACKLIST[i]);
						if (j > 0) { name = name.substring(0, j); }
					}
					
					// Strip trailing non-alpha chars (e.g. 0.1%), since we want to isolate brand names
					name = name.replaceAll("[^A-Z]+$", "");
					if (name.length() < 4) { continue; }
					
					trt.addAlias(buildEntry(FormularyEntryType.TREATMENT_ALIAS, name));
				}
			}
		}
		
		Collection<FormularyEntry> set = map.values();
		FormularyEntry[] arr = new FormularyEntry[set.size()];
		set.toArray(arr);
		
		// Use default sorting to initialize ranks (these will eventually be replaced with
		// popularity-based ranks)
		Arrays.sort(arr, 0, arr.length, ENTRY_SORTER);
		for (int i = 0; i < arr.length; i++) {
			arr[i].setRank(i + 1);
			
			// Don't save compound aliases since each part was added individually
			if (arr[i].getType() != FormularyEntryType.TREATMENT_ALIAS || !arr[i].isCompound()) {
				session.saveOrUpdate(arr[i]);
				arr[i].saveParts(session);
			}
		}
		
		for (FormularyTuple ft : tuples) { session.save(ft); }
	}
	
	private static FormularyEntry buildEntry(FormularyEntryType type, String str) {
		int idx = str.indexOf('*');
		if (idx > 0) {
			str = str.substring(0, idx);
		}
		idx = str.indexOf('(');
		if (idx > 0) {
			str = str.substring(0, idx);
		}
		if (str.startsWith("EQ")) {
			str = str.substring(2);
		}
		str = str.trim();
		
		String key = type.name() + "$$$" + str;
		
		FormularyEntry e = map.get(key);
		if (e == null) {
			String[] arr = split(str, ";");
			if (arr.length == 1) {
				e = new FormularyEntry(type, str);
			} else {
				e = new FormularyEntry(type, null);
				for (int i = 0; i < arr.length; i++) {
					e.addPart(new FormularyEntryPart(e, buildEntry(type, arr[i])));
				}
			}
			map.put(key, e);
		}
		return e;
	}
	
	private static String[] split(String s, String split) {
		String[] arr = s.split(split);
		for (int i = 0; i < arr.length; i++) {
			arr[i] = Utilities.clean(arr[i]);
		}
		return arr;
	}
	
	private static final Comparator<FormularyEntry> ENTRY_SORTER = (o1, o2) -> {
		if (!o1.isCompound() &&  o2.isCompound()) { return -1; }
		if (o1.isCompound() && !o2.isCompound()) { return 1; }
		return o1.toString().compareTo(o2.toString());
	};
	
	public static void main(String[] args) throws FileNotFoundException {
		Session s = Database.connect();
		parseFDA(s);
		Database.disconnect(s);
		Database.disconnect();
	}
}