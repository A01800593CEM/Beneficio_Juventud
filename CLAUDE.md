# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Beneficio Joven is a multi-platform coupon application for youth (ages 12-29) with three main components:
- **Mobile App** (Android): Kotlin/Jetpack Compose app for end users
- **Backend Server** (NestJS): TypeScript REST API with PostgreSQL database
- **Web Dashboard** (Next.js): Admin/collaborator management interface

## Architecture

### Backend Server (`server-bj/`)

NestJS application using Fastify adapter with TypeORM for PostgreSQL database access.

**Module Structure:**
- Core business modules follow NestJS convention: `<module>/entities/`, `<module>/dto/`, `<module>/<module>.controller.ts`, `<module>/<module>.service.ts`
- Main modules: `users`, `collaborators`, `categories`, `promotions`, `bookings`, `favorites`, `administrators`, `redeemedcoupon`, `branch`, `notifications`, `expirations`, `places`, `analytics`
- Uses Firebase Admin SDK for push notifications (see `notifications/notifications.service.ts`, `enviar.ts`)
- Authentication via AWS Cognito (entities use `cognitoId` field)
- Scheduled tasks enabled via `@nestjs/schedule` (see `expirations` module)
- Location services via `places` module for nearby promotions and collaborator discovery

**Database:**
- PostgreSQL with TypeORM (synchronize: false - migrations should be managed manually)
- Connection configured via environment variables (DB_HOST, DB_PORT, DB_USERNAME, DB_PASSWORD, DB_NAME)
- Schema reference available in `BD-BJ.sql` (PostgreSQL schema dump)

**HTTPS Configuration:**
- Runs HTTPS on port 3000 using self-signed certificates in `src/certificates/` (key.pem, cert.pem)
- CORS enabled for all origins with credentials

### Web Dashboard (`pagina-web/`)

Next.js 15 application using App Router with TypeScript.

**Structure:**
- `src/app/` - App Router pages (admin/, colaborator/, usuario/ routes)
- `src/lib/` - Authentication utilities (Amplify Auth, AWS Cognito integration)
- `src/features/` - Feature-specific components and logic
- `src/shared/` - Shared UI components
- Uses Tailwind CSS v4 for styling
- Authentication: AWS Amplify + Cognito (NextAuth.js configured)

### Mobile App (`app/`)

Android application using Kotlin and Jetpack Compose with MVVM architecture.

**Structure:**
- `app/src/main/java/mx/itesm/beneficiojuventud/`
  - `components/` - Reusable Compose UI components
  - `model/` - Data models and entities
  - `view/` - User screens
  - `viewcollab/` - Collaborator screens
  - `viewmodel/` - ViewModels for business logic
  - `utils/` - Utility functions
- Firebase integration via `google-services.json`
- Room database for local persistence (user preferences, cached promotions)
- Google Maps SDK and Play Services Location for nearby collaborator discovery
- CameraX and ML Kit for QR code scanning (coupon redemption)
- AWS Amplify for Cognito authentication
- Retrofit for backend API communication

## Common Development Commands

### Backend Server

```bash
cd server-bj

# Install dependencies
npm install

# Development
npm run start:dev        # Watch mode
npm run start:debug      # Debug mode with --inspect

# Build
npm run build

# Production
npm run start:prod

# Testing
npm run test             # Run all unit tests
npm run test:watch       # Run tests in watch mode
npm run test:cov         # Generate coverage report
npm run test:debug       # Run tests with debugger
npm run test:e2e         # Run E2E tests

# Code quality
npm run lint             # ESLint with auto-fix
npm run format           # Prettier formatting

# Database seeding
npx ts-node src/seedServer.ts
```

### Web Dashboard

```bash
cd pagina-web

# Install dependencies
npm install

# Development
npm run dev              # Turbopack dev server

# Build
npm run build            # Production build with Turbopack

# Production
npm run start

# Linting
npm run lint
```

### Mobile App

```bash
# Build (from root directory)
./gradlew build                    # Build all variants
./gradlew assembleDebug            # Build debug APK
./gradlew assembleRelease          # Build release APK

# Install and run
./gradlew installDebug             # Install debug build on connected device

# Testing
./gradlew test                     # Run unit tests
./gradlew testDebugUnitTest        # Run debug unit tests only
./gradlew connectedAndroidTest     # Run instrumented tests on device/emulator
./gradlew connectedDebugAndroidTest # Run debug instrumented tests

# Clean
./gradlew clean                    # Clean build artifacts
```

## Key Technical Patterns

### Backend Validation
- Uses `class-validator` and `class-transformer` for DTO validation
- All DTOs should extend appropriate mapped types from `@nestjs/mapped-types`

### Entity Relationships
- Users and Collaborators both use `cognitoId` for AWS Cognito integration
- Promotions belong to Collaborators and have stock management (totalStock, availableStock, limitPerUser)
- Bookings track reservation status via BookStatus enum (PENDING, CONFIRMED, etc.)
- RedeemedCoupons link Users, Collaborators, Branches, and Promotions

### State Management Enums
- UserState: ACTIVE, SUSPENDED, DELETED
- CollaboratorState: PENDING, ACTIVE, INACTIVE
- PromotionState: ACTIVE, INACTIVE, EXPIRED
- PromotionType: DISCOUNT, MULTYBUY, FREEBIE, etc.
- BookStatus: PENDING, CONFIRMED, CANCELLED, COMPLETED

### Authentication Flow
- Web and mobile apps authenticate via AWS Cognito
- Backend validates Cognito tokens and maps to database entities via cognitoId
- Firebase Admin SDK used for push notifications to mobile devices

### Notifications
- Push notifications sent via Firebase Cloud Messaging
- Scheduled expiration reminders handled by `expirations` module using `@nestjs/schedule`
- Service account credentials required for Firebase Admin initialization

### Location-Based Services
- Backend `places` module handles geospatial queries for nearby collaborators
- Mobile app uses Google Maps SDK for displaying collaborator locations
- Google Play Services Location API for user's current location
- Promotions filtered by proximity to user's location

## Development Notes

- Backend uses TypeScript with experimental decorators and metadata emission
- PostgreSQL SSL connection enabled with `rejectUnauthorized: false` for development
- Mobile app targets Android SDK 26+ (Android 8.0+), compile SDK 36
- Mobile app uses Kotlin with JVM target 11, Jetpack Compose for UI
- Web dashboard uses Next.js 15 App Router with Turbopack (not Pages Router)
- All three platforms share the same backend API at https://localhost:3000 (dev)
- Mobile app uses EncryptedSharedPreferences for secure local storage
- QR code redemption flow: Mobile app scans QR → validates with backend → creates RedeemedCoupon entry

## Testing Notes

### Backend Testing
- Jest configured with ts-jest, tests colocated with source in `*.spec.ts` files
- E2E tests use `@nestjs/testing` and `supertest`
- Test database should be separate from development database
- Run `npm run test:cov` to check coverage before committing

### Mobile Testing
- Unit tests: MockK for mocking, Turbine for Flow testing, kotlinx-coroutines-test for coroutines
- UI tests: Compose UI testing framework with `ui-test-junit4`
- Instrumented tests: Espresso for UI interactions, Navigation Testing for navigation flows
- Room database testing with in-memory database
- API mocking with MockWebServer for Retrofit testing

## Additional Documentation

The repository includes specialized development guides in the root directory:
- `FLUJO_QR_INSTRUCTIONS.md` - QR code redemption implementation details
- `IMPLEMENTACION_ROOMDB.md` - Room database setup and usage guide
- `OFFLINE_SUPPORT.md` - Offline functionality and data synchronization
- `TESTING_README.md` - Comprehensive testing strategies and best practices
- `GUIA_RAPIDA_MAPAS.md` - Google Maps integration quick start
- `IMPLEMENTACION_RESERVAR_CUPONES.md` - Booking system implementation
- `DEBUG_SUCURSALES_CERCANAS.md` - Debugging nearby branches/locations
- `RECOMENDACIONES_POR_UBICACION.md` - Location-based recommendations
- `ANALISIS_SUCURSALES.md` - Branch management analysis
- `EJEMPLO_USO_REPOSITORY.md` - Repository pattern examples
- `SOLUCION_ERROR_500.md` - Common error solutions
- `CORRECCIONES_ERRORES.md` - Error fixes and troubleshooting
