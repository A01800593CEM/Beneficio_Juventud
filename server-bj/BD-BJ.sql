--
-- PostgreSQL database dump
--

-- Dumped from database version 17.5 (6bc9ef8)
-- Dumped by pg_dump version 17.5

-- Started on 2025-10-14 11:53:23 CST

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 3518 (class 1262 OID 16391)
-- Name: neondb; Type: DATABASE; Schema: -; Owner: neondb_owner
--

CREATE DATABASE neondb WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = builtin LOCALE = 'C.UTF-8' BUILTIN_LOCALE = 'C.UTF-8';


ALTER DATABASE neondb OWNER TO neondb_owner;

\connect neondb

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 5 (class 2615 OID 74010)
-- Name: public; Type: SCHEMA; Schema: -; Owner: neondb_owner
--

CREATE SCHEMA public;


ALTER SCHEMA public OWNER TO neondb_owner;

--
-- TOC entry 891 (class 1247 OID 74074)
-- Name: destinatario_tipo; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.destinatario_tipo AS ENUM (
    'usuario',
    'colaborador',
    'admin'
);


ALTER TYPE public.destinatario_tipo OWNER TO neondb_owner;

--
-- TOC entry 873 (class 1247 OID 74026)
-- Name: estado_colaborador; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.estado_colaborador AS ENUM (
    'activo',
    'inactivo',
    'suspendido'
);


ALTER TYPE public.estado_colaborador OWNER TO neondb_owner;

--
-- TOC entry 867 (class 1247 OID 74012)
-- Name: estado_cuenta; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.estado_cuenta AS ENUM (
    'activo',
    'inactivo',
    'suspendido'
);


ALTER TYPE public.estado_cuenta OWNER TO neondb_owner;

--
-- TOC entry 894 (class 1247 OID 74082)
-- Name: estado_notificacion; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.estado_notificacion AS ENUM (
    'enviada',
    'pendiente',
    'leida'
);


ALTER TYPE public.estado_notificacion OWNER TO neondb_owner;

--
-- TOC entry 882 (class 1247 OID 74050)
-- Name: estado_promocion; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.estado_promocion AS ENUM (
    'activa',
    'inactiva',
    'finalizada'
);


ALTER TYPE public.estado_promocion OWNER TO neondb_owner;

--
-- TOC entry 885 (class 1247 OID 74058)
-- Name: estado_reserva; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.estado_reserva AS ENUM (
    'pendiente',
    'usada',
    'cancelada'
);


ALTER TYPE public.estado_reserva OWNER TO neondb_owner;

--
-- TOC entry 876 (class 1247 OID 74034)
-- Name: estado_sucursal; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.estado_sucursal AS ENUM (
    'activa',
    'inactiva'
);


ALTER TYPE public.estado_sucursal OWNER TO neondb_owner;

--
-- TOC entry 870 (class 1247 OID 74020)
-- Name: rol_admin; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.rol_admin AS ENUM (
    'superadmin',
    'admin'
);


ALTER TYPE public.rol_admin OWNER TO neondb_owner;

--
-- TOC entry 936 (class 1247 OID 81921)
-- Name: tema; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.tema AS ENUM (
    'light',
    'dark'
);


ALTER TYPE public.tema OWNER TO neondb_owner;

--
-- TOC entry 888 (class 1247 OID 74066)
-- Name: tipo_notificacion; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.tipo_notificacion AS ENUM (
    'info',
    'promo',
    'alerta'
);


ALTER TYPE public.tipo_notificacion OWNER TO neondb_owner;

--
-- TOC entry 879 (class 1247 OID 74040)
-- Name: tipo_promocion; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.tipo_promocion AS ENUM (
    'descuento',
    'multicompra',
    'regalo',
    'otro'
);


ALTER TYPE public.tipo_promocion OWNER TO neondb_owner;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 224 (class 1259 OID 74123)
-- Name: administrador; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.administrador (
    admin_id integer NOT NULL,
    cognito_id character varying(255),
    nombre character varying(255),
    apellido_paterno character varying(255),
    apellido_materno character varying(255),
    correo character varying(255),
    telefono character varying(20),
    rol public.rol_admin,
    estado public.estado_colaborador,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.administrador OWNER TO neondb_owner;

--
-- TOC entry 223 (class 1259 OID 74122)
-- Name: administrador_admin_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.administrador_admin_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.administrador_admin_id_seq OWNER TO neondb_owner;

--
-- TOC entry 3521 (class 0 OID 0)
-- Dependencies: 223
-- Name: administrador_admin_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.administrador_admin_id_seq OWNED BY public.administrador.admin_id;


--
-- TOC entry 220 (class 1259 OID 74103)
-- Name: categoria; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.categoria (
    categoria_id integer NOT NULL,
    nombre character varying(255)
);


ALTER TABLE public.categoria OWNER TO neondb_owner;

--
-- TOC entry 219 (class 1259 OID 74102)
-- Name: categoria_categoria_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.categoria_categoria_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.categoria_categoria_id_seq OWNER TO neondb_owner;

--
-- TOC entry 3522 (class 0 OID 0)
-- Dependencies: 219
-- Name: categoria_categoria_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.categoria_categoria_id_seq OWNED BY public.categoria.categoria_id;


--
-- TOC entry 222 (class 1259 OID 74110)
-- Name: colaborador; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.colaborador (
    colaborador_id integer NOT NULL,
    cognito_id character varying(255),
    nombre_negocio character varying(255),
    rfc character varying(20),
    representante_nombre character varying(255),
    telefono character varying(20),
    correo character varying(255),
    direccion character varying(255),
    codigo_postal character varying(10),
    logo_url character varying(255),
    descripcion text,
    fecha_registro timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    estado public.estado_colaborador
);


ALTER TABLE public.colaborador OWNER TO neondb_owner;

--
-- TOC entry 226 (class 1259 OID 74150)
-- Name: colaborador_categoria; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.colaborador_categoria (
    colaborador_id character varying(255) NOT NULL,
    categoria_id integer NOT NULL
);


ALTER TABLE public.colaborador_categoria OWNER TO neondb_owner;

--
-- TOC entry 221 (class 1259 OID 74109)
-- Name: colaborador_colaborador_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.colaborador_colaborador_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.colaborador_colaborador_id_seq OWNER TO neondb_owner;

--
-- TOC entry 3523 (class 0 OID 0)
-- Dependencies: 221
-- Name: colaborador_colaborador_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.colaborador_colaborador_id_seq OWNED BY public.colaborador.colaborador_id;


--
-- TOC entry 236 (class 1259 OID 74260)
-- Name: favorito; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.favorito (
    usuario_id character varying(255) NOT NULL,
    colaborador_id character varying(255) NOT NULL,
    fecha_agregado timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.favorito OWNER TO neondb_owner;

--
-- TOC entry 238 (class 1259 OID 74279)
-- Name: notificacion; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.notificacion (
    notificacion_id integer NOT NULL,
    titulo character varying(255),
    mensaje text,
    tipo public.tipo_notificacion,
    fecha_envio timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    destinatario_tipo public.destinatario_tipo,
    destinatario_id character varying(255),
    estado public.estado_notificacion,
    criterios_segmento json,
    promocion_id integer
);


ALTER TABLE public.notificacion OWNER TO neondb_owner;

--
-- TOC entry 237 (class 1259 OID 74278)
-- Name: notificacion_notificacion_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.notificacion_notificacion_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.notificacion_notificacion_id_seq OWNER TO neondb_owner;

--
-- TOC entry 3524 (class 0 OID 0)
-- Dependencies: 237
-- Name: notificacion_notificacion_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.notificacion_notificacion_id_seq OWNED BY public.notificacion.notificacion_id;


--
-- TOC entry 230 (class 1259 OID 74182)
-- Name: promocion; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.promocion (
    promocion_id integer NOT NULL,
    colaborador_id character varying(255),
    titulo character varying(255),
    descripcion text,
    imagen_url character varying(255),
    fecha_inicio date,
    fecha_fin date,
    tipo_promocion public.tipo_promocion,
    promocion_string character varying(255),
    stock_total integer,
    stock_disponible integer,
    limite_por_usuario integer,
    limite_diario_por_usuario integer,
    estado public.estado_promocion,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    theme public.tema
);


ALTER TABLE public.promocion OWNER TO neondb_owner;

--
-- TOC entry 231 (class 1259 OID 74197)
-- Name: promocion_categoria; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.promocion_categoria (
    promocion_id integer NOT NULL,
    categoria_id integer NOT NULL
);


ALTER TABLE public.promocion_categoria OWNER TO neondb_owner;

--
-- Name: promocion_sucursal; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.promocion_sucursal (
    promocion_id integer NOT NULL,
    sucursal_id integer NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.promocion_sucursal OWNER TO neondb_owner;

--
-- TOC entry 229 (class 1259 OID 74181)
-- Name: promocion_promocion_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.promocion_promocion_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.promocion_promocion_id_seq OWNER TO neondb_owner;

--
-- TOC entry 3525 (class 0 OID 0)
-- Dependencies: 229
-- Name: promocion_promocion_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.promocion_promocion_id_seq OWNED BY public.promocion.promocion_id;


--
-- TOC entry 233 (class 1259 OID 74213)
-- Name: reserva; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.reserva (
    reserva_id integer NOT NULL,
    usuario_id character varying(255),
    promocion_id integer,
    fecha_reserva timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    fecha_limite_uso timestamp without time zone,
    estado public.estado_reserva,
    promocion_reservada integer
);


ALTER TABLE public.reserva OWNER TO neondb_owner;

--
-- TOC entry 232 (class 1259 OID 74212)
-- Name: reserva_reserva_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.reserva_reserva_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.reserva_reserva_id_seq OWNER TO neondb_owner;

--
-- TOC entry 3526 (class 0 OID 0)
-- Dependencies: 232
-- Name: reserva_reserva_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.reserva_reserva_id_seq OWNED BY public.reserva.reserva_id;


--
-- TOC entry 228 (class 1259 OID 74166)
-- Name: sucursal; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.sucursal (
    sucursal_id integer NOT NULL,
    colaborador_id character varying(255),
    nombre character varying(255),
    telefono character varying(20),
    direccion character varying(255),
    codigo_postal character varying(10),
    ubicacion point,
    horario_json json,
    estado public.estado_sucursal,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.sucursal OWNER TO neondb_owner;

--
-- TOC entry 227 (class 1259 OID 74165)
-- Name: sucursal_sucursal_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.sucursal_sucursal_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sucursal_sucursal_id_seq OWNER TO neondb_owner;

--
-- TOC entry 3527 (class 0 OID 0)
-- Dependencies: 227
-- Name: sucursal_sucursal_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.sucursal_sucursal_id_seq OWNED BY public.sucursal.sucursal_id;


--
-- TOC entry 235 (class 1259 OID 74231)
-- Name: uso_cupon; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.uso_cupon (
    uso_id integer NOT NULL,
    usuario_id character varying(255),
    colaborador_id character varying(255),
    sucursal_id integer,
    promocion_id integer,
    fecha_uso timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.uso_cupon OWNER TO neondb_owner;

--
-- TOC entry 234 (class 1259 OID 74230)
-- Name: uso_cupon_uso_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.uso_cupon_uso_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.uso_cupon_uso_id_seq OWNER TO neondb_owner;

--
-- TOC entry 3528 (class 0 OID 0)
-- Dependencies: 234
-- Name: uso_cupon_uso_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.uso_cupon_uso_id_seq OWNED BY public.uso_cupon.uso_id;


--
-- TOC entry 218 (class 1259 OID 74090)
-- Name: usuario; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.usuario (
    usuario_id integer NOT NULL,
    cognito_id character varying(255),
    nombre character varying(255),
    apellido_paterno character varying(255),
    apellido_materno character varying(255),
    fecha_nacimiento date,
    telefono character varying(20),
    correo_electronico character varying(255),
    fecha_registro timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    estado_cuenta public.estado_cuenta,
    token_notificacion character varying(255)
);


ALTER TABLE public.usuario OWNER TO neondb_owner;

--
-- TOC entry 225 (class 1259 OID 74135)
-- Name: usuario_categoria; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.usuario_categoria (
    usuario_id character varying(255) NOT NULL,
    categoria_id integer NOT NULL
);


ALTER TABLE public.usuario_categoria OWNER TO neondb_owner;

--
-- TOC entry 217 (class 1259 OID 74089)
-- Name: usuario_usuario_id_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.usuario_usuario_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.usuario_usuario_id_seq OWNER TO neondb_owner;

--
-- TOC entry 3529 (class 0 OID 0)
-- Dependencies: 217
-- Name: usuario_usuario_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.usuario_usuario_id_seq OWNED BY public.usuario.usuario_id;


--
-- TOC entry 3281 (class 2604 OID 74126)
-- Name: administrador admin_id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.administrador ALTER COLUMN admin_id SET DEFAULT nextval('public.administrador_admin_id_seq'::regclass);


--
-- TOC entry 3277 (class 2604 OID 74106)
-- Name: categoria categoria_id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.categoria ALTER COLUMN categoria_id SET DEFAULT nextval('public.categoria_categoria_id_seq'::regclass);


--
-- TOC entry 3278 (class 2604 OID 74113)
-- Name: colaborador colaborador_id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.colaborador ALTER COLUMN colaborador_id SET DEFAULT nextval('public.colaborador_colaborador_id_seq'::regclass);


--
-- TOC entry 3295 (class 2604 OID 74282)
-- Name: notificacion notificacion_id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.notificacion ALTER COLUMN notificacion_id SET DEFAULT nextval('public.notificacion_notificacion_id_seq'::regclass);


--
-- TOC entry 3287 (class 2604 OID 74185)
-- Name: promocion promocion_id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.promocion ALTER COLUMN promocion_id SET DEFAULT nextval('public.promocion_promocion_id_seq'::regclass);


--
-- TOC entry 3290 (class 2604 OID 74216)
-- Name: reserva reserva_id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.reserva ALTER COLUMN reserva_id SET DEFAULT nextval('public.reserva_reserva_id_seq'::regclass);


--
-- TOC entry 3284 (class 2604 OID 74169)
-- Name: sucursal sucursal_id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.sucursal ALTER COLUMN sucursal_id SET DEFAULT nextval('public.sucursal_sucursal_id_seq'::regclass);


--
-- TOC entry 3292 (class 2604 OID 74234)
-- Name: uso_cupon uso_id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.uso_cupon ALTER COLUMN uso_id SET DEFAULT nextval('public.uso_cupon_uso_id_seq'::regclass);


--
-- TOC entry 3274 (class 2604 OID 74093)
-- Name: usuario usuario_id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.usuario ALTER COLUMN usuario_id SET DEFAULT nextval('public.usuario_usuario_id_seq'::regclass);


--
-- TOC entry 3498 (class 0 OID 74123)
-- Dependencies: 224
-- Data for Name: administrador; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--



--
-- TOC entry 3494 (class 0 OID 74103)
-- Dependencies: 220
-- Data for Name: categoria; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

INSERT INTO public.categoria (categoria_id, nombre) VALUES (1, 'ENTRETENIMIENTO');
INSERT INTO public.categoria (categoria_id, nombre) VALUES (2, 'COMIDA');
INSERT INTO public.categoria (categoria_id, nombre) VALUES (3, 'ROPA');


--
-- TOC entry 3496 (class 0 OID 74110)
-- Dependencies: 222
-- Data for Name: colaborador; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

INSERT INTO public.colaborador (colaborador_id, cognito_id, nombre_negocio, rfc, representante_nombre, telefono, correo, direccion, codigo_postal, logo_url, descripcion, fecha_registro, updated_at, estado) VALUES (3, 'us-east-1:c4f5b6e7-1d2c-3b4a-5e6f-7a8b9c0d1e2f', 'El Fuego Sagrado', 'FSA180521ABC', 'Alejandro Vega', '5512345678', 'contacto@elfuegosagrado.com', 'Av. de los Jinetes 145, Las Arboledas, Atizapán de Zaragoza, Méx.', '52950', 'https://example.com/logos/elfuegosagrado.png', 'Asador de alta calidad que rinde tributo a los mejores cortes de carne nacionales e importados. En un ambiente rústico y acogedor, cada platillo es preparado a la perfección en nuestra parrilla de leña, garantizando una experiencia de sabor inolvidable.', '2025-10-13 18:10:01.154129', '2025-10-13 18:10:01.154129', 'activo');
INSERT INTO public.colaborador (colaborador_id, cognito_id, nombre_negocio, rfc, representante_nombre, telefono, correo, direccion, codigo_postal, logo_url, descripcion, fecha_registro, updated_at, estado) VALUES (6, 'us-east-1:d5f6b7e8-2d3c-4b5a-6e7f-8a9b0c1d2e3f', 'Marea Alta', 'MAL201103DEF', 'Alan Tomás', '5587654321', 'contacto@mareaalta.mx', 'Blvd. Adolfo López Mateos 201, Jardines de Atizapán, Atizapán de Zaragoza, Méx.', '52978', 'https://example.com/logos/mareaalta.png', 'La frescura del mar directamente a tu mesa. Somos un restaurante de mariscos estilo costa del Pacífico, con un toque gourmet. Disfruta de nuestros ceviches, aguachiles y tacos en un ambiente relajado y familiar.', '2025-10-13 18:36:12.768719', '2025-10-13 18:36:12.768719', 'activo');
INSERT INTO public.colaborador (colaborador_id, cognito_id, nombre_negocio, rfc, representante_nombre, telefono, correo, direccion, codigo_postal, logo_url, descripcion, fecha_registro, updated_at, estado) VALUES (7, 'us-east-1:e6f7b8e9-3d4c-5b6a-7e8f-9a0b1c2d3e4f', 'Origo Café', 'OCF190915GHI', 'Isaac Abud', '5524681357', 'contacto@origocafe.com', 'Calz. de los Gigantes 33, Real de Atizapán, Atizapán de Zaragoza, Méx.', '52947', 'https://example.com/logos/origocafe.png', 'Un espacio para los amantes del buen café. Trabajamos con granos de productores mexicanos y métodos de extracción artesanales. Acompaña tu bebida con nuestra deliciosa repostería casera o un sándwich gourmet.', '2025-10-13 18:37:39.024148', '2025-10-13 18:37:39.024148', 'activo');
INSERT INTO public.colaborador (colaborador_id, cognito_id, nombre_negocio, rfc, representante_nombre, telefono, correo, direccion, codigo_postal, logo_url, descripcion, fecha_registro, updated_at, estado) VALUES (8, 'us-east-1:f7g8b9f0-4d5c-6b7a-8e9f-0a1b2c3d4e5f', 'La Vía Láctea', 'VLA210228JKL', 'Familia de León', '5513579246', 'contacto@lavialactea.burgers', 'Av. Lago de Guadalupe 5, San José del Jaral, Atizapán de Zaragoza, Méx.', '52924', 'https://example.com/logos/lavialactea.png', 'Hamburguesería con temática retro y espacial. Ofrecemos hamburguesas con carne 100% de res, bollos artesanales y las malteadas más cremosas de la galaxia. ¡Un viaje de sabor para toda la familia!', '2025-10-13 18:43:13.627689', '2025-10-13 18:43:13.627689', 'activo');
INSERT INTO public.colaborador (colaborador_id, cognito_id, nombre_negocio, rfc, representante_nombre, telefono, correo, direccion, codigo_postal, logo_url, descripcion, fecha_registro, updated_at, estado) VALUES (9, 'us-east-1:g8h9c0g1-5d6c-7b8a-9e0f-1a2b3c4d5e6f', 'Akrópolis Boliche & Bar', 'ABB150710MNO', 'Ricardo Jiménez', '5598765432', 'eventos@akropolisboliche.com', 'Plaza Galerías Atizapán, Av. Ruiz Cortines 255, Atizapán de Zaragoza, Méx.', '52977', 'https://example.com/logos/akropolis.png', 'Diversión garantizada para chicos y grandes. Contamos con 20 líneas de boliche profesionales, mesas de billar, bar con coctelería y un menú de snacks para que pases un momento increíble con amigos o familia.', '2025-10-13 18:43:28.525', '2025-10-13 18:43:28.525', 'activo');
INSERT INTO public.colaborador (colaborador_id, cognito_id, nombre_negocio, rfc, representante_nombre, telefono, correo, direccion, codigo_postal, logo_url, descripcion, fecha_registro, updated_at, estado) VALUES (10, 'us-east-1:h9i0d1h2-6d7c-8b9a-0f1g-2a3b4c5d6e7f', 'Escenario 360', 'PMV050801PQR', 'promotora Musical del Valle S.A.', '5555551234', 'eventos@escenario360.mx', 'Circuito Médicos 52, Cd. Satélite (Cercano a Atizapán), Naucalpan de Juárez, Méx.', '53100', 'https://example.com/logos/escenario360.png', 'Foro de espectáculos que presenta a los mejores artistas de la escena musical, comedia y teatro. Con una excelente visibilidad desde cualquier punto y un sistema de audio de primer nivel, cada evento es una experiencia única.', '2025-10-13 18:43:52.314053', '2025-10-13 18:43:52.314053', 'activo');
INSERT INTO public.colaborador (colaborador_id, cognito_id, nombre_negocio, rfc, representante_nombre, telefono, correo, direccion, codigo_postal, logo_url, descripcion, fecha_registro, updated_at, estado) VALUES (11, 'us-east-1:i0j1e2i3-7d8c-9b0a-1g2h-3a4b5c6d7e8f', 'Vértice Urbano', 'VUR200318STU', 'Andrea Contreras', '5543218765', 'contacto@verticeurbano.com', 'Av. San Mateo 120, Lomas de Atizapán, Atizapán de Zaragoza, Méx.', '52977', 'https://example.com/logos/verticeurbano.png', 'Boutique de moda urbana que reúne las mejores marcas de streetwear, sneakers de edición limitada y accesorios. Si buscas un estilo auténtico y a la vanguardia, este es tu lugar.', '2025-10-13 18:44:05.814853', '2025-10-13 18:44:05.814853', 'activo');
INSERT INTO public.colaborador (colaborador_id, cognito_id, nombre_negocio, rfc, representante_nombre, telefono, correo, direccion, codigo_postal, logo_url, descripcion, fecha_registro, updated_at, estado) VALUES (12, 'us-east-1:j1k2f3j4-8d9c-0c1b-2h3i-4a5b6c7d8e9f', 'Nido', 'NID191201VWX', 'Laura y Miguel Garza', '5587651234', 'contacto@nidoinfantil.mx', 'Plaza La Cantera, Calz. de los Jinetes 15, Atizapán de Zaragoza, Méx.', '52975', 'https://example.com/logos/nido.png', 'Ropa y accesorios para bebés y niños (0-8 años) con diseños únicos y materiales de alta calidad, priorizando el algodón orgánico. Creamos prendas cómodas, duraderas y con mucho estilo para los más pequeños.', '2025-10-13 18:44:19.884664', '2025-10-13 18:44:19.884664', 'activo');
INSERT INTO public.colaborador (colaborador_id, cognito_id, nombre_negocio, rfc, representante_nombre, telefono, correo, direccion, codigo_postal, logo_url, descripcion, fecha_registro, updated_at, estado) VALUES (2, '61dbe5a0-40f1-7057-5dc2-0b5531b02ac4', 'Sason de Iván', 'asjdfklsadjf', 'Sasonsito', '5530413030', 'ivan@example.com', 'Av. Lago de Gdp', '30941', NULL, NULL, '2025-10-12 20:59:45.731863', '2025-10-12 21:02:06.133812', 'activo');
INSERT INTO public.colaborador (colaborador_id, cognito_id, nombre_negocio, rfc, representante_nombre, telefono, correo, direccion, codigo_postal, logo_url, descripcion, fecha_registro, updated_at, estado) VALUES (13, 'us-east-1:m4n5i6m7-1g2c-3f4b-5k6j-7a8b9c0d1f2g', 'El Gato Lector Café', 'GLC211108LMN', 'Daniela Solís', '5519283746', 'juegos@gatolectorcafe.mx', 'Calz. San Mateo 30, Jardines de Atizapán, Atizapán de Zaragoza, Méx.', '52978', 'https://example.com/logos/gatolector.png', 'Tu refugio para el café y los juegos de mesa. Contamos con una ludoteca de más de 300 títulos y un menú de bebidas, snacks y postres para acompañar la diversión.', '2025-10-13 18:46:48.768825', '2025-10-13 18:46:48.768825', 'activo');
INSERT INTO public.colaborador (colaborador_id, cognito_id, nombre_negocio, rfc, representante_nombre, telefono, correo, direccion, codigo_postal, logo_url, descripcion, fecha_registro, updated_at, estado) VALUES (14, 'us-east-1:k2l3g4k5-9e0c-1d2b-3i4h-5a6b7c8d9e0f', 'Ascenso Urbano', 'AUR220815GHT', 'Sofía Ríos', '5561728394', 'contacto@ascensourbano.mx', 'Blvd. Ignacio Zaragoza 100, Lomas de Atizapán, Atizapán de Zaragoza, Méx.', '52977', 'https://example.com/logos/ascensourbano.png', 'Gimnasio de escalada y boulder para todos los niveles. Ofrecemos clases, pases de día y una tienda con el mejor equipo y ropa de marcas especializadas para tus aventuras verticales.', '2025-10-13 18:47:09.562009', '2025-10-13 18:47:09.562009', 'activo');
INSERT INTO public.colaborador (colaborador_id, cognito_id, nombre_negocio, rfc, representante_nombre, telefono, correo, direccion, codigo_postal, logo_url, descripcion, fecha_registro, updated_at, estado) VALUES (15, 'us-east-1:l3m4h5l6-0f1c-2e3b-4j5i-6a7b8c9d0e1f', 'Estilo & Sabor', 'EYS230321JKI', 'Mateo Herrera', '5593847561', 'hola@estiloysabor.com', 'Paseo de las Alamedas 5, Las Alamedas, Atizapán de Zaragoza, Méx.', '52970', 'https://example.com/logos/estiloysabor.png', 'Un espacio único que fusiona la moda y la gastronomía. Descubre las últimas tendencias de diseñadores locales mientras disfrutas de nuestro café de especialidad y repostería artesanal.', '2025-10-13 18:48:40.889724', '2025-10-13 18:48:40.889724', 'activo');


--
-- TOC entry 3500 (class 0 OID 74150)
-- Dependencies: 226
-- Data for Name: colaborador_categoria; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

INSERT INTO public.colaborador_categoria (colaborador_id, categoria_id) VALUES ('us-east-1:c4f5b6e7-1d2c-3b4a-5e6f-7a8b9c0d1e2f', 2);
INSERT INTO public.colaborador_categoria (colaborador_id, categoria_id) VALUES ('us-east-1:d5f6b7e8-2d3c-4b5a-6e7f-8a9b0c1d2e3f', 2);
INSERT INTO public.colaborador_categoria (colaborador_id, categoria_id) VALUES ('us-east-1:e6f7b8e9-3d4c-5b6a-7e8f-9a0b1c2d3e4f', 2);
INSERT INTO public.colaborador_categoria (colaborador_id, categoria_id) VALUES ('us-east-1:f7g8b9f0-4d5c-6b7a-8e9f-0a1b2c3d4e5f', 2);
INSERT INTO public.colaborador_categoria (colaborador_id, categoria_id) VALUES ('us-east-1:g8h9c0g1-5d6c-7b8a-9e0f-1a2b3c4d5e6f', 1);
INSERT INTO public.colaborador_categoria (colaborador_id, categoria_id) VALUES ('us-east-1:h9i0d1h2-6d7c-8b9a-0f1g-2a3b4c5d6e7f', 1);
INSERT INTO public.colaborador_categoria (colaborador_id, categoria_id) VALUES ('us-east-1:i0j1e2i3-7d8c-9b0a-1g2h-3a4b5c6d7e8f', 3);
INSERT INTO public.colaborador_categoria (colaborador_id, categoria_id) VALUES ('us-east-1:j1k2f3j4-8d9c-0c1b-2h3i-4a5b6c7d8e9f', 3);
INSERT INTO public.colaborador_categoria (colaborador_id, categoria_id) VALUES ('61dbe5a0-40f1-7057-5dc2-0b5531b02ac4', 1);
INSERT INTO public.colaborador_categoria (colaborador_id, categoria_id) VALUES ('61dbe5a0-40f1-7057-5dc2-0b5531b02ac4', 2);
INSERT INTO public.colaborador_categoria (colaborador_id, categoria_id) VALUES ('us-east-1:m4n5i6m7-1g2c-3f4b-5k6j-7a8b9c0d1f2g', 1);
INSERT INTO public.colaborador_categoria (colaborador_id, categoria_id) VALUES ('us-east-1:m4n5i6m7-1g2c-3f4b-5k6j-7a8b9c0d1f2g', 2);
INSERT INTO public.colaborador_categoria (colaborador_id, categoria_id) VALUES ('us-east-1:k2l3g4k5-9e0c-1d2b-3i4h-5a6b7c8d9e0f', 1);
INSERT INTO public.colaborador_categoria (colaborador_id, categoria_id) VALUES ('us-east-1:k2l3g4k5-9e0c-1d2b-3i4h-5a6b7c8d9e0f', 3);
INSERT INTO public.colaborador_categoria (colaborador_id, categoria_id) VALUES ('us-east-1:l3m4h5l6-0f1c-2e3b-4j5i-6a7b8c9d0e1f', 2);
INSERT INTO public.colaborador_categoria (colaborador_id, categoria_id) VALUES ('us-east-1:l3m4h5l6-0f1c-2e3b-4j5i-6a7b8c9d0e1f', 3);


--
-- TOC entry 3510 (class 0 OID 74260)
-- Dependencies: 236
-- Data for Name: favorito; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--



--
-- TOC entry 3512 (class 0 OID 74279)
-- Dependencies: 238
-- Data for Name: notificacion; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--



--
-- TOC entry 3504 (class 0 OID 74182)
-- Dependencies: 230
-- Data for Name: promocion; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

INSERT INTO public.promocion (promocion_id, colaborador_id, titulo, descripcion, imagen_url, fecha_inicio, fecha_fin, tipo_promocion, promocion_string, stock_total, stock_disponible, limite_por_usuario, limite_diario_por_usuario, estado, created_at, updated_at, theme) VALUES (1, '61dbe5a0-40f1-7057-5dc2-0b5531b02ac4', 'Prueba', NULL, NULL, NULL, NULL, 'descuento', NULL, NULL, NULL, NULL, NULL, 'activa', '2025-10-13 04:57:28.33199', '2025-10-13 04:57:28.33199', NULL);
INSERT INTO public.promocion (promocion_id, colaborador_id, titulo, descripcion, imagen_url, fecha_inicio, fecha_fin, tipo_promocion, promocion_string, stock_total, stock_disponible, limite_por_usuario, limite_diario_por_usuario, estado, created_at, updated_at, theme) VALUES (2, '61dbe5a0-40f1-7057-5dc2-0b5531b02ac4', 'Prueba', NULL, NULL, NULL, NULL, 'descuento', NULL, NULL, NULL, NULL, NULL, 'activa', '2025-10-13 04:58:11.05597', '2025-10-13 04:58:11.05597', NULL);
INSERT INTO public.promocion (promocion_id, colaborador_id, titulo, descripcion, imagen_url, fecha_inicio, fecha_fin, tipo_promocion, promocion_string, stock_total, stock_disponible, limite_por_usuario, limite_diario_por_usuario, estado, created_at, updated_at, theme) VALUES (3, '61dbe5a0-40f1-7057-5dc2-0b5531b02ac4', 'Prueba', NULL, NULL, NULL, NULL, 'descuento', NULL, NULL, NULL, NULL, NULL, 'activa', '2025-10-13 05:09:07.815034', '2025-10-13 05:09:07.815034', NULL);
INSERT INTO public.promocion (promocion_id, colaborador_id, titulo, descripcion, imagen_url, fecha_inicio, fecha_fin, tipo_promocion, promocion_string, stock_total, stock_disponible, limite_por_usuario, limite_diario_por_usuario, estado, created_at, updated_at, theme) VALUES (4, '61dbe5a0-40f1-7057-5dc2-0b5531b02ac4', 'Prueba', NULL, NULL, NULL, NULL, 'descuento', NULL, NULL, NULL, NULL, NULL, 'activa', '2025-10-13 05:10:21.956353', '2025-10-13 05:10:21.956353', NULL);
INSERT INTO public.promocion (promocion_id, colaborador_id, titulo, descripcion, imagen_url, fecha_inicio, fecha_fin, tipo_promocion, promocion_string, stock_total, stock_disponible, limite_por_usuario, limite_diario_por_usuario, estado, created_at, updated_at, theme) VALUES (6, 'us-east-1:c4f5b6e7-1d2c-3b4a-5e6f-7a8b9c0d1e2f', '2x1 en Hamburguesas', 'Disfruta nuestra promoción especial: lleva 2 hamburguesas por el precio de 1.', 'https://upload.wikimedia.org/wikipedia/commons/thumb/c/cc/Burger_King_2020.svg/330px-Burger_King_2020.svg.png', '2025-10-14', '2025-11-13', 'descuento', '2X1BURGER', 200, 200, 2, 1, 'activa', '2025-10-13 18:50:39.017437', '2025-10-13 18:50:39.017437', NULL);


--
-- TOC entry 3505 (class 0 OID 74197)
-- Dependencies: 231
-- Data for Name: promocion_categoria; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

INSERT INTO public.promocion_categoria (promocion_id, categoria_id) VALUES (6, 2);


--
-- TOC entry 3507 (class 0 OID 74213)
-- Dependencies: 233
-- Data for Name: reserva; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--



--
-- TOC entry 3502 (class 0 OID 74166)
-- Dependencies: 228
-- Data for Name: sucursal; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--



--
-- TOC entry 3509 (class 0 OID 74231)
-- Dependencies: 235
-- Data for Name: uso_cupon; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--



--
-- TOC entry 3492 (class 0 OID 74090)
-- Dependencies: 218
-- Data for Name: usuario; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

INSERT INTO public.usuario (usuario_id, cognito_id, nombre, apellido_paterno, apellido_materno, fecha_nacimiento, telefono, correo_electronico, fecha_registro, updated_at, estado_cuenta, token_notificacion) VALUES (11, 'a1fbe500-a091-70e3-5a7b-3b1f4537f10f', 'Emilio', 'De Leon', 'Vives', '2025-10-02', '5555555555', 'emileon888@gmail.com', '2025-10-13 20:51:10.836857', '2025-10-13 20:51:10.836857', 'activo', NULL);


--
-- TOC entry 3499 (class 0 OID 74135)
-- Dependencies: 225
-- Data for Name: usuario_categoria; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--



--
-- TOC entry 3530 (class 0 OID 0)
-- Dependencies: 223
-- Name: administrador_admin_id_seq; Type: SEQUENCE SET; Schema: public; Owner: neondb_owner
--

SELECT pg_catalog.setval('public.administrador_admin_id_seq', 1, false);


--
-- TOC entry 3531 (class 0 OID 0)
-- Dependencies: 219
-- Name: categoria_categoria_id_seq; Type: SEQUENCE SET; Schema: public; Owner: neondb_owner
--

SELECT pg_catalog.setval('public.categoria_categoria_id_seq', 3, true);


--
-- TOC entry 3532 (class 0 OID 0)
-- Dependencies: 221
-- Name: colaborador_colaborador_id_seq; Type: SEQUENCE SET; Schema: public; Owner: neondb_owner
--

SELECT pg_catalog.setval('public.colaborador_colaborador_id_seq', 15, true);


--
-- TOC entry 3533 (class 0 OID 0)
-- Dependencies: 237
-- Name: notificacion_notificacion_id_seq; Type: SEQUENCE SET; Schema: public; Owner: neondb_owner
--

SELECT pg_catalog.setval('public.notificacion_notificacion_id_seq', 1, false);


--
-- TOC entry 3534 (class 0 OID 0)
-- Dependencies: 229
-- Name: promocion_promocion_id_seq; Type: SEQUENCE SET; Schema: public; Owner: neondb_owner
--

SELECT pg_catalog.setval('public.promocion_promocion_id_seq', 8, true);


--
-- TOC entry 3535 (class 0 OID 0)
-- Dependencies: 232
-- Name: reserva_reserva_id_seq; Type: SEQUENCE SET; Schema: public; Owner: neondb_owner
--

SELECT pg_catalog.setval('public.reserva_reserva_id_seq', 1, false);


--
-- TOC entry 3536 (class 0 OID 0)
-- Dependencies: 227
-- Name: sucursal_sucursal_id_seq; Type: SEQUENCE SET; Schema: public; Owner: neondb_owner
--

SELECT pg_catalog.setval('public.sucursal_sucursal_id_seq', 1, false);


--
-- TOC entry 3537 (class 0 OID 0)
-- Dependencies: 234
-- Name: uso_cupon_uso_id_seq; Type: SEQUENCE SET; Schema: public; Owner: neondb_owner
--

SELECT pg_catalog.setval('public.uso_cupon_uso_id_seq', 1, false);


--
-- TOC entry 3538 (class 0 OID 0)
-- Dependencies: 217
-- Name: usuario_usuario_id_seq; Type: SEQUENCE SET; Schema: public; Owner: neondb_owner
--

SELECT pg_catalog.setval('public.usuario_usuario_id_seq', 11, true);


--
-- TOC entry 3308 (class 2606 OID 74134)
-- Name: administrador administrador_cognito_id_key; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.administrador
    ADD CONSTRAINT administrador_cognito_id_key UNIQUE (cognito_id);


--
-- TOC entry 3310 (class 2606 OID 74132)
-- Name: administrador administrador_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.administrador
    ADD CONSTRAINT administrador_pkey PRIMARY KEY (admin_id);


--
-- TOC entry 3302 (class 2606 OID 74108)
-- Name: categoria categoria_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.categoria
    ADD CONSTRAINT categoria_pkey PRIMARY KEY (categoria_id);


--
-- TOC entry 3314 (class 2606 OID 74154)
-- Name: colaborador_categoria colaborador_categoria_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.colaborador_categoria
    ADD CONSTRAINT colaborador_categoria_pkey PRIMARY KEY (colaborador_id, categoria_id);


--
-- TOC entry 3304 (class 2606 OID 74121)
-- Name: colaborador colaborador_cognito_id_key; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.colaborador
    ADD CONSTRAINT colaborador_cognito_id_key UNIQUE (cognito_id);


--
-- TOC entry 3306 (class 2606 OID 74119)
-- Name: colaborador colaborador_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.colaborador
    ADD CONSTRAINT colaborador_pkey PRIMARY KEY (colaborador_id);


--
-- TOC entry 3326 (class 2606 OID 74267)
-- Name: favorito favorito_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.favorito
    ADD CONSTRAINT favorito_pkey PRIMARY KEY (usuario_id, colaborador_id);


--
-- TOC entry 3328 (class 2606 OID 74287)
-- Name: notificacion notificacion_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.notificacion
    ADD CONSTRAINT notificacion_pkey PRIMARY KEY (notificacion_id);


--
-- TOC entry 3320 (class 2606 OID 74201)
-- Name: promocion_categoria promocion_categoria_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.promocion_categoria
    ADD CONSTRAINT promocion_categoria_pkey PRIMARY KEY (promocion_id, categoria_id);


--
-- Name: promocion_sucursal promocion_sucursal_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.promocion_sucursal
    ADD CONSTRAINT promocion_sucursal_pkey PRIMARY KEY (promocion_id, sucursal_id);


--
-- TOC entry 3318 (class 2606 OID 74191)
-- Name: promocion promocion_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.promocion
    ADD CONSTRAINT promocion_pkey PRIMARY KEY (promocion_id);


--
-- TOC entry 3322 (class 2606 OID 74219)
-- Name: reserva reserva_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.reserva
    ADD CONSTRAINT reserva_pkey PRIMARY KEY (reserva_id);


--
-- TOC entry 3316 (class 2606 OID 74175)
-- Name: sucursal sucursal_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.sucursal
    ADD CONSTRAINT sucursal_pkey PRIMARY KEY (sucursal_id);


--
-- TOC entry 3324 (class 2606 OID 74239)
-- Name: uso_cupon uso_cupon_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.uso_cupon
    ADD CONSTRAINT uso_cupon_pkey PRIMARY KEY (uso_id);


--
-- TOC entry 3312 (class 2606 OID 74139)
-- Name: usuario_categoria usuario_categoria_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.usuario_categoria
    ADD CONSTRAINT usuario_categoria_pkey PRIMARY KEY (usuario_id, categoria_id);


--
-- TOC entry 3298 (class 2606 OID 74101)
-- Name: usuario usuario_cognito_id_key; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_cognito_id_key UNIQUE (cognito_id);


--
-- TOC entry 3300 (class 2606 OID 74099)
-- Name: usuario usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (usuario_id);


--
-- TOC entry 3331 (class 2606 OID 74160)
-- Name: colaborador_categoria colaborador_categoria_categoria_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.colaborador_categoria
    ADD CONSTRAINT colaborador_categoria_categoria_id_fkey FOREIGN KEY (categoria_id) REFERENCES public.categoria(categoria_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3332 (class 2606 OID 74155)
-- Name: colaborador_categoria colaborador_categoria_colaborador_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.colaborador_categoria
    ADD CONSTRAINT colaborador_categoria_colaborador_id_fkey FOREIGN KEY (colaborador_id) REFERENCES public.colaborador(cognito_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3343 (class 2606 OID 74273)
-- Name: favorito favorito_colaborador_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.favorito
    ADD CONSTRAINT favorito_colaborador_id_fkey FOREIGN KEY (colaborador_id) REFERENCES public.colaborador(cognito_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3344 (class 2606 OID 74268)
-- Name: favorito favorito_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.favorito
    ADD CONSTRAINT favorito_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuario(cognito_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3345 (class 2606 OID 74288)
-- Name: notificacion notificacion_promocion_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.notificacion
    ADD CONSTRAINT notificacion_promocion_id_fkey FOREIGN KEY (promocion_id) REFERENCES public.promocion(promocion_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- TOC entry 3335 (class 2606 OID 74207)
-- Name: promocion_categoria promocion_categoria_categoria_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.promocion_categoria
    ADD CONSTRAINT promocion_categoria_categoria_id_fkey FOREIGN KEY (categoria_id) REFERENCES public.categoria(categoria_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3336 (class 2606 OID 74202)
-- Name: promocion_categoria promocion_categoria_promocion_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.promocion_categoria
    ADD CONSTRAINT promocion_categoria_promocion_id_fkey FOREIGN KEY (promocion_id) REFERENCES public.promocion(promocion_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: promocion_sucursal promocion_sucursal_promocion_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.promocion_sucursal
    ADD CONSTRAINT promocion_sucursal_promocion_id_fkey FOREIGN KEY (promocion_id) REFERENCES public.promocion(promocion_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: promocion_sucursal promocion_sucursal_sucursal_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.promocion_sucursal
    ADD CONSTRAINT promocion_sucursal_sucursal_id_fkey FOREIGN KEY (sucursal_id) REFERENCES public.sucursal(sucursal_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3334 (class 2606 OID 74192)
-- Name: promocion promocion_colaborador_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.promocion
    ADD CONSTRAINT promocion_colaborador_id_fkey FOREIGN KEY (colaborador_id) REFERENCES public.colaborador(cognito_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3337 (class 2606 OID 74225)
-- Name: reserva reserva_promocion_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.reserva
    ADD CONSTRAINT reserva_promocion_id_fkey FOREIGN KEY (promocion_id) REFERENCES public.promocion(promocion_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3338 (class 2606 OID 74220)
-- Name: reserva reserva_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.reserva
    ADD CONSTRAINT reserva_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuario(cognito_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3333 (class 2606 OID 74176)
-- Name: sucursal sucursal_colaborador_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.sucursal
    ADD CONSTRAINT sucursal_colaborador_id_fkey FOREIGN KEY (colaborador_id) REFERENCES public.colaborador(cognito_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3339 (class 2606 OID 74245)
-- Name: uso_cupon uso_cupon_colaborador_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.uso_cupon
    ADD CONSTRAINT uso_cupon_colaborador_id_fkey FOREIGN KEY (colaborador_id) REFERENCES public.colaborador(cognito_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3340 (class 2606 OID 74255)
-- Name: uso_cupon uso_cupon_promocion_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.uso_cupon
    ADD CONSTRAINT uso_cupon_promocion_id_fkey FOREIGN KEY (promocion_id) REFERENCES public.promocion(promocion_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3341 (class 2606 OID 74250)
-- Name: uso_cupon uso_cupon_sucursal_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.uso_cupon
    ADD CONSTRAINT uso_cupon_sucursal_id_fkey FOREIGN KEY (sucursal_id) REFERENCES public.sucursal(sucursal_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3342 (class 2606 OID 74240)
-- Name: uso_cupon uso_cupon_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.uso_cupon
    ADD CONSTRAINT uso_cupon_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuario(cognito_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3329 (class 2606 OID 74145)
-- Name: usuario_categoria usuario_categoria_categoria_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.usuario_categoria
    ADD CONSTRAINT usuario_categoria_categoria_id_fkey FOREIGN KEY (categoria_id) REFERENCES public.categoria(categoria_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3330 (class 2606 OID 74140)
-- Name: usuario_categoria usuario_categoria_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.usuario_categoria
    ADD CONSTRAINT usuario_categoria_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuario(cognito_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3519 (class 0 OID 0)
-- Dependencies: 3518
-- Name: DATABASE neondb; Type: ACL; Schema: -; Owner: neondb_owner
--

GRANT ALL ON DATABASE neondb TO neon_superuser;


--
-- TOC entry 3520 (class 0 OID 0)
-- Dependencies: 5
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: neondb_owner
--

REVOKE USAGE ON SCHEMA public FROM PUBLIC;


-- Completed on 2025-10-14 11:53:38 CST

--
-- PostgreSQL database dump complete
--

