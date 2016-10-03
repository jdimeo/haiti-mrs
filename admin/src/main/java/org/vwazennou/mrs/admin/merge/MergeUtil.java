/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.admin.merge;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;
import org.vwazennou.mrs.MRSMergable;
import org.vwazennou.mrs.data.Client;
import org.vwazennou.mrs.data.ParentOf;
import org.vwazennou.mrs.dictionary.Str;

import com.datamininglab.commons.lang.ReflectionUtils;
import com.datamininglab.commons.logging.LogContext;

import gnu.trove.procedure.TObjectProcedure;

public final class MergeUtil {
	private static Map<Class<?>, Field>   idFields = new HashMap<>();
	private static Map<Class<?>, Field[]> fieldMap = new HashMap<>();
	
	private MergeUtil() {
		// Prevent initialization
	}
	
	private static Field[] getFields(Class<?> c) {
		Field[] fields = fieldMap.get(c);
		if (fields == null) {
			List<Field> l = new ArrayList<>();
			for (Field f : ReflectionUtils.getFields(c)) {
				if (!Modifier.isStatic(f.getModifiers())) { l.add(f); }
				if ("id".equals(f.getName())) { idFields.put(c, f); }
			}
			fields = l.toArray(new Field[l.size()]);
			fieldMap.put(c, fields);
		}
		return fields;
	}
	
	private static Field getIDField(Object o) {
		Field f = idFields.get(o.getClass());
		if (f == null) {
			f = ReflectionUtils.getField(o.getClass(), "id");
			idFields.put(o.getClass(), f);
		}
		return f;
	}
	
	public static final TObjectProcedure<Object> RESET_ID = new TObjectProcedure<Object>() {
		@Override
		public boolean execute(Object obj) {
			if (obj instanceof MRSMergable) {
				MRSMergable m = (MRSMergable) obj;
				if (m.getOriginalClient() == null) {
					m.setOriginalClient(Client.UNKNOWN);
				}
				m.setOriginalId(m.getId());
			}
			if (obj instanceof ParentOf<?>) {
				ParentOf<?> po = (ParentOf<?>) obj;
				po.unpersistChildCollection();
			}
			
			ReflectionUtils.set(getIDField(obj), obj, 0);
			return true;
		}
	};
	
	public static void copy(Class<?> c, Object from, Object to) {
		if (from == null || to == null) { return; }
		
		Field[] fields = getFields(c);
		for (Field f : fields) {
			if (ArrayUtils.contains(SKIP_FIELDS, f.getName())) { continue; }
			
			Class<?> t = f.getType();
			if (t.isPrimitive() || t.equals(String.class) || t.equals(Str.class)) {
				Object newVal = ReflectionUtils.get(f, from);
				Object oldVal = ReflectionUtils.get(f, to);
				
				if (!Objects.equals(newVal, oldVal)) {
					LogContext.info("%30s: %30s > %30s", f.getName(), oldVal, newVal);
					ReflectionUtils.set(f, to, newVal);
				}
			}
		}
	}
	
	public static boolean equals(Class<?> c, Object o1, Object o2) {
		if (o1 == null || o2 == null) { return false; }
		
		Field[] fields = getFields(c);
		for (Field f : fields) {
			if (ArrayUtils.contains(SKIP_FIELDS, f.getName())) { continue; }
			if (Collection.class.isAssignableFrom(f.getType())) { continue; }
			
			Object val1 = ReflectionUtils.get(f, o1);
			Object val2 = ReflectionUtils.get(f, o2);
			if (val1 == null && val2 != null) { return false; }
			if (val1 != null && !val1.equals(val2)) { return false; }
		}
		return true;
	}
	
	private static final String[] SKIP_FIELDS = {"id", "version", "originalClient", "originalId"};
}
