import { useAuth } from "../context/AuthContext";

interface Props {
  page: string;
  setPage: (p: string) => void;
}

export default function Navbar({ page, setPage }: Props) {
  const { user, logout, isAdmin } = useAuth();

  return (
    <nav className="navbar">
      <button
        className={page === "search" ? "active" : ""}
        onClick={() => setPage("search")}
      >
        Search
      </button>
      {user && (
        <button
          className={page === "history" ? "active" : ""}
          onClick={() => setPage("history")}
        >
          History
        </button>
      )}
      {isAdmin && (
        <button
          className={page === "admin" ? "active" : ""}
          onClick={() => setPage("admin")}
        >
          Admin
        </button>
      )}
      <div className="nav-spacer" />
      {user ? (
        <div className="nav-user">
          <span>{user.email}</span>
          <button onClick={logout}>Logout</button>
        </div>
      ) : (
        <button
          className={page === "login" ? "active" : ""}
          onClick={() => setPage("login")}
        >
          Login
        </button>
      )}
    </nav>
  );
}
