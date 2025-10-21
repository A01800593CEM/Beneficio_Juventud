// ============================================================================
// HOOK: useCollaborator - Autenticación de colaborador
// ============================================================================

import { useState, useEffect } from 'react';
import { useSession } from 'next-auth/react';
import { promotionApiService } from '../services/api';
import { ApiCollaborator } from '../types';

export const useCollaborator = () => {
  const { data: session } = useSession();
  const [collaborator, setCollaborator] = useState<ApiCollaborator | null>(null);
  const [authLoading, setAuthLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const authenticateCollaborator = async () => {
      console.log('🔍 Session data:', session);

      if (!session) {
        console.log('❌ No session found');
        setAuthLoading(false);
        return;
      }

      // Extraer cognitoUsername de la sesión
      const sessionData = session as any;

      // Buscar en todas las posibles ubicaciones
      console.log('🔍 DEBUGGING SESSION STRUCTURE:');
      console.log('🔍 sessionData:', JSON.stringify(sessionData, null, 2));
      console.log('🔍 sessionData.profile:', sessionData.profile);
      console.log('🔍 sessionData.cognitoUsername:', sessionData.cognitoUsername);
      console.log('🔍 sessionData.sub:', sessionData.sub);
      console.log('🔍 sessionData.user:', sessionData.user);
      console.log('🔍 sessionData.accessToken:', sessionData.accessToken);

      const cognitoUsername = sessionData.cognitoUsername || sessionData.sub || sessionData.user?.id || sessionData.user?.sub;

      console.log('🔍 Extracted cognitoUsername:', cognitoUsername);

      // Intentar buscar colaborador siempre que tengamos cognitoUsername
      if (cognitoUsername) {
        try {
          console.log('🔄 Authenticating collaborator with cognitoUsername:', cognitoUsername);
          const collaboratorData = await promotionApiService.getCollaboratorByCognitoId(cognitoUsername);
          setCollaborator(collaboratorData);
          setError(null);
          console.log('✅ Collaborator authenticated:', collaboratorData.email);
        } catch (err) {
          console.error('❌ Error authenticating collaborator:', err);
          setError('Error de autenticación. No se encontró el colaborador.');
          setCollaborator(null);
        }
      } else {
        console.log('ℹ️ No cognitoUsername found in session');
        console.log('🔍 Available session keys:', Object.keys(sessionData));
        setError('No se pudo obtener la información de autenticación.');
      }

      setAuthLoading(false);
    };

    if (session !== undefined) {
      authenticateCollaborator();
    }
  }, [session]);

  return {
    collaborator,
    authLoading,
    error,
    isAuthenticated: !!collaborator,
    setError,
  };
};