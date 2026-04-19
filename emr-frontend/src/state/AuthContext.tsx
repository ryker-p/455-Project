import React, { createContext, useContext, useEffect, useMemo, useState } from "react";
import { api, MeResponse, Role } from "../lib/api";

type AuthState = {
  token: string | null;
  me: MeResponse | null;
  login: (token: string, me: MeResponse) => void;
  logout: () => void;
  refreshMe: () => Promise<void>;
  hasRole: (role: Role) => boolean;
};

const AuthContext = createContext<AuthState | null>(null);

const TOKEN_KEY = "emr.token";
const ME_KEY = "emr.me";
const IDLE_MS = 15 * 60 * 1000;

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY));
  const [me, setMe] = useState<MeResponse | null>(() => {
    const raw = localStorage.getItem(ME_KEY);
    return raw ? (JSON.parse(raw) as MeResponse) : null;
  });

  useEffect(() => {
    if (!token || me) return;
    (async () => {
      try {
        const nextMe = await api.users.me(token);
        setMe(nextMe);
      } catch {
        setToken(null);
        setMe(null);
      }
    })();
  }, [token, me]);

  useEffect(() => {
    if (!token) return;
    let last = Date.now();
    const touch = () => {
      last = Date.now();
    };
    const onVisibility = () => {
      if (!document.hidden) touch();
    };
    window.addEventListener("mousemove", touch);
    window.addEventListener("keydown", touch);
    window.addEventListener("click", touch);
    document.addEventListener("visibilitychange", onVisibility);
    const timer = window.setInterval(() => {
      if (Date.now() - last > IDLE_MS) {
        setToken(null);
        setMe(null);
      }
    }, 30_000);
    return () => {
      window.removeEventListener("mousemove", touch);
      window.removeEventListener("keydown", touch);
      window.removeEventListener("click", touch);
      document.removeEventListener("visibilitychange", onVisibility);
      window.clearInterval(timer);
    };
  }, [token]);

  useEffect(() => {
    if (token) localStorage.setItem(TOKEN_KEY, token);
    else localStorage.removeItem(TOKEN_KEY);
  }, [token]);

  useEffect(() => {
    if (me) localStorage.setItem(ME_KEY, JSON.stringify(me));
    else localStorage.removeItem(ME_KEY);
  }, [me]);

  const login = (nextToken: string, nextMe: MeResponse) => {
    setToken(nextToken);
    setMe(nextMe);
  };

  const logout = () => {
    setToken(null);
    setMe(null);
  };

  const refreshMe = async () => {
    if (!token) return;
    const nextMe = await api.users.me(token);
    setMe(nextMe);
  };

  const hasRole = (role: Role) => (me?.role ?? null) === role;

  const value = useMemo(
    () => ({ token, me, login, logout, refreshMe, hasRole }),
    [token, me]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("AuthContext missing");
  return ctx;
}
