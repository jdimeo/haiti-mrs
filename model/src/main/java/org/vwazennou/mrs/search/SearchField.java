/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.search;

import java.lang.reflect.Field;

import org.vwazennou.mrs.MRSField;
import org.vwazennou.mrs.MRSField.MRSFieldType;
import org.vwazennou.mrs.dictionary.Str;

import com.datamininglab.foundation.util.ReflectionUtils;

public final class SearchField {
	private Class<?> c;
	private Field    f;
	private MRSField mrsf;
	
	SearchField(Class<?> c, Field f, MRSField mrsf) {
		this.c = c; this.f = f; this.mrsf = mrsf;
	}
	
	public MRSFieldType getType()       { return mrsf.type(); }
	public Str          getName()       { return mrsf.name(); }
	public boolean      isDefault()     { return mrsf.isDefault(); }
	public boolean      hasMany()       { return mrsf.hasMany(); }
	public String       getFieldName()  { return f.getName(); }
	public Class<?>     getFieldType()  { return f.getType(); }
	public Class<?>     getFieldClass() { return c; }
	public String       getChildField() { return mrsf.property(); }
	
	public Object getValue(Object o) {
		return ReflectionUtils.get(f, o);
	}
	public void setValue(Object o, Object val) {
		ReflectionUtils.set(f, o, val);
	}
}
