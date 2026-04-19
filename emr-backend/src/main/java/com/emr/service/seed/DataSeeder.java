package com.emr.service.seed;

import com.emr.model.*;
import com.emr.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {
  private final UserRepository userRepository;
  private final PatientRepository patientRepository;
  private final DoctorRepository doctorRepository;
  private final NurseRepository nurseRepository;
  private final AdminRepository adminRepository;
  private final AccountInfoRepository accountInfoRepository;
  private final AppointmentRepository appointmentRepository;
  private final PrescriptionRepository prescriptionRepository;
  private final BillingRepository billingRepository;
  private final InsuranceRepository insuranceRepository;
  private final MedicalHistoryRepository medicalHistoryRepository;
  private final TestResultRepository testResultRepository;
  private final PasswordEncoder passwordEncoder;

  public DataSeeder(
      UserRepository userRepository,
      PatientRepository patientRepository,
      DoctorRepository doctorRepository,
      NurseRepository nurseRepository,
      AdminRepository adminRepository,
      AccountInfoRepository accountInfoRepository,
      AppointmentRepository appointmentRepository,
      PrescriptionRepository prescriptionRepository,
      BillingRepository billingRepository,
      InsuranceRepository insuranceRepository,
      MedicalHistoryRepository medicalHistoryRepository,
      TestResultRepository testResultRepository,
      PasswordEncoder passwordEncoder
  ) {
    this.userRepository = userRepository;
    this.patientRepository = patientRepository;
    this.doctorRepository = doctorRepository;
    this.nurseRepository = nurseRepository;
    this.adminRepository = adminRepository;
    this.accountInfoRepository = accountInfoRepository;
    this.appointmentRepository = appointmentRepository;
    this.prescriptionRepository = prescriptionRepository;
    this.billingRepository = billingRepository;
    this.insuranceRepository = insuranceRepository;
    this.medicalHistoryRepository = medicalHistoryRepository;
    this.testResultRepository = testResultRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public void run(String... args) {
    ensureDemoUsersExist();
    ensureSeedPasswordsHashed();
    ensureDemoClinicalData();
  }

  private void ensureDemoUsersExist() {
    if (userRepository.count() > 0) {
      return;
    }

    User admin = new User();
    admin.setUsername("admin1");
    admin.setEmail("admin1@emr.local");
    admin.setPasswordHash(passwordEncoder.encode("Password123!"));
    admin.setRole(Role.ADMIN);
    admin.setDisplayName("Admin One");
    admin = userRepository.save(admin);
    Admin adminProfile = new Admin();
    adminProfile.setUser(admin);
    adminProfile.setTitle("System Admin");
    adminRepository.save(adminProfile);

    User doctor = new User();
    doctor.setUsername("doctor1");
    doctor.setEmail("doctor1@emr.local");
    doctor.setPasswordHash(passwordEncoder.encode("Password123!"));
    doctor.setRole(Role.DOCTOR);
    doctor.setDisplayName("Dr. Sam Carter");
    doctor = userRepository.save(doctor);
    Doctor doctorProfile = new Doctor();
    doctorProfile.setUser(doctor);
    doctorProfile.setFirstName("Sam");
    doctorProfile.setLastName("Carter");
    doctorProfile.setSpecialty("Family Medicine");
    doctorRepository.save(doctorProfile);

    User nurse = new User();
    nurse.setUsername("nurse1");
    nurse.setEmail("nurse1@emr.local");
    nurse.setPasswordHash(passwordEncoder.encode("Password123!"));
    nurse.setRole(Role.NURSE);
    nurse.setDisplayName("Nurse Jordan Lee");
    nurse = userRepository.save(nurse);
    Nurse nurseProfile = new Nurse();
    nurseProfile.setUser(nurse);
    nurseProfile.setFirstName("Jordan");
    nurseProfile.setLastName("Lee");
    nurseProfile.setDepartment("Primary Care");
    nurseRepository.save(nurseProfile);

    User patient = new User();
    patient.setUsername("patient1");
    patient.setEmail("patient1@emr.local");
    patient.setPasswordHash(passwordEncoder.encode("Password123!"));
    patient.setRole(Role.PATIENT);
    patient.setDisplayName("Alex Morgan");
    patient = userRepository.save(patient);
    Patient patientProfile = new Patient();
    patientProfile.setUser(patient);
    patientProfile.setFirstName("Alex");
    patientProfile.setLastName("Morgan");
    patientProfile.setDateOfBirth(LocalDate.of(1999, 3, 14));
    patientProfile.setSex("Female");
    patientRepository.save(patientProfile);

    AccountInfo ai = new AccountInfo();
    ai.setUser(patient);
    ai.setPhone("555-0101");
    ai.setAddressLine1("123 Main St");
    ai.setCity("Chicago");
    ai.setState("IL");
    ai.setZip("60601");
    accountInfoRepository.save(ai);
  }

  private void ensureSeedPasswordsHashed() {
    userRepository.findAll().forEach(u -> {
      String hash = u.getPasswordHash();
      if (hash != null && !hash.startsWith("$2")) {
        u.setPasswordHash(passwordEncoder.encode(hash));
      }
    });
  }

  private void ensureDemoClinicalData() {
    Patient patient = patientRepository.findAll().stream().findFirst().orElse(null);
    Doctor doctor = doctorRepository.findAll().stream().findFirst().orElse(null);
    if (patient == null || doctor == null) {
      return;
    }

    if (insuranceRepository.count() == 0) {
      Insurance ins = new Insurance();
      ins.setPatient(patient);
      ins.setProviderName("BlueCross Demo");
      ins.setPolicyNumber("BC-123456");
      ins.setGroupNumber("GRP-100");
      ins.setEffectiveDate(LocalDate.now().minusYears(1));
      ins.setExpirationDate(LocalDate.now().plusYears(1));
      insuranceRepository.save(ins);
    }

    if (appointmentRepository.count() == 0) {
      Appointment appt = new Appointment();
      appt.setPatient(patient);
      appt.setDoctor(doctor);
      appt.setScheduledAt(Instant.now().plusSeconds(60L * 60 * 24 * 3));
      appt.setStatus("SCHEDULED");
      appt.setReason("Annual checkup");
      appointmentRepository.save(appt);
    }

    if (medicalHistoryRepository.count() == 0) {
      MedicalHistory mh = new MedicalHistory();
      mh.setPatient(patient);
      mh.setDoctor(doctor);
      mh.setConditionName("Seasonal allergies");
      mh.setNotes("Patient reports mild symptoms in spring.");
      medicalHistoryRepository.save(mh);
    }

    if (testResultRepository.count() == 0) {
      TestResult tr = new TestResult();
      tr.setPatient(patient);
      tr.setDoctor(doctor);
      tr.setTestName("CBC");
      tr.setResultValue("Normal");
      tr.setResultDate(LocalDate.now().minusDays(30));
      tr.setNotes("All values within normal range.");
      testResultRepository.save(tr);
    }

    if (prescriptionRepository.count() == 0) {
      Prescription p = new Prescription();
      p.setPatient(patient);
      p.setDoctor(doctor);
      p.setMedicationName("Cetirizine");
      p.setDosage("10mg");
      p.setInstructions("Take 1 tablet by mouth daily as needed.");
      p.setStartDate(LocalDate.now().minusDays(10));
      p.setStatus("ACTIVE");
      prescriptionRepository.save(p);
    }

    if (billingRepository.count() == 0) {
      Billing b = new Billing();
      b.setPatient(patient);
      b.setAmount(new java.math.BigDecimal("25.00"));
      b.setDueDate(LocalDate.now().plusDays(30));
      b.setStatus("OPEN");
      b.setDescription("Copay");
      billingRepository.save(b);
    }
  }
}
