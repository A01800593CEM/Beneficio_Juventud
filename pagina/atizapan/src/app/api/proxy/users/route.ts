import { NextRequest, NextResponse } from 'next/server';

const API_BASE_URL = 'https://beneficiojoven.lat';

async function handleRequest(request: NextRequest, method: string) {
  try {
    const url = new URL(request.url);
    const pathname = url.pathname.replace('/api/proxy', '');
    const searchParams = url.searchParams.toString();
    const fullUrl = `${API_BASE_URL}${pathname}${searchParams ? `?${searchParams}` : ''}`;

    console.log(`🔄 Proxy ${method}: Enviando request a API externa:`, fullUrl);

    const requestOptions: RequestInit = {
      method,
      headers: {
        'Content-Type': 'application/json',
      },
    };

    // Add body for POST, PUT, PATCH requests
    if (['POST', 'PUT', 'PATCH'].includes(method)) {
      try {
        const body = await request.json();
        requestOptions.body = JSON.stringify(body);
        console.log(`🔄 Proxy ${method}: Request body:`, body);
      } catch {
        // No body or invalid JSON
      }
    }

    const response = await fetch(fullUrl, requestOptions);

    console.log(`📬 Proxy ${method}: Respuesta de API externa:`, response.status, response.statusText);

    const responseText = await response.text();
    console.log(`📜 Proxy ${method}: Response body:`, responseText);

    if (!response.ok) {
      return NextResponse.json(
        {
          error: true,
          message: `API Error: ${response.status} ${response.statusText}`,
          details: responseText
        },
        { status: response.status }
      );
    }

    let responseData;
    try {
      responseData = JSON.parse(responseText);
    } catch {
      responseData = { success: true, data: responseText };
    }

    return NextResponse.json(responseData);
  } catch (error) {
    console.error(`💥 Proxy ${method} error:`, error);

    return NextResponse.json(
      {
        error: true,
        message: 'Error en el servidor proxy',
        details: error instanceof Error ? error.message : 'Error desconocido'
      },
      { status: 500 }
    );
  }
}

export async function GET(request: NextRequest) {
  return handleRequest(request, 'GET');
}

export async function POST(request: NextRequest) {
  return handleRequest(request, 'POST');
}

export async function PUT(request: NextRequest) {
  return handleRequest(request, 'PUT');
}

export async function PATCH(request: NextRequest) {
  return handleRequest(request, 'PATCH');
}

export async function DELETE(request: NextRequest) {
  return handleRequest(request, 'DELETE');
}