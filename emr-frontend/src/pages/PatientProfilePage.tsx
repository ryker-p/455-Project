import { FormEvent, useEffect, useState } from "react";
import Layout from "../components/Layout";
import Input from "../components/Input";
import Button from "../components/Button";
import Toast from "../components/Toast";
import { api, PatientProfileUpdate } from "../lib/api";
import { ApiError } from "../lib/http";
import { useAuth } from "../state/AuthContext";

export default function PatientProfilePage() {
  const { token } = useAuth();
  const [toast, setToast] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [readOnly, setReadOnly] = useState({
    patientId: "",
    username: "",
    email: "",
    firstName: "",
    lastName: "",
    dateOfBirth: "",
    sex: "",
    maskedSsn: "",
    insuranceId: ""
  });
  const [form, setForm] = useState<PatientProfileUpdate>({
    phone: null,
    addressLine1: null,
    addressLine2: null,
    city: null,
    state: null,
    zip: null,
    emergencyContactName: null,
    emergencyContactPhone: null
  });

  useEffect(() => {
    (async () => {
      try {
        const p = await api.patients.myProfile(token!);
        setReadOnly({
          patientId: String(p.patientId),
          username: p.username,
          email: p.email,
          firstName: p.firstName,
          lastName: p.lastName,
          dateOfBirth: p.dateOfBirth ?? "",
          sex: p.sex ?? "",
          maskedSsn: p.maskedSsn ?? "",
          insuranceId: p.insuranceId == null ? "" : String(p.insuranceId)
        });
        setForm({
          phone: p.phone,
          addressLine1: p.addressLine1,
          addressLine2: p.addressLine2,
          city: p.city,
          state: p.state,
          zip: p.zip,
          emergencyContactName: p.emergencyContactName,
          emergencyContactPhone: p.emergencyContactPhone
        });
      } catch (err: any) {
        const msg = err instanceof ApiError ? err.message : "Failed to load profile";
        setToast(msg);
      }
    })();
  }, [token]);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setToast(null);
    try {
      await api.patients.updateMyProfile(token!, form);
      setToast("Profile saved.");
    } catch (err: any) {
      const msg = err instanceof ApiError ? err.message : "Save failed";
      setToast(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Layout title="Patient Profile">
      <Toast message={toast} onClose={() => setToast(null)} />
      <form className="card" onSubmit={onSubmit}>
        <h2 className="sectionTitle">Read-only fields</h2>
        <div className="grid3">
          <Input label="Patient ID" value={readOnly.patientId} onChange={() => {}} disabled />
          <Input label="Insurance ID" value={readOnly.insuranceId} onChange={() => {}} disabled />
          <Input label="SSN (masked)" value={readOnly.maskedSsn} onChange={() => {}} disabled />
        </div>
        <div className="grid2">
          <Input label="First name" value={readOnly.firstName} onChange={() => {}} disabled />
          <Input label="Last name" value={readOnly.lastName} onChange={() => {}} disabled />
        </div>
        <div className="grid3">
          <Input label="Date of birth" value={readOnly.dateOfBirth} onChange={() => {}} disabled />
          <Input label="Sex" value={readOnly.sex} onChange={() => {}} disabled />
          <Input label="Username" value={readOnly.username} onChange={() => {}} disabled />
        </div>
        <Input label="Email" value={readOnly.email} onChange={() => {}} disabled />

        <h2 className="sectionTitle" style={{ marginTop: 14 }}>
          Editable fields
        </h2>
        <div className="grid2">
          <Input label="Phone" value={form.phone ?? ""} onChange={(e) => setForm({ ...form, phone: e.target.value || null })} />
          <Input label="Address line 1" value={form.addressLine1 ?? ""} onChange={(e) => setForm({ ...form, addressLine1: e.target.value || null })} />
        </div>
        <Input label="Address line 2" value={form.addressLine2 ?? ""} onChange={(e) => setForm({ ...form, addressLine2: e.target.value || null })} />
        <div className="grid3">
          <Input label="City" value={form.city ?? ""} onChange={(e) => setForm({ ...form, city: e.target.value || null })} />
          <Input label="State" value={form.state ?? ""} onChange={(e) => setForm({ ...form, state: e.target.value || null })} />
          <Input label="ZIP" value={form.zip ?? ""} onChange={(e) => setForm({ ...form, zip: e.target.value || null })} />
        </div>
        <div className="grid2">
          <Input
            label="Emergency contact name"
            value={form.emergencyContactName ?? ""}
            onChange={(e) => setForm({ ...form, emergencyContactName: e.target.value || null })}
          />
          <Input
            label="Emergency contact phone"
            value={form.emergencyContactPhone ?? ""}
            onChange={(e) => setForm({ ...form, emergencyContactPhone: e.target.value || null })}
          />
        </div>

        <Button disabled={loading} type="submit">
          {loading ? "Saving..." : "Save profile"}
        </Button>
      </form>
    </Layout>
  );
}
