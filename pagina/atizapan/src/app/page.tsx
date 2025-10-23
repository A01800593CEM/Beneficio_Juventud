// src/app/page.tsx
"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useSession } from "next-auth/react";
import Image from "next/image";
import { motion, useReducedMotion, type Variants } from "framer-motion";

export default function HomePage() {
  const router = useRouter();
  const { data: session, status } = useSession();
  const [mounted, setMounted] = useState(false);
  const prefersReduced = useReducedMotion();

  // Navbar glass on scroll
  const [scrolled, setScrolled] = useState(false);
  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 8);
    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  useEffect(() => setMounted(true), []);
  useEffect(() => {
    if (status === "loading") return;
    if (session) router.push("/admin");
  }, [session, status, router]);

  if (!mounted) return null;

  // ===== Variants (reutilizables) =====
  const EASE = [0.22, 1, 0.36, 1] as const;

  const fadeUp: Variants = {
    hidden: { opacity: 0, y: 24 },
    visible: {
      opacity: 1,
      y: 0,
      transition: { duration: prefersReduced ? 0 : 0.65, ease: EASE },
    },
  };

  const fadeRight: Variants = {
    hidden: { opacity: 0, x: -28 },
    visible: { opacity: 1, x: 0, transition: { duration: prefersReduced ? 0 : 0.7, ease: EASE } },
  };

  const fadeLeft: Variants = {
    hidden: { opacity: 0, x: 28 },
    visible: { opacity: 1, x: 0, transition: { duration: prefersReduced ? 0 : 0.7, ease: EASE } },
  };

  const stagger: Variants = {
    hidden: {},
    visible: {
      transition: { staggerChildren: prefersReduced ? 0 : 0.12, delayChildren: prefersReduced ? 0 : 0.05 },
    },
  };

  const headerClasses = [
    "sticky top-0 z-50 border-b transition-all duration-300",
    "supports-[backdrop-filter]:backdrop-blur-md backdrop-blur", // frosted
    scrolled
      ? "bg-white/70 border-black/5 shadow-sm"
      : "bg-white/30 border-transparent"
  ].join(" ");

  return (
    <div className="min-h-screen bg-white text-gray-900">
      {/* Header / Navbar vidrio */}
      <header className={headerClasses}>
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 h-20 flex items-center justify-between">
          <motion.div
            className="flex items-center gap-3"
            initial={{ opacity: 0, y: -8 }}
            animate={{ opacity: 1, y: 0, transition: { duration: prefersReduced ? 0 : 0.5, ease: EASE } }}
          >
            <div className="h-16 w-16  grid place-items-center">
              <img src="/logo_beneficio_joven.png" alt="" />
            </div>
            
            <div className="h-14 w-40  grid place-items-center">
              <img src="/atizapan_logo.png" alt="" />
              </div>
          </motion.div>

          <nav className="flex items-center gap-2">
            <motion.button
              whileHover={{ scale: prefersReduced ? 1 : 1.04 }}
              whileTap={{ scale: prefersReduced ? 1 : 0.98 }}
              onClick={() => router.push("/login")}
              className="hidden sm:inline-flex h-9 items-center rounded-xl px-4 text-sm font-medium text-gray-700 hover:bg-gray-100"
            >
              Iniciar sesión
            </motion.button>
            <motion.button
              whileHover={{ scale: prefersReduced ? 1 : 1.04 }}
              whileTap={{ scale: prefersReduced ? 1 : 0.98 }}
              onClick={() => router.push("/register")}
              className="hidden sm:inline-flex h-9 items-center rounded-xl px-4 text-sm font-medium text-white bg-gray-900 hover:bg-black"
            >
              Registrarse
            </motion.button>
            <motion.button
              whileHover={{ y: prefersReduced ? 0 : -2 }}
              whileTap={{ scale: prefersReduced ? 1 : 0.98 }}
              onClick={() => router.push("/colaboradores")}
              className="inline-flex h-9 items-center rounded-xl px-4 text-sm font-semibold text-white bg-gradient-to-r from-[#4B4C7E] to-[#008D96] hover:opacity-90"
              aria-label="Para Negocios"
            >
              Para Negocios
            </motion.button>
          </nav>
        </div>
      </header>

      <main>
        {/* HERO ================================================================== */}
        <section className="relative overflow-hidden pt-24 bg-gradient-to-br from-[#2A2B5F] via-[#4B4C7E] to-[#006B73]">
          {/* Fondos animados */}
          <motion.div
            aria-hidden
            className="pointer-events-none absolute -top-32 -left-32 h-80 w-80 rounded-full bg-[#4B4C7E] blur-3xl opacity-40"
            animate={{ y: prefersReduced ? 0 : [0, 16, 0] }}
            transition={{ repeat: Infinity, duration: 10, ease: "easeInOut" }}
          />
          <motion.div
            aria-hidden
            className="pointer-events-none absolute -bottom-24 -right-24 h-96 w-96 rounded-full bg-[#008D96] blur-3xl opacity-40"
            animate={{ y: prefersReduced ? 0 : [0, -20, 0] }}
            transition={{ repeat: Infinity, duration: 12, ease: "easeInOut" }}
          />

          <div className="absolute inset-0 -z-10 bg-gradient-to-r from-[#4B4C7E]/90 to-[#008D96]/90" />
          <div className="relative min-h-[80vh]">
            <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 h-full">
              <motion.div
                variants={fadeUp}
                initial="hidden"
                animate="visible"
                className="text-white pt-16 max-w-2xl"
              >
              <h1 className="text-4xl md:text-6xl font-extrabold leading-[1.05]">
                Descarga Beneficio Joven <br /> y ahorra en grande
              </h1>
              <p className="mt-6 text-white/90 text-lg">
                La app móvil oficial para jóvenes de 12 a 29 años. Encuentra descuentos exclusivos
                cerca de ti, reserva cupones con QR, guarda tus favoritos y descubre nuevos lugares.
                ¡Todo desde tu teléfono!
              </p>
              <div className="mt-4 flex flex-wrap gap-3">
                <div className="inline-flex items-center gap-2 bg-white/20 rounded-full px-4 py-2 text-white/90">
                  <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z" clipRule="evenodd" />
                  </svg>
                  <span className="text-sm font-medium">Mapa interactivo</span>
                </div>
                <div className="inline-flex items-center gap-2 bg-white/20 rounded-full px-4 py-2 text-white/90">
                  <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zM3 10a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H4a1 1 0 01-1-1v-6zM14 9a1 1 0 00-1 1v6a1 1 0 001 1h2a1 1 0 001-1v-6a1 1 0 00-1-1h-2z" />
                  </svg>
                  <span className="text-sm font-medium">Códigos QR</span>
                </div>
                <div className="inline-flex items-center gap-2 bg-white/20 rounded-full px-4 py-2 text-white/90">
                  <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M3.172 5.172a4 4 0 015.656 0L10 6.343l1.172-1.171a4 4 0 115.656 5.656L10 17.657l-6.828-6.829a4 4 0 010-5.656z" clipRule="evenodd" />
                  </svg>
                  <span className="text-sm font-medium">Favoritos</span>
                </div>
              </div>

              <motion.div
                className="mt-8 flex items-center gap-4"
                initial={{ opacity: 0, y: 12 }}
                animate={{ opacity: 1, y: 0, transition: { delay: prefersReduced ? 0 : 0.2, duration: 0.5, ease: EASE } }}
              >
                <a href="#" className="inline-flex" aria-label="Disponible en Google Play" title="Disponible en Google Play">
                  <Image
                    src="/google-play-badge.svg"
                    alt="Disponible en Google Play"
                    width={180}
                    height={54}
                    className="h-12 w-auto"
                    priority
                  />
                </a>
                <motion.button
                  whileHover={{ scale: prefersReduced ? 1 : 1.05 }}
                  whileTap={{ scale: prefersReduced ? 1 : 0.98 }}
                  onClick={() => document.getElementById('como-funciona')?.scrollIntoView({ behavior: 'smooth' })}
                  className="inline-flex items-center rounded-xl border-2 border-gray-900 text-gray-900 bg-white/90 px-6 py-3 font-semibold hover:bg-white"
                >
                  Ver cómo funciona
                </motion.button>
              </motion.div>
              </motion.div>
            </div>
          </div>

          {/* Imagen posicionada absoluta en la esquina inferior derecha */}
          <div className="absolute bottom-0 right-0 w-80 md:w-96 lg:w-[28rem] xl:w-[38rem] z-10">
            <Image
              src="/phone-home.svg"
              alt="Vista previa de la app"
              width={700}
              height={1200}
              className="w-full h-auto select-none pointer-events-none"
              priority
            />
          </div>
        </section>

        {/* CÓMO FUNCIONA ========================================================= */}
        <motion.section
          className="py-16 md:py-20 bg-gradient-to-br from-gray-50 to-white relative overflow-hidden"
          initial="hidden"
          whileInView="visible"
          viewport={{ once: true, amount: 0.2 }}
        >
          {/* Elementos decorativos de fondo */}
          <div className="absolute top-20 left-10 w-32 h-32 bg-[#4B4C7E]/5 rounded-full blur-xl"></div>
          <div className="absolute bottom-20 right-10 w-40 h-40 bg-[#008D96]/5 rounded-full blur-xl"></div>

          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 relative">
            <motion.div
              variants={fadeUp}
              className="text-center mb-16"
            >
              <h2 className="text-3xl md:text-5xl font-extrabold text-gray-900 mb-6" id="como-funciona">
                ¿Cómo funciona la app?
              </h2>
              <p className="text-gray-600 max-w-2xl mx-auto text-lg leading-relaxed">
                Descarga, regístrate y comienza a ahorrar con cupones digitales al instante
              </p>
            </motion.div>

            <div className="relative max-w-5xl mx-auto h-[700px] md:h-[600px]">
              {[
                {
                  number: "01",
                  title: "Descarga y Regístrate",
                  desc: "Descarga la app gratuita desde Google Play, crea tu cuenta con Google o email y selecciona tus categorías de interés favoritas.",
                  icon: (
                    <svg viewBox="0 0 24 24" className="h-7 w-7">
                      <path d="M12 15l3-3m0 0l-3-3m3 3H4m5 4.5c0 .621.504 1.125 1.125 1.125H19.5a2.25 2.25 0 002.25-2.25V8.25a2.25 2.25 0 00-2.25-2.25H10.125C9.504 6 9 6.504 9 7.125v8.25" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round" />
                    </svg>
                  ),
                  delay: 0.1,
                  position: "left-0 top-0"
                },
                {
                  number: "02",
                  title: "Encuentra y Reserva",
                  desc: "Usa el mapa interactivo para encontrar promociones cerca de ti. Reserva cupones con un toque y recibe tu código QR único.",
                  icon: (
                    <svg viewBox="0 0 24 24" className="h-7 w-7">
                      <path fillRule="evenodd" d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z" fill="currentColor" />
                    </svg>
                  ),
                  delay: 0.3,
                  position: "left-1/2 top-20 -translate-x-1/2"
                },
                {
                  number: "03",
                  title: "Canjea con QR",
                  desc: "Presenta tu código QR en el negocio para canjear tu descuento. Todo se actualiza automáticamente en tiempo real.",
                  icon: (
                    <svg viewBox="0 0 24 24" className="h-7 w-7">
                      <path d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zM3 10a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H4a1 1 0 01-1-1v-6zM14 9a1 1 0 00-1 1v6a1 1 0 001 1h2a1 1 0 001-1v-6a1 1 0 00-1-1h-2z" fill="currentColor" />
                    </svg>
                  ),
                  delay: 0.5,
                  position: "right-0 top-40"
                },
              ].map((step, index) => (
                <motion.div
                  key={step.title}
                  initial={{
                    opacity: 0,
                    scale: 0.8,
                    y: 30
                  }}
                  whileInView={{
                    opacity: 1,
                    scale: 1,
                    y: 0,
                    transition: {
                      duration: prefersReduced ? 0 : 0.6,
                      delay: prefersReduced ? 0 : step.delay,
                      ease: EASE
                    }
                  }}
                  whileHover={{
                    scale: prefersReduced ? 1 : 1.05,
                    y: prefersReduced ? 0 : -5,
                    transition: { duration: 0.2 }
                  }}
                  viewport={{ once: true, amount: 0.3 }}
                  className={`absolute ${step.position} w-80 group cursor-pointer`}
                >
                  <div className="relative">
                    {/* Conexión visual con líneas punteadas */}
                    {index < 2 && (
                      <motion.div
                        initial={{ pathLength: 0, opacity: 0 }}
                        whileInView={{
                          pathLength: 1,
                          opacity: 0.3,
                          transition: { duration: 1, delay: step.delay + 0.5 }
                        }}
                        viewport={{ once: true }}
                        className="absolute top-1/2 left-full z-0"
                      >
                        <svg
                          width={index === 0 ? "200" : "160"}
                          height="100"
                          className="text-[#008D96]"
                        >
                          <motion.path
                            d={index === 0 ? "M0,0 Q100,50 200,80" : "M0,0 Q80,-30 160,40"}
                            stroke="currentColor"
                            strokeWidth="2"
                            strokeDasharray="5,5"
                            fill="none"
                            initial={{ pathLength: 0 }}
                            whileInView={{ pathLength: 1 }}
                            transition={{ duration: 1, delay: step.delay + 0.5 }}
                          />
                        </svg>
                      </motion.div>
                    )}

                    {/* Card principal */}
                    <div className="relative bg-white rounded-3xl p-8 border border-gray-200 shadow-lg hover:shadow-xl transition-all duration-300 backdrop-blur-sm h-80">
                      {/* Número como badge en la esquina */}
                      <div className="absolute -top-3 -left-3 h-12 w-12 rounded-full bg-gradient-to-br from-[#4B4C7E] to-[#008D96] flex items-center justify-center shadow-lg">
                        <span className="text-white font-bold text-lg">{step.number}</span>
                      </div>

                      {/* Icono principal centrado */}
                      <div className="flex justify-center mb-6 mt-4">
                        <div className="h-16 w-16 rounded-2xl bg-gradient-to-r from-[#4B4C7E]/10 to-[#008D96]/10 grid place-items-center text-[#008D96]">
                          {step.icon}
                        </div>
                      </div>

                      {/* Contenido centrado */}
                      <div className="text-center space-y-4">
                        <h3 className="text-xl font-bold text-[#4B4C7E] group-hover:text-[#008D96] transition-colors">
                          {step.title}
                        </h3>
                        <p className="text-gray-600 text-sm leading-relaxed px-2">
                          {step.desc}
                        </p>
                      </div>

                      {/* Barra de progreso inferior */}
                      <div className="absolute bottom-4 left-4 right-4 h-1 bg-gray-100 rounded-full overflow-hidden">
                        <motion.div
                          initial={{ width: 0 }}
                          whileInView={{ width: "100%" }}
                          transition={{ duration: 0.8, delay: step.delay + 0.2 }}
                          className="h-full bg-gradient-to-r from-[#4B4C7E] to-[#008D96]"
                        />
                      </div>
                    </div>
                  </div>
                </motion.div>
              ))}
            </div>
          </div>
        </motion.section>

        {/* DESCUBRE ALREDEDOR ==================================================== */}
        <motion.section
          className="py-16 md:py-20"
          initial="hidden"
          whileInView="visible"
          viewport={{ once: true, amount: 0.25 }}
        >
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 grid md:grid-cols-2 gap-10 items-center">
            <motion.div variants={fadeRight} className="order-2 md:order-1">
              <h2 className="text-3xl md:text-4xl font-extrabold leading-tight">Mapa inteligente con ubicación</h2>
              <p className="mt-4 text-gray-600">
                Descubre promociones cerca de ti con nuestro mapa interactivo. La app usa tu ubicación para
                mostrarte solo las ofertas disponibles en tu zona.
              </p>
              <div className="mt-6 space-y-3">
                <div className="flex items-center gap-3 bg-blue-50 rounded-lg p-3">
                  <div className="h-8 w-8 bg-blue-500 rounded-full flex items-center justify-center">
                    <svg className="h-4 w-4 text-white" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z" clipRule="evenodd" />
                    </svg>
                  </div>
                  <div>
                    <div className="font-semibold">Búsqueda por proximidad</div>
                    <div className="text-sm text-gray-600">Encuentra ofertas a 500m, 1km o 5km de distancia</div>
                  </div>
                </div>
                <div className="flex items-center gap-3 bg-green-50 rounded-lg p-3">
                  <div className="h-8 w-8 bg-green-500 rounded-full flex items-center justify-center">
                    <svg className="h-4 w-4 text-white" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zM3 10a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H4a1 1 0 01-1-1v-6zM14 9a1 1 0 00-1 1v6a1 1 0 001 1h2a1 1 0 001-1v-6a1 1 0 00-1-1h-2z" />
                    </svg>
                  </div>
                  <div>
                    <div className="font-semibold">Filtros inteligentes</div>
                    <div className="text-sm text-gray-600">Busca por categoría, tipo de descuento o negocio</div>
                  </div>
                </div>
              </div>
            </motion.div>

            <motion.div variants={fadeLeft} className="order-1 md:order-2">
              {/* Solo espacio con la imagen, sin marco */}
              <div className="relative mx-auto w-full max-w-sm">
                <Image
                  src="/phone-map.svg"
                  alt="Mapa con ofertas cercanas"
                  width={640}
                  height={1300}
                  className="w-full h-auto select-none pointer-events-none"
                />
              </div>
            </motion.div>
          </div>
        </motion.section>

        {/* EXPERIENCIA PERSONALIZADA ============================================ */}
        <motion.section
          className="py-16 md:py-20 bg-gray-50"
          initial="hidden"
          whileInView="visible"
          viewport={{ once: true, amount: 0.25 }}
        >
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 grid md:grid-cols-2 gap-10 items-center">
            <motion.div variants={fadeRight}>
              <h2 className="text-3xl md:text-4xl font-extrabold leading-tight">Tu perfil personalizado</h2>
              <p className="mt-4 text-gray-600">
                Personaliza tu experiencia seleccionando tus categorías favoritas. Guarda promociones,
                ve tu historial de cupones canjeados y gestiona tus preferencias.
              </p>
              <div className="mt-6 space-y-4">
                <div className="flex items-start gap-3">
                  <div className="h-6 w-6 bg-purple-100 rounded-full flex items-center justify-center mt-0.5">
                    <svg className="h-4 w-4 text-purple-600" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M3.172 5.172a4 4 0 015.656 0L10 6.343l1.172-1.171a4 4 0 115.656 5.656L10 17.657l-6.828-6.829a4 4 0 010-5.656z" clipRule="evenodd" />
                    </svg>
                  </div>
                  <div>
                    <h3 className="font-semibold">Sistema de favoritos</h3>
                    <p className="text-gray-600 text-sm">Guarda promociones y negocios para acceso rápido</p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <div className="h-6 w-6 bg-blue-100 rounded-full flex items-center justify-center mt-0.5">
                    <svg className="h-4 w-4 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clipRule="evenodd" />
                    </svg>
                  </div>
                  <div>
                    <h3 className="font-semibold">Historial completo</h3>
                    <p className="text-gray-600 text-sm">Ve todas tus reservas y cupones canjeados</p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <div className="h-6 w-6 bg-green-100 rounded-full flex items-center justify-center mt-0.5">
                    <svg className="h-4 w-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M11.49 3.17c-.38-1.56-2.6-1.56-2.98 0a1.532 1.532 0 01-2.286.948c-1.372-.836-2.942.734-2.106 2.106.54.886.061 2.042-.947 2.287-1.561.379-1.561 2.6 0 2.978a1.532 1.532 0 01.947 2.287c-.836 1.372.734 2.942 2.106 2.106a1.532 1.532 0 012.287.947c.379 1.561 2.6 1.561 2.978 0a1.533 1.533 0 012.287-.947c1.372.836 2.942-.734 2.106-2.106a1.533 1.533 0 01.947-2.287c1.561-.379 1.561-2.6 0-2.978a1.532 1.532 0 01-.947-2.287c.836-1.372-.734-2.942-2.106-2.106a1.532 1.532 0 01-2.287-.947zM10 13a3 3 0 100-6 3 3 0 000 6z" clipRule="evenodd" />
                    </svg>
                  </div>
                  <div>
                    <h3 className="font-semibold">Preferencias personalizadas</h3>
                    <p className="text-gray-600 text-sm">Recibe recomendaciones basadas en tus gustos</p>
                  </div>
                </div>
              </div>
            </motion.div>

            <motion.div
              className="grid grid-cols-2 gap-6"
              variants={stagger}
              initial="hidden"
              whileInView="visible"
              viewport={{ once: true, amount: 0.3 }}
            >
              {/* Sin marcos/bordes/sombras */}
              <motion.div variants={fadeUp} className="relative">
                <Image
                  src="/phone-profile.svg"
                  alt="Perfil de usuario en la app"
                  width={640}
                  height={1300}
                  className="w-full h-auto select-none pointer-events-none"
                />
              </motion.div>
              <motion.div variants={fadeUp} className="relative">
                <Image
                  src="/phone-preferences.svg"
                  alt="Preferencias e intereses"
                  width={640}
                  height={1300}
                  className="w-full h-auto select-none pointer-events-none"
                />
              </motion.div>
            </motion.div>
          </div>
        </motion.section>

        {/* RESERVA Y CANJEA ====================================================== */}
        <motion.section
          className="py-16 md:py-20"
          initial="hidden"
          whileInView="visible"
          viewport={{ once: true, amount: 0.25 }}
        >
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 grid md:grid-cols-2 gap-10 items-center">
            <motion.div variants={fadeRight} className="relative mx-auto w-full max-w-sm">
              {/* Solo imagen */}
              <Image
                src="/phone-promo.svg"
                alt="Detalle de promoción y canje con QR"
                width={640}
                height={1300}
                className="w-full h-auto select-none pointer-events-none"
              />
            </motion.div>

            <motion.div variants={fadeLeft} className="space-y-8">
              <div className="text-center">
                <h2 className="text-3xl md:text-4xl font-extrabold text-[#4B4C7E] mb-4" id="descuentos">
                  Cupones digitales instantáneos
                </h2>
                <p className="text-gray-600 text-lg max-w-2xl mx-auto">
                  Reserva, canjea y ahorra con nuestro sistema de QR en tiempo real. Sin complicaciones, sin esperas.
                </p>
              </div>

              {/* Proceso de 3 pasos */}
              <div className="grid md:grid-cols-3 gap-6">
                <motion.div
                  variants={fadeUp}
                  className="bg-white rounded-2xl p-6 border border-gray-200 shadow-sm hover:shadow-lg transition-all group"
                >
                  <div className="h-16 w-16 mx-auto mb-4 bg-gradient-to-br from-[#4B4C7E] to-[#008D96] rounded-2xl flex items-center justify-center group-hover:scale-110 transition-transform">
                    <svg className="h-8 w-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24" strokeWidth="2">
                      <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
                    </svg>
                  </div>
                  <h3 className="text-lg font-bold text-[#4B4C7E] text-center mb-2">Busca y Selecciona</h3>
                  <p className="text-gray-600 text-sm text-center">Encuentra la promoción perfecta cerca de ti usando el mapa interactivo</p>
                </motion.div>

                <motion.div
                  variants={fadeUp}
                  className="bg-white rounded-2xl p-6 border border-gray-200 shadow-sm hover:shadow-lg transition-all group"
                >
                  <div className="h-16 w-16 mx-auto mb-4 bg-gradient-to-br from-[#4B4C7E] to-[#008D96] rounded-2xl flex items-center justify-center group-hover:scale-110 transition-transform">
                    <svg className="h-8 w-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24" strokeWidth="2">
                      <path strokeLinecap="round" strokeLinejoin="round" d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2H5a2 2 0 00-2-2z" />
                      <path strokeLinecap="round" strokeLinejoin="round" d="M8 15h8" />
                    </svg>
                  </div>
                  <h3 className="text-lg font-bold text-[#4B4C7E] text-center mb-2">Reserva al Instante</h3>
                  <p className="text-gray-600 text-sm text-center">Un toque para reservar y recibir tu código QR único listo para usar</p>
                </motion.div>

                <motion.div
                  variants={fadeUp}
                  className="bg-white rounded-2xl p-6 border border-gray-200 shadow-sm hover:shadow-lg transition-all group"
                >
                  <div className="h-16 w-16 mx-auto mb-4 bg-gradient-to-br from-[#4B4C7E] to-[#008D96] rounded-2xl flex items-center justify-center group-hover:scale-110 transition-transform">
                    <svg className="h-8 w-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24" strokeWidth="2">
                      <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M9 9h6v6H9z"/>
                    </svg>
                  </div>
                  <h3 className="text-lg font-bold text-[#4B4C7E] text-center mb-2">Canjea y Disfruta</h3>
                  <p className="text-gray-600 text-sm text-center">Presenta tu QR en el negocio y disfruta tu descuento al momento</p>
                </motion.div>
              </div>

              {/* Características destacadas */}
              <div className="bg-gradient-to-r from-[#4B4C7E]/5 to-[#008D96]/5 rounded-2xl p-8">
                <h3 className="text-xl font-bold text-[#4B4C7E] text-center mb-6">¿Por qué elegir nuestro sistema?</h3>
                <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
                  <div className="text-center">
                    <div className="h-12 w-12 mx-auto mb-3 bg-white rounded-xl shadow-sm flex items-center justify-center">
                      <svg className="h-6 w-6 text-[#008D96]" fill="none" stroke="currentColor" viewBox="0 0 24 24" strokeWidth="2">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M13 10V3L4 14h7v7l9-11h-7z" />
                      </svg>
                    </div>
                    <h4 className="font-semibold text-[#4B4C7E] mb-1">Instantáneo</h4>
                    <p className="text-gray-600 text-xs">Sin esperas ni confirmaciones</p>
                  </div>

                  <div className="text-center">
                    <div className="h-12 w-12 mx-auto mb-3 bg-white rounded-xl shadow-sm flex items-center justify-center">
                      <svg className="h-6 w-6 text-[#008D96]" fill="none" stroke="currentColor" viewBox="0 0 24 24" strokeWidth="2">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m5-6v6a2 2 0 01-2 2H6a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2z" />
                      </svg>
                    </div>
                    <h4 className="font-semibold text-[#4B4C7E] mb-1">Seguro</h4>
                    <p className="text-gray-600 text-xs">Códigos únicos verificados</p>
                  </div>

                  <div className="text-center">
                    <div className="h-12 w-12 mx-auto mb-3 bg-white rounded-xl shadow-sm flex items-center justify-center">
                      <svg className="h-6 w-6 text-[#008D96]" fill="none" stroke="currentColor" viewBox="0 0 24 24" strokeWidth="2">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                    </div>
                    <h4 className="font-semibold text-[#4B4C7E] mb-1">Tiempo Real</h4>
                    <p className="text-gray-600 text-xs">Stock actualizado al momento</p>
                  </div>

                  <div className="text-center">
                    <div className="h-12 w-12 mx-auto mb-3 bg-white rounded-xl shadow-sm flex items-center justify-center">
                      <svg className="h-6 w-6 text-[#008D96]" fill="none" stroke="currentColor" viewBox="0 0 24 24" strokeWidth="2">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                      </svg>
                    </div>
                    <h4 className="font-semibold text-[#4B4C7E] mb-1">Historial</h4>
                    <p className="text-gray-600 text-xs">Revisa todos tus canjes</p>
                  </div>
                </div>
              </div>

              {/* Categorías disponibles */}
              <div>
                <h3 className="text-xl font-bold text-[#4B4C7E] text-center mb-6">Categorías disponibles</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <motion.div
                    variants={fadeUp}
                    className="bg-white rounded-xl p-4 border border-[#008D96]/20 hover:border-[#008D96]/40 shadow-sm hover:shadow-md transition-all text-center group"
                  >
                    <div className="h-12 w-12 mx-auto mb-3 text-[#008D96] group-hover:scale-110 transition-transform">
                      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" strokeWidth="2">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M12 6v6l4 2" />
                        <circle cx="12" cy="12" r="10" />
                      </svg>
                    </div>
                    <h4 className="font-semibold text-[#4B4C7E] text-sm mb-1">Restaurantes</h4>
                    <p className="text-[#4B4C7E]/60 text-xs">2x1, Descuentos especiales</p>
                  </motion.div>

                  <motion.div
                    variants={fadeUp}
                    className="bg-white rounded-xl p-4 border border-[#008D96]/20 hover:border-[#008D96]/40 shadow-sm hover:shadow-md transition-all text-center group"
                  >
                    <div className="h-12 w-12 mx-auto mb-3 text-[#008D96] group-hover:scale-110 transition-transform">
                      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" strokeWidth="2">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
                      </svg>
                    </div>
                    <h4 className="font-semibold text-[#4B4C7E] text-sm mb-1">Moda</h4>
                    <p className="text-[#4B4C7E]/60 text-xs">Hasta 50% de descuento</p>
                  </motion.div>

                  <motion.div
                    variants={fadeUp}
                    className="bg-white rounded-xl p-4 border border-[#008D96]/20 hover:border-[#008D96]/40 shadow-sm hover:shadow-md transition-all text-center group"
                  >
                    <div className="h-12 w-12 mx-auto mb-3 text-[#008D96] group-hover:scale-110 transition-transform">
                      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" strokeWidth="2">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" />
                      </svg>
                    </div>
                    <h4 className="font-semibold text-[#4B4C7E] text-sm mb-1">Entretenimiento</h4>
                    <p className="text-[#4B4C7E]/60 text-xs">Entradas gratis, descuentos</p>
                  </motion.div>

                  <motion.div
                    variants={fadeUp}
                    className="bg-white rounded-xl p-4 border border-[#008D96]/20 hover:border-[#008D96]/40 shadow-sm hover:shadow-md transition-all text-center group"
                  >
                    <div className="h-12 w-12 mx-auto mb-3 text-[#008D96] group-hover:scale-110 transition-transform">
                      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" strokeWidth="2">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                      </svg>
                    </div>
                    <h4 className="font-semibold text-[#4B4C7E] text-sm mb-1">Salud y Bienestar</h4>
                    <p className="text-[#4B4C7E]/60 text-xs">Consultas, estudios, spa</p>
                  </motion.div>
                </div>
              </div>
            </motion.div>
          </div>
        </motion.section>

        {/* PARA NEGOCIOS ========================================================= */}
        <motion.section
          className="py-16 md:py-20 relative overflow-hidden"
          initial="hidden"
          whileInView="visible"
          viewport={{ once: true, amount: 0.25 }}
        >
          <div className="absolute inset-0 -z-10 bg-gradient-to-br from-[#4B4C7E] via-[#4B4C7E] to-[#008D96]" />
          <div className="absolute inset-0 bg-black/20 -z-10"></div>
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 grid md:grid-cols-2 gap-10 items-center relative">
            <motion.div variants={fadeUp} className="text-[#4B4C7E]">
              <h2 className="text-3xl md:text-4xl font-extrabold leading-tight">
                ¿Tienes un negocio? <br /> Plataforma completa para colaboradores
              </h2>
              <p className="mt-4 text-[#4B4C7E]/80">
                Dashboard web completo + app móvil para gestionar tu negocio. Crea promociones con IA,
                escanea QR para canjes, ve estadísticas en tiempo real y atrae nuevos clientes.
              </p>
              <div className="mt-6 grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="bg-white/95 rounded-xl p-6 backdrop-blur border border-[#008D96]/20">
                  <h3 className="font-bold text-lg mb-3 text-[#4B4C7E]">Dashboard Web</h3>
                  <div className="space-y-2 text-sm text-[#4B4C7E]/80">
                    <div className="flex items-center gap-2">
                      <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                      </svg>
                      <span>Estadísticas con gráficas</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                      </svg>
                      <span>Crear promociones con IA</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                      </svg>
                      <span>Gestión de sucursales</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                      </svg>
                      <span>Notificaciones push</span>
                    </div>
                  </div>
                </div>

                <div className="bg-white/95 rounded-xl p-6 backdrop-blur border border-[#008D96]/20">
                  <h3 className="font-bold text-lg mb-3 text-[#4B4C7E]">App Móvil</h3>
                  <div className="space-y-2 text-sm text-[#4B4C7E]/80">
                    <div className="flex items-center gap-2">
                      <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                      </svg>
                      <span>Escaneo de QR para canjes</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                      </svg>
                      <span>Analytics en tiempo real</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                      </svg>
                      <span>Crear promociones rápido</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                      </svg>
                      <span>Gestión desde cualquier lugar</span>
                    </div>
                  </div>
                </div>
              </div>
              <motion.div className="mt-6" whileHover={{ scale: prefersReduced ? 1 : 1.03 }}>
                <button
                  onClick={() => router.push("/colaboradores")}
                  className="inline-flex items-center rounded-xl bg-white text-gray-900 px-5 py-3 font-semibold shadow hover:shadow-md"
                >
                  Unirse como colaborador
                </button>
              </motion.div>
            </motion.div>

            <motion.div variants={fadeLeft} className="relative">
              {/* Espacio con la imagen, sin marco */}
              <Image
                src="/laptop-dashboard.svg"
                alt="Panel para negocios"
                width={900}
                height={560}
                className="w-full h-auto select-none pointer-events-none rounded-xl"
              />
            </motion.div>
          </div>
        </motion.section>

        {/* CONTACTO ============================================================== */}
        <motion.section
          className="py-16 md:py-20 bg-gray-50"
          initial="hidden"
          whileInView="visible"
          viewport={{ once: true, amount: 0.25 }}
        >
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <motion.h2 variants={fadeUp} className="text-center text-3xl md:text-4xl font-extrabold">
              Contáctanos
            </motion.h2>
            <motion.p variants={fadeUp} className="text-center text-gray-600 mt-2">
              ¿Tienes preguntas? Estamos aquí para ayudarte
            </motion.p>

            <div className="mt-10 grid sm:grid-cols-2 gap-6 max-w-3xl mx-auto">
              <motion.a
                variants={fadeUp}
                whileHover={{ y: prefersReduced ? 0 : -3, boxShadow: "0 12px 30px rgba(0,0,0,0.06)" }}
                href="mailto:soporte@itzipan.gob.mx"
                className="flex items-center gap-4 rounded-2xl border border-gray-200 bg-white p-5 shadow-sm transition"
              >
                <div className="h-11 w-11 grid place-items-center rounded-xl bg-gray-100">
                  <svg viewBox="0 0 24 24" className="h-6 w-6 text-gray-700">
                    <path d="M20 4H4a2 2 0 00-2 2v1.2l10 5.6 10-5.6V6a2 2 0 00-2-2zm0 4.4l-8.7 4.9a1 1 0 01-1 0L4 8.4V18a2 2 0 002 2h12a2 2 0 002-2V8.4z" fill="currentColor" />
                  </svg>
                </div>
                <div>
                  <div className="text-sm text-gray-500">Contactar Soporte</div>
                  <div className="font-semibold">soporte@itzipan.gob.mx</div>
                </div>
              </motion.a>

              <motion.a
                variants={fadeUp}
                whileHover={{ y: prefersReduced ? 0 : -3, boxShadow: "0 12px 30px rgba(0,0,0,0.06)" }}
                href="tel:+525536222800"
                className="flex items-center gap-4 rounded-2xl border border-gray-200 bg-white p-5 shadow-sm transition"
              >
                <div className="h-11 w-11 grid place-items-center rounded-xl bg-gray-100">
                  <svg viewBox="0 0 24 24" className="h-6 w-6 text-gray-700">
                    <path d="M21 16.5v3a2 2 0 01-2.18 2 19.8 19.8 0 01-8.63-3.07 19.4 19.4 0 01-6-6A19.8 19.8 0 011.5 3.18 2 2 0 013.5 1h3a2 2 0 012 1.72 12.8 12.8 0 00.7 2.81 2 2 0 01-.45 2L7.2 8.8a16 16 0 006 6l1.27-1.55a2 2 0 012-.45 12.8 12.8 0 002.81.7A2 2 0 0121 16.5z" fill="currentColor" />
                  </svg>
                </div>
                <div>
                  <div className="text-sm text-gray-500">Línea de Ayuda</div>
                  <div className="font-semibold">+52 55 3622 2800</div>
                </div>
              </motion.a>
            </div>
          </div>
        </motion.section>
      </main>

      {/* FOOTER ================================================================= */}
      <footer className="border-t border-black/5">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-10">
          <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <div className="h-9 w-9 rounded-xl bg-gradient-to-r from-[#4B4C7E] to-[#008D96] grid place-items-center">
                <span className="text-white font-bold">b</span>
              </div>
              <span className="font-semibold tracking-tight">Beneficio Joven</span>
            </div>
            <p className="text-sm text-gray-500">2025 Beneficio Joven. Todos los derechos reservados.</p>
            <div className="flex items-center gap-4 text-sm">
              <a className="text-gray-600 hover:text-gray-900" href="#">Términos</a>
              <a className="text-gray-600 hover:text-gray-900" href="#">Privacidad</a>
              <a className="text-gray-600 hover:text-gray-900" href="#">Ayuda</a>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
}
