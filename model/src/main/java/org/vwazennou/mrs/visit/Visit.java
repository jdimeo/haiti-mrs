/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.visit;

import static org.vwazennou.mrs.dictionary.Str.contains;
import static org.vwazennou.mrs.dictionary.Str.toBoolean;
import static org.vwazennou.mrs.dictionary.Str.toStr;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.vwazennou.mrs.MRSField;
import org.vwazennou.mrs.MRSField.MRSFieldType;
import org.vwazennou.mrs.MRSMergable;
import org.vwazennou.mrs.data.Client;
import org.vwazennou.mrs.data.ParentOf;
import org.vwazennou.mrs.dictionary.Language;
import org.vwazennou.mrs.dictionary.Str;
import org.vwazennou.mrs.formulary.Formulary;
import org.vwazennou.mrs.patient.Patient;
import org.vwazennou.mrs.script.Prescription;
import org.vwazennou.mrs.visit.VisitText.VisitTextType;

import com.datamininglab.commons.lang.Utilities;


@Entity @Table(name = "visits")
@Inheritance(strategy = InheritanceType.JOINED)
public class Visit implements ParentOf<Prescription>, Comparable<Visit>, MRSMergable {
	private static final DateFormat DF  = new SimpleDateFormat("dd.MM.yyyy");
	public static final long DEFAULT_DATE = Utilities.DATE_1900_1_1; 
	
	public static final Str[] MALARIA_RESULTS = {
		Str.NA, Str.POSITIVE, Str.NEGATIVE
	};
	public static final Str[] STREP_RESULTS = {
		Str.NA, Str.POSITIVE, Str.NEGATIVE
	};
	
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false)
	private long id;
	
	@Version @Column(name = "version")
	private int version;

	@Column(name = "original_id")
	private long originalId = UNMERGED;

	@ManyToOne @JoinColumn(name = "original_client")
	private Client originalClient;
	
	@JoinColumn(name = "patient_id") @ManyToOne(fetch = FetchType.LAZY)
	@MRSField(name = Str.PATIENT, type = MRSFieldType.PARENT)
	private Patient patient;
	
	@Column @MRSField(name = Str.DATE, type = MRSFieldType.DATE, isDefault = true)
	private long date = Utilities.DATE_1900_1_1;
	
	@JoinColumn(name = "clinic_team_id") @ManyToOne
	@MRSField(name = Str.CLINIC_TEAM, type = MRSFieldType.STRING, property = "name")
	private ClinicTeam clinicTeam;
	
	@Column(name = "provider")
	private String provider;
	
	@Column @MRSField(name = Str.SYSTOLIC, type = MRSFieldType.NUMBER, isDefault = true)
	private int systolic;
	
	@Column @MRSField(name = Str.DIASTOLIC, type = MRSFieldType.NUMBER, isDefault = true)
	private int diastolic;
	
	@Column @MRSField(name = Str.PULSE, type = MRSFieldType.NUMBER, isDefault = true)
	private int pulse;
	
	@Column @MRSField(name = Str.BLOOD_GLUCOSE, type = MRSFieldType.NUMBER, isDefault = true)
	private int glucose;
	
	@Column @MRSField(name = Str.HEMOGLOBIN, type = MRSFieldType.NUMBER, isDefault = true)
	private float hemoglobin;
	
	@Column @Enumerated(EnumType.ORDINAL)
	@MRSField(name = Str.MALARIA, type = MRSFieldType.STR)
	private Str malaria = Str.NA;
	
	@Column @Enumerated(EnumType.ORDINAL)
	@MRSField(name = Str.STREP, type = MRSFieldType.STR)
	private Str strep = Str.NA;
	
	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	@JoinColumn(name = "urine_test_id")
	private UrineTest urineTest;
	
	@Column @MRSField(name = Str.RESPIRATION, type = MRSFieldType.NUMBER, isDefault = true)
	private int respiration;
	
	@Column @MRSField(name = Str.TEMPERATURE, type = MRSFieldType.NUMBER, isDefault = true)
	private float temperature;
	
	@Column @MRSField(name = Str.HEIGHT, type = MRSFieldType.NUMBER, isDefault = true)
	private float height;
	
	@Column @MRSField(name = Str.WEIGHT, type = MRSFieldType.NUMBER, isDefault = true)
	private float weight;
	
	@Column @Enumerated(EnumType.ORDINAL)
	@MRSField(name = Str.PREGNANT, type = MRSFieldType.STR)
	private Str pregnant = Str.NO;
	
	@Column @Enumerated(EnumType.ORDINAL)
	@MRSField(name = Str.NURSING, type = MRSFieldType.STR)
	private Str nursing = Str.NO;
	
	@Column @MRSField(name = Str.SYMPTOMS)
	private String symptoms = "";
	
	@Column(name = "followup_this") @Enumerated(EnumType.ORDINAL)
	@MRSField(name = Str.LATER_THIS_WEEK, type = MRSFieldType.STR)
	private Str followUpThis = Str.NO;
	
	@Column(name = "followup_next") @Enumerated(EnumType.ORDINAL)
	@MRSField(name = Str.NEXT_CLINIC, type = MRSFieldType.STR)
	private Str followUpNext = Str.NO;
	
	@Column(name = "followup_lads") @Enumerated(EnumType.ORDINAL)
	@MRSField(name = Str.LADS, type = MRSFieldType.STR)
	private Str followUpLADS = Str.NO;
	
	@Column(name = "followup_referral") @Enumerated(EnumType.ORDINAL)
	@MRSField(name = Str.OUTSIDE_REFERRAL, type = MRSFieldType.STR)
	private Str followUpRefer = Str.NO;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "taking_meds")
	private Str takingMeds = Str.NA;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "understands_ed")
	private Str understandsEd = Str.NA;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "finished_meds")
	private Str finishedMeds = Str.NA;
	
	@OneToMany(mappedBy = "visit", cascade = CascadeType.ALL)
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	private List<VisitText> text = new ArrayList<>();
	
	@OneToMany(mappedBy = "visit", cascade = CascadeType.ALL)
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	@LazyCollection(LazyCollectionOption.EXTRA)
	private List<Prescription> scripts = new ArrayList<>();
	
	public Visit() {
		// Default constructor provided for Hibernate
	}
	public Visit(Patient p) {
		patient = p;
		p.getVisits().add(this);
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
	
	public Patient getPatient()                     { return patient; }
	public void    setPatient(Patient p)            { this.patient = p; }
	
	public void setDate(Date date) {
		this.date = (date == null)? Utilities.DATE_1900_1_1 : date.getTime();
	}
	
	public Date    getDate()                        { return new Date(date); }
	public long    getDateInMillis()                { return date; }
	public String  getFormattedDate()               { return DF.format(getDate()); }
	
	public int     getSystolic()                    { return systolic; }
	public void    setSystolic(int systolic)        { this.systolic = systolic; }
	
	public int     getDiastolic()                   { return diastolic; }
	public void    setDiastolic(int diastolic)      { this.diastolic = diastolic; }
	
	public int     getPulse()                       { return pulse; }
	public void    setPulse(int pulse)              { this.pulse = pulse; }
	
	public int     getBloodGlucose()                { return glucose; }
	public void    setBloodGlucose(int glucose)     { this.glucose = glucose; }
	
	public float   getHemoglobin()                  { return hemoglobin; }
	public void    setHemoglobin(float hgb)         { this.hemoglobin = hgb; }
	
	public Str     getMalariaResult()               { return malaria; }
	public void    setMalariaResult(Str m)          { malaria = contains(MALARIA_RESULTS, m); }
	
	public Str     getStrepResult()                 { return strep; }
	public void    setStrep(Str s)                  { strep = contains(STREP_RESULTS, s); }
	
	public UrineTest getUrineTestResult()           { return urineTest; }
	public void    setUrineTestResult(UrineTest ut) { this.urineTest = ut; }
	
	public int     getRespiration()                 { return respiration; }
	public void    setRespiration(int resp)         { this.respiration = resp; }
	
	public boolean isPregnant()                     { return toBoolean(pregnant); }
	public void    setPregnant(boolean preg)        { this.pregnant = toStr(preg); } 
	
	public boolean isNursing()                      { return toBoolean(nursing); }
	public void    setNursing(boolean nursing)      { this.nursing = toStr(nursing); }
	
	public String  getSymptoms()                    { return symptoms; }
	public void    setSymptoms(String symptoms)     { this.symptoms = symptoms; }
	
	public boolean followUpThis()                   { return toBoolean(followUpThis); }
	public void    setFollowUpThis(boolean fup)     { this.followUpThis = toStr(fup); }
	
	public boolean followUpNext()                   { return toBoolean(followUpNext); }
	public void    setFollowUpNext(boolean fup)     { this.followUpNext = toStr(fup); }
	
	public boolean followUpLADS()                   { return toBoolean(followUpLADS); }
	public void    setFollowUpLADS(boolean fup)     { this.followUpLADS = toStr(fup); }
	
	public boolean followUpReferral()               { return toBoolean(followUpRefer); }
	public void    setFollowUpReferral(boolean fup) { this.followUpRefer = toStr(fup); }
	
	public ClinicTeam getClinicTeam()               { return clinicTeam; }
	public void       setClinicTeam(ClinicTeam ct)  { this.clinicTeam = ct; }
	
	public String  getProvider()                    { return provider; }
	public void    setProvider(String provider)     { this.provider = provider; }
	
	public List<Prescription> getPrescriptions() { return scripts; }
	public List<VisitText>    getText()          { return text; }
	
	public boolean isBlank() {
		return getClinicTeam() == null || getDateInMillis() < Utilities.DATE_1970_1_1;
	}
	
	private VisitText getVisitText(VisitTextType type, Language l, boolean useAnyLang) {
		VisitText ret = null;
		for (VisitText vt : text) {
			if (vt.getType() != type) { continue; }
			
			if (l.equals(vt.getLanguage())) {
				ret = vt;
			} else if (ret == null && useAnyLang) {
				ret = vt;
			}
		}
		return ret;
	}
	
	public VisitText getText(VisitTextType type, Language lang) {
		return getVisitText(type, lang, true);
	}
	public boolean hasText(VisitTextType type, Language lang) {
		return getVisitText(type, lang, false) != null;
	}
	public boolean hasAnyText() {
		return !text.isEmpty();
	}
	
	public void setText(VisitTextType type, Language l, String text) {
		VisitText vt = getVisitText(type, l, false);
		if (vt == null && !StringUtils.isEmpty(text)) {
			this.text.add(new VisitText(this, l, type, text));
		} else if (vt != null && StringUtils.isEmpty(text)) {
			this.text.remove(vt);
		} else if (vt != null) {
			vt.setText(text);
		}
	}
	
	public float getTemperatureC() { return temperature; }
	public float getTemperatureF() { return 1.8f * temperature + 32.0f; }
	public void  setTemperature(float temp) {
		// Convert all temperatures to C
		temperature = (temp > 60.0f)? 5.0f * (temp - 32.0f) / 9.0f : temp;
	}
	
	public float getHeightCm() { return height; }
	public float getHeightFt() { return height * 0.0328083989501f; }
	public void  setHeight(float ht) {
		// Convert all heights to cm
		height = (ht < 10.0f)? ht * 30.48f : ht;
	}
	
	public float getWeightKg() { return weight; }
	public float getWeightLb() { return weight * 2.20462262f; }
	public void  setWeight(float wt) {
		// Convert all weights to kg
		// Since the ranges overlap, use age to guess the units
		float age   = getPatient().getAgeAt(getDateInMillis());
		float maxWt = 135.0f / (float) (1.0 + Math.exp(2.5 - 0.2 * age));
		weight = (wt > maxWt)? wt * 0.45359237f : wt;
	}
	
	public float getBMI() {
		float htm = height * 0.01f;
		return weight / (htm * htm);
	}
	
	public Str isTakingMeds() { return takingMeds; }
	public void setTakingMeds(Str takingMeds) {
		this.takingMeds = takingMeds;
	}
	
	public Str isUnderstandingEducation() { return understandsEd; }
	public void setUnderstandsEducation(Str understandsEd) {
		this.understandsEd = understandsEd;
	}
	
	public Str isOutOfMeds() { return finishedMeds; }
	public void setOutOfMeds(Str finishedMeds) {
		this.finishedMeds = finishedMeds;
	}
	
	@Override
	public Collection<Prescription> getChildren() {
		return getPrescriptions();
	}
	
	@Override
	public void inflateChildren(Formulary f) {
		getPatient().getId();
		getText().size();
		
		for (Prescription p : getPrescriptions()) {
			p.inflateChildren(f);
		}
		if (getUrineTestResult() != null) {
			getUrineTestResult().getPH();
		}
	}
	
	@Override
	public void unpersistChildCollection() {
		scripts = new ArrayList<>(getPrescriptions());
		text = new ArrayList<>(text);
	}
	
	@Override
	public String toString() {
		return getPatient() + " - " + (isBlank()? "Unsaved Visit" : getFormattedDate() + " - " + getClinicTeam());
	}
	
	@Override
	public int compareTo(Visit o) {
		return Long.compare(id, o.id);
	}
}