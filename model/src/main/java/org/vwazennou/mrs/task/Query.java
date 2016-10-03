/*
 * Copyright (c) 2015 Elder Research, Inc.
 * All rights reserved.
 */
package org.vwazennou.mrs.task;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.vwazennou.mrs.MRSField;
import org.vwazennou.mrs.data.ParentOf;
import org.vwazennou.mrs.dictionary.Str;
import org.vwazennou.mrs.formulary.Formulary;
import org.vwazennou.mrs.search.SearchField;

import com.datamininglab.foundation.ui.StatusMonitor;
import com.datamininglab.foundation.util.Utilities;

import gnu.trove.map.hash.TObjectFloatHashMap;
import gnu.trove.procedure.TObjectFloatProcedure;

public class Query implements TObjectFloatProcedure<Object> {
	private static volatile long searchCount;
	
	public interface QueryCallback {
		Criterion getFilter(Class<?> c);
		Order     getOrder(Class<?> c);
		void      handleResultsFor(Query q);
	}
	
	private long queryId;
	private Session session;
	private Formulary formulary;
	private Object query;
	private Collection<SearchField> fields;
	private QueryCallback callback;
	private Object[] results;
	private float[] scores;
	
	public Query(Session session, Formulary formulary, Object query, Collection<SearchField> fields, QueryCallback callback) {
		this.queryId   = ++searchCount;
		this.session   = session;
		this.formulary = formulary;
		this.query     = query;
		this.fields    = fields;
		this.callback  = callback;
	}
	
	private boolean isStale() { return queryId < searchCount; }
	
	public void perform(StatusMonitor sm) {
		if (isStale()) { return; }
		
		long start = System.currentTimeMillis();
		sm.newTask(Str.QUERYING + " " + query + "...");
		
		TObjectFloatHashMap<Object> map = new TObjectFloatHashMap<>();
		for (SearchField sf : fields) {
			Order     order  = callback.getOrder(sf.getFieldClass());
			Criterion filter = callback.getFilter(sf.getFieldClass());
			
			Criteria crit = session.createCriteria(sf.getFieldClass());
			Criteria base = crit;
			String   prop = sf.getFieldName();
			if (!sf.getChildField().equals(MRSField.THIS)) {
				crit = crit.createCriteria(prop);
				prop = sf.getChildField();
			}
			
			String queryStr = query.toString();
			switch (sf.getType()) {
				case NUMBER:
					Number n;
					try {
						n = NumberUtils.createNumber(queryStr);
					} catch (NumberFormatException | StringIndexOutOfBoundsException ex) {
						break;
					}
					
					if (double.class.equals(sf.getFieldType())) {
						n = n.doubleValue();
					} else if (float.class.equals(sf.getFieldType())) {
						n = n.floatValue();
					} else if (long.class.equals(sf.getFieldType())) {
						n = n.longValue();
					} else if (int.class.equals(sf.getFieldType())) {
						n = n.intValue();
					} else {
						break;
					}
					
					crit.add(Restrictions.eq(prop, n));
					
					base.setMaxResults(10);
					if (filter != null) { base.add(filter); }
					if (order  != null) { base.addOrder(order); }
					
					for (Object o : crit.list()) {
						map.adjustOrPutValue(o, 100.0f, 100.0f);
					}
					break;
				case DATE:
					// TODO: Date searching
					break;
				case STR:
					if (sf.hasMany()) {
						// TODO: "binary flag" search
						break;
					}
					
					queryStr = StringUtils.lowerCase(queryStr);
					Str match = null;
					for (Str str : Str.values()) {
						if (str.toString().toLowerCase().equals(queryStr)) {
							match = str; break;
						}
					}
					if (match == null) { break; }

					crit.add(Restrictions.eq(prop, match));

					crit.setMaxResults(50);
					for (Object o : crit.list()) { map.adjustOrPutValue(o, 100.0f, 100.0f);	 }
					break;
				case PARENT:
					// The object itself is the query
					crit.add(Restrictions.eq(prop, query));
					
					if (filter != null) { base.add(filter); }
					if (order  != null) { base.addOrder(order); }
						
					float score = 0.0f;
					for (Object o : crit.list()) {
						map.adjustOrPutValue(o, score, score--);		
					}
					break;
				case STRING: default:
					String[] parts;
					if (sf.getName() == Str.FULL_NAME) {
						StringBuilder sb = new StringBuilder();
						parts = queryStr.toUpperCase().split("[^A-Z]+");
						for (int i = 0; i < parts.length; i++) {
							sb.append(parts[i]);
						}
						queryStr = sb.toString();
					} else {
						parts = queryStr.split("[\\s]+");
					}
					if (parts.length == 0) { continue; }
					
					int validParts = 0;
					for (String part : parts) {
						if (part.length() > 2) {
							validParts++;
							crit.add(Restrictions.like(prop, part, MatchMode.ANYWHERE));
						}
					}
					if (validParts == 0) { continue; }
					
					if (isStale()) { return; }
				
					base.setMaxResults(50);
					if (filter != null) { base.add(filter); }
					if (order  != null) { base.addOrder(order); }
					
					for (Object o : crit.list()) {
						if (isStale()) { break; }
						
						Object val = sf.getValue(o);
						if (val == null) { continue; }
						
						String valStr = val.toString();
						score = valStr.length() - Utilities.getCharacterDifference(valStr, queryStr, false);
						
						// Increment the score by the ID so it acts as a tie breaker
						score -= ((Number) session.getIdentifier(o)).floatValue() * 0.0001f;
						
						map.adjustOrPutValue(o, score, score);
					}
					
					break;
			}
			if (isStale()) { return; }
		}
		
		this.results = new Object[map.size()];
		this.scores = new float[map.size()];
		Arrays.fill(scores, Float.POSITIVE_INFINITY);
		
		map.forEachEntry(this);
		
		if (isStale()) { return; }

		System.out.println("Query took " + (System.currentTimeMillis() - start) + " ms");
		callback.handleResultsFor(this);
	}
	
	@Override
	public boolean execute(Object o, float score) {
		if (isStale()) { return false; }
		
		// Do some necessary proxy initialization while still attached to a session
		((ParentOf<?>) o).inflateChildren(formulary);
		
		score *= -1.0f;
		int index = Arrays.binarySearch(scores, score);
		if (index < 0) { index = -index - 1; }
		
		int len = scores.length - index - 1;
		if (len > 0) {
			System.arraycopy(scores,  index, scores,  index + 1, len);	
			System.arraycopy(results, index, results, index + 1, len);
		}
		scores[index]  = score;
		results[index] = o;
		return true;
	}
	
	public Object[] getResults() {
		return results;
	}
}
