import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Field from '../components/Field';
import { useAuth } from '../context/AuthContext';

const blankSignup = {
  name: '',
  email: '',
  phone: '',
  password: ''
};

export default function AuthPage({ mode = 'login' }) {
  const navigate = useNavigate();
  const { signIn, signUp, refreshProfile } = useAuth();
  const [form, setForm] = useState(blankSignup);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const isSignup = mode === 'signup';

  const onChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const onSubmit = async (event) => {
    event.preventDefault();
    setBusy(true);
    setError('');
    try {
      let payload;
      if (isSignup) {
        payload = await signUp(form);
      } else {
        payload = await signIn(form.email, form.password);
      }
      if (payload?.token) {
        await refreshProfile(payload.token).catch(() => null);
      }
      navigate('/');
    } catch (err) {
      setError(err.message || 'Authentication failed');
    } finally {
      setBusy(false);
    }
  };

  return (
    <section className="section">
      <div className="section-head">
        <div>
          <p className="eyebrow">Authentication</p>
          <h2>{isSignup ? 'Create an account' : 'Sign in'}</h2>
          <p className="section-description">
            This React app talks to the existing Spring API. Use the same backend token flow the server already exposes.
          </p>
        </div>
      </div>
      <form className="panel form-panel" onSubmit={onSubmit}>
        {isSignup ? <Field label="Name" name="name" value={form.name} onChange={onChange} required /> : null}
        <Field label="Email" name="email" type="email" value={form.email} onChange={onChange} required />
        {isSignup ? <Field label="Phone" name="phone" value={form.phone} onChange={onChange} required /> : null}
        <Field label="Password" name="password" type="password" value={form.password} onChange={onChange} required />
        {error ? <div className="error-banner">{error}</div> : null}
        <div className="form-actions">
          <button className="button button-primary" type="submit" disabled={busy}>
            {busy ? 'Working...' : isSignup ? 'Create account' : 'Sign in'}
          </button>
        </div>
      </form>
    </section>
  );
}
