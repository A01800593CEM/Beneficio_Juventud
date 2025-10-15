import { UserState } from "./users/enums/user-state.enum";
import { PromotionState } from "./promotions/enums/promotion-state.enums";
import { PromotionType } from "./promotions/enums/promotion-type.enums";
import { CollaboratorState } from "./collaborators/enums/collaborator-state.enum";
import { BookStatus } from "./bookings/enums/book-status.enum";
 

// --- Define interfaces for API responses ---
interface User {
  id: number;
  cognitoId: string;
  name: string;
}

interface Collaborator {
  colaborador_id: number;
  nombre_negocio: string;
  cognitoId: string;
}

interface Promotion {
  promotion_id: number;
  title: string;
}

export async function seedDatabase() {
  const baseUrl = 'http://localhost:3000';

  const log = (msg: string, ok = true) =>
    console.log(`${ok ? '✅' : '❌'} ${msg}`);

  // === 1️⃣ Create Users ===
  const users = [
    {
      name: 'Ana',
      lastNamePaternal: 'López',
      lastNameMaternal: 'García',
      birthDate: new Date('1999-06-12'),
      phoneNumber: '+525511223344',
      email: 'ana@example.com',
      cognitoId: 'user-ana-001',
      accountState: UserState.ACTIVE,
      userPrefCategories: ['ropa', 'tecnología', 'hogar']
    },
    {
      name: 'Luis',
      lastNamePaternal: 'Pérez',
      lastNameMaternal: 'Santos',
      birthDate: new Date('1995-03-21'),
      phoneNumber: '+525566778899',
      email: 'luis@example.com',
      cognitoId: 'user-luis-002',
      accountState: UserState.ACTIVE,
      userPrefCategories: ['restaurantes', 'viajes']
    }
  ];

  const createdUsers: User[] = [];
  for (const u of users) {
    const res = await fetch(`${baseUrl}/users`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(u)
    });
    if (res.ok) {
      const data: User = await res.json();
      createdUsers.push(data);
      log(`User created: ${data.name}`);
    } else {
      log(`User creation failed for ${u.name}`, false);
    }
  }

  // === 2️⃣ Create Collaborators ===
  const collaborators = [
    {
      nombre_negocio: 'Moda MX',
      rfc: 'MODA123456XYZ',
      representante_nombre: 'Carla Ramos',
      telefono: '+525533221100',
      correo: 'contacto@modamx.com',
      direccion: 'Av. Reforma 100, CDMX',
      codigo_postal: '06000',
      categoria_id: 1,
      colaborador_state: CollaboratorState.ACTIVE,
      cognitoId: 'colab-moda-001'
    },
    {
      nombre_negocio: 'Café Luna',
      rfc: 'CAFE123456XYZ',
      representante_nombre: 'Miguel Torres',
      telefono: '+525566334455',
      correo: 'info@cafeluna.com',
      direccion: 'Calle Hidalgo 45, CDMX',
      codigo_postal: '06100',
      categoria_id: 2,
      colaborador_state: CollaboratorState.ACTIVE,
      cognitoId: 'colab-cafe-002'
    }
  ];

  const createdCollaborators: Collaborator[] = [];
  for (const c of collaborators) {
    const res = await fetch(`${baseUrl}/collaborators`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(c)
    });
    if (res.ok) {
      const data: Collaborator = await res.json();
      createdCollaborators.push(data);
      log(`Collaborator created: ${data.nombre_negocio}`);
    } else {
      log(`Collaborator creation failed for ${c.nombre_negocio}`, false);
    }
  }

  // === 3️⃣ Create Promotions ===
  const promotions = [
    {
      collaboratorId: createdCollaborators[0]?.cognitoId ?? 'colab-moda-001',
      title: '20% de descuento en toda la tienda',
      description: 'Promoción válida durante octubre.',
      initialDate: new Date('2025-10-01'),
      endDate: new Date('2025-10-31'),
      promotionType: PromotionType.DISCOUNT,
      promotionState: PromotionState.ACTIVE,
      totalStock: 100,
      availableStock: 100,
      limitPerUser: 1
    },
    {
      collaboratorId: createdCollaborators[1]?.cognitoId ?? 'colab-cafe-002',
      title: '2x1 en bebidas frías',
      description: 'Promoción válida hasta agotar existencias.',
      initialDate: new Date('2025-10-05'),
      endDate: new Date('2025-11-05'),
      promotionType: PromotionType.MULTYBUY,
      promotionState: PromotionState.ACTIVE,
      totalStock: 50,
      availableStock: 50,
      limitPerUser: 2
    }
  ];

  const createdPromotions: Promotion[] = [];
  for (const p of promotions) {
    const res = await fetch(`${baseUrl}/promotions`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(p)
    });
    if (res.ok) {
      const data: Promotion = await res.json();
      createdPromotions.push(data);
      log(`Promotion created: ${data.title}`);
    } else {
      log(`Promotion creation failed for ${p.title}`, false);
    }
  }

  // === 4️⃣ Favorites ===
  const favorites = [
    {
      userId: createdUsers[0]?.id ?? 1,
      collaboratorId: createdCollaborators[0]?.colaborador_id ?? 1
    },
    {
      userId: createdUsers[1]?.id ?? 2,
      collaboratorId: createdCollaborators[1]?.colaborador_id ?? 2
    }
  ];

  for (const f of favorites) {
    const res = await fetch(`${baseUrl}/favorites`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(f)
    });
    log(`Favorite creation for user ${f.userId}`, res.ok);
  }

  // === 5️⃣ Redeemed Coupons ===
  const redeemed = [
    {
      userId: createdUsers[0]?.id ?? 1,
      collaboratorId: createdCollaborators[0]?.colaborador_id ?? 1,
      branchId: 1,
      promotionId: createdPromotions[0]?.promotion_id ?? 1
    }
  ];

  for (const r of redeemed) {
    const res = await fetch(`${baseUrl}/redeemed-coupons`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(r)
    });
    log(`Redeemed coupon for user ${r.userId}`, res.ok);
  }

  // === 6️⃣ Bookings ===
  const bookings = [
    {
      promotionId: createdPromotions[1]?.promotion_id ?? 2,
      userId: createdUsers[1]?.id ?? 2,
      limitUseDate: new Date('2025-11-01'),
      bookStatus: BookStatus.PENDING
    }
  ];

  for (const b of bookings) {
    const res = await fetch(`${baseUrl}/bookings`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(b)
    });
    log(`Booking created for user ${b.userId}`, res.ok);
  }

  console.log('🎉 Seeding complete!');
}

seedDatabase().catch((err) => console.error('❌ Seed failed', err));
