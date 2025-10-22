-- =============================================
-- DADOS REALISTAS BASEADOS NO ENUNCIADO MEDWAY
-- =============================================

-- 1. OWNERS (Proprietários de infraestrutura)
INSERT INTO Owner (owner_id, name, contact) VALUES
                                                (1, 'Infraestruturas de Portugal', 'contact@ip.pt'),
                                                (2, 'Refer - Rede Ferroviária Nacional', 'info@refer.pt'),
                                                (3, 'Medway Iberia', 'operations@medway-iberia.com');

-- 2. OPERATORS (Operadores ferroviários - baseado no mapa Medway)
INSERT INTO Operator (operator_id, name) VALUES
                                             (1, 'Medway Portugal'),
                                             (2, 'CP Carga'),
                                             (3, 'Takargo Rail');

-- 3. STATIONS (Estações - baseado no mapa Medway Portugal)
INSERT INTO Station (station_id, name, station_type) VALUES
-- Terminais principais
(1, 'Lisboa Santa Apolónia', 'TERMINAL'),
(2, 'Porto Campanhã', 'TERMINAL'),
(3, 'Sines', 'TERMINAL'),
(4, 'Póvoa de Santa Iria', 'TERMINAL'),
-- Estações regulares
(5, 'Entroncamento', 'REGULAR'),
(6, 'Pampilhosa', 'REGULAR'),
(7, 'Faro', 'REGULAR'),
(8, 'Évora', 'REGULAR');

-- 4. WAREHOUSES & BAYS (Armazéns e docas - para ESINF)
INSERT INTO Warehouse (warehouse_id, station_id) VALUES
                                                     (1, 1), (2, 2), (3, 3), (4, 4);

INSERT INTO Bay (bay_id, warehouse_id, capacity_n) VALUES
-- Armazém de Lisboa
(1, 1, 100), (2, 1, 150), (3, 1, 200),
-- Armazém do Porto
(4, 2, 120), (5, 2, 180);

-- 5. GAUGES (Bitolas - baseado no enunciado)
INSERT INTO Gauge (gauge_code, line_start_point, line_end_point, linestation_id, width, description, line_id) VALUES
                                                                                                                  ('IBERO', 1, 2, 1, 1668, 'Bitola Ibérica', 1),
                                                                                                                  ('STANDARD', 2, 3, 2, 1435, 'Bitola Europeia', 2),
                                                                                                                  ('IBERO', 3, 4, 3, 1668, 'Bitola Ibérica', 3);

-- 6. LINES (Linhas ferroviárias - baseado na rede Medway)
INSERT INTO Line (station_id, start_station_id, end_station_id, gauge_code, name, owner_id, start_id, end_id) VALUES
-- Linha do Norte
(1, 1, 2, 'IBERO', 'Linha do Norte', 1, 1, 2),
-- Linha do Sul
(2, 1, 7, 'IBERO', 'Linha do Sul', 1, 1, 7),
-- Linha do Oeste
(3, 1, 3, 'IBERO', 'Linha de Sines', 1, 1, 3);

-- 7. SEGMENT TYPES (Tipos de segmento)
INSERT INTO SegmentType (track_type_code, description) VALUES
                                                           (1, 'SINGLE'),
                                                           (2, 'DOUBLE');

-- 8. SEGMENTS (Segmentos de linha - características técnicas)
INSERT INTO Segment (segment_id, segment_type_code, length_km, line_id, gauge_code, electrified_flag, track_type) VALUES
-- Segmentos da Linha do Norte
(1, 2, 45.5, 1, 'IBERO', 'Y', 'DOUBLE'),   -- Lisboa-Entroncamento (eletrificado)
(2, 2, 60.2, 1, 'IBERO', 'Y', 'DOUBLE'),   -- Entroncamento-Pampilhosa
(3, 1, 85.7, 1, 'IBERO', 'N', 'SINGLE'),   -- Pampilhosa-Porto (não eletrificado)
-- Segmentos da Linha de Sines
(4, 1, 120.3, 3, 'IBERO', 'N', 'SINGLE');  -- Lisboa-Sines

-- 9. LOCOMOTIVE MODELS (Modelos de locomotivas)
INSERT INTO LocomotiveModel (locomotive_model_id, name, power, acelaration, fuel_capacity, max_weight) VALUES
                                                                                                           (1, 'Siemens Vectron', 6000, 0.8, NULL, 90000),    -- Elétrica
                                                                                                           (2, 'Bombardier TRAXX', 5600, 0.7, NULL, 85000),   -- Elétrica
                                                                                                           (3, 'EMD JT42C', 3200, 0.5, 6500, 126000),         -- Diesel
                                                                                                           (4, 'GE PowerHaul', 3600, 0.6, 7000, 130000);      -- Diesel

-- 10. LOCOMOTIVES (Locomotivas reais em operação)
INSERT INTO Locomotive (locomotive_id, locomotive_type, owner_id, gauge_code, operator_id, entry_year, locomotive_model_id, height, weight, lenght) VALUES
-- Locomotivas elétricas
(1, 'ELECTRIC', 3, 'IBERO', 1, 2018, 1, 4.3, 87000, 18.9),
(2, 'ELECTRIC', 3, 'IBERO', 1, 2019, 2, 4.2, 82000, 18.6),
-- Locomotivas diesel
(3, 'DIESEL', 3, 'IBERO', 1, 2015, 3, 4.4, 124000, 21.3),
(4, 'DIESEL', 3, 'STANDARD', 1, 2016, 4, 4.5, 128000, 22.1);

-- 11. WAGON TYPES (Tipos de vagão - baseado no enunciado)
INSERT INTO WagonType (wagon_type_code, description, max_weight_kg, max_volume_m3) VALUES
                                                                                       (1, 'BOXCAR', 60000, 120),      -- Mercadoria geral
                                                                                       (2, 'TANK_CAR', 80000, 90),     -- Químicos/líquidos
                                                                                       (3, 'FLATCAR', 70000, 0),       -- Contentores/maquinaria
                                                                                       (4, 'HOPPER', 75000, 110),      -- Granéis
                                                                                       (5, 'REFRIGERATED', 50000, 85); -- Produtos perecíveis

-- 12. WAGONS (Vagões - dados realistas)
INSERT INTO Wagon (wagon_id, operator_id, gauge_code, wagon_type_code, serial_code, payload, service_year, max_speed, weight, height, length) VALUES
                                                                                                                                                  (1, 1, 'IBERO', 1, 'MED-BX-001', 58000, 2018, 100, 22000, 4.2, 14.8),
                                                                                                                                                  (2, 1, 'IBERO', 2, 'MED-TK-001', 78000, 2019, 90, 32000, 3.8, 12.5),
                                                                                                                                                  (3, 1, 'IBERO', 3, 'MED-FL-001', 68000, 2020, 110, 18000, 1.2, 19.2),
                                                                                                                                                  (4, 1, 'STANDARD', 4, 'MED-HP-001', 73000, 2017, 85, 27000, 4.5, 15.3);

-- 13. ROUTES & FREIGHTS (Rotas e fretes)
INSERT INTO Route_Type (route_type_id) VALUES (1), (2), (3);
INSERT INTO Route (route_id, route_type_id) VALUES (1, 1), (2, 2);

INSERT INTO Freight (freight_id, initial_station_id, final_station_id, route_id) VALUES
                                                                                     (1, 1, 2, 1),  -- Lisboa -> Porto
                                                                                     (2, 3, 1, 2);  -- Sines -> Lisboa

-- 14. LOCOMOTIVE_WAGON (Composições de comboios)
INSERT INTO Locomotive_Wagon (Locomotivelocomotive_id, Wagonwagon_id) VALUES
                                                                          (1, 1), (1, 2),  -- Locomotiva 1 puxa vagões 1 e 2
                                                                          (3, 3), (3, 4);  -- Locomotiva 3 puxa vagões 3 e 4

COMMIT;