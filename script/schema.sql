-- Escuela Jurídica — creación de tablas (PostgreSQL)
-- Ejecutar manualmente una sola vez sobre la base de datos.

CREATE TABLE administrador (
  id_admin          SERIAL        PRIMARY KEY,
  nombre            VARCHAR(100)  NOT NULL,
  apellido          VARCHAR(100)  NOT NULL,
  correo            VARCHAR(150)  NOT NULL UNIQUE,
  telefono          VARCHAR(20),
  usuario           VARCHAR(50)   NOT NULL UNIQUE,
  clave             VARCHAR(255)  NOT NULL,
  estado            CHAR(1)       NOT NULL DEFAULT 'A' CHECK (estado IN ('A','I')),
  fecha_creacion     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP    NULL,
  fecha_ultimo_login TIMESTAMP    NULL
);

CREATE TABLE tipo_curso (
  id_tipo_curso      SERIAL        PRIMARY KEY,
  nombre             VARCHAR(100)  NOT NULL UNIQUE,
  descripcion        VARCHAR(300),
  fecha_creacion     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP     NULL
);

CREATE TABLE docente (
  id_docente         SERIAL        PRIMARY KEY,
  nombre             VARCHAR(100)  NOT NULL,
  apellido           VARCHAR(100)  NOT NULL,
  especialidad       VARCHAR(100),           -- "Derecho Registral", "Derecho Notarial", "Derecho Civil"
  cargo              VARCHAR(100),           -- "Notario Público", "Vocal", "Abogado Litigante"
  estado             CHAR(1)       NOT NULL DEFAULT 'A' CHECK (estado IN ('A','I')),
  fecha_creacion     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP     NULL
);

CREATE TABLE curso (
  id_curso        SERIAL        PRIMARY KEY,
  codigo          VARCHAR(20)   NOT NULL UNIQUE,
  nombre          VARCHAR(150)  NOT NULL,
  descripcion     VARCHAR(500)  NOT NULL,
  institucion     VARCHAR(100)  DEFAULT 'Escuela Jurídica',
  modalidad       VARCHAR(20)   CHECK (modalidad IN ('Virtual','Presencial','Híbrido')),
  duracion_horas  INT,
  cupos           INT,
  fecha_inicio    DATE,
  fecha_fin       DATE,
  precio          NUMERIC(10,2) NOT NULL,
  imagen          VARCHAR(255)  NOT NULL,
  destacado       BOOLEAN       NOT NULL DEFAULT false,
  estado          CHAR(1)       NOT NULL DEFAULT 'A' CHECK (estado IN ('A','I')),
  fecha_creacion  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP  NULL,
  id_tipo_curso   INT           NOT NULL REFERENCES tipo_curso(id_tipo_curso),
  id_admin        INT           NOT NULL REFERENCES administrador(id_admin),
  id_docente      INT           NOT NULL REFERENCES docente(id_docente)
);

-- Datos de referencia: tipos de curso (coinciden con los botones del banner de /catalogo)
INSERT INTO tipo_curso (nombre, descripcion) VALUES
  ('Diplomado', 'Programa de certificación extendida respaldado por instituciones del sector jurídico.'),
  ('Curso Corto', 'Formación puntual, 100% virtual, a tu propio ritmo.'),
  ('Seminario', 'Sesión formativa dictada por especialistas en ejercicio.'),
  ('Programa', 'Formación estructurada con respaldo académico institucional.');
