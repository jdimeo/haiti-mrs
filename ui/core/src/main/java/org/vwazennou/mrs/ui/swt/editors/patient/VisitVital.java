/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.ui.swt.editors.patient;

import java.util.Date;

import org.vwazennou.mrs.dictionary.Str;

import com.datamininglab.foundation.data.field.DataField;
import com.datamininglab.foundation.data.field.DataFields.DateField;
import com.datamininglab.foundation.data.field.DataFields.FloatField;
import com.datamininglab.foundation.data.field.DataFields.StringField;

public class VisitVital {
	private Str    vital;
	private Date   date;
	private String dateStr;
	private float  value;
	private String icon;
	
	public VisitVital(Str vital, String icon, Date date, String dateStr, float value) {
		this.vital   = vital;
		this.icon    = icon;
		this.date    = date;
		this.dateStr = dateStr;
		this.value   = value;
	}
	
	public float getValue() {
		return value;
	}
	public Date getDate() {
		return date;
	}
	public String getIcon() {
		return icon;
	}
	
	@Override
	public String toString() {
		return dateStr + " " + vital + ": " + PatientSummary.FLOAT_RENDERER.render(value);
	}
	
	public static final DataField<VisitVital, Date> DATE = new DateField<VisitVital>(Str.DATE) {
		@Override
		public Date get(VisitVital row) { return row.getDate(); }
	};
	public static final DataField<VisitVital, Float> VALUE = new FloatField<VisitVital>(Str.VALUE) {
		@Override
		public Float get(VisitVital row) { return row.getValue(); }
	};
	public static final DataField<VisitVital, String> ICON = new StringField<VisitVital>("Icon") {
		@Override
		public String get(VisitVital row) { return row.getIcon(); }
	};
}
