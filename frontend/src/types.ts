export type Role = "USER" | "ADMIN";

export interface UserResponse {
  id: number;
  email: string;
  role: Role;
}

export interface AuthTokenResponse {
  token: string;
  user: UserResponse;
}

export interface SearchResult {
  url: string;
  title: string;
  snippet: string;
}

export interface SearchHistoryEntry {
  query: string;
  domain: string | null;
  timestamp: string;
}

export interface SourceResponse {
  id: number;
  url: string;
  name: string;
  active: boolean;
  createdAt: string;
}

export interface CrawlJobResponse {
  id: number;
  sourceUrl: string;
  status: "PENDING" | "RUNNING" | "COMPLETED" | "FAILED";
  pagesFound: number;
  createdAt: string;
}
