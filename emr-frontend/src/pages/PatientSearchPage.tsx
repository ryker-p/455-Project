import { useEffect, useState } from "react";
import Layout from "../components/Layout";
import Input from "../components/Input";
import Button from "../components/Button";
import Toast from "../components/Toast";
import { api, PatientProfile, PatientSearch } from "../lib/api";
import { ApiError } from "../lib/http";
import { useAuth } from "../state/AuthContext";

export default function PatientSearchPage() {
  const { token } = useAuth();
  const [q, setQ] = useState("");
  const [dob, setDob] = useState("");
  const [patientId, setPatientId] = useState("");
  const [rows, setRows] = useState<PatientSearch[]>([]);
  const [selected, setSelected] = useState<PatientProfile | null>(null);
  const [toast, setToast] = useState<string | null>(null);

  useEffect(() => {
    setSelected(null);
  }, [q, dob, patientId]);

  const run = async () => {
    setToast(null);
    setSelected(null);
    try {
      const r = await api.patients.search(token!, {
        q: q.trim() || undefined,
        dob: dob || undefined,
        patientId: patientId.trim() || undefined
      });
      setRows(r);
    } catch (err: any) {
      const msg = err instanceof ApiError ? err.message : "Search failed";
      setToast(msg);
    }
  };

  const open = async (patientId: number) => {
    setToast(null);
    try {
      const p = await api.patients.getById(token!, patientId);
      setSelected(p);
    } catch (err: any) {
      const msg = err instanceof ApiError ? err.message : "Failed to load patient";
      setToast(msg);
    }
  };

  return (
    <Layout title="Patient Search">
      <Toast message={toast} onClose={() => setToast(null)} />
      <div className="card">
        <div className="grid3">
          <Input label="Name / Email / Username" value={q} onChange={(e) => setQ(e.target.value)} />
          <Input label="Date of birth" type="date" value={dob} onChange={(e) => setDob(e.target.value)} />
          <Input label="Patient ID" value={patientId} onChange={(e) => setPatientId(e.target.value)} placeholder="Example: 1" />
        </div>
        <div style={{ marginTop: 10 }}>
          <Button onClick={run} variant="secondary" disabled={!q.trim() && !dob && !patientId.trim()}>
            Search
          </Button>
        </div>
        <table className="table">
          <thead>
            <tr>
              <th>Patient</th>
              <th>DOB</th>
              <th>Email</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.patientId}>
                <td>
                  {r.firstName} {r.lastName}
                </td>
                <td>{r.dateOfBirth ?? "-"}</td>
                <td>{r.email}</td>
                <td>
                  <Button variant="secondary" onClick={() => open(r.patientId)}>
                    Open
                  </Button>
                </td>
              </tr>
            ))}
            {rows.length === 0 && (
              <tr>
                <td colSpan={4} className="muted">
                  Search to see results.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {selected && (
        <div className="card">
          <h2 className="sectionTitle">Patient Record</h2>
          <div className="grid3">
            <div>
              <div className="muted small">Name</div>
              <div>
                {selected.firstName} {selected.lastName}
              </div>
            </div>
            <div>
              <div className="muted small">DOB</div>
              <div>{selected.dateOfBirth ?? "-"}</div>
            </div>
            <div>
              <div className="muted small">Email</div>
              <div>{selected.email}</div>
            </div>
          </div>
          <div className="grid3">
            <div>
              <div className="muted small">Phone</div>
              <div>{selected.phone ?? "-"}</div>
            </div>
            <div>
              <div className="muted small">Address</div>
              <div>
                {(selected.addressLine1 ?? "-") + (selected.city ? `, ${selected.city}` : "")}
              </div>
            </div>
            <div>
              <div className="muted small">Patient ID</div>
              <div>{selected.patientId}</div>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}
