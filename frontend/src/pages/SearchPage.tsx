import { useState, useEffect, useRef } from "react";
import { search, suggest } from "../api";
import type { SearchResult } from "../types";

export default function SearchPage() {
  const [query, setQuery] = useState("");
  const [domain, setDomain] = useState("");
  const [results, setResults] = useState<SearchResult[]>([]);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [error, setError] = useState("");
  const [suggestions, setSuggestions] = useState<string[]>([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [activeSuggestion, setActiveSuggestion] = useState(-1);
  const suggestionsRef = useRef<HTMLUListElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (query.trim().length < 2) {
      setSuggestions([]);
      return;
    }

    const timer = setTimeout(async () => {
      try {
        const data = await suggest(query.trim());
        setSuggestions(data);
        setShowSuggestions(data.length > 0);
      } catch {
        setSuggestions([]);
      }
    }, 200);

    return () => clearTimeout(timer);
  }, [query]);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (
        suggestionsRef.current &&
        !suggestionsRef.current.contains(e.target as Node) &&
        inputRef.current &&
        !inputRef.current.contains(e.target as Node)
      ) {
        setShowSuggestions(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  function selectSuggestion(value: string) {
    setQuery(value);
    setShowSuggestions(false);
    setActiveSuggestion(-1);
  }

  function handleKeyDown(e: React.KeyboardEvent) {
    if (!showSuggestions || suggestions.length === 0) return;

    if (e.key === "ArrowDown") {
      e.preventDefault();
      setActiveSuggestion((prev) =>
        prev < suggestions.length - 1 ? prev + 1 : 0
      );
    } else if (e.key === "ArrowUp") {
      e.preventDefault();
      setActiveSuggestion((prev) =>
        prev > 0 ? prev - 1 : suggestions.length - 1
      );
    } else if (e.key === "Enter" && activeSuggestion >= 0) {
      e.preventDefault();
      selectSuggestion(suggestions[activeSuggestion]);
    } else if (e.key === "Escape") {
      setShowSuggestions(false);
    }
  }

  async function handleSearch(p = 0) {
    if (!query.trim()) return;
    setShowSuggestions(false);
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
        <div className="search-input-wrapper">
          <input
            ref={inputRef}
            type="text"
            placeholder="Search..."
            value={query}
            onChange={(e) => {
              setQuery(e.target.value);
              setActiveSuggestion(-1);
            }}
            onFocus={() => suggestions.length > 0 && setShowSuggestions(true)}
            onKeyDown={handleKeyDown}
          />
          {showSuggestions && (
            <ul className="suggestions" ref={suggestionsRef}>
              {suggestions.map((s, i) => (
                <li
                  key={s}
                  className={i === activeSuggestion ? "active" : ""}
                  onMouseDown={() => selectSuggestion(s)}
                >
                  {s}
                </li>
              ))}
            </ul>
          )}
        </div>
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
