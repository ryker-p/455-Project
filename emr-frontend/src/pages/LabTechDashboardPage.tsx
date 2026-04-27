import Layout from "../components/Layout";

export default function LabTechDashboardPage() {
  return (
    <Layout title="Lab Technician Dashboard">
      <div className="grid2">
        <div className="card">
          <h2 className="sectionTitle">Lab workflow</h2>
          <p className="muted">1. Use Patient Search to find the patient.</p>
          <p className="muted">2. Copy the numeric Patient ID from the patient record card.</p>
          <p className="muted">3. Open Medical History / Tests to review history and add a new test result.</p>
        </div>
        <div className="card">
          <h2 className="sectionTitle">Role access</h2>
          <p className="muted">Lab techs can search patients, view history/test data, and add test results.</p>
          <p className="muted">Prescriptions, billing, reports, and user management stay restricted to other roles.</p>
        </div>
      </div>
    </Layout>
  );
}
