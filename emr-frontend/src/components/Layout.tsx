import { Link, NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../state/AuthContext";
import Button from "./Button";

type Item = { to: string; label: string };

function navFor(role: string): Item[] {
  if (role === "PATIENT") {
    return [
      { to: "/patient", label: "Dashboard" },
      { to: "/patient/profile", label: "Profile" },
      { to: "/patient/schedule", label: "Schedule" },
      { to: "/patient/prescriptions", label: "Prescriptions" },
      { to: "/patient/billing", label: "Billing & Insurance" },
      { to: "/patient/history", label: "Medical History" }
    ];
  }
  if (role === "DOCTOR") {
    return [
      { to: "/doctor", label: "Dashboard" },
      { to: "/doctor/patients", label: "Patient Search" },
      { to: "/doctor/appointments", label: "Appointments" },
      { to: "/doctor/prescriptions", label: "Prescriptions" },
      { to: "/doctor/billing", label: "Billing" },
      { to: "/doctor/history", label: "History / Tests" }
    ];
  }
  if (role === "NURSE") {
    return [
      { to: "/nurse", label: "Dashboard" },
      { to: "/nurse/patients", label: "Patient Search" },
      { to: "/nurse/appointments", label: "Appointments" },
      { to: "/nurse/prescriptions", label: "Prescriptions" },
      { to: "/nurse/history", label: "History / Tests" }
    ];
  }
  if (role === "LABTECH") {
    return [
      { to: "/labtech", label: "Dashboard" },
      { to: "/labtech/patients", label: "Patient Search" },
      { to: "/labtech/history", label: "History / Tests" }
    ];
  }
  return [
    { to: "/admin", label: "Dashboard" },
    { to: "/admin/patients", label: "Patient Search" },
    { to: "/admin/billing", label: "Billing & Insurance" },
    { to: "/admin/access-logs", label: "Access Logs" },
    { to: "/admin/reports", label: "Reports" },
    { to: "/admin/users", label: "User Management" }
  ];
}

export default function Layout({ title, children }: { title: string; children: React.ReactNode }) {
  const { me, logout } = useAuth();
  const navigate = useNavigate();

  const items = navFor(me?.role ?? "PATIENT");

  return (
    <div className="appShell">
      <aside className="sidebar">
        <Link to="/" className="brand">
          EMR
        </Link>
        <nav className="nav">
          {items.map((i) => (
            <NavLink key={i.to} to={i.to} className={({ isActive }) => (isActive ? "navItem active" : "navItem")}>
              {i.label}
            </NavLink>
          ))}
        </nav>
        <div className="sidebarFooter">
          <div className="muted small">{me?.displayName}</div>
          <div className="muted small">{me?.email}</div>
          <Button
            variant="secondary"
            onClick={() => {
              logout();
              navigate("/login");
            }}
          >
            Sign out
          </Button>
        </div>
      </aside>

      <main className="content">
        <header className="topbar">
          <h1 className="pageTitle">{title}</h1>
        </header>
        <div className="page">{children}</div>
      </main>
    </div>
  );
}
