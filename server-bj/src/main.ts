import * as fs from 'fs';
import * as path from 'path';
import { NestFactory } from '@nestjs/core';
import {
  FastifyAdapter,
  NestFastifyApplication,
} from '@nestjs/platform-fastify';
import { AppModule } from './app.module';

async function bootstrap() {
  const certDir = path.join(process.cwd(), 'src', 'certificates');
  const httpsOptions = {
    key: fs.readFileSync(path.join(certDir, 'key.pem')),
    cert: fs.readFileSync(path.join(certDir, 'cert.pem')),
  };

  const app = await NestFactory.create<NestFastifyApplication>(
    AppModule,
    new FastifyAdapter({ https: httpsOptions }),
  );

  app.enableCors({ origin: true, credentials: true });
  await app.listen(3000, '0.0.0.0');
  console.log('✅ HTTPS running at https://localhost:3000');
}

bootstrap();