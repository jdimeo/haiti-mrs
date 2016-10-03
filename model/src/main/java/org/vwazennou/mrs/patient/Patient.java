/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.patient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.annotations.Cascade;
import org.vwazennou.mrs.MRSField;
import org.vwazennou.mrs.MRSField.MRSFieldType;
import org.vwazennou.mrs.MRSMergable;
import org.vwazennou.mrs.data.Client;
import org.vwazennou.mrs.data.ParentOf;
import org.vwazennou.mrs.dictionary.Str;
import org.vwazennou.mrs.formulary.Formulary;
import org.vwazennou.mrs.visit.Visit;

import com.datamininglab.commons.lang.Utilities;

@Entity
@Table(name = "patients")
public class Patient implements ParentOf<Visit>, Comparable<Patient>, MRSMergable {
	public static final long DEFAULT_DOB = Utilities.DATE_1900_1_1;
	
	public static final Str[] GENDERS = {
		Str.MALE, Str.FEMALE
	};
	public static final Str[] MARITAL_STATUSES = {
		Str.SINGLE, Str.MARRIED, Str.WIDOWED, Str.DIVORCED
	};
	public static final Str[] MEDICAL_CONDITIONS = {
		Str.AIDS,
		Str.TYPHOID,
		Str.STI,
		Str.DIABETES,
		Str.MALARIA,
		Str.ANEMIA,
		Str.HYPERTENSION,
		Str.TUBERCULOSIS,
		Str.FILARIASIS,
	};
	
	private static final double   MS_PER_YEAR = 3.155692E10;
	private static final long     ONE_MASK    = 0xFFFFFFFFFFFFFFFFL;
	private static final Calendar CALENDAR    = Calendar.getInstance();

	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(nullable = false)
	@MRSField(name = Str.PATIENT_ID, type = MRSFieldType.NUMBER, isDefault = true)
	private long id;
	
	@Version @Column(name = "version")
	private int version;
	
	@Column(name = "original_id")
	private long originalId = UNMERGED;

	@ManyToOne @JoinColumn(name = "original_client")
	private Client originalClient;
	
	@Column(name = "last_name")
	@MRSField(name = Str.LAST_NAME)
	private String lastName = "";
	
	@Column(name = "first_name")
	@MRSField(name = Str.FIRST_NAME)
	private String firstName = "";
	
	@Column(name = "full_name")
	@MRSField(name = Str.FULL_NAME, isDefault = true)
	private String fullName = "";
	
	@Column(name = "address1")
	@MRSField(name = Str.ADDRESS, isDefault = true)
	private String address = "";
	
	@Column(name = "address2")
	@MRSField(name = Str.COMMUNITY, isDefault = true)
	private String community = "";
	
	@Column(name = "city")
	@MRSField(name = Str.CITY)
	private String city = "";
	
	@Column(name = "phone1")
	@MRSField(name = Str.PHONE, isDefault = true)
	private String phone = "";
	
	@Column(name = "phone2")
	@MRSField(name = Str.ALTERNATE_PHONE, isDefault = true)
	private String altPhone = "";
	
	@Column(name = "email")
	@MRSField(name = Str.E_MAIL)
	private String email = "";
	
	@Column(name = "gender") @Enumerated(EnumType.ORDINAL)
	@MRSField(name = Str.GENDER, type = MRSFieldType.STR)
	private Str gender = GENDERS[0];
	
	@Column(name = "marital_status") @Enumerated(EnumType.ORDINAL)
	@MRSField(name = Str.MARITAL_STATUS, type = MRSFieldType.STR)
	private Str maritalStatus = MARITAL_STATUSES[0];
	
	@Column(name = "dob")
	@MRSField(name = Str.DOB, type = MRSFieldType.DATE)
	private long birthDate = DEFAULT_DOB;
	
	@Column(name = "is_deceased") @Enumerated(EnumType.ORDINAL)
	@MRSField(name = Str.DECEASED, type = MRSFieldType.STR)
	private Str isDeceased = Str.NO;
	
	@Column(name = "is_church_member") @Enumerated(EnumType.ORDINAL)
	@MRSField(name = Str.CHURCH_MEMBER, type = MRSFieldType.STR)
	private Str isChurchMember = Str.NO;
	
	@Column(name = "is_followed_by_lads") @Enumerated(EnumType.ORDINAL)
	@MRSField(name = Str.LADS_PATIENT, type = MRSFieldType.STR)
	private Str isFollowedByLADS = Str.NO;
	
	@Column(name = "children")
	@MRSField(name = Str.CHILDREN, type = MRSFieldType.NUMBER)
	private int children;
	
	@Column(name = "children_dob")
	@MRSField(name = Str.CHILDREN_AGES, hasMany = true, delimiter = "|")
	private String childrenDOB = "";
	
	@Column(name = "condition_flags")
	@MRSField(name = Str.MEDICAL_HISTORY, type = MRSFieldType.STR, hasMany = true)
	private long conditions;
	
	@Column(name = "medications")
	@MRSField(name = Str.MEDICATIONS)
	private String medications = "";
	
	@Column(name = "notes")
	@MRSField(name = Str.NOTES, isDefault = true)
	private String notes = "";
	
	@ManyToOne @JoinColumn(name = "patient_group_id")
	@MRSField(name = Str.PATIENT_GROUP, type = MRSFieldType.STRING, property = "name")
	private PatientGroup patientGroup;
	
	@OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	private List<Visit> visits = new ArrayList<>();

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
	
	public String  getLastName()                     { return lastName; }
	public String  getFirstName()                    { return firstName; }
	public String  getFullName()                     { return fullName; }
	
	public String  getAddress()                      { return address; }
	public void    setAddress(String address)        { this.address = address; }
	
	public String  getCommunity()                    { return community; }
	public void    setCommunity(String community)    { this.community = community; }
	
	public String  getCity()                         { return city; }
	public void    setCity(String city)              { this.city = city; }
	
	public String  getPhone()                        { return phone; }
	public void    setPhone(String phone)            { this.phone = phone; }
	
	public String  getAlternatePhone()               { return altPhone; }
	public void    setAlternatePhone(String phone)   { this.altPhone = phone; }
	
	public String  getEmail()                        { return email; }
	public void    setEmail(String email)            { this.email = email; }
	
	public Str     getGender()                       { return gender; }
	public void    setGender(Str gender)             { this.gender = Str.contains(GENDERS, gender); }
	
	public Str     getMaritalStatus()                { return maritalStatus; }
	public void    setMaritalStatus(Str ms)          { maritalStatus = Str.contains(MARITAL_STATUSES, ms); }
	
	public Date    getBirthdate()                    { return new Date(birthDate); }
	public long    getBirthdateInMillis()            { return birthDate; }
	public void    setBirthdate(Date dob)            { this.birthDate = dob.getTime(); }
	
	public boolean isDeceased()                      { return Str.toBoolean(isDeceased); }
	public void    setDeceased(boolean deceased)     { this.isDeceased = Str.toStr(deceased); }
	
	public boolean isChurchMember()                  { return Str.toBoolean(isChurchMember); }
	public void    setChurchMember(boolean member)   { this.isChurchMember = Str.toStr(member); }
	
	public boolean isFollowedByLADS()                { return Str.toBoolean(isFollowedByLADS); }
	public void    setFollowedByLADS(boolean lads)   { this.isFollowedByLADS = Str.toStr(lads); }
	
	public int     getNumberOfChildren()             { return children; }
	public void    setNumberOfChildren(int children) { this.children = children; }
	
	public String  getMedications()                  { return medications; }
	public void    setMedications(String meds)       { this.medications = meds; }
	
	public String  getNotes()                        { return notes; }
	public void    setNotes(String notes)            { this.notes = notes; }
	
	public PatientGroup getPatientGroup()            { return patientGroup; }
	public void    setPatientGroup(PatientGroup pg)  { this.patientGroup = pg; }
	
	public List<Visit> getVisits() { return visits; }
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public void updateFullName() {
		StringBuilder sb = new StringBuilder(firstName.length() + lastName.length() + 1);
		sb.append(firstName.toUpperCase()).append(' ').append(lastName.toUpperCase());
		
		String[] parts = sb.toString().split("[^A-Z]+");
		Arrays.sort(parts);
		
		sb = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			sb.append(parts[i]);
		}
		fullName = sb.toString();
	}
	
	public float getAge() {
		return getAgeAt(System.currentTimeMillis());
	}
	public float getAgeAt(long date) {
		if (birthDate == DEFAULT_DOB) { return Float.NaN; }
		
		long diff = date - birthDate;
		return (float) (diff / MS_PER_YEAR);
	}

	public void setAge(float age) {
		long ms = Math.round(age * MS_PER_YEAR);
		birthDate = System.currentTimeMillis() - ms;
	}
	public void setBirthYear(int year) {
		CALENDAR.setTimeInMillis(System.currentTimeMillis());
		CALENDAR.set(Calendar.YEAR, year);
		birthDate = CALENDAR.getTimeInMillis();
	}

	public String getChildAges() {
		return convertAgesYears(childrenDOB);
	}
	public void setChildAges(String childAges) {
		childrenDOB = convertAgesYears(childAges);
	}
	private static String convertAgesYears(String s) {
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		
		StringBuilder sb = new StringBuilder();
		String[] arr = s.split("[^.0-9]+");
		for (int i = 0; i < arr.length; i++) {
			float age = NumberUtils.toFloat(arr[i], Float.NaN);
			if (Float.isNaN(age)) { continue; }
			
			if (sb.length() > 0) { sb.append(", "); }
			sb.append(year - Math.round(age));
		}
		return sb.toString();
	}

	public boolean hasCondition(Str condition) {
		return Utilities.isOn(conditions, Str.contains(MEDICAL_CONDITIONS, condition));
	}
	public void setCondition(Str condition, boolean hasCondition) {
		long flag = 1L << Str.contains(MEDICAL_CONDITIONS, condition).ordinal();
		if (hasCondition) {
			conditions = conditions | flag;
		} else {
			conditions = conditions & (flag ^ ONE_MASK);
		}
	}
	
	@Override
	public Collection<Visit> getChildren() {
		return getVisits();
	}
	
	@Override
	public void inflateChildren(Formulary f) {
		for (Visit v : getVisits()) { v.getId(); }
	}
	
	@Override
	public void unpersistChildCollection() {
		visits = new ArrayList<>(getVisits());
	}

	@Override
	public String toString() {
		if (id < 1L) { return "Unsaved Patient"; }
		String ret = "#" + id;
		if (lastName.isEmpty() && firstName.isEmpty()) { return ret; }
		return ret + " " + lastName + ", " + firstName;
	}
	
	@Override
	public int compareTo(Patient o) {
		return Long.compare(id, o.id);
	}
}