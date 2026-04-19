import { FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Button from "../components/Button";
import Input from "../components/Input";
import Toast from "../components/Toast";
import { api } from "../lib/api";
import { ApiError } from "../lib/http";
import { useAuth } from "../state/AuthContext";

export default function LoginPage() {
  const { login } = useAuth();
  const nav = useNavigate();
  const [identifier, setIdentifier] = useState("admin1");
  const [password, setPassword] = useState("Password123!");
  const [twoFactorCode, setTwoFactorCode] = useState("");
  const [needs2fa, setNeeds2fa] = useState(false);
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState<string | null>(null);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setToast(null);
    try {
      const res = await api.auth.login(identifier, password, needs2fa ? twoFactorCode : undefined);
      login(res.token, res.me);
      nav("/", { replace: true });
    } catch (err: any) {
      if (err instanceof ApiError && err.status === 428) {
        setNeeds2fa(true);
        setToast("2FA code required. Enter your 6-digit code and sign in again.");
      } else {
        const msg = err instanceof ApiError ? err.message : "Login failed";
        setToast(msg);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="authPage">
      <Toast message={toast} onClose={() => setToast(null)} />
      <form className="card authCard" onSubmit={onSubmit}>
        <h1 className="authTitle">Sign in</h1>
        <p className="muted">Use the demo accounts or create a new patient account.</p>

        <Input
          label="Username or Email"
          value={identifier}
          onChange={(e) => setIdentifier(e.target.value)}
          autoComplete="username"
        />
        <Input
          label="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          type="password"
          autoComplete="current-password"
        />
        {needs2fa && (
          <Input
            label="2FA code"
            value={twoFactorCode}
            onChange={(e) => setTwoFactorCode(e.target.value)}
            inputMode="numeric"
            placeholder="123456"
          />
        )}

        <Button disabled={loading} type="submit">
          {loading ? "Signing in..." : "Sign in"}
        </Button>

        <div className="authLinks">
          <Link to="/register">Create account</Link>
          <Link to="/reset-password">Reset password</Link>
        </div>
      </form>
    </div>
  );
}
