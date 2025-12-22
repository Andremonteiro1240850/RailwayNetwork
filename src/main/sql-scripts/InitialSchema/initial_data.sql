-- 1. Facility_type
INSERT INTO Facility_type (type_id, name) VALUES (1, 'Terminal');
INSERT INTO Facility_type (type_id, name) VALUES (2, 'Regular Station');

-- 2. Facility
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (1, 'São Romão', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (2, 'Tamel', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (3, 'Senhora das Dores', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (4, 'Lousado', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (5, 'Porto Campanhã', 1);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (6, 'Leandro', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (7, 'Porto São Bento', 1);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (8, 'Barcelos', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (9, 'Vila Nova da Cerveira', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (10, 'Midões', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (11, 'Valença', 1);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (12, 'Darque', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (13, 'Contumil', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (14, 'Ermesinde', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (15, 'São Frutuoso', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (16, 'São Pedro da Torre', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (17, 'Viana do Castelo', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (18, 'Famalicão', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (19, 'Barroselas', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (20, 'Nine', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (21, 'Caminha', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (22, 'Carvalha', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (23, 'Carreço', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (30, 'Braga', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (31, 'Manzagão', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (32, 'Cerqueiral', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (33, 'Gemieira', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (35, 'Paredes de Coura', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (43, 'São Gemil', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (45, 'São Mamede de Infesta', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (48, 'Leça do Balio', 2);
INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (50, 'Leixões', 1);

-- 3. Owner
INSERT INTO Owner (owner_id, name) VALUES ('IP', 'Infraestruturas de Portugal, SA');

-- 4. Operator
INSERT INTO Operator (operator_id, name) VALUES ('Medway', 'Medway - Operador Ferroviário de Mercadorias, S.A');
INSERT INTO Operator (operator_id, name) VALUES ('Captrain', 'Captrain Portugal S.A.');

-- 5. Gauge
INSERT INTO Gauge (code, width) VALUES (1668, 1668);
INSERT INTO Gauge (code, width) VALUES (1435, 1435);

-- 6. Line
INSERT INTO Line (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode)
VALUES (1, 7, 5, 'Ramal São Bento - Campanhã', 'IP', 1668);
INSERT INTO Line (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode)
VALUES (2, 5, 13, 'Ramal Camapanhã - Contumil', 'IP', 1668);
INSERT INTO Line (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode)
VALUES (3, 13, 20, 'Ramal Contumil - Nine', 'IP', 1668);
INSERT INTO Line (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode)
VALUES (4, 20, 8, 'Ramal Nine - Barcelos', 'IP', 1668);
INSERT INTO Line (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode)
VALUES (5, 8, 12, 'Ramal Barcelos - Darque', 'IP', 1668);
INSERT INTO Line (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode)
VALUES (6, 12, 17, 'Ramal Darque - Viana', 'IP', 1668);
INSERT INTO Line (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode)
VALUES (7, 17, 21, 'Ramal Viana - Caminha', 'IP', 1668);
INSERT INTO Line (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode)
VALUES (8, 21, 16, 'Ramal Caminha - Torre', 'IP', 1668);
INSERT INTO Line (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode)
VALUES (9, 16, 11, 'Ramal Torre - Valença', 'IP', 1668);
INSERT INTO Line (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode)
VALUES (21, 13, 43, 'Ramal Contumil - São Gemil', 'IP', 1668);
INSERT INTO Line (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode)
VALUES (22, 43, 45, 'Ramal São Gemil - São Mamede de Infesta', 'IP', 1668);
INSERT INTO Line (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode)
VALUES (23, 45, 48, 'Ramal São Mamede de Infesta - Leça do Balio', 'IP', 1668);
INSERT INTO Line (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode)
VALUES (24, 48, 50, 'Ramal Leça do Balio - Leixões', 'IP', 1668);
INSERT INTO Line (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode)
VALUES (30, 31, 30, 'Ramal Braga', 'IP', 1668);
INSERT INTO Line (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode)
VALUES (31, 20, 31, 'Ramal Nine - Manzagão', 'IP', 1668);
INSERT INTO Line (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode)
VALUES (32, 31, 32, 'Ramal Manzagão - Cerqueiral', 'IP', 1668);
INSERT INTO Line (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode)
VALUES (35, 32, 33, 'Ramal Cerqueiral - Gemieira', 'IP', 1668);
INSERT INTO Line (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode)
VALUES (36, 33, 35, 'Ramal Gemieira - Paredes de Coura', 'IP', 1668);
INSERT INTO Line (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode)
VALUES (37, 35, 11, 'Ramal Paredes de Coura - Valença', 'IP', 1668);

-- 7. SegmentType
INSERT INTO SegmentType (trackType, numberOfTracks) VALUES ('SINGLE', 1);
INSERT INTO SegmentType (trackType, numberOfTracks) VALUES ('DOUBLE', 2);
INSERT INTO SegmentType (trackType, numberOfTracks) VALUES ('QUAD', 4);

-- 8. Siding
INSERT INTO Siding (SIding_id, "Size") VALUES (1, 864);
INSERT INTO Siding (SIding_id, "Size") VALUES (2, 266);

-- 9. Segment
INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (1, 1, 2618, 1, 'Y', 4, 'QUAD', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (3, 2, 2443, 2, 'Y', 4, 'QUAD', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (10, 3, 26560, 3, 'Y', 2, 'DOUBLE', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (11, 3, 10000, 4, 'Y', 2, 'DOUBLE', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (15, 4, 5286, 5, 'Y', 2, 'DOUBLE', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (16, 4, 6000, 6, 'Y', 2, 'DOUBLE', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (14, 5, 10387, 7, 'Y', 2, 'DOUBLE', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (12, 5, 12000, 8, 'Y', 2, 'DOUBLE', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (13, 5, 3100, 9, 'Y', 2, 'DOUBLE', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (20, 6, 4890, 10, 'Y', 2, 'DOUBLE', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (18, 7, 6000, 11, 'Y', 1, 'SINGLE', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (21, 7, 5000, 12, 'Y', 1, 'SINGLE', 1); -- Tem siding

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (22, 7, 12000, 13, 'Y', 1, 'SINGLE', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (25, 8, 20829, 14, 'Y', 1, 'SINGLE', 2); -- Tem siding

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (26, 9, 4264, 15, 'Y', 1, 'SINGLE', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (30, 21, 3883, 16, 'Y', 2, 'DOUBLE', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (31, 22, 1174, 17, 'Y', 2, 'DOUBLE', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (32, 22, 2534, 18, 'Y', 2, 'DOUBLE', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (33, 23, 1566, 19, 'Y', 2, 'DOUBLE', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (34, 23, 1453, 20, 'Y', 2, 'DOUBLE', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (35, 24, 3597, 21, 'Y', 2, 'DOUBLE', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (36, 24, 4334, 22, 'Y', 2, 'DOUBLE', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (50, 30, 3555, 23, 'Y', 2, 'DOUBLE', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (51, 31, 1222, 24, 'Y', 2, 'DOUBLE', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (52, 31, 1760, 25, 'Y', 2, 'DOUBLE', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (53, 31, 1720, 26, 'Y', 2, 'DOUBLE', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (54, 31, 3350, 27, 'Y', 2, 'DOUBLE', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (55, 31, 3470, 28, 'Y', 2, 'DOUBLE', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (58, 32, 8050, 29, 'Y', 2, 'DOUBLE', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (59, 35, 22320, 30, 'Y', 2, 'DOUBLE', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (60, 36, 16310, 31, 'Y', 2, 'DOUBLE', NULL);

INSERT INTO Segment (segment_id, line_id, length_m, segment_order, electrified_flag, SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id)
VALUES (61, 37, 15200, 32, 'Y', 2, 'DOUBLE', NULL);

-- 10. WagonType
INSERT INTO WagonType (type_id, type_name) VALUES (1, 'Cereal wagon');
INSERT INTO WagonType (type_id, type_name) VALUES (2, 'Covered wagon with sliding door');
INSERT INTO WagonType (type_id, type_name) VALUES (3, 'Container wagon (max 40'' HC)');
INSERT INTO WagonType (type_id, type_name) VALUES (4, 'Biodiesel wagon');
INSERT INTO WagonType (type_id, type_name) VALUES (5, 'Wood wagon');

-- 11. WagonModel

INSERT INTO WagonModel (modelID, name, maker, bogies_number, lenght, height, width, weight_t, max_speed, payload, volume, WagonTypetype_id)
VALUES (1245, 'Tadgs 32 94 082 3', 'Metalsines', 2, 17240, 4270, 3072, 24, 120, 56, 75, 1);

INSERT INTO WagonModel (modelID, name, maker, bogies_number, lenght, height, width, weight_t, max_speed, payload, volume, WagonTypetype_id)
VALUES (1278, 'Tdgs 41 94 074 1', 'Equimetal', 2, 9640, 4165, 3120, 13, 100, 26, 38, 1);

INSERT INTO WagonModel (modelID, name, maker, bogies_number, lenght, height, width, weight_t, max_speed, payload, volume, WagonTypetype_id)
VALUES (1325, 'Gabs 81 94 181 1', 'Sepsa Cometna', 2, 21700, 4170, 3180, 29, 100, 50, 110, 2);

INSERT INTO WagonModel (modelID, name, maker, bogies_number, lenght, height, width, weight_t, max_speed, payload, volume, WagonTypetype_id)
VALUES (1104, 'Regmms 32 94 356 3', 'Metalsines', 2, 14040, 2535, 3104, 21, 120, 60, 76, 3);

INSERT INTO WagonModel (modelID, name, maker, bogies_number, lenght, height, width, weight_t, max_speed, payload, volume, WagonTypetype_id)
VALUES (985, 'Lgs 22 94 441 6', 'Metalsines', 2, 13860, 1060, 2850, 11, 120, 28, 76, 3);

INSERT INTO WagonModel (modelID, name, maker, bogies_number, lenght, height, width, weight_t, max_speed, payload, volume, WagonTypetype_id)
VALUES (987, 'Sgnss 12 94 455 2', 'Emef', 2, 18116, 1030, 2950, 21, 120, 68, 76, 3);

INSERT INTO WagonModel (modelID, name, maker, bogies_number, lenght, height, width, weight_t, max_speed, payload, volume, WagonTypetype_id)
VALUES (988, 'Sgnss 12 94 455 2', 'Emef', 2, 18116, 1030, 2950, 21, 120, 68, 76, 3);

INSERT INTO WagonModel (modelID, name, maker, bogies_number, lenght, height, width, weight_t, max_speed, payload, volume, WagonTypetype_id)
VALUES (1525, 'Zaes 81 94 788', 'Equimetal', 2, 13800, 4226, 2950, 22, 120, 57, 64, 4);

INSERT INTO WagonModel (modelID, name, maker, bogies_number, lenght, height, width, weight_t, max_speed, payload, volume, WagonTypetype_id)
VALUES (1523, 'Zaes 81 94 788', 'Equimetal', 2, 13800, 4226, 2950, 22, 120, 57, 64, 4);

INSERT INTO WagonModel (modelID, name, maker, bogies_number, lenght, height, width, weight_t, max_speed, payload, volume, WagonTypetype_id)
VALUES (1212, 'Kbs 41 94 333', 'Simmering', 2, 14020, 3300, 2842, 14, 100, 25, 62, 5);

-- 12. LocomotiveModel

INSERT INTO LocomotiveModel (locomotive_model_id, power, acelaration, make, lenght, weight, heigth, max_speed, bogies_number)
VALUES ('Eurosprinter', 5600, 300, 'Siemens', 19200, 87, 4375, 220, 2);

INSERT INTO LocomotiveModel (locomotive_model_id, power, acelaration, make, lenght, weight, heigth, max_speed, bogies_number)
VALUES ('CP 1900', 1623, 396, 'Sorefame - Alsthom', 19084, 117, 4310, 100, 2);

INSERT INTO LocomotiveModel (locomotive_model_id, power, acelaration, make, lenght, weight, heigth, max_speed, bogies_number)
VALUES ('E4000', 3178, 400, 'Stadler', 23020, 124, 4264, 120, 3);

-- 13. Diesel_Locomotive
INSERT INTO Diesel_Locomotive (LocomotiveModellocomotive_model_id, fuelCapacity)
VALUES ('CP 1900', 4882);

INSERT INTO Diesel_Locomotive (LocomotiveModellocomotive_model_id, fuelCapacity)
VALUES ('E4000', 6700);

-- 14. Electric_Locomotive
INSERT INTO Electric_Locomotive (LocomotiveModellocomotive_model_id, Voltage)
VALUES ('Eurosprinter', 25000); -- 25 KV = 25000 volts

-- 15. Locomotive

INSERT INTO Locomotive (locomotive_id, operator_id, entry_year, locomotive_model_id, Traintrain_id, Trainoperator_id)
VALUES (5621, 'Medway', 1995, 'Eurosprinter', 5421, 'Medway'); -- inventar dados para train

INSERT INTO Locomotive (locomotive_id, operator_id, entry_year, locomotive_model_id, Traintrain_id, Trainoperator_id)
VALUES (5623, 'Medway', 1995, 'Eurosprinter', 5421, 'Medway');

INSERT INTO Locomotive (locomotive_id, operator_id, entry_year, locomotive_model_id, Traintrain_id, Trainoperator_id)
VALUES (5630, 'Medway', 1996, 'Eurosprinter', 5421, 'Medway');

INSERT INTO Locomotive (locomotive_id, operator_id, entry_year, locomotive_model_id, Traintrain_id, Trainoperator_id)
VALUES (1903, 'Medway', 1981, 'CP 1900', 5435, 'Medway');

INSERT INTO Locomotive (locomotive_id, operator_id, entry_year, locomotive_model_id, Traintrain_id, Trainoperator_id)
VALUES (5034, 'Medway', 2017, 'E4000', 5437, 'Medway');

INSERT INTO Locomotive (locomotive_id, operator_id, entry_year, locomotive_model_id, Traintrain_id, Trainoperator_id)
VALUES (5036, 'Medway', 2017, 'E4000', 5437, 'Medway');


INSERT INTO Locomotive (locomotive_id, operator_id, entry_year, locomotive_model_id, Traintrain_id, Trainoperator_id)
VALUES (5037, 'Medway', 2017, 'E4000', 5437, 'Medway');

INSERT INTO Locomotive (locomotive_id, operator_id, entry_year, locomotive_model_id, Traintrain_id, Trainoperator_id)
VALUES (335001, 'Captrain', 2019, 'E4000', 5437, 'Medway'); -- inventar dados para train

INSERT INTO Locomotive (locomotive_id, operator_id, entry_year, locomotive_model_id, Traintrain_id, Trainoperator_id)
VALUES (335003, 'Captrain', 2019, 'E4000', 5437, 'Medway');

-- 16. Locomotive_Gauge
INSERT INTO Locomotive_Gauge (Locomotivelocomotive_id, Gaugecode)
VALUES (5621, 1668);

INSERT INTO Locomotive_Gauge (Locomotivelocomotive_id, Gaugecode)
VALUES (5623, 1668);

INSERT INTO Locomotive_Gauge (Locomotivelocomotive_id, Gaugecode)
VALUES (5630, 1668);

INSERT INTO Locomotive_Gauge (Locomotivelocomotive_id, Gaugecode)
VALUES (1903, 1668);

INSERT INTO Locomotive_Gauge (Locomotivelocomotive_id, Gaugecode)
VALUES (5034, 1668);

INSERT INTO Locomotive_Gauge (Locomotivelocomotive_id, Gaugecode)
VALUES (5036, 1668);

INSERT INTO Locomotive_Gauge (Locomotivelocomotive_id, Gaugecode)
VALUES (5037, 1435); -- Esta é a duplicada com bitola 1435

INSERT INTO Locomotive_Gauge (Locomotivelocomotive_id, Gaugecode)
VALUES (335001, 1668);

INSERT INTO Locomotive_Gauge (Locomotivelocomotive_id, Gaugecode)
VALUES (335003, 1668);

-- 17. Wagon

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (77077, 'Medway', 1987, 1104); -- 356 3 077

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (78078, 'Medway', 1987, 1104); -- 356 3 078

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (79079, 'Medway', 1987, 1104); -- 356 3 079

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (80080, 'Medway', 1987, 1104); -- 356 3 080

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (81081, 'Medway', 1987, 1104); -- 356 3 081

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (82082, 'Medway', 1987, 1104); -- 356 3 082

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (83083, 'Medway', 1987, 1104); -- 356 3 083

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (84084, 'Medway', 1987, 1104); -- 356 3 084

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (85085, 'Medway', 1987, 1104); -- 356 3 085

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (86086, 'Medway', 1987, 1104); -- 356 3 086

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (87087, 'Medway', 1987, 1104); -- 356 3 087

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (88088, 'Medway', 1987, 1104); -- 356 3 088

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (89089, 'Medway', 1987, 1104); -- 356 3 089

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (90090, 'Medway', 1987, 1104); -- 356 3 090

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (91091, 'Medway', 1987, 1104); -- 356 3 091

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (92092, 'Medway', 1987, 1104); -- 356 3 092

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (45045, 'Medway', 1990, 1245); -- 082 3 045

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (46046, 'Medway', 1990, 1245); -- 082 3 046

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (47047, 'Medway', 1990, 1245); -- 082 3 047

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (48048, 'Medway', 1990, 1245); -- 082 3 048

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (1001, 'Medway', 1977, 1278); -- 074 1 001

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (1002, 'Medway', 1977, 1278); -- 074 1 002

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (1003, 'Medway', 1977, 1278); -- 074 1 003

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (1004, 'Medway', 1977, 1278); -- 074 1 004

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (1005, 'Medway', 1977, 1278); -- 074 1 005

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (1006, 'Medway', 1977, 1278); -- 074 1 006

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (1010, 'Medway', 1977, 1325); -- 181 1 010

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (1011, 'Medway', 1977, 1325); -- 181 1 011

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (1012, 'Medway', 1977, 1325); -- 181 1 012

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (1013, 'Medway', 1977, 1325); -- 181 1 013

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (1014, 'Medway', 1977, 1325); -- 181 1 014

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (2001, 'Medway', 2005, 1212); -- 333 0 001

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (2002, 'Medway', 2005, 1212); -- 333 0 002

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (2003, 'Medway', 2005, 1212); -- 333 0 003

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (2004, 'Medway', 2005, 1212); -- 333 0 004

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (2005, 'Medway', 2005, 1212); -- 333 0 005

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (2006, 'Medway', 2005, 1212); -- 333 0 006

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (2007, 'Medway', 2005, 1212); -- 333 0 007

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (2008, 'Medway', 2005, 1212); -- 333 0 008

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (2009, 'Medway', 2005, 1212); -- 333 0 009

INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (2010, 'Medway', 2005, 1212); -- 333 0 010

-- 18. Gauge_Wagon

INSERT INTO Gauge_Wagon (Gaugecode, Wagonwagon_id) SELECT 1668, wagon_id FROM Wagon WHERE WagonModelmodelID IN (1245, 1278, 1325, 1104, 985, 987, 1525, 1212);
INSERT INTO Gauge_Wagon (Gaugecode, Wagonwagon_id) SELECT 1435, wagon_id FROM Wagon WHERE WagonModelmodelID IN (988, 1523);

-- 19. Train
INSERT INTO Train (train_id, operator_id, max_length) VALUES (5421, 'Medway', 500);
INSERT INTO Train (train_id, operator_id, max_length) VALUES (5435, 'Medway', 400);
INSERT INTO Train (train_id, operator_id, max_length) VALUES (5437, 'Medway', 600);

-- 20. Locomotive_Train
INSERT INTO Locomotive_Train (Locomotivelocomotive_id, Traintrain_id, Trainoperator_id, Adddate)
VALUES (5621, 5421, 'Medway', TO_TIMESTAMP('2025-10-03 08:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO Locomotive_Train (Locomotivelocomotive_id, Traintrain_id, Trainoperator_id, Adddate)
VALUES (5623, 5421, 'Medway', TO_TIMESTAMP('2025-10-03 08:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO Locomotive_Train (Locomotivelocomotive_id, Traintrain_id, Trainoperator_id, Adddate)
VALUES (1903, 5435, 'Medway', TO_TIMESTAMP('2025-10-03 17:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO Locomotive_Train (Locomotivelocomotive_id, Traintrain_id, Trainoperator_id, Adddate)
VALUES (5034, 5437, 'Medway', TO_TIMESTAMP('2025-10-06 09:00:00', 'YYYY-MM-DD HH24:MI:SS'));

-- 21. FreighTrain
INSERT INTO FreighTrain (Freigth_id, Traintrain_id, TrainLocomotivelocomotive_id, Trainoperator_id)
VALUES (2001, 5421, 5621, 'Medway');

INSERT INTO FreighTrain (Freigth_id, Traintrain_id, TrainLocomotivelocomotive_id, Trainoperator_id)
VALUES (2002, 5421, 5621, 'Medway');

INSERT INTO FreighTrain (Freigth_id, Traintrain_id, TrainLocomotivelocomotive_id, Trainoperator_id)
VALUES (2003, 5421, 5621, 'Medway');

INSERT INTO FreighTrain (Freigth_id, Traintrain_id, TrainLocomotivelocomotive_id, Trainoperator_id)
VALUES (2004, 5421, 5621, 'Medway');

INSERT INTO FreighTrain (Freigth_id, Traintrain_id, TrainLocomotivelocomotive_id, Trainoperator_id)
VALUES (2005, 5421, 5621, 'Medway');

INSERT INTO FreighTrain (Freigth_id, Traintrain_id, TrainLocomotivelocomotive_id, Trainoperator_id)
VALUES (2006, 5435, 1903, 'Medway');

INSERT INTO FreighTrain (Freigth_id, Traintrain_id, TrainLocomotivelocomotive_id, Trainoperator_id)
VALUES (2007, 5435, 1903, 'Medway');

INSERT INTO FreighTrain (Freigth_id, Traintrain_id, TrainLocomotivelocomotive_id, Trainoperator_id)
VALUES (2050, 5437, 5034, 'Medway');

INSERT INTO FreighTrain (Freigth_id, Traintrain_id, TrainLocomotivelocomotive_id, Trainoperator_id)
VALUES (2051, 5437, 5034, 'Medway');

-- 22. Freight
INSERT INTO Freight (freight_id, Originfacility_id, Destinationfacility_id, FreighTrainFreigth_id, createDate, FreighTrainTraintrain_id)
VALUES (2001, 50, 12, 2001, TO_TIMESTAMP('2025-10-03 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 5421);

INSERT INTO Freight (freight_id, Originfacility_id, Destinationfacility_id, FreighTrainFreigth_id, createDate, FreighTrainTraintrain_id)
VALUES (2002, 13, 11, 2002, TO_TIMESTAMP('2025-10-03 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 5421);

INSERT INTO Freight (freight_id, Originfacility_id, Destinationfacility_id, FreighTrainFreigth_id, createDate, FreighTrainTraintrain_id)
VALUES (2003, 50, 11, 2003, TO_TIMESTAMP('2025-10-03 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 5421);

INSERT INTO Freight (freight_id, Originfacility_id, Destinationfacility_id, FreighTrainFreigth_id, createDate, FreighTrainTraintrain_id)
VALUES (2004, 50, 21, 2004, TO_TIMESTAMP('2025-10-03 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 5421);

INSERT INTO Freight (freight_id, Originfacility_id, Destinationfacility_id, FreighTrainFreigth_id, createDate, FreighTrainTraintrain_id)
VALUES (2005, 20, 11, 2005, TO_TIMESTAMP('2025-10-03 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 5421);

INSERT INTO Freight (freight_id, Originfacility_id, Destinationfacility_id, FreighTrainFreigth_id, createDate, FreighTrainTraintrain_id)
VALUES (2006, 12, 50, 2006, TO_TIMESTAMP('2025-10-03 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 5435);

INSERT INTO Freight (freight_id, Originfacility_id, Destinationfacility_id, FreighTrainFreigth_id, createDate, FreighTrainTraintrain_id)
VALUES (2007, 50, 5, 2007, TO_TIMESTAMP('2025-10-03 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 5435);

INSERT INTO Freight (freight_id, Originfacility_id, Destinationfacility_id, FreighTrainFreigth_id, createDate, FreighTrainTraintrain_id)
VALUES (2050, 12, 50, 2050, TO_TIMESTAMP('2025-10-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 5437);

INSERT INTO Freight (freight_id, Originfacility_id, Destinationfacility_id, FreighTrainFreigth_id, createDate, FreighTrainTraintrain_id)
VALUES (2051, 11, 50, 2051, TO_TIMESTAMP('2025-10-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 5437);

-- 23. Wagon_In_Freight
INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (2001, 2001); -- 333 0 001
INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (2002, 2001); -- 333 0 002
INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (2004, 2001); -- 333 0 004
INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (2005, 2001); -- 333 0 005
INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (2006, 2001); -- 333 0 006

INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (89089, 2002); -- 356 3 089

INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (1011, 2003); -- 181 1 011
INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (1012, 2003); -- 181 1 012

INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (1013, 2004); -- 181 1 013

INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (77077, 2005); -- 356 3 077
INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (78078, 2005); -- 356 3 078
INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (79079, 2005); -- 356 3 079
INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (80080, 2005); -- 356 3 080

INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (2003, 2006); -- 333 0 003
INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (2007, 2006); -- 333 0 007

INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (90090, 2007); -- 356 3 090
INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (91091, 2007); -- 356 3 091
INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (92092, 2007); -- 356 3 092

INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (2001, 2050); -- 333 0 001
INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (2002, 2050); -- 333 0 002
INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (2004, 2050); -- 333 0 004
INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (2005, 2050); -- 333 0 005
INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (2006, 2050); -- 333 0 006

INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (1011, 2051); -- 181 1 011
INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (1012, 2051); -- 181 1 012
INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (77077, 2051); -- 356 3 077
INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (78078, 2051); -- 356 3 078
INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (79079, 2051); -- 356 3 079
INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) VALUES (80080, 2051); -- 356 3 080

-- 24. Train_route

INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time)
VALUES (50, 5421, 1, 5621, 'Medway', 945, 950); -- 09:45 como 945

INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time)
VALUES (48, 5421, 2, 5621, 'Medway', 1000, 1005);

INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time)
VALUES (45, 5421, 3, 5621, 'Medway', 1030, 1035);

INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time)
VALUES (43, 5421, 4, 5621, 'Medway', 1100, 1105);

INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time)
VALUES (13, 5421, 5, 5621, 'Medway', 1130, 1135);

INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time)
VALUES (20, 5421, 6, 5621, 'Medway', 1200, 1205);

INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time)
VALUES (8, 5421, 7, 5621, 'Medway', 1230, 1235);

INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time)
VALUES (12, 5421, 8, 5621, 'Medway', 1300, 1305);

INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time)
VALUES (17, 5421, 9, 5621, 'Medway', 1330, 1335);

INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time)
VALUES (21, 5421, 10, 5621, 'Medway', 1400, 1405);

INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time)
VALUES (16, 5421, 11, 5621, 'Medway', 1430, 1435);

INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time)
VALUES (11, 5421, 12, 5621, 'Medway', 1500, 1505);


INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time)
VALUES (11, 5435, 1, 1903, 'Medway', 1800, 1805); -- 18:00 como 1800

INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time)
VALUES (16, 5435, 2, 1903, 'Medway', 1830, 1835);
INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time)
VALUES (21, 5435, 2, 1903, 'Medway', 1830, 1835);
INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time)
VALUES (17, 5435, 2, 1903, 'Medway', 1830, 1835);
INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time)
VALUES (12, 5435, 2, 1903, 'Medway', 1830, 1835);
INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time)
VALUES (8, 5435, 2, 1903, 'Medway', 1830, 1835);
INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time)
VALUES (20, 5435, 2, 1903, 'Medway', 1830, 1835);
INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time)
VALUES (13, 5435, 2, 1903, 'Medway', 1830, 1835);
INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time)
VALUES (43, 5435, 2, 1903, 'Medway', 1830, 1835);
INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time)
VALUES (45, 5435, 2, 1903, 'Medway', 1830, 1835);
INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time)
VALUES (48, 5435, 2, 1903, 'Medway', 1830, 1835);
INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time)
VALUES (50, 5435, 2, 1903, 'Medway', 1830, 1835);


-- 25. TrainLog
INSERT INTO TrainLog (Traintrain_id, Trainoperator_id, Arrival, TrainLogTraintrain_id)
VALUES (5421, 'Medway', TO_TIMESTAMP('2025-10-03 15:05:00', 'YYYY-MM-DD HH24:MI:SS'), 5421);

INSERT INTO TrainLog (Traintrain_id, Trainoperator_id, Arrival, TrainLogTraintrain_id)
VALUES (5435, 'Medway', TO_TIMESTAMP('2025-10-03 22:00:00', 'YYYY-MM-DD HH24:MI:SS'), 5435);

INSERT INTO TrainLog (Traintrain_id, Trainoperator_id, Arrival, TrainLogTraintrain_id)
VALUES (5437, 'Medway', TO_TIMESTAMP('2025-10-06 18:00:00', 'YYYY-MM-DD HH24:MI:SS'), 5437);

-- 26. TrainLogDeparture
INSERT INTO TrainLogDeparture (TrainLogTraintrain_id, Departure)
VALUES (5421, TO_TIMESTAMP('2025-10-03 09:45:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO TrainLogDeparture (TrainLogTraintrain_id, Departure)
VALUES (5435, TO_TIMESTAMP('2025-10-03 18:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO TrainLogDeparture (TrainLogTraintrain_id, Departure)
VALUES (5437, TO_TIMESTAMP('2025-10-06 10:00:00', 'YYYY-MM-DD HH24:MI:SS'));

-- 27. FreightLog
INSERT INTO FreightLog (Freightfreight_id, unloadFreightDate)
VALUES (2001, TO_TIMESTAMP('2025-10-03 16:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO FreightLog (Freightfreight_id, unloadFreightDate)
VALUES (2002, TO_TIMESTAMP('2025-10-03 16:30:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO FreightLog (Freightfreight_id, unloadFreightDate)
VALUES (2003, TO_TIMESTAMP('2025-10-03 16:30:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO FreightLog (Freightfreight_id, unloadFreightDate)
VALUES (2004, TO_TIMESTAMP('2025-10-03 17:30:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO FreightLog (Freightfreight_id, unloadFreightDate)
VALUES (2005, TO_TIMESTAMP('2025-11-03 16:30:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO FreightLog (Freightfreight_id, unloadFreightDate)
VALUES (2006, TO_TIMESTAMP('2025-12-03 16:35:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO FreightLog (Freightfreight_id, unloadFreightDate)
VALUES (2007, TO_TIMESTAMP('2025-11-03 18:30:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO FreightLog (Freightfreight_id, unloadFreightDate)
VALUES (2050, TO_TIMESTAMP('2025-7-04 16:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO FreightLog (Freightfreight_id, unloadFreightDate)
VALUES (2051, TO_TIMESTAMP('2025-7-04 16:30:00', 'YYYY-MM-DD HH24:MI:SS'));


-- 28. BuildingType
INSERT INTO BuildingType (ID, name) VALUES (1, 'Warehouse');
INSERT INTO BuildingType (ID, name) VALUES (2, 'Grain Silos');
INSERT INTO BuildingType (ID, name) VALUES (3, 'Cold Areas');

-- 29. Building
INSERT INTO Building (ID, name, NUMBEROFBUILDINGS, BuildingTypeID, Facilityfacility_id)
VALUES (1, 'Main Warehouse Leixões', 1, 1, 50);

INSERT INTO Building (ID, name, NUMBEROFBUILDINGS, BuildingTypeID, Facilityfacility_id)
VALUES (2, 'Grain Silo Leixões', 1, 2, 50);

INSERT INTO Building (ID, name, NUMBEROFBUILDINGS, BuildingTypeID, Facilityfacility_id)
VALUES (3, 'Refrigerated Area Valença', 1, 3, 11);

INSERT INTO Building (ID, name, NUMBEROFBUILDINGS, BuildingTypeID, Facilityfacility_id)
VALUES (4, 'Campanhã Warehouse', 1, 1, 5);

INSERT INTO Building (ID, name, NUMBEROFBUILDINGS, BuildingTypeID, Facilityfacility_id)
VALUES (5, 'Grain Silo of São Bento', 1, 2, 7);