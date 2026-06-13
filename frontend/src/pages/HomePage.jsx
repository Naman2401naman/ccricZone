import React, { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import Section from '../components/Section';
import { useAuth } from '../context/AuthContext';
import { apiRequest } from '../lib/api';
import { formatDate, formatNumber } from '../lib/format';

const scoreText = (team = {}) => `${Number(team.score || 0)}/${Number(team.wickets || 0)} (${team.overs || '0.0'})`;

export default function HomePage() {
  const { token, user } = useAuth();
  const [snapshot, setSnapshot] = useState({ matches: [], tournaments: [], teams: [], leaderboard: [] });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let mounted = true;
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const [matches, tournaments, leaderboard, teams] = await Promise.all([
          apiRequest('/matches'),
          apiRequest('/tournaments'),
          apiRequest('/leaderboard/batsmen', { query: { limit: 5 } }).catch(() => ({ leaderboard: [] })),
          token ? apiRequest('/teams', { token }).catch(() => ({ data: [] })) : Promise.resolve({ data: [] })
        ]);
        if (!mounted) return;
        setSnapshot({
          matches: Array.isArray(matches.data) ? matches.data : [],
          tournaments: Array.isArray(tournaments.tournaments) ? tournaments.tournaments : [],
          leaderboard: Array.isArray(leaderboard.leaderboard) ? leaderboard.leaderboard : [],
          teams: Array.isArray(teams.data) ? teams.data : []
        });
      } catch (err) {
        if (mounted) setError(err.message || 'Failed to load dashboard');
      } finally {
        if (mounted) setLoading(false);
      }
    };
    load();
    return () => {
      mounted = false;
    };
  }, [token]);

  const liveMatches = snapshot.matches.filter((match) => String(match.status || '').toLowerCase() === 'live');
  const upcomingMatches = snapshot.matches.filter((match) => ['scheduled', 'upcoming'].includes(String(match.status || '').toLowerCase()));
  const openTournaments = snapshot.tournaments.filter((tournament) =>
    ['ongoing', 'upcoming', 'registration_open', 'registration_closed', 'playoffs'].includes(String(tournament.status || '').toLowerCase())
  );

  const nextAction = useMemo(() => {
    if (!token) return { title: 'Create your account', text: 'Sign in first so teams, matches, bookings, and stats stay linked.', to: '/signup', cta: 'Create Account' };
    if (!snapshot.teams.length) return { title: 'Save your first reusable team', text: 'Create a squad once, then reuse it across fixtures and tournaments.', to: '/teams', cta: 'Create Team' };
    const liveOwnedMatch = liveMatches[0];
    if (liveOwnedMatch) return { title: `Resume ${liveOwnedMatch.matchName || 'live scoring'}`, text: 'Jump back into ball-by-ball scoring from Matches.', to: '/matches', cta: 'Open Matches' };
    return { title: 'Publish your next fixture', text: 'You have the workspace ready. Create the match and set the toss when play begins.', to: '/matches', cta: 'Host Match' };
  }, [token, snapshot.teams.length, liveMatches]);

  return (
    <>
      <section className="hero">
        <div className="hero-layout">
          <div className="hero-copy">
            <p className="eyebrow">{user ? `${String(user.name || user.email || 'Player').split(/\s+/)[0]}'s cricket workspace` : 'Matchday starts here'}</p>
            <h1 className="hero-title">CricZone control center</h1>
            <p className="hero-subtitle">Create teams, host matches, score every ball, manage tournaments, book turfs, and keep billing in one React app.</p>
            <div className="hero-buttons">
              <Link className="btn-primary" to="/matches">Host a Match</Link>
              <Link className="btn-secondary" to="/tournaments">View Tournaments</Link>
            </div>
            <div className="hero-metrics">
              <div className="metric-chip"><span>{formatNumber(liveMatches.length)}</span><small>Live</small></div>
              <div className="metric-chip"><span>{formatNumber(upcomingMatches.length)}</span><small>Upcoming</small></div>
              <div className="metric-chip"><span>{formatNumber(openTournaments.length)}</span><small>Tournaments</small></div>
            </div>
            <div className="hero-trust-row">
              <span>Ball-by-ball scoring</span>
              <span>Reusable teams</span>
              <span>PWA ready</span>
            </div>
          </div>
          <div className="hero-panels">
            <div className="hero-panel">
              <h3>{nextAction.title}</h3>
              <p>{nextAction.text}</p>
              <Link className="button button-primary" to={nextAction.to}>{nextAction.cta}</Link>
            </div>
            <div className="hero-panel">
              <h3>Workspace snapshot</h3>
              <div className="workspace-stats">
                <div className="workspace-stat"><strong>{snapshot.matches.length}</strong><span>Matches</span></div>
                <div className="workspace-stat"><strong>{snapshot.teams.length}</strong><span>My teams</span></div>
                <div className="workspace-stat"><strong>{snapshot.tournaments.length}</strong><span>Events</span></div>
              </div>
            </div>
            <div className="hero-panel">
              <h3>Agenda</h3>
              <p>{upcomingMatches.length ? `${upcomingMatches.length} upcoming fixture${upcomingMatches.length === 1 ? '' : 's'} to prepare.` : 'Create a match or tournament to activate your dashboard.'}</p>
            </div>
          </div>
        </div>
      </section>

      {loading ? <div className="loading-text">Loading dashboard data...</div> : null}
      {error ? <div className="error-banner">{error}</div> : null}

      <Section eyebrow="Live desk" title="Matches" description="Recent active and upcoming fixtures from the backend.">
        <div className="matches-grid">
          {[...liveMatches, ...upcomingMatches].slice(0, 6).map((match) => {
            const status = String(match.status || 'scheduled').toLowerCase();
            return (
              <div className={`data-card match-card ${status}`} key={match._id || match.id}>
                <div className="match-header">
                  <span className={status === 'live' ? 'live-badge' : 'scheduled-badge'}>{status}</span>
                  <h3>{match.matchName || 'Untitled Match'}</h3>
                </div>
                <div className="match-score">
                  <div className="team"><span className="team-name">{match.teamA?.name || 'Team A'}</span><span className="score">{scoreText(match.teamA)}</span></div>
                  <div className="vs">vs</div>
                  <div className="team"><span className="team-name">{match.teamB?.name || 'Team B'}</span><span className="score">{scoreText(match.teamB)}</span></div>
                </div>
                <div className="match-footer"><span>{match.venue || 'Venue TBD'}</span><span>{formatDate(match.matchDate)}</span></div>
              </div>
            );
          })}
          {!liveMatches.length && !upcomingMatches.length ? <div className="empty-state">No matches yet.</div> : null}
        </div>
      </Section>

      <Section eyebrow="Leaderboard" title="Top batsmen" description="A quick view into current player form.">
        <div className="players-grid">
          {snapshot.leaderboard.map((player, index) => (
            <div className="data-card player-card" key={player._id || player.id || player.email || index}>
              <div className="player-card-head">
                <h3>#{index + 1} {player.name || 'Player'}</h3>
                <span className="status-badge ongoing">Form</span>
              </div>
              <p className="player-sub">{player.email || 'No email'}</p>
            </div>
          ))}
          {!snapshot.leaderboard.length ? <div className="empty-state">No leaderboard data yet.</div> : null}
        </div>
      </Section>
    </>
  );
}
