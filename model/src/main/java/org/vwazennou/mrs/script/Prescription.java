/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang3.text.WordUtils;
import org.hibernate.annotations.Cascade;
import org.vwazennou.mrs.MRSField;
import org.vwazennou.mrs.MRSField.MRSFieldType;
import org.vwazennou.mrs.MRSMergable;
import org.vwazennou.mrs.data.Client;
import org.vwazennou.mrs.data.ParentOf;
import org.vwazennou.mrs.dictionary.Str;
import org.vwazennou.mrs.formulary.Formulary;
import org.vwazennou.mrs.formulary.FormularyEntry;
import org.vwazennou.mrs.visit.Visit;

import com.datamininglab.foundation.text.UnicodeChars;
import com.datamininglab.foundation.util.Utilities;

@Entity
@Table(name = "prescriptions")
public class Prescription implements ParentOf<PrescriptionDirective>, Comparable<Prescription>, MRSMergable {
	public static final Str[] QUANTITY_MODIFIERS = {
		Str.QTY_WHOLE, Str.QTY_HALF, Str.QTY_QUARTER, Str.QTY_BOT_PACK_TUBE
	};
	
	@Id @GeneratedValue(strategy = GenerationType.AUTO) 
	@Column(nullable = false)
	private long id;
	
	@Version @Column(name = "version")
	private int version;
	
	@Column(name = "original_id")
	private long originalId = UNMERGED;

	@ManyToOne @JoinColumn(name = "original_client")
	private Client originalClient;
	
	@JoinColumn(name = "visit_id") @ManyToOne(fetch = FetchType.LAZY)
	@MRSField(name = Str.VISIT, type = MRSFieldType.PARENT)
	private Visit visit;

	@Column(name = "diagnosis_id") @Enumerated(EnumType.ORDINAL)
	@MRSField(name = Str.DIAGNOSIS, type = MRSFieldType.STR)
	private Str diagnosisCode = Str.NA;
	
	@Column @MRSField(name = Str.DIAGNOSIS_SPECIFIC, isDefault = true)
	private String diagnosis = "";
	
	@JoinColumn(name = "treatment_id") @ManyToOne
	@MRSField(name = Str.TREATMENT, type = MRSFieldType.STRING, property = "name", isDefault = true)
	private FormularyEntry treatment;
	
	@JoinColumn(name = "dosage_id") @ManyToOne
	@MRSField(name = Str.DOSAGE, type = MRSFieldType.STRING, property = "name", isDefault = true)
	private FormularyEntry dosage;
	
	@JoinColumn(name = "form_id") @ManyToOne
	@MRSField(name = Str.FORM, type = MRSFieldType.STRING, property = "name", isDefault = true)
	private FormularyEntry form;
	
	@Column @MRSField(name = Str.QUANTITY, type = MRSFieldType.NUMBER, isDefault = true)
	private Float quantity;
	
	@Column(name = "qty_mod_id") @Enumerated(EnumType.ORDINAL)
	@MRSField(name = Str.QTY_MODIFIER, type = MRSFieldType.STR)
	private Str qtyMod = Str.QTY_WHOLE;
	
	@OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
	@Cascade(org.hibernate.annotations.CascadeType.ALL) @OrderBy("directiveSeq")
	private List<PrescriptionDirective> directives = new ArrayList<>();
	
	public Prescription() {
		// Default constructor provided for Hibernate
	}
	public Prescription(Visit v) {
		visit = v;
		v.getPrescriptions().add(this);
	}

	@Override
	public long getId() { return id; }
	@Override
	public Client getOriginalClient() { return originalClient; }
	@Override
	public long getOriginalId() { return originalId; }
	@Override
	public void setOriginalClient(Client c) { originalClient = c; }
	@Override
	public void setOriginalId(long id) { originalId = id; }
	@Override
	public int getVersion() { return version; }
	
	public Visit          getVisit()            { return visit;     }
	public float          getQuantity()         { return quantity == null? Float.NaN : quantity; }
	public Str            getQuantityModifier() { return qtyMod;    }
	public FormularyEntry getTreatment()        { return treatment; }
	public FormularyEntry getDosage()           { return dosage;    }
	public FormularyEntry getForm()             { return form;      }
	
	public List<PrescriptionDirective> getDirectives() { return directives; }
	
	public void setVisit(Visit visit)           { this.visit         = visit;    }
	public void setQuantity(Float quantity)     { this.quantity      = quantity; }
	public void setQuantityModifier(Str qtyMod) { this.qtyMod        = qtyMod;   }
	public void setTreatment(FormularyEntry fe) { this.treatment     = fe;       }
	public void setDosage(FormularyEntry fe)    { this.dosage        = fe;       }
	public void setForm(FormularyEntry fe)      { this.form          = fe;       }
	
	private float getMultiplier() {
		switch (qtyMod) {
			case QTY_HALF:    return 0.50f;
			case QTY_QUARTER: return 0.25f;
			case QTY_PAIR:    return 2.00f;
			default:          return 1.00f;
		}
	}
	
	public String getModifiedDosage() {
		if (dosage == null) { return Str.NA.toString(); }
		float m = getMultiplier();
		return m == 1.0f? dosage.toString() : dosage.adjustFirstNumber(m);
	}
	
	public float getModifiedQuantity() {
		return getQuantity() * getMultiplier();
	}
	
	public String getDiagnosis() {
		if (diagnosis != null) { return diagnosis; }
		return diagnosisCode == Str.NA? null : diagnosisCode.toString();
	}
	public Str getDiagnosisCode() {
		return diagnosisCode;
	}
	
	public void setDiagnosis(String dx) {
		setDiagnosis(null, dx);
	}
	public void setDiagnosis(Str code, String dx) {
		Str code2 = findDiagnosisCode(dx);
		if (code2 != Str.NA) {
			diagnosis = null;
			diagnosisCode = (code == null)? code2 : code;
		} else {
			diagnosis = dx;
			diagnosisCode = (code == null)? Str.NA : code;
		}
	}
	
	private static Str findDiagnosisCode(String code) {
		code = code.toUpperCase().replaceAll("[^A-Z0-9]+", "_");
		
		try {
			return Str.valueOf(code);
		} catch (IllegalArgumentException ex) {
			if ("ALLERGIES".equals(code)) {
				return Str.ALLERGY;
			}
			if ("NEED_VITAMINS".equals(code) || "NEEDS_VITAMINS".equals(code)) {
				return Str.VITAMINS;
			}
			if ("VAGINAL_INFECTION".equals(code)) {
				return Str.VAGINITIS;
			}
			
			for (int i = 0; i < DIAGNOSES.length; i++) {
				DiagnosisCode dc = DIAGNOSES[i];
				String dx = dc.getDiagnosis().toString().toUpperCase();
				if (code.equals(dc.getCode()) || dx.contains(code)) {
					return DIAGNOSES[i].getDiagnosis();
				}
			}
		}
		return Str.NA;
	}
	
	private String getModStr(String frmStr) {
		if (qtyMod == Str.QTY_BOT_PACK_TUBE) {
			for (int i = 0; i < BOTTLE_KEYWORDS.length; i++) {
				if (frmStr.contains(BOTTLE_KEYWORDS[i])) { return Str.BOTTLE.toString(); }
			}
			for (int i = 0; i < TUBE_KEYWORDS.length; i++) {
				if (frmStr.contains(TUBE_KEYWORDS[i])) { return Str.TUBE.toString(); }
			}
			return Str.PACKAGE.toString();
		} else if (qtyMod != null && qtyMod != Str.QTY_WHOLE) {
			return qtyMod.toString();
		} else {
			return "";
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (!Float.isNaN(getQuantity()) && getQuantity() > 0.0f) {
			sb.append(Utilities.stringValue(getQuantity(), 2))
			  .append(' ').append(UnicodeChars.MULTIPLICATION_SIGN).append(' ');
		}

		String frmStr = (form      == null)? "" : " " + form.toString().toLowerCase();
		String trtStr = (treatment == null)? "" : WordUtils.capitalize(" " + treatment);
		String modStr = " " + getModStr(frmStr);
		sb.append(getModifiedDosage()).append(modStr).append(trtStr).append(frmStr);
		
		if (getMultiplier() != 1.0f) {
			sb.append(" (").append(Str.ORIGINALLY).append(' ');
			float modQty = getModifiedQuantity();
			if (!Float.isNaN(modQty)) {
				sb.append(Utilities.stringValue(modQty, 2))
				  .append(' ').append(UnicodeChars.MULTIPLICATION_SIGN).append(' ');
			}
			sb.append(dosage).append(frmStr).append(')');
		}

		String dx = getDiagnosis();
		if (dx != null) {
			sb.append(' ').append(Str.FOR).append(' ').append(dx);
		}
		return sb.toString();
	}
	
	public PrescriptionDirective addDirective(Directive d) {
		PrescriptionDirective pd = new PrescriptionDirective(this, d);
		pd.setDirectiveSequence(directives.size());
		return pd;
	}
	
	@Override
	public Collection<PrescriptionDirective> getChildren() {
		return getDirectives();
	}
	
	@Override
	public void unpersistChildCollection() {
		directives = new ArrayList<>(getDirectives());
	}
	
	/**
	 * This inflates a prescription that may have proxy fields by replacing all formulary entry
	 * instances with the instance that is already loaded into memory, loading the directives, and
	 * inflating the visit and patient.
	 */
	@Override
	public void inflateChildren(Formulary f) {
		if (getVisit() != null) { getVisit().getPatient().getId(); }
		
		for (PrescriptionDirective pd : getDirectives()) {
			pd.inflateChildren(f);
		}
		
		if (f != null) {
			setTreatment(f.getEntry(getTreatment()));
			setDosage(f.getEntry(getDosage()));
			setForm(f.getEntry(getForm()));
		}
	}
	
	// TODO: load from file/DB?
	public static final class DiagnosisCode {
		private String code;
		private Str dx;
		private DiagnosisCode(String c, Str d) { code = c; dx = d; }
		
		public String getCode()      { return code; }
		public Str    getDiagnosis() { return dx;   }
		
		@Override
		public String toString() { return "[" + code + "] " + dx.toString(); }
	}
	private static final DiagnosisCode[] DIAGNOSES = {
		new DiagnosisCode("ABD",  Str.ABDOMINAL_PAIN),
		new DiagnosisCode("GERD", Str.ACID_REFLUX),
		new DiagnosisCode("ALL",  Str.ALLERGY),
		new DiagnosisCode("ANEM", Str.ANEMIA),
		new DiagnosisCode("AZM",  Str.ASTHMA),
		new DiagnosisCode("URI",  Str.COLD),
		new DiagnosisCode("TUS",  Str.COUGH),
		new DiagnosisCode("PSY",  Str.DEPRESSION_ANXIETY),
		new DiagnosisCode("DM",   Str.DIABETES),
		new DiagnosisCode("DRH",  Str.DIARRHEA),
		new DiagnosisCode("SZ",   Str.EPILEPSY_SEIZURES),
		new DiagnosisCode("EYE",  Str.EYE_CONDITION),
		
		new DiagnosisCode("F",    Str.FEVER),
		new DiagnosisCode("HA",   Str.HEADACHE),
		new DiagnosisCode("HTN",  Str.HYPERTENSION),
		new DiagnosisCode("IFX",  Str.INFECTION),
		new DiagnosisCode("ITCH", Str.ITCHING_RASH),
		new DiagnosisCode("MAL",  Str.MALARIA),
		new DiagnosisCode("P",    Str.PAIN),
		new DiagnosisCode("SK",   Str.SKIN_CONDITION),
		new DiagnosisCode("VAG",  Str.VAGINITIS),
		new DiagnosisCode("VIT",  Str.VITAMINS),
		new DiagnosisCode("VE",   Str.WORMS),
	};
	
	/**
	 * Returns the set of standard/translatable diagnoses and their corresponding codes.
	 * @return the standard diagnoses
	 */
	public static DiagnosisCode[] getDiagnosisCodes() { return DIAGNOSES; }
	
	private static final String[] BOTTLE_KEYWORDS = {
		"suspension", "solution", "drops", "liquid", "shampoo", "syrup", "spray", "aerosol", "vial"
	};
	private static final String[] TUBE_KEYWORDS = {
		"lotion", "cream", "ointment", "gel"
	};
	
	@Override
	public int compareTo(Prescription o) {
		return Long.compare(id, o.id);
	}
}