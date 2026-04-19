import { FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Button from "../components/Button";
import Input from "../components/Input";
import Toast from "../components/Toast";
import { api } from "../lib/api";
import { ApiError } from "../lib/http";
import { useAuth } from "../state/AuthContext";

export default function RegisterPage() {
  const { login } = useAuth();
  const nav = useNavigate();
  const [username, setUsername] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState<string | null>(null);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setToast(null);
    try {
      const res = await api.auth.register(username, email, password, firstName, lastName);
      login(res.token, res.me);
      nav("/", { replace: true });
    } catch (err: any) {
      const msg = err instanceof ApiError ? err.message : "Registration failed";
      setToast(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="authPage">
      <Toast message={toast} onClose={() => setToast(null)} />
      <form className="card authCard" onSubmit={onSubmit}>
        <h1 className="authTitle">Create account</h1>
        <p className="muted">Registration creates a Patient role account.</p>

        <Input label="Username" value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" />
        <div className="grid2">
          <Input label="First name" value={firstName} onChange={(e) => setFirstName(e.target.value)} />
          <Input label="Last name" value={lastName} onChange={(e) => setLastName(e.target.value)} />
        </div>
        <Input label="Email" value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email" />
        <Input
          label="Password (min 8 chars)"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          type="password"
          autoComplete="new-password"
        />

        <Button disabled={loading || !username || !firstName || !lastName || !email || !password} type="submit">
          {loading ? "Creating..." : "Create account"}
        </Button>

        <div className="authLinks">
          <Link to="/login">Back to login</Link>
        </div>
      </form>
    </div>
  );
}
