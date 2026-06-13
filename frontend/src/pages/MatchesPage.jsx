import React, { useEffect, useMemo, useState } from 'react';
import Field from '../components/Field';
import Section from '../components/Section';
import { useAuth } from '../context/AuthContext';
import { buildUrl } from '../lib/api';
import { parseMemberLines } from '../lib/parsers';
import { formatDateTime, formatOversFromBallCount } from '../lib/format';

const blankForm = {
  matchName: '',
  matchType: 'T20',
  customOvers: '10',
  venue: '',
  matchDate: '',
  teamAName: '',
  teamBName: '',
  teamAPlayers: '',
  teamBPlayers: '',
  tournamentId: ''
};

const blankScoring = {
  matchId: '',
  matchName: '',
  battingTeamName: 'Batting Team',
  totalRuns: 0,
  totalWickets: 0,
  currentBall: 0,
  balls: [],
  striker: { name: '', runs: 0, balls: 0 },
  nonStriker: { name: '', runs: 0, balls: 0 },
  bowler: { name: '', runs: 0, wickets: 0, balls: 0 }
};

const scoreText = (team = {}) => `${Number(team.score || 0)}/${Number(team.wickets || 0)} (${team.overs || '0.0'})`;

const playerOptionsFromTeam = (team = {}) => {
  if (Array.isArray(team.playerLinks) && team.playerLinks.length) {
    return team.playerLinks.map((player) => player.name || player.playerName).filter(Boolean);
  }
  return Array.isArray(team.players) ? team.players.filter(Boolean) : [];
};

const cloneScoringState = (state) => ({
  ...state,
  balls: [...state.balls],
  striker: { ...state.striker },
  nonStriker: { ...state.nonStriker },
  bowler: { ...state.bowler },
  battingOptions: [...(state.battingOptions || [])],
  bowlingOptions: [...(state.bowlingOptions || [])]
});

export default function MatchesPage() {
  const { request, token, isAuthenticated } = useAuth();
  const [matches, setMatches] = useState([]);
  const [myMatches, setMyMatches] = useState([]);
  const [form, setForm] = useState(blankForm);
  const [scoring, setScoring] = useState(null);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const load = async () => {
    const [allPayload, minePayload] = await Promise.all([
      request('/matches'),
      isAuthenticated ? request('/matches/user/my-matches') : Promise.resolve({ data: [] })
    ]);
    setMatches(Array.isArray(allPayload.data) ? allPayload.data : []);
    setMyMatches(Array.isArray(minePayload.data) ? minePayload.data : []);
  };

  useEffect(() => {
    load().catch((err) => setError(err.message || 'Failed to load matches'));
  }, [isAuthenticated]);

  const onChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const createMatch = async (event) => {
    event.preventDefault();
    setBusy(true);
    setError('');
    try {
      await request('/matches', {
        method: 'POST',
        body: {
          matchName: form.matchName,
          matchType: form.matchType,
          customOvers: form.matchType === 'Custom' ? form.customOvers : null,
          venue: form.venue,
          matchDate: form.matchDate,
          teamAName: form.teamAName,
          teamAPlayers: parseMemberLines(form.teamAPlayers),
          teamBName: form.teamBName,
          teamBPlayers: parseMemberLines(form.teamBPlayers),
          tournamentId: form.tournamentId || null
        }
      });
      setForm(blankForm);
      await load();
    } catch (err) {
      setError(err.message || 'Failed to create match');
    } finally {
      setBusy(false);
    }
  };

  const startMatch = async (match) => {
    const matchId = match._id || match.id;
    const tossWinnerTeam = window.prompt(`Toss winner:\n1. ${match.teamA?.name || 'Team A'}\n2. ${match.teamB?.name || 'Team B'}`, '1') === '2' ? 'teamB' : 'teamA';
    const decision = String(window.prompt('Toss decision: bat or bowl', 'bat') || '').trim().toLowerCase();
    if (!['bat', 'bowl'].includes(decision)) {
      setError("Toss decision must be 'bat' or 'bowl'");
      return;
    }
    await request(`/matches/${matchId}/toss`, {
      method: 'PUT',
      body: { tossWinnerTeam, decision }
    });
    await openScoring(matchId);
    await load();
  };

  const openScoring = async (matchId) => {
    const payload = await request(`/matches/${matchId}`);
    const match = payload.data || payload.match || payload;
    const inningNumber = Number(match.currentInning || 1);
    const inningKey = inningNumber === 1 ? 'first' : 'second';
    const battingTeamKey = match.innings?.[inningKey]?.battingTeam || 'teamA';
    const bowlingTeamKey = battingTeamKey === 'teamA' ? 'teamB' : 'teamA';
    const battingTeam = battingTeamKey === 'teamB' ? match.teamB : match.teamA;
    const bowlingTeam = bowlingTeamKey === 'teamB' ? match.teamB : match.teamA;
    const battingOptions = playerOptionsFromTeam(battingTeam);
    const bowlingOptions = playerOptionsFromTeam(bowlingTeam);
    const [completedOvers = 0, ballsInOver = 0] = String(battingTeam?.overs || '0.0').split('.').map(Number);

    setScoring({
      ...blankScoring,
      matchId,
      matchName: match.matchName || 'Match',
      battingTeamName: battingTeam?.name || 'Batting Team',
      totalRuns: Number(battingTeam?.score || 0),
      totalWickets: Number(battingTeam?.wickets || 0),
      currentBall: completedOvers * 6 + (ballsInOver || 0),
      striker: { name: match.currentStriker || match.currentBatsman || battingOptions[0] || '', runs: 0, balls: 0 },
      nonStriker: { name: match.currentNonStriker || battingOptions[1] || '', runs: 0, balls: 0 },
      bowler: { name: match.currentBowler || bowlingOptions[0] || '', runs: 0, wickets: 0, balls: 0 },
      battingOptions,
      bowlingOptions
    });
  };

  const persistScore = async (next) => {
    await request(`/matches/${next.matchId}/score`, {
      method: 'PUT',
      body: {
        mode: 'absolute',
        runs: next.totalRuns,
        wickets: next.totalWickets,
        overs: formatOversFromBallCount(next.currentBall),
        batsmanName: next.striker.name,
        nonStrikerName: next.nonStriker.name,
        bowlerName: next.bowler.name,
        ballEvents: next.balls.map((ball) => ({
          runs: ball.runs,
          isExtra: ball.isExtra,
          extraType: ball.extraType,
          isWicket: ball.isWicket,
          strikerName: ball.strikerName,
          nonStrikerName: ball.nonStrikerName,
          bowlerName: ball.bowlerName,
          wicketPlayerName: ball.wicketPlayerName,
          wicketKind: ball.wicketKind
        })),
        status: 'live'
      }
    });
  };

  const commitScoring = async (producer) => {
    if (!scoring) return;
    const preState = {
      totalRuns: scoring.totalRuns,
      totalWickets: scoring.totalWickets,
      currentBall: scoring.currentBall,
      striker: scoring.striker,
      nonStriker: scoring.nonStriker,
      bowler: scoring.bowler
    };
    const next = producer(scoring, preState);
    setScoring(next);
    try {
      await persistScore(next);
      await load();
    } catch (err) {
      setError(err.message || 'Failed to save score');
    }
  };

  const swapBatsmen = (state) => ({
    ...state,
    striker: state.nonStriker,
    nonStriker: state.striker
  });

  const recordBall = (runs, isExtra = false, extraType = null) => {
    commitScoring((state, preState) => {
      let next = cloneScoringState(state);
      const totalRuns = Number(runs || 0);
      const normalizedExtra = String(extraType || '').toLowerCase();
      const isWideOrNoBall = isExtra && ['wd', 'nb'].includes(normalizedExtra);
      const isByeOrLegBye = isExtra && ['bye', 'lb'].includes(normalizedExtra);
      let batsmanRuns = isExtra ? 0 : totalRuns;

      next.totalRuns += totalRuns;
      if (!isByeOrLegBye) next.bowler.runs += totalRuns;

      if (isWideOrNoBall) {
        if (normalizedExtra === 'nb') {
          batsmanRuns = Math.max(totalRuns - 1, 0);
          next.striker.runs += batsmanRuns;
          if (batsmanRuns % 2 === 1) next = swapBatsmen(next);
        } else if (totalRuns > 1 && (totalRuns - 1) % 2 === 1) {
          next = swapBatsmen(next);
        }
      } else {
        next.currentBall += 1;
        next.bowler.balls += 1;
        next.striker.balls += isByeOrLegBye ? 0 : 1;
        next.striker.runs += isByeOrLegBye ? 0 : totalRuns;
        if (totalRuns % 2 === 1) next = swapBatsmen(next);
      }

      const display = isExtra
        ? `${normalizedExtra === 'bye' ? 'BYE' : normalizedExtra === 'lb' ? 'LB' : normalizedExtra.toUpperCase()}${totalRuns > 1 ? totalRuns : ''}`
        : totalRuns === 0 ? '.' : String(totalRuns);
      const cssClass = isExtra ? 'extra' : totalRuns === 0 ? 'dot' : totalRuns === 4 ? 'four' : totalRuns === 6 ? 'six' : 'run';
      next.balls.push({
        display,
        class: cssClass,
        runs: totalRuns,
        isExtra,
        extraType,
        isWicket: false,
        strikerName: state.striker.name,
        nonStrikerName: state.nonStriker.name,
        bowlerName: state.bowler.name,
        preState
      });
      return next;
    });
  };

  const recordWicket = () => {
    const newBatsman = window.prompt('Incoming batsman name', '');
    commitScoring((state, preState) => {
      const next = cloneScoringState(state);
      next.totalWickets += 1;
      next.currentBall += 1;
      next.striker.balls += 1;
      next.bowler.balls += 1;
      next.bowler.wickets += 1;
      next.balls.push({
        display: 'W',
        class: 'wicket',
        runs: 0,
        isExtra: false,
        extraType: null,
        isWicket: true,
        strikerName: state.striker.name,
        nonStrikerName: state.nonStriker.name,
        bowlerName: state.bowler.name,
        wicketPlayerName: state.striker.name,
        wicketKind: 'bowled',
        preState
      });
      if (newBatsman) next.striker = { name: newBatsman.trim(), runs: 0, balls: 0 };
      return next;
    });
  };

  const undoLastBall = () => {
    if (!scoring?.balls.length) return;
    const lastBall = scoring.balls[scoring.balls.length - 1];
    if (!lastBall.preState) return;
    const next = {
      ...scoring,
      balls: scoring.balls.slice(0, -1),
      totalRuns: lastBall.preState.totalRuns,
      totalWickets: lastBall.preState.totalWickets,
      currentBall: lastBall.preState.currentBall,
      striker: lastBall.preState.striker,
      nonStriker: lastBall.preState.nonStriker,
      bowler: lastBall.preState.bowler
    };
    setScoring(next);
    persistScore(next).catch((err) => setError(err.message || 'Failed to save undo'));
  };

  const downloadReport = async (matchId) => {
    const response = await fetch(buildUrl(`/matches/${matchId}/report`, { format: 'csv' }), {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    });
    if (!response.ok) throw new Error(`Report download failed (${response.status})`);
    const blob = await response.blob();
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = `match-report-${matchId}.csv`;
    document.body.appendChild(anchor);
    anchor.click();
    anchor.remove();
    URL.revokeObjectURL(url);
  };

  const visibleMatches = useMemo(() => (myMatches.length ? myMatches : matches), [matches, myMatches]);

  return (
    <>
      <Section eyebrow="Match operations" title="Host Match" description="Create fixtures, start toss, score live balls, and download match reports.">
        <form className="panel form-panel" onSubmit={createMatch}>
          <Field label="Match name" name="matchName" value={form.matchName} onChange={onChange} required />
          <div className="two-col">
            <Field label="Match type" name="matchType" type="select" value={form.matchType} onChange={onChange} options={[
              { value: 'T20', label: 'T20' },
              { value: 'ODI', label: 'ODI' },
              { value: 'Test', label: 'Test' },
              { value: 'Custom', label: 'Custom' }
            ]} />
            <Field label="Custom overs" name="customOvers" type="number" value={form.customOvers} onChange={onChange} />
          </div>
          <Field label="Venue" name="venue" value={form.venue} onChange={onChange} required />
          <Field label="Match date" name="matchDate" type="datetime-local" value={form.matchDate} onChange={onChange} required />
          <div className="two-col">
            <Field label="Team A" name="teamAName" value={form.teamAName} onChange={onChange} required />
            <Field label="Team B" name="teamBName" value={form.teamBName} onChange={onChange} required />
          </div>
          <div className="two-col">
            <Field label="Team A players" name="teamAPlayers" type="textarea" rows={5} value={form.teamAPlayers} onChange={onChange} placeholder="Player One|player@email.com" />
            <Field label="Team B players" name="teamBPlayers" type="textarea" rows={5} value={form.teamBPlayers} onChange={onChange} placeholder="Player Two|player@email.com" />
          </div>
          <Field label="Tournament ID" name="tournamentId" value={form.tournamentId} onChange={onChange} />
          {error ? <div className="error-banner">{error}</div> : null}
          <button className="button button-primary" type="submit" disabled={busy || !isAuthenticated}>
            {busy ? 'Saving...' : 'Create match'}
          </button>
        </form>
      </Section>

      <Section eyebrow="Fixtures" title="My Matches" description="Open a match for live scoring or download its CSV report.">
        <div className="matches-grid">
          {visibleMatches.map((match) => {
            const id = match._id || match.id;
            const status = String(match.status || 'scheduled').toLowerCase();
            return (
              <div className={`data-card match-card ${status}`} key={id}>
                <div className="match-header">
                  <span className={status === 'live' ? 'live-badge' : 'scheduled-badge'}>{status}</span>
                  <h3>{match.matchName || 'Untitled Match'}</h3>
                </div>
                <div className="match-score">
                  <div className="team">
                    <span className="team-name">{match.teamA?.name || 'Team A'}</span>
                    <span className="score">{scoreText(match.teamA)}</span>
                  </div>
                  <div className="vs">vs</div>
                  <div className="team">
                    <span className="team-name">{match.teamB?.name || 'Team B'}</span>
                    <span className="score">{scoreText(match.teamB)}</span>
                  </div>
                </div>
                <div className="match-footer">
                  <span>{formatDateTime(match.matchDate)} at {match.venue || 'Venue TBD'}</span>
                  <div className="match-actions-inline">
                    {['scheduled', 'upcoming'].includes(status) ? (
                      <button className="start-match-btn" type="button" onClick={() => startMatch(match)} disabled={!isAuthenticated}>Start</button>
                    ) : (
                      <button className="update-score-btn" type="button" onClick={() => openScoring(id)} disabled={!isAuthenticated}>Score</button>
                    )}
                    <button className="download-report-btn" type="button" onClick={() => downloadReport(id).catch((err) => setError(err.message))}>Report</button>
                  </div>
                </div>
              </div>
            );
          })}
          {!visibleMatches.length ? <div className="empty-state">No matches found.</div> : null}
        </div>
      </Section>

      {scoring ? (
        <Section eyebrow="Live scorer" title={scoring.matchName} description="Ball-by-ball scoring runs entirely in React state and saves absolute score snapshots to the backend.">
          <div className="scoring-grid">
            <div>
              <div className="score-display">
                <h3>{scoring.battingTeamName}</h3>
                <div className="big-score">{scoring.totalRuns}/{scoring.totalWickets}</div>
                <p>Overs {formatOversFromBallCount(scoring.currentBall)}</p>
              </div>
              <div className="batsmen-display">
                <div className="player-line"><strong>{scoring.striker.name || 'Striker'}</strong><span>{scoring.striker.runs} ({scoring.striker.balls})</span></div>
                <div className="player-line"><strong>{scoring.nonStriker.name || 'Non-striker'}</strong><span>{scoring.nonStriker.runs} ({scoring.nonStriker.balls})</span></div>
                <button className="swap-batsmen-btn" type="button" onClick={() => setScoring((state) => swapBatsmen(state))}>Swap batsmen</button>
              </div>
              <div className="bowler-display">
                <div className="player-line"><strong>{scoring.bowler.name || 'Bowler'}</strong><span>{formatOversFromBallCount(scoring.bowler.balls)} - {scoring.bowler.runs}/{scoring.bowler.wickets}</span></div>
              </div>
              <div className="over-display">
                <strong>This over</strong>
                <div className="over-balls">
                  {scoring.balls.slice(-6).map((ball, index) => <span className={`ball-circle ${ball.class}`} key={`${ball.display}-${index}`}>{ball.display}</span>)}
                </div>
              </div>
            </div>
            <div className="scoring-pad">
              <h3>Runs</h3>
              <div className="runs-buttons">
                {[0, 1, 2, 3, 4, 6].map((runs) => (
                  <button className={`run-btn ${runs === 4 ? 'four-btn' : runs === 6 ? 'six-btn' : ''}`} type="button" key={runs} onClick={() => recordBall(runs)}>{runs}</button>
                ))}
              </div>
              <div className="extras-buttons">
                {[
                  ['wd', 'Wide'],
                  ['nb', 'No ball'],
                  ['bye', 'Bye'],
                  ['lb', 'Leg bye']
                ].map(([type, label]) => (
                  <button className="extra-btn" type="button" key={type} onClick={() => recordBall(Number(window.prompt(`${label} runs`, type === 'wd' || type === 'nb' ? '1' : '1') || 0), true, type)}>{label}</button>
                ))}
              </div>
              <div className="wicket-buttons">
                <button className="wicket-btn" type="button" onClick={recordWicket}>Wicket</button>
                <button className="undo-btn" type="button" onClick={undoLastBall}>Undo</button>
              </div>
            </div>
          </div>
        </Section>
      ) : null}
    </>
  );
}
