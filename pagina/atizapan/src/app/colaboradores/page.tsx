// src/app/colaboradores/page.tsx
"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useSession } from "next-auth/react";
import Image from "next/image";
import { motion, useReducedMotion, type Variants } from "framer-motion";

export default function ColaboradoresPage() {
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
    "supports-[backdrop-filter]:backdrop-blur-md backdrop-blur",
    scrolled
      ? "bg-white/70 border-black/5 shadow-sm"
      : "bg-white/30 border-transparent"
  ].join(" ");

  return (
    <div className="min-h-screen bg-white text-gray-900">
      {/* Header / Navbar */}
      <header className={headerClasses}>
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 h-20 flex items-center justify-between">
          <motion.div
            className="flex items-center gap-3"
            initial={{ opacity: 0, y: -8 }}
            animate={{ opacity: 1, y: 0, transition: { duration: prefersReduced ? 0 : 0.5, ease: EASE } }}
          >
            <div className="h-16 w-16 grid place-items-center">
              <img src="/logo_beneficio_joven.png" alt="" />
            </div>
            <div className="h-14 w-40 grid place-items-center">
              <img src="/atizapan_logo.png" alt="" />
            </div>
          </motion.div>

          <nav className="flex items-center gap-2">
            <motion.button
              whileHover={{ scale: prefersReduced ? 1 : 1.04 }}
              whileTap={{ scale: prefersReduced ? 1 : 0.98 }}
              onClick={() => router.push("/")}
              className="hidden sm:inline-flex h-9 items-center rounded-xl px-4 text-sm font-medium text-gray-700 hover:bg-gray-100"
            >
              Para Usuarios
            </motion.button>
            <motion.button
              whileHover={{ scale: prefersReduced ? 1 : 1.04 }}
              whileTap={{ scale: prefersReduced ? 1 : 0.98 }}
              onClick={() => router.push("/login")}
              className="inline-flex h-9 items-center rounded-xl px-4 text-sm font-medium text-white bg-gradient-to-r from-[#4B4C7E] to-[#008D96] hover:opacity-90"
            >
              Iniciar Sesión
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
                className="text-white pt-16 max-w-3xl"
              >
                <h1 className="text-4xl md:text-6xl font-extrabold leading-[1.05]">
                  Haz crecer tu negocio <br /> con Beneficio Joven
                </h1>
                <p className="mt-6 text-white/90 text-lg">
                  Únete a más de 200 comercios que ya atraen miles de jóvenes clientes.
                  Dashboard web completo + app móvil para gestionar promociones, escanear QR y ver estadísticas en tiempo real.
                </p>
                <div className="mt-4 flex flex-wrap gap-3">
                  <div className="inline-flex items-center gap-2 bg-white/20 rounded-full px-4 py-2 text-white/90">
                    <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z" />
                    </svg>
                    <span className="text-sm font-medium">Sin costo de afiliación</span>
                  </div>
                  <div className="inline-flex items-center gap-2 bg-white/20 rounded-full px-4 py-2 text-white/90">
                    <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zM3 10a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H4a1 1 0 01-1-1v-6zM14 9a1 1 0 00-1 1v6a1 1 0 001 1h2a1 1 0 001-1v-6a1 1 0 00-1-1h-2z" clipRule="evenodd" />
                    </svg>
                    <span className="text-sm font-medium">Escaneo QR</span>
                  </div>
                  <div className="inline-flex items-center gap-2 bg-white/20 rounded-full px-4 py-2 text-white/90">
                    <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M2 10a8 8 0 018-8v8h8a8 8 0 11-16 0z" />
                      <path d="M12 2.252A8.014 8.014 0 0117.748 8H12V2.252z" />
                    </svg>
                    <span className="text-sm font-medium">Analytics completos</span>
                  </div>
                </div>

                <motion.div
                  className="mt-8 flex items-center gap-4"
                  initial={{ opacity: 0, y: 12 }}
                  animate={{ opacity: 1, y: 0, transition: { delay: prefersReduced ? 0 : 0.2, duration: 0.5, ease: EASE } }}
                >
                  <motion.button
                    whileHover={{ scale: prefersReduced ? 1 : 1.05 }}
                    whileTap={{ scale: prefersReduced ? 1 : 0.98 }}
                    onClick={() => router.push("/register")}
                    className="inline-flex items-center rounded-xl bg-white text-[#4B4C7E] px-6 py-3 font-semibold shadow-lg hover:shadow-xl transition-shadow"
                  >
                    <svg className="h-5 w-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clipRule="evenodd" />
                    </svg>
                    Registrar mi Negocio
                  </motion.button>
                  <motion.button
                    whileHover={{ scale: prefersReduced ? 1 : 1.05 }}
                    whileTap={{ scale: prefersReduced ? 1 : 0.98 }}
                    onClick={() => document.getElementById('beneficios')?.scrollIntoView({ behavior: 'smooth' })}
                    className="inline-flex items-center rounded-xl border-2 border-white text-white px-6 py-3 font-semibold hover:bg-white/10"
                  >
                    Ver Beneficios
                  </motion.button>
                </motion.div>
              </motion.div>
            </div>
          </div>

          {/* Imagen del dashboard posicionada en la esquina inferior derecha */}
          <div className="absolute bottom-0 right-0 w-80 md:w-96 lg:w-[28rem] xl:w-[38rem] z-10">
            <div className="bg-white rounded-tl-xl shadow-2xl overflow-hidden border">
              <div className="bg-gray-100 p-3 border-b">
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 bg-red-500 rounded-full"></div>
                  <div className="w-3 h-3 bg-yellow-500 rounded-full"></div>
                  <div className="w-3 h-3 bg-green-500 rounded-full"></div>
                  <div className="ml-4 text-sm text-gray-600">Panel de Colaboradores - Beneficio Joven</div>
                </div>
              </div>
              <div className="p-6">
                <div className="grid grid-cols-3 gap-4 mb-6">
                  <div className="bg-gradient-to-br from-[#4B4C7E]/10 to-[#008D96]/10 rounded-lg p-4 text-center">
                    <div className="text-2xl font-bold text-[#4B4C7E]">1,234</div>
                    <div className="text-sm text-gray-600">Cupones canjeados</div>
                  </div>
                  <div className="bg-gradient-to-br from-green-50 to-green-100 rounded-lg p-4 text-center">
                    <div className="text-2xl font-bold text-green-600">$45,678</div>
                    <div className="text-sm text-gray-600">Ventas generadas</div>
                  </div>
                  <div className="bg-gradient-to-br from-blue-50 to-blue-100 rounded-lg p-4 text-center">
                    <div className="text-2xl font-bold text-blue-600">892</div>
                    <div className="text-sm text-gray-600">Nuevos clientes</div>
                  </div>
                </div>
                <div className="space-y-3">
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="font-semibold text-[#4B4C7E]">Pizza 2x1</span>
                    <span className="text-[#008D96] font-semibold">24 canjes hoy</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="font-semibold text-[#4B4C7E]">Descuento 30%</span>
                    <span className="text-[#008D96] font-semibold">18 canjes hoy</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* BENEFICIOS =========================================================== */}
        <motion.section
          className="py-16 md:py-20 bg-gray-50"
          variants={stagger}
          initial="hidden"
          whileInView="visible"
          viewport={{ once: true, amount: 0.2 }}
          id="beneficios"
        >
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <motion.h2 className="text-center text-3xl md:text-4xl font-extrabold text-gray-900 mb-4" variants={fadeUp}>
              ¿Por qué elegir Beneficio Joven?
            </motion.h2>
            <motion.p className="text-center text-gray-600 max-w-2xl mx-auto mb-12" variants={fadeUp}>
              Herramientas completas para hacer crecer tu negocio y atraer más clientes jóvenes
            </motion.p>

            <div className="grid md:grid-cols-3 gap-8">
              {[
                {
                  title: "Dashboard Web Completo",
                  desc: "Gestiona promociones, ve estadísticas detalladas con gráficas, crea ofertas con IA y administra múltiples sucursales desde una sola plataforma.",
                  icon: (
                    <svg className="h-8 w-8" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z"/>
                    </svg>
                  ),
                  features: ["Estadísticas en tiempo real", "Creación con IA", "Multi-sucursales", "Notificaciones push"]
                },
                {
                  title: "App Móvil para Colaboradores",
                  desc: "Escanea códigos QR para validar cupones, crea promociones rápidas y accede a tus analytics desde cualquier lugar.",
                  icon: (
                    <svg className="h-8 w-8" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M17 2H7c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h10c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM7 4h10v12H7V4z"/>
                    </svg>
                  ),
                  features: ["Escaneo QR instantáneo", "Creación móvil", "Analytics móviles", "Gestión on-the-go"]
                },
                {
                  title: "Sin Costos de Afiliación",
                  desc: "Únete gratis a la plataforma y comienza a atraer clientes inmediatamente. Solo pagas cuando generas ventas.",
                  icon: (
                    <svg className="h-8 w-8" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                    </svg>
                  ),
                  features: ["Registro gratuito", "Sin cuotas mensuales", "Pago por resultados", "ROI inmediato"]
                }
              ].map((benefit, i) => (
                <motion.div
                  key={benefit.title}
                  variants={fadeUp}
                  whileHover={{ y: prefersReduced ? 0 : -8, boxShadow: "0 20px 40px rgba(0,0,0,0.1)" }}
                  className="bg-white rounded-2xl p-8 border border-gray-200 shadow-sm transition-all"
                >
                  <div className="h-16 w-16 rounded-2xl bg-gradient-to-r from-[#4B4C7E]/10 to-[#008D96]/10 grid place-items-center text-[#008D96] mb-6">
                    {benefit.icon}
                  </div>
                  <h3 className="text-xl font-bold text-[#4B4C7E] mb-4">{benefit.title}</h3>
                  <p className="text-gray-600 mb-6 leading-relaxed">{benefit.desc}</p>
                  <div className="space-y-2">
                    {benefit.features.map((feature) => (
                      <div key={feature} className="flex items-center gap-2 text-sm">
                        <div className="h-1.5 w-1.5 bg-[#008D96] rounded-full"></div>
                        <span className="text-[#4B4C7E]/80">{feature}</span>
                      </div>
                    ))}
                  </div>
                </motion.div>
              ))}
            </div>
          </div>
        </motion.section>

        {/* CÓMO FUNCIONA ======================================================== */}
        <motion.section
          className="py-16 md:py-20"
          initial="hidden"
          whileInView="visible"
          viewport={{ once: true, amount: 0.25 }}
        >
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <motion.h2 variants={fadeUp} className="text-center text-3xl md:text-4xl font-extrabold text-gray-900 mb-4">
              Cómo empezar en 3 pasos
            </motion.h2>
            <motion.p variants={fadeUp} className="text-center text-gray-600 max-w-2xl mx-auto mb-12">
              Proceso simple y rápido para comenzar a atraer más clientes
            </motion.p>

            <div className="grid md:grid-cols-3 gap-8">
              {[
                {
                  step: "01",
                  title: "Regístrate Gratis",
                  desc: "Completa el formulario con los datos de tu negocio. Verificación en menos de 24 horas.",
                  icon: "📝"
                },
                {
                  step: "02",
                  title: "Configura tu Perfil",
                  desc: "Sube fotos, agrega información de tus sucursales y crea tus primeras promociones.",
                  icon: "⚙️"
                },
                {
                  step: "03",
                  title: "Comienza a Vender",
                  desc: "Los usuarios ya pueden ver y reservar tus promociones. Usa la app para escanear cupones.",
                  icon: "🚀"
                }
              ].map((step, i) => (
                <motion.div
                  key={step.step}
                  variants={fadeUp}
                  className="text-center"
                >
                  <div className="relative mb-6">
                    <div className="h-20 w-20 mx-auto rounded-full bg-gradient-to-r from-[#4B4C7E] to-[#008D96] flex items-center justify-center text-white font-bold text-xl">
                      {step.step}
                    </div>
                    {i < 2 && (
                      <div className="hidden md:block absolute top-10 left-[60%] w-[80%] h-0.5 bg-gradient-to-r from-[#4B4C7E]/30 to-[#008D96]/30"></div>
                    )}
                  </div>
                  <h3 className="text-xl font-bold text-[#4B4C7E] mb-3">{step.title}</h3>
                  <p className="text-gray-600">{step.desc}</p>
                </motion.div>
              ))}
            </div>
          </div>
        </motion.section>

        {/* TESTIMONIOS ========================================================== */}
        <motion.section
          className="py-16 md:py-20 bg-gradient-to-br from-[#4B4C7E]/5 to-[#008D96]/5"
          initial="hidden"
          whileInView="visible"
          viewport={{ once: true, amount: 0.25 }}
        >
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <motion.h2 variants={fadeUp} className="text-center text-3xl md:text-4xl font-extrabold text-gray-900 mb-12">
              Lo que dicen nuestros colaboradores
            </motion.h2>

            <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
              {[
                {
                  name: "María González",
                  business: "Restaurante La Tradición",
                  quote: "Desde que nos unimos a Beneficio Joven, hemos visto un aumento del 40% en clientes jóvenes. La plataforma es muy fácil de usar.",
                  avatar: "MG"
                },
                {
                  name: "Carlos Ruiz",
                  business: "Gimnasio FitZone",
                  quote: "Las estadísticas en tiempo real nos ayudan a entender mejor a nuestros clientes. Hemos optimizado nuestras promociones y mejorado las ventas.",
                  avatar: "CR"
                },
                {
                  name: "Ana Martínez",
                  business: "Boutique Estilo",
                  quote: "La app móvil es perfecta para escanear cupones rápidamente. Nuestros empleados la adoptaron de inmediato.",
                  avatar: "AM"
                }
              ].map((testimonial) => (
                <motion.div
                  key={testimonial.name}
                  variants={fadeUp}
                  className="bg-white rounded-2xl p-6 shadow-sm border border-gray-200"
                >
                  <div className="flex items-center gap-4 mb-4">
                    <div className="h-12 w-12 rounded-full bg-gradient-to-r from-[#4B4C7E] to-[#008D96] flex items-center justify-center text-white font-semibold">
                      {testimonial.avatar}
                    </div>
                    <div>
                      <div className="font-semibold text-[#4B4C7E]">{testimonial.name}</div>
                      <div className="text-sm text-gray-600">{testimonial.business}</div>
                    </div>
                  </div>
                  <p className="text-gray-600 italic">"{testimonial.quote}"</p>
                </motion.div>
              ))}
            </div>
          </div>
        </motion.section>

        {/* CTA FINAL ============================================================ */}
        <motion.section
          className="py-16 md:py-20 bg-gradient-to-br from-[#2A2B5F] via-[#4B4C7E] to-[#006B73]"
          initial="hidden"
          whileInView="visible"
          viewport={{ once: true, amount: 0.25 }}
        >
          <div className="mx-auto max-w-4xl px-4 sm:px-6 lg:px-8 text-center">
            <motion.h2 variants={fadeUp} className="text-3xl md:text-4xl font-extrabold text-white mb-6">
              ¿Listo para hacer crecer tu negocio?
            </motion.h2>
            <motion.p variants={fadeUp} className="text-white/90 text-lg mb-8">
              Únete a cientos de negocios que ya están aprovechando el poder de Beneficio Joven para atraer más clientes y aumentar sus ventas.
            </motion.p>
            <motion.div variants={fadeUp} className="flex flex-col sm:flex-row gap-4 justify-center">
              <motion.button
                whileHover={{ scale: prefersReduced ? 1 : 1.05 }}
                whileTap={{ scale: prefersReduced ? 1 : 0.98 }}
                onClick={() => router.push("/register")}
                className="inline-flex items-center justify-center rounded-xl bg-white text-[#4B4C7E] px-8 py-4 font-semibold text-lg shadow-lg hover:shadow-xl transition-shadow"
              >
                <svg className="h-5 w-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clipRule="evenodd" />
                </svg>
                Registrar mi Negocio Gratis
              </motion.button>
              <motion.button
                whileHover={{ scale: prefersReduced ? 1 : 1.05 }}
                whileTap={{ scale: prefersReduced ? 1 : 0.98 }}
                onClick={() => router.push("/contacto")}
                className="inline-flex items-center justify-center rounded-xl border-2 border-white text-white px-8 py-4 font-semibold text-lg hover:bg-white/10"
              >
                Contactar Ventas
              </motion.button>
            </motion.div>
          </div>
        </motion.section>
      </main>

      {/* FOOTER =============================================================== */}
      <footer className="border-t border-black/5">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-10">
          <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <div className="h-9 w-9 rounded-xl bg-gradient-to-r from-[#4B4C7E] to-[#008D96] grid place-items-center">
                <span className="text-white font-bold">b</span>
              </div>
              <span className="font-semibold tracking-tight">Beneficio Joven - Colaboradores</span>
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