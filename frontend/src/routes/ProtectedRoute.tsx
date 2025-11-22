import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

interface ProtectedRouteProps {
  children: React.ReactElement;
  requireAdmin?: boolean;
  allowDefaultPassword?: boolean;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, requireAdmin, allowDefaultPassword }) => {
  const { isAuthenticated, isAdmin, needsPasswordChange } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  // Force users with the default password to go to the change-password screen,
  // unless this particular route explicitly allows it (e.g. the change-password page itself).
  if (needsPasswordChange && !allowDefaultPassword) {
    return <Navigate to="/change-password" replace />;
  }

  if (requireAdmin && !isAdmin) {
    return (
      <div className="p-8">
        <div className="max-w-xl mx-auto rounded-lg border border-red-200 bg-red-50 p-6 text-red-800">
          <h2 className="text-lg font-semibold mb-2">Access denied</h2>
          <p className="text-sm">
            You do not have permission to access this section. Please contact an administrator if you believe this is a mistake.
          </p>
        </div>
      </div>
    );
  }

  return children;
};


