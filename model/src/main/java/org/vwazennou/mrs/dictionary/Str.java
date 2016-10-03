/*
 * Copyright (c) 2015 Elder Research, Inc.
 * All rights reserved.
 */
package org.vwazennou.mrs.dictionary;

public enum Str {
	AIDS("AIDS"),
	TYPHOID("Typhoid"),
	STI("Sexually-Transmitted Infection"),
	DIABETES("Diabetes"),
	MALARIA("Malaria"),
	ANEMIA("Anemia"),
	HYPERTENSION("Hypertension"),
	TUBERCULOSIS("Tuberculosis"),
	FILARIASIS("Filariasis"),
	MEDICAL_HISTORY("Medical History"),
	
	APP_TITLE("Haiti Medical Records System"),
	YES("Yes"),
	NO("No"),
	OK("OK"),
	ADD("Add"),
	REMOVE("Remove"),
	CANCEL("Cancel"),
	CLOSE("Return to previous screen"),
	FOR("for"),
	NA("N/A"),
	READY("Ready"),
	UNSUPPORTED_ACTION("Unsupported action"),
	
	FILE("File"),
	EDIT("Edit"),
	TOOLS("Tools"),
	HELP("Help"),
	
	PATIENT("Patient"),
	PATIENTS("Patients"),
	NEW_PATIENT("New Patient"),
	EDIT_PATIENT("Edit Patient"),
	DELETE_PATIENT("Delete Patient"),
	
	VISIT("Visit"),
	VISITS("Visits"),
	SHOW_VISITS("Show Visits"),
	NEW_VISIT("New Visit"),
	EDIT_VISIT("Edit Visit"),
	DELETE_VISIT("Delete Visit"),
	
	PRESCRIPTION("Prescription"),
	PRESCRIPTIONS("Prescriptions"),
	SHOW_PRESCRIPTIONS("Show Prescriptions"),
	NEW_PRESCRIPTION("New Prescription"),
	EDIT_PRESCRIPTION("Edit Prescription"),
	DELETE_PRESCRIPTION("Delete Prescription"),
	
	SEARCH("Search"),
	MATCHES("Matches"),
	FIND("Find"),
	CUSTOM("Custom"),
	INSTANT_RESULTS("Instantly update results"),
	EXCLUDE_DECEASED("Exclude deceased patients"),
	SEARCH_FIELDS("Fields to search"),
	EXPAND_ALL("Expand All"),
	COLLAPSE_ALL("Collapse All"),
	
	LAST_NAME("Last Name"),
	FIRST_NAME("First Name"),
	
	CONTACT_INFORMATION("Contact Information"),
	ADDRESS("Address"),
	COMMUNITY("Community"),
	CITY("City"),
	PHONE("Phone"),
	ALTERNATE_PHONE("Alternate Phone"),
	E_MAIL("E-mail"),
	
	OTHER_INFORMATION("Other Information"),
	DOB("Date of Birth"),
	AGE("Age"),
	BIRTH_YEAR("Birth Year"),
	NO_DOB("Date of birth not provided"),
	DAY("Day"), MONTH("Month"), YEAR("Year"),
	YEARS("Years"),
	MONTHS("Months"),
	GENDER("Gender"),
	MALE("Male"), FEMALE("Female"),
	MARITAL_STATUS("Marital Status"),
	SINGLE("Single"), MARRIED("Married"), WIDOWED("Widowed"), DIVORCED("Divorced"),
	CHILDREN("Children"),
	CHILDREN_AGES("Children's Ages"),
	CHILDREN_AGES_HELP("Separate ages with spaces or commas"),
	CHURCH_MEMBER("Church Member"),
	LADS_PATIENT("LADS Patient"),
	NOTES("Notes"),
	DECEASED("Deceased"),
	MEDICATIONS("Medications"),
	
	VITALS("Vitals"),
	DATE("Date"),
	WEIGHT("Weight"),
	HEIGHT("Height"),
	TEMPERATURE("Temperature"),
	SYSTOLIC("Systolic"),
	DIASTOLIC("Diastolic"),
	BLOOD_PRESSURE("Blood Pressure"),
	PULSE("Pulse"),
	MATERNITY("Maternity"),
	RESPIRATION("Respiration"),
	PREGNANT("Pregnant"),
	NURSING("Nursing"),
	
	LABS("Labs"),
	BLOOD_GLUCOSE("Blood Glucose"),
	HEMOGLOBIN("Hemoglobin"),
	NEGATIVE("Negative"),
	POSITIVE("Positive"),
	STREP("Strep"),
	URINE("Urine"),
	LEUKOCYTES("Leukocytes"),
	TRACE("Trace"),
	SMALL("Small"),
	MODERATE("Moderate"),
	LARGE("Large"),
	NITRITE("Nitrite"),
	UROBILINOGEN("Uribilinogen"),
	NORMAL("Normal"),
	PROTEIN("Protein"),
	PH("pH"),
	BLOOD("Blood"),
	NON_HEMOLIZED("Non-hemolized"),
	HEMOLIZED("Hemolized"),
	
	STANDARD_SCRIPTS("Standard/Pre-Bagged Prescriptions"),
	DIAGNOSIS("Diagnosis"),
	DIAGNOSIS_SPECIFIC("Specific Diagnosis"),
	TREATMENT("Treatment"),
	GENERIC_TREATMENT("Generic Equivalent"),
	QUANTITY("Quantity"),
	DOSAGE("Dosage"),
	FORM("Form"),
	DIRECTIVE("Directive"),
	DIRECTIVES("Directives/Patient Instructions"),
	SELECT_DIRECTIVE("Please select a valid directive number"),
	SELECT_PRESCRIPTION("Please select a prescription"),
	
	QTY_MODIFIER("Quantity Modifier"),
	QTY_WHOLE("whole"),
	QTY_HALF("half of"),
	QTY_QUARTER("quarter of"),
	QTY_PAIR("pair of"),
	QTY_BOT_PACK_TUBE("bottle/pack/tube of"),
	ORIGINALLY("originally"),
	BOTTLE("bottle of"),
	PACKAGE("pack of"),
	TUBE("tube of"),
	
	EAR("ear"),
	EYE("eye"),
	LEFT("left"),
	RIGHT("right"),
	
	ABDOMINAL_PAIN("Abdominal Pain"),
	ACID_REFLUX("Acid Reflux"),
	ALLERGY("Allergy"),
	ASTHMA("Asthma"),
	COLD("Cold"),
	COUGH("Cough"),
	DEPRESSION_ANXIETY("Depression/Anxiety"),
	DIARRHEA("Diarrhea"),
	EPILEPSY_SEIZURES("Epilepsy/Seizures"),
	EYE_CONDITION("Eye Condition"),
	FEVER("Fever"),
	HYGIENE("Hygiene"),
	HEADACHE("Headache"),
	INFECTION("Infection"),
	ITCHING_RASH("Itching/Rash"),
	PAIN("Pain"),
	SKIN_CONDITION("Skin Condition"),
	VAGINITIS("Vaginitis"),
	VITAMINS("Vitamins"),
	WORMS("Worms"),
	OTHER("Other"),
	
	SYMPTOMS("Symptoms/Complaints"),
	FOLLOW_UP_COMMENTS("Follow-up and Comments"),
	FOLLOW_UP("Follow-up with patient"),
	LATER_THIS_WEEK("Later this week"),
	NEXT_CLINIC("Next clinic"),
	LADS("LADS"),
	OUTSIDE_REFERRAL("Outside referral"),
	COMMENTS("Comments"),
	PATIENT_GROUP("Patient Group"),
	CLINIC_TEAM("Clinic Team"),
	
	SET_PATIENT_GROUP("Set Patient Group"),
	PATIENT_GROUP_PROMPT("Which patient group is this clinic serving?"),
	SET_CLINIC_TEAM("Set Clinic Team"),
	CLINIC_TEAM_PROMPT("Which team is running this clinic?"),
	
	ENUM_ADD_NEW("Add new value..."),
	ENUM_SEL_ERROR("You must select an existing value or create a new one"),
	
	EDIT_DICTIONARY("Edit Dictionary"),
	SAVING_DICTIONARY("Saving changes to dictionary..."),
	CATEOGRY("Category"),
	NAME("Name"),
	COUNTRY("Country"),
	ADD_LANGUAGE("Add new language..."),
	ADD_PHRASE("Add Phrase"),
	EDIT_TRANSLATION("Edit Translation"),
	DELETE_PHRASE("Delete Phrase"),
	GOOGLE_TRANSLATE("Use Google Translate"),
	TRANSLATING_WITH_GOOGLE("Translating phrases using Google Translate"),
	
	ADD_RESULTS("Add Results"),
	VIEW_RESULTS("View Results"),
	REMOVE_RESULTS("Remove Results"),
	SAVE("Save"),
	SAVING("Saving"),
	SAVE_ALL("Save All Changes"),
	REENTER_INPUT("Invalid input. Please re-enter:"),
	EXPORT_TO_EXCEL("Export to Microsoft Excel"),
	EXPORT_PT_IDX("Export patient index"),
	SPECIFY_EXPORT_DIRECTORY("Specify export directory:"),
	EXPORTING("Exporting to"),
	LANGUAGE("Language"),
	
	// At this point, the data went "live" so all future Str's need to come
	// after LANGUAGE so that the ordinals don't change
	PREVIOUS_SEARCH("Previous Search"),
	NEXT_SEARCH("Next Search"),
	PATIENT_ID("Patient ID"),
	FULL_NAME("Full Name"),
	DICTIONARY("the dictionary"),
	BOTH("both"),
	EXIT("Exit"), 
	
	PREVIOUS_DIRECTIVE("Edit previous directive"),
	NEXT_DIRECTIVE("Edit next directive"),
	NEW_DIRECTIVE("Add new directive"),
	DELETE_DIRECTIVE("Delete directive"),
	
	EXPORT_MED_REPORT("Export medicine report"),
	EXPORT_DIR_REPORT("Export directive (sticker) report"),
	DATA_TABLE("Tabular Data"),
	TABLE("Table"),
	GRAPH("Graph"),
	REFRESH("Refresh"),
	NEXT_PAGE("Next Page"),
	PREVIOUS_PAGE("Previous Page"),
	
	PATIENT_SUMMARY("Patient Summary"),
	TAGS("Tags"),
	RESULT("Result"),
	PRESCRIBED("Prescribed"),
	CURRENT_MEDICATIONS("Current Medications"),
	ALL_MEDICATIONS("All Medications"),
	VALUE("Value"),
	BMI("BMI"),
	QUERYING("Querying"),
	
	EDUCATION("Education"),
	EDUCATION_GIVEN("Education given to the patient"),
	EDUCATION_UNDERSTOOD("The patient understands the education"),
	COMPLIANCE("Patient Compliance"),
	TAKING_MEDS("The patient is taking their medications correctly"),
	FINISHED_MEDS("The patient has run out of medications"),
	PROVIDER("Provider"),
	LADS_NAME("LADS Name"),
	UNTRANSLATED_TEXT("This text is in another language- it has not been translated to the se"
		+ "lected language. Please translate it if you are able."),
			
	ERROR("Error: "),
	ERROR_TOO_MANY_ROWS("Export will not be complete; exceeded number of rows allowed in Excel worksheet"),
	ERROR_EXPORT_TO_EXCEL("Error exporting to Excel: "),
	ERROR_MESSAGE("Unfortunately there has been an unexpected error in the program.\n"
		+ "Please restart the program and connect to the internet as soon as\n"
		+ "possible so that information about the error can be sent to the \n"
		+ "development team. Sorry for the inconvenience."),
	ERROR_MUST_SELECT("You must select an item before clicking: "),
	ERROR_MUST_ENTER_VAL("You must enter a value before clicking: "),
	ERROR_DIR_NUM("You must select a directive number."),
	ERROR_SAVING("Error saving: "),
	ERROR_FIRST_SEARCH("No previous searches"),
	ERROR_LAST_SEARCH("Current search is the most recent"),
	CONFIRM("Confirm"),
	CONFIRM_PROMPT("Are you sure you want to "),
	CONFIRM_SAVE("Do you want to save your changes?"),
	INVALID_DATA("Warning: you have entered invalid data that will not be saved."),
	SET_DATA_PATH("Set Data Folder"),

	ID("Identifier"),
	EXPORT("Export"),
	ALL_DATA("All data"),
	LADS_REPORT("LADS Report"),
	PATIENT_SORT("Sort patients by"),
	
	ENUM_NEW_VAL_HERE("Enter new value here"),
	
	ABOUT("About");
	
	private String defVal;
	Str(String s) { defVal = s; }
	
	@Override
	public String toString() {
		if (DictionaryEntry.globalDict == null) { return defVal; }
		return DictionaryEntry.globalDict.getPhrase(this);
	}
	
	public String getDefault() { return defVal; }
	
	public static Str contains(Str[] group, Str s) {
		for (int i = 0; i < group.length; i++) {
			if (s == group[i]) { return s; }
		}
		throw new IllegalArgumentException(s + " is not a valid value for this field");
	}
	public static Str toStr(boolean b) {
		return b? Str.YES : Str.NO;
	}
	public static boolean toBoolean(Str str) {
		return str == Str.YES;
	}
}
