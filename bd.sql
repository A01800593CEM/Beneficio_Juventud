-- ============================================================================
-- Beneficio Juve+  (MySQL 8.0.21+)
-- Esquema con IDs INT UNSIGNED en todas las PK/FK
-- ============================================================================

/*---------------------------------------------------------------------------
  0) Configuración inicial de BD
---------------------------------------------------------------------------*/
CREATE DATABASE IF NOT EXISTS beneficioJuveplus
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE beneficioJuveplus;

-- Opcional:
-- SET sql_mode = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';


/*---------------------------------------------------------------------------
  1) USUARIOS
---------------------------------------------------------------------------*/
CREATE TABLE usuario (
  usuarioId         INT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombreCompleto    VARCHAR(150) NOT NULL,
  fechaNacimiento   DATE NOT NULL,
  telefono          VARCHAR(20),
  correoElectronico VARCHAR(160) NOT NULL,
  contrasenaHash    VARCHAR(255) NOT NULL,
  fechaRegistro     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updatedAt         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  qrUsuario         VARCHAR(100) NOT NULL,
  estadoCuenta      ENUM('activo','suspendido','eliminado') NOT NULL DEFAULT 'activo',
  fotoPerfil        VARCHAR(255),
  verificado        TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (usuarioId),
  UNIQUE KEY uqUsuarioCorreo (correoElectronico),
  UNIQUE KEY uqUsuarioQr (qrUsuario)
) ENGINE=InnoDB;


/*---------------------------------------------------------------------------
  2) COLABORADORES y SUCURSALES
---------------------------------------------------------------------------*/
CREATE TABLE colaborador (
  colaboradorId       INT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombreNegocio       VARCHAR(160) NOT NULL,
  rfc                 VARCHAR(20),
  representanteNombre VARCHAR(120),
  telefono            VARCHAR(20),
  correo              VARCHAR(160),
  direccion           VARCHAR(255),
  codigoPostal        VARCHAR(10),
  categoria           VARCHAR(60),
  logoUrl             VARCHAR(255),
  descripcion         TEXT,
  fechaRegistro       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updatedAt           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  estado              ENUM('pendiente','activo','inactivo') NOT NULL DEFAULT 'pendiente',
  usuarioAdminId      INT UNSIGNED,
  PRIMARY KEY (colaboradorId),
  KEY idxColCategoria (categoria),
  KEY idxColCp (codigoPostal),
  CONSTRAINT fkColabUsuarioAdmin
    FOREIGN KEY (usuarioAdminId) REFERENCES usuario(usuarioId)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE sucursal (
  sucursalId     INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  colaboradorId  INT UNSIGNED NOT NULL,
  nombre         VARCHAR(160) NOT NULL,
  telefono       VARCHAR(20),
  direccion      VARCHAR(255),
  codigoPostal   VARCHAR(10),
  ubicacion      POINT NOT NULL SRID 4326,
  horarioJson    JSON,
  estado         ENUM('activo','inactivo') NOT NULL DEFAULT 'activo',
  createdAt      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updatedAt      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  lat                 DECIMAL(10,7),
  lng                 DECIMAL(10,7),
  CONSTRAINT fkSucursalColab FOREIGN KEY (colaboradorId)
    REFERENCES colaborador(colaboradorId)
    ON UPDATE CASCADE ON DELETE CASCADE,
  SPATIAL INDEX idxSucursalUbicacion (ubicacion),
  KEY idxSucursalCp (codigoPostal)
) ENGINE=InnoDB;


/*---------------------------------------------------------------------------
  3) PROMOCIONES / CUPONES
---------------------------------------------------------------------------*/
CREATE TABLE promocion (
  promocionId       INT UNSIGNED NOT NULL AUTO_INCREMENT,
  colaboradorId     INT UNSIGNED NOT NULL,
  titulo            VARCHAR(140) NOT NULL,
  descripcion       TEXT,
  imagenUrl         VARCHAR(255),
  fechaInicio       DATE NOT NULL,
  fechaFin          DATE NOT NULL,
  categoria         VARCHAR(60),
  tipoDescuento     ENUM('PORCENTAJE','MONTO') NOT NULL DEFAULT 'PORCENTAJE',
  descuento         DECIMAL(10,2) NOT NULL,
  stockTotal        INT UNSIGNED NOT NULL DEFAULT 0,
  stockDisponible   INT UNSIGNED NOT NULL DEFAULT 0,
  limitePorUsuario        INT UNSIGNED NOT NULL DEFAULT 1,
  limiteDiarioPorUsuario  INT UNSIGNED NOT NULL DEFAULT 1,
  estado            ENUM('activa','inactiva','agotada') NOT NULL DEFAULT 'activa',
  requiereQr        TINYINT(1) NOT NULL DEFAULT 1,
  createdAt         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updatedAt         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (promocionId),
  KEY idxPromocionVigencia (fechaInicio, fechaFin),
  KEY idxPromocionCategoria (categoria),
  CONSTRAINT fkPromoColab
    FOREIGN KEY (colaboradorId) REFERENCES colaborador(colaboradorId)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CHECK (fechaFin >= fechaInicio),
  CHECK (descuento >= 0),
  CHECK (
    (tipoDescuento = 'MONTO') OR
    (tipoDescuento = 'PORCENTAJE' AND descuento <= 100)
  ),
  CHECK (stockDisponible <= stockTotal)
) ENGINE=InnoDB;


/*---------------------------------------------------------------------------
  4) RESERVAS
---------------------------------------------------------------------------*/
CREATE TABLE reserva (
  reservaId          INT UNSIGNED NOT NULL AUTO_INCREMENT,
  usuarioId          INT UNSIGNED NOT NULL,
  promocionId        INT UNSIGNED NOT NULL,
  fechaReserva       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fechaLimiteUso     TIMESTAMP NULL,
  estado             ENUM('reservado','usado','liberado','expirado') NOT NULL DEFAULT 'reservado',
  qrCupon            VARCHAR(120) NOT NULL,
  promocionReservada INT UNSIGNED GENERATED ALWAYS AS (
    CASE WHEN estado = 'reservado' THEN promocionId END
  ) STORED,
  PRIMARY KEY (reservaId),
  UNIQUE KEY uqReservaQr (qrCupon),
  UNIQUE KEY uqReservaActiva (usuarioId, promocionReservada),
  KEY idxReservaUsuarioEstado (usuarioId, estado),
  KEY idxReservaPromocion (promocionId),
  CONSTRAINT fkReservaUsuario
    FOREIGN KEY (usuarioId) REFERENCES usuario(usuarioId)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fkReservaPromocion
    FOREIGN KEY (promocionId) REFERENCES promocion(promocionId)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CHECK (fechaLimiteUso IS NULL OR fechaLimiteUso >= fechaReserva)
) ENGINE=InnoDB;


/*---------------------------------------------------------------------------
  5) USO / REDENCIÓN
---------------------------------------------------------------------------*/
CREATE TABLE usoCupon (
  usoId            INT UNSIGNED NOT NULL AUTO_INCREMENT,
  usuarioId        INT UNSIGNED NOT NULL,
  colaboradorId    INT UNSIGNED NOT NULL,
  sucursalId       INT UNSIGNED NULL,
  promocionId      INT UNSIGNED NOT NULL,
  fechaUso         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  montoAhorrado    DECIMAL(12,2) NOT NULL DEFAULT 0,
  metodoValidacion ENUM('QR','CODIGO','POS') NOT NULL DEFAULT 'QR',
  PRIMARY KEY (usoId),
  KEY idxUsoFechas (fechaUso),
  KEY idxUsoSegmentacion (colaboradorId, promocionId),
  UNIQUE KEY uqUsoUnico (usuarioId, promocionId),
  CONSTRAINT fkUsoUsuario   FOREIGN KEY (usuarioId)     REFERENCES usuario(usuarioId)       ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fkUsoColab     FOREIGN KEY (colaboradorId) REFERENCES colaborador(colaboradorId) ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fkUsoSucursal  FOREIGN KEY (sucursalId)    REFERENCES sucursal(sucursalId)     ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT fkUsoPromocion FOREIGN KEY (promocionId)   REFERENCES promocion(promocionId)   ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;


/*---------------------------------------------------------------------------
  6) FAVORITOS
---------------------------------------------------------------------------*/
CREATE TABLE favorito (
  usuarioId     INT UNSIGNED NOT NULL,
  colaboradorId INT UNSIGNED NOT NULL,
  createdAt     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (usuarioId, colaboradorId),
  CONSTRAINT fkFavUsuario FOREIGN KEY (usuarioId)     REFERENCES usuario(usuarioId)       ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fkFavColab   FOREIGN KEY (colaboradorId) REFERENCES colaborador(colaboradorId) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;


/*---------------------------------------------------------------------------
  7) NOTIFICACIONES
---------------------------------------------------------------------------*/
CREATE TABLE notificacion (
  notificacionId     INT UNSIGNED NOT NULL AUTO_INCREMENT,
  titulo             VARCHAR(140) NOT NULL,
  mensaje            TEXT NOT NULL,
  tipo               ENUM('promocion','sistema','recordatorio') NOT NULL,
  fechaEnvio         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  destinatarioTipo   ENUM('usuario','colaborador','todos','segmento') NOT NULL,
  destinatarioId     INT UNSIGNED NULL,
  estado             ENUM('pendiente','enviada','error') NOT NULL DEFAULT 'pendiente',
  criteriosSegmento  JSON NULL,
  PRIMARY KEY (notificacionId),
  KEY idxNotifTipoEstado (tipo, estado)
) ENGINE=InnoDB;


/*---------------------------------------------------------------------------
  8) ADMINISTRADORES
---------------------------------------------------------------------------*/
CREATE TABLE administrador (
  adminId        INT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombre         VARCHAR(120) NOT NULL,
  correo         VARCHAR(160) NOT NULL,
  telefono       VARCHAR(20),
  contrasenaHash VARCHAR(255) NOT NULL,
  rol            ENUM('superadmin','soporte','marketing') NOT NULL DEFAULT 'soporte',
  estado         ENUM('activo','inactivo') NOT NULL DEFAULT 'activo',
  createdAt      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updatedAt      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (adminId),
  UNIQUE KEY uqAdminCorreo (correo)
) ENGINE=InnoDB;


/*---------------------------------------------------------------------------
  9) REPORTES
---------------------------------------------------------------------------*/
CREATE TABLE reporte (
  reporteId        INT UNSIGNED NOT NULL AUTO_INCREMENT,
  tipoReporte      VARCHAR(80) NOT NULL,
  fechaGeneracion  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  parametros       JSON NOT NULL,
  resultadoResumen JSON NULL,
  archivoUrl       VARCHAR(255),
  PRIMARY KEY (reporteId),
  KEY idxReporteTipoFecha (tipoReporte, fechaGeneracion)
) ENGINE=InnoDB;


/*---------------------------------------------------------------------------
  10) TRIGGERS
---------------------------------------------------------------------------*/
DELIMITER $$

CREATE TRIGGER trgUsoAfterInsert
AFTER INSERT ON usoCupon
FOR EACH ROW
BEGIN
  UPDATE promocion
     SET stockDisponible = CASE WHEN stockDisponible > 0 THEN stockDisponible - 1 ELSE 0 END,
         estado = CASE WHEN stockDisponible <= 1 THEN 'agotada' ELSE estado END
   WHERE promocionId = NEW.promocionId;
END$$

CREATE TRIGGER trgReservaAfterUpdate
AFTER UPDATE ON reserva
FOR EACH ROW
BEGIN
  IF (OLD.estado = 'reservado' AND NEW.estado IN ('liberado','expirado')) THEN
    UPDATE promocion
       SET stockDisponible = LEAST(stockTotal, stockDisponible + 1),
           estado = 'activa'
     WHERE promocionId = NEW.promocionId;
  END IF;
END$$

DELIMITER ;


/*---------------------------------------------------------------------------
  11) EVENTOS (Scheduler)
---------------------------------------------------------------------------*/
CREATE EVENT IF NOT EXISTS evExpiraReservas
ON SCHEDULE EVERY 5 MINUTE
DO
  UPDATE reserva
     SET estado = 'expirado'
   WHERE estado = 'reservado'
     AND fechaLimiteUso IS NOT NULL
     AND fechaLimiteUso < CURRENT_TIMESTAMP;


/*---------------------------------------------------------------------------
  12) VISTAS
---------------------------------------------------------------------------*/
CREATE OR REPLACE VIEW vUsoPorZona AS
SELECT
  c.codigoPostal,
  p.categoria,
  DATE_FORMAT(u.fechaUso, '%Y-%m-01') AS mes,
  COUNT(*) AS usos,
  SUM(u.montoAhorrado) AS ahorroTotal
FROM usoCupon u
JOIN usuario us     ON us.usuarioId = u.usuarioId
JOIN colaborador c  ON c.colaboradorId = u.colaboradorId
JOIN promocion p    ON p.promocionId = u.promocionId
GROUP BY c.codigoPostal, p.categoria, DATE_FORMAT(u.fechaUso, '%Y-%m-01');

CREATE OR REPLACE VIEW vRfmUsuarios AS
SELECT
  u.usuarioId,
  MAX(uc.fechaUso)                    AS lastUse,
  COUNT(uc.usoId)                     AS frequency,
  COALESCE(SUM(uc.montoAhorrado), 0)  AS monetary
FROM usuario u
LEFT JOIN usoCupon uc ON uc.usuarioId = u.usuarioId
GROUP BY u.usuarioId;


/*---------------------------------------------------------------------------
  13) ÍNDICES adicionales (FULLTEXT y por estado/fechas)
---------------------------------------------------------------------------*/
CREATE FULLTEXT INDEX ftColaboradorNombreDesc ON colaborador (nombreNegocio, descripcion);
CREATE FULLTEXT INDEX ftPromocionTituloDesc   ON promocion   (titulo, descripcion);

CREATE INDEX idxUsuarioFechaRegistro ON usuario(fechaRegistro);
CREATE INDEX idxPromocionEstado      ON promocion(estado);
CREATE INDEX idxReservaEstadoFecha   ON reserva(estado, fechaReserva);
CREATE INDEX idxUsoUsuarioFecha      ON usoCupon(usuarioId, fechaUso);
