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
  const EASE: number[] = [0.22, 1, 0.36, 1];

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
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <motion.div
            className="flex items-center gap-3"
            initial={{ opacity: 0, y: -8 }}
            animate={{ opacity: 1, y: 0, transition: { duration: prefersReduced ? 0 : 0.5, ease: EASE } }}
          >
            <div className="h-9 w-9 rounded-xl bg-gradient-to-r from-[#4B4C7E] to-[#008D96] grid place-items-center">
              <span className="text-white font-bold">b</span>
            </div>
            <span className="font-semibold tracking-tight">BENEFICIO JOVEN</span>
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
              className="h-9 items-center rounded-xl px-4 text-sm font-semibold text-white bg-gradient-to-r from-[#4B4C7E] to-[#008D96] hover:opacity-90"
              aria-label="Para Negocios"
            >
              Para Negocios
            </motion.button>
          </nav>
        </div>
      </header>

      <main>
        {/* HERO ================================================================== */}
        <section className="relative overflow-hidden">
          {/* Fondos animados */}
          <motion.div
            aria-hidden
            className="pointer-events-none absolute -top-32 -left-32 h-80 w-80 rounded-full bg-[#4B4C7E] blur-3xl opacity-30"
            animate={{ y: prefersReduced ? 0 : [0, 16, 0] }}
            transition={{ repeat: Infinity, duration: 10, ease: "easeInOut" }}
          />
          <motion.div
            aria-hidden
            className="pointer-events-none absolute -bottom-24 -right-24 h-96 w-96 rounded-full bg-[#008D96] blur-3xl opacity-30"
            animate={{ y: prefersReduced ? 0 : [0, -20, 0] }}
            transition={{ repeat: Infinity, duration: 12, ease: "easeInOut" }}
          />

          <div className="absolute inset-0 -z-10 bg-gradient-to-br from-[#4B4C7E] via-[#4B4C7E] to-[#008D96]" />
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-14 md:py-20 grid md:grid-cols-2 gap-12 items-center">
            <motion.div
              variants={fadeUp}
              initial="hidden"
              animate="visible"
              className="text-white"
            >
              <h1 className="text-4xl md:text-6xl font-extrabold leading-[1.05]">
                Ahorra en grande. <br /> Descubre local.
              </h1>
              <p className="mt-6 text-white/90 text-lg">
                Beneficio Joven es tu app de cupones para encontrar descuentos increíbles en comida,
                moda, entretenimiento y mucho más. ¡Todo cerca de ti!
              </p>

              <motion.div
                className="mt-8 flex items-center gap-4"
                initial={{ opacity: 0, y: 12 }}
                animate={{ opacity: 1, y: 0, transition: { delay: prefersReduced ? 0 : 0.2, duration: 0.5, ease: EASE } }}
              >
                <a href="#" className="inline-flex" aria-label="Disponible en Google Play" title="Disponible en Google Play">
                  <Image
                    src="/google-play-badge.png"
                    alt="Disponible en Google Play"
                    width={180}
                    height={54}
                    className="h-12 w-auto"
                    priority
                  />
                </a>
              </motion.div>
            </motion.div>

            {/* Solo la imagen, sin marco/bordes/sombras */}
            <div className="relative mx-auto w-full max-w-sm md:max-w-md">
              <Image
                src="/phone-home.svg"
                alt="Vista previa de la app"
                width={420}
                height={840}
                className="w-full h-auto select-none pointer-events-none"
                priority
              />
            </div>
          </div>
        </section>

        {/* CÓMO FUNCIONA ========================================================= */}
        <motion.section
          className="py-16 md:py-20 bg-gray-50"
          variants={stagger}
          initial="hidden"
          whileInView="visible"
          viewport={{ once: true, amount: 0.2 }}
        >
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <motion.h2 className="text-center text-lg font-semibold text-gray-600 mb-2" variants={fadeUp}>
              Cómo Funciona
            </motion.h2>

            <div className="grid md:grid-cols-3 gap-6 mt-6">
              {[
                {
                  title: "Explora Promociones",
                  desc: "Usa nuestro mapa interactivo para ver ofertas cerca de ti o filtra por tus categorías favoritas.",
                  icon: (
                    <svg viewBox="0 0 24 24" className="h-7 w-7">
                      <path d="M12 2a7 7 0 00-7 7c0 5.25 7 13 7 13s7-7.75 7-13a7 7 0 00-7-7zm0 9.5a2.5 2.5 0 110-5 2.5 2.5 0 010 5z" fill="currentColor" />
                    </svg>
                  ),
                },
                {
                  title: "Guarda tus Favoritos",
                  desc: "¿Viste un cupón que te gustó? Guarda cupones y negocios para tenerlos siempre a la mano.",
                  icon: (
                    <svg viewBox="0 0 24 24" className="h-7 w-7">
                      <path d="M12 21s-6.716-4.632-9.193-8.11C.33 10.222 1.286 7 4.2 7c1.88 0 3.037 1.245 3.8 2.3C8.763 8.245 9.92 7 11.8 7c2.915 0 3.87 3.222 1.393 5.89C18.716 16.368 12 21 12 21z" fill="currentColor" />
                    </svg>
                  ),
                },
                {
                  title: "Canjea y Disfruta",
                  desc: "Enseña tu cupón en el establecimiento para aplicar tu descuento al instante.",
                  icon: (
                    <svg viewBox="0 0 24 24" className="h-7 w-7">
                      <path d="M3 6a3 3 0 013-3h8l4 4v10a3 3 0 01-3 3H6a3 3 0 01-3-3V6z" fill="currentColor" />
                    </svg>
                  ),
                },
              ].map((c) => (
                <motion.div
                  key={c.title}
                  variants={fadeUp}
                  whileHover={{ y: prefersReduced ? 0 : -4, boxShadow: "0 10px 30px rgba(0,0,0,0.06)" }}
                  className="rounded-2xl bg-white p-7 border border-gray-200 shadow-sm transition"
                >
                  <div className="h-12 w-12 rounded-2xl bg-gradient-to-r from-[#4B4C7E]/10 to-[#008D96]/10 grid place-items-center text-[#008D96]">
                    {c.icon}
                  </div>
                  <h3 className="mt-4 text-lg font-semibold">{c.title}</h3>
                  <p className="mt-2 text-gray-600 text-sm leading-relaxed">{c.desc}</p>
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
              <h2 className="text-3xl md:text-4xl font-extrabold leading-tight">Descubre ofertas a tu alrededor</h2>
              <p className="mt-4 text-gray-600">
                Activa tu ubicación y navega por el mapa para encontrar los mejores descuentos y locales que están a solo unos pasos de ti.
              </p>
              <p className="mt-4 font-semibold text-gray-800">¡Perfecto para descubrir tu nuevo lugar favorito!</p>
            </motion.div>

            <motion.div variants={fadeLeft} className="order-1 md:order-2">
              {/* Solo espacio con la imagen, sin marco */}
              <div className="relative mx-auto w-full max-w-sm">
                <Image
                  src="/phone-map.png"
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
              <h2 className="text-3xl md:text-4xl font-extrabold leading-tight">Una experiencia hecha para ti</h2>
              <p className="mt-4 text-gray-600">
                Desde que entras, personalizas tu experiencia seleccionando tus intereses. Guarda tus cupones y negocios preferidos y mantienes el control total de tu perfil con todas tus preferencias.
              </p>
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
                  src="/phone-profile.png"
                  alt="Perfil de usuario en la app"
                  width={640}
                  height={1300}
                  className="w-full h-auto select-none pointer-events-none"
                />
              </motion.div>
              <motion.div variants={fadeUp} className="relative">
                <Image
                  src="/phone-preferences.png"
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
                src="/phone-promo.png"
                alt="Detalle de promoción y canje con QR"
                width={640}
                height={1300}
                className="w-full h-auto select-none pointer-events-none"
              />
            </motion.div>

            <motion.div variants={fadeLeft}>
              <h2 className="text-3xl md:text-4xl font-extrabold leading-tight">Reserva y canjea sin complicaciones</h2>
              <p className="mt-4 text-gray-600">
                Revisa los términos, escoge tu cupón favorito, resérvalo con un solo toque y cánjealo cuando quieras. Tu historial guarda todos los cupones usados y favoritos para un control total.
              </p>
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
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 grid md:grid-cols-2 gap-10 items-center">
            <motion.div variants={fadeUp} className="text-white">
              <h2 className="text-3xl md:text-4xl font-extrabold leading-tight">
                ¿Tienes un negocio? <br /> Únete a Beneficio Joven
              </h2>
              <p className="mt-4 text-white/90">
                Atrae a miles de nuevos clientes, aumenta tu visibilidad y gestiona tus propias promociones. Nuestra plataforma te da estadísticas detalladas e incluso te permite ¡generar promociones con Inteligencia Artificial!
              </p>
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
                src="/laptop-dashboard.png"
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
