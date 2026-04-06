import { useState } from "react";
import { AuthProvider } from "./context/AuthContext";
import Navbar from "./components/Navbar";
import SearchPage from "./pages/SearchPage";
import LoginPage from "./pages/LoginPage";
import HistoryPage from "./pages/HistoryPage";
import AdminPage from "./pages/AdminPage";

function AppContent() {
  const [page, setPage] = useState("search");

  return (
    <>
      <Navbar page={page} setPage={setPage} />
      <main>
        {page === "search" && <SearchPage />}
        {page === "login" && <LoginPage onSuccess={() => setPage("search")} />}
        {page === "history" && <HistoryPage />}
        {page === "admin" && <AdminPage />}
      </main>
    </>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}
