// Amplify v6 – reemplazo de amazon-cognito-identity-js
// Mantiene interfaces y funciones similares a tu implementación original.

import { Amplify } from "aws-amplify";
import {
  signIn,
  signUp,
  confirmSignUp as amplifyConfirmSignUp,
  signOut,
  getCurrentUser as amplifyGetCurrentUser,
  fetchAuthSession,
  type AuthUser,
} from "aws-amplify/auth";

// ==== Configuración Amplify (solo del lado del cliente) ====
const userPoolId = process.env.NEXT_PUBLIC_COGNITO_USER_POOL_ID!;
const userPoolClientId = process.env.NEXT_PUBLIC_COGNITO_CLIENT_ID!;

if (!Amplify.getConfig()) {
  Amplify.configure({
    Auth: {
      Cognito: {
        userPoolId,
        userPoolClientId,
        // Opcional: controla cómo inician sesión (email/username/phone)
        loginWith: {
          email: true,
          username: false,
          phone: false,
        },
        // Opcional: método de verificación para sign up
        signUpVerificationMethod: "code",
        // El region se toma del poolId; incluye identityPoolId solo si usas Federated Identities
      },
    },
    // region “global” de Amplify (usado por otros módulos). Opcional aquí.
    // (Amplify v6 no requiere poner region top-level si ya está en los recursos)
  });
}

// ==== Tipos compatibles con tu código ====
export interface LoginCredentials {
  email: string;
  password: string;
}

export interface SignUpData {
  email: string;
  password: string;
  name: string;
}

// Para mantener compatibilidad con tu firma previa que devolvía "CognitoUserSession",
// aquí devolvemos un objeto con los tokens de la sesión cuando exista.
// Puedes ajustar tu código consumidor para leer accessToken/idToken/refreshToken.
export interface CognitoUserSessionLike {
  accessToken?: string;
  idToken?: string;
  refreshToken?: string;
}

// ==== Login ====
export const cognitoLogin = async (
  credentials: LoginCredentials
): Promise<CognitoUserSessionLike> => {
  const { email, password } = credentials;

  const out = await signIn({
    username: email, // en Amplify, "username" = el identificador (usamos email)
    password,
  });

  // Manejo del "NEW_PASSWORD_REQUIRED" (cambio de contraseña forzado)
  // Amplify v6 expone nextStep.signInStep
  const step = out.nextStep?.signInStep;

  if (step === "CONFIRM_SIGN_IN_WITH_NEW_PASSWORD_REQUIRED") {
    // Mantengo la misma semántica que tu versión: rechazar con este error
    throw new Error("NEW_PASSWORD_REQUIRED");
  }

  // Si ya está autenticado, puedes recuperar la sesión (tokens)
  const session = await fetchAuthSession();

  return {
    accessToken: session?.tokens?.accessToken?.toString(),
    idToken: session?.tokens?.idToken?.toString(),
    refreshToken: undefined, // Refresh tokens are handled internally by Amplify v6
  };
};

// ==== Registro (Sign Up) ====
export const cognitoSignUp = async (userData: SignUpData) => {
  const { email, password, name } = userData;

  const res = await signUp({
    username: email,
    password,
    options: {
      userAttributes: {
        email,
        name,
      },
    },
  });

  // res.nextStep.describe si necesitas saber si requiere confirmación de código
  return res;
};

// ==== Confirmar registro con código ====
export const confirmSignUp = async (email: string, code: string) => {
  const res = await amplifyConfirmSignUp({
    username: email,
    confirmationCode: code,
  });
  return res;
};

// ==== Logout ====
export const cognitoLogout = async (): Promise<void> => {
  await signOut(); // Limpia tokens y estado local
};

// ==== Obtener usuario actual ====
export const getCurrentUser = async (): Promise<AuthUser | null> => {
  try {
    const user = await amplifyGetCurrentUser(); // { username, userId, signInDetails }
    // Si también quieres validar que haya sesión válida/tokens:
    const session = await fetchAuthSession();
    if (!session?.tokens?.accessToken) return null;
    return user;
  } catch {
    return null;
  }
};
