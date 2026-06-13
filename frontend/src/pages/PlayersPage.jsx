import React, { useState } from 'react';
import Field from '../components/Field';
import Section from '../components/Section';
import CardList from '../components/CardList';
import { useAuth } from '../context/AuthContext';

export default function PlayersPage() {
  const { request } = useAuth();
  const [query, setQuery] = useState('');
  const [players, setPlayers] = useState([]);
  const [leaderboard, setLeaderboard] = useState([]);
  const [error, setError] = useState('');

  const searchPlayers = async () => {
    setError('');
    try {
      const payload = await request('/users/search-players', { query: { search: query } });
      setPlayers(Array.isArray(payload.data) ? payload.data : []);
    } catch (err) {
      setError(err.message || 'Search failed');
    }
  };

  const loadLeaderboard = async () => {
    setError('');
    try {
      const payload = await request('/leaderboard/batsmen', { query: { limit: 10 } });
      setLeaderboard(Array.isArray(payload.leaderboard) ? payload.leaderboard : []);
    } catch (err) {
      setError(err.message || 'Failed to load leaderboard');
    }
  };

  return (
    <>
      <Section eyebrow="Discovery" title="Players" description="Search players and read leaderboard data from the backend.">
        <div className="panel form-panel">
          <Field label="Search players" name="query" value={query} onChange={(e) => setQuery(e.target.value)} />
          {error ? <div className="error-banner">{error}</div> : null}
          <div className="row-actions">
            <button className="button button-primary" type="button" onClick={searchPlayers}>
              Search
            </button>
            <button className="button button-secondary" type="button" onClick={loadLeaderboard}>
              Load leaderboard
            </button>
          </div>
        </div>
      </Section>

      <Section eyebrow="Players" title="Search results" description="Results returned by /api/users/search-players.">
        <CardList
          items={players}
          emptyText="No players loaded yet."
          renderItem={(player) => (
            <>
              <h3>{player.name || 'Player'}</h3>
              <p>{player.email || 'No email'}</p>
              <p>{player.profile?.playerType || 'Player'}</p>
            </>
          )}
        />
      </Section>

      <Section eyebrow="Leaderboard" title="Batsmen" description="This can be switched to the bowlers/all-rounders endpoint later.">
        <CardList
          items={leaderboard}
          emptyText="No leaderboard loaded yet."
          renderItem={(player, index) => (
            <>
              <h3>
                #{index + 1} {player.name || 'Player'}
              </h3>
              <p>{player.email || 'No email'}</p>
            </>
          )}
        />
      </Section>
    </>
  );
}
