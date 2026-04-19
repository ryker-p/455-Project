import React from "react";

export default function Button({
  children,
  variant = "primary",
  ...props
}: React.ButtonHTMLAttributes<HTMLButtonElement> & { variant?: "primary" | "secondary" | "danger" }) {
  return (
    <button {...props} className={`btn btn-${variant} ${props.className ?? ""}`.trim()}>
      {children}
    </button>
  );
}

