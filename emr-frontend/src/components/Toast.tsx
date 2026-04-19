import React, { useEffect } from "react";

export default function Toast({
  message,
  onClose
}: {
  message: string | null;
  onClose: () => void;
}) {
  useEffect(() => {
    if (!message) return;
    const t = window.setTimeout(onClose, 3500);
    return () => window.clearTimeout(t);
  }, [message, onClose]);

  if (!message) return null;
  return (
    <div className="toast" role="status" onClick={onClose}>
      {message}
    </div>
  );
}

