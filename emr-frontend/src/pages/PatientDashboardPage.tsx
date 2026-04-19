import { useEffect, useMemo, useState } from "react";
import Layout from "../components/Layout";
import { api, Appointment, Billing, Prescription } from "../lib/api";
import { ApiError } from "../lib/http";
import Toast from "../components/Toast";
import { useAuth } from "../state/AuthContext";

export default function PatientDashboardPage() {
  const { token } = useAuth();
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [prescriptions, setPrescriptions] = useState<Prescription[]>([]);
  const [bills, setBills] = useState<Billing[]>([]);
  const [toast, setToast] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      try {
        const [a, p, b] = await Promise.all([
          api.appointments.my(token!),
          api.prescriptions.my(token!),
          api.billing.my(token!)
        ]);
        setAppointments(a.slice(0, 5));
        setPrescriptions(p.slice(0, 5));
        setBills(b.slice(0, 5));
      } catch (err: any) {
        const msg = err instanceof ApiError ? err.message : "Failed to load dashboard";
        setToast(msg);
      }
    })();
  }, [token]);

  const openBills = useMemo(() => bills.filter((b) => b.status === "OPEN").length, [bills]);

  return (
    <Layout title="Patient Dashboard">
      <Toast message={toast} onClose={() => setToast(null)} />
      <div className="grid3">
        <div className="card">
          <div className="kpiLabel">Upcoming appointments</div>
          <div className="kpiValue">{appointments.length}</div>
        </div>
        <div className="card">
          <div className="kpiLabel">Active prescriptions</div>
          <div className="kpiValue">{prescriptions.filter((p) => p.status === "ACTIVE").length}</div>
        </div>
        <div className="card">
          <div className="kpiLabel">Open bills</div>
          <div className="kpiValue">{openBills}</div>
        </div>
      </div>

      <div className="grid2">
        <section className="card">
          <h2 className="sectionTitle">Appointments</h2>
          <table className="table">
            <thead>
              <tr>
                <th>When</th>
                <th>Doctor</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {appointments.map((a) => (
                <tr key={a.id}>
                  <td>{new Date(a.scheduledAt).toLocaleString()}</td>
                  <td>{a.doctorName}</td>
                  <td>
                    <span className="badge">{a.status}</span>
                  </td>
                </tr>
              ))}
              {appointments.length === 0 && (
                <tr>
                  <td colSpan={3} className="muted">
                    No appointments yet.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </section>

        <section className="card">
          <h2 className="sectionTitle">Billing</h2>
          <table className="table">
            <thead>
              <tr>
                <th>Amount</th>
                <th>Due</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {bills.map((b) => (
                <tr key={b.id}>
                  <td>${b.amount}</td>
                  <td>{b.dueDate}</td>
                  <td>
                    <span className="badge">{b.status}</span>
                  </td>
                </tr>
              ))}
              {bills.length === 0 && (
                <tr>
                  <td colSpan={3} className="muted">
                    No billing records.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </section>
      </div>
    </Layout>
  );
}

