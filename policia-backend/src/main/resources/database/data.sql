-- =================================================================
-- NOTA PARA DESARROLLADORES:
-- Todos los usuarios de prueba generados en este script tienen
-- la contraseña por defecto: 1234
-- =================================================================

-- =========================================================
-- ROLES Y PERMISOS
-- =========================================================


INSERT INTO rol (id, activo, fecha_creacion, codigo, creado_por, descripcion, nombre, prefijo) VALUES
(1, true, NOW(), 'ROL00001', 'SISTEMA', 'Rol con permisos administrativos del sistema', 'ADMINISTRADOR', 'ADM'),
(2, true, NOW(), 'ROL00002', 'SISTEMA', 'Rol para personal de investigación', 'INVESTIGADOR', 'INV'),
(3, true, NOW(), 'ROL00003', 'SISTEMA', 'Rol para personal de vigilancia', 'VIGILANTE', 'VIG');


INSERT INTO rol_permiso (rol_id, permiso) VALUES
(1, 'ESCRITURA'),
(1, 'LECTURA'),
(2, 'LECTURA'),
(3, 'LECTURA_DATOS_PROPIOS');


-- =========================================================
-- BANDAS
-- =========================================================


INSERT INTO banda (id, codigo, activo, creado_por, fecha_creacion) VALUES
(1, 'BAN00001', true, 'SISTEMA', NOW()),
(2, 'BAN00002', true, 'SISTEMA', NOW()),
(3, 'BAN00003', true, 'SISTEMA', NOW()),
(4, 'BAN00004', true, 'SISTEMA', NOW()),
(5, 'BAN00005', true, 'SISTEMA', NOW()),
(6, 'BAN00006', true, 'SISTEMA', NOW());


-- =========================================================
-- ENTIDADES BANCARIAS
-- =========================================================

INSERT INTO entidad_bancaria (id, nombre, domicilio_central, activo, codigo, creado_por, fecha_creacion) VALUES
(1, 'Banco del Sur', 'Av. Corrientes 1200, Buenos Aires', true, 'EBA00001', 'SISTEMA', NOW()),
(2, 'Banco Federal', 'Av. Santa Fe 1800, Buenos Aires', true, 'EBA00002', 'SISTEMA', NOW()),
(3, 'Banco Nacional', 'Av. Rivadavia 4500, Buenos Aires', true, 'EBA00003', 'SISTEMA', NOW()),
(4, 'Banco Metropolitano', 'Av. Cabildo 2200, Buenos Aires', true, 'EBA00004', 'SISTEMA', NOW()),
(5, 'Banco del Plata', 'Av. de Mayo 900, Buenos Aires', true, 'EBA00005', 'SISTEMA', NOW()),
(6, 'Banco Horizonte', 'Av. San Martin 3100, Buenos Aires', true, 'EBA00006', 'SISTEMA', NOW());


-- Usuarios: herencia JOINED.

-- =========================================================
-- VIGILANTES
-- =========================================================

INSERT INTO usuario (id, activo, fecha_creacion, rol_id, codigo, creado_por, nombre, password) VALUES
(1, true, NOW(), 3, 'VIG00001', 'SISTEMA', 'Pedro Ramirez', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(2, true, NOW(), 3, 'VIG00002', 'SISTEMA', 'Laura Fernandez', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(3, true, NOW(), 3, 'VIG00003', 'SISTEMA', 'Martin Gonzalez', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(4, true, NOW(), 3, 'VIG00004', 'SISTEMA', 'Sofia Acosta', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(5, true, NOW(), 3, 'VIG00005', 'SISTEMA', 'Diego Romero', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(6, true, NOW(), 3, 'VIG00006', 'SISTEMA', 'Carolina Silva', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(7, true, NOW(), 3, 'VIG00007', 'SISTEMA', 'Hernan Torres', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(8, true, NOW(), 3, 'VIG00008', 'SISTEMA', 'Luciana Castro', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(9, true, NOW(), 3, 'VIG00009', 'SISTEMA', 'Gustavo Medina', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(10, true, NOW(), 3, 'VIG00010', 'SISTEMA', 'Valeria Moreno', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(11, true, NOW(), 3, 'VIG00011', 'SISTEMA', 'Nicolas Cabrera', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(12, true, NOW(), 3, 'VIG00012', 'SISTEMA', 'Camila Suarez', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(13, true, NOW(), 3, 'VIG00013', 'SISTEMA', 'Federico Molina', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(14, true, NOW(), 3, 'VIG00014', 'SISTEMA', 'Agustina Rojas', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(15, true, NOW(), 3, 'VIG00015', 'SISTEMA', 'Marcos Benitez', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(26, true, NOW(), 3, 'VIG00016', 'SISTEMA', 'Lucas Herrera', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(27, true, NOW(), 3, 'VIG00017', 'SISTEMA', 'Valentina Lopez', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(28, true, NOW(), 3, 'VIG00018', 'SISTEMA', 'Matias Fernandez', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(29, true, NOW(), 3, 'VIG00019', 'SISTEMA', 'Julieta Castro', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(30, true, NOW(), 3, 'VIG00020', 'SISTEMA', 'Tomas Medina', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(31, true, NOW(), 3, 'VIG00021', 'SISTEMA', 'Martina Rojas', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(32, true, NOW(), 3, 'VIG00022', 'SISTEMA', 'Facundo Perez', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(33, true, NOW(), 3, 'VIG00023', 'SISTEMA', 'Agustina Torres', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(34, true, NOW(), 3, 'VIG00024', 'SISTEMA', 'Nicolas Romero', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(35, true, NOW(), 3, 'VIG00025', 'SISTEMA', 'Florencia Diaz', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy');


INSERT INTO vigilante (id, edad) VALUES
(1, 32),
(2, 28),
(3, 41),
(4, 35),
(5, 30),
(6, 27),
(7, 45),
(8, 33),
(9, 39),
(10, 26),
(11, 34),
(12, 29),
(13, 42),
(14, 31),
(15, 37),
(26, 36),
(27, 29),
(28, 41),
(29, 32),
(30, 27),
(31, 38),
(32, 44),
(33, 30),
(34, 35),
(35, 33);

-- =========================================================
-- INVESTIGADORES
-- =========================================================

INSERT INTO usuario (id, activo, fecha_creacion, rol_id, codigo, creado_por, nombre, password) VALUES
(16, true, NOW(), 2, 'INV00001', 'SISTEMA', 'Lionel Messi', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(17, true, NOW(), 2, 'INV00002', 'SISTEMA', 'Angel Di Maria', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(18, true, NOW(), 2, 'INV00003', 'SISTEMA', 'Emiliano Martinez', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(19, true, NOW(), 2, 'INV00004', 'SISTEMA', 'Julian Alvarez', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(20, true, NOW(), 2, 'INV00005', 'SISTEMA', 'Rodrigo De Paul', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(21, true, NOW(), 2, 'INV00006', 'SISTEMA', 'Enzo Fernandez', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy');


-- =========================================================
-- ADMINISTRADORES
-- =========================================================

INSERT INTO usuario (id, activo, fecha_creacion, rol_id, codigo, creado_por, nombre, password) VALUES
(22, true, NOW(), 1, 'ADM00001', 'SISTEMA', 'Charly Garcia', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(23, true, NOW(), 1, 'ADM00002', 'SISTEMA', 'Fito Paez', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(24, true, NOW(), 1, 'ADM00003', 'SISTEMA', 'Gustavo Cerati', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy'),
(25, true, NOW(), 1, 'ADM00004', 'SISTEMA', 'Andres Calamaro', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy');

INSERT INTO usuario_generico (id) VALUES
(16),
(17),
(18),
(19),
(20),
(21),
(22),
(23),
(24),
(25);


-- =========================================================
-- JUECES
-- =========================================================

INSERT INTO juez (id, nombre, juez_desde, activo, codigo, creado_por, fecha_creacion) VALUES
(1, 'Alberto Fernandez', '2012-03-15', true, 'JUE00001', 'SISTEMA', NOW()),
(2, 'Claudia Romero', '2015-08-20', true, 'JUE00002', 'SISTEMA', NOW()),
(3, 'Ricardo Martinez', '2018-02-10', true, 'JUE00003', 'SISTEMA', NOW()),
(4, 'Patricia Gomez', '2020-06-05', true, 'JUE00004', 'SISTEMA', NOW()),
(5, 'Mariano Salvatierra', '2011-11-18', true, 'JUE00005', 'SISTEMA', NOW()),
(6, 'Elena Quiroga', '2019-04-22', true, 'JUE00006', 'SISTEMA', NOW());


-- =========================================================
-- ASALTANTES (CON Y SIN BANDA)
-- =========================================================

INSERT INTO asaltante (id, codigo, nombre, banda_id, activo, motivo_baja, creado_por, fecha_creacion) VALUES
(1, 'ASS00001', 'Copito Wojtasik', 1, true, NULL, 'SISTEMA', NOW()),
(2, 'ASS00002', 'Coli Norte', 2, true, NULL, 'SISTEMA', NOW()),
(3, 'ASS00003', 'Asisa Santos', 1, true, NULL, 'SISTEMA', NOW()),
(4, 'ASS00004', 'Tota Bolla', 3, true, NULL, 'SISTEMA', NOW()),
(5, 'ASS00005', 'Duque Mendizabal', 2, true, NULL, 'SISTEMA', NOW()),
(6, 'ASS00006', 'Leia North', 4, true, NULL, 'SISTEMA', NOW()),
(7, 'ASS00007', 'Isis Seiler', 4, true, NULL, 'SISTEMA', NOW()),
(8, 'ASS00008', 'Pepito Gonzalez', 3, true, NULL, 'SISTEMA', NOW()),
(9, 'ASS00009', 'Cony Nicastro', 2, true, NULL, 'SISTEMA', NOW()),
(10, 'ASS00010', 'Emma Ancans', 1, true, NULL, 'SISTEMA', NOW()),
(11, 'ASS00011', 'Nico Benitez', 4, false, 'FALLECIMIENTO', 'SISTEMA', NOW()),
(12, 'ASS00012', 'Kiara Huelmo', 5, true, NULL, 'SISTEMA', NOW()),
(13, 'ASS00013', 'Lima Della Rolle', 6, true, NULL, 'SISTEMA', NOW()),
(14, 'ASS00014', 'Matias Aguirre', 5, true, NULL, 'SISTEMA', NOW()),
(15, 'ASS00015', 'Florencia Vega', 6, true, NULL, 'SISTEMA', NOW()),
(16, 'ASS00016', 'Ezequiel Navarro', 3, true, NULL, 'SISTEMA', NOW()),
(17, 'ASS00017', 'Brenda Castillo', 5, true, NULL, 'SISTEMA', NOW()),
(18, 'ASS00018', 'Joaquin Paredes', 2, true, NULL, 'SISTEMA', NOW()),
(19, 'ASS00019', 'Milagros Acosta', 6, true, NULL, 'SISTEMA', NOW()),
(20, 'ASS00020', 'Santiago Mendez', 1, true, NULL, 'SISTEMA', NOW()),
(21, 'ASS00021', 'Rocio Farias', 4, true, NULL, 'SISTEMA', NOW()),
(22, 'ASS00022', 'Ivan Peralta', 5, true, NULL, 'SISTEMA', NOW()),
(23, 'ASS00023', 'Tamara Luna', 3, true, NULL, 'SISTEMA', NOW()),
(24, 'ASS00024', 'Facundo Arias', 6, true, NULL, 'SISTEMA', NOW()),
(25, 'ASS00025', 'Micaela Herrera', 2, true, NULL, 'SISTEMA', NOW()),
(26, 'ASS00026', 'Leandro Ortiz', 1, true, NULL, 'SISTEMA', NOW()),
(27, 'ASS00027', 'Noelia Campos', 5, true, NULL, 'SISTEMA', NOW()),
(28, 'ASS00028', 'Dario Salinas', 4, true, NULL, 'SISTEMA', NOW()),
(29, 'ASS00029', 'Candela Duarte', 6, true, NULL, 'SISTEMA', NOW()),
(30, 'ASS00030', 'Franco Cabrera', 3, true, NULL, 'SISTEMA', NOW()),
(31, 'ASS00031', 'Gabriel Molina', NULL, true, NULL, 'SISTEMA', NOW()),
(32, 'ASS00032', 'Luciano Ferreyra', NULL, true, NULL, 'SISTEMA', NOW()),
(33, 'ASS00033', 'Camila Duarte', NULL, true, NULL, 'SISTEMA', NOW()),
(34, 'ASS00034', 'Bruno Sosa', NULL, true, NULL, 'SISTEMA', NOW()),
(35, 'ASS00035', 'Valeria Ponce', NULL, true, NULL, 'SISTEMA', NOW()),
(36, 'ASS00036', 'Thiago Morales', NULL, true, NULL, 'SISTEMA', NOW()),
(37, 'ASS00037', 'Milena Rivas', NULL, true, NULL, 'SISTEMA', NOW()),
(38, 'ASS00038', 'Jeronimo Castro', NULL, true, NULL, 'SISTEMA', NOW()),
(39, 'ASS00039', 'Antonella Vega', NULL, true, NULL, 'SISTEMA', NOW()),
(40, 'ASS00040', 'Kevin Acosta', NULL, true, NULL, 'SISTEMA', NOW()),
(41, 'ASS00041', 'Abril Navarro', NULL, true, NULL, 'SISTEMA', NOW()),
(42, 'ASS00042', 'Benjamin Herrera', NULL, true, NULL, 'SISTEMA', NOW()),
(43, 'ASS00043', 'Sofia Benitez', NULL, true, NULL, 'SISTEMA', NOW()),
(44, 'ASS00044', 'Elias Romero', NULL, true, NULL, 'SISTEMA', NOW()),
(45, 'ASS00045', 'Malena Torres', NULL, true, NULL, 'SISTEMA', NOW()),
(46, 'ASS00046', 'Franco Aguirre', NULL, true, NULL, 'SISTEMA', NOW()),
(47, 'ASS00047', 'Julieta Medina', NULL, true, NULL, 'SISTEMA', NOW()),
(48, 'ASS00048', 'Lautaro Cabrera', NULL, true, NULL, 'SISTEMA', NOW()),
(49, 'ASS00049', 'Candela Ortiz', NULL, true, NULL, 'SISTEMA', NOW()),
(50, 'ASS00050', 'Nicolas Peralta', NULL, true, NULL, 'SISTEMA', NOW());


-- =========================================================
-- SUCURSALES
-- =========================================================

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
(16, 'Av. Federico Lacroze 2200, Buenos Aires', 26, 4, true, 'SUC00016', 'SISTEMA', NOW()),
(17, 'Av. Boedo 950, Buenos Aires', 22, 5, true, 'SUC00017', 'SISTEMA', NOW()),
(18, 'Av. Medrano 1450, Buenos Aires', 15, 5, true, 'SUC00018', 'SISTEMA', NOW()),
(19, 'Av. Nazca 2700, Buenos Aires', 31, 5, true, 'SUC00019', 'SISTEMA', NOW()),
(20, 'Av. Entre Rios 1050, Buenos Aires', 20, 5, true, 'SUC00020', 'SISTEMA', NOW()),
(21, 'Av. Jujuy 1300, Buenos Aires', 24, 6, true, 'SUC00021', 'SISTEMA', NOW()),
(22, 'Av. Warnes 1900, Buenos Aires', 18, 6, true, 'SUC00022', 'SISTEMA', NOW()),
(23, 'Av. Dorrego 1750, Buenos Aires', 27, 6, true, 'SUC00023', 'SISTEMA', NOW()),
(24, 'Av. Alberdi 3600, Buenos Aires', 21, 6, true, 'SUC00024', 'SISTEMA', NOW());


-- =========================================================
-- CONTRATOS
-- =========================================================

INSERT INTO contrato_vigilancia (id, fecha, con_arma, codigo, vigilante_id, sucursal_id, activo, creado_por, fecha_creacion) VALUES
(1, '2026-09-01', true, 'CDV00001', 1, 1, true, 'SISTEMA', NOW()),
(2, '2026-09-05', false, 'CDV00002', 2, 6, true, 'SISTEMA', NOW()),
(3, '2026-09-10', true, 'CDV00003', 3, 10, true, 'SISTEMA', NOW()),
(4, '2026-09-12', false, 'CDV00004', 4, 3, true, 'SISTEMA', NOW()),
(5, '2026-09-12', true, 'CDV00005', 5, 8, true, 'SISTEMA', NOW()),
(6, '2026-09-15', false, 'CDV00006', 6, 14, true, 'SISTEMA', NOW()),
(7, '2026-09-18', true, 'CDV00007', 7, 17, true, 'SISTEMA', NOW()),
(8, '2026-09-18', false, 'CDV00008', 8, 20, true, 'SISTEMA', NOW()),
(9, '2026-09-20', true, 'CDV00009', 9, 22, true, 'SISTEMA', NOW()),
(10, '2026-09-25', false, 'CDV00010', 10, 10, true, 'SISTEMA', NOW()),
(11, '2026-09-25', true, 'CDV00011', 11, 13, true, 'SISTEMA', NOW()),
(12, '2026-09-28', false, 'CDV00012', 12, 19, true, 'SISTEMA', NOW()),
(13, '2026-10-02', true, 'CDV00013', 13, 23, true, 'SISTEMA', NOW()),
(14, '2026-10-06', false, 'CDV00014', 14, 4, true, 'SISTEMA', NOW()),
(15, '2026-10-10', true, 'CDV00015', 15, 16, true, 'SISTEMA', NOW()),
(16, '2026-10-12', false, 'CDV00016', 1, 5, true, 'SISTEMA', NOW()),
(17, '2026-10-12', true, 'CDV00017', 2, 9, true, 'SISTEMA', NOW()),
(18, '2026-10-15', false, 'CDV00018', 3, 18, true, 'SISTEMA', NOW()),
(19, '2026-10-16', true, 'CDV00019', 26, 2, true, 'SISTEMA', NOW()),
(20, '2026-10-17', false, 'CDV00020', 27, 7, true, 'SISTEMA', NOW()),
(21, '2026-10-18', true, 'CDV00021', 28, 11, true, 'SISTEMA', NOW()),
(22, '2026-10-19', false, 'CDV00022', 29, 15, true, 'SISTEMA', NOW()),
(23, '2026-10-20', true, 'CDV00023', 30, 21, true, 'SISTEMA', NOW()),
(24, '2026-10-21', false, 'CDV00024', 31, 4, true, 'SISTEMA', NOW()),
(25, '2026-10-22', true, 'CDV00025', 32, 9, true, 'SISTEMA', NOW()),
(26, '2026-10-23', false, 'CDV00026', 33, 13, true, 'SISTEMA', NOW()),
(27, '2026-10-24', true, 'CDV00027', 34, 18, true, 'SISTEMA', NOW()),
(28, '2026-10-25', false, 'CDV00028', 35, 24, true, 'SISTEMA', NOW()),
(29, '2026-11-10', true, 'CDV00029', 4, 1, true, 'SISTEMA', NOW()),
(30, '2026-11-15', false, 'CDV00030', 5, 5, true, 'SISTEMA', NOW()),
(31, '2026-11-20', true, 'CDV00031', 6, 8, true, 'SISTEMA', NOW()),
(32, '2026-11-25', false, 'CDV00032', 7, 12, true, 'SISTEMA', NOW()),
(33, '2026-11-30', true, 'CDV00033', 8, 16, true, 'SISTEMA', NOW()),
(34, '2026-12-05', false, 'CDV00034', 9, 20, true, 'SISTEMA', NOW()),
(35, '2026-12-10', true, 'CDV00035', 10, 24, true, 'SISTEMA', NOW()),
(36, '2026-12-15', false, 'CDV00036', 11, 3, true, 'SISTEMA', NOW()),
(37, '2026-12-20', true, 'CDV00037', 12, 7, true, 'SISTEMA', NOW()),
(38, '2026-12-28', false, 'CDV00038', 13, 11, true, 'SISTEMA', NOW());


-- =========================================================
-- ASALTOS
-- =========================================================

INSERT INTO asalto (id, fecha, codigo, sucursal_id, creado_por, fecha_creacion) VALUES
(1, '2026-09-12', 'AST00001', 3, 'SISTEMA', NOW()),
(2, '2026-09-25', 'AST00002', 10, 'SISTEMA', NOW()),
(3, '2026-09-18', 'AST00003', 14, 'SISTEMA', NOW()),
(4, '2026-09-20', 'AST00004', 22, 'SISTEMA', NOW()),
(5, '2026-09-28', 'AST00005', 19, 'SISTEMA', NOW()),
(6, '2026-10-02', 'AST00006', 23, 'SISTEMA', NOW()),
(7, '2026-10-06', 'AST00007', 4, 'SISTEMA', NOW()),
(8, '2026-10-10', 'AST00008', 16, 'SISTEMA', NOW()),
(9, '2026-10-14', 'AST00009', 8, 'SISTEMA', NOW()),
(10, '2026-10-20', 'AST00010', 21, 'SISTEMA', NOW());


-- =========================================================
-- ASALTANTES POR ASALTO
-- =========================================================

INSERT INTO asalto_asaltante (asalto_id, asaltante_id) VALUES
(1, 1),
(1, 3),
(1, 20),
(2, 4),
(2, 7),
(2, 10),
(2, 21),
(3, 2),
(3, 5),
(3, 18),
(3, 25),
(4, 6),
(4, 11),
(4, 22),
(5, 8),
(5, 16),
(5, 23),
(5, 30),
(6, 12),
(6, 14),
(6, 17),
(6, 27),
(7, 9),
(7, 13),
(7, 19),
(8, 15),
(8, 24),
(8, 26),
(8, 28),
(9, 3),
(9, 18),
(9, 29),
(10, 1),
(10, 5),
(10, 6),
(10, 10),
(10, 13),
(10, 17),
(10, 21),
(10, 30);


-- =========================================================
-- CASOS JUDICIALES
-- =========================================================

INSERT INTO caso_judicial (id, activo, codigo, sentenciado, asalto_id, juez_id, estado, creado_por, fecha_creacion) VALUES
(1, false, 'CJU00001', true, 1, 1, 'CERRADO', 'SISTEMA', NOW()),
(2, true, 'CJU00002', false, 2, 2, 'EN_TRAMITE', 'SISTEMA', NOW()),
(3, false, 'CJU00003', true, 3, 3, 'CERRADO', 'SISTEMA', NOW()),
(4, false, 'CJU00004', false, 4, 4, 'CERRADO', 'SISTEMA', NOW()),
(5, false, 'CJU00005', true, 5, 5, 'CERRADO', 'SISTEMA', NOW()),
(6, true, 'CJU00006', false, 6, 6, 'EN_TRAMITE', 'SISTEMA', NOW()),
(7, false, 'CJU00007', true, 7, 1, 'CERRADO', 'SISTEMA', NOW()),
(8, false, 'CJU00008', true, 8, 2, 'CERRADO', 'SISTEMA', NOW()),
(9, false, 'CJU00009', true, 9, 3, 'CERRADO', 'SISTEMA', NOW()),
(10, true, 'CJU00010', false, 10, 4, 'EN_TRAMITE', 'SISTEMA', NOW());


-- =========================================================
-- SENTENCIAS INDIVIDUALES
-- =========================================================

INSERT INTO sentencia_asaltante (caso_judicial_id, asaltante_id, sentenciado, condenado, tiempo_carcel, estado, creado_por, fecha_creacion) VALUES
(1, 1, true, true, 5, 'SENTENCIADO', 'SISTEMA', NOW()),
(1, 3, true, true, 3, 'SENTENCIADO', 'SISTEMA', NOW()),
(1, 20, true, true, 2, 'SENTENCIADO', 'SISTEMA', NOW()),
(2, 4, true, false, 0, 'SENTENCIADO', 'SISTEMA', NOW()),
(2, 7, true, true, 8, 'SENTENCIADO', 'SISTEMA', NOW()),
(2, 10, true, true, 3, 'SENTENCIADO', 'SISTEMA', NOW()),
(2, 21, false, false, 0, 'PENDIENTE', 'SISTEMA', NOW()),
(3, 2, true, true, 4, 'SENTENCIADO', 'SISTEMA', NOW()),
(3, 5, true, true, 6, 'SENTENCIADO', 'SISTEMA', NOW()),
(3, 18, true, true, 2, 'SENTENCIADO', 'SISTEMA', NOW()),
(3, 25, true, true, 5, 'SENTENCIADO', 'SISTEMA', NOW()),
(4, 6, true, true, 7, 'SENTENCIADO', 'SISTEMA', NOW()),
(4, 11, false, false, 0, 'EXTINGUIDO_POR_FALLECIMIENTO', 'SISTEMA', NOW()),
(4, 22, true, true, 1, 'SENTENCIADO', 'SISTEMA', NOW()),
(5, 8, true, true, 3, 'SENTENCIADO', 'SISTEMA', NOW()),
(5, 16, true, true, 2, 'SENTENCIADO', 'SISTEMA', NOW()),
(5, 23, true, false, 0, 'SENTENCIADO', 'SISTEMA', NOW()),
(5, 30, true, true, 4, 'SENTENCIADO', 'SISTEMA', NOW()),
(6, 12, false, false, 0, 'PENDIENTE', 'SISTEMA', NOW()),
(6, 14, false, false, 0, 'PENDIENTE', 'SISTEMA', NOW()),
(6, 17, false, false, 0, 'PENDIENTE', 'SISTEMA', NOW()),
(6, 27, false, false, 0, 'PENDIENTE', 'SISTEMA', NOW()),
(7, 9, true, true, 6, 'SENTENCIADO', 'SISTEMA', NOW()),
(7, 13, true, true, 4, 'SENTENCIADO', 'SISTEMA', NOW()),
(7, 19, true, true, 3, 'SENTENCIADO', 'SISTEMA', NOW()),
(8, 15, true, true, 5, 'SENTENCIADO', 'SISTEMA', NOW()),
(8, 24, true, true, 2, 'SENTENCIADO', 'SISTEMA', NOW()),
(8, 26, true, true, 8, 'SENTENCIADO', 'SISTEMA', NOW()),
(8, 28, true, true, 1, 'SENTENCIADO', 'SISTEMA', NOW()),
(9, 3, true, true, 2, 'SENTENCIADO', 'SISTEMA', NOW()),
(9, 18, true, false, 0, 'SENTENCIADO', 'SISTEMA', NOW()),
(9, 29, true, true, 4, 'SENTENCIADO', 'SISTEMA', NOW()),
(10, 1, true, true, 6, 'SENTENCIADO', 'SISTEMA', NOW()),
(10, 5, true, true, 3, 'SENTENCIADO', 'SISTEMA', NOW()),
(10, 6, false, false, 0, 'PENDIENTE', 'SISTEMA', NOW()),
(10, 10, true, true, 2, 'SENTENCIADO', 'SISTEMA', NOW()),
(10, 13, false, false, 0, 'PENDIENTE', 'SISTEMA', NOW()),
(10, 17, true, true, 5, 'SENTENCIADO', 'SISTEMA', NOW()),
(10, 21, false, false, 0, 'PENDIENTE', 'SISTEMA', NOW()),
(10, 30, false, false, 0, 'PENDIENTE', 'SISTEMA', NOW());
