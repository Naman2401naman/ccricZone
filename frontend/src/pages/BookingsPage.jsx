import React, { useEffect, useMemo, useState } from 'react';
import Field from '../components/Field';
import Section from '../components/Section';
import CardList from '../components/CardList';
import { useAuth } from '../context/AuthContext';
import { formatDate, formatMoney } from '../lib/format';

const blankForm = {
  turfId: '',
  date: '',
  startTime: '',
  endTime: ''
};

export default function BookingsPage() {
  const { request, isAuthenticated, user } = useAuth();
  const [bookings, setBookings] = useState([]);
  const [summary, setSummary] = useState(null);
  const [form, setForm] = useState(blankForm);
  const [error, setError] = useState('');

  const load = async () => {
    const [bookingsPayload, summaryPayload] = await Promise.all([
      request('/bookings/mybookings'),
      user?.role === 'admin' || user?.role === 'turf_owner' ? request('/bookings/billing/summary') : Promise.resolve(null)
    ]);
    setBookings(Array.isArray(bookingsPayload.bookings) ? bookingsPayload.bookings : []);
    setSummary(summaryPayload?.summary || null);
  };

  useEffect(() => {
    if (!isAuthenticated) return;
    load().catch((err) => setError(err.message || 'Failed to load bookings'));
  }, [isAuthenticated, user?.role]);

  const onChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const createBooking = async (event) => {
    event.preventDefault();
    setError('');
    try {
      await request('/bookings', {
        method: 'POST',
        body: form
      });
      setForm(blankForm);
      await load();
    } catch (err) {
      setError(err.message || 'Failed to create booking');
    }
  };

  const cancelBooking = async (bookingId) => {
    await request(`/bookings/${bookingId}/cancel`, { method: 'PUT' });
    await load();
  };

  const cards = useMemo(
    () =>
      bookings.map((booking) => ({
        id: booking._id || booking.id,
        title: `Booking ${booking.billing?.invoiceNumber || booking._id || ''}`,
        body: [
          `Date: ${formatDate(booking.date)}`,
          `Time: ${booking.startTime} - ${booking.endTime}`,
          `Amount: ${formatMoney(booking.totalPrice || 0)}`,
          `Status: ${booking.status || 'unknown'}`
        ]
      })),
    [bookings]
  );

  return (
    <>
      <Section eyebrow="Reservations" title="Bookings" description="Create bookings and manage billing from the same backend.">
        <form className="panel form-panel" onSubmit={createBooking}>
          <Field label="Turf ID" name="turfId" value={form.turfId} onChange={onChange} required />
          <div className="two-col">
            <Field label="Date" name="date" type="date" value={form.date} onChange={onChange} required />
            <Field label="Start time" name="startTime" type="time" value={form.startTime} onChange={onChange} required />
          </div>
          <Field label="End time" name="endTime" type="time" value={form.endTime} onChange={onChange} required />
          {error ? <div className="error-banner">{error}</div> : null}
          <button className="button button-primary" type="submit" disabled={!isAuthenticated}>
            Create booking
          </button>
        </form>
      </Section>

      <Section eyebrow="Bookings" title="My bookings" description="Loaded from GET /api/bookings/mybookings.">
        <CardList
          items={cards}
          emptyText="No bookings found."
          renderItem={(item) => (
            <>
              <h3>{item.title}</h3>
              {item.body.map((line) => (
                <p key={line}>{line}</p>
              ))}
              <button className="button button-secondary" type="button" onClick={() => cancelBooking(item.id)}>
                Cancel booking
              </button>
            </>
          )}
        />
      </Section>

      {summary ? (
        <Section eyebrow="Billing" title="Billing summary" description="Visible to admin and turf owners.">
          <div className="metric-grid">
            <div className="metric-card">
              <span>Total bookings</span>
              <strong>{summary.totalBookings || 0}</strong>
            </div>
            <div className="metric-card">
              <span>Total paid</span>
              <strong>{formatMoney(summary.totalPaid || 0)}</strong>
            </div>
            <div className="metric-card">
              <span>Total pending</span>
              <strong>{formatMoney(summary.totalPending || 0)}</strong>
            </div>
          </div>
        </Section>
      ) : null}
    </>
  );
}
