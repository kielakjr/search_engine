import { useState } from "react";
import { search } from "../api";
import type { SearchResult } from "../types";

export default function SearchPage() {
  const [query, setQuery] = useState("");
  const [domain, setDomain] = useState("");
  const [results, setResults] = useState<SearchResult[]>([]);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [error, setError] = useState("");

  async function handleSearch(p = 0) {
    if (!query.trim()) return;
    setLoading(true);
    setError("");
    try {
      const data = await search(query, p, 10, domain || undefined);
      setResults(data);
      setPage(p);
      setSearched(true);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Search failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page search-page">
      <h1>Search</h1>
      <form
        className="search-form"
        onSubmit={(e) => {
          e.preventDefault();
          handleSearch(0);
        }}
      >
        <input
          type="text"
          placeholder="Search..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <input
          type="text"
          placeholder="Filter by domain (optional)"
          value={domain}
          onChange={(e) => setDomain(e.target.value)}
          className="domain-input"
        />
        <button type="submit" disabled={loading}>
          {loading ? "Searching..." : "Search"}
        </button>
      </form>

      {error && <p className="error">{error}</p>}

      {searched && results.length === 0 && !loading && (
        <p className="no-results">No results found.</p>
      )}

      <ul className="results">
        {results.map((r, i) => (
          <li key={i} className="result-item">
            <a href={r.url} target="_blank" rel="noopener noreferrer">
              {r.title || r.url}
            </a>
            <span className="result-url">{r.url}</span>
            {r.snippet && (
              <p
                className="result-snippet"
                dangerouslySetInnerHTML={{ __html: r.snippet }}
              />
            )}
          </li>
        ))}
      </ul>

      {searched && results.length > 0 && (
        <div className="pagination">
          <button disabled={page === 0} onClick={() => handleSearch(page - 1)}>
            Previous
          </button>
          <span>Page {page + 1}</span>
          <button
            disabled={results.length < 10}
            onClick={() => handleSearch(page + 1)}
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
}
