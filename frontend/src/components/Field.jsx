import React from 'react';

export default function Field({
  label,
  type = 'text',
  name,
  value,
  onChange,
  placeholder,
  options = [],
  rows = 4,
  hint,
  required = false
}) {
  return (
    <label className="field">
      <span className="field-label">
        {label}
        {required ? ' *' : ''}
      </span>
      {type === 'textarea' ? (
        <textarea
          name={name}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          rows={rows}
        />
      ) : type === 'select' ? (
        <select name={name} value={value} onChange={onChange}>
          {options.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      ) : (
        <input
          name={name}
          type={type}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          required={required}
        />
      )}
      {hint ? <span className="field-hint">{hint}</span> : null}
    </label>
  );
}
