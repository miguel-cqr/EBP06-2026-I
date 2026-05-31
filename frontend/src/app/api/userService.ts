import api from './client';

export const userService = {
  updatePassword(currentPassword: string, newPassword: string) {
    return api.put('/users/password', { currentPassword, newPassword });
  },

  getSessions() {
    return api.get('/users/sessions');
  },

  terminateSessions() {
    return api.delete('/users/sessions');
  },

  updateProfile(data: { fullName: string; email: string; currency?: string }) {
    return api.put('/users/me', data);
  }
};