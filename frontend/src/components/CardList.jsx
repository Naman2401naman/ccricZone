import React from 'react';
import { shortText } from '../lib/format';

export default function CardList({ items = [], emptyText = 'No data yet.', renderItem }) {
  if (!items.length) {
    return <div className="empty-state">{emptyText}</div>;
  }

  return (
    <div className="card-grid">
      {items.map((item, index) => (
        <div className="data-card" key={item?._id || item?.id || index}>
          {renderItem ? renderItem(item, index) : <pre>{shortText(JSON.stringify(item, null, 2), 220)}</pre>}
        </div>
      ))}
    </div>
  );
}
