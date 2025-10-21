// src/lib/cognito.ts - Versión completa con recuperación de contraseña
import { Amplify } from "aws-amplify";
import {
  signIn,
  signOut,
  signUp,
  confirmSignUp as amplifyConfirmSignUp,
  resendSignUpCode as amplifyResendSignUpCode,
  getCurrentUser as amplifyGetCurrentUser,
  fetchAuthSession,
  confirmSignIn,
  resetPassword,
  confirmResetPassword,
  type AuthUser,
} from "aws-amplify/auth";

// ========= Config única (evita re-configurar en HMR) =========
let _amplifyReady = false;
function ensureAmplifyConfigured() {
  if (_amplifyReady) return;

  const userPoolId = process.env.NEXT_PUBLIC_COGNITO_USER_POOL_ID!;
  const userPoolClientId = process.env.NEXT_PUBLIC_COGNITO_CLIENT_ID!;

  Amplify.configure({
    Auth: {
      Cognito: {
        userPoolId,
        userPoolClientId,
        loginWith: { email: true, username: false, phone: false },
        signUpVerificationMethod: "code",
      },
    },
  });

  _amplifyReady = true;
}

// ========= Tipos =========
export interface LoginCredentials {
  email: string;
  password: string;
}

export interface SignUpData {
  email: string;
  password: string;
  name: string;
}

export interface SessionTokens {
  accessToken?: string;
  idToken?: string;
  refreshToken?: string;
}

export type AuthErrorCode =
  | "NEW_PASSWORD_REQUIRED"
  | "USER_NOT_CONFIRMED"
  | "USER_NOT_FOUND"
  | "NOT_AUTHORIZED"
  | "CODE_MISMATCH"
  | "LIMIT_EXCEEDED"
  | "EXPIRED_CODE"
  | "INVALID_PASSWORD"
  | "UNKNOWN";

function mapAuthError(e: unknown): AuthErrorCode {
  const error = e as { name?: string; message?: string };
  const msg = error?.name || error?.message || "";
  if (/NEW_PASSWORD_REQUIRED/i.test(msg)) return "NEW_PASSWORD_REQUIRED";
  if (/UserNotConfirmedException/i.test(msg)) return "USER_NOT_CONFIRMED";
  if (/UserNotFoundException/i.test(msg)) return "USER_NOT_FOUND";
  if (/NotAuthorizedException|Incorrect username or password/i.test(msg)) return "NOT_AUTHORIZED";
  if (/CodeMismatchException/i.test(msg)) return "CODE_MISMATCH";
  if (/ExpiredCodeException/i.test(msg)) return "EXPIRED_CODE";
  if (/InvalidPasswordException/i.test(msg)) return "INVALID_PASSWORD";
  if (/LimitExceededException/i.test(msg)) return "LIMIT_EXCEEDED";
  return "UNKNOWN";
}

// ========= Helpers de sesión =========
export async function getSessionTokens(): Promise<SessionTokens> {
  ensureAmplifyConfigured();
  const { tokens } = await fetchAuthSession();
  return {
    accessToken: tokens?.accessToken?.toString(),
    idToken: tokens?.idToken?.toString(),
    refreshToken: undefined, // refreshToken not available directly from Amplify v6
  };
}

export async function isAuthenticated(): Promise<boolean> {
  ensureAmplifyConfigured();
  try {
    const { tokens } = await fetchAuthSession();
    return Boolean(tokens?.accessToken);
  } catch {
    return false;
  }
}

// ========= Login / Flujo de contraseña nueva =========
export async function cognitoLogin(
  { email, password }: LoginCredentials
): Promise<SessionTokens> {
  ensureAmplifyConfigured();

  try {
    const res = await signIn({ username: email, password });
    const step = res.nextStep?.signInStep;

    if (step === "CONFIRM_SIGN_IN_WITH_NEW_PASSWORD_REQUIRED") {
      const err = new Error("NEW_PASSWORD_REQUIRED");
      (err as Error & { code?: string }).code = "NEW_PASSWORD_REQUIRED";
      throw err;
    }

    return await getSessionTokens();
  } catch (e) {
    const code = mapAuthError(e);
    if (code === "NEW_PASSWORD_REQUIRED") throw new Error("NEW_PASSWORD_REQUIRED");
    throw e;
  }
}

export async function completeNewPassword(newPassword: string): Promise<SessionTokens> {
  ensureAmplifyConfigured();
  const res = await confirmSignIn({ challengeResponse: newPassword });
  if (res.nextStep?.signInStep && res.nextStep.signInStep !== "DONE") {
    throw new Error("No se pudo completar el cambio de contraseña.");
  }
  return await getSessionTokens();
}

// ========= Registro / Confirmación =========
export async function cognitoSignUp({ email, password, name }: SignUpData) {
  ensureAmplifyConfigured();
  return await signUp({
    username: email,
    password,
    options: { userAttributes: { email, name } },
  });
}

export async function confirmSignUp(email: string, code: string) {
  ensureAmplifyConfigured();
  return await amplifyConfirmSignUp({ username: email, confirmationCode: code });
}

export async function resendSignUpCode(email: string) {
  ensureAmplifyConfigured();
  return await amplifyResendSignUpCode({ username: email });
}

// ========= Recuperación de contraseña =========
export async function forgotPassword(email: string) {
  ensureAmplifyConfigured();
  
  try {
    const result = await resetPassword({
      username: email,
    });
    
    console.log("✅ Código de recuperación enviado:", result.nextStep);
    return result;
  } catch (error) {
    console.error("❌ Error enviando código de recuperación:", error);
    throw error;
  }
}

export async function resetPasswordWithCode(
  email: string, 
  code: string, 
  newPassword: string
) {
  ensureAmplifyConfigured();
  
  try {
    const result = await confirmResetPassword({
      username: email,
      confirmationCode: code,
      newPassword,
    });
    
    console.log("✅ Contraseña restablecida exitosamente");
    return result;
  } catch (error) {
    console.error("❌ Error restableciendo contraseña:", error);
    throw error;
  }
}

export async function resendForgotPasswordCode(email: string) {
  ensureAmplifyConfigured();
  
  try {
    const result = await resetPassword({
      username: email,
    });
    
    console.log("✅ Código reenviado exitosamente");
    return result;
  } catch (error) {
    console.error("❌ Error reenviando código:", error);
    throw error;
  }
}

// ========= Logout / Usuario actual =========
export async function cognitoLogout(): Promise<void> {
  ensureAmplifyConfigured();
  await signOut();
}

export async function getCurrentUser(): Promise<AuthUser | null> {
  ensureAmplifyConfigured();
  try {
    const user = await amplifyGetCurrentUser();
    const { tokens } = await fetchAuthSession();
    if (!tokens?.accessToken) return null;
    return user;
  } catch {
    return null;
  }
}