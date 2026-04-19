import React from "react";

export default function Input({
  label,
  ...props
}: React.InputHTMLAttributes<HTMLInputElement> & { label: string }) {
  return (
    <label className="field">
      <div className="label">{label}</div>
      <input {...props} className={`input ${props.className ?? ""}`.trim()} />
    </label>
  );
}

