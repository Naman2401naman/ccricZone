import React, { useEffect, useMemo, useState } from 'react';
import Field from '../components/Field';
import Section from '../components/Section';
import CardList from '../components/CardList';
import { useAuth } from '../context/AuthContext';
import { formatMoney, shortText } from '../lib/format';

const blankForm = {
  turfName: '',
  surfaceType: 'synthetic',
  basePricingPerSlot: '0',
  location: ''
};

export default function TurfsPage() {
  const { request, isAuthenticated } = useAuth();
  const [turfs, setTurfs] = useState([]);
  const [form, setForm] = useState(blankForm);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const load = async () => {
    const payload = await request('/turfs/all');
    setTurfs(Array.isArray(payload.data) ? payload.data : []);
  };

  useEffect(() => {
    load().catch((err) => setError(err.message || 'Failed to load turfs'));
  }, []);

  const onChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const createTurf = async (event) => {
    event.preventDefault();
    setBusy(true);
    setError('');
    try {
      await request('/turfs/add', {
        method: 'POST',
        body: {
          turfName: form.turfName,
          surfaceType: form.surfaceType,
          basePricingPerSlot: Number(form.basePricingPerSlot || 0),
          location: {
            address: form.location
          },
          sportTypes: ['Cricket'],
          turfSize: {},
          amenities: {},
          images: []
        }
      });
      setForm(blankForm);
      await load();
    } catch (err) {
      setError(err.message || 'Failed to add turf');
    } finally {
      setBusy(false);
    }
  };

  const cards = useMemo(
    () =>
      turfs.map((turf) => ({
        id: turf._id || turf.id,
        title: turf.turfName || 'Turf',
        body: [
          `Surface: ${turf.surfaceType || 'n/a'}`,
          `Price: ${formatMoney(turf.basePricingPerSlot || 0)}`,
          `Location: ${shortText(turf.location?.address || turf.location?.city || 'n/a', 90)}`
        ]
      })),
    [turfs]
  );

  return (
    <>
      <Section eyebrow="Venue inventory" title="Turfs" description="The backend already exposes turf management endpoints.">
        <form className="panel form-panel" onSubmit={createTurf}>
          <Field label="Turf name" name="turfName" value={form.turfName} onChange={onChange} required />
          <div className="two-col">
            <Field label="Surface type" name="surfaceType" value={form.surfaceType} onChange={onChange} />
            <Field label="Base price / slot" name="basePricingPerSlot" type="number" value={form.basePricingPerSlot} onChange={onChange} />
          </div>
          <Field label="Location text" name="location" value={form.location} onChange={onChange} />
          {error ? <div className="error-banner">{error}</div> : null}
          <button className="button button-primary" type="submit" disabled={busy || !isAuthenticated}>
            {busy ? 'Saving...' : 'Add turf'}
          </button>
        </form>
      </Section>

      <Section eyebrow="Inventory" title="Available turfs" description="Loaded from GET /api/turfs/all.">
        <CardList
          items={cards}
          emptyText="No turfs found."
          renderItem={(item) => (
            <>
              <h3>{item.title}</h3>
              {item.body.map((line) => (
                <p key={line}>{line}</p>
              ))}
            </>
          )}
        />
      </Section>
    </>
  );
}
