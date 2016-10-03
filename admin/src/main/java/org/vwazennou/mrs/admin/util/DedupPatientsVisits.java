/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.admin.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.vwazennou.mrs.MRSMergable;
import org.vwazennou.mrs.admin.merge.MergeUtil;
import org.vwazennou.mrs.data.Database;
import org.vwazennou.mrs.data.Option;
import org.vwazennou.mrs.data.ParentOf;
import org.vwazennou.mrs.formulary.Formulary;
import org.vwazennou.mrs.patient.Patient;
import org.vwazennou.mrs.script.Prescription;
import org.vwazennou.mrs.visit.Visit;
import org.vwazennou.mrs.visit.VisitText;

import com.datamininglab.commons.lang.StatusListener.DefaultStatusListener;
import com.datamininglab.commons.lang.StatusMonitor;

import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.hash.TLongObjectHashMap;

public final class DedupPatientsVisits {
	private DedupPatientsVisits() {
		// Prevent initialization
	}
	
	public static void main(String[] args) {
		if (!Option.isDataPathValid()) {
			System.err.println("Error: required data files could not be found. Exiting.");
			return;
		}
		
		StatusMonitor sm = new StatusMonitor("DupPatients").addListener(new DefaultStatusListener());
		sm.newTask("Initializing database...");
		Session s = Database.connect();
		Formulary f = new Formulary(s);
		
		List<MRSMergable> toMerge = new LinkedList<>();
		dedup(sm, s, f, toMerge, VISIT_MERGER);
		dedup(sm, s, f, toMerge, SCRIPT_MERGER);
		
		sm.newTask("Saving changes...");
		Database.disconnect(s);
		
		sm.newTask("Adding merged changes...");
		s = Database.connect();
		for (MRSMergable m : toMerge) {
			ParentOf.recurse(null, m, MergeUtil.RESET_ID);
			s.save(m);
		}
		Database.disconnect(s);
		Database.disconnect();
		sm.setFinished();
	}
	
	@SuppressWarnings("unchecked")
	private static <P extends MRSMergable & ParentOf<C>, C extends MRSMergable> void dedup(StatusMonitor sm, Session s, Formulary f, List<MRSMergable> toMerge, Merger<P, C> merger) {
		List<?> l = s.createCriteria(merger.getParentClass()).list();

		sm.newTask("Finding " + merger.getParentClass().getSimpleName() + " duplicates...", l.size());
		TLongObjectHashMap<Set<P>> dups = new TLongObjectHashMap<>();
		for (int i = 0; i < l.size(); i++) {
			P p1 = (P) l.get(i);
			for (int j = i + 1; j < l.size(); j++) {
				P p2 = (P) l.get(j);
				if (!MergeUtil.equals(merger.getParentClass(), p1, p2)) { continue; }
				
				Set<P> set1 = dups.get(p1.getId());
				if (set1 == null) {
					set1 = new HashSet<>();
					dups.put(p1.getId(), set1);
				}
				Set<P> set2 = dups.put(p2.getId(), set1);
				if (set2 != null) { set1.addAll(set2); }
				set1.add(p1);
				set1.add(p2);
			}
			sm.setProgress(i, false);
		}
		
		sm.newTask("Deleting/merging duplicates...", dups.size());
		
		TLongObjectIterator<Set<P>> iter = dups.iterator();
		while (iter.hasNext()) {
			iter.advance();
			sm.setProgress(1L, true);
			
			Set<P> set = iter.value();
			if (set.isEmpty()) { continue; }
			
			System.out.println("Duplicate set: " + StringUtils.join(set, '|'));
			
			P base = null;
			for (P p : set) {
				if (base == null) { base = p; continue; }

				// Try to pick the most complete visit based on number of scripts,
				// then text entries, whether or not there is a urine test, and finally
				// the lowest (presumably oldest) ID
				int comp = Integer.compare(CollectionUtils.size(p.getChildren()), CollectionUtils.size(base.getChildren()));
				if (p instanceof Visit) {
					Visit v = (Visit) p;
					Visit vBase = (Visit) base;
					if (comp == 0) { comp = Integer.compare(CollectionUtils.size(v.getText()), CollectionUtils.size(vBase.getText())); }
					if (comp == 0) { comp = Integer.compare(v.getUrineTestResult() == null? 0 : 1, vBase.getUrineTestResult() == null? 0 : 1); }
				}
				if (comp == 0) { comp = Long.compare(base.getId(), p.getId()); }
				if (comp > 0) { base = p; }
			}
			base.inflateChildren(f);
			
			for (P p : set) {
				if (p == base) { continue; }
				
				p.inflateChildren(f);
				Iterator<C> childIter = p.getChildren().iterator();
				next: while (childIter.hasNext()) {
					C child = childIter.next();
					for (C baseChild : base.getChildren()) {
						if (MergeUtil.equals(merger.getChildClass(), baseChild, child)) {
							iter.remove();
							continue next;
						}
					}
				}
				
				for (C child : p.getChildren()) {
					System.out.println("Merging " + child + " into " + base);
					merger.setParent(child, base);
					toMerge.add(child);
				}
				p.getChildren().clear();
			
				if (p instanceof Visit) {
					Visit v = (Visit) p;
					Visit vBase = (Visit) base;
					if (v.getUrineTestResult() != null && vBase.getUrineTestResult() == null) {
						vBase.setUrineTestResult(v.getUrineTestResult());
					}
					v.setUrineTestResult(null);
					
					for (VisitText vt : v.getText()) {
						vt.setVisit(vBase);
						toMerge.add(vt);
					}
					v.getText().clear();
				}
				
				s.delete(p);
			}
			set.clear();
		}
	}
	
	private interface Merger<P extends MRSMergable & ParentOf<C>, C extends MRSMergable> {
		void setParent(C child, P parent);
		Class<P> getParentClass();
		Class<C> getChildClass();
	}
	private static final Merger<Patient, Visit> VISIT_MERGER = new Merger<Patient, Visit>() {
		@Override
		public Class<Patient> getParentClass() { return Patient.class; }
		@Override
		public Class<Visit> getChildClass() { return Visit.class; }
		@Override
		public void setParent(Visit child, Patient parent) { child.setPatient(parent); }
	};
	private static final Merger<Visit, Prescription> SCRIPT_MERGER = new Merger<Visit, Prescription>() {
		@Override
		public Class<Visit> getParentClass() { return Visit.class; }
		@Override
		public Class<Prescription> getChildClass() { return Prescription.class; }
		@Override
		public void setParent(Prescription child, Visit parent) { child.setVisit(parent); }
	};
}
