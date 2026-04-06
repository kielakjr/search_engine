import { useEffect, useState } from "react";
import { getSearchHistory } from "../api";
import type { SearchHistoryEntry } from "../types";

export default function HistoryPage() {
  const [history, setHistory] = useState<SearchHistoryEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    getSearchHistory()
      .then(setHistory)
      .catch((e) => setError(e instanceof Error ? e.message : "Failed to load"))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p>Loading...</p>;
  if (error) return <p className="error">{error}</p>;

  return (
    <div className="page">
      <h1>Search History</h1>
      {history.length === 0 ? (
        <p>No search history yet.</p>
      ) : (
        <table className="data-table">
          <thead>
            <tr>
              <th>Query</th>
              <th>Domain</th>
              <th>Time</th>
            </tr>
          </thead>
          <tbody>
            {history.map((h, i) => (
              <tr key={i}>
                <td>{h.query}</td>
                <td>{h.domain ?? "—"}</td>
                <td>{h.timestamp}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
