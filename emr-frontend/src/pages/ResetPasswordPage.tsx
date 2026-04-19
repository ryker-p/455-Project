import { FormEvent, useState } from "react";
import { Link } from "react-router-dom";
import Button from "../components/Button";
import Input from "../components/Input";
import Toast from "../components/Toast";
import { api } from "../lib/api";
import { ApiError } from "../lib/http";

export default function ResetPasswordPage() {
  const [email, setEmail] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState<string | null>(null);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setToast(null);
    try {
      await api.auth.resetPassword(email, newPassword);
      setToast("Password updated. You can sign in now.");
      setNewPassword("");
    } catch (err: any) {
      const msg = err instanceof ApiError ? err.message : "Reset failed";
      setToast(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="authPage">
      <Toast message={toast} onClose={() => setToast(null)} />
      <form className="card authCard" onSubmit={onSubmit}>
        <h1 className="authTitle">Reset password</h1>
        <p className="muted">Demo reset: no email token required.</p>
        <Input label="Email" value={email} onChange={(e) => setEmail(e.target.value)} />
        <Input
          label="New password"
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
          type="password"
        />
        <Button disabled={loading} type="submit">
          {loading ? "Updating..." : "Update password"}
        </Button>
        <div className="authLinks">
          <Link to="/login">Back to login</Link>
        </div>
      </form>
    </div>
  );
}

