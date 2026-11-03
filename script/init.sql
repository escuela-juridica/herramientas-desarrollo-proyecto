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
  modalidad          CHAR(1)       CHECK (modalidad IN ('V','P')),
  duracion_horas     INT,
  cupos              INT,
  fecha_inicio       DATE         NOT NULL,
  fecha_fin          DATE         NOT NULL,
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

-- 30 cursos: son los mismos del catalogo de la plataforma ESEJUR (nombres, descripciones, horas, precios, instituciones y
-- portadas), adaptados a las tablas de este sistema. Las fechas son relativas al dia en que se ejecuta el script.
-- Tipo: Diplomado / Curso Corto / Seminario / Programa. Modalidad: V = virtual, P = presencial (los hibridos). El
-- todos quedan activos (estado A). Las imagenes son uploads/cursos/curso-NN.jpg (NN = numero del codigo).
INSERT INTO curso (codigo, nombre, descripcion, institucion, modalidad, duracion_horas, cupos, fecha_inicio, fecha_fin, precio, imagen, destacado, estado, id_tipo_curso, id_admin, id_docente) VALUES

('EJ-2026-001', 'Derecho Registral y Notarial',
 'Función registral y notarial: calificación de títulos, publicidad registral, escrituras públicas y casos prácticos de inscripción.',
 'Colegio de Abogados de Lima', 'V', 120, NULL, CURRENT_DATE-90, CURRENT_DATE+60, 450.00, '/uploads/cursos/curso-01.jpg', true, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Diplomado'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Manuel Alejandro' AND apellidos = 'Ortiz Piñella')),

('EJ-2026-002', 'Contrataciones del Estado',
 'Planificación, selección, ejecución contractual y solución de controversias en la contratación pública, con análisis de casos.',
 'Colegio de Abogados de Lima', 'P', 96, 40, CURRENT_DATE-18, CURRENT_DATE+78, 380.00, '/uploads/cursos/curso-02.jpg', true, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Programa'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Milagros del Pilar' AND apellidos = 'Cárdenas Vega')),

('EJ-2026-003', 'Litigación Oral en el Proceso Penal',
 'Entrenamiento práctico en teoría del caso, alegatos, interrogatorio y contraexamen, con simulaciones de audiencia.',
 'Escuela Jurídica', 'V', 48, 35, CURRENT_DATE+10, CURRENT_DATE+66, 290.00, '/uploads/cursos/curso-03.jpg', false, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Curso Corto'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Fernando Alonso' AND apellidos = 'Quispe Mendoza')),

('EJ-2026-004', 'Derecho Laboral y Procesal Laboral',
 'Relación individual de trabajo y proceso laboral: contratación, beneficios, extinción del vínculo y audiencias de la NLPT.',
 'Colegio de Abogados de Lima Sur', 'V', 132, NULL, CURRENT_DATE-45, CURRENT_DATE+75, 520.00, '/uploads/cursos/curso-04.jpg', true, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Diplomado'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Patricia Elena' AND apellidos = 'Salinas Rojas')),

('EJ-2026-005', 'Procedimiento Administrativo General',
 'Principios, validez del acto administrativo, recursos y silencio administrativo, con casos de la gestión pública.',
 'Escuela Jurídica', 'V', 16, 100, CURRENT_DATE+6, CURRENT_DATE+7, 0.00, '/uploads/cursos/curso-05.jpg', false, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Seminario'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Milagros del Pilar' AND apellidos = 'Cárdenas Vega')),

('EJ-2026-006', 'Gestión Pública y Modernización del Estado',
 'Gestión por resultados, planeamiento, presupuesto, integridad pública y gobierno digital para el sector público.',
 'Ilustre Colegio de Abogados de Cañete', 'P', 120, 50, CURRENT_DATE+21, CURRENT_DATE+120, 490.00, '/uploads/cursos/curso-06.jpg', true, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Diplomado'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Lilia Mercedes' AND apellidos = 'Guerra Macedo')),

('EJ-2026-007', 'Derecho Notarial y Función Notarial',
 'La fe pública y la función notarial: escrituras públicas, actas notariales y asuntos no contenciosos.',
 'Escuela Jurídica', 'V', 32, NULL, CURRENT_DATE-30, CURRENT_DATE+90, 320.00, '/uploads/cursos/curso-07.jpg', false, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Curso Corto'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Jorge Luis' AND apellidos = 'Ramírez Salazar')),

('EJ-2026-008', 'Saneamiento Físico Legal de Inmuebles',
 'Estudio de títulos, cargas y gravámenes, prescripción adquisitiva y formalización de la propiedad ante el registro.',
 'Colegio de Abogados de Lima', 'V', 72, NULL, CURRENT_DATE-20, CURRENT_DATE+60, 290.00, '/uploads/cursos/curso-08.jpg', true, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Programa'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Carlos Alberto' AND apellidos = 'Medina Torres')),

('EJ-2026-009', 'Redacción Jurídica Profesional',
 'Escribe con claridad y precisión: estructura de escritos y resoluciones, corrección de estilo y errores frecuentes.',
 'Escuela Jurídica', 'V', 20, NULL, CURRENT_DATE-15, CURRENT_DATE+105, 0.00, '/uploads/cursos/curso-09.jpg', false, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Curso Corto'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Ximena' AND apellidos = 'Góngora Guerra')),

('EJ-2026-010', 'Tributación Empresarial',
 'Impuesto a la Renta, IGV, comprobantes de pago, fiscalización y planificación tributaria para reducir contingencias.',
 'Escuela Jurídica', 'V', 40, NULL, CURRENT_DATE-60, CURRENT_DATE+60, 280.00, '/uploads/cursos/curso-10.jpg', false, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Programa'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Rosario' AND apellidos = 'Guerra Macedo')),

('EJ-2026-011', 'Arbitraje en Contrataciones Públicas',
 'Convenio arbitral, constitución del tribunal, laudo, anulación y ejecución en controversias con el Estado.',
 'Colegio de Abogados de Lima', 'P', 64, 45, CURRENT_DATE+35, CURRENT_DATE+105, 350.00, '/uploads/cursos/curso-11.jpg', false, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Curso Corto'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Lilia Mercedes' AND apellidos = 'Guerra Macedo')),

('EJ-2026-012', 'Derecho Urbanístico',
 'Zonificación, parámetros urbanísticos, habilitaciones urbanas y licencias de edificación, con casos de gobiernos locales.',
 'Colegio de Abogados de Lima Sur', 'V', 72, NULL, CURRENT_DATE-50, CURRENT_DATE+45, 390.00, '/uploads/cursos/curso-12.jpg', true, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Programa'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Carlos Alberto' AND apellidos = 'Medina Torres')),

('EJ-2026-013', 'Argumentación Jurídica',
 'Construye y evalúa argumentos jurídicos: falacias frecuentes, argumentación en escritos y audiencias, y refutación.',
 'Escuela Jurídica', 'V', 18, NULL, CURRENT_DATE-10, CURRENT_DATE+30, 0.00, '/uploads/cursos/curso-13.jpg', false, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Seminario'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Ximena' AND apellidos = 'Góngora Guerra')),

('EJ-2026-014', 'Innovación y Gestión Pública',
 'Dos jornadas de conferencias sobre modernización, innovación, integridad y gobierno digital en el sector público.',
 'Escuela Jurídica', 'V', 16, 200, CURRENT_DATE+45, CURRENT_DATE+46, 180.00, '/uploads/cursos/curso-14.jpg', false, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Seminario'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Lilia Mercedes' AND apellidos = 'Guerra Macedo')),

('EJ-2026-015', 'Procedimiento Administrativo en la Entidad',
 'Aplicación del procedimiento administrativo general a los trámites, expedientes y notificaciones de una entidad.',
 'Escuela Jurídica', 'V', 24, 30, CURRENT_DATE+45, CURRENT_DATE+60, 900.00, '/uploads/cursos/curso-15.jpg', false, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Programa'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Milagros del Pilar' AND apellidos = 'Cárdenas Vega')),

('EJ-2026-016', 'Delitos contra la Administración Pública',
 'Cohecho, colusión, peculado y negociación incompatible, con su investigación y defensa en el proceso penal.',
 'Colegio de Abogados de Lima', 'P', 120, 60, CURRENT_DATE+25, CURRENT_DATE+115, 540.00, '/uploads/cursos/curso-16.jpg', false, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Diplomado'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Fernando Alonso' AND apellidos = 'Quispe Mendoza')),

('EJ-2026-017', 'Proceso Civil: Estrategia y Práctica Forense',
 'Del escrito de demanda a la ejecución de la sentencia, con medidas cautelares y modelos de escritos listos para adaptar.',
 'Escuela Jurídica', 'V', 40, NULL, CURRENT_DATE-12, CURRENT_DATE+108, 300.00, '/uploads/cursos/curso-17.jpg', false, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Curso Corto'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Lilia Mercedes' AND apellidos = 'Guerra Macedo')),

('EJ-2026-018', 'Contratos y Responsabilidad Civil',
 'Teoría del contrato y responsabilidad civil: validez del acto jurídico, incumplimiento y reparación del daño.',
 'Colegio de Abogados de Lima Sur', 'V', 80, NULL, CURRENT_DATE-25, CURRENT_DATE+50, 280.00, '/uploads/cursos/curso-18.jpg', false, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Programa'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Rosario' AND apellidos = 'Guerra Macedo')),

('EJ-2026-019', 'Procesos Constitucionales: Amparo y Hábeas Corpus',
 'Hábeas corpus, amparo, hábeas data y cumplimiento: requisitos de procedencia y jurisprudencia del Tribunal Constitucional.',
 'Escuela Jurídica', 'V', 16, 120, CURRENT_DATE+12, CURRENT_DATE+13, 0.00, '/uploads/cursos/curso-19.jpg', false, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Seminario'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Milagros del Pilar' AND apellidos = 'Cárdenas Vega')),

('EJ-2026-020', 'Compliance y Responsabilidad de la Persona Jurídica',
 'Modelos de prevención de delitos en la empresa: mapa de riesgos, canal de denuncias y supervisión del cumplimiento.',
 'Escuela Jurídica', 'V', 48, NULL, CURRENT_DATE-8, CURRENT_DATE+112, 340.00, '/uploads/cursos/curso-20.jpg', false, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Programa'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Renzo Gabriel' AND apellidos = 'Huamán Paredes')),

('EJ-2026-021', 'Derecho Societario y Gobierno Corporativo',
 'Tipos societarios, órganos de gobierno, deberes de los administradores y reorganización de sociedades.',
 'Colegio de Abogados de Lima', 'P', 128, 50, CURRENT_DATE+40, CURRENT_DATE+130, 590.00, '/uploads/cursos/curso-21.jpg', false, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Diplomado'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Ximena' AND apellidos = 'Góngora Guerra')),

('EJ-2026-022', 'Seguridad Social y Régimen Pensionario',
 'Salud, pensiones y riesgos del trabajo: Sistema Nacional y Privado de Pensiones, requisitos y reclamo de prestaciones.',
 'Escuela Jurídica', 'V', 36, NULL, CURRENT_DATE-20, CURRENT_DATE+100, 260.00, '/uploads/cursos/curso-22.jpg', false, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Curso Corto'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Patricia Elena' AND apellidos = 'Salinas Rojas')),

('EJ-2026-023', 'Negociación Colectiva y Relaciones Sindicales',
 'Pliego de reclamos, estrategias de negociación y convención colectiva, con simulaciones de mesa de negociación.',
 'Escuela Jurídica', 'V', 12, 40, CURRENT_DATE+15, CURRENT_DATE+16, 150.00, '/uploads/cursos/curso-23.jpg', false, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Seminario'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Patricia Elena' AND apellidos = 'Salinas Rojas')),

('EJ-2026-024', 'Obras Públicas: Ejecución, Valorizaciones y Liquidación',
 'Cuaderno de obra, adicionales, ampliaciones de plazo, valorizaciones, recepción y liquidación del contrato de obra.',
 'Colegio de Abogados de Lima', 'P', 96, 45, CURRENT_DATE-5, CURRENT_DATE+85, 340.00, '/uploads/cursos/curso-24.jpg', false, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Programa'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Manuel Alejandro' AND apellidos = 'Ortiz Piñella')),

('EJ-2026-025', 'Tributación Municipal y Cobranza Coactiva',
 'Impuesto predial, alcabala, arbitrios y licencias; cobranza coactiva y defensa del contribuyente.',
 'Escuela Jurídica', 'V', 40, NULL, CURRENT_DATE-35, CURRENT_DATE+85, 290.00, '/uploads/cursos/curso-25.jpg', false, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Curso Corto'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Rosario' AND apellidos = 'Guerra Macedo')),

('EJ-2026-026', 'Conciliación Extrajudicial y Mediación',
 'Conciliación extrajudicial: rol del conciliador, técnicas de comunicación y negociación, y redacción del acta de acuerdo.',
 'Escuela Jurídica', 'V', 16, NULL, CURRENT_DATE-5, CURRENT_DATE+25, 0.00, '/uploads/cursos/curso-26.jpg', false, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Curso Corto'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Ximena' AND apellidos = 'Góngora Guerra')),

('EJ-2026-027', 'Oratoria y Comunicación Jurídica',
 'Manejo de la voz y el lenguaje corporal, estructura del discurso y técnicas para alegatos e informes orales.',
 'Escuela Jurídica', 'V', 12, 40, CURRENT_DATE+18, CURRENT_DATE+19, 120.00, '/uploads/cursos/curso-27.jpg', false, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Curso Corto'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Fernando Alonso' AND apellidos = 'Quispe Mendoza')),

('EJ-2026-028', 'Catastro y Registro de Predios',
 'Identificación y georreferenciación de predios, planos, independizaciones y corrección de discrepancias registrales.',
 'Colegio de Abogados de Lima', 'V', 44, NULL, CURRENT_DATE-40, CURRENT_DATE+80, 250.00, '/uploads/cursos/curso-28.jpg', false, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Curso Corto'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Carlos Alberto' AND apellidos = 'Medina Torres')),

('EJ-2026-029', 'Derecho Ambiental y Evaluación de Impacto',
 'Principios ambientales, evaluación de impacto, fiscalización, sanciones y responsabilidad por daño ambiental.',
 'Ilustre Colegio de Abogados de Cañete', 'V', 72, NULL, CURRENT_DATE+30, CURRENT_DATE+100, 380.00, '/uploads/cursos/curso-29.jpg', false, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Programa'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Milagros del Pilar' AND apellidos = 'Cárdenas Vega')),

('EJ-2026-030', 'Congreso Nacional de Derecho Notarial y Registral',
 'Conferencias y paneles sobre tecnología en la fe pública, seguridad jurídica inmobiliaria y criterios del Tribunal Registral.',
 'Colegio de Abogados de Lima', 'V', 16, 250, CURRENT_DATE+60, CURRENT_DATE+61, 150.00, '/uploads/cursos/curso-30.jpg', false, 'A',
 (SELECT id_tipo_curso FROM tipo_curso WHERE nombre = 'Seminario'),
 (SELECT id_admin FROM administrador LIMIT 1),
 (SELECT id_docente FROM docente WHERE nombres = 'Yourka' AND apellidos = 'Lucich Berrío'));
