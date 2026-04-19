import { useEffect, useState } from "react";
import Layout from "../components/Layout";
import Toast from "../components/Toast";
import { api, Appointment } from "../lib/api";
import { ApiError } from "../lib/http";
import { useAuth } from "../state/AuthContext";
import Select from "../components/Select";
import Button from "../components/Button";
import TextArea from "../components/TextArea";

const STATUSES = ["SCHEDULED", "CONFIRMED", "COMPLETED", "CANCELLED", "NO_SHOW"];

export default function DoctorAppointmentManagementPage() {
  const { token } = useAuth();
  const [rows, setRows] = useState<Appointment[]>([]);
  const [toast, setToast] = useState<string | null>(null);
  const [notes, setNotes] = useState<Record<number, string>>({});
  const [status, setStatus] = useState<Record<number, string>>({});
  const [scheduled, setScheduled] = useState<Record<number, string>>({});

  const load = async () => {
    setToast(null);
    try {
      const a = await api.appointments.my(token!);
      setRows(a);
      const nextStatus: Record<number, string> = {};
      const nextNotes: Record<number, string> = {};
      const nextScheduled: Record<number, string> = {};
      a.forEach((r) => {
        nextStatus[r.id] = r.status;
        nextNotes[r.id] = r.notes ?? "";
        // Convert ISO -> datetime-local value
        const d = new Date(r.scheduledAt);
        const pad = (n: number) => String(n).padStart(2, "0");
        nextScheduled[r.id] = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
      });
      setStatus(nextStatus);
      setNotes(nextNotes);
      setScheduled(nextScheduled);
    } catch (err: any) {
      const msg = err instanceof ApiError ? err.message : "Failed to load appointments";
      setToast(msg);
    }
  };

  useEffect(() => {
    load();
  }, [token]);

  const save = async (id: number) => {
    setToast(null);
    try {
      await api.appointments.update(token!, id, {
        status: status[id],
        notes: notes[id],
        scheduledAt: scheduled[id] ? new Date(scheduled[id]).toISOString() : null
      });
      setToast("Updated.");
      await load();
    } catch (err: any) {
      const msg = err instanceof ApiError ? err.message : "Update failed";
      setToast(msg);
    }
  };

  return (
    <Layout title="Appointment Management">
      <Toast message={toast} onClose={() => setToast(null)} />
      <div className="card">
        <table className="table">
          <thead>
            <tr>
              <th>When</th>
              <th>Patient</th>
              <th>Status</th>
              <th>Reschedule</th>
              <th style={{ width: 360 }}>Notes</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.id}>
                <td>{new Date(r.scheduledAt).toLocaleString()}</td>
                <td>{r.patientName}</td>
                <td>
                  <Select label="" value={status[r.id] ?? r.status} onChange={(e) => setStatus({ ...status, [r.id]: e.target.value })}>
                    {STATUSES.map((s) => (
                      <option key={s} value={s}>
                        {s}
                      </option>
                    ))}
                  </Select>
                </td>
                <td>
                  <input
                    className="input"
                    type="datetime-local"
                    value={scheduled[r.id] ?? ""}
                    onChange={(e) => setScheduled({ ...scheduled, [r.id]: e.target.value })}
                  />
                </td>
                <td>
                  <TextArea label="" value={notes[r.id] ?? ""} onChange={(e) => setNotes({ ...notes, [r.id]: e.target.value })} rows={2} />
                </td>
                <td>
                  <Button variant="secondary" onClick={() => save(r.id)}>
                    Save
                  </Button>
                </td>
              </tr>
            ))}
            {rows.length === 0 && (
              <tr>
                <td colSpan={6} className="muted">
                  No appointments found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </Layout>
  );
}
