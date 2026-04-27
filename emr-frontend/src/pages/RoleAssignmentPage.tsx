import { FormEvent, useEffect, useState } from "react";
import Layout from "../components/Layout";
import Toast from "../components/Toast";
import { api, AdminCreateUserRequest, Role, TwoFactorSetup, UserListResponse } from "../lib/api";
import { ApiError } from "../lib/http";
import { useAuth } from "../state/AuthContext";
import Input from "../components/Input";
import Select from "../components/Select";
import Button from "../components/Button";

const ROLES: Role[] = ["PATIENT", "DOCTOR", "NURSE", "LABTECH", "ADMIN"];

export default function RoleAssignmentPage() {
  const { token } = useAuth();
  const [rows, setRows] = useState<UserListResponse[]>([]);
  const [toast, setToast] = useState<string | null>(null);
  const [setup, setSetup] = useState<TwoFactorSetup | null>(null);
  const [createForm, setCreateForm] = useState<AdminCreateUserRequest>({
    username: "",
    email: "",
    password: "",
    role: "PATIENT",
    firstName: "",
    lastName: ""
  });

  const load = async () => {
    setToast(null);
    try {
      const r = await api.users.list(token!);
      setRows(r);
    } catch (err: any) {
      const msg = err instanceof ApiError ? err.message : "Failed to load users";
      setToast(msg);
    }
  };

  useEffect(() => {
    load();
  }, [token]);

  const update = async (userId: number, role: Role) => {
    setToast(null);
    try {
      await api.users.updateRole(token!, userId, role);
      setToast("Role updated.");
      await load();
    } catch (err: any) {
      const msg = err instanceof ApiError ? err.message : "Update failed";
      setToast(msg);
    }
  };

  const create = async (e: FormEvent) => {
    e.preventDefault();
    setToast(null);
    try {
      await api.users.create(token!, createForm);
      setToast("User created.");
      setCreateForm({ username: "", email: "", password: "", role: "PATIENT", firstName: "", lastName: "" });
      await load();
    } catch (err: any) {
      const msg = err instanceof ApiError ? err.message : "Create failed";
      setToast(msg);
    }
  };

  const toggle2fa = async (userId: number, enabled: boolean) => {
    setToast(null);
    setSetup(null);
    try {
      const res = await api.users.updateTwoFactor(token!, userId, enabled);
      setSetup(res);
      setToast(enabled ? "2FA enabled." : "2FA disabled.");
      await load();
    } catch (err: any) {
      const msg = err instanceof ApiError ? err.message : "2FA update failed";
      setToast(msg);
    }
  };

  return (
    <Layout title="User Management / Role Assignment">
      <Toast message={toast} onClose={() => setToast(null)} />

      <form className="card" onSubmit={create}>
        <h2 className="sectionTitle">Create user (Admin)</h2>
        <Input label="Username" value={createForm.username} onChange={(e) => setCreateForm({ ...createForm, username: e.target.value })} />
        <div className="grid2">
          <Input label="First name" value={createForm.firstName} onChange={(e) => setCreateForm({ ...createForm, firstName: e.target.value })} />
          <Input label="Last name" value={createForm.lastName} onChange={(e) => setCreateForm({ ...createForm, lastName: e.target.value })} />
        </div>
        <div className="grid2">
          <Input label="Email" value={createForm.email} onChange={(e) => setCreateForm({ ...createForm, email: e.target.value })} />
          <Input label="Temp password" type="password" value={createForm.password} onChange={(e) => setCreateForm({ ...createForm, password: e.target.value })} />
        </div>
        <Select label="Role" value={createForm.role} onChange={(e) => setCreateForm({ ...createForm, role: e.target.value as Role })}>
          {ROLES.map((r) => (
            <option key={r} value={r}>
              {r}
            </option>
          ))}
        </Select>
        <Button
          type="submit"
          disabled={!createForm.username || !createForm.email || !createForm.password || !createForm.firstName || !createForm.lastName}
        >
          Create user
        </Button>
      </form>

      {setup && setup.enabled && (
        <div className="card">
          <h2 className="sectionTitle">2FA Setup</h2>
          <div className="muted small">User ID: {setup.userId}</div>
          <div className="muted small">Secret: {setup.secret}</div>
          <div className="muted small">otpauth URI: {setup.otpAuthUri}</div>
          <p className="muted small">
            Add this secret to an authenticator app (TOTP). Then the user must enter the 6-digit code at login.
          </p>
        </div>
      )}

      <div className="card">
        <h2 className="sectionTitle">Users</h2>
        <table className="table">
          <thead>
            <tr>
              <th>User</th>
              <th>Email</th>
              <th>Username</th>
              <th>Role</th>
              <th>2FA</th>
              <th>Enabled</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((u) => (
              <tr key={u.userId}>
                <td>{u.displayName}</td>
                <td className="muted">{u.email}</td>
                <td className="muted">{u.username}</td>
                <td style={{ width: 220 }}>
                  <Select label="" value={u.role} onChange={(e) => update(u.userId, e.target.value as Role)}>
                    {ROLES.map((r) => (
                      <option key={r} value={r}>
                        {r}
                      </option>
                    ))}
                  </Select>
                </td>
                <td style={{ width: 140 }}>
                  <Button
                    variant="secondary"
                    onClick={() => toggle2fa(u.userId, !u.twoFactorEnabled)}
                  >
                    {u.twoFactorEnabled ? "Disable" : "Enable"}
                  </Button>
                </td>
                <td>{u.enabled ? "Yes" : "No"}</td>
              </tr>
            ))}
            {rows.length === 0 && (
              <tr>
                <td colSpan={6} className="muted">
                  No users found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </Layout>
  );
}
