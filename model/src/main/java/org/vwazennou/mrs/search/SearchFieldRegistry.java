/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.search;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vwazennou.mrs.MRSField;
import org.vwazennou.mrs.MRSField.MRSFieldType;

public class SearchFieldRegistry {
	private Map<Class<?>, List<SearchField>> searchFields = new HashMap<>();
	private Map<Class<?>, List<SearchField>> parentFields = new HashMap<>();
	
	public SearchFieldRegistry register(Class<?> c) {
		Field[] fields = c.getDeclaredFields();
		List<SearchField> list = new ArrayList<>(fields.length);
		List<SearchField> parent = new ArrayList<>();
		
		for (int i = 0; i < fields.length; i++) {
			MRSField mrsf = fields[i].getAnnotation(MRSField.class);
			if (mrsf != null) {
				fields[i].setAccessible(true);

				List<SearchField> l = mrsf.type() == MRSFieldType.PARENT? parent : list;
				l.add(new SearchField(c, fields[i], mrsf));
			}
		}
		
		searchFields.put(c, list);
		parentFields.put(c, parent);
		return this;
	}
	
	public List<SearchField> getFields(Class<?> c) {
		return searchFields.get(c);
	}
	
	public List<SearchField> getParentFields(Class<?> c) {
		return parentFields.get(c);
	}
}
