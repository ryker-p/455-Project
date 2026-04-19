import Layout from "../components/Layout";

export default function LabTechDashboardPage() {
  return (
    <Layout title="Lab Technician Dashboard">
      <div className="card">
        <h2 className="sectionTitle">Quick start</h2>
        <p className="muted">
          Use Patient Search to locate a patient, then open Medical History to add test results.
        </p>
      </div>
    </Layout>
  );
}

