-- Demo seed: inserts users with plaintext passwords for readability.
-- On backend startup, `DataSeeder` automatically hashes any `User.password_hash` that is not already BCrypt.

INSERT IGNORE INTO `User` (`id`, `username`, `email`, `password_hash`, `two_factor_enabled`, `role`, `enabled`, `display_name`)
VALUES
  (1, 'admin1', 'admin1@emr.local', 'Password123!', false, 'ADMIN', true, 'Admin One'),
  (2, 'doctor1', 'doctor1@emr.local', 'Password123!', false, 'DOCTOR', true, 'Dr. Sam Carter'),
  (3, 'nurse1', 'nurse1@emr.local', 'Password123!', false, 'NURSE', true, 'Nurse Jordan Lee'),
  (4, 'labtech1', 'labtech1@emr.local', 'Password123!', false, 'LABTECH', true, 'Lab Tech Casey Kim'),
  (5, 'patient1', 'patient1@emr.local', 'Password123!', false, 'PATIENT', true, 'Alex Morgan');

INSERT IGNORE INTO `Admin` (`id`, `user_id`, `title`) VALUES (1, 1, 'System Admin');

INSERT IGNORE INTO `Doctor` (`id`, `user_id`, `first_name`, `last_name`, `specialty`)
VALUES (1, 2, 'Sam', 'Carter', 'Family Medicine');

INSERT IGNORE INTO `Nurse` (`id`, `user_id`, `first_name`, `last_name`, `department`)
VALUES (1, 3, 'Jordan', 'Lee', 'Primary Care');

INSERT IGNORE INTO `Lab_Technician` (`id`, `user_id`, `first_name`, `last_name`)
VALUES (1, 4, 'Casey', 'Kim');

INSERT IGNORE INTO `Patient` (`id`, `user_id`, `first_name`, `last_name`, `ssn`, `date_of_birth`, `sex`)
VALUES (1, 5, 'Alex', 'Morgan', '123-45-6789', '1999-03-14', 'Female');

INSERT IGNORE INTO `Account_Info` (`id`, `user_id`, `phone`, `address_line1`, `city`, `state`, `zip`)
VALUES (1, 5, '555-0101', '123 Main St', 'Chicago', 'IL', '60601');

INSERT IGNORE INTO `Insurance` (`id`, `patient_id`, `provider_name`, `policy_number`, `group_number`, `effective_date`, `expiration_date`)
VALUES (1, 1, 'BlueCross Demo', 'BC-123456', 'GRP-100', DATE_SUB(CURDATE(), INTERVAL 1 YEAR), DATE_ADD(CURDATE(), INTERVAL 1 YEAR));

INSERT IGNORE INTO `Appointment` (`id`, `patient_id`, `doctor_id`, `scheduled_at`, `status`, `reason`)
VALUES (1, 1, 1, DATE_ADD(NOW(), INTERVAL 3 DAY), 'SCHEDULED', 'Annual checkup');

INSERT IGNORE INTO `Medical_History` (`id`, `patient_id`, `doctor_id`, `condition_name`, `notes`)
VALUES (1, 1, 1, 'Seasonal allergies', 'Patient reports mild symptoms in spring.');

INSERT IGNORE INTO `Test_Result` (`id`, `patient_id`, `doctor_id`, `test_name`, `result_value`, `result_date`, `notes`)
VALUES (1, 1, 1, 'CBC', 'Normal', DATE_SUB(CURDATE(), INTERVAL 30 DAY), 'All values within normal range.');

INSERT IGNORE INTO `Prescription` (`id`, `patient_id`, `doctor_id`, `medication_name`, `dosage`, `instructions`, `start_date`, `status`)
VALUES (1, 1, 1, 'Cetirizine', '10mg', 'Take 1 tablet by mouth daily as needed.', DATE_SUB(CURDATE(), INTERVAL 10 DAY), 'ACTIVE');

INSERT IGNORE INTO `Billing` (`id`, `patient_id`, `appointment_id`, `amount`, `status`, `due_date`, `description`)
VALUES (1, 1, 1, 25.00, 'OPEN', DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'Copay');
