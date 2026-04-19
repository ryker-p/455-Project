import { FormEvent, useEffect, useState } from "react";
import Layout from "../components/Layout";
import Toast from "../components/Toast";
import { api, Billing, Insurance, Role } from "../lib/api";
import { ApiError } from "../lib/http";
import { useAuth } from "../state/AuthContext";
import Input from "../components/Input";
import Button from "../components/Button";
import Select from "../components/Select";

const BILLING_STATUS = ["OPEN", "PAID", "VOID"];

export default function BillingInsurancePage() {
  const { token, me } = useAuth();
  const [toast, setToast] = useState<string | null>(null);
  const [patientId, setPatientId] = useState<string>("");
  const [bills, setBills] = useState<Billing[]>([]);
  const [insurance, setInsurance] = useState<Insurance[]>([]);
  const [billForm, setBillForm] = useState({
    amount: "",
    dueDate: "",
    description: "",
    appointmentId: ""
  });
  const [insForm, setInsForm] = useState({
    providerName: "",
    policyNumber: "",
    groupNumber: "",
    effectiveDate: "",
    expirationDate: ""
  });

  const isPatient = (me?.role as Role) === "PATIENT";
  const canEdit = me?.role === "ADMIN" || me?.role === "NURSE";

  const load = async () => {
    setToast(null);
    try {
      if (isPatient) {
        const [b, i] = await Promise.all([api.billing.my(token!), api.insurance.my(token!)]);
        setBills(b);
        setInsurance(i);
      } else if (patientId) {
        const [b, i] = await Promise.all([
          api.billing.forPatient(token!, Number(patientId)),
          api.insurance.forPatient(token!, Number(patientId))
        ]);
        setBills(b);
        setInsurance(i);
      } else {
        setBills([]);
        setInsurance([]);
      }
    } catch (err: any) {
      const msg = err instanceof ApiError ? err.message : "Failed to load billing/insurance";
      setToast(msg);
    }
  };

  useEffect(() => {
    load();
  }, [token, me?.role, patientId]);

  const createBill = async (e: FormEvent) => {
    e.preventDefault();
    if (!patientId) return;
    setToast(null);
    try {
      await api.billing.create(token!, {
        patientId: Number(patientId),
        amount: billForm.amount,
        dueDate: billForm.dueDate,
        description: billForm.description || null,
        appointmentId: billForm.appointmentId ? Number(billForm.appointmentId) : null
      });
      setToast("Billing record created.");
      setBillForm({ amount: "", dueDate: "", description: "", appointmentId: "" });
      await load();
    } catch (err: any) {
      const msg = err instanceof ApiError ? err.message : "Create failed";
      setToast(msg);
    }
  };

  const upsertInsurance = async (e: FormEvent) => {
    e.preventDefault();
    if (!patientId) return;
    setToast(null);
    try {
      await api.insurance.upsert(token!, Number(patientId), {
        providerName: insForm.providerName,
        policyNumber: insForm.policyNumber,
        groupNumber: insForm.groupNumber || null,
        effectiveDate: insForm.effectiveDate,
        expirationDate: insForm.expirationDate || null
      });
      setToast("Insurance saved.");
      await load();
    } catch (err: any) {
      const msg = err instanceof ApiError ? err.message : "Save failed";
      setToast(msg);
    }
  };

  const updateBillStatus = async (billingId: number, status: string) => {
    setToast(null);
    try {
      await api.billing.updateStatus(token!, billingId, status);
      setToast("Updated.");
      await load();
    } catch (err: any) {
      const msg = err instanceof ApiError ? err.message : "Update failed";
      setToast(msg);
    }
  };

  return (
    <Layout title="Billing & Insurance">
      <Toast message={toast} onClose={() => setToast(null)} />

      {!isPatient && (
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

      {canEdit && (
        <div className="grid2">
          <form className="card" onSubmit={createBill}>
            <h2 className="sectionTitle">Create billing record</h2>
            <Input label="Amount" value={billForm.amount} onChange={(e) => setBillForm({ ...billForm, amount: e.target.value })} placeholder="25.00" />
            <Input label="Due date" type="date" value={billForm.dueDate} onChange={(e) => setBillForm({ ...billForm, dueDate: e.target.value })} />
            <Input label="Appointment ID (optional)" value={billForm.appointmentId} onChange={(e) => setBillForm({ ...billForm, appointmentId: e.target.value })} />
            <Input label="Description" value={billForm.description} onChange={(e) => setBillForm({ ...billForm, description: e.target.value })} />
            <Button type="submit" disabled={!patientId || !billForm.amount || !billForm.dueDate}>
              Create
            </Button>
          </form>

          <form className="card" onSubmit={upsertInsurance}>
            <h2 className="sectionTitle">Upsert insurance</h2>
            <Input label="Provider" value={insForm.providerName} onChange={(e) => setInsForm({ ...insForm, providerName: e.target.value })} />
            <Input label="Policy #" value={insForm.policyNumber} onChange={(e) => setInsForm({ ...insForm, policyNumber: e.target.value })} />
            <Input label="Group #" value={insForm.groupNumber} onChange={(e) => setInsForm({ ...insForm, groupNumber: e.target.value })} />
            <Input label="Effective date" type="date" value={insForm.effectiveDate} onChange={(e) => setInsForm({ ...insForm, effectiveDate: e.target.value })} />
            <Input label="Expiration date" type="date" value={insForm.expirationDate} onChange={(e) => setInsForm({ ...insForm, expirationDate: e.target.value })} />
            <Button
              type="submit"
              disabled={!patientId || !insForm.providerName || !insForm.policyNumber || !insForm.effectiveDate}
            >
              Save insurance
            </Button>
          </form>
        </div>
      )}

      <div className="grid2">
        <div className="card">
          <h2 className="sectionTitle">Billing</h2>
          <table className="table">
            <thead>
              <tr>
                <th>Amount</th>
                <th>Due</th>
                <th>Status</th>
                {canEdit && <th></th>}
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
                  {canEdit && (
                    <td>
                      <Select label="" value={b.status} onChange={(e) => updateBillStatus(b.id, e.target.value)}>
                        {BILLING_STATUS.map((s) => (
                          <option key={s} value={s}>
                            {s}
                          </option>
                        ))}
                      </Select>
                    </td>
                  )}
                </tr>
              ))}
              {bills.length === 0 && (
                <tr>
                  <td colSpan={canEdit ? 4 : 3} className="muted">
                    {isPatient ? "No billing records." : "Enter a patient ID to load billing."}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="card">
          <h2 className="sectionTitle">Insurance</h2>
          <table className="table">
            <thead>
              <tr>
                <th>Provider</th>
                <th>Policy #</th>
                <th>Effective</th>
              </tr>
            </thead>
            <tbody>
              {insurance.map((i) => (
                <tr key={i.id}>
                  <td>{i.providerName}</td>
                  <td className="muted">{i.policyNumber}</td>
                  <td>{i.effectiveDate}</td>
                </tr>
              ))}
              {insurance.length === 0 && (
                <tr>
                  <td colSpan={3} className="muted">
                    {isPatient ? "No insurance on file." : "Enter a patient ID to load insurance."}
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

