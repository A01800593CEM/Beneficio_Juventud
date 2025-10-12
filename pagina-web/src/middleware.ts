// src/middleware.ts
import { withAuth } from "next-auth/middleware";
import { NextResponse } from "next/server";

export default withAuth(
  function middleware(req) {
    const { pathname } = req.nextUrl;
    const token = req.nextauth.token;

    // Log minimo para debugging
    if (process.env.NODE_ENV === "development") {
      console.log("🔐 Middleware check:", pathname, !!token, "profile:", token?.profile);
    }

    // Redirigir según el perfil si el usuario está en la ruta incorrecta
    const profile = token?.profile as string;

    if (profile === "admin") {
      if (pathname.startsWith("/colaborator")) {
        console.log("🔄 Redirecting admin from colaborator to admin");
        return NextResponse.redirect(new URL("/admin", req.url));
      }
      if (pathname.startsWith("/usuario")) {
        console.log("🔄 Redirecting admin from usuario to admin");
        return NextResponse.redirect(new URL("/admin", req.url));
      }
    }

    if (profile === "colaborator" || profile === "collaborator") {
      if (pathname.startsWith("/admin")) {
        console.log("🔄 Redirecting colaborator from admin to colaborator");
        return NextResponse.redirect(new URL("/colaborator", req.url));
      }
      if (pathname.startsWith("/usuario")) {
        console.log("🔄 Redirecting colaborator from usuario to colaborator");
        return NextResponse.redirect(new URL("/colaborator", req.url));
      }
    }

    // Usuario normal (sin perfil definido o perfil no reconocido)
    if (!profile || (profile !== "admin" && profile !== "colaborator" && profile !== "collaborator")) {
      if (pathname.startsWith("/admin")) {
        console.log("🔄 Redirecting normal user from admin to usuario");
        return NextResponse.redirect(new URL("/usuario", req.url));
      }
      if (pathname.startsWith("/colaborator")) {
        console.log("🔄 Redirecting normal user from colaborator to usuario");
        return NextResponse.redirect(new URL("/usuario", req.url));
      }
    }

    // Verificación de expiración SOLO en middleware (más eficiente)
    if (token?.expiresAt) {
      const now = Math.floor(Date.now() / 1000);
      const timeUntilExpiry = (token.expiresAt as number) - now;
      
      // Solo actuar si está realmente expirado (no anticipado)
      if (timeUntilExpiry <= 0) {
        console.log("🔐 Token expirado en middleware, redirigiendo...");
        
        const response = NextResponse.redirect(new URL("/login", req.url));
        
        // Limpiar cookies de NextAuth
        const cookiesToClear = [
          "next-auth.session-token",
          "__Secure-next-auth.session-token", 
          "next-auth.csrf-token",
          "__Secure-next-auth.csrf-token",
          "next-auth.callback-url",
          "__Secure-next-auth.callback-url"
        ];

        cookiesToClear.forEach(cookieName => {
          response.cookies.set(cookieName, "", {
            maxAge: 0,
            expires: new Date(0),
            path: "/",
            secure: process.env.NODE_ENV === "production",
            httpOnly: true,
            sameSite: "lax"
          });
        });
        
        return response;
      }
    }

    return NextResponse.next();
  },
  {
    callbacks: {
      authorized: ({ token }) => {
        // Verificación simple: existe token y no está expirado
        if (!token) return false;
        
        if (token.expiresAt) {
          const now = Math.floor(Date.now() / 1000);
          return now < (token.expiresAt as number);
        }
        
        return true;
      },
    },
  }
);

export const config = {
  matcher: [
    "/admin/:path*",
    "/colaborator/:path*",
    "/usuario/:path*",
    // Agregar más rutas protegidas aquí
  ],
};