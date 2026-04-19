import { useEffect, useState } from "react";
import Layout from "../components/Layout";
import Toast from "../components/Toast";
import { api, AccessLog } from "../lib/api";
import { ApiError } from "../lib/http";
import { useAuth } from "../state/AuthContext";

export default function AccessLogsPage() {
  const { token } = useAuth();
  const [rows, setRows] = useState<AccessLog[]>([]);
  const [toast, setToast] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      try {
        const r = await api.accessLogs.list(token!);
        setRows(r);
      } catch (err: any) {
        const msg = err instanceof ApiError ? err.message : "Failed to load access logs";
        setToast(msg);
      }
    })();
  }, [token]);

  return (
    <Layout title="Access Logs">
      <Toast message={toast} onClose={() => setToast(null)} />
      <div className="card">
        <table className="table">
          <thead>
            <tr>
              <th>Time</th>
              <th>Actor</th>
              <th>Action</th>
              <th>Resource</th>
              <th>IP</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.id}>
                <td>{new Date(r.createdAt).toLocaleString()}</td>
                <td>{r.actorEmail}</td>
                <td>
                  <span className="badge">{r.action}</span>
                </td>
                <td className="muted">
                  {r.resourceType}
                  {r.resourceId ? ` #${r.resourceId}` : ""}
                </td>
                <td className="muted">{r.ipAddress ?? "-"}</td>
              </tr>
            ))}
            {rows.length === 0 && (
              <tr>
                <td colSpan={5} className="muted">
                  No access logs.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </Layout>
  );
}

