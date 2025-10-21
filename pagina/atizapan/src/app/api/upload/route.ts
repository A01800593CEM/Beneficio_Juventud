import { NextRequest, NextResponse } from 'next/server';
import { S3Client, PutObjectCommand } from '@aws-sdk/client-s3';

// Debug: Verificar variables de entorno
console.log('🔍 AWS Environment Variables Check:');
console.log('AWS_REGION:', process.env.AWS_REGION || 'us-east-1');
console.log('AWS_ACCESS_KEY_ID:', process.env.AWS_ACCESS_KEY_ID ? `${process.env.AWS_ACCESS_KEY_ID.substring(0, 4)}...` : 'NOT SET');
console.log('AWS_SECRET_ACCESS_KEY:', process.env.AWS_SECRET_ACCESS_KEY ? `${process.env.AWS_SECRET_ACCESS_KEY.substring(0, 4)}...` : 'NOT SET');
console.log('S3_BUCKET_NAME:', process.env.S3_BUCKET_NAME || 'joven-atizapan-images');

// Validar que las credenciales existan
if (!process.env.AWS_ACCESS_KEY_ID || !process.env.AWS_SECRET_ACCESS_KEY) {
  console.error('❌ Missing AWS credentials in environment variables');
}

const s3Client = new S3Client({
  region: process.env.AWS_REGION || 'us-east-1',
  credentials: {
    accessKeyId: process.env.AWS_ACCESS_KEY_ID!,
    secretAccessKey: process.env.AWS_SECRET_ACCESS_KEY!,
  },
});

const BUCKET_NAME = process.env.S3_BUCKET_NAME || 'joven-atizapan-images';

export async function POST(request: NextRequest) {
  try {
    // Verificar credenciales antes de procesar
    if (!process.env.AWS_ACCESS_KEY_ID || !process.env.AWS_SECRET_ACCESS_KEY) {
      console.error('❌ AWS credentials not found in environment');
      return NextResponse.json(
        {
          error: 'Configuración de AWS incompleta. Verifica las variables de entorno.',
          details: 'AWS_ACCESS_KEY_ID y AWS_SECRET_ACCESS_KEY son requeridas'
        },
        { status: 500 }
      );
    }

    const formData = await request.formData();
    const file = formData.get('file') as File;

    if (!file) {
      return NextResponse.json(
        { error: 'No file provided' },
        { status: 400 }
      );
    }

    // Validar tipo de archivo
    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
    if (!allowedTypes.includes(file.type)) {
      return NextResponse.json(
        { error: 'Tipo de archivo no permitido. Solo se permiten: JPG, PNG, WebP' },
        { status: 400 }
      );
    }

    // Validar tamaño (5MB máximo)
    const maxSize = 5 * 1024 * 1024; // 5MB
    if (file.size > maxSize) {
      return NextResponse.json(
        { error: 'El archivo es demasiado grande. Máximo 5MB' },
        { status: 400 }
      );
    }

    // Generar nombre único para el archivo
    const timestamp = Date.now();
    const randomString = Math.random().toString(36).substring(2, 15);
    const fileExtension = file.name.split('.').pop();
    const fileName = `promotions/${timestamp}-${randomString}.${fileExtension}`;

    // Convertir archivo a buffer
    const bytes = await file.arrayBuffer();
    const buffer = Buffer.from(bytes);

    // Limpiar el nombre original para los metadatos (solo caracteres ASCII)
    const cleanOriginalName = file.name.replace(/[^\x20-\x7E]/g, '').replace(/[^\w\s.-]/g, '');

    // Configurar el comando de subida a S3
    const uploadCommand = new PutObjectCommand({
      Bucket: BUCKET_NAME,
      Key: fileName,
      Body: buffer,
      ContentType: file.type,
      CacheControl: 'max-age=31536000', // 1 año
      Metadata: {
        originalName: cleanOriginalName || 'image',
        uploadedAt: new Date().toISOString(),
      },
    });

    console.log('📤 Subiendo imagen a S3:', fileName);

    // Subir archivo a S3
    await s3Client.send(uploadCommand);

    // Construir URL pública de la imagen
    const region = process.env.AWS_REGION || 'us-east-1';
    const imageUrl = `https://${BUCKET_NAME}.s3.${region}.amazonaws.com/${fileName}`;

    console.log('✅ Imagen subida exitosamente:', imageUrl);

    return NextResponse.json({
      success: true,
      imageUrl,
      fileName,
      originalName: cleanOriginalName || file.name,
      size: file.size,
      type: file.type,
    });

  } catch (error) {
    console.error('❌ Error subiendo imagen a S3:', error);

    return NextResponse.json(
      {
        error: 'Error interno del servidor al subir la imagen',
        details: error instanceof Error ? error.message : 'Error desconocido'
      },
      { status: 500 }
    );
  }
}

// Endpoint para generar URL firmada (opcional, para subidas directas desde el cliente)
export async function GET(request: NextRequest) {
  try {
    const { searchParams } = new URL(request.url);
    const fileName = searchParams.get('fileName');
    const fileType = searchParams.get('fileType');

    if (!fileName || !fileType) {
      return NextResponse.json(
        { error: 'fileName y fileType son requeridos' },
        { status: 400 }
      );
    }

    // Aquí podrías generar una URL firmada para subida directa
    // Por ahora devolvemos información para el POST upload
    return NextResponse.json({
      uploadUrl: '/api/upload',
      method: 'POST',
      fields: {
        fileName,
        fileType
      }
    });

  } catch (error) {
    console.error('❌ Error generando URL de subida:', error);
    return NextResponse.json(
      { error: 'Error interno del servidor' },
      { status: 500 }
    );
  }
}