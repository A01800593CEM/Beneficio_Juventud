import { NextRequest, NextResponse } from 'next/server';

const N8N_WEBHOOK_URL = 'https://primary-production-0858b.up.railway.app/webhook/feb97458-c7ba-4f4c-8b2b-2664935ac260';

export async function POST(request: NextRequest) {
  try {
    const { text } = await request.json();

    if (!text || text.trim() === '') {
      return NextResponse.json(
        { error: 'El texto es requerido para generar la imagen' },
        { status: 400 }
      );
    }

    console.log('🤖 Proxy: Enviando request a n8n webhook para generar imagen');
    console.log('📝 Texto para generar:', text);

    // Hacer el request al webhook de n8n
    const response = await fetch(N8N_WEBHOOK_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        text: text.trim()
      }),
    });

    console.log('📬 Respuesta del webhook n8n:', response.status, response.statusText);

    if (!response.ok) {
      const errorText = await response.text();
      console.error('❌ Error del webhook n8n:', errorText);

      return NextResponse.json(
        {
          error: `Error del webhook de IA: ${response.status} ${response.statusText}`,
          details: errorText
        },
        { status: response.status }
      );
    }

    const result = await response.json();
    console.log('✅ Imagen generada por n8n:', result);

    // Validar que la respuesta contenga una URL de imagen
    const imageUrl = `https://joven-atizapan-images.s3.us-east-2.amazonaws.com/${result[0].codigoUnico}-image.png`;

    if (!imageUrl) {
      console.error('❌ El webhook no devolvió una URL de imagen válida:', result);
      return NextResponse.json(
        {
          error: 'El webhook no devolvió una URL de imagen válida',
          details: 'La respuesta no contiene imageUrl, uri o url'
        },
        { status: 500 }
      );
    }

    // Devolver la URL de la imagen
    return NextResponse.json({
      success: true,
      imageUrl,
      originalResponse: result
    });

  } catch (error) {
    console.error('💥 Error en proxy de generación de imagen:', error);

    return NextResponse.json(
      {
        error: 'Error interno del servidor al generar imagen',
        details: error instanceof Error ? error.message : 'Error desconocido'
      },
      { status: 500 }
    );
  }
}