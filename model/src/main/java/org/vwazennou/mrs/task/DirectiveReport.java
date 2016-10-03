/*
 * Copyright (c) 2015 Elder Research, Inc.
 * All rights reserved.
 */
package org.vwazennou.mrs.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Session;
import org.vwazennou.mrs.data.Database;
import org.vwazennou.mrs.script.Directive;
import org.vwazennou.mrs.script.PrescriptionDirective;
import org.vwazennou.mrs.visit.ClinicTeam;
import org.vwazennou.mrs.visit.Visit;

import com.datamininglab.commons.lang.StatusMonitor;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.hash.TLongHashSet;

public class DirectiveReport {
	private static final float PCT = 100.0f;
	
	public void generate(StatusMonitor sm) {
		Session s = Database.connect();
		List<ClinicTeam> clinicTeams = s.createCriteria(ClinicTeam.class).list();
		List<Directive> directives = s.createCriteria(Directive.class).list();

		Map<ClinicTeam, TIntIntHashMap> countMap = new HashMap<>();
		Map<ClinicTeam, TLongHashSet> visitMap = new HashMap<>();
		for (ClinicTeam ct : clinicTeams) {
			countMap.put(ct, new TIntIntHashMap());
			visitMap.put(ct, new TLongHashSet());	
		}
		
		List<?> l = s.createCriteria(PrescriptionDirective.class).list();
		sm.newTask("Generating directive report...", l.size());
		
		for (Object o : l) {
			PrescriptionDirective pd = (PrescriptionDirective) o;
			
			Visit v = pd.getPrescription().getVisit();
			// Standard scripts
			if (v == null) { continue; }
			
			visitMap.get(v.getClinicTeam()).add(v.getId());
			countMap.get(v.getClinicTeam()).adjustOrPutValue(pd.getDirective().getCode(), 1, 1);
			
			sm.setProgress(1L, true);
		}
		
		System.out.println("Average directives used per 100 patients per clinic");
		for (ClinicTeam ct : visitMap.keySet()) { System.out.print("\t" + ct); }
		System.out.println();
		
		for (Directive d : directives) {
			System.out.print(d.getCode());
			for (Entry<ClinicTeam, TLongHashSet> e : visitMap.entrySet()) {
				float denom = PCT / e.getValue().size();
				if (Float.isNaN(denom)) { denom = 0.0f; }
					
				System.out.format("\t%4.2f", countMap.get(e.getKey()).get(d.getCode()) * denom);
			}
			System.out.println();
		}
		Database.disconnect(s);
	}
}
