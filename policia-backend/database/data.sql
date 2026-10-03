-- =================================================================
-- NOTA PARA DESARROLLADORES:
-- Todos los usuarios de prueba generados en este script tienen
-- la contraseña por defecto: 1234
-- =================================================================

INSERT INTO banda (id, codigo, activo) VALUES
(1, 'BAN00001', true),
(2, 'BAN00002', true),
(3, 'BAN00003', true),
(4, 'BAN00004', true);

INSERT INTO entidad_bancaria (id, nombre, domicilio_central, activo, codigo) VALUES
(1, 'Banco del Sur', 'Av. Corrientes 1200, Buenos Aires', true, 'EBA00001'),
(2, 'Banco Federal', 'Av. Santa Fe 1800, Buenos Aires', true, 'EBA00002'),
(3, 'Banco Nacional', 'Av. Rivadavia 4500, Buenos Aires', true, 'EBA00003'),
(4, 'Banco Metropolitano', 'Av. Cabildo 2200, Buenos Aires', true, 'EBA00004');

INSERT INTO vigilante (id, codigo, nombre, password, edad, activo, rol) VALUES
(1, 'VIG00001', 'Pedro Ramirez', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy', 32, true, 'VIGILANTE'),
(2, 'VIG00002', 'Laura Fernandez', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy', 28, true, 'VIGILANTE'),
(3, 'VIG00003', 'Martin Gonzalez', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy', 41, true, 'VIGILANTE'),
(4, 'VIG00004', 'Sofia Acosta', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy', 35, true, 'VIGILANTE'),
(5, 'VIG00005', 'Diego Romero', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy', 30, true, 'VIGILANTE'),
(6, 'VIG00006', 'Carolina Silva', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy', 27, true, 'VIGILANTE'),
(7, 'VIG00007', 'Hernan Torres', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy', 45, true, 'VIGILANTE'),
(8, 'VIG00008', 'Luciana Castro', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy', 33, true, 'VIGILANTE'),
(9, 'VIG00009', 'Gustavo Medina', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy', 39, true, 'VIGILANTE'),
(10, 'VIG00010', 'Valeria Moreno', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy', 26, true, 'VIGILANTE');

INSERT INTO investigador (id, codigo, nombre, password, activo, rol) VALUES
(1, 'INV00001', 'Lionel Messi', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy', true, 'INVESTIGADOR'),
(2, 'INV00002', 'Angel Di Maria', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy', true, 'INVESTIGADOR'),
(3, 'INV00003', 'Emiliano Martinez', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy', true, 'INVESTIGADOR'),
(4, 'INV00004', 'Julian Alvarez', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy', true, 'INVESTIGADOR'),
(5, 'INV00005', 'Rodrigo De Paul', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy', true, 'INVESTIGADOR');

INSERT INTO administrador (id, activo, codigo, nombre, password, rol) VALUES
(1, true, 'ADM00001', 'Charly Garcia', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy', 'ADMINISTRADOR'),
(2, true, 'ADM00002', 'Fito Paez', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy', 'ADMINISTRADOR'),
(3, true, 'ADM00003', 'Gustavo Cerati', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy', 'ADMINISTRADOR'),
(4, true, 'ADM00004', 'Andres Calamaro', '$2a$10$OqCtZ31qaW5pW.Ny3rDybewssfEZ/cfNk3bDadlM.GiL033ckHTLy', 'ADMINISTRADOR');

INSERT INTO juez (id, nombre, juez_desde, activo, codigo) VALUES
(1, 'Alberto Fernandez', '2012-03-15', true, 'JUE00001'),
(2, 'Claudia Romero', '2015-08-20', true, 'JUE00002'),
(3, 'Ricardo Martinez', '2018-02-10', true, 'JUE00003'),
(4, 'Patricia Gomez', '2020-06-05', true, 'JUE00004');

INSERT INTO asaltante (id, codigo, nombre, banda_id, activo) VALUES
(1, 'ASS00001', 'Juan Perez', 1, true),
(2, 'ASS00002', 'Carlos Gomez', 2, true),
(3, 'ASS00003', 'Maria Lopez', 1, true),
(4, 'ASS00004', 'Copito Wojtasik', 3, true),
(5, 'ASS00005', 'Ana Martinez', 2, true),
(6, 'ASS00006', 'Asisa Santos', 4, true),
(7, 'ASS00007', 'Rolo Figueroa', 4, true),
(8, 'ASS00008', 'Pepito Gonzalez', 3, true),
(9, 'ASS00009', 'Tano Rodriguez', 2, true),
(10, 'ASS00010', 'Lucho Fernandez', 1, true),
(11, 'ASS00011', 'Nico Benitez', 4, true);

INSERT INTO sucursal (id, domicilio, cant_empleados, entidad_bancaria_id, activo, codigo) VALUES
(1, 'Av. Callao 850, Buenos Aires', 25, 1, true, 'SUC00001'),
(2, 'Av. Pueyrredon 1200, Buenos Aires', 18, 1, true, 'SUC00002'),
(3, 'Av. Belgrano 2100, Buenos Aires', 30, 1, true, 'SUC00003'),
(4, 'Av. Juan B. Justo 1500, Buenos Aires', 22, 1, true, 'SUC00004'),

(5, 'Av. Las Heras 900, Buenos Aires', 20, 2, true, 'SUC00005'),
(6, 'Av. Córdoba 2800, Buenos Aires', 27, 2, true, 'SUC00006'),
(7, 'Av. Independencia 1700, Buenos Aires', 16, 2, true, 'SUC00007'),
(8, 'Av. San Juan 2400, Buenos Aires', 24, 2, true, 'SUC00008'),

(9, 'Av. Scalabrini Ortiz 1100, Buenos Aires', 19, 3, true, 'SUC00009'),
(10, 'Av. Directorio 1300, Buenos Aires', 28, 3, true, 'SUC00010'),
(11, 'Av. Gaona 2500, Buenos Aires', 21, 3, true, 'SUC00011'),
(12, 'Av. Congreso 1900, Buenos Aires', 17, 3, true, 'SUC00012'),

(13, 'Av. Monroe 800, Buenos Aires', 23, 4, true, 'SUC00013'),
(14, 'Av. Triunvirato 3200, Buenos Aires', 29, 4, true, 'SUC00014'),
(15, 'Av. Lacroze 1400, Buenos Aires', 18, 4, true, 'SUC00015'),
(16, 'Av. Federico Lacroze 2200, Buenos Aires', 26, 4, true, 'SUC00016');

INSERT INTO contrato_vigilancia (id, fecha, con_arma, codigo, vigilante_id, sucursal_id, activo) VALUES
(1, '2026-09-01', true,  'CDV00001', 1, 1, true),
(2, '2026-09-05', false, 'CDV00002', 2, 6, true),
(3, '2026-09-10', true,  'CDV00003', 3, 10, true);

INSERT INTO asalto (id, fecha, codigo, sucursal_id) VALUES
(1, '2026-09-12', 'AST00001', 3),
(2, '2026-09-25', 'AST00002', 10);

INSERT INTO asalto_asaltante (asalto_id, asaltante_id) VALUES
(1, 1),
(2, 4),
(2, 7),
(2, 10);

INSERT INTO caso_judicial
(id, activo, codigo, condenado, tiempo_carcel, asalto_id, asaltante_id, juez_id)
VALUES
(1, false, 'CJU00001', true, 5, 1, 1, 1),
(2, true,  'CJU00002', false, 0, 2, 4, 2),
(3, false, 'CJU00003', true, 8, 2, 7, 3),
(4, false, 'CJU00004', true, 3, 2, 10, 4);
