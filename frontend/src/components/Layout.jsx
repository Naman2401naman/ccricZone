import React from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const navItems = [
  { to: '/', label: 'Home' },
  { to: '/players', label: 'Players' },
  { to: '/teams', label: 'Teams' },
  { to: '/matches', label: 'Matches' },
  { to: '/tournaments', label: 'Tournaments' },
  { to: '/turfs', label: 'Turfs' },
  { to: '/bookings', label: 'Bookings' },
  { to: '/posts', label: 'Posts' },
  { to: '/profile', label: 'Profile' }
];

export default function Layout() {
  const { user, token, clearSession } = useAuth();

  return (
    <div className="app-shell">
      <div className="backdrop-grid" />
      <header className="navbar">
        <div className="nav-container">
          <NavLink to="/" className="nav-brand">
            <div className="brand-icon">CZ</div>
            <div className="brand-name">CricZone</div>
          </NavLink>
          <nav className="nav-menu open">
            <div className="nav-links">
              {navItems.map((item) => (
                <NavLink key={item.to} to={item.to} end={item.to === '/'} className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                  {item.label}
                </NavLink>
              ))}
            </div>
            <div className="nav-actions">
              {user ? (
                <div className="nav-auth">
                  <span className="user-email">{user.email || user.name}</span>
                  <button className="btn-logout" type="button" onClick={clearSession}>
                    Sign out
                  </button>
                </div>
              ) : (
                <NavLink to="/login" className="btn-logout nav-api-btn">
                  Sign in
                </NavLink>
              )}
            </div>
          </nav>
        </div>
      </header>

      <main className="main">
        <Outlet />
      </main>
      <footer className="site-footer">
        CricZone React frontend. {token ? 'Signed in' : 'Guest mode'}.
      </footer>
    </div>
  );
}
