import React, { useEffect, useState } from 'react';
import Field from '../components/Field';
import Section from '../components/Section';
import { useAuth } from '../context/AuthContext';

export default function ProfilePage() {
  const { user, updateProfile, refreshProfile, isAuthenticated } = useAuth();
  const [form, setForm] = useState({
    name: '',
    phone: '',
    profile: JSON.stringify({}, null, 2)
  });
  const [error, setError] = useState('');

  useEffect(() => {
    if (!user) return;
    setForm({
      name: user.name || '',
      phone: user.phone || '',
      profile: JSON.stringify(user.profile || {}, null, 2)
    });
  }, [user]);

  const onChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const onSubmit = async (event) => {
    event.preventDefault();
    setError('');
    try {
      let profileObject = {};
      try {
        profileObject = JSON.parse(form.profile || '{}');
      } catch {
        throw new Error('Profile JSON is invalid');
      }
      await updateProfile({
        name: form.name,
        phone: form.phone,
        profile: profileObject
      });
      await refreshProfile();
    } catch (err) {
      setError(err.message || 'Failed to update profile');
    }
  };

  return (
    <Section eyebrow="Account" title="Profile" description="Update the same user record the backend already stores.">
      <form className="panel form-panel" onSubmit={onSubmit}>
        <Field label="Name" name="name" value={form.name} onChange={onChange} required />
        <Field label="Phone" name="phone" value={form.phone} onChange={onChange} required />
        <Field label="Profile JSON" name="profile" type="textarea" rows={8} value={form.profile} onChange={onChange} />
        {error ? <div className="error-banner">{error}</div> : null}
        <button className="button button-primary" type="submit" disabled={!isAuthenticated}>
          Save profile
        </button>
      </form>
    </Section>
  );
}
