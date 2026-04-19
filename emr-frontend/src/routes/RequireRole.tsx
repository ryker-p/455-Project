import { Navigate, Outlet } from "react-router-dom";
import { Role } from "../lib/api";
import { useAuth } from "../state/AuthContext";

export default function RequireRole({ role }: { role: Role }) {
  const { me } = useAuth();
  if (!me) return <Navigate to="/login" replace />;
  if (me.role !== role) return <Navigate to="/" replace />;
  return <Outlet />;
}

