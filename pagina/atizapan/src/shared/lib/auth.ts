// src/lib/auth.ts
import type { NextAuthOptions, Account, User, Profile, Session } from "next-auth";
import { JWT } from "next-auth/jwt";
import CognitoProvider from "next-auth/providers/cognito";
import CredentialsProvider from "next-auth/providers/credentials";
import * as jose from "jose";

// Type definitions for Cognito and auth objects
interface CognitoError {
  code?: string;
  name?: string;
  message?: string;
}

interface ExtendedUser extends User {
  idToken?: string;
}

interface ExtendedAccount extends Account {
  expires_in?: number;
}

interface CognitoProfile extends Profile {
  sub?: string;
  email?: string;
  profile?: string;
}

interface ExtendedJWT extends JWT {
  accessToken?: string;
  idToken?: string;
  refreshToken?: string;
  expiresAt?: number;
  cognitoUsername?: string;
  email?: string;
  profile?: string;
}

interface ExtendedSession extends Session {
  accessToken?: string;
  idToken?: string;
  expiresAt?: number;
  cognitoUsername?: string;
  profile?: string;
}

const region = process.env.NEXT_PUBLIC_COGNITO_REGION!;
const userPoolId = process.env.NEXT_PUBLIC_COGNITO_USER_POOL_ID!;
const clientId = process.env.AUTH_COGNITO_ID!;

const issuer = `https://cognito-idp.${region}.amazonaws.com/${userPoolId}`;
const jwks = jose.createRemoteJWKSet(new URL(`${issuer}/.well-known/jwks.json`));

export const getProfileBasedRedirect = (profile?: string): string => {
  switch (profile) {
    case "admin":
      return "/admin";
    case "colaborator":
    case "collaborator":
      return "/colaborator";
    default:
      return "/usuario";
  }
};

export const authOptions: NextAuthOptions = {
  secret: process.env.NEXTAUTH_SECRET,
  session: { strategy: "jwt" },

  providers: [
    // Hosted UI (OIDC)
    CognitoProvider({
      clientId: process.env.AUTH_COGNITO_ID!,
      clientSecret: process.env.AUTH_COGNITO_SECRET!,
      issuer,
      authorization: { params: { scope: "email openid profile" } },
    }),

    // Bridge: recibir idToken desde Amplify y crear sesión NextAuth
    CredentialsProvider({
      id: "credentials",
      name: "cognito-bridge",
      credentials: {
        idToken: { label: "idToken", type: "text" },
      },
      async authorize(credentials) {
        console.log("🔍 Credentials Provider - authorize called");
        
        if (!credentials?.idToken) {
          console.log("❌ No idToken provided in credentials");
          return null;
        }

        console.log("🔍 IdToken recibido, length:", credentials.idToken.length);

        try {
          // Log de configuración
          console.log("🔍 Verificando JWT con:");
          console.log("  - Issuer:", issuer);
          console.log("  - Audience:", clientId);
          
          // Verificamos firma y claims del idToken de Cognito
          const { payload } = await jose.jwtVerify(credentials.idToken, jwks, {
            issuer,
            audience: clientId,
          });

          console.log("✅ JWT verificado exitosamente");
          console.log("✅ Subject:", payload.sub);
          console.log("✅ Email:", payload.email);
          console.log("✅ Token use:", payload.token_use);

          // Devuelve un "user" para NextAuth
          const user = {
            id: payload.sub as string,
            email: (payload.email as string) || undefined,
            name: (payload.name as string) || (payload.email as string) || undefined,
            profile: (payload.profile as string) || undefined,
            // Guardamos el idToken para usarlo en el callback jwt
            idToken: credentials.idToken,
          };
          
          console.log("✅ User object created:", { id: user.id, email: user.email, profile: user.profile });
          return user;
          
        } catch (error) {
          const cognitoError = error as CognitoError;
          console.error("❌ Error verificando JWT:");
          console.error("  - Error name:", cognitoError?.name);
          console.error("  - Error message:", cognitoError?.message);
          console.error("  - Error code:", cognitoError?.code);
          console.error("  - Full error:", error);
          
          // Intentar decodificar sin verificar para debug
          try {
            const parts = credentials.idToken.split('.');
            const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')));
            console.log("🔍 Payload sin verificar:", {
              iss: payload.iss,
              aud: payload.aud,
              token_use: payload.token_use,
              exp: new Date(payload.exp * 1000).toISOString()
            });
          } catch (decodeError) {
            console.error("❌ No se pudo decodificar el token para debug:", decodeError);
          }
          
          return null;
        }
      },
    }),
  ],

  callbacks: {
    async jwt({ token, account, user, profile }): Promise<ExtendedJWT> {
      const extendedToken = token as ExtendedJWT;
      const extendedAccount = account as ExtendedAccount;
      const extendedUser = user as ExtendedUser;
      const cognitoProfile = profile as CognitoProfile;

      // Hosted UI (OIDC)
      if (extendedAccount && extendedAccount.provider === "cognito") {
        console.log("🔐 Hosted UI login");
        extendedToken.accessToken = extendedAccount.access_token;
        extendedToken.idToken = extendedAccount.id_token;
        extendedToken.refreshToken = extendedAccount.refresh_token;
        const exp = typeof extendedAccount.expires_in === "number" ? extendedAccount.expires_in : 3600;
        extendedToken.expiresAt = Math.floor(Date.now() / 1000) + exp;

        if (cognitoProfile) {
          extendedToken.cognitoUsername = cognitoProfile.sub;
          extendedToken.email = cognitoProfile.email;
          extendedToken.profile = cognitoProfile.profile;
        }
      }

      // Bridge con Credentials: cuando el usuario viene del custom login
      if (extendedUser && extendedUser.idToken && extendedAccount?.provider === "credentials") {
        console.log("🔐 Custom login bridge");
        extendedToken.idToken = extendedUser.idToken;
        extendedToken.accessToken = extendedUser.idToken; // Para API que acepta idToken
        extendedToken.expiresAt = Math.floor(Date.now() / 1000) + 3600;
        extendedToken.cognitoUsername = extendedUser.id;
        extendedToken.email = extendedUser.email || undefined;
        extendedToken.profile = (extendedUser as { profile?: string }).profile || undefined;
      }

      // Verificar si el token está expirado
      const now = Math.floor(Date.now() / 1000);
      if (extendedToken.expiresAt && now >= extendedToken.expiresAt) {
        console.warn("⚠️ Token expirado");
      }

      return extendedToken;
    },

    async session({ session, token }): Promise<ExtendedSession> {
      const extendedSession = { ...session } as ExtendedSession;
      const extendedToken = token as ExtendedJWT;

      extendedSession.accessToken = extendedToken.accessToken;
      extendedSession.idToken = extendedToken.idToken;
      extendedSession.expiresAt = extendedToken.expiresAt;
      extendedSession.cognitoUsername = extendedToken.cognitoUsername;
      extendedSession.profile = extendedToken.profile;
      return extendedSession;
    },

    async signIn({ user, account }) {
      // Permitir todos los sign-ins válidos
      if (account?.provider === "credentials" && user) {
        console.log("✅ Credentials sign-in permitido para:", user.email);
        return true;
      }

      if (account?.provider === "cognito" && user) {
        console.log("✅ Cognito hosted UI sign-in permitido para:", user.email);
        return true;
      }

      return true;
    },

    async redirect({ url, baseUrl }) {
      // Si la URL ya es absoluta y pertenece al dominio base, usarla
      if (url.startsWith(baseUrl)) return url;

      // Si es una ruta relativa, construir la URL completa
      if (url.startsWith("/")) return `${baseUrl}${url}`;

      // Por defecto, redirigir al baseUrl
      return baseUrl;
    },
  },

  pages: { 
    signIn: "/login", 
    error: "/login" 
  },
  debug: process.env.NODE_ENV === "development",
};