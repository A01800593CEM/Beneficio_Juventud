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
- Main modules: `users`, `collaborators`, `categories`, `promotions`, `bookings`, `favorites`, `administrators`, `redeemedcoupon`, `branch`, `notifications`, `expirations`, `places`
- Uses Firebase Admin SDK for push notifications (see `notifications/notifications.service.ts`, `enviar.ts`)
- Authentication via AWS Cognito (entities use `cognitoId` field)
- Scheduled tasks enabled via `@nestjs/schedule` (see `expirations` module)

**Database:**
- PostgreSQL with TypeORM (synchronize: false - migrations should be managed manually)
- Connection configured via environment variables (DB_HOST, DB_PORT, DB_USERNAME, DB_PASSWORD, DB_NAME)
- Schema reference available in `bd.sql` (MySQL format, adapted for PostgreSQL in production)

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
npm run test             # Unit tests
npm run test:e2e         # E2E tests
npm run test:cov         # Coverage

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
# Build (from root or app/ directory)
./gradlew build

# Run on device/emulator
./gradlew installDebug
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

## Development Notes

- Backend uses TypeScript with experimental decorators and metadata emission
- PostgreSQL SSL connection enabled with `rejectUnauthorized: false` for development
- Mobile app targets Android with Kotlin, uses Jetpack Compose for UI
- Web dashboard uses Next.js App Router (not Pages Router)
- All three platforms share the same backend API at https://localhost:3000 (dev)

## Testing Notes

- Backend: Jest configured with ts-jest, tests colocated with source in `*.spec.ts` files
- E2E tests use `@nestjs/testing` and `supertest`
- Test database should be separate from development database
- Run `npm run test:cov` to check coverage before committing
