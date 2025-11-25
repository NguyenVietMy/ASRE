"use client";

import React, { createContext, useContext, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import {
  login,
  logout,
  register,
  refreshToken,
  type LoginRequest,
  type RegisterRequest,
} from "@/lib/api";
import { decodeJWT, isTokenExpired, type DecodedToken } from "@/lib/jwt";

interface User {
  id: string;
  email: string;
  role: string;
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  loading: boolean;
  signIn: (data: LoginRequest) => Promise<void>;
  signUp: (data: RegisterRequest) => Promise<void>;
  signOut: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    // Check for token in localStorage on mount
    const storedToken = localStorage.getItem("accessToken");
    if (storedToken) {
      if (isTokenExpired(storedToken)) {
        // Try to refresh
        refreshToken()
          .then((response) => {
            localStorage.setItem("accessToken", response.accessToken);
            setToken(response.accessToken);
            const decoded = decodeJWT(response.accessToken);
            if (decoded) {
              setUser({
                id: decoded.sub,
                email: decoded.email,
                role: decoded.role,
              });
            }
          })
          .catch(() => {
            localStorage.removeItem("accessToken");
            setToken(null);
            setUser(null);
          })
          .finally(() => setLoading(false));
      } else {
        setToken(storedToken);
        const decoded = decodeJWT(storedToken);
        if (decoded) {
          setUser({
            id: decoded.sub,
            email: decoded.email,
            role: decoded.role,
          });
        }
        setLoading(false);
      }
    } else {
      setLoading(false);
    }
  }, []);

  const signIn = async (data: LoginRequest) => {
    const response = await login(data);
    localStorage.setItem("accessToken", response.accessToken);
    setToken(response.accessToken);
    const decoded = decodeJWT(response.accessToken);
    if (decoded) {
      setUser({
        id: decoded.sub,
        email: decoded.email,
        role: decoded.role,
      });
    }
    router.push("/dashboard");
  };

  const signUp = async (data: RegisterRequest) => {
    const response = await register(data);
    localStorage.setItem("accessToken", response.accessToken);
    setToken(response.accessToken);
    const decoded = decodeJWT(response.accessToken);
    if (decoded) {
      setUser({
        id: decoded.sub,
        email: decoded.email,
        role: decoded.role,
      });
    }
    router.push("/dashboard");
  };

  const signOut = async () => {
    await logout();
    localStorage.removeItem("accessToken");
    setToken(null);
    setUser(null);
    router.push("/signin");
  };

  return (
    <AuthContext.Provider
      value={{ user, token, loading, signIn, signUp, signOut }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
