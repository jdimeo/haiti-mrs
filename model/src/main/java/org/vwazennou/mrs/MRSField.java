/*
 * Copyright (c) 2015 Elder Research, Inc.
 * All rights reserved.
 */
package org.vwazennou.mrs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.vwazennou.mrs.dictionary.Str;

@Target(ElementType.FIELD) @Retention(RetentionPolicy.RUNTIME)
public @interface MRSField {
	String THIS = "this";
	
	enum MRSFieldType {
		STRING,
		NUMBER,
		DATE,
		/** The dictionary Str enums (holds enumerated fields) */
		STR,
		PARENT
	}
	
	/** The dictionary key whose <tt>toString()</tt> provides the name of the field. */
	Str          name();
	/** The type of field, which indicates to the search interface how to query its values. */
	MRSFieldType type()       default MRSFieldType.STRING;
	/** The name of the property to be searched, or {@link #THIS} if the name of the
	 *  field with which this annotation is associated should be used. */
	String       property()   default THIS;
	/** If this field is type <tt>STR</tt> or <tt>STRING</tt>, it can potentially store many
	 *  values using bitwise OR'ing of the enum's ordinals or a delimiter in the string. If this
	 *  is <tt>true</tt>, the value of this field will be treated not as a single ordinal or
	 *  string, but the bitwise OR of enum ordinals or concatenation of many values. */
	boolean      hasMany()    default false;
	/** If this field is type <tt>STRING</tt> and <tt>hasMany</tt> is <tt>true</tt>, this is
	 *  the delimiter that separates multiple values in the string value. */
	String       delimiter()  default ",";
	/** Whether or not this field is searched by default. */
	boolean      isDefault()  default false;
}