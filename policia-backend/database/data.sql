-- =================================================================
-- NOTA PARA DESARROLLADORES:
-- Todos los usuarios de prueba generados en este script tienen
-- la contraseña por defecto: 1234
-- =================================================================

INSERT INTO banda (id, codigo, activo, creado_por, fecha_creacion) VALUES
(1, 'BAN00001', true, 'SISTEMA', NOW()),
(2, 'BAN00002', true, 'SISTEMA', NOW()),
(3, 'BAN00003', true, 'SISTEMA', NOW()),
(4, 'BAN00004', true, 'SISTEMA', NOW());

INSERT INTO entidad_bancaria (id, nombre, domicilio_central, activo, codigo, creado_por, fecha_creacion) VALUES
(1, 'Banco del Sur', 'Av. Corrientes 1200, Buenos Aires', true, 'EBA00001', 'SISTEMA', NOW()),
(2, 'Banco Federal', 'Av. Santa Fe 1800, Buenos Aires', true, 'EBA00002', 'SISTEMA', NOW()),
(3, 'Banco Nacional', 'Av. Rivadavia 4500, Buenos Aires', true, 'EBA00003', 'SISTEMA', NOW()),
(4, 'Banco Metropolitano', 'Av. Cabildo 2200, Buenos Aires', true, 'EBA00004', 'SISTEMA', NOW());

-- =========================================================
-- VIGILANTES
-- =========================================================

INSERT INTO usuario (activo, fecha_creacion, rol_id, codigo, creado_por, nombre, password) VALUES
(true, NOW(), 3, 'VIG00001', 'SISTEMA', 'Pedro Ramirez',
 '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy');
SET @id = LAST_INSERT_ID();
INSERT INTO vigilante (id, edad) VALUES (@id, 32);

INSERT INTO usuario
(activo, fecha_creacion, rol_id, codigo, creado_por, nombre, password)
VALUES
(true, NOW(), 3, 'VIG00002', 'SISTEMA', 'Laura Fernandez',
 '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy');
SET @id = LAST_INSERT_ID();
INSERT INTO vigilante (id, edad) VALUES (@id, 28);

INSERT INTO usuario
(activo, fecha_creacion, rol_id, codigo, creado_por, nombre, password)
VALUES
(true, NOW(), 3, 'VIG00003', 'SISTEMA', 'Martin Gonzalez',
 '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy');
SET @id = LAST_INSERT_ID();
INSERT INTO vigilante (id, edad) VALUES (@id, 41);

INSERT INTO usuario
(activo, fecha_creacion, rol_id, codigo, creado_por, nombre, password)
VALUES
(true, NOW(), 3, 'VIG00004', 'SISTEMA', 'Sofia Acosta',
 '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy');
SET @id = LAST_INSERT_ID();
INSERT INTO vigilante (id, edad) VALUES (@id, 35);

INSERT INTO usuario
(activo, fecha_creacion, rol_id, codigo, creado_por, nombre, password)
VALUES
(true, NOW(), 3, 'VIG00005', 'SISTEMA', 'Diego Romero',
 '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy');
SET @id = LAST_INSERT_ID();
INSERT INTO vigilante (id, edad) VALUES (@id, 30);

INSERT INTO usuario
(activo, fecha_creacion, rol_id, codigo, creado_por, nombre, password)
VALUES
(true, NOW(), 3, 'VIG00006', 'SISTEMA', 'Carolina Silva',
 '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy');
SET @id = LAST_INSERT_ID();
INSERT INTO vigilante (id, edad) VALUES (@id, 27);

INSERT INTO usuario
(activo, fecha_creacion, rol_id, codigo, creado_por, nombre, password)
VALUES
(true, NOW(), 3, 'VIG00007', 'SISTEMA', 'Hernan Torres',
 '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy');
SET @id = LAST_INSERT_ID();
INSERT INTO vigilante (id, edad) VALUES (@id, 45);

INSERT INTO usuario
(activo, fecha_creacion, rol_id, codigo, creado_por, nombre, password)
VALUES
(true, NOW(), 3, 'VIG00008', 'SISTEMA', 'Luciana Castro',
 '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy');
SET @id = LAST_INSERT_ID();
INSERT INTO vigilante (id, edad) VALUES (@id, 33);

INSERT INTO usuario
(activo, fecha_creacion, rol_id, codigo, creado_por, nombre, password)
VALUES
(true, NOW(), 3, 'VIG00009', 'SISTEMA', 'Gustavo Medina',
 '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy');
SET @id = LAST_INSERT_ID();
INSERT INTO vigilante (id, edad) VALUES (@id, 39);

INSERT INTO usuario
(activo, fecha_creacion, rol_id, codigo, creado_por, nombre, password)
VALUES
(true, NOW(), 3, 'VIG00010', 'SISTEMA', 'Valeria Moreno',
 '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy');
SET @id = LAST_INSERT_ID();
INSERT INTO vigilante (id, edad) VALUES (@id, 26);


-- =========================================================
-- INVESTIGADORES
-- =========================================================

INSERT INTO usuario
(activo, fecha_creacion, rol_id, codigo, creado_por, nombre, password)
VALUES
(true, NOW(), 2, 'INV00001', 'SISTEMA', 'Lionel Messi',
 '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy');
SET @id = LAST_INSERT_ID();
INSERT INTO investigador (id) VALUES (@id);

INSERT INTO usuario
(activo, fecha_creacion, rol_id, codigo, creado_por, nombre, password)
VALUES
(true, NOW(), 2, 'INV00002', 'SISTEMA', 'Angel Di Maria',
 '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy');
SET @id = LAST_INSERT_ID();
INSERT INTO investigador (id) VALUES (@id);

INSERT INTO usuario
(activo, fecha_creacion, rol_id, codigo, creado_por, nombre, password)
VALUES
(true, NOW(), 2, 'INV00003', 'SISTEMA', 'Emiliano Martinez',
 '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy');
SET @id = LAST_INSERT_ID();
INSERT INTO investigador (id) VALUES (@id);

INSERT INTO usuario
(activo, fecha_creacion, rol_id, codigo, creado_por, nombre, password)
VALUES
(true, NOW(), 2, 'INV00004', 'SISTEMA', 'Julian Alvarez',
 '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy');
SET @id = LAST_INSERT_ID();
INSERT INTO investigador (id) VALUES (@id);

INSERT INTO usuario
(activo, fecha_creacion, rol_id, codigo, creado_por, nombre, password)
VALUES
(true, NOW(), 2, 'INV00005', 'SISTEMA', 'Rodrigo De Paul',
 '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy');
SET @id = LAST_INSERT_ID();
INSERT INTO investigador (id) VALUES (@id);


-- =========================================================
-- ADMINISTRADORES
-- =========================================================

INSERT INTO usuario
(activo, fecha_creacion, rol_id, codigo, creado_por, nombre, password)
VALUES
(true, NOW(), 1, 'ADM00001', 'SISTEMA', 'Charly Garcia',
 '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy');
SET @id = LAST_INSERT_ID();
INSERT INTO administrador (id) VALUES (@id);

INSERT INTO usuario
(activo, fecha_creacion, rol_id, codigo, creado_por, nombre, password)
VALUES
(true, NOW(), 1, 'ADM00002', 'SISTEMA', 'Fito Paez',
 '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy');
SET @id = LAST_INSERT_ID();
INSERT INTO administrador (id) VALUES (@id);

INSERT INTO usuario
(activo, fecha_creacion, rol_id, codigo, creado_por, nombre, password)
VALUES
(true, NOW(), 1, 'ADM00003', 'SISTEMA', 'Gustavo Cerati',
 '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy');
SET @id = LAST_INSERT_ID();
INSERT INTO administrador (id) VALUES (@id);

INSERT INTO usuario
(activo, fecha_creacion, rol_id, codigo, creado_por, nombre, password)
VALUES
(true, NOW(), 1, 'ADM00004', 'SISTEMA', 'Andres Calamaro',
 '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy');
SET @id = LAST_INSERT_ID();
INSERT INTO administrador (id) VALUES (@id);

INSERT INTO juez (id, nombre, juez_desde, activo, codigo, creado_por, fecha_creacion) VALUES
(1, 'Alberto Fernandez', '2012-03-15', true, 'JUE00001', 'SISTEMA', NOW()),
(2, 'Claudia Romero', '2015-08-20', true, 'JUE00002', 'SISTEMA', NOW()),
(3, 'Ricardo Martinez', '2018-02-10', true, 'JUE00003', 'SISTEMA', NOW()),
(4, 'Patricia Gomez', '2020-06-05', true, 'JUE00004', 'SISTEMA', NOW());

INSERT INTO asaltante (id, codigo, nombre, banda_id, activo, creado_por, fecha_creacion) VALUES
(1, 'ASS00001', 'Copito Wojtasik', 1, true, 'SISTEMA', NOW()),
(2, 'ASS00002', 'Carlos Gomez', 2, true, 'SISTEMA', NOW()),
(3, 'ASS00003', 'Maria Lopez', 1, true, 'SISTEMA', NOW()),
(4, 'ASS00004', 'Tota Bolla', 3, true, 'SISTEMA', NOW()),
(5, 'ASS00005', 'Duque Mendizabal', 2, true, 'SISTEMA', NOW()),
(6, 'ASS00006', 'Asisa Santos', 4, true, 'SISTEMA', NOW()),
(7, 'ASS00007', 'Rolo Figueroa', 4, true, 'SISTEMA', NOW()),
(8, 'ASS00008', 'Pepito Gonzalez', 3, true, 'SISTEMA', NOW()),
(9, 'ASS00009', 'Tano Rodriguez', 2, true, 'SISTEMA', NOW()),
(10, 'ASS00010', 'Emma Ancans', 1, true, 'SISTEMA', NOW()),
(11, 'ASS00011', 'Nico Benitez', 4, true, 'SISTEMA', NOW());

INSERT INTO sucursal (id, domicilio, cant_empleados, entidad_bancaria_id, activo, codigo, creado_por, fecha_creacion) VALUES
(1, 'Av. Callao 850, Buenos Aires', 25, 1, true, 'SUC00001', 'SISTEMA', NOW()),
(2, 'Av. Pueyrredon 1200, Buenos Aires', 18, 1, true, 'SUC00002', 'SISTEMA', NOW()),
(3, 'Av. Belgrano 2100, Buenos Aires', 30, 1, true, 'SUC00003', 'SISTEMA', NOW()),
(4, 'Av. Juan B. Justo 1500, Buenos Aires', 22, 1, true, 'SUC00004', 'SISTEMA', NOW()),
(5, 'Av. Las Heras 900, Buenos Aires', 20, 2, true, 'SUC00005', 'SISTEMA', NOW()),
(6, 'Av. Córdoba 2800, Buenos Aires', 27, 2, true, 'SUC00006', 'SISTEMA', NOW()),
(7, 'Av. Independencia 1700, Buenos Aires', 16, 2, true, 'SUC00007', 'SISTEMA', NOW()),
(8, 'Av. San Juan 2400, Buenos Aires', 24, 2, true, 'SUC00008', 'SISTEMA', NOW()),
(9, 'Av. Scalabrini Ortiz 1100, Buenos Aires', 19, 3, true, 'SUC00009', 'SISTEMA', NOW()),
(10, 'Av. Directorio 1300, Buenos Aires', 28, 3, true, 'SUC00010', 'SISTEMA', NOW()),
(11, 'Av. Gaona 2500, Buenos Aires', 21, 3, true, 'SUC00011', 'SISTEMA', NOW()),
(12, 'Av. Congreso 1900, Buenos Aires', 17, 3, true, 'SUC00012', 'SISTEMA', NOW()),
(13, 'Av. Monroe 800, Buenos Aires', 23, 4, true, 'SUC00013', 'SISTEMA', NOW()),
(14, 'Av. Triunvirato 3200, Buenos Aires', 29, 4, true, 'SUC00014', 'SISTEMA', NOW()),
(15, 'Av. Lacroze 1400, Buenos Aires', 18, 4, true, 'SUC00015', 'SISTEMA', NOW()),
(16, 'Av. Federico Lacroze 2200, Buenos Aires', 26, 4, true, 'SUC00016', 'SISTEMA', NOW());

INSERT INTO contrato_vigilancia (id, fecha, con_arma, codigo, vigilante_id, sucursal_id, activo, creado_por, fecha_creacion) VALUES
(1, '2026-09-01', true,  'CDV00001', 1, 1, true, 'SISTEMA', NOW()),
(2, '2026-09-05', false, 'CDV00002', 2, 6, true, 'SISTEMA', NOW()),
(3, '2026-09-10', true,  'CDV00003', 3, 10, true, 'SISTEMA', NOW());

INSERT INTO asalto (id, fecha, codigo, sucursal_id, creado_por, fecha_creacion) VALUES
(1, '2026-09-12', 'AST00001', 3, 'SISTEMA', NOW()),
(2, '2026-09-25', 'AST00002', 10, 'SISTEMA', NOW());

INSERT INTO asalto_asaltante (asalto_id, asaltante_id) VALUES
(1, 1),
(2, 4),
(2, 7),
(2, 10);

INSERT INTO caso_judicial (activo, codigo, condenado, sentenciado, tiempo_carcel, asalto_id, juez_id, creado_por, fecha_creacion) VALUES
(false, 'CJU00001', true,  true,  5, 1, 1, 'SISTEMA', NOW()),
(true,  'CJU00002', false, false, 0, 2, 2, 'SISTEMA', NOW()),
(false, 'CJU00003', true,  true,  8, 2, 3, 'SISTEMA', NOW()),
(false, 'CJU00004', true,  true,  3, 2, 4, 'SISTEMA', NOW());

INSERT INTO caso_judicial_asaltantes (caso_judicial_id, asaltantes_id) VALUES
(1, 1),
(2, 4),
(3, 7),
(4, 10);

INSERT INTO rol
(activo, fecha_creacion, codigo, creado_por, descripcion, nombre, prefijo)
VALUES
(true, NOW(), 'ROL00001', 'SISTEMA',
 'Rol con permisos administrativos del sistema',
 'ADMINISTRADOR', 'ADM'),

(true, NOW(), 'ROL00002', 'SISTEMA',
 'Rol para personal de investigación',
 'INVESTIGADOR', 'INV'),

(true, NOW(), 'ROL00003', 'SISTEMA',
 'Rol para personal de vigilancia',
 'VIGILANTE', 'VIG');
 

INSERT INTO rol_permiso (rol_id, permiso) VALUES
-- ADMINISTRADOR
(1, 'ESCRITURA'),
(1, 'LECTURA'),

-- INVESTIGADOR
(2, 'LECTURA'),

-- VIGILANTE
(3, 'LECTURA_DATOS_PROPIOS');