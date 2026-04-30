-- MySQL schema for EMR (matches required table names)

CREATE TABLE IF NOT EXISTS `User` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(60) NOT NULL UNIQUE,
  `email` VARCHAR(180) NOT NULL UNIQUE,
  `password_hash` VARCHAR(255) NOT NULL,
  `two_factor_enabled` BOOLEAN NOT NULL DEFAULT FALSE,
  `two_factor_secret` VARCHAR(64) NULL,
  `role` VARCHAR(24) NOT NULL,
  `enabled` BOOLEAN NOT NULL DEFAULT TRUE,
  `failed_login_attempts` INT NOT NULL DEFAULT 0,
  `locked_until` TIMESTAMP NULL,
  `last_login_at` TIMESTAMP NULL,
  `display_name` VARCHAR(120) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `Patient` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL UNIQUE,
  `first_name` VARCHAR(60) NOT NULL,
  `last_name` VARCHAR(60) NOT NULL,
  `ssn` CHAR(11) NULL UNIQUE,
  `date_of_birth` DATE NULL,
  `sex` VARCHAR(24) NULL,
  `phone` VARCHAR(20) NULL,
  `address` VARCHAR(255) NULL,
  CONSTRAINT `fk_patient_user` FOREIGN KEY (`user_id`) REFERENCES `User` (`id`)
);

CREATE TABLE IF NOT EXISTS `Doctor` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL UNIQUE,
  `first_name` VARCHAR(60) NOT NULL,
  `last_name` VARCHAR(60) NOT NULL,
  `ssn` CHAR(11) NULL UNIQUE,
  `specialty` VARCHAR(80) NULL,
  CONSTRAINT `fk_doctor_user` FOREIGN KEY (`user_id`) REFERENCES `User` (`id`)
);

CREATE TABLE IF NOT EXISTS `Nurse` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL UNIQUE,
  `first_name` VARCHAR(60) NOT NULL,
  `last_name` VARCHAR(60) NOT NULL,
  `ssn` CHAR(11) NULL UNIQUE,
  `department` VARCHAR(80) NULL,
  CONSTRAINT `fk_nurse_user` FOREIGN KEY (`user_id`) REFERENCES `User` (`id`)
);

CREATE TABLE IF NOT EXISTS `Lab_Technician` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL UNIQUE,
  `first_name` VARCHAR(60) NOT NULL,
  `last_name` VARCHAR(60) NOT NULL,
  `ssn` CHAR(11) NULL UNIQUE,
  CONSTRAINT `fk_lab_tech_user` FOREIGN KEY (`user_id`) REFERENCES `User` (`id`)
);

CREATE TABLE IF NOT EXISTS `Admin` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL UNIQUE,
  `title` VARCHAR(80) NULL,
  CONSTRAINT `fk_admin_user` FOREIGN KEY (`user_id`) REFERENCES `User` (`id`)
);

CREATE TABLE IF NOT EXISTS `Account_Info` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL UNIQUE,
  `phone` VARCHAR(32) NULL,
  `address_line1` VARCHAR(120) NULL,
  `address_line2` VARCHAR(120) NULL,
  `city` VARCHAR(80) NULL,
  `state` VARCHAR(32) NULL,
  `zip` VARCHAR(16) NULL,
  `emergency_contact_name` VARCHAR(120) NULL,
  `emergency_contact_phone` VARCHAR(32) NULL,
  CONSTRAINT `fk_account_info_user` FOREIGN KEY (`user_id`) REFERENCES `User` (`id`)
);

CREATE TABLE IF NOT EXISTS `Appointment` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `patient_id` BIGINT NOT NULL,
  `doctor_id` BIGINT NOT NULL,
  `scheduled_at` TIMESTAMP NOT NULL,
  `status` VARCHAR(24) NOT NULL,
  `reason` VARCHAR(200) NOT NULL,
  `notes` VARCHAR(500) NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT `fk_appointment_patient` FOREIGN KEY (`patient_id`) REFERENCES `Patient` (`id`),
  CONSTRAINT `fk_appointment_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `Doctor` (`id`)
);

CREATE TABLE IF NOT EXISTS `Prescription` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `patient_id` BIGINT NOT NULL,
  `doctor_id` BIGINT NOT NULL,
  `medication_name` VARCHAR(120) NOT NULL,
  `dosage` VARCHAR(60) NOT NULL,
  `instructions` VARCHAR(400) NOT NULL,
  `start_date` DATE NULL,
  `end_date` DATE NULL,
  `status` VARCHAR(24) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT `fk_prescription_patient` FOREIGN KEY (`patient_id`) REFERENCES `Patient` (`id`),
  CONSTRAINT `fk_prescription_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `Doctor` (`id`)
);

CREATE TABLE IF NOT EXISTS `Medical_History` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `patient_id` BIGINT NOT NULL,
  `doctor_id` BIGINT NULL,
  `condition_name` VARCHAR(120) NOT NULL,
  `notes` VARCHAR(500) NULL,
  `recorded_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT `fk_medical_history_patient` FOREIGN KEY (`patient_id`) REFERENCES `Patient` (`id`),
  CONSTRAINT `fk_medical_history_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `Doctor` (`id`)
);

CREATE TABLE IF NOT EXISTS `Test_Result` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `patient_id` BIGINT NOT NULL,
  `doctor_id` BIGINT NULL,
  `test_name` VARCHAR(120) NOT NULL,
  `result_value` VARCHAR(120) NOT NULL,
  `units` VARCHAR(40) NULL,
  `normal_range` VARCHAR(60) NULL,
  `result_date` DATE NULL,
  `notes` VARCHAR(500) NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT `fk_test_result_patient` FOREIGN KEY (`patient_id`) REFERENCES `Patient` (`id`),
  CONSTRAINT `fk_test_result_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `Doctor` (`id`)
);

CREATE TABLE IF NOT EXISTS `Billing` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `patient_id` BIGINT NOT NULL,
  `appointment_id` BIGINT NULL,
  `amount` DECIMAL(12,2) NOT NULL,
  `status` VARCHAR(24) NOT NULL,
  `due_date` DATE NOT NULL,
  `description` VARCHAR(200) NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT `fk_billing_patient` FOREIGN KEY (`patient_id`) REFERENCES `Patient` (`id`),
  CONSTRAINT `fk_billing_appointment` FOREIGN KEY (`appointment_id`) REFERENCES `Appointment` (`id`)
);

CREATE TABLE IF NOT EXISTS `Insurance` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `patient_id` BIGINT NOT NULL,
  `provider_name` VARCHAR(120) NOT NULL,
  `policy_number` VARCHAR(80) NOT NULL,
  `group_number` VARCHAR(80) NULL,
  `effective_date` DATE NOT NULL,
  `expiration_date` DATE NULL,
  CONSTRAINT `fk_insurance_patient` FOREIGN KEY (`patient_id`) REFERENCES `Patient` (`id`)
);

CREATE TABLE IF NOT EXISTS `Access_Log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `actor_user_id` BIGINT NOT NULL,
  `action` VARCHAR(80) NOT NULL,
  `resource_type` VARCHAR(60) NOT NULL,
  `resource_id` VARCHAR(60) NULL,
  `ip_address` VARCHAR(60) NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT `fk_access_log_actor` FOREIGN KEY (`actor_user_id`) REFERENCES `User` (`id`)
);
