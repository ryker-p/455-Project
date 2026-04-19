import Layout from "../components/Layout";

export default function NurseDashboardPage() {
  return (
    <Layout title="Nurse Dashboard">
      <div className="card">
        <h2 className="sectionTitle">Quick start</h2>
        <p className="muted">
          Use Patient Search to find records. Nurses can view appointments, history/tests, billing, and insurance.
        </p>
      </div>
    </Layout>
  );
}

