import { FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Button from "../components/Button";
import Input from "../components/Input";
import Select from "../components/Select";
import Toast from "../components/Toast";
import { api } from "../lib/api";
import { ApiError } from "../lib/http";
import { useAuth } from "../state/AuthContext";

export default function RegisterPage() {
  const { login } = useAuth();
  const nav = useNavigate();
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState<string | null>(null);

  const [form, setForm] = useState({
    username: "", firstName: "", lastName: "", email: "", password: "",
    dateOfBirth: "", ssn: "", phone: "", address: "", sex: "",
    insuranceProvider: "", policyNumber: "", groupNumber: "", effectiveDate: ""
  });

  const set = (field: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) =>
    setForm({ ...form, [field]: e.target.value });

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (form.password.length < 8) { setToast("Password must be at least 8 characters."); return; }
    setLoading(true);
    setToast(null);
    try {
      const res = await api.auth.register(
        form.username, form.email, form.password, form.firstName, form.lastName,
        form.dateOfBirth || undefined, form.ssn || undefined, form.phone || undefined,
        form.address || undefined, form.sex || undefined,
        form.insuranceProvider || undefined, form.policyNumber || undefined,
        form.groupNumber || undefined, form.effectiveDate || undefined
      );
      login(res.token, res.me);
      nav("/", { replace: true });
    } catch (err: any) {
      setToast(err instanceof ApiError ? err.message : "Registration failed");
    } finally {
      setLoading(false);
    }
  };

  const disabled = loading || !form.username || !form.firstName || !form.lastName || !form.email || form.password.length < 8;

  return (
    <div className="authPage">
      <Toast message={toast} onClose={() => setToast(null)} />
      <form className="card authCard" onSubmit={onSubmit}>
        <h1 className="authTitle">Create account</h1>
        <p className="muted">Registration creates a Patient role account.</p>

        <h2 className="sectionTitle">Account Info</h2>
        <Input label="Username" value={form.username} onChange={set("username")} autoComplete="username" />
        <div className="grid2">
          <Input label="First name" value={form.firstName} onChange={set("firstName")} />
          <Input label="Last name" value={form.lastName} onChange={set("lastName")} />
        </div>
        <Input label="Email" value={form.email} onChange={set("email")} autoComplete="email" />
        <Input label="Password (min 8 chars)" value={form.password} onChange={set("password")} type="password" autoComplete="new-password" />

        <h2 className="sectionTitle">Personal Info</h2>
        <Input label="Date of Birth" value={form.dateOfBirth} onChange={set("dateOfBirth")} type="date" />
        <Select label="Gender" value={form.sex} onChange={set("sex")}>
          <option value="">-- Select --</option>
          <option value="Male">Male</option>
          <option value="Female">Female</option>
          <option value="Non-binary">Non-binary</option>
          <option value="Prefer not to say">Prefer not to say</option>
        </Select>
        <Input label="SSN (e.g. 123-45-6789)" value={form.ssn} onChange={set("ssn")} />
        <Input label="Phone Number" value={form.phone} onChange={set("phone")} type="tel" />
        <Input label="Address" value={form.address} onChange={set("address")} />

        <h2 className="sectionTitle">Insurance (optional)</h2>
        <Input label="Provider" value={form.insuranceProvider} onChange={set("insuranceProvider")} />
        <div className="grid2">
          <Input label="Policy #" value={form.policyNumber} onChange={set("policyNumber")} />
          <Input label="Group #" value={form.groupNumber} onChange={set("groupNumber")} />
        </div>
        <Input label="Effective Date" value={form.effectiveDate} onChange={set("effectiveDate")} type="date" />

        <Button disabled={disabled} type="submit">
          {loading ? "Creating..." : "Create account"}
        </Button>

        <div className="authLinks">
          <Link to="/login">Back to login</Link>
        </div>
      </form>
    </div>
  );
}
