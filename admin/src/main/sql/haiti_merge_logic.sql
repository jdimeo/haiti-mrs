--sqlite3 data-base.db
attach database 'data-new1.db' as new1;
attach database 'data-new2.db' as new2;

--create the error log

DROP TABLE IF EXISTS patients_merge_conflict_log;

-- create table with merge conflict id & 1/0 flag for field with issues
CREATE TABLE patients_merge_conflict_log (id INTEGER 
, gender_location INTEGER 
, is_deceased_location INTEGER 
, phone2_location INTEGER 
, notes_location INTEGER 
, num_visits_location INTEGER 
, address1_location INTEGER 
, last_name_location INTEGER 
, id_location INTEGER 
, condition_flags_location INTEGER 
, address2_location INTEGER 
, patient_group_id_location INTEGER 
, children_dob_location INTEGER 
, dob_location INTEGER 
, email_location INTEGER 
, is_church_member_location INTEGER 
, is_followed_by_lads_location INTEGER 
, marital_status_location INTEGER 
, city_location INTEGER 
, first_name_location INTEGER 
, children_location INTEGER 
, medications_location INTEGER 
, phone1_location INTEGER 
);

insert into patients_merge_conflict_log
select b.id, case when  b.gender != n1.gender and b.gender != n2.gender and n1.gender != n2.gender 
 then 1 else 0 end as gender_location 
, case when  b.is_deceased != n1.is_deceased and b.is_deceased != n2.is_deceased and n1.is_deceased != n2.is_deceased 
 then 1 else 0 end as is_deceased_location 
, case when  b.phone2 != n1.phone2 and b.phone2 != n2.phone2 and n1.phone2 != n2.phone2 
 then 1 else 0 end as phone2_location 
, case when  b.notes != n1.notes and b.notes != n2.notes and n1.notes != n2.notes 
 then 1 else 0 end as notes_location 
, case when  b.num_visits != n1.num_visits and b.num_visits != n2.num_visits and n1.num_visits != n2.num_visits 
 then 1 else 0 end as num_visits_location 
, case when  b.address1 != n1.address1 and b.address1 != n2.address1 and n1.address1 != n2.address1 
 then 1 else 0 end as address1_location 
, case when  b.last_name != n1.last_name and b.last_name != n2.last_name and n1.last_name != n2.last_name 
 then 1 else 0 end as last_name_location 
, case when  b.id != n1.id and b.id != n2.id and n1.id != n2.id 
 then 1 else 0 end as id_location 
, case when  b.condition_flags != n1.condition_flags and b.condition_flags != n2.condition_flags and n1.condition_flags != n2.condition_flags 
 then 1 else 0 end as condition_flags_location 
, case when  b.address2 != n1.address2 and b.address2 != n2.address2 and n1.address2 != n2.address2 
 then 1 else 0 end as address2_location 
, case when  b.patient_group_id != n1.patient_group_id and b.patient_group_id != n2.patient_group_id and n1.patient_group_id != n2.patient_group_id 
 then 1 else 0 end as patient_group_id_location 
, case when  b.children_dob != n1.children_dob and b.children_dob != n2.children_dob and n1.children_dob != n2.children_dob 
 then 1 else 0 end as children_dob_location 
, case when  b.dob != n1.dob and b.dob != n2.dob and n1.dob != n2.dob 
 then 1 else 0 end as dob_location 
, case when  b.email != n1.email and b.email != n2.email and n1.email != n2.email 
 then 1 else 0 end as email_location 
, case when  b.is_church_member != n1.is_church_member and b.is_church_member != n2.is_church_member and n1.is_church_member != n2.is_church_member 
 then 1 else 0 end as is_church_member_location 
, case when  b.is_followed_by_lads != n1.is_followed_by_lads and b.is_followed_by_lads != n2.is_followed_by_lads and n1.is_followed_by_lads != n2.is_followed_by_lads 
 then 1 else 0 end as is_followed_by_lads_location 
, case when  b.marital_status != n1.marital_status and b.marital_status != n2.marital_status and n1.marital_status != n2.marital_status 
 then 1 else 0 end as marital_status_location 
, case when  b.city != n1.city and b.city != n2.city and n1.city != n2.city 
 then 1 else 0 end as city_location 
, case when  b.first_name != n1.first_name and b.first_name != n2.first_name and n1.first_name != n2.first_name 
 then 1 else 0 end as first_name_location 
, case when  b.children != n1.children and b.children != n2.children and n1.children != n2.children 
 then 1 else 0 end as children_location 
, case when  b.medications != n1.medications and b.medications != n2.medications and n1.medications != n2.medications 
 then 1 else 0 end as medications_location 
, case when  b.phone1 != n1.phone1 and b.phone1 != n2.phone1 and n1.phone1 != n2.phone1 
 then 1 else 0 end as phone1_location 
from patients as b 
inner join new1.patients as n1 
on n1.id = b.id 
 inner join new2.patients as n2 
 on n2.id = b.id where 
 b.gender != n1.gender and b.gender != n2.gender and n1.gender != n2.gender 
or  b.is_deceased != n1.is_deceased and b.is_deceased != n2.is_deceased and n1.is_deceased != n2.is_deceased 
or  b.phone2 != n1.phone2 and b.phone2 != n2.phone2 and n1.phone2 != n2.phone2 
or  b.notes != n1.notes and b.notes != n2.notes and n1.notes != n2.notes 
or  b.num_visits != n1.num_visits and b.num_visits != n2.num_visits and n1.num_visits != n2.num_visits 
or  b.address1 != n1.address1 and b.address1 != n2.address1 and n1.address1 != n2.address1 
or  b.last_name != n1.last_name and b.last_name != n2.last_name and n1.last_name != n2.last_name 
or  b.id != n1.id and b.id != n2.id and n1.id != n2.id 
or  b.condition_flags != n1.condition_flags and b.condition_flags != n2.condition_flags and n1.condition_flags != n2.condition_flags 
or  b.address2 != n1.address2 and b.address2 != n2.address2 and n1.address2 != n2.address2 
or  b.patient_group_id != n1.patient_group_id and b.patient_group_id != n2.patient_group_id and n1.patient_group_id != n2.patient_group_id 
or  b.children_dob != n1.children_dob and b.children_dob != n2.children_dob and n1.children_dob != n2.children_dob 
or  b.dob != n1.dob and b.dob != n2.dob and n1.dob != n2.dob 
or  b.email != n1.email and b.email != n2.email and n1.email != n2.email 
or  b.is_church_member != n1.is_church_member and b.is_church_member != n2.is_church_member and n1.is_church_member != n2.is_church_member 
or  b.is_followed_by_lads != n1.is_followed_by_lads and b.is_followed_by_lads != n2.is_followed_by_lads and n1.is_followed_by_lads != n2.is_followed_by_lads 
or  b.marital_status != n1.marital_status and b.marital_status != n2.marital_status and n1.marital_status != n2.marital_status 
or  b.city != n1.city and b.city != n2.city and n1.city != n2.city 
or  b.first_name != n1.first_name and b.first_name != n2.first_name and n1.first_name != n2.first_name 
or  b.children != n1.children and b.children != n2.children and n1.children != n2.children 
or  b.medications != n1.medications and b.medications != n2.medications and n1.medications != n2.medications 
or  b.phone1 != n1.phone1 and b.phone1 != n2.phone1 and n1.phone1 != n2.phone1 
;

--STOP!!! At this point you may want to review the patients merge conflict log and make changes

--create table with autoincrement id field, orginal_id, source marker & all names
DROP TABLE IF EXISTS patients_temp;

CREATE TABLE patients_temp (id INTEGER PRIMARY KEY AUTOINCREMENT,
original_id INTEGER, source TEXT 
, gender INTEGER 
, is_deceased INTEGER 
, phone2 TEXT 
, notes TEXT 
, num_visits INTEGER 
, address1 TEXT 
, last_name TEXT 
, condition_flags INTEGER 
, address2 TEXT 
, patient_group_id INTEGER 
, children_dob TEXT 
, dob INTEGER 
, email TEXT 
, is_church_member INTEGER 
, is_followed_by_lads INTEGER 
, marital_status INTEGER 
, city TEXT 
, first_name TEXT 
, children INTEGER 
, medications TEXT 
, phone1 TEXT 
);

--create the updated values from the base dataset
insert into patients_temp
select NULL as id, b.id as original_id, 'Base' as source , case 
when b.gender != n1.gender and b.gender != n2.gender and n1.gender != n2.gender 
then n2.gender 
when b.gender != n1.gender and b.gender = n2.gender 
then n1.gender 
when b.gender = n1.gender and b.gender != n2.gender 
then n2.gender 
when n1.gender = n2.gender 
then n2.gender 
else b.gender end as gender
, case 
when b.is_deceased != n1.is_deceased and b.is_deceased != n2.is_deceased and n1.is_deceased != n2.is_deceased 
then n2.is_deceased 
when b.is_deceased != n1.is_deceased and b.is_deceased = n2.is_deceased 
then n1.is_deceased 
when b.is_deceased = n1.is_deceased and b.is_deceased != n2.is_deceased 
then n2.is_deceased 
when n1.is_deceased = n2.is_deceased 
then n2.is_deceased 
else b.is_deceased end as is_deceased
, case 
when b.phone2 != n1.phone2 and b.phone2 != n2.phone2 and n1.phone2 != n2.phone2 
then n2.phone2 
when b.phone2 != n1.phone2 and b.phone2 = n2.phone2 
then n1.phone2 
when b.phone2 = n1.phone2 and b.phone2 != n2.phone2 
then n2.phone2 
when n1.phone2 = n2.phone2 
then n2.phone2 
else b.phone2 end as phone2
, case 
when b.notes != n1.notes and b.notes != n2.notes and n1.notes != n2.notes 
then n2.notes 
when b.notes != n1.notes and b.notes = n2.notes 
then n1.notes 
when b.notes = n1.notes and b.notes != n2.notes 
then n2.notes 
when n1.notes = n2.notes 
then n2.notes 
else b.notes end as notes
, case 
when b.num_visits != n1.num_visits and b.num_visits != n2.num_visits and n1.num_visits != n2.num_visits 
then n2.num_visits 
when b.num_visits != n1.num_visits and b.num_visits = n2.num_visits 
then n1.num_visits 
when b.num_visits = n1.num_visits and b.num_visits != n2.num_visits 
then n2.num_visits 
when n1.num_visits = n2.num_visits 
then n2.num_visits 
else b.num_visits end as num_visits
, case 
when b.address1 != n1.address1 and b.address1 != n2.address1 and n1.address1 != n2.address1 
then n2.address1 
when b.address1 != n1.address1 and b.address1 = n2.address1 
then n1.address1 
when b.address1 = n1.address1 and b.address1 != n2.address1 
then n2.address1 
when n1.address1 = n2.address1 
then n2.address1 
else b.address1 end as address1
, case 
when b.last_name != n1.last_name and b.last_name != n2.last_name and n1.last_name != n2.last_name 
then n2.last_name 
when b.last_name != n1.last_name and b.last_name = n2.last_name 
then n1.last_name 
when b.last_name = n1.last_name and b.last_name != n2.last_name 
then n2.last_name 
when n1.last_name = n2.last_name 
then n2.last_name 
else b.last_name end as last_name
, case 
when b.condition_flags != n1.condition_flags and b.condition_flags != n2.condition_flags and n1.condition_flags != n2.condition_flags 
then n2.condition_flags 
when b.condition_flags != n1.condition_flags and b.condition_flags = n2.condition_flags 
then n1.condition_flags 
when b.condition_flags = n1.condition_flags and b.condition_flags != n2.condition_flags 
then n2.condition_flags 
when n1.condition_flags = n2.condition_flags 
then n2.condition_flags 
else b.condition_flags end as condition_flags
, case 
when b.address2 != n1.address2 and b.address2 != n2.address2 and n1.address2 != n2.address2 
then n2.address2 
when b.address2 != n1.address2 and b.address2 = n2.address2 
then n1.address2 
when b.address2 = n1.address2 and b.address2 != n2.address2 
then n2.address2 
when n1.address2 = n2.address2 
then n2.address2 
else b.address2 end as address2
, case 
when b.patient_group_id != n1.patient_group_id and b.patient_group_id != n2.patient_group_id and n1.patient_group_id != n2.patient_group_id 
then n2.patient_group_id 
when b.patient_group_id != n1.patient_group_id and b.patient_group_id = n2.patient_group_id 
then n1.patient_group_id 
when b.patient_group_id = n1.patient_group_id and b.patient_group_id != n2.patient_group_id 
then n2.patient_group_id 
when n1.patient_group_id = n2.patient_group_id 
then n2.patient_group_id 
else b.patient_group_id end as patient_group_id
, case 
when b.children_dob != n1.children_dob and b.children_dob != n2.children_dob and n1.children_dob != n2.children_dob 
then n2.children_dob 
when b.children_dob != n1.children_dob and b.children_dob = n2.children_dob 
then n1.children_dob 
when b.children_dob = n1.children_dob and b.children_dob != n2.children_dob 
then n2.children_dob 
when n1.children_dob = n2.children_dob 
then n2.children_dob 
else b.children_dob end as children_dob
, case 
when b.dob != n1.dob and b.dob != n2.dob and n1.dob != n2.dob 
then n2.dob 
when b.dob != n1.dob and b.dob = n2.dob 
then n1.dob 
when b.dob = n1.dob and b.dob != n2.dob 
then n2.dob 
when n1.dob = n2.dob 
then n2.dob 
else b.dob end as dob
, case 
when b.email != n1.email and b.email != n2.email and n1.email != n2.email 
then n2.email 
when b.email != n1.email and b.email = n2.email 
then n1.email 
when b.email = n1.email and b.email != n2.email 
then n2.email 
when n1.email = n2.email 
then n2.email 
else b.email end as email
, case 
when b.is_church_member != n1.is_church_member and b.is_church_member != n2.is_church_member and n1.is_church_member != n2.is_church_member 
then n2.is_church_member 
when b.is_church_member != n1.is_church_member and b.is_church_member = n2.is_church_member 
then n1.is_church_member 
when b.is_church_member = n1.is_church_member and b.is_church_member != n2.is_church_member 
then n2.is_church_member 
when n1.is_church_member = n2.is_church_member 
then n2.is_church_member 
else b.is_church_member end as is_church_member
, case 
when b.is_followed_by_lads != n1.is_followed_by_lads and b.is_followed_by_lads != n2.is_followed_by_lads and n1.is_followed_by_lads != n2.is_followed_by_lads 
then n2.is_followed_by_lads 
when b.is_followed_by_lads != n1.is_followed_by_lads and b.is_followed_by_lads = n2.is_followed_by_lads 
then n1.is_followed_by_lads 
when b.is_followed_by_lads = n1.is_followed_by_lads and b.is_followed_by_lads != n2.is_followed_by_lads 
then n2.is_followed_by_lads 
when n1.is_followed_by_lads = n2.is_followed_by_lads 
then n2.is_followed_by_lads 
else b.is_followed_by_lads end as is_followed_by_lads
, case 
when b.marital_status != n1.marital_status and b.marital_status != n2.marital_status and n1.marital_status != n2.marital_status 
then n2.marital_status 
when b.marital_status != n1.marital_status and b.marital_status = n2.marital_status 
then n1.marital_status 
when b.marital_status = n1.marital_status and b.marital_status != n2.marital_status 
then n2.marital_status 
when n1.marital_status = n2.marital_status 
then n2.marital_status 
else b.marital_status end as marital_status
, case 
when b.city != n1.city and b.city != n2.city and n1.city != n2.city 
then n2.city 
when b.city != n1.city and b.city = n2.city 
then n1.city 
when b.city = n1.city and b.city != n2.city 
then n2.city 
when n1.city = n2.city 
then n2.city 
else b.city end as city
, case 
when b.first_name != n1.first_name and b.first_name != n2.first_name and n1.first_name != n2.first_name 
then n2.first_name 
when b.first_name != n1.first_name and b.first_name = n2.first_name 
then n1.first_name 
when b.first_name = n1.first_name and b.first_name != n2.first_name 
then n2.first_name 
when n1.first_name = n2.first_name 
then n2.first_name 
else b.first_name end as first_name
, case 
when b.children != n1.children and b.children != n2.children and n1.children != n2.children 
then n2.children 
when b.children != n1.children and b.children = n2.children 
then n1.children 
when b.children = n1.children and b.children != n2.children 
then n2.children 
when n1.children = n2.children 
then n2.children 
else b.children end as children
, case 
when b.medications != n1.medications and b.medications != n2.medications and n1.medications != n2.medications 
then n2.medications 
when b.medications != n1.medications and b.medications = n2.medications 
then n1.medications 
when b.medications = n1.medications and b.medications != n2.medications 
then n2.medications 
when n1.medications = n2.medications 
then n2.medications 
else b.medications end as medications
, case 
when b.phone1 != n1.phone1 and b.phone1 != n2.phone1 and n1.phone1 != n2.phone1 
then n2.phone1 
when b.phone1 != n1.phone1 and b.phone1 = n2.phone1 
then n1.phone1 
when b.phone1 = n1.phone1 and b.phone1 != n2.phone1 
then n2.phone1 
when n1.phone1 = n2.phone1 
then n2.phone1 
else b.phone1 end as phone1
from patients as b 
inner join new1.patients as n1 
on n1.id = b.id 
 inner join new2.patients as n2 
 on n2.id = b.id;



--get new1 and add it
insert into patients_temp
select NULL as id, n.id as original_id, 'new1' as source , n.gender 
, n.is_deceased 
, n.phone2 
, n.notes 
, n.num_visits 
, n.address1 
, n.last_name 
, n.condition_flags 
, n.address2 
, n.patient_group_id 
, n.children_dob 
, n.dob 
, n.email 
, n.is_church_member 
, n.is_followed_by_lads 
, n.marital_status 
, n.city 
, n.first_name 
, n.children 
, n.medications 
, n.phone1 
from new1.patients as n 
where n.id > (select max(id) from patients);


--get new2 and add it
insert into patients_temp
select NULL as id, n.id as original_id, 'new2' as source , n.gender 
, n.is_deceased 
, n.phone2 
, n.notes 
, n.num_visits 
, n.address1 
, n.last_name 
, n.condition_flags 
, n.address2 
, n.patient_group_id 
, n.children_dob 
, n.dob 
, n.email 
, n.is_church_member 
, n.is_followed_by_lads 
, n.marital_status 
, n.city 
, n.first_name 
, n.children 
, n.medications 
, n.phone1 
from new2.patients as n 
where n.id > (select max(id) from patients);


--create the new visits table
DROP TABLE IF EXISTS visits_temp;

CREATE TABLE visits_temp (id INTEGER PRIMARY KEY AUTOINCREMENT,
original_id INTEGER, source TEXT 
, num_prescriptions INTEGER 
, comments TEXT 
, followup_referral INTEGER 
, systolic INTEGER 
, followup_lads INTEGER 
, date INTEGER 
, diastolic INTEGER 
, height NUMERIC 
, urine_test_id INTEGER 
, strep INTEGER 
, pregnant INTEGER 
, symptoms TEXT 
, nursing INTEGER 
, followup_next INTEGER 
, clinic_team_id INTEGER 
, respiration INTEGER 
, glucose INTEGER 
, pulse INTEGER 
, malaria INTEGER 
, followup_this INTEGER 
, temperature NUMERIC 
, hemoglobin NUMERIC 
, patient_id INTEGER 
, weight NUMERIC 
);


-- insert the visits from the base, new1, and new2 into the updated visits table. Replace the original
--patient ids with the updated ids, track the original ids from the visits table. Do the same thing
--with the prescriptions table
insert into visits_temp
select NULL as id, t1.id as original_id, 'Base' as source 
, t1.num_prescriptions 
, t1.comments 
, t1.followup_referral 
, t1.systolic 
, t1.followup_lads 
, t1.date 
, t1.diastolic 
, t1.height 
, t1.urine_test_id 
, t1.strep 
, t1.pregnant 
, t1.symptoms 
, t1.nursing 
, t1.followup_next 
, t1.clinic_team_id 
, t1.respiration 
, t1.glucose 
, t1.pulse 
, t1.malaria 
, t1.followup_this 
, t1.temperature 
, t1.hemoglobin 
, t2.id as patient_id 
, t1.weight 
from visits as t1 
inner join patients_temp as t2 
on t1.patient_id = t2.original_id;

insert into visits_temp
select NULL as id, t1.id as original_id, 'new1' as source 
, t1.num_prescriptions 
, t1.comments 
, t1.followup_referral 
, t1.systolic 
, t1.followup_lads 
, t1.date 
, t1.diastolic 
, t1.height 
, t1.urine_test_id 
, t1.strep 
, t1.pregnant 
, t1.symptoms 
, t1.nursing 
, t1.followup_next 
, t1.clinic_team_id 
, t1.respiration 
, t1.glucose 
, t1.pulse 
, t1.malaria 
, t1.followup_this 
, t1.temperature 
, t1.hemoglobin 
, t2.id as patient_id 
, t1.weight 
from new1.visits as t1 
inner join patients_temp as t2 
on t1.patient_id = t2.original_id 
where t1.clinic_team_id > (select max(clinic_team_id) from visits);

insert into visits_temp
select NULL as id, t1.id as original_id, 'new2' as source 
, t1.num_prescriptions 
, t1.comments 
, t1.followup_referral 
, t1.systolic 
, t1.followup_lads 
, t1.date 
, t1.diastolic 
, t1.height 
, t1.urine_test_id 
, t1.strep 
, t1.pregnant 
, t1.symptoms 
, t1.nursing 
, t1.followup_next 
, t1.clinic_team_id 
, t1.respiration 
, t1.glucose 
, t1.pulse 
, t1.malaria 
, t1.followup_this 
, t1.temperature 
, t1.hemoglobin 
, t2.id as patient_id 
, t1.weight 
from new2.visits as t1 
inner join patients_temp as t2 
on t1.patient_id = t2.original_id 
where t1.clinic_team_id > (select max(clinic_team_id) from visits);

CREATE TABLE prescriptions_temp (id INTEGER PRIMARY KEY AUTOINCREMENT,
original_id INTEGER, source TEXT 
, dosage_id INTEGER 
, qty_mod_id INTEGER 
, treatment_id INTEGER 
, form_id INTEGER 
, diagnosis_id INTEGER 
, visit_id INTEGER 
, diagnosis TEXT 
, quantity NUMERIC 
);


insert into prescriptions_temp
select NULL as id, t1.id as original_id, 'Base' as source 
, t1.dosage_id 
, t1.qty_mod_id 
, t1.treatment_id 
, t1.form_id 
, t1.diagnosis_id 
, t2.id as visit_id 
, t1.diagnosis 
, t1.quantity 
from prescriptions as t1 
inner join visits_temp as t2 
on t1.visit_id = t2.original_id 
where t2.source = 'Base';


insert into prescriptions_temp
select NULL as id, t1.id as original_id, 'new1' as source 
, t1.dosage_id 
, t1.qty_mod_id 
, t1.treatment_id 
, t1.form_id 
, t1.diagnosis_id 
, t2.id as visit_id 
, t1.diagnosis 
, t1.quantity 
from new1.prescriptions as t1 
inner join visits_temp as t2 
on t1.visit_id = t2.original_id 
where t2.source = 'new1';


insert into prescriptions_temp
select NULL as id, t1.id as original_id, 'new2' as source 
, t1.dosage_id 
, t1.qty_mod_id 
, t1.treatment_id 
, t1.form_id 
, t1.diagnosis_id 
, t2.id as visit_id 
, t1.diagnosis 
, t1.quantity 
from new2.prescriptions as t1 
inner join visits_temp as t2 
on t1.visit_id = t2.original_id 
where t2.source = 'new2';

--once the final patient, visit, prescription tables are created, delete * the original tables and 
--insert into select the appropriate fields from the final tables

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

CREATE TABLE prescriptions_new (qty_mod_id INTEGER, diagnosis_id INTEGER, form_id INTEGER, 
id INTEGER PRIMARY KEY, visit_id INTEGER, diagnosis TEXT, treatment_id INTEGER, 
quantity NUMERIC, dosage_id INTEGER);

insert into patients_new
select t1.num_visits, t1.patient_group_id, 
t1.children_dob, t1.children, t1.medications, t1.condition_flags, 
t1.notes, t1.is_followed_by_lads, t1.is_church_member, 
t1.is_deceased, t1.dob, t1.marital_status, t1.gender, 
t1.id,
t1.last_name, t1.first_name, t1.address1, 
t1.address2, t1.city, t1.phone1, t1.phone2, t1.email
from patients_temp as t1 
;

insert into visits_new
select t1.num_prescriptions, t1.symptoms, t1.strep, 
t1.urine_test_id, t1.malaria, 
t1.id, 
t1.patient_id,
t1.date, t1.systolic, t1.diastolic, t1.pulse, t1.glucose, 
t1.hemoglobin, t1.respiration, t1.temperature, t1.height, 
t1.weight, t1.pregnant, t1.nursing, t1.comments, t1.followup_this, 
t1.followup_next, t1.followup_lads, t1.followup_referral, t1.clinic_team_id
from visits_temp as t1
;

insert into prescriptions_new 
select 
t1.qty_mod_id, t1.diagnosis_id, t1.form_id, 
t1.id, t1.visit_id, t1.diagnosis, t1.treatment_id, 
t1.quantity, t1.dosage_id
from prescriptions_temp as t1;


--drop the indices & recreate
DROP INDEX visit_patient_idx;
DROP INDEX script_visit_idx;



CREATE INDEX visit_patient_idx ON visits_new(patient_id ASC);
CREATE INDEX script_visit_idx ON prescriptions_new(visit_id ASC);

--drop all of the old tables & temp tables
DROP TABLE patients;
DROP TABLE patients_temp;
DROP TABLE visits;
DROP TABLE visits_temp;
DROP TABLE prescriptions;
DROP TABLE prescriptions_temp;

ALTER TABLE patients_new
RENAME TO patients;

ALTER TABLE visits_new
RENAME TO visits;

ALTER TABLE prescriptions_new
RENAME TO prescriptions;

VACUUM;