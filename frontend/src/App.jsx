import React from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout';
import { useAuth } from './context/AuthContext';
import HomePage from './pages/HomePage';
import AuthPage from './pages/AuthPage';
import TeamsPage from './pages/TeamsPage';
import MatchesPage from './pages/MatchesPage';
import TournamentsPage from './pages/TournamentsPage';
import TurfsPage from './pages/TurfsPage';
import PlayersPage from './pages/PlayersPage';
import PostsPage from './pages/PostsPage';
import BookingsPage from './pages/BookingsPage';
import ProfilePage from './pages/ProfilePage';

function RequireAuth({ children }) {
  const { isAuthenticated, ready } = useAuth();
  if (!ready) return <div className="page-state">Loading session...</div>;
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return children;
}

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<HomePage />} />
        <Route path="login" element={<AuthPage mode="login" />} />
        <Route path="signup" element={<AuthPage mode="signup" />} />
        <Route path="players" element={<PlayersPage />} />
        <Route
          path="teams"
          element={
            <RequireAuth>
              <TeamsPage />
            </RequireAuth>
          }
        />
        <Route path="matches" element={<MatchesPage />} />
        <Route
          path="tournaments"
          element={
            <RequireAuth>
              <TournamentsPage />
            </RequireAuth>
          }
        />
        <Route path="turfs" element={<TurfsPage />} />
        <Route
          path="bookings"
          element={
            <RequireAuth>
              <BookingsPage />
            </RequireAuth>
          }
        />
        <Route
          path="posts"
          element={
            <RequireAuth>
              <PostsPage />
            </RequireAuth>
          }
        />
        <Route
          path="profile"
          element={
            <RequireAuth>
              <ProfilePage />
            </RequireAuth>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}
