import { useEffect, useState } from "react";
import Layout from "../components/Layout";
import Toast from "../components/Toast";
import { api, ReportSummary } from "../lib/api";
import { ApiError } from "../lib/http";
import { useAuth } from "../state/AuthContext";

export default function AdminDashboardPage() {
  const { token } = useAuth();
  const [summary, setSummary] = useState<ReportSummary | null>(null);
  const [toast, setToast] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      try {
        const s = await api.reports.summary(token!);
        setSummary(s);
      } catch (err: any) {
        const msg = err instanceof ApiError ? err.message : "Failed to load admin dashboard";
        setToast(msg);
      }
    })();
  }, [token]);

  return (
    <Layout title="Admin Dashboard">
      <Toast message={toast} onClose={() => setToast(null)} />
      <div className="grid3">
        <div className="card">
          <div className="kpiLabel">Users</div>
          <div className="kpiValue">{summary?.users ?? "-"}</div>
        </div>
        <div className="card">
          <div className="kpiLabel">Patients</div>
          <div className="kpiValue">{summary?.patients ?? "-"}</div>
        </div>
        <div className="card">
          <div className="kpiLabel">Open bills</div>
          <div className="kpiValue">{summary?.openBills ?? "-"}</div>
        </div>
      </div>
      <div className="card">
        <p className="muted">
          Admin tools: Access Logs, Reports, and User Management (role assignment).
        </p>
      </div>
    </Layout>
  );
}
