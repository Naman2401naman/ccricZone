import React, { useEffect, useMemo, useState } from 'react';
import Field from '../components/Field';
import Section from '../components/Section';
import CardList from '../components/CardList';
import { useAuth } from '../context/AuthContext';
import { formatDateTime, shortText } from '../lib/format';

const blankForm = {
  content: '',
  imageUrl: ''
};

export default function PostsPage() {
  const { request, isAuthenticated } = useAuth();
  const [posts, setPosts] = useState([]);
  const [form, setForm] = useState(blankForm);
  const [error, setError] = useState('');

  const load = async () => {
    const payload = await request('/posts');
    setPosts(Array.isArray(payload.posts) ? payload.posts : []);
  };

  useEffect(() => {
    load().catch((err) => setError(err.message || 'Failed to load posts'));
  }, []);

  const onChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const createPost = async (event) => {
    event.preventDefault();
    setError('');
    try {
      await request('/posts', { method: 'POST', body: form });
      setForm(blankForm);
      await load();
    } catch (err) {
      setError(err.message || 'Failed to create post');
    }
  };

  const cards = useMemo(
    () =>
      posts.map((post) => ({
        id: post._id || post.id,
        title: shortText(post.content || 'Post', 80),
        body: [`Likes: ${post.likes?.length || 0}`, `Created: ${formatDateTime(post.createdAt)}`],
        post
      })),
    [posts]
  );

  const toggleLike = async (postId) => {
    await request(`/posts/${postId}/like`, { method: 'POST' });
    await load();
  };

  return (
    <>
      <Section eyebrow="Social" title="Posts" description="Feed operations from the existing backend.">
        <form className="panel form-panel" onSubmit={createPost}>
          <Field label="Content" name="content" type="textarea" value={form.content} onChange={onChange} rows={5} />
          <Field label="Image URL" name="imageUrl" value={form.imageUrl} onChange={onChange} />
          {error ? <div className="error-banner">{error}</div> : null}
          <button className="button button-primary" type="submit" disabled={!isAuthenticated}>
            Create post
          </button>
        </form>
      </Section>

      <Section eyebrow="Feed" title="Latest posts" description="Read and like posts.">
        <CardList
          items={cards}
          emptyText="No posts found."
          renderItem={(item) => (
            <>
              <h3>{item.title}</h3>
              <p>{item.post.content || 'No content'}</p>
              <p>{item.body[1]}</p>
              <div className="row-actions">
                <button className="button button-secondary" type="button" onClick={() => toggleLike(item.id)}>
                  Like / unlike
                </button>
              </div>
            </>
          )}
        />
      </Section>
    </>
  );
}
