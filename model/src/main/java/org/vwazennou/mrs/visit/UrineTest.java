/*
 * Copyright (c) 2013 Vwazen Nou
 * All rights reserved.
 */
package org.vwazennou.mrs.visit;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.vwazennou.mrs.MRSMergable;
import org.vwazennou.mrs.data.Client;

@Entity
@Table(name = "urine_tests")
public class UrineTest implements MRSMergable {
	// Note: these options aren't externalized because the assumption is that
	// English urine tests are being used, and so users will want to match the
	// words as written regardless of their language.
	public enum Leukocytes {
		NEGATIVE("Negative"),
		TRACE   ("Trace"),
		SMALL   ("Small (+)"),
		MODERATE("Moderate (++)"),
		LARGE   ("Large (+++)");
	
		private String s;
		Leukocytes(String s) { this.s = s; }
		@Override
		public  String toString() { return s; }
	}
	public enum Nitrite {
		NEGATIVE("Negative"),
		POSITIVE("Positive");
		
		private String s;
		Nitrite(String s) { this.s = s; }
		@Override
		public  String toString() { return s; }
	}
	public enum Urobilinogen {
		NORMAL   ("Normal"),
		POINT_TWO("0.2"),
		ONE      ("1"),
		TWO      ("2"),
		FOUR     ("4"),
		EIGHT    ("8");
		
		private String s;
		Urobilinogen(String s) { this.s = s; }
		@Override
		public  String toString() { return s; }
	}
	public enum Protein {
		NEGATIVE        ("Negative"),
		TRACE           ("Trace"),
		THIRTY          ("30 (+)"),
		ONE_HUNDRED     ("100 (++)"),
		THREE_HUNDRED   ("300 (+++)"),
		GTE_TWO_THOUSAND("2000 or more (++++)");
		
		private String s;
		Protein(String s) { this.s = s; }
		@Override
		public  String toString() { return s; }
	}
	public enum PH {
		FIVE            ("5.0"),
		SIX             ("6.0"),
		SIX_POINT_FIVE  ("6.5"),
		SEVEN           ("7.0"),
		SEVEN_POINT_FIVE("7.5"),
		EIGHT           ("8.0"),
		EIGHT_POINT_FIVE("8.5");
		
		private String s;
		PH(String s) { this.s = s; }
		@Override
		public  String toString() { return s; }
	}
	public enum Blood {
		NEGATIVE              ("Negative"),
		NON_HEMOLIZED_TRACE   ("Non-hemolized Trace"),
		NON_HEMOLIZED_MODERATE("Non-hemolized Moderate"),
		HEMOLIZED_TRACE       ("Hemolized Trace"),
		SMALL                 ("Small (+)"),
		MODERATE              ("Moderate (++)"),
		LARGE                 ("Large (+++)");
		
		private String s;
		Blood(String s) { this.s = s; }
		@Override
		public  String toString() { return s; }
	}
	public enum SpecificGravity {
		ONE_THOUSAND            ("1.000"),
		ONE_THOUSAND_FIVE       ("1.005"),
		ONE_THOUSAND_TEN        ("1.010"),
		ONE_THOUSAND_FIFTEEN    ("1.015"),
		ONE_THOUSAND_TWENTY     ("1.020"),
		ONE_THOUSAND_TWENTY_FIVE("1.025"),
		ONE_THOUSAND_THIRTY     ("1.030");
		
		private String s;
		SpecificGravity(String s) { this.s = s; }
		@Override
		public  String toString() { return s; }
	}
	public enum Ketone {
		NEGATIVE   ("Negative"),
		TRACE_5    ("Trace (5)"),
		SMALL_15   ("Small (15)"),
		MODERATE_40("Moderate (40)"),
		LARGE_80   ("Large (80)"),
		LARGE_160  ("Large (160)");
		
		private String s;
		Ketone(String s) { this.s = s; }
		@Override
		public  String toString() { return s; }
	}
	public enum Bilirubin {
		NEGATIVE("Negative"),
		SMALL   ("Small (+)"),
		MODERATE("Moderate (++)"),
		LARGE   ("Large (+++)");
		
		private String s;
		Bilirubin(String s) { this.s = s; }
		@Override
		public  String toString() { return s; }
	}
	public enum Glucose {
		NEGATIVE    ("Negative"),
		TRACE_100   ("1/10 (100)"),
		QUARTER_250 ("1/4 (250)"),
		HALF_500    ("1/2 (500)"),
		ONE_1000    ("1 (1000)"),
		GTE_TWO_2000("2 (2000 or more)");
		
		private String s; 
		Glucose(String s) { this.s = s; }
		@Override
		public  String toString() { return s; }
	}

	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false)
	private long id;
	
	@Version @Column(name = "version")
	private int version;
	
	@Column(name = "original_id")
	private long originalId = UNMERGED;

	@ManyToOne @JoinColumn(name = "original_client")
	private Client originalClient;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "leukocytes")
	private Leukocytes leukocytes = Leukocytes.NEGATIVE;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "nitrite")
	private Nitrite nitrite = Nitrite.NEGATIVE;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "urobilinogen")
	private Urobilinogen urobilinogen = Urobilinogen.NORMAL;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "protein")
	private Protein protein = Protein.NEGATIVE;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "ph")
	private PH ph = PH.FIVE;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "blood")
	private Blood blood = Blood.NEGATIVE;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "specific_gravity")
	private SpecificGravity specificGravity = SpecificGravity.ONE_THOUSAND;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "keytone")
	private Ketone ketone = Ketone.NEGATIVE;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "bilirubin")
	private Bilirubin bilirubin = Bilirubin.NEGATIVE;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "glucose")
	private Glucose glucose = Glucose.NEGATIVE;

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

	public Leukocytes getLeukocytes() { return leukocytes; }
	public void setLeukocytes(Leukocytes leukocytes) {
		if (leukocytes != null) { this.leukocytes = leukocytes; }
	}

	public Nitrite getNitrite() { return nitrite; }
	public void setNitrite(Nitrite nitrite) {
		if (nitrite != null) { this.nitrite = nitrite; }
	}

	public Urobilinogen getUrobilinogen() { return urobilinogen; }
	public void setUrobilinogen(Urobilinogen urobilinogen) {
		if (urobilinogen != null) { this.urobilinogen = urobilinogen; }
	}

	public Protein getProtein() { return protein; }
	public void setProtein(Protein protein) {
		if (protein != null) { this.protein = protein; }
	}

	public PH getPH() { return ph; }
	public void setPH(PH ph) {
		if (ph != null) { this.ph = ph; }
	}

	public Blood getBlood() { return blood; }
	public void setBlood(Blood blood) {
		if (blood != null) { this.blood = blood; }
	}

	public SpecificGravity getSpecificGravity() { return specificGravity; }
	public void setSpecificGravity(SpecificGravity sg) {
		if (sg != null) { this.specificGravity = sg; }
	}

	public Ketone getKetone() { return ketone; }
	public void setKetone(Ketone ketone) {
		if (ketone != null) { this.ketone = ketone; }
	}

	public Bilirubin getBilirubin() { return bilirubin; }
	public void setBilirubin(Bilirubin bilirubin) {
		if (bilirubin != null) { this.bilirubin = bilirubin; }
	}

	public Glucose getGlucose() { return glucose; }
	public void setGlucose(Glucose glucose) {
		if (glucose != null) { this.glucose = glucose; }
	}
	
	@Override
	public String toString() {
		String delim = ", ";
		StringBuilder sb = new StringBuilder();
		if (leukocytes != null) { sb.append(leukocytes.toString()).append(delim); }
		if (nitrite    != null) { sb.append(nitrite.toString()).append(delim); }
		if (protein    != null) { sb.append(protein.toString()).append(delim); }
		if (blood      != null) { sb.append(blood.toString()).append(delim); }
		if (glucose    != null) { sb.append(glucose.toString()); }
		return sb.toString();
	}
}