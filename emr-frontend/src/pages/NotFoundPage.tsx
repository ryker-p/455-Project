import { Link } from "react-router-dom";

export default function NotFoundPage() {
  return (
    <div className="authPage">
      <div className="card authCard">
        <h1 className="authTitle">Page not found</h1>
        <p className="muted">The page you requested does not exist.</p>
        <Link to="/">Go home</Link>
      </div>
    </div>
  );
}

