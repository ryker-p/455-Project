import { FormEvent, useEffect, useState } from "react";
import Layout from "../components/Layout";
import Toast from "../components/Toast";
import { api, Prescription, Role } from "../lib/api";
import { ApiError } from "../lib/http";
import { useAuth } from "../state/AuthContext";
import Input from "../components/Input";
import TextArea from "../components/TextArea";
import Button from "../components/Button";
import Select from "../components/Select";

const STATUS = ["ACTIVE", "DISCONTINUED", "COMPLETED"];

export default function PrescriptionManagementPage() {
  const { token, me } = useAuth();
  const [toast, setToast] = useState<string | null>(null);
  const [rows, setRows] = useState<Prescription[]>([]);
  const [patientId, setPatientId] = useState<string>("");
  const [requestedDrug, setRequestedDrug] = useState<string>("");
  const [form, setForm] = useState({
    medicationName: "",
    dosage: "",
    instructions: "",
    startDate: "",
    endDate: ""
  });

  const load = async () => {
    setToast(null);
    try {
      if (me?.role === "PATIENT") {
        setRows(await api.prescriptions.my(token!));
      } else if (patientId) {
        setRows(await api.prescriptions.forPatient(token!, Number(patientId)));
      } else {
        setRows([]);
      }
    } catch (err: any) {
      const msg = err instanceof ApiError ? err.message : "Failed to load prescriptions";
      setToast(msg);
    }
  };

  useEffect(() => {
    load();
  }, [token, me?.role, patientId]);

  const canCreate = (me?.role as Role) === "DOCTOR";
  const canUpdateStatus = me?.role === "DOCTOR" || me?.role === "ADMIN";
  const canRequestPrescription = me?.role === "PATIENT";

  const create = async (e: FormEvent) => {
    e.preventDefault();
    setToast(null);
    if (!patientId) return;
    try {
      await api.prescriptions.createForPatient(token!, Number(patientId), {
        medicationName: form.medicationName,
        dosage: form.dosage,
        instructions: form.instructions,
        startDate: form.startDate || null,
        endDate: form.endDate || null
      });
      setToast("Prescription created.");
      setForm({ medicationName: "", dosage: "", instructions: "", startDate: "", endDate: "" });
      await load();
    } catch (err: any) {
      const msg = err instanceof ApiError ? err.message : "Create failed";
      setToast(msg);
    }
  };

  const updateStatus = async (prescriptionId: number, status: string) => {
    setToast(null);
    try {
      await api.prescriptions.updateStatus(token!, prescriptionId, status);
      setToast("Updated.");
      await load();
    } catch (err: any) {
      const msg = err instanceof ApiError ? err.message : "Update failed";
      setToast(msg);
    }
  };

  const requestPrescription = () => {
    if (!requestedDrug.trim()) return;
    setToast(`Prescription request for ${requestedDrug.trim()} was sent.`);
    setRequestedDrug("");
  };

  return (
    <Layout title="Prescription Management">
      <Toast message={toast} onClose={() => setToast(null)} />

      {me?.role !== "PATIENT" && (
        <div className="card">
          <div className="row">
            <Input label="Patient ID" value={patientId} onChange={(e) => setPatientId(e.target.value)} placeholder="Example: 1" />
            <Button variant="secondary" onClick={load} disabled={!patientId}>
              Load
            </Button>
          </div>
          <p className="muted small">Tip: use Patient Search to find the patient ID.</p>
        </div>
      )}

      {canCreate && (
        <form className="card" onSubmit={create}>
          <h2 className="sectionTitle">Create prescription</h2>
          <div className="grid2">
            <Input label="Medication" value={form.medicationName} onChange={(e) => setForm({ ...form, medicationName: e.target.value })} />
            <Input label="Dosage" value={form.dosage} onChange={(e) => setForm({ ...form, dosage: e.target.value })} />
          </div>
          <TextArea
            label="Instructions"
            value={form.instructions}
            onChange={(e) => setForm({ ...form, instructions: e.target.value })}
            rows={4}
          />
          <div className="grid2">
            <Input label="Start date" type="date" value={form.startDate} onChange={(e) => setForm({ ...form, startDate: e.target.value })} />
            <Input label="End date" type="date" value={form.endDate} onChange={(e) => setForm({ ...form, endDate: e.target.value })} />
          </div>
          <Button type="submit" disabled={!patientId || !form.medicationName || !form.dosage || !form.instructions}>
            Create
          </Button>
        </form>
      )}

      {canRequestPrescription && (
        <div className="card">
          <h2 className="sectionTitle">Request prescription</h2>
          <div className="row">
            <Input
              label="Drug name"
              value={requestedDrug}
              onChange={(e) => setRequestedDrug(e.target.value)}
              placeholder="Request prescription for this drug"
            />
            <Button variant="secondary" onClick={requestPrescription} disabled={!requestedDrug.trim()}>
              Send request
            </Button>
          </div>
        </div>
      )}

      <div className="card">
        <h2 className="sectionTitle">Prescriptions</h2>
        <table className="table">
          <thead>
            <tr>
              <th>Medication</th>
              <th>Dosage</th>
              <th>Status</th>
              <th>Start</th>
              <th>End</th>
              {canUpdateStatus && <th></th>}
            </tr>
          </thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.id}>
                <td>{r.medicationName}</td>
                <td className="muted">{r.dosage}</td>
                <td>
                  <span className="badge">{r.status}</span>
                </td>
                <td>{r.startDate ?? "-"}</td>
                <td>{r.endDate ?? "-"}</td>
                {canUpdateStatus && (
                  <td>
                    <Select
                      label=""
                      value={r.status}
                      onChange={(e) => updateStatus(r.id, e.target.value)}
                    >
                      {STATUS.map((s) => (
                        <option key={s} value={s}>
                          {s}
                        </option>
                      ))}
                    </Select>
                  </td>
                )}
              </tr>
            ))}
            {rows.length === 0 && (
              <tr>
                <td colSpan={canUpdateStatus ? 6 : 5} className="muted">
                  {me?.role === "PATIENT" ? "No prescriptions found." : "Enter a patient ID to load prescriptions."}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </Layout>
  );
}
