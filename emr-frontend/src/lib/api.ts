import { apiFetch } from "./http";

export type Role = "PATIENT" | "DOCTOR" | "NURSE" | "LABTECH" | "ADMIN";

export type MeResponse = {
  userId: number;
  username: string;
  email: string;
  role: Role;
  patientId: number | null;
  doctorId: number | null;
  nurseId: number | null;
  labTechId: number | null;
  adminId: number | null;
  displayName: string;
  twoFactorEnabled: boolean;
};

export type AuthResponse = { token: string; me: MeResponse };

export const api = {
  auth: {
    login: (identifier: string, password: string, twoFactorCode?: string) =>
      apiFetch<AuthResponse>("/auth/login", {
        method: "POST",
        body: { identifier, password, twoFactorCode: twoFactorCode ?? null }
      }),
    register: (username: string, email: string, password: string, firstName: string, lastName: string,
      dateOfBirth?: string, ssn?: string, phone?: string, address?: string, sex?: string,
      insuranceProvider?: string, policyNumber?: string, groupNumber?: string, effectiveDate?: string) =>
      apiFetch<AuthResponse>("/auth/register", {
        method: "POST",
        body: { username, email, password, firstName, lastName, dateOfBirth, ssn, phone, address, sex,
          insuranceProvider, policyNumber, groupNumber, effectiveDate }
      }),
    resetPassword: (email: string, newPassword: string) =>
      apiFetch<void>("/auth/reset-password", {
        method: "POST",
        body: { email, newPassword }
      })
  },
  users: {
    me: (token: string) => apiFetch<MeResponse>("/users/me", { token }),
    list: (token: string) => apiFetch<UserListResponse[]>("/users", { token }),
    create: (token: string, req: AdminCreateUserRequest) =>
      apiFetch<UserListResponse>("/users", { method: "POST", token, body: req }),
    updateRole: (token: string, userId: number, role: Role) =>
      apiFetch<UserListResponse>(`/users/${userId}/role`, {
        method: "PUT",
        token,
        body: { role }
      }),
    updateTwoFactor: (token: string, userId: number, enabled: boolean) =>
      apiFetch<TwoFactorSetup>(`/users/${userId}/2fa`, { method: "PUT", token, body: { enabled } })
  },
  doctors: {
    list: (token: string) => apiFetch<DoctorListResponse[]>("/doctors", { token })
  },
  patients: {
    myProfile: (token: string) => apiFetch<PatientProfile>("/patients/my-profile", { token }),
    updateMyProfile: (token: string, req: PatientProfileUpdate) =>
      apiFetch<PatientProfile>("/patients/my-profile", { method: "PUT", token, body: req }),
    search: (token: string, params: { q?: string; dob?: string; patientId?: string }) => {
      const qp = new URLSearchParams();
      if (params.q) qp.set("q", params.q);
      if (params.dob) qp.set("dob", params.dob);
      if (params.patientId) qp.set("patientId", params.patientId);
      return apiFetch<PatientSearch[]>(`/patients/search?${qp.toString()}`, { token });
    },
    getById: (token: string, patientId: number) =>
      apiFetch<PatientProfile>(`/patients/${patientId}`, { token })
  },
  appointments: {
    my: (token: string) => apiFetch<Appointment[]>("/appointments/my", { token }),
    create: (token: string, req: AppointmentCreate) =>
      apiFetch<Appointment>("/appointments", { method: "POST", token, body: req }),
    updateStatus: (token: string, appointmentId: number, req: AppointmentStatusUpdate) =>
      apiFetch<Appointment>(`/appointments/${appointmentId}/status`, { method: "PUT", token, body: req }),
    update: (token: string, appointmentId: number, req: { scheduledAt?: string | null; status?: string | null; notes?: string | null }) =>
      apiFetch<Appointment>(`/appointments/${appointmentId}`, { method: "PUT", token, body: req })
  },
  prescriptions: {
    my: (token: string) => apiFetch<Prescription[]>("/prescriptions/my", { token }),
    forPatient: (token: string, patientId: number) =>
      apiFetch<Prescription[]>(`/prescriptions/patient/${patientId}`, { token }),
    createForPatient: (token: string, patientId: number, req: PrescriptionCreate) =>
      apiFetch<Prescription>(`/prescriptions/patient/${patientId}`, { method: "POST", token, body: req }),
    updateStatus: (token: string, prescriptionId: number, status: string) =>
      apiFetch<Prescription>(`/prescriptions/${prescriptionId}/status`, { method: "PUT", token, body: { status } })
  },
  billing: {
    my: (token: string) => apiFetch<Billing[]>("/billing/my", { token }),
    forPatient: (token: string, patientId: number) => apiFetch<Billing[]>(`/billing/patient/${patientId}`, { token }),
    create: (token: string, req: BillingCreate) =>
      apiFetch<Billing>("/billing", { method: "POST", token, body: req }),
    updateStatus: (token: string, billingId: number, status: string) =>
      apiFetch<Billing>(`/billing/${billingId}/status`, { method: "PUT", token, body: { status } })
  },
  insurance: {
    my: (token: string) => apiFetch<Insurance[]>("/insurance/my", { token }),
    forPatient: (token: string, patientId: number) =>
      apiFetch<Insurance[]>(`/insurance/patient/${patientId}`, { token }),
    upsert: (token: string, patientId: number, req: InsuranceUpsert) =>
      apiFetch<Insurance>(`/insurance/patient/${patientId}`, { method: "POST", token, body: req })
  },
  medicalHistory: {
    my: (token: string) => apiFetch<MedicalHistory[]>("/medical-history/my", { token }),
    forPatient: (token: string, patientId: number) =>
      apiFetch<MedicalHistory[]>(`/medical-history/patient/${patientId}`, { token }),
    add: (token: string, patientId: number, req: MedicalHistoryCreate) =>
      apiFetch<MedicalHistory>(`/medical-history/patient/${patientId}`, { method: "POST", token, body: req })
  },
  testResults: {
    my: (token: string) => apiFetch<TestResult[]>("/test-results/my", { token }),
    forPatient: (token: string, patientId: number) =>
      apiFetch<TestResult[]>(`/test-results/patient/${patientId}`, { token }),
    add: (token: string, patientId: number, req: TestResultCreate) =>
      apiFetch<TestResult>(`/test-results/patient/${patientId}`, { method: "POST", token, body: req })
  },
  accessLogs: {
    list: (token: string) => apiFetch<AccessLog[]>("/access-logs", { token })
  },
  reports: {
    summary: (token: string) => apiFetch<ReportSummary>("/reports/summary", { token }),
    appointmentsByDoctor: (token: string) => apiFetch<DoctorAppointmentReportRow[]>("/reports/appointments-by-doctor", { token }),
    billingStatus: (token: string) => apiFetch<BillingStatusReportRow[]>("/reports/billing-status", { token }),
    accessLogActions: (token: string) => apiFetch<AccessLogActionReportRow[]>("/reports/access-log-actions", { token })
  }
};

export type DoctorListResponse = {
  doctorId: number;
  firstName: string;
  lastName: string;
  specialty: string | null;
};

export type PatientProfile = {
  patientId: number;
  userId: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  maskedSsn: string | null;
  dateOfBirth: string | null;
  sex: string | null;
  phone: string | null;
  addressLine1: string | null;
  addressLine2: string | null;
  city: string | null;
  state: string | null;
  zip: string | null;
  insuranceId: number | null;
  emergencyContactName: string | null;
  emergencyContactPhone: string | null;
};

export type PatientProfileUpdate = {
  phone?: string | null;
  addressLine1?: string | null;
  addressLine2?: string | null;
  city?: string | null;
  state?: string | null;
  zip?: string | null;
  emergencyContactName?: string | null;
  emergencyContactPhone?: string | null;
};

export type PatientSearch = {
  patientId: number;
  firstName: string;
  lastName: string;
  dateOfBirth: string | null;
  email: string;
};

export type Appointment = {
  id: number;
  patientId: number;
  patientName: string;
  doctorId: number;
  doctorName: string;
  scheduledAt: string;
  status: string;
  reason: string;
  notes: string | null;
};

export type AppointmentCreate = {
  doctorId: number;
  scheduledAt: string;
  reason: string;
};

export type AppointmentStatusUpdate = { status: string; notes?: string | null };

export type Prescription = {
  id: number;
  patientId: number;
  doctorId: number;
  medicationName: string;
  dosage: string;
  instructions: string;
  startDate: string | null;
  endDate: string | null;
  status: string;
  createdAt: string;
};

export type PrescriptionCreate = {
  medicationName: string;
  dosage: string;
  instructions: string;
  startDate?: string | null;
  endDate?: string | null;
};

export type Billing = {
  id: number;
  patientId: number;
  appointmentId: number | null;
  amount: string;
  status: string;
  dueDate: string;
  description: string | null;
  createdAt: string;
};

export type BillingCreate = {
  patientId: number;
  appointmentId?: number | null;
  amount: string;
  dueDate: string;
  description?: string | null;
};

export type Insurance = {
  id: number;
  patientId: number;
  providerName: string;
  policyNumber: string;
  groupNumber: string | null;
  effectiveDate: string;
  expirationDate: string | null;
};

export type InsuranceUpsert = {
  providerName: string;
  policyNumber: string;
  groupNumber?: string | null;
  effectiveDate: string;
  expirationDate?: string | null;
};

export type MedicalHistory = {
  id: number;
  patientId: number;
  doctorId: number | null;
  conditionName: string;
  notes: string | null;
  recordedAt: string;
};

export type MedicalHistoryCreate = { conditionName: string; notes?: string | null };

export type TestResult = {
  id: number;
  patientId: number;
  doctorId: number | null;
  testName: string;
  resultValue: string;
  units: string | null;
  normalRange: string | null;
  resultDate: string | null;
  notes: string | null;
  createdAt: string;
};

export type TestResultCreate = {
  testName: string;
  resultValue: string;
  units?: string | null;
  normalRange?: string | null;
  resultDate?: string | null;
  notes?: string | null;
};

export type AccessLog = {
  id: number;
  actorUserId: number;
  actorEmail: string;
  action: string;
  resourceType: string;
  resourceId: string | null;
  ipAddress: string | null;
  createdAt: string;
};

export type ReportSummary = {
  users: number;
  patients: number;
  doctors: number;
  appointments: number;
  prescriptions: number;
  openBills: number;
};

export type UserListResponse = {
  userId: number;
  username: string;
  email: string;
  role: Role;
  enabled: boolean;
  createdAt: string;
  displayName: string;
  twoFactorEnabled: boolean;
};

export type AdminCreateUserRequest = {
  username: string;
  email: string;
  password: string;
  role: Role;
  firstName: string;
  lastName: string;
};

export type TwoFactorSetup = {
  userId: number;
  enabled: boolean;
  secret: string | null;
  otpAuthUri: string | null;
};

export type DoctorAppointmentReportRow = {
  doctorId: number;
  doctorName: string;
  total: number;
  scheduled: number;
  confirmed: number;
  completed: number;
  cancelled: number;
};

export type BillingStatusReportRow = {
  status: string;
  count: number;
};

export type AccessLogActionReportRow = {
  action: string;
  count: number;
};
