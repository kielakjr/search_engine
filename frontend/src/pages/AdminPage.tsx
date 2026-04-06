import { useEffect, useState } from "react";
import {
  getSources,
  createSource,
  deleteSource,
  startCrawl,
  getCrawlJobs,
} from "../api";
import type { SourceResponse, CrawlJobResponse } from "../types";

export default function AdminPage() {
  const [sources, setSources] = useState<SourceResponse[]>([]);
  const [jobs, setJobs] = useState<CrawlJobResponse[]>([]);
  const [name, setName] = useState("");
  const [url, setUrl] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  function loadData() {
    setLoading(true);
    Promise.all([getSources(), getCrawlJobs()])
      .then(([s, j]) => {
        setSources(s);
        setJobs(j);
      })
      .catch((e) => setError(e instanceof Error ? e.message : "Failed to load"))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    loadData();
  }, []);

  async function handleAddSource(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    try {
      await createSource(name, url);
      setName("");
      setUrl("");
      loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to create source");
    }
  }

  async function handleDelete(id: number) {
    try {
      await deleteSource(id);
      loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to delete");
    }
  }

  async function handleCrawl(sourceId: number) {
    try {
      await startCrawl(sourceId);
      loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to start crawl");
    }
  }

  if (loading) return <p>Loading...</p>;

  return (
    <div className="page admin-page">
      <h1>Admin</h1>
      {error && <p className="error">{error}</p>}

      <section>
        <h2>Add Source</h2>
        <form className="source-form" onSubmit={handleAddSource}>
          <input
            type="text"
            placeholder="Name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />
          <input
            type="url"
            placeholder="https://example.com"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            required
          />
          <button type="submit">Add</button>
        </form>
      </section>

      <section>
        <h2>Sources</h2>
        {sources.length === 0 ? (
          <p>No sources yet.</p>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>URL</th>
                <th>Active</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {sources.map((s) => (
                <tr key={s.id}>
                  <td>{s.name}</td>
                  <td>{s.url}</td>
                  <td>{s.active ? "Yes" : "No"}</td>
                  <td className="actions">
                    <button onClick={() => handleCrawl(s.id)}>Crawl</button>
                    <button
                      className="danger"
                      onClick={() => handleDelete(s.id)}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      <section>
        <h2>Crawl Jobs</h2>
        {jobs.length === 0 ? (
          <p>No crawl jobs yet.</p>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Source</th>
                <th>Status</th>
                <th>Pages</th>
                <th>Started</th>
              </tr>
            </thead>
            <tbody>
              {jobs.map((j) => (
                <tr key={j.id}>
                  <td>{j.sourceUrl}</td>
                  <td>
                    <span className={`status status-${j.status.toLowerCase()}`}>
                      {j.status}
                    </span>
                  </td>
                  <td>{j.pagesFound}</td>
                  <td>{new Date(j.createdAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </div>
  );
}
