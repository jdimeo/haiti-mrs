/*
 * Copyright (c) 2015 Elder Research, Inc.
 * All rights reserved.
 */
package org.vwazennou.mrs.admin.merge;

import java.util.Collection;

import org.hibernate.Session;
import org.vwazennou.mrs.MRSMergable;
import org.vwazennou.mrs.admin.merge.MergeClients.MergableTable;
import org.vwazennou.mrs.data.Client;
import org.vwazennou.mrs.data.ParentOf;

import com.datamininglab.commons.logging.LogContext;

import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.hash.TLongObjectHashMap;

class MergeData {
	private MergeClient parent;
	private MergableTable table;
	private TLongObjectHashMap<MRSMergable> existingData, newData;
	
	MergeData(MergeClient parent, MergableTable table) {
		this.parent = parent;
		this.table = table;
	}

	MergeData loadAll(Session s) {
		LogContext.info("%s: Loading %s...", parent, table);
		existingData = new TLongObjectHashMap<>();
		newData = new TLongObjectHashMap<>();
		for (Object o : s.createCriteria(table.getTableClass()).list()) {
			MRSMergable m = (MRSMergable) o;
			(m.getOriginalId() == MRSMergable.UNMERGED? newData : existingData).put(m.getId(), m);
		}
		return this;
	}
	
	private TLongObjectHashMap<MRSMergable> getDataFrom(Client originalClient) {
		TLongObjectHashMap<MRSMergable> ret = new TLongObjectHashMap<>();
		addDataFrom(existingData, originalClient, ret);
		addDataFrom(newData, originalClient, ret);
		return ret;
	}
	private static void addDataFrom(TLongObjectHashMap<MRSMergable> map, Client originalClient, TLongObjectHashMap<MRSMergable> list) {
		for (MRSMergable m : map.valueCollection()) {
			if (m.getOriginalClient() == originalClient) { list.put(m.getOriginalId(), m); }
		}		
	}
	
	void checkOldBaseline(MergeData master) {
		TLongObjectHashMap<MRSMergable> masterMap = master.getDataFrom(parent.getClient());
		
		TLongObjectIterator<MRSMergable> iter = newData.iterator();
		while (iter.hasNext()) {
			iter.advance();
			
			MRSMergable inMaster = masterMap.get(iter.key());
			if (inMaster != null) {
				LogContext.info("New %s from client %s already merged; treating as existing not new", iter.value(), parent.getClient());
				iter.value().setOriginalId(iter.key());
				existingData.put(inMaster.getId(), iter.value());
				iter.remove();
			}
		}
	}
	
	void merge(MergeData master) {
		TLongObjectIterator<MRSMergable> iter = existingData.iterator();
		while (iter.hasNext()) {
			iter.advance();
			
			MRSMergable inMaster = master.existingData.get(iter.key());
			if (inMaster == null) {
				LogContext.info("Existing %s from client %s not found in master data; treating as new", iter.value(), parent.getClient());
				newData.put(iter.key(), iter.value());
			} else if (iter.value().getVersion() > inMaster.getVersion()) {
				LogContext.info("%s was edited by client %s", inMaster, parent.getClient());
				MergeUtil.copy(table.getTableClass(), iter.value(), inMaster);
			}
		}
	}
	
	void saveNew(Session s) {
		for (MRSMergable m : getNew()) {
			LogContext.info("%s was new on client %s", m, parent.getClient());
			if (!s.contains(m)) {
				ParentOf.recurse(null, m, MergeUtil.RESET_ID);
				s.save(m);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	<T extends MRSMergable> Collection<T> getNew() { return (Collection<T>) newData.valueCollection(); }
	@SuppressWarnings("unchecked")
	<T extends MRSMergable> Collection<T> getExisting() { return (Collection<T>) existingData.valueCollection(); }
}
