import React, { useEffect, useMemo, useState } from 'react';
import Field from '../components/Field';
import Section from '../components/Section';
import CardList from '../components/CardList';
import { useAuth } from '../context/AuthContext';
import { formatDate, shortText } from '../lib/format';

const blankForm = {
  name: '',
  description: '',
  startDate: '',
  endDate: '',
  registrationDeadline: '',
  venue: '',
  format: 'T20',
  maxTeams: '8'
};

export default function TournamentsPage() {
  const { request, isAuthenticated } = useAuth();
  const [tournaments, setTournaments] = useState([]);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [form, setForm] = useState(blankForm);
  const [teamId, setTeamId] = useState('');

  const load = async () => {
    const payload = await request('/tournaments');
    setTournaments(Array.isArray(payload.tournaments) ? payload.tournaments : []);
  };

  useEffect(() => {
    load().catch((err) => setError(err.message || 'Failed to load tournaments'));
  }, [isAuthenticated]);

  const onChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const createTournament = async (event) => {
    event.preventDefault();
    setBusy(true);
    setError('');
    try {
      await request('/tournaments', {
        method: 'POST',
        body: {
          ...form,
          maxTeams: Number(form.maxTeams || 8)
        }
      });
      setForm(blankForm);
      await load();
    } catch (err) {
      setError(err.message || 'Failed to create tournament');
    } finally {
      setBusy(false);
    }
  };

  const cards = useMemo(
    () =>
      tournaments.map((tournament) => ({
        id: tournament._id || tournament.id,
        title: tournament.name || 'Tournament',
        body: [
          `Status: ${tournament.status || 'unknown'}`,
          `Venue: ${tournament.venue || 'n/a'}`,
          `Starts: ${formatDate(tournament.startDate)}`,
          `Teams: ${tournament.registeredTeams?.length || 0}/${tournament.maxTeams || 0}`
        ]
      })),
    [tournaments]
  );

  const registerTeam = async (tournamentId) => {
    if (!teamId.trim()) {
      throw new Error('Enter a team ID first');
    }
    await request(`/tournaments/${tournamentId}/register`, {
      method: 'POST',
      body: { teamId: teamId.trim() }
    });
    await load();
  };

  const updateStatus = async (tournamentId, nextStatus) => {
    await request(`/tournaments/${tournamentId}/status`, {
      method: 'PUT',
      body: { status: nextStatus }
    });
    await load();
  };

  return (
    <>
      <Section eyebrow="Competition" title="Tournaments" description="Create events, register teams, and manage the tournament state.">
        <form className="panel form-panel" onSubmit={createTournament}>
          <Field label="Name" name="name" value={form.name} onChange={onChange} required />
          <Field label="Description" name="description" type="textarea" value={form.description} onChange={onChange} rows={4} />
          <div className="two-col">
            <Field label="Start date" name="startDate" type="datetime-local" value={form.startDate} onChange={onChange} required />
            <Field label="End date" name="endDate" type="datetime-local" value={form.endDate} onChange={onChange} required />
          </div>
          <Field label="Registration deadline" name="registrationDeadline" type="datetime-local" value={form.registrationDeadline} onChange={onChange} />
          <Field label="Venue" name="venue" value={form.venue} onChange={onChange} required />
          <Field label="Format" name="format" type="select" value={form.format} onChange={onChange} options={[{ value: 'T20', label: 'T20' }, { value: 'ODI', label: 'ODI' }, { value: 'Test', label: 'Test' }]} />
          <Field label="Max teams" name="maxTeams" type="number" value={form.maxTeams} onChange={onChange} />
          {error ? <div className="error-banner">{error}</div> : null}
          <button className="button button-primary" type="submit" disabled={busy || !isAuthenticated}>
            {busy ? 'Saving...' : 'Create tournament'}
          </button>
        </form>
      </Section>

      <Section eyebrow="Tournament list" title="All tournaments" description="Current data from GET /api/tournaments.">
        <div className="panel form-panel" style={{ marginBottom: 16 }}>
          <Field label="Team ID for registration" name="teamId" value={teamId} onChange={(e) => setTeamId(e.target.value)} />
        </div>
        <CardList
          items={cards}
          emptyText="No tournaments found."
          renderItem={(item) => (
            <>
              <h3>{item.title}</h3>
              {item.body.map((line) => (
                <p key={line}>{shortText(line, 120)}</p>
              ))}
              <div className="row-actions">
                <button className="button button-secondary" type="button" onClick={() => registerTeam(item.id).catch((err) => setError(err.message || 'Registration failed'))}>
                  Register team
                </button>
                <button className="button button-ghost" type="button" onClick={() => updateStatus(item.id, 'ongoing')}>
                  Mark ongoing
                </button>
              </div>
            </>
          )}
        />
      </Section>
    </>
  );
}
