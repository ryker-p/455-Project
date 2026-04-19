import { Navigate, Route, Routes } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import ResetPasswordPage from "./pages/ResetPasswordPage";
import RequireAuth from "./routes/RequireAuth";
import RequireRole from "./routes/RequireRole";
import PatientDashboardPage from "./pages/PatientDashboardPage";
import DoctorDashboardPage from "./pages/DoctorDashboardPage";
import NurseDashboardPage from "./pages/NurseDashboardPage";
import LabTechDashboardPage from "./pages/LabTechDashboardPage";
import AdminDashboardPage from "./pages/AdminDashboardPage";
import PatientProfilePage from "./pages/PatientProfilePage";
import PatientSearchPage from "./pages/PatientSearchPage";
import PatientAppointmentSchedulingPage from "./pages/PatientAppointmentSchedulingPage";
import DoctorAppointmentManagementPage from "./pages/DoctorAppointmentManagementPage";
import PrescriptionManagementPage from "./pages/PrescriptionManagementPage";
import BillingInsurancePage from "./pages/BillingInsurancePage";
import MedicalHistoryPage from "./pages/MedicalHistoryPage";
import AccessLogsPage from "./pages/AccessLogsPage";
import ReportsPage from "./pages/ReportsPage";
import RoleAssignmentPage from "./pages/RoleAssignmentPage";
import NotFoundPage from "./pages/NotFoundPage";
import { useAuth } from "./state/AuthContext";

function HomeRedirect() {
  const { me } = useAuth();
  if (!me) return <Navigate to="/login" replace />;
  const role = me.role;
  if (role === "PATIENT") return <Navigate to="/patient" replace />;
  if (role === "DOCTOR") return <Navigate to="/doctor" replace />;
  if (role === "NURSE") return <Navigate to="/nurse" replace />;
  if (role === "LABTECH") return <Navigate to="/labtech" replace />;
  return <Navigate to="/admin" replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />

      <Route element={<RequireAuth />}>
        <Route path="/patient" element={<RequireRole role="PATIENT" />}>
          <Route index element={<PatientDashboardPage />} />
          <Route path="profile" element={<PatientProfilePage />} />
          <Route path="schedule" element={<PatientAppointmentSchedulingPage />} />
          <Route path="prescriptions" element={<PrescriptionManagementPage />} />
          <Route path="billing" element={<BillingInsurancePage />} />
          <Route path="history" element={<MedicalHistoryPage />} />
        </Route>

        <Route path="/doctor" element={<RequireRole role="DOCTOR" />}>
          <Route index element={<DoctorDashboardPage />} />
          <Route path="patients" element={<PatientSearchPage />} />
          <Route path="appointments" element={<DoctorAppointmentManagementPage />} />
          <Route path="prescriptions" element={<PrescriptionManagementPage />} />
          <Route path="billing" element={<BillingInsurancePage />} />
          <Route path="history" element={<MedicalHistoryPage />} />
        </Route>

        <Route path="/nurse" element={<RequireRole role="NURSE" />}>
          <Route index element={<NurseDashboardPage />} />
          <Route path="patients" element={<PatientSearchPage />} />
          <Route path="appointments" element={<DoctorAppointmentManagementPage />} />
          <Route path="prescriptions" element={<PrescriptionManagementPage />} />
          <Route path="billing" element={<BillingInsurancePage />} />
          <Route path="history" element={<MedicalHistoryPage />} />
        </Route>

        <Route path="/labtech" element={<RequireRole role="LABTECH" />}>
          <Route index element={<LabTechDashboardPage />} />
          <Route path="patients" element={<PatientSearchPage />} />
          <Route path="history" element={<MedicalHistoryPage />} />
        </Route>

        <Route path="/admin" element={<RequireRole role="ADMIN" />}>
          <Route index element={<AdminDashboardPage />} />
          <Route path="patients" element={<PatientSearchPage />} />
          <Route path="access-logs" element={<AccessLogsPage />} />
          <Route path="reports" element={<ReportsPage />} />
          <Route path="users" element={<RoleAssignmentPage />} />
        </Route>
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
