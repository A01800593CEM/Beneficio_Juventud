// ============================================================================
// COMPONENT: ImageUploadSection - Sección de manejo de imágenes
// ============================================================================

import { useState } from 'react';
import { ImageService } from '../../services/image';

interface ImageUploadSectionProps {
  imageUrl: string;
  title: string;
  description: string;
  onImageChange: (imageUrl: string) => void;
}

export default function ImageUploadSection({ imageUrl, title, description, onImageChange }: ImageUploadSectionProps) {
  const [uploading, setUploading] = useState(false);
  const [dragActive, setDragActive] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);

  // Manejar selección de archivo
  const handleFileSelect = async (file: File) => {
    setUploading(true);
    setUploadError(null);

    try {
      if (!ImageService.isValidImageFile(file)) {
        throw new Error('Por favor selecciona un archivo de imagen válido');
      }

      if (!ImageService.validateFileSize(file, 5)) {
        throw new Error('El archivo es demasiado grande. Máximo 5MB');
      }

      console.log('📤 Subiendo imagen:', file.name);
      const uploadedImageUrl = await ImageService.uploadImageToS3(file);
      onImageChange(uploadedImageUrl);
      console.log('✅ Imagen subida exitosamente:', uploadedImageUrl);
    } catch (error) {
      console.error('❌ Error subiendo imagen:', error);
      setUploadError(error instanceof Error ? error.message : 'Error subiendo imagen');
    } finally {
      setUploading(false);
    }
  };

  // Manejar click en el área de imagen
  const handleImageAction = () => {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*';
    input.onchange = (e) => {
      const file = (e.target as HTMLInputElement).files?.[0];
      if (file) {
        handleFileSelect(file);
      }
    };
    input.click();
  };

  // Manejar drag & drop
  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(true);
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    const files = Array.from(e.dataTransfer.files);
    const imageFile = files.find(file => file.type.startsWith('image/'));

    if (imageFile) {
      handleFileSelect(imageFile);
    } else {
      setUploadError('Por favor selecciona un archivo de imagen válido');
    }
  };

  // Generar imagen con IA
  const handleGenerateImageWithAI = async () => {
    if (!title.trim() || !description.trim()) {
      setUploadError('Necesitas completar el título y descripción antes de generar una imagen con IA');
      return;
    }

    setUploading(true);
    setUploadError(null);

    try {
      console.log('🤖 Generando imagen con IA para:', title);
      const generatedImageUrl = await ImageService.generateImageWithAI(title, description);
      onImageChange(generatedImageUrl);
      console.log('✅ Imagen generada exitosamente con IA:', generatedImageUrl);
    } catch (error) {
      console.error('❌ Error generando imagen con IA:', error);
      setUploadError(
        error instanceof Error
          ? error.message
          : 'Error al generar imagen con IA. Inténtalo de nuevo.'
      );
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="bg-white border border-gray-200 rounded-lg p-5">
      <h3 className="text-lg font-medium text-[#015463] mb-4 flex items-center gap-2">
        <svg className="w-5 h-5 text-[#008D96]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
        </svg>
        Imagen de la Promoción
      </h3>

      {/* Área de imagen */}
      <div
        className={`relative border-2 border-dashed rounded-lg p-8 text-center transition-all ${
          dragActive
            ? 'border-[#008D96] bg-[#008D96]/5'
            : imageUrl
            ? 'border-gray-200'
            : 'border-gray-300 hover:border-[#008D96] hover:bg-gray-50'
        } ${uploading ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'}`}
        onClick={!uploading ? handleImageAction : undefined}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
      >
        {uploading ? (
          <div className="flex flex-col items-center">
            <svg className="animate-spin h-8 w-8 text-[#008D96] mb-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <span className="text-[#008D96] font-medium">Subiendo imagen...</span>
          </div>
        ) : imageUrl ? (
          <div className="relative">
            <img
              src={imageUrl}
              alt="Vista previa"
              className="max-h-48 mx-auto rounded-lg object-cover"
            />
            <div className="absolute inset-0 bg-black bg-opacity-0 hover:bg-opacity-20 rounded-lg transition-all flex items-center justify-center opacity-0 hover:opacity-100">
              <span className="text-white font-medium">Cambiar imagen</span>
            </div>
          </div>
        ) : (
          <div className="flex flex-col items-center">
            <svg className="h-12 w-12 text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            <p className="text-lg font-medium text-gray-700 mb-1">Agregar imagen</p>
            <p className="text-sm text-gray-500 mb-4">Arrastra una imagen o haz clic para seleccionar</p>
            <p className="text-xs text-gray-400">PNG, JPG hasta 5MB</p>
          </div>
        )}
      </div>

      {/* Error message */}
      {uploadError && (
        <div className="mt-3 p-3 bg-red-50 border border-red-200 rounded-lg">
          <p className="text-red-600 text-sm">{uploadError}</p>
        </div>
      )}

      {/* Botón de IA */}
      <div className="mt-4">
        <button
          type="button"
          onClick={handleGenerateImageWithAI}
          disabled={uploading || !title.trim() || !description.trim()}
          className="w-full bg-gradient-to-r from-[#4B4C7E] to-[#008D96] text-white px-4 py-3 rounded-lg hover:opacity-90 disabled:opacity-50 transition-all flex items-center justify-center gap-2"
        >
          <span className="text-lg">🤖</span>
          {uploading ? 'Generando imagen...' : 'Generar imagen con IA'}
        </button>
        {(!title.trim() || !description.trim()) && (
          <p className="text-xs text-gray-500 mt-2 text-center">
            Completa el título y descripción para generar con IA
          </p>
        )}
      </div>
    </div>
  );
}