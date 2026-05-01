import { ChevronRight, LogOut, X } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import { useState } from 'react';

interface ProfileProps {
  onBack: () => void;
}

export function Profile({ onBack }: ProfileProps) {
  const { user, logout } = useAuth();
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [passwordError, setPasswordError] = useState('');
  const [passwordSuccess, setPasswordSuccess] = useState(false);

  const handleChangePassword = () => {
    setPasswordError('');

    if (!currentPassword || !newPassword || !confirmPassword) {
      setPasswordError('Todos los campos son obligatorios');
      return;
    }

    if (newPassword !== confirmPassword) {
      setPasswordError('Las contraseñas nuevas no coinciden');
      return;
    }

    if (newPassword.length < 6) {
      setPasswordError('La contraseña debe tener al menos 6 caracteres');
      return;
    }

    // Simulate password change (in a real app, this would call an API)
    setPasswordSuccess(true);
    setTimeout(() => {
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
      setShowPasswordModal(false);
      setPasswordSuccess(false);
    }, 2000);
  };

  return (
    <div className="min-h-screen bg-[#F7F5F0] p-4 md:p-6 lg:p-8 flex items-start justify-center pt-8 md:pt-12 lg:pt-16">
      <div className="w-full max-w-md lg:max-w-2xl">
        {/* Header */}
        <div className="mb-6 md:mb-8">
          <h1 className="text-[#3D2C8D] mb-1 text-[30px]">Mi perfil</h1>
        </div>

        {/* Profile Card */}
        <div className="bg-white rounded-2xl shadow-sm border border-[#D8D0F0] p-6 md:p-8 mb-5">
          <div className="flex flex-col items-center text-center">
            {/* Avatar */}
            <div className="w-24 h-24 bg-[#FFD200] rounded-full flex items-center justify-center mb-4">
              <span className="text-4xl font-semibold text-white text-[#000000]">
                {user?.name?.charAt(0).toUpperCase()}
              </span>
            </div>

            {/* User Info */}
            <h2 className="text-slate-900 mb-1 text-[24px]">{user?.name}</h2>
            <p className="text-slate-500">{user?.email}</p>
          </div>
        </div>

        {/* Account Details Card */}
        <div className="bg-white rounded-2xl shadow-sm border border-[#D8D0F0] mb-5">
          {/* Name */}
          <button className="w-full flex items-center justify-between p-5 hover:bg-slate-50 transition-colors border-b border-slate-100">
            <span className="text-slate-900">Nombre</span>
            <div className="flex items-center gap-2">
              <span className="text-slate-400">{user?.name}</span>
              <ChevronRight className="w-5 h-5 text-slate-400" />
            </div>
          </button>

          {/* Email */}
          <button className="w-full flex items-center justify-between p-5 hover:bg-slate-50 transition-colors border-b border-slate-100">
            <span className="text-slate-900">Correo</span>
            <div className="flex items-center gap-2">
              <span className="text-slate-400">{user?.email}</span>
              <ChevronRight className="w-5 h-5 text-slate-400" />
            </div>
          </button>

          {/* Password */}
          <button
            onClick={() => setShowPasswordModal(true)}
            className="w-full flex items-center justify-between p-5 hover:bg-slate-50 transition-colors border-b border-slate-100"
          >
            <span className="text-slate-900">Contraseña</span>
            <div className="flex items-center gap-2">
              <span className="text-slate-400">••••••••</span>
              <ChevronRight className="w-5 h-5 text-slate-400" />
            </div>
          </button>

          {/* Notifications */}
          <button className="w-full flex items-center justify-between p-5 hover:bg-slate-50 transition-colors">
            <span className="text-slate-900">Notificaciones</span>
            <div className="flex items-center gap-2">
              <span className="text-green-600">Activas</span>
              <ChevronRight className="w-5 h-5 text-slate-400" />
            </div>
          </button>
        </div>

        {/* Logout Button */}
        <div className="bg-white rounded-2xl shadow-sm border border-[#D8D0F0]">
          <button
            onClick={logout}
            className="w-full flex items-center justify-between p-5 hover:bg-red-50 transition-colors"
          >
            <span className="text-red-600">Cerrar sesión</span>
            <ChevronRight className="w-5 h-5 text-red-600" />
          </button>
        </div>

        {/* Back Button */}
        <div className="mt-6">
          <button
            onClick={onBack}
            className="w-full text-slate-500 py-3 hover:text-slate-700 transition-colors"
          >
            Volver
          </button>
        </div>
      </div>

      {/* Password Change Modal */}
      {showPasswordModal && (
        <>
          <div className="fixed inset-0 bg-black/30 backdrop-blur-sm z-40" onClick={() => setShowPasswordModal(false)} />

          <div className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 z-50 w-full max-w-md mx-4">
            <div className="bg-white rounded-2xl shadow-2xl border border-[#D8D0F0] p-6">
              {/* Header */}
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-xl text-[#3D2C8D]">Cambiar contraseña</h2>
                <button
                  onClick={() => setShowPasswordModal(false)}
                  className="p-2 hover:bg-slate-100 rounded-lg transition-colors"
                >
                  <X className="w-5 h-5 text-slate-500" />
                </button>
              </div>

              {/* Success Message */}
              {passwordSuccess && (
                <div className="mb-4 p-4 bg-green-50 border border-green-200 rounded-xl">
                  <p className="text-green-700 text-sm">¡Contraseña actualizada exitosamente!</p>
                </div>
              )}

              {/* Error Message */}
              {passwordError && (
                <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-xl">
                  <p className="text-red-700 text-sm">{passwordError}</p>
                </div>
              )}

              {/* Form */}
              <div className="space-y-4">
                <div>
                  <label className="block text-sm text-slate-700 mb-2">Contraseña actual</label>
                  <input
                    type="password"
                    value={currentPassword}
                    onChange={(e) => setCurrentPassword(e.target.value)}
                    className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary"
                    placeholder="••••••••"
                  />
                </div>

                <div>
                  <label className="block text-sm text-slate-700 mb-2">Nueva contraseña</label>
                  <input
                    type="password"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary"
                    placeholder="••••••••"
                  />
                </div>

                <div>
                  <label className="block text-sm text-slate-700 mb-2">Confirmar nueva contraseña</label>
                  <input
                    type="password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary"
                    placeholder="••••••••"
                  />
                </div>
              </div>

              {/* Actions */}
              <div className="flex gap-3 mt-6">
                <button
                  onClick={handleChangePassword}
                  className="flex-1 bg-[#0D0D0D] text-white border-2 border-[#3D2C8D] py-3 rounded-xl hover:shadow-md active:scale-[0.98] transition-all"
                >
                  Actualizar contraseña
                </button>
                <button
                  onClick={() => {
                    setShowPasswordModal(false);
                    setPasswordError('');
                    setCurrentPassword('');
                    setNewPassword('');
                    setConfirmPassword('');
                  }}
                  className="px-6 py-3 text-slate-500 hover:text-slate-700 transition-colors"
                >
                  Cancelar
                </button>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
