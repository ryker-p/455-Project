import { FormEvent, useEffect, useState } from "react";
import Layout from "../components/Layout";
import Toast from "../components/Toast";
import { api, DoctorListResponse } from "../lib/api";
import { ApiError } from "../lib/http";
import { useAuth } from "../state/AuthContext";
import Select from "../components/Select";
import Input from "../components/Input";
import Button from "../components/Button";
import TextArea from "../components/TextArea";

export default function PatientAppointmentSchedulingPage() {
  const { token } = useAuth();
  const [doctors, setDoctors] = useState<DoctorListResponse[]>([]);
  const [doctorId, setDoctorId] = useState<string>("");
  const [scheduledAt, setScheduledAt] = useState<string>("");
  const [reason, setReason] = useState<string>("");
  const [toast, setToast] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        const d = await api.doctors.list(token!);
        setDoctors(d);
      } catch (err: any) {
        const msg = err instanceof ApiError ? err.message : "Failed to load doctors";
        setToast(msg);
      }
    })();
  }, [token]);

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setToast(null);
    try {
      await api.appointments.create(token!, {
        doctorId: Number(doctorId),
        scheduledAt: new Date(scheduledAt).toISOString(),
        reason
      });
      setToast("Appointment scheduled.");
      setReason("");
      setScheduledAt("");
    } catch (err: any) {
      const msg = err instanceof ApiError ? err.message : "Scheduling failed";
      setToast(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Layout title="Schedule Appointment">
      <Toast message={toast} onClose={() => setToast(null)} />
      <form className="card" onSubmit={submit}>
        <Select label="Doctor" value={doctorId} onChange={(e) => setDoctorId(e.target.value)}>
          <option value="">Select a doctor...</option>
          {doctors.map((d) => (
            <option key={d.doctorId} value={String(d.doctorId)}>
              {d.firstName} {d.lastName} {d.specialty ? `(${d.specialty})` : ""}
            </option>
          ))}
        </Select>
        <Input
          label="Date/time"
          type="datetime-local"
          value={scheduledAt}
          onChange={(e) => setScheduledAt(e.target.value)}
        />
        <TextArea label="Reason" value={reason} onChange={(e) => setReason(e.target.value)} rows={4} />
        <Button type="submit" disabled={loading || !doctorId || !scheduledAt || !reason.trim()}>
          {loading ? "Scheduling..." : "Schedule"}
        </Button>
      </form>
    </Layout>
  );
}

