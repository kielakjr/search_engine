import type {
  AuthTokenResponse,
  SearchResult,
  SearchHistoryEntry,
  SourceResponse,
  CrawlJobResponse,
  UserResponse,
} from "./types";

const API = "/api";

function getToken(): string | null {
  return localStorage.getItem("token");
}

async function request<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const token = getToken();
  const headers: Record<string, string> = {
    ...((options.headers as Record<string, string>) ?? {}),
  };
  if (token) headers["Authorization"] = `Bearer ${token}`;
  if (options.body) headers["Content-Type"] = "application/json";

  const res = await fetch(`${API}${path}`, { ...options, headers });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || res.statusText);
  }

  if (res.status === 204) return undefined as T;
  return res.json();
}

export async function login(
  email: string,
  password: string
): Promise<AuthTokenResponse> {
  return request<AuthTokenResponse>("/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
}

export async function register(
  email: string,
  password: string
): Promise<UserResponse> {
  return request<UserResponse>("/auth/register", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
}

export async function search(
  query: string,
  page = 0,
  size = 10,
  domain?: string
): Promise<SearchResult[]> {
  const params = new URLSearchParams({ query, page: String(page), size: String(size) });
  if (domain) params.set("domain", domain);
  return request<SearchResult[]>(`/../search?${params}`);
}

export async function getSearchHistory(): Promise<SearchHistoryEntry[]> {
  return request<SearchHistoryEntry[]>("/../search/history");
}

export async function getSources(): Promise<SourceResponse[]> {
  return request<SourceResponse[]>("/sources");
}

export async function createSource(
  name: string,
  url: string
): Promise<SourceResponse> {
  return request<SourceResponse>("/sources", {
    method: "POST",
    body: JSON.stringify({ name, url }),
  });
}

export async function deleteSource(id: number): Promise<void> {
  return request<void>(`/sources/${id}`, { method: "DELETE" });
}

export async function startCrawl(sourceId: number): Promise<string> {
  return request<string>(`/crawler/start/${sourceId}`, { method: "POST" });
}

export async function getCrawlJobs(): Promise<CrawlJobResponse[]> {
  return request<CrawlJobResponse[]>("/crawler/jobs");
}
