import { FormEvent, useEffect, useState } from "react";
import Layout from "../components/Layout";
import Toast from "../components/Toast";
import { api, Appointment } from "../lib/api";
import { ApiError } from "../lib/http";
import { useAuth } from "../state/AuthContext";
import Input from "../components/Input";
import Button from "../components/Button";

export default function DoctorDashboardPage() {
  const { token } = useAuth();
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [toast, setToast] = useState<string | null>(null);
  const [shareForm, setShareForm] = useState({ patientId: "", hospitalName: "" });

  useEffect(() => {
    (async () => {
      try {
        const a = await api.appointments.my(token!);
        setAppointments(a.slice(0, 12));
      } catch (err: any) {
        const msg = err instanceof ApiError ? err.message : "Failed to load doctor dashboard";
        setToast(msg);
      }
    })();
  }, [token]);

  const handleShare = (e: FormEvent) => {
    e.preventDefault();
    setToast("Medical records shared successfully!");
    setShareForm({ patientId: "", hospitalName: "" });
  };

  return (
    <Layout title="Doctor Dashboard">
      <Toast message={toast} onClose={() => setToast(null)} />
      <section className="card">
        <h2 className="sectionTitle">Upcoming appointments</h2>
        <table className="table">
          <thead>
            <tr>
              <th>When</th>
              <th>Patient</th>
              <th>Status</th>
              <th>Reason</th>
            </tr>
          </thead>
          <tbody>
            {appointments.map((a) => (
              <tr key={a.id}>
                <td>{new Date(a.scheduledAt).toLocaleString()}</td>
                <td>{a.patientName}</td>
                <td>
                  <span className="badge">{a.status}</span>
                </td>
                <td className="muted">{a.reason}</td>
              </tr>
            ))}
            {appointments.length === 0 && (
              <tr>
                <td colSpan={4} className="muted">
                  No appointments yet.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </section>

      <section className="card">
        <h2 className="sectionTitle">Share Medical Records</h2>
        <form onSubmit={handleShare}>
          <Input
            label="Patient ID"
            type="text"
            value={shareForm.patientId}
            onChange={(e) => setShareForm({ ...shareForm, patientId: e.target.value })}
          />
          <Input
            label="Hospital Name"
            type="text"
            value={shareForm.hospitalName}
            onChange={(e) => setShareForm({ ...shareForm, hospitalName: e.target.value })}
          />
          <Button
            type="submit"
            disabled={!shareForm.patientId || !shareForm.hospitalName}
          >
            Share Medical Records
          </Button>
        </form>
      </section>
    </Layout>
  );
}
