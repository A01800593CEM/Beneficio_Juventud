import type { NextAuthOptions } from "next-auth";
import CognitoProvider from "next-auth/providers/cognito";

export const authOptions: NextAuthOptions = {
  secret: process.env.NEXTAUTH_SECRET,
  session: { strategy: "jwt" },
  providers: [
    CognitoProvider({
      clientId: process.env.AUTH_COGNITO_ID!,
      clientSecret: process.env.AUTH_COGNITO_SECRET!, // <- mantener
      issuer: process.env.AUTH_COGNITO_ISSUER!,
      authorization: { params: { scope: "email openid profile" } },
    }),
  ],
  callbacks: {
    async jwt({ token, account, profile }) {
      if (account) {
        token.accessToken = account.access_token;
        token.idToken = account.id_token;
        token.refreshToken = account.refresh_token;
        const exp = typeof account.expires_in === "number" ? account.expires_in : 3600;
        token.expiresAt = Math.floor(Date.now() / 1000) + exp;
        if (profile) {
          token.cognitoUsername = (profile as { sub?: string }).sub;
          token.email = (profile as { email?: string }).email;
        }
      }
      const now = Math.floor(Date.now() / 1000);
      if (token.expiresAt && now >= (token.expiresAt as number)) {
        console.warn("Token expirado, agrega lógica de refresh si usas refresh_token");
      }
      return token;
    },
    async session({ session, token }) {
      const extendedSession = session as typeof session & {
        accessToken?: string;
        idToken?: string;
        expiresAt?: number;
        cognitoUsername?: string;
      };
      extendedSession.accessToken = token.accessToken as string;
      extendedSession.idToken = token.idToken as string;
      extendedSession.expiresAt = token.expiresAt as number;
      extendedSession.cognitoUsername = token.cognitoUsername as string;
      return session;
    },
  },
  pages: { signIn: "/login", error: "/login" },
  debug: process.env.NODE_ENV === "development",
};
