-- ============================================================================
-- Base de datos Beneficio Juve+  (MySQL 8.0+)
-- ============================================================================

CREATE DATABASE IF NOT EXISTS beneficioJuveplus
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE beneficioJuveplus;

-- ============================================================================
-- TABLAS DE APOYO (Lealtad / Niveles)
-- ============================================================================

CREATE TABLE nivel (
  nivelId        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombreNivel    VARCHAR(40) NOT NULL,
  puntosMinimos  INT UNSIGNED NOT NULL DEFAULT 0,
  beneficios     TEXT,
  prioridad      INT UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (nivelId),
  UNIQUE KEY uqNivelNombre (nombreNivel)
) ENGINE=InnoDB;

-- ============================================================================
-- USUARIOS
-- ============================================================================

CREATE TABLE usuario (
  usuarioId         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombreCompleto    VARCHAR(150) NOT NULL,
  fechaNacimiento   DATE NOT NULL,
  curp              VARCHAR(18),
  direccion         VARCHAR(255),
  codigoPostal      VARCHAR(10),
  telefono          VARCHAR(20),
  correoElectronico VARCHAR(160) NOT NULL,
  contrasenaHash    VARCHAR(255) NOT NULL,
  fechaRegistro     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  nivelId           BIGINT UNSIGNED,
  puntos            INT UNSIGNED NOT NULL DEFAULT 0,
  qrUsuario         VARCHAR(100) NOT NULL,
  estadoCuenta      ENUM('activo','suspendido','eliminado') NOT NULL DEFAULT 'activo',
  fotoPerfil        VARCHAR(255),
  verificado        TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (usuarioId),
  UNIQUE KEY uqUsuarioCorreo (correoElectronico),
  UNIQUE KEY uqUsuarioQr (qrUsuario),
  KEY idxUsuarioCp (codigoPostal),
  CONSTRAINT fkUsuarioNivel
    FOREIGN KEY (nivelId) REFERENCES nivel(nivelId)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

-- Documentos de verificación (INE, CURP, comprobante)
CREATE TABLE documentoVerif (
  docId         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  usuarioId     BIGINT UNSIGNED NOT NULL,
  tipo          ENUM('INE','CURP','COMPROBANTE') NOT NULL,
  archivoUrl    VARCHAR(255) NOT NULL,
  fechaCarga    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  estado        ENUM('pendiente','aprobado','rechazado') NOT NULL DEFAULT 'pendiente',
  motivoRechazo VARCHAR(255),
  PRIMARY KEY (docId),
  KEY idxDocUsuario (usuarioId, estado),
  CONSTRAINT fkDocUsuario
    FOREIGN KEY (usuarioId) REFERENCES usuario(usuarioId)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- Migración desde tarjeta física
CREATE TABLE tarjetaFisicaMigra (
  migracionId     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  usuarioId       BIGINT UNSIGNED NOT NULL,
  numeroTarjeta   VARCHAR(50) NOT NULL,
  fechaMigracion  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  puntosBono      INT UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (migracionId),
  UNIQUE KEY uqNumeroTarjeta (numeroTarjeta),
  KEY idxMigraUsuario (usuarioId),
  CONSTRAINT fkMigraUsuario
    FOREIGN KEY (usuarioId) REFERENCES usuario(usuarioId)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================================
-- COLABORADORES (NEGOCIOS)
-- ============================================================================

CREATE TABLE colaborador (
  colaboradorId     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombreNegocio     VARCHAR(160) NOT NULL,
  rfc               VARCHAR(20),
  representanteNombre VARCHAR(120),
  telefono          VARCHAR(20),
  correo            VARCHAR(160),
  direccion         VARCHAR(255),
  codigoPostal      VARCHAR(10),
  categoria         VARCHAR(60),
  logoUrl           VARCHAR(255),
  descripcion       TEXT,
  fechaRegistro     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  estado            ENUM('pendiente','activo','inactivo') NOT NULL DEFAULT 'pendiente',
  usuarioAdminId    BIGINT UNSIGNED,
  lat               DECIMAL(10,7),
  lng               DECIMAL(10,7),
  PRIMARY KEY (colaboradorId),
  KEY idxColCategoria (categoria),
  KEY idxColCp (codigoPostal),
  CONSTRAINT fkColabUsuarioAdmin
    FOREIGN KEY (usuarioAdminId) REFERENCES usuario(usuarioId)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

-- ============================================================================
-- PROMOCIONES / CUPONES
-- ============================================================================

CREATE TABLE promocion (
  promocionId      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  colaboradorId    BIGINT UNSIGNED NOT NULL,
  titulo           VARCHAR(140) NOT NULL,
  descripcion      TEXT,
  imagenUrl        VARCHAR(255),
  fechaInicio      DATE NOT NULL,
  fechaFin         DATE NOT NULL,
  categoria        VARCHAR(60),
  descuento        DECIMAL(10,2) NOT NULL,        -- define en app si es % o monto
  stockTotal       INT UNSIGNED NOT NULL DEFAULT 0,
  stockDisponible  INT UNSIGNED NOT NULL DEFAULT 0,
  nivelMinimo      BIGINT UNSIGNED,
  estado           ENUM('activa','inactiva','agotada') NOT NULL DEFAULT 'activa',
  requiereQr       TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (promocionId),
  KEY idxPromocionVigencia (fechaInicio, fechaFin),
  KEY idxPromocionCategoria (categoria),
  CONSTRAINT fkPromoColab
    FOREIGN KEY (colaboradorId) REFERENCES colaborador(colaboradorId)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fkPromoNivelMin
    FOREIGN KEY (nivelMinimo) REFERENCES nivel(nivelId)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

-- ============================================================================
-- RESERVAS
-- ============================================================================

CREATE TABLE reserva (
  reservaId        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  usuarioId        BIGINT UNSIGNED NOT NULL,
  promocionId      BIGINT UNSIGNED NOT NULL,
  fechaReserva     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fechaLimiteUso   TIMESTAMP NULL,
  estado           ENUM('reservado','usado','liberado','expirado') NOT NULL DEFAULT 'reservado',
  qrCupon          VARCHAR(120) NOT NULL,
  PRIMARY KEY (reservaId),
  UNIQUE KEY uqReservaQr (qrCupon),
  KEY idxReservaUsuarioEstado (usuarioId, estado),
  KEY idxReservaPromocion (promocionId),
  CONSTRAINT fkReservaUsuario
    FOREIGN KEY (usuarioId) REFERENCES usuario(usuarioId)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fkReservaPromocion
    FOREIGN KEY (promocionId) REFERENCES promocion(promocionId)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================================
-- USO / REDENCIÓN
-- ============================================================================

CREATE TABLE usoCupon (
  usoId            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  usuarioId        BIGINT UNSIGNED NOT NULL,
  colaboradorId    BIGINT UNSIGNED NOT NULL,
  promocionId      BIGINT UNSIGNED NOT NULL,
  fechaUso         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  montoAhorrado    DECIMAL(12,2) NOT NULL DEFAULT 0,
  puntosOtorgados  INT UNSIGNED NOT NULL DEFAULT 0,
  metodoValidacion ENUM('QR','CODIGO','POS') NOT NULL DEFAULT 'QR',
  PRIMARY KEY (usoId),
  KEY idxUsoFechas (fechaUso),
  KEY idxUsoSegmentacion (colaboradorId, promocionId),
  CONSTRAINT fkUsoUsuario
    FOREIGN KEY (usuarioId) REFERENCES usuario(usuarioId)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fkUsoColab
    FOREIGN KEY (colaboradorId) REFERENCES colaborador(colaboradorId)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fkUsoPromocion
    FOREIGN KEY (promocionId) REFERENCES promocion(promocionId)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================================
-- NOTIFICACIONES (con segmentación JSON)
-- ============================================================================

CREATE TABLE notificacion (
  notificacionId     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  titulo             VARCHAR(140) NOT NULL,
  mensaje            TEXT NOT NULL,
  tipo               ENUM('promocion','sistema','recordatorio') NOT NULL,
  fechaEnvio         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  destinatarioTipo   ENUM('usuario','colaborador','todos','segmento') NOT NULL,
  destinatarioId     BIGINT UNSIGNED NULL,
  estado             ENUM('pendiente','enviada','error') NOT NULL DEFAULT 'pendiente',
  criteriosSegmento  JSON NULL,
  PRIMARY KEY (notificacionId),
  KEY idxNotifTipoEstado (tipo, estado)
) ENGINE=InnoDB;

-- ============================================================================
-- ADMINISTRADORES
-- ============================================================================

CREATE TABLE administrador (
  adminId        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombre         VARCHAR(120) NOT NULL,
  correo         VARCHAR(160) NOT NULL,
  telefono       VARCHAR(20),
  contrasenaHash VARCHAR(255) NOT NULL,
  rol            ENUM('superadmin','soporte','marketing') NOT NULL DEFAULT 'soporte',
  estado         ENUM('activo','inactivo') NOT NULL DEFAULT 'activo',
  PRIMARY KEY (adminId),
  UNIQUE KEY uqAdminCorreo (correo)
) ENGINE=InnoDB;

-- ============================================================================
-- REPORTES (resultado resumido + URL a export)
-- ============================================================================

CREATE TABLE reporte (
  reporteId        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  tipoReporte      VARCHAR(80) NOT NULL,
  fechaGeneracion  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  parametros       JSON NOT NULL,
  resultadoResumen JSON NULL,
  archivoUrl       VARCHAR(255),
  PRIMARY KEY (reporteId),
  KEY idxReporteTipoFecha (tipoReporte, fechaGeneracion)
) ENGINE=InnoDB;

-- ============================================================================
-- TRIGGERS / REGLAS DE NEGOCIO
-- ============================================================================

-- 1) Al insertar un uso: descuenta stock de la promoción y suma puntos al usuario.
DELIMITER $$

CREATE TRIGGER trgUsoAfterInsert
AFTER INSERT ON usoCupon
FOR EACH ROW
BEGIN
  -- Descontar stockDisponible (sin bajar de 0)
  UPDATE promocion
    SET stockDisponible = CASE
                    WHEN stockDisponible > 0 THEN stockDisponible - 1
                    ELSE 0
                   END,
      estado = CASE
              WHEN stockDisponible <= 1 THEN 'agotada'
              ELSE estado
            END
  WHERE promocionId = NEW.promocionId;

  -- Sumar puntos al usuario
  UPDATE usuario
    SET puntos = puntos + NEW.puntosOtorgados
  WHERE usuarioId = NEW.usuarioId;

  -- Recalcular nivel por puntos (elige el nivel con mayor puntosMinimos <= puntos)
  UPDATE usuario u
    JOIN (
        SELECT n.nivelId
        FROM nivel n
        WHERE n.puntosMinimos <= u.puntos
        ORDER BY n.puntosMinimos DESC
        LIMIT 1
       ) x
     SET u.nivelId = x.nivelId
  WHERE u.usuarioId = NEW.usuarioId;
END$$

-- 2) Al actualizar una reserva: si se libera o expira, reponer stock de la promoción.
CREATE TRIGGER trgReservaAfterUpdate
AFTER UPDATE ON reserva
FOR EACH ROW
BEGIN
  IF (OLD.estado IN ('reservado') AND NEW.estado IN ('liberado','expirado')) THEN
      UPDATE promocion
         SET stockDisponible = stockDisponible + 1,
             estado = 'activa'
       WHERE promocionId = NEW.promocionId
         AND stockDisponible < stockTotal;
  END IF;
END$$

DELIMITER ;

-- ============================================================================
-- EVENTO (opcional) para expirar reservas automáticamente al pasar fecha_limite_uso
-- Habilita el event scheduler: SET GLOBAL event_scheduler = ON;
-- ============================================================================

CREATE EVENT IF NOT EXISTS evExpiraReservas
ON SCHEDULE EVERY 5 MINUTE
DO
  UPDATE reserva
     SET estado = 'expirado'
   WHERE estado = 'reservado'
     AND fechaLimiteUso IS NOT NULL
     AND fechaLimiteUso < CURRENT_TIMESTAMP;

-- ============================================================================
-- VISTAS (agregados para métricas). MySQL no tiene materialized views nativas.
-- ============================================================================

CREATE OR REPLACE VIEW vUsoPorZona AS
SELECT
  c.codigoPostal,
  p.categoria,
  DATE_FORMAT(u.fechaUso, '%Y-%m-01') AS mes,
  COUNT(*) AS usos,
  SUM(u.montoAhorrado) AS ahorroTotal
FROM usoCupon u
JOIN usuario us   ON us.usuarioId = u.usuarioId
JOIN colaborador c ON c.colaboradorId = u.colaboradorId
JOIN promocion p   ON p.promocionId = u.promocionId
GROUP BY c.codigoPostal, p.categoria, DATE_FORMAT(u.fechaUso, '%Y-%m-01');

CREATE OR REPLACE VIEW vRfmUsuarios AS
SELECT
  u.usuarioId,
  MAX(uc.fechaUso) AS lastUse,
  COUNT(uc.usoId)  AS frequency,
  COALESCE(SUM(uc.montoAhorrado),0) AS monetary
FROM usuario u
LEFT JOIN usoCupon uc ON uc.usuarioId = u.usuarioId
GROUP BY u.usuarioId;

-- ============================================================================
-- ÍNDICES ADICIONALES RECOMENDADOS
-- ============================================================================

CREATE INDEX idxUsuarioFechaRegistro ON usuario(fechaRegistro);
CREATE INDEX idxPromocionEstado ON promocion(estado);
CREATE INDEX idxReservaEstadoFecha ON reserva(estado, fechaReserva);
CREATE INDEX idxUsoUsuarioFecha ON usoCupon(usuarioId, fechaUso);
