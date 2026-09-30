-- Escuela Jurídica — script completo (PostgreSQL)
-- Sirve tanto para la base local como para la de la nube. Ejecutar completo, de arriba a abajo.

-- ============================================================
-- SECCIÓN 1: ELIMINAR TABLAS (si existen)
-- ============================================================

DROP TABLE IF EXISTS curso;
DROP TABLE IF EXISTS docente;
DROP TABLE IF EXISTS tipo_curso;
DROP TABLE IF EXISTS administrador;

-- ============================================================
-- SECCIÓN 2: CREAR TABLAS
-- ============================================================

CREATE TABLE administrador (
  id_admin           SERIAL        PRIMARY KEY,
  nombres            VARCHAR(100)  NOT NULL,
  apellidos          VARCHAR(100)  NOT NULL,
  correo             VARCHAR(150)  NOT NULL UNIQUE,
  telefono           VARCHAR(20),
  clave              VARCHAR(255)  NOT NULL,
  estado             CHAR(1)       NOT NULL DEFAULT 'A' CHECK (estado IN ('A','I')),
  fecha_creacion     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP     NULL,
  fecha_ultimo_login TIMESTAMP     NULL
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
  nombres            VARCHAR(100)  NOT NULL,
  apellidos          VARCHAR(100)  NOT NULL,
  especialidad       VARCHAR(100),
  cargo              VARCHAR(100),
  estado             CHAR(1)       NOT NULL DEFAULT 'A' CHECK (estado IN ('A','I')),
  fecha_creacion     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP     NULL
);

CREATE TABLE curso (
  id_curso           SERIAL        PRIMARY KEY,
  codigo             VARCHAR(20)   NOT NULL UNIQUE,
  nombre             VARCHAR(150)  NOT NULL,
  descripcion        VARCHAR(500)  NOT NULL,
  institucion        VARCHAR(100)  DEFAULT 'Escuela Jurídica',
  modalidad          VARCHAR(20)   CHECK (modalidad IN ('Virtual','Presencial','Híbrido')),
  duracion_horas     INT,
  cupos              INT,
  fecha_inicio       DATE,
  fecha_fin          DATE,
  precio             NUMERIC(10,2) NOT NULL,
  imagen             VARCHAR(255)  NOT NULL,
  destacado          BOOLEAN       NOT NULL DEFAULT false,
  estado             CHAR(1)       NOT NULL DEFAULT 'A' CHECK (estado IN ('A','I')),
  fecha_creacion     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP     NULL,
  id_tipo_curso      INT           NOT NULL REFERENCES tipo_curso(id_tipo_curso),
  id_admin           INT           NOT NULL REFERENCES administrador(id_admin),
  id_docente         INT           NOT NULL REFERENCES docente(id_docente)
);

-- ============================================================
-- SECCIÓN 3: INSERTS (datos de referencia + semilla)
-- ============================================================

-- Login por correo, no por usuario separado. Clave: 1234 (ya hasheada con BCrypt).
-- Cambiar esta clave despues del primer login.
INSERT INTO administrador (nombres, apellidos, correo, telefono, clave) VALUES
  ('Maykol Adán', 'Calle Paredes', 'admin@escuelajuridica.edu.pe', '912016161', '$2b$10$IwXX67uMmtk0O2/4UqsS9uIopSTunMPAfUCzskIT3hrbaWAjw/d.i');

INSERT INTO tipo_curso (nombre, descripcion) VALUES
  ('Diplomado', 'Programa de certificación extendida respaldado por instituciones del sector jurídico.'),
  ('Curso Corto', 'Formación puntual, 100% virtual, a tu propio ritmo.'),
  ('Seminario', 'Sesión formativa dictada por especialistas en ejercicio.'),
  ('Programa', 'Formación estructurada con respaldo académico institucional.');

INSERT INTO docente (nombres, apellidos, especialidad, cargo) VALUES
  ('Lilia Mercedes', 'Guerra Macedo', 'Derecho Civil', 'Directora Académica'),
  ('Yourka', 'Lucich Berrío', 'Derecho Notarial', 'Docente Especialista'),
  ('Rosario', 'Guerra Macedo', 'Derecho Civil', 'Abogada Litigante'),
  ('Ximena', 'Góngora Guerra', 'Derecho Civil', 'Coordinadora Académica'),
  ('Jorge Luis', 'Ramírez Salazar', 'Derecho Notarial', 'Notario Público'),
  ('Fernando Alonso', 'Quispe Mendoza', 'Derecho Penal', 'Vocal'),
  ('Patricia Elena', 'Salinas Rojas', 'Derecho Laboral', 'Abogada Litigante'),
  ('Carlos Alberto', 'Medina Torres', 'Derecho Registral', 'Funcionario Registral'),
  ('Milagros del Pilar', 'Cárdenas Vega', 'Derecho Constitucional', 'Docente Especialista'),
  ('Renzo Gabriel', 'Huamán Paredes', 'Derecho Digital', 'Abogado Litigante'),
  ('Manuel Alejandro', 'Ortiz Piñella', 'Derecho Registral', 'Docente Titular');

INSERT INTO curso (codigo, nombre, descripcion, institucion, modalidad, duracion_horas, cupos, fecha_inicio, fecha_fin, precio, imagen, destacado, id_tipo_curso, id_admin, id_docente) VALUES

('EJ-2026-001', 'Diplomado en Derecho Registral y Catastral',
 'Formación especializada en calificación registral, inscripción de predios y saneamiento catastral, con enfoque práctico en la normativa peruana vigente.',
 'Colegio de Abogados de Lima', 'Virtual', 120, 30, '2026-10-13', '2026-12-13', 1200.00, '/uploads/cursos/curso-01.jpg', true,
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Diplomado'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Manuel Alejandro' AND apellidos = 'Ortiz Piñella')),

('EJ-2026-002', 'Diplomado en Derecho Notarial y Contratos',
 'Profundiza en la función notarial, elaboración de escrituras públicas y redacción de contratos civiles y mercantiles bajo los estándares del notariado peruano.',
 'Colegio de Abogados de Lima Sur', 'Virtual', 100, 28, '2026-10-20', '2026-12-15', 1100.00, '/uploads/cursos/curso-02.jpg', true,
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Diplomado'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Jorge Luis' AND apellidos = 'Ramírez Salazar')),

('EJ-2026-003', 'Diplomado en Derecho Civil Patrimonial',
 'Analiza a fondo los derechos reales, obligaciones y contratos patrimoniales, con casos reales de litigio civil en el Perú.',
 'Escuela Jurídica', 'Presencial', 110, 25, '2026-10-06', '2026-12-06', 1300.00, '/uploads/cursos/curso-03.jpg', true,
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Diplomado'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Rosario' AND apellidos = 'Guerra Macedo')),

('EJ-2026-004', 'Diplomado en Derecho Procesal Civil',
 'Domina las etapas del proceso civil peruano, desde la postulación de la demanda hasta la ejecución de sentencias, con talleres de litigación.',
 'Academia de la Magistratura', 'Híbrido', 130, 20, '2026-11-03', '2027-01-15', 1350.00, '/uploads/cursos/curso-04.jpg', true,
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Diplomado'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Lilia Mercedes' AND apellidos = 'Guerra Macedo')),

('EJ-2026-005', 'Diplomado en Derecho Penal y Litigación Oral',
 'Desarrolla habilidades de litigación oral en el sistema penal acusatorio, con simulacros de audiencia y análisis de jurisprudencia.',
 'Escuela Jurídica', 'Virtual', 140, 35, '2026-10-27', '2027-01-10', 1400.00, '/uploads/cursos/curso-05.jpg', true,
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Diplomado'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Fernando Alonso' AND apellidos = 'Quispe Mendoza')),

('EJ-2026-006', 'Curso Corto de Redacción de Escrituras Públicas',
 'Aprende a redactar minutas y escrituras públicas con la técnica y el formato exigido por el notariado peruano.',
 'Escuela Jurídica', 'Virtual', 20, 40, '2026-10-06', '2026-10-20', 250.00, '/uploads/cursos/curso-06.jpg', true,
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Curso Corto'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Jorge Luis' AND apellidos = 'Ramírez Salazar')),

('EJ-2026-007', 'Curso Corto de Calificación Registral',
 'Revisa los criterios y procedimientos que usan los registradores públicos para calificar e inscribir títulos.',
 'Escuela Jurídica', 'Virtual', 24, 35, '2026-10-13', '2026-10-30', 280.00, '/uploads/cursos/curso-07.jpg', false,
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Curso Corto'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Carlos Alberto' AND apellidos = 'Medina Torres')),

('EJ-2026-008', 'Curso Corto de Contratos Civiles',
 'Curso práctico sobre elaboración y análisis de contratos civiles de uso frecuente: compraventa, arrendamiento y mutuo.',
 'Escuela Jurídica', 'Virtual', 18, 38, '2026-10-20', '2026-11-03', 220.00, '/uploads/cursos/curso-08.jpg', false,
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Curso Corto'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Rosario' AND apellidos = 'Guerra Macedo')),

('EJ-2026-009', 'Curso Corto de Derecho Laboral Aplicado',
 'Introducción práctica a los derechos y obligaciones laborales vigentes, con casos de despido, liquidación y beneficios sociales.',
 'Escuela Jurídica', 'Virtual', 16, 40, '2026-10-27', '2026-11-10', 200.00, '/uploads/cursos/curso-09.jpg', false,
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Curso Corto'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Patricia Elena' AND apellidos = 'Salinas Rojas')),

('EJ-2026-010', 'Curso Corto de Derecho Digital y Protección de Datos',
 'Explora el marco legal peruano de protección de datos personales y los retos jurídicos de la inteligencia artificial.',
 'Escuela Jurídica', 'Virtual', 20, 30, '2026-11-03', '2026-11-17', 260.00, '/uploads/cursos/curso-10.jpg', false,
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Curso Corto'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Renzo Gabriel' AND apellidos = 'Huamán Paredes')),

('EJ-2026-011', 'Seminario de Actualización en Derecho Registral',
 'Sesión intensiva sobre las últimas modificaciones al reglamento de inscripciones del Registro de Predios.',
 'Colegio de Abogados de Lima', 'Presencial', 8, 50, '2026-10-17', '2026-10-17', 150.00, '/uploads/cursos/curso-11.jpg', false,
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Seminario'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Carlos Alberto' AND apellidos = 'Medina Torres')),

('EJ-2026-012', 'Seminario de Litigación Oral Penal',
 'Taller práctico de técnicas de interrogatorio y alegatos orales para el proceso penal acusatorio.',
 'Escuela Jurídica', 'Presencial', 10, 45, '2026-10-24', '2026-10-24', 180.00, '/uploads/cursos/curso-12.jpg', false,
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Seminario'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Fernando Alonso' AND apellidos = 'Quispe Mendoza')),

('EJ-2026-013', 'Seminario de Derecho Constitucional Contemporáneo',
 'Analiza los últimos precedentes del Tribunal Constitucional y su impacto en el ejercicio profesional del derecho.',
 'Escuela Jurídica', 'Híbrido', 8, 50, '2026-10-31', '2026-10-31', 160.00, '/uploads/cursos/curso-13.jpg', false,
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Seminario'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Milagros del Pilar' AND apellidos = 'Cárdenas Vega')),

('EJ-2026-014', 'Seminario de Inteligencia Artificial Aplicada al Derecho',
 'Panorama sobre el uso de inteligencia artificial en la práctica legal y los retos éticos y normativos que plantea.',
 'Escuela Jurídica', 'Virtual', 6, 60, '2026-11-07', '2026-11-07', 140.00, '/uploads/cursos/curso-14.jpg', false,
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Seminario'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Renzo Gabriel' AND apellidos = 'Huamán Paredes')),

('EJ-2026-015', 'Seminario de Nueva Ley Procesal del Trabajo',
 'Revisa los cambios procesales más relevantes para la defensa de derechos laborales ante el Poder Judicial.',
 'Escuela Jurídica', 'Virtual', 8, 50, '2026-11-14', '2026-11-14', 150.00, '/uploads/cursos/curso-15.jpg', false,
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Seminario'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Patricia Elena' AND apellidos = 'Salinas Rojas')),

('EJ-2026-016', 'Programa de Especialización en Derecho Notarial',
 'Formación estructurada en función notarial, fe pública y gestión de una notaría, con respaldo institucional.',
 'Colegio de Abogados de Lima Sur', 'Presencial', 90, 22, '2026-10-12', '2026-12-12', 1000.00, '/uploads/cursos/curso-16.jpg', false,
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Programa'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Yourka' AND apellidos = 'Lucich Berrío')),

('EJ-2026-017', 'Programa de Especialización en Derecho Registral',
 'Profundiza en los sistemas registrales peruanos: predios, personas jurídicas, bienes muebles y garantías mobiliarias.',
 'Colegio de Abogados de Lima', 'Virtual', 95, 25, '2026-10-19', '2026-12-19', 1050.00, '/uploads/cursos/curso-17.jpg', false,
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Programa'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Carlos Alberto' AND apellidos = 'Medina Torres')),

('EJ-2026-018', 'Programa de Especialización en Derecho Civil y Familia',
 'Aborda los principales conflictos de derecho de familia: filiación, alimentos, divorcio y régimen patrimonial.',
 'Escuela Jurídica', 'Híbrido', 85, 28, '2026-10-26', '2026-12-20', 980.00, '/uploads/cursos/curso-18.jpg', false,
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Programa'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Ximena' AND apellidos = 'Góngora Guerra')),

('EJ-2026-019', 'Programa de Especialización en Derecho Administrativo',
 'Estudia el procedimiento administrativo general y los mecanismos de defensa del administrado frente al Estado.',
 'Escuela Jurídica', 'Virtual', 80, 30, '2026-11-02', '2026-12-28', 950.00, '/uploads/cursos/curso-19.jpg', false,
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Programa'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Milagros del Pilar' AND apellidos = 'Cárdenas Vega')),

('EJ-2026-020', 'Programa de Especialización en Derecho Municipal y Gestión Pública',
 'Capacitación en gestión municipal, ordenanzas y régimen de competencias de los gobiernos locales.',
 'Academia de la Magistratura', 'Presencial', 75, 24, '2026-11-09', '2027-01-04', 900.00, '/uploads/cursos/curso-20.jpg', false,
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Programa'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Lilia Mercedes' AND apellidos = 'Guerra Macedo'));
