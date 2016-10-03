/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.formulary;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.hibernate.Session;
import org.vwazennou.mrs.formulary.FormularyEntry.FormularyEntryType;

import com.datamininglab.commons.logging.LogContext;
import com.datamininglab.foundation.util.CRC64;
import com.datamininglab.foundation.util.HashUtils;

public class Formulary {
	private static final char DELIM = '$';
	
	private FormularyEntry[] all;
	private TIntHashSet[]    names;
	private TIntHashSet[]    parts;
	private TIntHashSet[]    dosages;
	
	private volatile boolean loaded;
	private TIntObjectHashMap<TIntHashSet>  forms;
	private TLongObjectHashMap<TIntHashSet> index;
	
	public Formulary(Session s) {
		LogContext.info("Loading medicine database...");
		
		List<?> entries = s.createCriteria(FormularyEntry.class).list();
		index = new TLongObjectHashMap<>();
		forms = new TIntObjectHashMap<>();
		
		// IDs are 1-based so allocate one extra space to avoid a lot of -1 ops
		all     = new FormularyEntry[entries.size() + 1];
		names   = new TIntHashSet[all.length];
		parts   = new TIntHashSet[all.length];
		dosages = new TIntHashSet[all.length];
		
		// We don't need the whole POJO because of the compressed way we store it in RAM
		fill(names,   s.createSQLQuery("select alias_id, entry_id from formulary_aliases").list());
		fill(dosages, s.createSQLQuery("select treatment_id, dosage_id, form_id from formulary_tuples").list());
		
		List<?> partList = s.createCriteria(FormularyEntryPart.class).list();
		for (Object o : partList) {
			// This is fast (i.e. doesn't trigger a join) because
			// all entries are in the session cache at this point
			FormularyEntryPart p = (FormularyEntryPart) o;
			p.getEntry().addPart(p);
			
			int pid = p.getPart().getId();
			if (parts[pid] == null) {
				parts[pid] = new TIntHashSet();
			}
			parts[pid].add(p.getEntry().getId());
		}
		
		// Initialize the index by filling with type/n-gram tuples (min length 2):
		// TREATMENT$HY
		// TREATMENT$HYD
		// TREATMENT$HYDR
		// TREATMENT$HYDRO ... etc.
		for (Object o : entries) {
			FormularyEntry fe = (FormularyEntry) o;
			all[fe.getId()] = fe;
			
			StringBuilder sb = new StringBuilder(fe.getType().name());
			sb.append(DELIM);
			String str = fe.toString();
			for (int i = 0; i < str.length(); i++) {
				sb.append(str.charAt(i));
				if (i > 0) {
					long h = CRC64.getCRC64(sb.toString());
					TIntHashSet set = index.get(h);
					if (set == null) {
						set = new TIntHashSet();
						index.put(h, set);
					}
					set.add(fe.getId());
				}
			}
		}
		
		// Since we didn't specify an order in the query, make sure each 
		// compound entry's parts are sorted in the right order
		for (int i = 1; i < all.length; i++) {
			if (all[i].isCompound()) { all[i].sortParts(); }
		}
		loaded = true;
	}
	
	private void fill(TIntHashSet[] arr, List<?> l) {
		for (Object o : l) {
			Object[] row = (Object[]) o;
			int id1 = (Integer) row[0];
			int id2 = (Integer) row[1];
			
			if (arr[id1] == null) {
				arr[id1] = new TIntHashSet();
			}
			arr[id1].add(id2);
			
			if (arr == dosages) {
				int id3 = (Integer) row[2];
				
				int hash = hash(id1, id2);
				TIntHashSet set = forms.get(hash);
				
				if (set == null) {
					set = new TIntHashSet();
					forms.put(hash, set);
				}
				set.add(id3);
			}
		}
	}
	
	private static int hash(int tid, int did) {
		return (int) (tid * HashUtils.HASH_COEFF + did);
	}
	
	/**
	 * Returns the formulary instance for the given entry instance.  This allows you to replace
	 * a formulary entry loaded by some other means (like a database query) with the entry instance
	 * maintained by the formulary.
	 * @param fe the entry
	 * @return the canonical entry
	 */
	public FormularyEntry getEntry(FormularyEntry fe) {
		return fe == null? null : all[fe.getId()];
	}
	
	public FormularyEntry[] query(String query, FormularyEntryType... types) {
		if (!loaded) { return null; }
		
		query = query.toUpperCase();
		EntryCollecter ec = new EntryCollecter();
		for (int i = 0; i < types.length; i++) {
			long h = CRC64.getCRC64(types[i].name() + DELIM + query);
			TIntHashSet set = index.get(h);
			if (set != null) { set.forEach(ec); }
		}
		return ec.toArray(new FormularyEntry[ec.size()]);
	}
	
	public FormularyEntry[] getNext(FormularyEntryType type, FormularyEntry... fe) {
		if (!loaded) { return null; }
		
		switch (type) {
			case TREATMENT:
				FormularyEntry[] ret = get(names[fe[0].getId()]);
				return ret == null? fe : ret;
			case DOSAGE:
				return get(dosages[fe[0].getId()]);
			case FORM:
				return get(forms.get(hash(fe[0].getId(), fe[1].getId())));
			default:
				return null;
		}
	}
	
	private FormularyEntry[] get(TIntHashSet set) {
		if (set == null) { return null; }
		
		FormularyEntry[] ret = new FormularyEntry[set.size()];
		TIntIterator iter = set.iterator();
		for (int i = 0; iter.hasNext(); i++) {
			ret[i] = all[iter.next()];
		}
		Arrays.sort(ret);
		return ret;
	}
	
	private class EntryCollecter extends TreeSet<FormularyEntry> implements TIntProcedure {
		@Override
		public boolean execute(int value) {
			add(all[value]);
			
			TIntHashSet set = parts[value];
			if (set != null) { set.forEach(this); }
			return true;
		}
	}
}