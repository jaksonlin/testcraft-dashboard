import React, { useEffect, useState } from 'react';
import { api, type UserSummary } from '../../lib/api';

type NewUserRole = 'USER' | 'ADMIN';

const UserManagementTab: React.FC = () => {
  const [users, setUsers] = useState<UserSummary[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const [newUsername, setNewUsername] = useState('');
  const [newUserRole, setNewUserRole] = useState<NewUserRole>('USER');

  const loadUsers = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await api.users.list();
      setUsers(data);
    } catch (err) {
      console.error('Failed to load users', err);
      setError('Failed to load users');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadUsers();
  }, []);

  const handleCreateUser = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newUsername.trim()) {
      return;
    }
    setError(null);
    setSuccess(null);
    try {
      const roles =
        newUserRole === 'ADMIN' ? ['ROLE_ADMIN', 'ROLE_USER'] : ['ROLE_USER'];
      await api.users.create({ username: newUsername.trim(), roles });
      setSuccess('User created with a default password');
      setNewUsername('');
      setNewUserRole('USER');
      void loadUsers();
    } catch (err: any) {
      console.error('Failed to create user', err);
      const message =
        err?.response?.data && typeof err.response.data === 'string'
          ? err.response.data
          : 'Failed to create user';
      setError(message);
    }
  };

  return (
    <div className="space-y-8 text-gray-900 dark:text-gray-100">
      <section>
        <h2 className="text-lg font-semibold mb-2">Create new user</h2>
        <p className="text-sm text-gray-600 dark:text-gray-300 mb-4">
          New users are created with a secure default password. The password is{' '}
          <span className="font-semibold">not shown</span> here; share it with the user out of band
          and ask them to change it after first login.
        </p>

        <form onSubmit={handleCreateUser} className="space-y-4 max-w-md">
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-1">
              Username
            </label>
            <input
              type="text"
              value={newUsername}
              onChange={(e) => setNewUsername(e.target.value)}
              className="w-full rounded-md border border-gray-300 dark:border-gray-600 px-3 py-2 text-sm
                         bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100
                         focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="e.g. jdoe"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-1">
              Role
            </label>
            <select
              value={newUserRole}
              onChange={(e) => setNewUserRole(e.target.value as NewUserRole)}
              className="w-full rounded-md border border-gray-300 dark:border-gray-600 px-3 py-2 text-sm
                         bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100
                         focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="USER">User (read-only access)</option>
              <option value="ADMIN">Admin (full access, including scans & settings)</option>
            </select>
          </div>

          <button
            type="submit"
            disabled={!newUsername.trim()}
            className="inline-flex items-center rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:bg-blue-300"
          >
            Create user
          </button>

          {error && (
            <p className="mt-2 text-sm text-red-600 dark:text-red-400">
              {error}
            </p>
          )}
          {success && (
            <p className="mt-2 text-sm text-green-600 dark:text-green-400">
              {success}
            </p>
          )}
        </form>
      </section>

      <section>
        <h2 className="text-lg font-semibold mb-2">Existing users</h2>
        {loading ? (
          <p className="text-sm text-gray-600 dark:text-gray-300">Loading users…</p>
        ) : users.length === 0 ? (
          <p className="text-sm text-gray-600 dark:text-gray-300">No users found.</p>
        ) : (
          <div className="overflow-x-auto border border-gray-200 dark:border-gray-700 rounded-lg">
            <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700 text-sm">
              <thead className="bg-gray-50 dark:bg-gray-800/60">
                <tr>
                  <th className="px-4 py-2 text-left font-medium text-gray-700 dark:text-gray-200">Username</th>
                  <th className="px-4 py-2 text-left font-medium text-gray-700 dark:text-gray-200">Roles</th>
                  <th className="px-4 py-2 text-left font-medium text-gray-700 dark:text-gray-200">Status</th>
                  <th className="px-4 py-2 text-left font-medium text-gray-700 dark:text-gray-200">
                    Default password
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 dark:divide-gray-700 bg-white dark:bg-gray-900">
                {users.map((user) => (
                  <tr key={user.id}>
                    <td className="px-4 py-2">{user.username}</td>
                    <td className="px-4 py-2">
                      {user.roles && user.roles.length > 0
                        ? user.roles.join(', ')
                        : '—'}
                    </td>
                    <td className="px-4 py-2">
                      {user.enabled ? (
                        <span className="inline-flex items-center rounded-full bg-green-50 dark:bg-green-900/30 px-2 py-0.5 text-xs font-medium text-green-800 dark:text-green-300">
                          Active
                        </span>
                      ) : (
                        <span className="inline-flex items-center rounded-full bg-gray-100 dark:bg-gray-800 px-2 py-0.5 text-xs font-medium text-gray-700 dark:text-gray-300">
                          Disabled
                        </span>
                      )}
                    </td>
                    <td className="px-4 py-2 text-xs text-gray-600 dark:text-gray-300">
                      {user.defaultPasswordInUse ? 'In use' : 'Changed'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
};

export default UserManagementTab;


