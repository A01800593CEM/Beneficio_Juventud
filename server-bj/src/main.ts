
import { NestFactory } from '@nestjs/core';
import {
  FastifyAdapter,
  NestFastifyApplication,
} from '@nestjs/platform-fastify';
import { AppModule } from './app.module';
import { sendNotification } from './enviar';
import { seedDatabase } from './seedServer'

async function bootstrap() {
  const app = await NestFactory.create<NestFastifyApplication>(
    AppModule,
    new FastifyAdapter()
  );
  await app.listen(process.env.PORT ?? 3000, "0.0.0.0");

  // setTimeout(async () => {
  //   try {
  //     await seedDatabase();
  //     console.log('🌱 Database seeded successfully');
  //   } catch (err) {
  //     console.error('❌ Seed failed:', err);
  //   }
  // }, 1000);
}
bootstrap();
