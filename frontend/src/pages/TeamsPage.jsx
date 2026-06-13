import React, { useEffect, useMemo, useState } from 'react';
import Field from '../components/Field';
import Section from '../components/Section';
import CardList from '../components/CardList';
import { useAuth } from '../context/AuthContext';
import { parseMemberLines } from '../lib/parsers';
import { formatDateTime, shortText } from '../lib/format';

const blankForm = {
  name: '',
  members: ''
};

export default function TeamsPage() {
  const { request, isAuthenticated } = useAuth();
  const [teams, setTeams] = useState([]);
  const [invitations, setInvitations] = useState([]);
  const [suggestions, setSuggestions] = useState([]);
  const [query, setQuery] = useState('');
  const [form, setForm] = useState(blankForm);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const load = async () => {
    const [teamPayload, invitePayload] = await Promise.all([request('/teams'), request('/teams/invitations/my')]);
    setTeams(Array.isArray(teamPayload.data) ? teamPayload.data : []);
    setInvitations(Array.isArray(invitePayload.data) ? invitePayload.data : []);
  };

  useEffect(() => {
    if (!isAuthenticated) return;
    load().catch((err) => setError(err.message || 'Failed to load teams'));
  }, [isAuthenticated]);

  const onChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const createTeam = async (event) => {
    event.preventDefault();
    setBusy(true);
    setError('');
    try {
      await request('/teams', {
        method: 'POST',
        body: {
          name: form.name,
          members: parseMemberLines(form.members)
        }
      });
      setForm(blankForm);
      await load();
    } catch (err) {
      setError(err.message || 'Failed to create team');
    } finally {
      setBusy(false);
    }
  };

  const searchPlayers = async () => {
    setBusy(true);
    setError('');
    try {
      const payload = await request('/teams/suggestions', {
        query: { q: query, limit: 12 }
      });
      setSuggestions(Array.isArray(payload.data) ? payload.data : []);
    } catch (err) {
      setError(err.message || 'Failed to search players');
    } finally {
      setBusy(false);
    }
  };

  const respondToInvite = async (teamId, memberId, action) => {
    await request(`/teams/${teamId}/invitations/${memberId}/respond`, {
      method: 'PUT',
      body: { action }
    });
    await load();
  };

  const teamCards = useMemo(
    () =>
      teams.map((team) => ({
        id: team._id || team.id,
        title: team.name || 'Team',
        body: [
          `Members: ${Array.isArray(team.members) ? team.members.length : 0}`,
          `Updated: ${formatDateTime(team.updatedAt)}`,
          `Owner: ${team.owner || 'n/a'}`
        ]
      })),
    [teams]
  );

  return (
    <>
      <Section eyebrow="Squad management" title="Teams" description="Create reusable squads and manage invitations.">
        <form className="panel form-panel" onSubmit={createTeam}>
          <Field label="Team name" name="name" value={form.name} onChange={onChange} required />
          <Field
            label="Members"
            name="members"
            type="textarea"
            value={form.members}
            onChange={onChange}
            rows={6}
            placeholder={'Player One|player1@email.com\nPlayer Two|player2@email.com'}
            hint="One member per line. Use name|email|userId when you know the user ID."
          />
          {error ? <div className="error-banner">{error}</div> : null}
          <button className="button button-primary" type="submit" disabled={busy || !isAuthenticated}>
            {busy ? 'Saving...' : 'Save team'}
          </button>
        </form>
      </Section>

      <Section eyebrow="Teams" title="My teams" description={isAuthenticated ? 'Teams returned from GET /api/teams.' : 'Sign in to load your teams.'}>
        <CardList
          items={teamCards}
          emptyText="No teams found."
          renderItem={(item) => (
            <>
              <h3>{item.title}</h3>
              {item.body.map((line) => (
                <p key={line}>{shortText(line, 120)}</p>
              ))}
            </>
          )}
        />
      </Section>

      <Section eyebrow="Invitations" title="Pending invites" description="Accept or reject invitations to join teams.">
        <CardList
          items={invitations}
          emptyText="No pending invitations."
          renderItem={(invite) => (
            <>
              <h3>{invite.teamName || 'Team invite'}</h3>
              <p>{invite.name || invite.email || 'Member invite'}</p>
              <div className="row-actions">
                <button
                  className="button button-secondary"
                  type="button"
                  onClick={() => respondToInvite(invite.teamId, invite.memberId, 'accept')}
                >
                  Accept
                </button>
                <button
                  className="button button-ghost"
                  type="button"
                  onClick={() => respondToInvite(invite.teamId, invite.memberId, 'reject')}
                >
                  Reject
                </button>
              </div>
            </>
          )}
        />
      </Section>

      <Section eyebrow="Search" title="Player suggestions" description="Search the existing users API from the frontend.">
        <div className="panel form-panel">
          <Field label="Search query" name="query" value={query} onChange={(e) => setQuery(e.target.value)} />
          <button className="button button-secondary" type="button" onClick={searchPlayers} disabled={busy || !isAuthenticated}>
            Find players
          </button>
        </div>
        <CardList
          items={suggestions}
          emptyText="No player suggestions yet."
          renderItem={(player) => (
            <>
              <h3>{player.name || 'Player'}</h3>
              <p>{player.email || 'No email'}</p>
              <p>{shortText(JSON.stringify(player.profile || {}, null, 0), 90)}</p>
            </>
          )}
        />
      </Section>
    </>
  );
}
