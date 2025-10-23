import { NextRequest, NextResponse } from 'next/server';

const API_BASE_URL = 'https://api.beneficiojoven.lat';

async function handleRequest(request: NextRequest, method: string) {
  try {
    const url = new URL(request.url);
    const pathSegments = url.pathname.split('/');
    // Remove /api/proxy from the path
    const apiPath = '/' + pathSegments.slice(3).join('/');
    const searchParams = url.searchParams.toString();
    const fullUrl = `${API_BASE_URL}${apiPath}${searchParams ? `?${searchParams}` : ''}`;

    console.log(`🔄 Proxy ${method}: Enviando request a API externa:`, fullUrl);

    const requestOptions: RequestInit = {
      method,
      headers: {},
    };

    // Add timeout configuration
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 5000); // 5 second timeout
    requestOptions.signal = controller.signal;

    // Add body for POST, PUT, PATCH requests
    if (['POST', 'PUT', 'PATCH'].includes(method)) {
      // Solo agregar Content-Type si hay body
      requestOptions.headers = {
        'Content-Type': 'application/json',
      };

      try {
        const body = await request.json();
        requestOptions.body = JSON.stringify(body);
        console.log(`🔄 Proxy ${method}: Request body:`, body);
      } catch {
        // No body or invalid JSON
      }
    }

    const response = await fetch(fullUrl, requestOptions);
    clearTimeout(timeoutId); // Clear timeout on successful response

    console.log(`📬 Proxy ${method}: Respuesta de API externa:`, response.status, response.statusText);

    const responseText = await response.text();
    console.log(`📜 Proxy ${method}: Response body:`, responseText.substring(0, 500) + (responseText.length > 500 ? '...' : ''));

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