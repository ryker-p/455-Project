import { useEffect, useMemo, useState } from "react";
import Layout from "../components/Layout";
import Toast from "../components/Toast";
import { api, Appointment } from "../lib/api";
import { ApiError } from "../lib/http";
import { useAuth } from "../state/AuthContext";

export default function NurseDashboardPage() {
  const { token } = useAuth();
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [toast, setToast] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      try {
        const rows = await api.appointments.my(token!);
        setAppointments(rows.slice(0, 8));
      } catch (err: any) {
        const msg = err instanceof ApiError ? err.message : "Failed to load nurse dashboard";
        setToast(msg);
      }
    })();
  }, [token]);

  const scheduledCount = useMemo(() => appointments.filter((row) => row.status === "SCHEDULED").length, [appointments]);
  const confirmedCount = useMemo(() => appointments.filter((row) => row.status === "CONFIRMED").length, [appointments]);

  return (
    <Layout title="Nurse Dashboard">
      <Toast message={toast} onClose={() => setToast(null)} />
      <div className="grid3">
        <div className="card">
          <div className="kpiLabel">Loaded appointments</div>
          <div className="kpiValue">{appointments.length}</div>
        </div>
        <div className="card">
          <div className="kpiLabel">Still scheduled</div>
          <div className="kpiValue">{scheduledCount}</div>
        </div>
        <div className="card">
          <div className="kpiLabel">Confirmed</div>
          <div className="kpiValue">{confirmedCount}</div>
        </div>
      </div>

      <div className="grid2">
        <div className="card">
          <h2 className="sectionTitle">Current workflow</h2>
          <p className="muted">Use Patient Search to find the patient ID, or Medical History / Tests of that patient.</p>
          <p className="muted">Nurses can review appointments and add test results.</p>
        </div>

        <div className="card">
          <h2 className="sectionTitle">Recent appointments</h2>
          <table className="table">
            <thead>
              <tr>
                <th>When</th>
                <th>Patient</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {appointments.map((appointment) => (
                <tr key={appointment.id}>
                  <td>{new Date(appointment.scheduledAt).toLocaleString()}</td>
                  <td>{appointment.patientName}</td>
                  <td>
                    <span className="badge">{appointment.status}</span>
                  </td>
                </tr>
              ))}
              {appointments.length === 0 && (
                <tr>
                  <td colSpan={3} className="muted">
                    No appointments available yet.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </Layout>
  );
}
