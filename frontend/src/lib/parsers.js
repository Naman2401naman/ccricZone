export const parseDelimitedLines = (value = '') => {
  return String(value)
    .split(/[\n,]/g)
    .map((item) => item.trim())
    .filter(Boolean);
};

export const parseMemberLines = (value = '') => {
  return parseDelimitedLines(value).map((entry) => {
    const [namePart = '', emailPart = '', userIdPart = ''] = entry
      .split('|')
      .map((item) => item.trim());

    const email = emailPart || (namePart.includes('@') ? namePart : '');
    const name = emailPart ? namePart : namePart.replace(/@.*/, '');
    const userId = userIdPart || '';

    return {
      name: name.trim(),
      email: email.trim(),
      userId: userId.trim()
    };
  });
};

export const stringifyMemberLines = (members = []) =>
  members
    .map((member) => [member?.name || '', member?.email || '', member?.userId || ''].join('|'))
    .join('\n');
