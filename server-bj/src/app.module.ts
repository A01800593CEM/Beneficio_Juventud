import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { UsersModule } from './users/users.module';
import { CollaboratorsModule } from './collaborators/collaborators.module';
import { CategoriesModule } from './categories/categories.module';
import { PromotionsModule } from './promotions/promotions.module';
import { BookingsModule } from './bookings/bookings.module';
import { FavoritesModule } from './favorites/favorites.module';
import { AdministratorsModule } from './administrators/administrators.module';
//import { ReportsModule } from './reports/reports.module';
import { RedeemedcouponModule } from './redeemedcoupon/redeemedcoupon.module';
import { BranchModule } from './branch/branch.module';
import { NotificationsModule } from './notifications/notifications.module';
//Notificaciones con schedule
import { ScheduleModule } from '@nestjs/schedule';
import { ExpirationsModule } from './expirations/expirations.module';
import { PlacesModule } from './places/places.module';
import { AnalyticsModule } from './analytics/analytics.module';
import { UploadsModule } from './uploads/uploads.module';


@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
    }),
    TypeOrmModule.forRootAsync({
      imports: [ConfigModule],
      inject: [ConfigService],
      useFactory: (configService: ConfigService) => ({
        type: 'postgres' as const,
        host: configService.get<string>('DB_HOST'),
        port: configService.get<number>('DB_PORT'),
        username: configService.get<string>('DB_USERNAME'),
        password: configService.get<string>('DB_PASSWORD'),
        database: configService.get<string>('DB_NAME'),
        ssl: {
          rejectUnauthorized: false
        },
        autoLoadEntities: true,
        synchronize: false,
      }),
    }),
    UsersModule,
    CollaboratorsModule,
    CategoriesModule,
    PromotionsModule,
    BookingsModule,
    FavoritesModule,
    AdministratorsModule,
    //ReportsModule
    RedeemedcouponModule,
    BranchModule,
    BranchModule,
    NotificationsModule,
    //Schedule
    ScheduleModule.forRoot(),
    ExpirationsModule,
    PlacesModule,
    AnalyticsModule,
    UploadsModule
  ],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
