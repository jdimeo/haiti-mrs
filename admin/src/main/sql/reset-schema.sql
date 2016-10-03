delete from patient_groups;
delete from urine_tests;
delete from patients;
delete from visits;
delete from visits_text;
delete from prescriptions where visit_id is not null;
delete from prescription_directives where prescription_id not in (select id from prescriptions);
delete from prescription_directive_blanks where prescription_directive_id not in (select id from prescription_directives);
delete from clinic_teams;