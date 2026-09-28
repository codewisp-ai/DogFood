import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

interface ProtectedRouteProps {
  children: JSX.Element;
  requiredRole?: string;
  eventId?: string;
}

export const ProtectedRoute = ({ children, requiredRole, eventId }: ProtectedRouteProps) => {
  const { user, hasRole } = useAuth();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRole && eventId && !hasRole(eventId, requiredRole)) {
    return <Navigate to="/" replace />;
  }

  return children;
};
