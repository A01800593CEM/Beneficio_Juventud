-- ============================================================================
-- Base de datos Beneficio Juve+  (MySQL 8.0+)
-- ============================================================================

CREATE DATABASE IF NOT EXISTS beneficio_juveplus
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE beneficio_juveplus;

-- ============================================================================
-- TABLAS DE APOYO (Lealtad / Niveles)
-- ============================================================================

CREATE TABLE nivel (
  nivel_id        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombre_nivel    VARCHAR(40) NOT NULL,
  puntos_minimos  INT UNSIGNED NOT NULL DEFAULT 0,
  beneficios      TEXT,
  prioridad       INT UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (nivel_id),
  UNIQUE KEY uq_nivel_nombre (nombre_nivel)
) ENGINE=InnoDB;

-- ============================================================================
-- USUARIOS
-- ============================================================================

CREATE TABLE usuario (
  usuario_id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombre_completo    VARCHAR(150) NOT NULL,
  fecha_nacimiento   DATE NOT NULL,
  curp               VARCHAR(18),
  direccion          VARCHAR(255),
  codigo_postal      VARCHAR(10),
  telefono           VARCHAR(20),
  correo_electronico VARCHAR(160) NOT NULL,
  contrasena_hash    VARCHAR(255) NOT NULL,
  fecha_registro     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  nivel_id           BIGINT UNSIGNED,
  puntos             INT UNSIGNED NOT NULL DEFAULT 0,
  qr_usuario         VARCHAR(100) NOT NULL,
  estado_cuenta      ENUM('activo','suspendido','eliminado') NOT NULL DEFAULT 'activo',
  foto_perfil        VARCHAR(255),
  verificado         TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (usuario_id),
  UNIQUE KEY uq_usuario_correo (correo_electronico),
  UNIQUE KEY uq_usuario_qr (qr_usuario),
  KEY idx_usuario_cp (codigo_postal),
  CONSTRAINT fk_usuario_nivel
    FOREIGN KEY (nivel_id) REFERENCES nivel(nivel_id)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

-- Documentos de verificación (INE, CURP, comprobante)
CREATE TABLE documento_verif (
  doc_id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  usuario_id     BIGINT UNSIGNED NOT NULL,
  tipo           ENUM('INE','CURP','COMPROBANTE') NOT NULL,
  archivo_url    VARCHAR(255) NOT NULL,
  fecha_carga    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  estado         ENUM('pendiente','aprobado','rechazado') NOT NULL DEFAULT 'pendiente',
  motivo_rechazo VARCHAR(255),
  PRIMARY KEY (doc_id),
  KEY idx_doc_usuario (usuario_id, estado),
  CONSTRAINT fk_doc_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuario(usuario_id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- Migración desde tarjeta física
CREATE TABLE tarjeta_fisica_migra (
  migracion_id     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  usuario_id       BIGINT UNSIGNED NOT NULL,
  numero_tarjeta   VARCHAR(50) NOT NULL,
  fecha_migracion  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  puntos_bono      INT UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (migracion_id),
  UNIQUE KEY uq_numero_tarjeta (numero_tarjeta),
  KEY idx_migra_usuario (usuario_id),
  CONSTRAINT fk_migra_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuario(usuario_id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================================
-- COLABORADORES (NEGOCIOS)
-- ============================================================================

CREATE TABLE colaborador (
  colaborador_id     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombre_negocio     VARCHAR(160) NOT NULL,
  rfc                VARCHAR(20),
  representante_nombre VARCHAR(120),
  telefono           VARCHAR(20),
  correo             VARCHAR(160),
  direccion          VARCHAR(255),
  codigo_postal      VARCHAR(10),
  categoria          VARCHAR(60),
  logo_url           VARCHAR(255),
  descripcion        TEXT,
  fecha_registro     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  estado             ENUM('pendiente','activo','inactivo') NOT NULL DEFAULT 'pendiente',
  usuario_admin_id   BIGINT UNSIGNED,
  lat                DECIMAL(10,7),
  lng                DECIMAL(10,7),
  PRIMARY KEY (colaborador_id),
  KEY idx_col_categoria (categoria),
  KEY idx_col_cp (codigo_postal),
  CONSTRAINT fk_colab_usuario_admin
    FOREIGN KEY (usuario_admin_id) REFERENCES usuario(usuario_id)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

-- ============================================================================
-- PROMOCIONES / CUPONES
-- ============================================================================

CREATE TABLE promocion (
  promocion_id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  colaborador_id    BIGINT UNSIGNED NOT NULL,
  titulo            VARCHAR(140) NOT NULL,
  descripcion       TEXT,
  imagen_url        VARCHAR(255),
  fecha_inicio      DATE NOT NULL,
  fecha_fin         DATE NOT NULL,
  categoria         VARCHAR(60),
  descuento         DECIMAL(10,2) NOT NULL,        -- define en app si es % o monto
  stock_total       INT UNSIGNED NOT NULL DEFAULT 0,
  stock_disponible  INT UNSIGNED NOT NULL DEFAULT 0,
  nivel_minimo      BIGINT UNSIGNED,
  estado            ENUM('activa','inactiva','agotada') NOT NULL DEFAULT 'activa',
  requiere_qr       TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (promocion_id),
  KEY idx_promocion_vigencia (fecha_inicio, fecha_fin),
  KEY idx_promocion_categoria (categoria),
  CONSTRAINT fk_promo_colab
    FOREIGN KEY (colaborador_id) REFERENCES colaborador(colaborador_id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_promo_nivel_min
    FOREIGN KEY (nivel_minimo) REFERENCES nivel(nivel_id)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

-- ============================================================================
-- RESERVAS
-- ============================================================================

CREATE TABLE reserva (
  reserva_id        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  usuario_id        BIGINT UNSIGNED NOT NULL,
  promocion_id      BIGINT UNSIGNED NOT NULL,
  fecha_reserva     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_limite_uso  TIMESTAMP NULL,
  estado            ENUM('reservado','usado','liberado','expirado') NOT NULL DEFAULT 'reservado',
  qr_cupon          VARCHAR(120) NOT NULL,
  PRIMARY KEY (reserva_id),
  UNIQUE KEY uq_reserva_qr (qr_cupon),
  KEY idx_reserva_usuario_estado (usuario_id, estado),
  KEY idx_reserva_promocion (promocion_id),
  CONSTRAINT fk_reserva_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuario(usuario_id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_reserva_promocion
    FOREIGN KEY (promocion_id) REFERENCES promocion(promocion_id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================================
-- USO / REDENCIÓN
-- ============================================================================

CREATE TABLE uso_cupon (
  uso_id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  usuario_id        BIGINT UNSIGNED NOT NULL,
  colaborador_id    BIGINT UNSIGNED NOT NULL,
  promocion_id      BIGINT UNSIGNED NOT NULL,
  fecha_uso         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  monto_ahorrado    DECIMAL(12,2) NOT NULL DEFAULT 0,
  puntos_otorgados  INT UNSIGNED NOT NULL DEFAULT 0,
  metodo_validacion ENUM('QR','CODIGO','POS') NOT NULL DEFAULT 'QR',
  PRIMARY KEY (uso_id),
  KEY idx_uso_fechas (fecha_uso),
  KEY idx_uso_segmentacion (colaborador_id, promocion_id),
  CONSTRAINT fk_uso_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuario(usuario_id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_uso_colab
    FOREIGN KEY (colaborador_id) REFERENCES colaborador(colaborador_id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_uso_promocion
    FOREIGN KEY (promocion_id) REFERENCES promocion(promocion_id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================================
-- NOTIFICACIONES (con segmentación JSON)
-- ============================================================================

CREATE TABLE notificacion (
  notificacion_id     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  titulo              VARCHAR(140) NOT NULL,
  mensaje             TEXT NOT NULL,
  tipo                ENUM('promocion','sistema','recordatorio') NOT NULL,
  fecha_envio         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  destinatario_tipo   ENUM('usuario','colaborador','todos','segmento') NOT NULL,
  destinatario_id     BIGINT UNSIGNED NULL,
  estado              ENUM('pendiente','enviada','error') NOT NULL DEFAULT 'pendiente',
  criterios_segmento  JSON NULL,
  PRIMARY KEY (notificacion_id),
  KEY idx_notif_tipo_estado (tipo, estado)
) ENGINE=InnoDB;

-- ============================================================================
-- ADMINISTRADORES
-- ============================================================================

CREATE TABLE administrador (
  admin_id        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombre          VARCHAR(120) NOT NULL,
  correo          VARCHAR(160) NOT NULL,
  telefono        VARCHAR(20),
  contrasena_hash VARCHAR(255) NOT NULL,
  rol             ENUM('superadmin','soporte','marketing') NOT NULL DEFAULT 'soporte',
  estado          ENUM('activo','inactivo') NOT NULL DEFAULT 'activo',
  PRIMARY KEY (admin_id),
  UNIQUE KEY uq_admin_correo (correo)
) ENGINE=InnoDB;

-- ============================================================================
-- REPORTES (resultado resumido + URL a export)
-- ============================================================================

CREATE TABLE reporte (
  reporte_id        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  tipo_reporte      VARCHAR(80) NOT NULL,
  fecha_generacion  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  parametros        JSON NOT NULL,
  resultado_resumen JSON NULL,
  archivo_url       VARCHAR(255),
  PRIMARY KEY (reporte_id),
  KEY idx_reporte_tipo_fecha (tipo_reporte, fecha_generacion)
) ENGINE=InnoDB;

-- ============================================================================
-- TRIGGERS / REGLAS DE NEGOCIO
-- ============================================================================

-- 1) Al insertar un uso: descuenta stock de la promoción y suma puntos al usuario.
DELIMITER $$

CREATE TRIGGER trg_uso_after_insert
AFTER INSERT ON uso_cupon
FOR EACH ROW
BEGIN
  -- Descontar stock_disponible (sin bajar de 0)
  UPDATE promocion
     SET stock_disponible = CASE
                              WHEN stock_disponible > 0 THEN stock_disponible - 1
                              ELSE 0
                            END,
         estado = CASE
                    WHEN stock_disponible <= 1 THEN 'agotada'
                    ELSE estado
                  END
   WHERE promocion_id = NEW.promocion_id;

  -- Sumar puntos al usuario
  UPDATE usuario
     SET puntos = puntos + NEW.puntos_otorgados
   WHERE usuario_id = NEW.usuario_id;

  -- Recalcular nivel por puntos (elige el nivel con mayor puntos_minimos <= puntos)
  UPDATE usuario u
     JOIN (
           SELECT n.nivel_id
           FROM nivel n
           WHERE n.puntos_minimos <= u.puntos
           ORDER BY n.puntos_minimos DESC
           LIMIT 1
          ) x
       SET u.nivel_id = x.nivel_id
   WHERE u.usuario_id = NEW.usuario_id;
END$$

-- 2) Al actualizar una reserva: si se libera o expira, reponer stock de la promoción.
CREATE TRIGGER trg_reserva_after_update
AFTER UPDATE ON reserva
FOR EACH ROW
BEGIN
  IF (OLD.estado IN ('reservado') AND NEW.estado IN ('liberado','expirado')) THEN
      UPDATE promocion
         SET stock_disponible = stock_disponible + 1,
             estado = 'activa'
       WHERE promocion_id = NEW.promocion_id
         AND stock_disponible < stock_total;
  END IF;
END$$

DELIMITER ;

-- ============================================================================
-- EVENTO (opcional) para expirar reservas automáticamente al pasar fecha_limite_uso
-- Habilita el event scheduler: SET GLOBAL event_scheduler = ON;
-- ============================================================================

CREATE EVENT IF NOT EXISTS ev_expira_reservas
ON SCHEDULE EVERY 5 MINUTE
DO
  UPDATE reserva
     SET estado = 'expirado'
   WHERE estado = 'reservado'
     AND fecha_limite_uso IS NOT NULL
     AND fecha_limite_uso < CURRENT_TIMESTAMP;

-- ============================================================================
-- VISTAS (agregados para métricas). MySQL no tiene materialized views nativas.
-- ============================================================================

CREATE OR REPLACE VIEW v_uso_por_zona AS
SELECT
  c.codigo_postal,
  p.categoria,
  DATE_FORMAT(u.fecha_uso, '%Y-%m-01') AS mes,
  COUNT(*) AS usos,
  SUM(u.monto_ahorrado) AS ahorro_total
FROM uso_cupon u
JOIN usuario us   ON us.usuario_id = u.usuario_id
JOIN colaborador c ON c.colaborador_id = u.colaborador_id
JOIN promocion p   ON p.promocion_id = u.promocion_id
GROUP BY c.codigo_postal, p.categoria, DATE_FORMAT(u.fecha_uso, '%Y-%m-01');

CREATE OR REPLACE VIEW v_rfm_usuarios AS
SELECT
  u.usuario_id,
  MAX(uc.fecha_uso) AS last_use,
  COUNT(uc.uso_id)  AS frequency,
  COALESCE(SUM(uc.monto_ahorrado),0) AS monetary
FROM usuario u
LEFT JOIN uso_cupon uc ON uc.usuario_id = u.usuario_id
GROUP BY u.usuario_id;

-- ============================================================================
-- ÍNDICES ADICIONALES RECOMENDADOS
-- ============================================================================

CREATE INDEX idx_usuario_fecha_registro ON usuario(fecha_registro);
CREATE INDEX idx_promocion_estado ON promocion(estado);
CREATE INDEX idx_reserva_estado_fecha ON reserva(estado, fecha_reserva);
CREATE INDEX idx_uso_usuario_fecha ON uso_cupon(usuario_id, fecha_uso);
