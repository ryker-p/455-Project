import React from "react";

export default function Select({
  label,
  children,
  ...props
}: React.SelectHTMLAttributes<HTMLSelectElement> & { label: string; children: React.ReactNode }) {
  return (
    <label className="field">
      <div className="label">{label}</div>
      <select {...props} className={`input ${props.className ?? ""}`.trim()}>
        {children}
      </select>
    </label>
  );
}

