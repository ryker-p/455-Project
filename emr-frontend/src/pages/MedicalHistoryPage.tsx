import { FormEvent, useEffect, useState } from "react";
import Layout from "../components/Layout";
import Toast from "../components/Toast";
import { api, MedicalHistory, TestResult } from "../lib/api";
import { ApiError } from "../lib/http";
import { useAuth } from "../state/AuthContext";
import Input from "../components/Input";
import TextArea from "../components/TextArea";
import Button from "../components/Button";

export default function MedicalHistoryPage() {
  const { token, me } = useAuth();
  const [toast, setToast] = useState<string | null>(null);
  const [patientId, setPatientId] = useState<string>("");
  const [history, setHistory] = useState<MedicalHistory[]>([]);
  const [tests, setTests] = useState<TestResult[]>([]);
  const [hxForm, setHxForm] = useState({ conditionName: "", notes: "" });
  const [testForm, setTestForm] = useState({ testName: "", resultValue: "", resultDate: "", notes: "" });

  const isPatient = me?.role === "PATIENT";
  const canAddHistory = me?.role === "DOCTOR" || me?.role === "ADMIN";
  const canAddTest = me?.role === "DOCTOR" || me?.role === "NURSE" || me?.role === "ADMIN" || me?.role === "LABTECH";
  const normalizedPatientId = patientId.trim();
  const hasValidPatientId = /^\d+$/.test(normalizedPatientId);

  const load = async () => {
    setToast(null);
    try {
      if (isPatient) {
        const [h, t] = await Promise.all([api.medicalHistory.my(token!), api.testResults.my(token!)]);
        setHistory(h);
        setTests(t);
      } else if (hasValidPatientId) {
        if (me?.role === "LABTECH") {
          const t = await api.testResults.forPatient(token!, Number(normalizedPatientId));
          setHistory([]);
          setTests(t);
        } else {
          const [h, t] = await Promise.all([
            api.medicalHistory.forPatient(token!, Number(normalizedPatientId)),
            api.testResults.forPatient(token!, Number(normalizedPatientId))
          ]);
          setHistory(h);
          setTests(t);
        }
      } else {
        setHistory([]);
        setTests([]);
      }
    } catch (err: any) {
      const msg = err instanceof ApiError ? err.message : "Failed to load history/tests";
      setToast(msg);
    }
  };

  useEffect(() => {
    load();
  }, [token, me?.role, patientId]);

  const addHistory = async (e: FormEvent) => {
    e.preventDefault();
    if (!hasValidPatientId) return;
    setToast(null);
    try {
      await api.medicalHistory.add(token!, Number(normalizedPatientId), { conditionName: hxForm.conditionName, notes: hxForm.notes || null });
      setToast("Added medical history.");
      setHxForm({ conditionName: "", notes: "" });
      await load();
    } catch (err: any) {
      const msg = err instanceof ApiError ? err.message : "Add failed";
      setToast(msg);
    }
  };

  const addTest = async (e: FormEvent) => {
    e.preventDefault();
    if (!hasValidPatientId) return;
    setToast(null);
    try {
      await api.testResults.add(token!, Number(normalizedPatientId), {
        testName: testForm.testName,
        resultValue: testForm.resultValue,
        resultDate: testForm.resultDate || null,
        notes: testForm.notes || null
      });
      setToast("Added test result.");
      setTestForm({ testName: "", resultValue: "", resultDate: "", notes: "" });
      await load();
    } catch (err: any) {
      const msg = err instanceof ApiError ? err.message : "Add failed";
      setToast(msg);
    }
  };

  return (
    <Layout title="Medical History">
      <Toast message={toast} onClose={() => setToast(null)} />

      {!isPatient && (
        <div className="card">
          <div className="row">
            <Input label="Patient ID" value={patientId} onChange={(e) => setPatientId(e.target.value)} placeholder="Example: 1" />
            <Button variant="secondary" onClick={load} disabled={!hasValidPatientId}>
              Load
            </Button>
          </div>
          <p className="muted small">Use the numeric Patient ID from Patient Search.</p>
        </div>
      )}

      {canAddTest && !isPatient && (
        <div className="grid2">
          {canAddHistory ? (
            <form className="card" onSubmit={addHistory}>
              <h2 className="sectionTitle">Add medical history</h2>
              <Input label="Condition" value={hxForm.conditionName} onChange={(e) => setHxForm({ ...hxForm, conditionName: e.target.value })} />
              <TextArea label="Notes" value={hxForm.notes} onChange={(e) => setHxForm({ ...hxForm, notes: e.target.value })} rows={4} />
              <Button type="submit" disabled={!hasValidPatientId || !hxForm.conditionName}>
                Add
              </Button>
            </form>
          ) : (
            <div className="card">
              <h2 className="sectionTitle">Medical history</h2>
              <p className="muted">Only Doctors/Nurses can add diagnoses/history entries.</p>
            </div>
          )}
          <form className="card" onSubmit={addTest}>
            <h2 className="sectionTitle">Add test result</h2>
            <Input label="Test name" value={testForm.testName} onChange={(e) => setTestForm({ ...testForm, testName: e.target.value })} />
            <Input label="Result value" value={testForm.resultValue} onChange={(e) => setTestForm({ ...testForm, resultValue: e.target.value })} />
            <Input label="Result date" type="date" value={testForm.resultDate} onChange={(e) => setTestForm({ ...testForm, resultDate: e.target.value })} />
            <TextArea label="Notes" value={testForm.notes} onChange={(e) => setTestForm({ ...testForm, notes: e.target.value })} rows={3} />
            <Button type="submit" disabled={!hasValidPatientId || !testForm.testName || !testForm.resultValue}>
              Add
            </Button>
          </form>
        </div>
      )}

      <div className="grid2">
        {me?.role !== "LABTECH" && (
        <div className="card">
          <h2 className="sectionTitle">History</h2>
          <table className="table">
            <thead>
              <tr>
                <th>Condition</th>
                <th>Recorded</th>
                <th>Notes</th>
              </tr>
            </thead>
            <tbody>
              {history.map((h) => (
                <tr key={h.id}>
                  <td>{h.conditionName}</td>
                  <td className="muted">{new Date(h.recordedAt).toLocaleString()}</td>
                  <td className="muted">{h.notes ?? "-"}</td>
                </tr>
              ))}
              {history.length === 0 && (
                <tr>
                  <td colSpan={3} className="muted">
                    {isPatient ? "No medical history records." : "Enter a numeric patient ID to load medical history."}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        )}

        <div className="card">
          <h2 className="sectionTitle">Test results</h2>
          <table className="table">
            <thead>
              <tr>
                <th>Test</th>
                <th>Result</th>
                <th>Date</th>
              </tr>
            </thead>
            <tbody>
              {tests.map((t) => (
                <tr key={t.id}>
                  <td>{t.testName}</td>
                  <td className="muted">{t.resultValue}</td>
                  <td>{t.resultDate ?? "-"}</td>
                </tr>
              ))}
              {tests.length === 0 && (
                <tr>
                  <td colSpan={3} className="muted">
                    {isPatient ? "No test results." : "Enter a numeric patient ID to load test results."}
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
