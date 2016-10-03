--This logic needs to be run on each table imported from the xml files

CREATE TABLE patients_correct_id (global_id INTEGER, id_base INTEGER, id_t1 INTEGER, id_t2);

.separator ","

.import patients_correct_id.csv patients_correct_id 

CREATE TABLE patients_new (num_visits INTEGER, patient_group_id INTEGER, 
children_dob TEXT, children INTEGER, medications TEXT, condition_flags INTEGER, 
notes TEXT, is_followed_by_lads INTEGER, is_church_member INTEGER, 
is_deceased INTEGER, dob INTEGER, marital_status INTEGER, gender INTEGER, 
id INTEGER PRIMARY KEY, last_name TEXT, first_name TEXT, address1 TEXT, 
address2 TEXT, city TEXT, phone1 TEXT, phone2 TEXT, email TEXT);

CREATE TABLE visits_new (num_prescriptions INTEGER, symptoms TEXT, strep INTEGER, 
urine_test_id INTEGER, malaria INTEGER, id INTEGER PRIMARY KEY, patient_id INTEGER, 
date INTEGER, systolic INTEGER, diastolic INTEGER, pulse INTEGER, glucose INTEGER, 
hemoglobin NUMERIC, respiration INTEGER, temperature NUMERIC, height NUMERIC, 
weight NUMERIC, pregnant INTEGER, nursing INTEGER, comments TEXT, followup_this INTEGER, 
followup_next INTEGER, followup_lads INTEGER, followup_referral INTEGER, clinic_team_id INTEGER);


insert into patients_new
select t1.num_visits, t1.patient_group_id, 
t1.children_dob, t1.children, t1.medications, t1.condition_flags, 
t1.notes, t1.is_followed_by_lads, t1.is_church_member, 
t1.is_deceased, t1.dob, t1.marital_status, t1.gender, 
t2.global_id as id,
t1.last_name, t1.first_name, t1.address1, 
t1.address2, t1.city, t1.phone1, t1.phone2, t1.email
from patients as t1 
inner join patients_correct_id as t2 
on t1.id = t2.id_t2;

insert into visits_new
select num_prescriptions, symptoms, strep, 
urine_test_id, malaria, 
id, 
t2.global_id as patient_id,
date, systolic, diastolic, pulse, glucose, 
hemoglobin, respiration, temperature, height, 
weight, pregnant, nursing, comments, followup_this, 
followup_next, followup_lads, followup_referral, clinic_team_id
from visits as t1
inner join patients_correct_id as t2
on t1.patient_id = t2.id_t2;



DROP INDEX visit_patient_idx;


CREATE INDEX visit_patient_idx ON visits_new(patient_id ASC);


DROP TABLE patients;
DROP TABLE visits;
DROP TABLE patients_correct_id;

ALTER TABLE patients_new
RENAME TO patients;

ALTER TABLE visits_new
RENAME TO visits;