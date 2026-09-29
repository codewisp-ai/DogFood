import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { jwtDecode } from 'jwt-decode';

interface User {
  id: string;
  email: string;
  eventRoles: string[]; // e.g. "eventId:ROLE"
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  login: (token: string) => void;
  logout: () => void;
  hasRole: (eventId: string, role: string) => boolean;
  getUserRole: () => 'ORGANIZER' | 'JUDGE' | 'PARTICIPANT' | null;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [token, setToken] = useState<string | null>(localStorage.getItem('token'));
  const [user, setUser] = useState<User | null>(null);

  useEffect(() => {
    if (token) {
      try {
        const decoded: any = jwtDecode(token);
        setUser({
          id: decoded.sub,
          email: decoded.email,
          eventRoles: decoded.eventRoles || []
        });
        localStorage.setItem('token', token);
      } catch (e) {
        logout();
      }
    } else {
      setUser(null);
      localStorage.removeItem('token');
    }
  }, [token]);

  const login = (newToken: string) => {
    // Write to localStorage immediately so ProtectedRoute sees it on first render
    localStorage.setItem('token', newToken);
    // Decode user synchronously so React can batch this with the navigate() call
    try {
      const decoded: any = jwtDecode(newToken);
      setUser({
        id: decoded.sub,
        email: decoded.email,
        eventRoles: decoded.eventRoles || []
      });
    } catch (e) {
      // ignore bad token
    }
    setToken(newToken);
  };

  const logout = () => {
    localStorage.removeItem('token');
    setUser(null);
    setToken(null);
  };
  
  const hasRole = (eventId: string, role: string) => {
    if (!user) return false;
    if (user.eventRoles.includes('GLOBAL:ADMIN')) return true;
    return user.eventRoles.includes(`${eventId}:${role}`);
  };

  // Extract primary role from any eventRole claim (e.g. "uuid:ORGANIZER" => "ORGANIZER")
  const getUserRole = (): 'ORGANIZER' | 'JUDGE' | 'PARTICIPANT' | null => {
    if (!user) return null;
    if (user.eventRoles.some(r => r.endsWith(':ORGANIZER'))) return 'ORGANIZER';
    if (user.eventRoles.some(r => r.endsWith(':JUDGE'))) return 'JUDGE';
    if (user.eventRoles.some(r => r.endsWith(':PARTICIPANT'))) return 'PARTICIPANT';
    return 'PARTICIPANT'; // default fallback for registered users
  };

  return (
    <AuthContext.Provider value={{ user, token, login, logout, hasRole, getUserRole }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
};
