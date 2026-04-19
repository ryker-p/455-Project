import { useEffect, useState } from "react";
import Layout from "../components/Layout";
import Toast from "../components/Toast";
import { api, AccessLogActionReportRow, BillingStatusReportRow, DoctorAppointmentReportRow, ReportSummary } from "../lib/api";
import { ApiError } from "../lib/http";
import { useAuth } from "../state/AuthContext";

export default function ReportsPage() {
  const { token } = useAuth();
  const [summary, setSummary] = useState<ReportSummary | null>(null);
  const [appt, setAppt] = useState<DoctorAppointmentReportRow[]>([]);
  const [billing, setBilling] = useState<BillingStatusReportRow[]>([]);
  const [actions, setActions] = useState<AccessLogActionReportRow[]>([]);
  const [toast, setToast] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      try {
        const [s, a, b, al] = await Promise.all([
          api.reports.summary(token!),
          api.reports.appointmentsByDoctor(token!),
          api.reports.billingStatus(token!),
          api.reports.accessLogActions(token!)
        ]);
        setSummary(s);
        setAppt(a);
        setBilling(b);
        setActions(al);
      } catch (err: any) {
        const msg = err instanceof ApiError ? err.message : "Failed to load reports";
        setToast(msg);
      }
    })();
  }, [token]);

  return (
    <Layout title="Reports">
      <Toast message={toast} onClose={() => setToast(null)} />
      <div className="grid3">
        <div className="card">
          <div className="kpiLabel">Appointments</div>
          <div className="kpiValue">{summary?.appointments ?? "-"}</div>
        </div>
        <div className="card">
          <div className="kpiLabel">Prescriptions</div>
          <div className="kpiValue">{summary?.prescriptions ?? "-"}</div>
        </div>
        <div className="card">
          <div className="kpiLabel">Doctors</div>
          <div className="kpiValue">{summary?.doctors ?? "-"}</div>
        </div>
      </div>
      <div className="card">
        <h2 className="sectionTitle">Summary</h2>
        <div className="grid2">
          <div className="muted">Users</div>
          <div>{summary?.users ?? "-"}</div>
          <div className="muted">Patients</div>
          <div>{summary?.patients ?? "-"}</div>
          <div className="muted">Open bills</div>
          <div>{summary?.openBills ?? "-"}</div>
        </div>
      </div>

      <div className="grid2">
        <div className="card">
          <h2 className="sectionTitle">Appointments by doctor</h2>
          <table className="table">
            <thead>
              <tr>
                <th>Doctor</th>
                <th>Total</th>
                <th>Scheduled</th>
                <th>Completed</th>
              </tr>
            </thead>
            <tbody>
              {appt.map((r) => (
                <tr key={r.doctorId}>
                  <td>{r.doctorName}</td>
                  <td>{r.total}</td>
                  <td className="muted">{r.scheduled + r.confirmed}</td>
                  <td className="muted">{r.completed}</td>
                </tr>
              ))}
              {appt.length === 0 && (
                <tr>
                  <td colSpan={4} className="muted">
                    No appointment data.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="card">
          <h2 className="sectionTitle">Billing status</h2>
          <table className="table">
            <thead>
              <tr>
                <th>Status</th>
                <th>Count</th>
              </tr>
            </thead>
            <tbody>
              {billing.map((r) => (
                <tr key={r.status}>
                  <td>{r.status}</td>
                  <td>{r.count}</td>
                </tr>
              ))}
              {billing.length === 0 && (
                <tr>
                  <td colSpan={2} className="muted">
                    No billing data.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      <div className="card">
        <h2 className="sectionTitle">Access log actions</h2>
        <table className="table">
          <thead>
            <tr>
              <th>Action</th>
              <th>Count</th>
            </tr>
          </thead>
          <tbody>
            {actions.slice(0, 25).map((r) => (
              <tr key={r.action}>
                <td>{r.action}</td>
                <td>{r.count}</td>
              </tr>
            ))}
            {actions.length === 0 && (
              <tr>
                <td colSpan={2} className="muted">
                  No access log data.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </Layout>
  );
}
