--
-- PostgreSQL database dump
--

-- Dumped from database version 17.5 (6bc9ef8)
-- Dumped by pg_dump version 17.5

-- Started on 2025-10-25 21:32:00 CST

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
-- TOC entry 3536 (class 1262 OID 16391)
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
-- TOC entry 893 (class 1247 OID 74074)
-- Name: destinatario_tipo; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.destinatario_tipo AS ENUM (
    'usuario',
    'colaborador',
    'admin'
);


ALTER TYPE public.destinatario_tipo OWNER TO neondb_owner;

--
-- TOC entry 875 (class 1247 OID 74026)
-- Name: estado_colaborador; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.estado_colaborador AS ENUM (
    'activo',
    'inactivo',
    'suspendido'
);


ALTER TYPE public.estado_colaborador OWNER TO neondb_owner;

--
-- TOC entry 869 (class 1247 OID 74012)
-- Name: estado_cuenta; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.estado_cuenta AS ENUM (
    'activo',
    'inactivo',
    'suspendido'
);


ALTER TYPE public.estado_cuenta OWNER TO neondb_owner;

--
-- TOC entry 896 (class 1247 OID 74082)
-- Name: estado_notificacion; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.estado_notificacion AS ENUM (
    'enviada',
    'pendiente',
    'leida'
);


ALTER TYPE public.estado_notificacion OWNER TO neondb_owner;

--
-- TOC entry 884 (class 1247 OID 74050)
-- Name: estado_promocion; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.estado_promocion AS ENUM (
    'activa',
    'inactiva',
    'finalizada'
);


ALTER TYPE public.estado_promocion OWNER TO neondb_owner;

--
-- TOC entry 887 (class 1247 OID 74058)
-- Name: estado_reserva; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.estado_reserva AS ENUM (
    'pendiente',
    'usada',
    'cancelada'
);


ALTER TYPE public.estado_reserva OWNER TO neondb_owner;

--
-- TOC entry 878 (class 1247 OID 74034)
-- Name: estado_sucursal; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.estado_sucursal AS ENUM (
    'activa',
    'inactiva'
);


ALTER TYPE public.estado_sucursal OWNER TO neondb_owner;

--
-- TOC entry 872 (class 1247 OID 74020)
-- Name: rol_admin; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.rol_admin AS ENUM (
    'superadmin',
    'admin'
);


ALTER TYPE public.rol_admin OWNER TO neondb_owner;

--
-- TOC entry 938 (class 1247 OID 81921)
-- Name: tema; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.tema AS ENUM (
    'light',
    'dark'
);


ALTER TYPE public.tema OWNER TO neondb_owner;

--
-- TOC entry 890 (class 1247 OID 74066)
-- Name: tipo_notificacion; Type: TYPE; Schema: public; Owner: neondb_owner
--

CREATE TYPE public.tipo_notificacion AS ENUM (
    'info',
    'promo',
    'alerta'
);


ALTER TYPE public.tipo_notificacion OWNER TO neondb_owner;

--
-- TOC entry 881 (class 1247 OID 74040)
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
-- TOC entry 3539 (class 0 OID 0)
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
-- TOC entry 3540 (class 0 OID 0)
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
-- TOC entry 3541 (class 0 OID 0)
-- Dependencies: 221
-- Name: colaborador_colaborador_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.colaborador_colaborador_id_seq OWNED BY public.colaborador.colaborador_id;


--
-- TOC entry 239 (class 1259 OID 98304)
-- Name: cupon_favorito; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.cupon_favorito (
    usuario_id character varying(255),
    promocion_id integer
);


ALTER TABLE public.cupon_favorito OWNER TO neondb_owner;

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
-- TOC entry 3542 (class 0 OID 0)
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
    imagen_url character varying(500),
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
    theme public.tema,
    es_reservable boolean
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
-- TOC entry 3543 (class 0 OID 0)
-- Dependencies: 229
-- Name: promocion_promocion_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.promocion_promocion_id_seq OWNED BY public.promocion.promocion_id;


--
-- TOC entry 240 (class 1259 OID 106496)
-- Name: promocion_sucursal; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.promocion_sucursal (
    promocion_id integer NOT NULL,
    sucursal_id integer NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.promocion_sucursal OWNER TO neondb_owner;

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
    promocion_reservada integer,
    fecha_cancelacion timestamp without time zone,
    fecha_auto_expiracion timestamp without time zone,
    cooldown_hasta timestamp without time zone
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
-- TOC entry 3544 (class 0 OID 0)
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
-- TOC entry 3545 (class 0 OID 0)
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
    sucursal_id integer,
    promocion_id integer,
    fecha_uso timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    nonce character varying(255),
    qr_timestamp bigint
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
-- TOC entry 3546 (class 0 OID 0)
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
-- TOC entry 3547 (class 0 OID 0)
-- Dependencies: 217
-- Name: usuario_usuario_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.usuario_usuario_id_seq OWNED BY public.usuario.usuario_id;


--
-- TOC entry 3289 (class 2604 OID 74126)
-- Name: administrador admin_id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.administrador ALTER COLUMN admin_id SET DEFAULT nextval('public.administrador_admin_id_seq'::regclass);


--
-- TOC entry 3285 (class 2604 OID 74106)
-- Name: categoria categoria_id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.categoria ALTER COLUMN categoria_id SET DEFAULT nextval('public.categoria_categoria_id_seq'::regclass);


--
-- TOC entry 3286 (class 2604 OID 74113)
-- Name: colaborador colaborador_id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.colaborador ALTER COLUMN colaborador_id SET DEFAULT nextval('public.colaborador_colaborador_id_seq'::regclass);


--
-- TOC entry 3303 (class 2604 OID 74282)
-- Name: notificacion notificacion_id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.notificacion ALTER COLUMN notificacion_id SET DEFAULT nextval('public.notificacion_notificacion_id_seq'::regclass);


--
-- TOC entry 3295 (class 2604 OID 74185)
-- Name: promocion promocion_id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.promocion ALTER COLUMN promocion_id SET DEFAULT nextval('public.promocion_promocion_id_seq'::regclass);


--
-- TOC entry 3298 (class 2604 OID 74216)
-- Name: reserva reserva_id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.reserva ALTER COLUMN reserva_id SET DEFAULT nextval('public.reserva_reserva_id_seq'::regclass);


--
-- TOC entry 3292 (class 2604 OID 74169)
-- Name: sucursal sucursal_id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.sucursal ALTER COLUMN sucursal_id SET DEFAULT nextval('public.sucursal_sucursal_id_seq'::regclass);


--
-- TOC entry 3300 (class 2604 OID 74234)
-- Name: uso_cupon uso_id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.uso_cupon ALTER COLUMN uso_id SET DEFAULT nextval('public.uso_cupon_uso_id_seq'::regclass);


--
-- TOC entry 3282 (class 2604 OID 74093)
-- Name: usuario usuario_id; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.usuario ALTER COLUMN usuario_id SET DEFAULT nextval('public.usuario_usuario_id_seq'::regclass);


--
-- TOC entry 3514 (class 0 OID 74123)
-- Dependencies: 224
-- Data for Name: administrador; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.administrador (admin_id, cognito_id, nombre, apellido_paterno, apellido_materno, correo, telefono, rol, estado, created_at, updated_at) FROM stdin;
\.


--
-- TOC entry 3510 (class 0 OID 74103)
-- Dependencies: 220
-- Data for Name: categoria; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.categoria (categoria_id, nombre) FROM stdin;
5	BELLEZA
7	SERVICIOS
6	EDUCACIÓN
4	SALUD
2	ENTRETENIMIENTO
3	ROPA
1	ALIMENTOS
\.


--
-- TOC entry 3512 (class 0 OID 74110)
-- Dependencies: 222
-- Data for Name: colaborador; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.colaborador (colaborador_id, cognito_id, nombre_negocio, rfc, representante_nombre, telefono, correo, direccion, codigo_postal, logo_url, descripcion, fecha_registro, updated_at, estado) FROM stdin;
\.


--
-- TOC entry 3516 (class 0 OID 74150)
-- Dependencies: 226
-- Data for Name: colaborador_categoria; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.colaborador_categoria (colaborador_id, categoria_id) FROM stdin;
\.


--
-- TOC entry 3529 (class 0 OID 98304)
-- Dependencies: 239
-- Data for Name: cupon_favorito; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.cupon_favorito (usuario_id, promocion_id) FROM stdin;
\.


--
-- TOC entry 3526 (class 0 OID 74260)
-- Dependencies: 236
-- Data for Name: favorito; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.favorito (usuario_id, colaborador_id, fecha_agregado) FROM stdin;
\.


--
-- TOC entry 3528 (class 0 OID 74279)
-- Dependencies: 238
-- Data for Name: notificacion; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.notificacion (notificacion_id, titulo, mensaje, tipo, fecha_envio, destinatario_tipo, destinatario_id, estado, criterios_segmento, promocion_id) FROM stdin;
\.


--
-- TOC entry 3520 (class 0 OID 74182)
-- Dependencies: 230
-- Data for Name: promocion; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.promocion (promocion_id, colaborador_id, titulo, descripcion, imagen_url, fecha_inicio, fecha_fin, tipo_promocion, promocion_string, stock_total, stock_disponible, limite_por_usuario, limite_diario_por_usuario, estado, created_at, updated_at, theme, es_reservable) FROM stdin;
\.


--
-- TOC entry 3521 (class 0 OID 74197)
-- Dependencies: 231
-- Data for Name: promocion_categoria; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.promocion_categoria (promocion_id, categoria_id) FROM stdin;
\.


--
-- TOC entry 3530 (class 0 OID 106496)
-- Dependencies: 240
-- Data for Name: promocion_sucursal; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.promocion_sucursal (promocion_id, sucursal_id, created_at) FROM stdin;
\.


--
-- TOC entry 3523 (class 0 OID 74213)
-- Dependencies: 233
-- Data for Name: reserva; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.reserva (reserva_id, usuario_id, promocion_id, fecha_reserva, fecha_limite_uso, estado, promocion_reservada, fecha_cancelacion, fecha_auto_expiracion, cooldown_hasta) FROM stdin;
\.


--
-- TOC entry 3518 (class 0 OID 74166)
-- Dependencies: 228
-- Data for Name: sucursal; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.sucursal (sucursal_id, colaborador_id, nombre, telefono, direccion, codigo_postal, ubicacion, horario_json, estado, created_at, updated_at) FROM stdin;
\.


--
-- TOC entry 3525 (class 0 OID 74231)
-- Dependencies: 235
-- Data for Name: uso_cupon; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.uso_cupon (uso_id, usuario_id, sucursal_id, promocion_id, fecha_uso, nonce, qr_timestamp) FROM stdin;
\.


--
-- TOC entry 3508 (class 0 OID 74090)
-- Dependencies: 218
-- Data for Name: usuario; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.usuario (usuario_id, cognito_id, nombre, apellido_paterno, apellido_materno, fecha_nacimiento, telefono, correo_electronico, fecha_registro, updated_at, estado_cuenta, token_notificacion) FROM stdin;
\.


--
-- TOC entry 3515 (class 0 OID 74135)
-- Dependencies: 225
-- Data for Name: usuario_categoria; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.usuario_categoria (usuario_id, categoria_id) FROM stdin;
\.


--
-- TOC entry 3548 (class 0 OID 0)
-- Dependencies: 223
-- Name: administrador_admin_id_seq; Type: SEQUENCE SET; Schema: public; Owner: neondb_owner
--

SELECT pg_catalog.setval('public.administrador_admin_id_seq', 1, false);


--
-- TOC entry 3549 (class 0 OID 0)
-- Dependencies: 219
-- Name: categoria_categoria_id_seq; Type: SEQUENCE SET; Schema: public; Owner: neondb_owner
--

SELECT pg_catalog.setval('public.categoria_categoria_id_seq', 3, true);


--
-- TOC entry 3550 (class 0 OID 0)
-- Dependencies: 221
-- Name: colaborador_colaborador_id_seq; Type: SEQUENCE SET; Schema: public; Owner: neondb_owner
--

SELECT pg_catalog.setval('public.colaborador_colaborador_id_seq', 55, true);


--
-- TOC entry 3551 (class 0 OID 0)
-- Dependencies: 237
-- Name: notificacion_notificacion_id_seq; Type: SEQUENCE SET; Schema: public; Owner: neondb_owner
--

SELECT pg_catalog.setval('public.notificacion_notificacion_id_seq', 30, true);


--
-- TOC entry 3552 (class 0 OID 0)
-- Dependencies: 229
-- Name: promocion_promocion_id_seq; Type: SEQUENCE SET; Schema: public; Owner: neondb_owner
--

SELECT pg_catalog.setval('public.promocion_promocion_id_seq', 93, true);


--
-- TOC entry 3553 (class 0 OID 0)
-- Dependencies: 232
-- Name: reserva_reserva_id_seq; Type: SEQUENCE SET; Schema: public; Owner: neondb_owner
--

SELECT pg_catalog.setval('public.reserva_reserva_id_seq', 82, true);


--
-- TOC entry 3554 (class 0 OID 0)
-- Dependencies: 227
-- Name: sucursal_sucursal_id_seq; Type: SEQUENCE SET; Schema: public; Owner: neondb_owner
--

SELECT pg_catalog.setval('public.sucursal_sucursal_id_seq', 38, true);


--
-- TOC entry 3555 (class 0 OID 0)
-- Dependencies: 234
-- Name: uso_cupon_uso_id_seq; Type: SEQUENCE SET; Schema: public; Owner: neondb_owner
--

SELECT pg_catalog.setval('public.uso_cupon_uso_id_seq', 36, true);


--
-- TOC entry 3556 (class 0 OID 0)
-- Dependencies: 217
-- Name: usuario_usuario_id_seq; Type: SEQUENCE SET; Schema: public; Owner: neondb_owner
--

SELECT pg_catalog.setval('public.usuario_usuario_id_seq', 60, true);


--
-- TOC entry 3317 (class 2606 OID 74134)
-- Name: administrador administrador_cognito_id_key; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.administrador
    ADD CONSTRAINT administrador_cognito_id_key UNIQUE (cognito_id);


--
-- TOC entry 3319 (class 2606 OID 74132)
-- Name: administrador administrador_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.administrador
    ADD CONSTRAINT administrador_pkey PRIMARY KEY (admin_id);


--
-- TOC entry 3311 (class 2606 OID 74108)
-- Name: categoria categoria_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.categoria
    ADD CONSTRAINT categoria_pkey PRIMARY KEY (categoria_id);


--
-- TOC entry 3323 (class 2606 OID 74154)
-- Name: colaborador_categoria colaborador_categoria_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.colaborador_categoria
    ADD CONSTRAINT colaborador_categoria_pkey PRIMARY KEY (colaborador_id, categoria_id);


--
-- TOC entry 3313 (class 2606 OID 74121)
-- Name: colaborador colaborador_cognito_id_key; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.colaborador
    ADD CONSTRAINT colaborador_cognito_id_key UNIQUE (cognito_id);


--
-- TOC entry 3315 (class 2606 OID 74119)
-- Name: colaborador colaborador_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.colaborador
    ADD CONSTRAINT colaborador_pkey PRIMARY KEY (colaborador_id);


--
-- TOC entry 3337 (class 2606 OID 74267)
-- Name: favorito favorito_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.favorito
    ADD CONSTRAINT favorito_pkey PRIMARY KEY (usuario_id, colaborador_id);


--
-- TOC entry 3339 (class 2606 OID 74287)
-- Name: notificacion notificacion_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.notificacion
    ADD CONSTRAINT notificacion_pkey PRIMARY KEY (notificacion_id);


--
-- TOC entry 3329 (class 2606 OID 74201)
-- Name: promocion_categoria promocion_categoria_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.promocion_categoria
    ADD CONSTRAINT promocion_categoria_pkey PRIMARY KEY (promocion_id, categoria_id);


--
-- TOC entry 3327 (class 2606 OID 74191)
-- Name: promocion promocion_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.promocion
    ADD CONSTRAINT promocion_pkey PRIMARY KEY (promocion_id);


--
-- TOC entry 3341 (class 2606 OID 106501)
-- Name: promocion_sucursal promocion_sucursal_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.promocion_sucursal
    ADD CONSTRAINT promocion_sucursal_pkey PRIMARY KEY (promocion_id, sucursal_id);


--
-- TOC entry 3331 (class 2606 OID 74219)
-- Name: reserva reserva_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.reserva
    ADD CONSTRAINT reserva_pkey PRIMARY KEY (reserva_id);


--
-- TOC entry 3325 (class 2606 OID 74175)
-- Name: sucursal sucursal_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.sucursal
    ADD CONSTRAINT sucursal_pkey PRIMARY KEY (sucursal_id);


--
-- TOC entry 3335 (class 2606 OID 74239)
-- Name: uso_cupon uso_cupon_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.uso_cupon
    ADD CONSTRAINT uso_cupon_pkey PRIMARY KEY (uso_id);


--
-- TOC entry 3321 (class 2606 OID 74139)
-- Name: usuario_categoria usuario_categoria_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.usuario_categoria
    ADD CONSTRAINT usuario_categoria_pkey PRIMARY KEY (usuario_id, categoria_id);


--
-- TOC entry 3307 (class 2606 OID 74101)
-- Name: usuario usuario_cognito_id_key; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_cognito_id_key UNIQUE (cognito_id);


--
-- TOC entry 3309 (class 2606 OID 74099)
-- Name: usuario usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (usuario_id);


--
-- TOC entry 3332 (class 1259 OID 114688)
-- Name: idx_uso_cupon_nonce; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_uso_cupon_nonce ON public.uso_cupon USING btree (nonce);


--
-- TOC entry 3333 (class 1259 OID 114689)
-- Name: idx_uso_cupon_promocion_nonce; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_uso_cupon_promocion_nonce ON public.uso_cupon USING btree (promocion_id, nonce);


--
-- TOC entry 3344 (class 2606 OID 74160)
-- Name: colaborador_categoria colaborador_categoria_categoria_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.colaborador_categoria
    ADD CONSTRAINT colaborador_categoria_categoria_id_fkey FOREIGN KEY (categoria_id) REFERENCES public.categoria(categoria_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3345 (class 2606 OID 74155)
-- Name: colaborador_categoria colaborador_categoria_colaborador_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.colaborador_categoria
    ADD CONSTRAINT colaborador_categoria_colaborador_id_fkey FOREIGN KEY (colaborador_id) REFERENCES public.colaborador(cognito_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3358 (class 2606 OID 98312)
-- Name: cupon_favorito cupon_favorito_promocion_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.cupon_favorito
    ADD CONSTRAINT cupon_favorito_promocion_id_fkey FOREIGN KEY (promocion_id) REFERENCES public.promocion(promocion_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3359 (class 2606 OID 98307)
-- Name: cupon_favorito cupon_favorito_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.cupon_favorito
    ADD CONSTRAINT cupon_favorito_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuario(cognito_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3355 (class 2606 OID 74273)
-- Name: favorito favorito_colaborador_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.favorito
    ADD CONSTRAINT favorito_colaborador_id_fkey FOREIGN KEY (colaborador_id) REFERENCES public.colaborador(cognito_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3356 (class 2606 OID 74268)
-- Name: favorito favorito_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.favorito
    ADD CONSTRAINT favorito_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuario(cognito_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3357 (class 2606 OID 74288)
-- Name: notificacion notificacion_promocion_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.notificacion
    ADD CONSTRAINT notificacion_promocion_id_fkey FOREIGN KEY (promocion_id) REFERENCES public.promocion(promocion_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- TOC entry 3348 (class 2606 OID 74207)
-- Name: promocion_categoria promocion_categoria_categoria_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.promocion_categoria
    ADD CONSTRAINT promocion_categoria_categoria_id_fkey FOREIGN KEY (categoria_id) REFERENCES public.categoria(categoria_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3349 (class 2606 OID 74202)
-- Name: promocion_categoria promocion_categoria_promocion_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.promocion_categoria
    ADD CONSTRAINT promocion_categoria_promocion_id_fkey FOREIGN KEY (promocion_id) REFERENCES public.promocion(promocion_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3347 (class 2606 OID 74192)
-- Name: promocion promocion_colaborador_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.promocion
    ADD CONSTRAINT promocion_colaborador_id_fkey FOREIGN KEY (colaborador_id) REFERENCES public.colaborador(cognito_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3360 (class 2606 OID 106502)
-- Name: promocion_sucursal promocion_sucursal_promocion_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.promocion_sucursal
    ADD CONSTRAINT promocion_sucursal_promocion_id_fkey FOREIGN KEY (promocion_id) REFERENCES public.promocion(promocion_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3361 (class 2606 OID 106507)
-- Name: promocion_sucursal promocion_sucursal_sucursal_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.promocion_sucursal
    ADD CONSTRAINT promocion_sucursal_sucursal_id_fkey FOREIGN KEY (sucursal_id) REFERENCES public.sucursal(sucursal_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3350 (class 2606 OID 74225)
-- Name: reserva reserva_promocion_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.reserva
    ADD CONSTRAINT reserva_promocion_id_fkey FOREIGN KEY (promocion_id) REFERENCES public.promocion(promocion_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3351 (class 2606 OID 74220)
-- Name: reserva reserva_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.reserva
    ADD CONSTRAINT reserva_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuario(cognito_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3346 (class 2606 OID 74176)
-- Name: sucursal sucursal_colaborador_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.sucursal
    ADD CONSTRAINT sucursal_colaborador_id_fkey FOREIGN KEY (colaborador_id) REFERENCES public.colaborador(cognito_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3352 (class 2606 OID 74255)
-- Name: uso_cupon uso_cupon_promocion_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.uso_cupon
    ADD CONSTRAINT uso_cupon_promocion_id_fkey FOREIGN KEY (promocion_id) REFERENCES public.promocion(promocion_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3353 (class 2606 OID 74250)
-- Name: uso_cupon uso_cupon_sucursal_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.uso_cupon
    ADD CONSTRAINT uso_cupon_sucursal_id_fkey FOREIGN KEY (sucursal_id) REFERENCES public.sucursal(sucursal_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3354 (class 2606 OID 74240)
-- Name: uso_cupon uso_cupon_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.uso_cupon
    ADD CONSTRAINT uso_cupon_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuario(cognito_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3342 (class 2606 OID 74145)
-- Name: usuario_categoria usuario_categoria_categoria_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.usuario_categoria
    ADD CONSTRAINT usuario_categoria_categoria_id_fkey FOREIGN KEY (categoria_id) REFERENCES public.categoria(categoria_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3343 (class 2606 OID 74140)
-- Name: usuario_categoria usuario_categoria_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.usuario_categoria
    ADD CONSTRAINT usuario_categoria_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuario(cognito_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3537 (class 0 OID 0)
-- Dependencies: 3536
-- Name: DATABASE neondb; Type: ACL; Schema: -; Owner: neondb_owner
--

GRANT ALL ON DATABASE neondb TO neon_superuser;


--
-- TOC entry 3538 (class 0 OID 0)
-- Dependencies: 5
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: neondb_owner
--

REVOKE USAGE ON SCHEMA public FROM PUBLIC;


-- Completed on 2025-10-25 21:32:16 CST

--
-- PostgreSQL database dump complete
--

