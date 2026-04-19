import React from "react";

export default function TextArea({
  label,
  ...props
}: React.TextareaHTMLAttributes<HTMLTextAreaElement> & { label: string }) {
  return (
    <label className="field">
      <div className="label">{label}</div>
      <textarea {...props} className={`input textarea ${props.className ?? ""}`.trim()} />
    </label>
  );
}

