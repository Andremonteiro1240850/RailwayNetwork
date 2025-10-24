-- ======= FACILITY =======
INSERT INTO Facility (facility_id, name, facility_type) VALUES (1, 'São Romão', 'REGULAR');
INSERT INTO Facility (facility_id, name, facility_type) VALUES (2, 'Tamel', 'REGULAR');
INSERT INTO Facility (facility_id, name, facility_type) VALUES (3, 'Senhora das Dores', 'REGULAR');
INSERT INTO Facility (facility_id, name, facility_type) VALUES (4, 'Lousado', 'REGULAR');
INSERT INTO Facility (facility_id, name, facility_type) VALUES (5, 'Porto Campanhã', 'REGULAR');
INSERT INTO Facility (facility_id, name, facility_type) VALUES (6, 'Leandro', 'REGULAR');
INSERT INTO Facility (facility_id, name, facility_type) VALUES (7, 'Porto São Bento', 'TERMINAL');
INSERT INTO Facility (facility_id, name, facility_type) VALUES (8, 'Barcelos', 'REGULAR');
INSERT INTO Facility (facility_id, name, facility_type) VALUES (9, 'Vila Nova da Cerveira', 'REGULAR');
INSERT INTO Facility (facility_id, name, facility_type) VALUES (10, 'Midões', 'REGULAR');
INSERT INTO Facility (facility_id, name, facility_type) VALUES (11, 'Valença', 'REGULAR');
INSERT INTO Facility (facility_id, name, facility_type) VALUES (12, 'Darque', 'REGULAR');
INSERT INTO Facility (facility_id, name, facility_type) VALUES (13, 'Contumil', 'REGULAR');
INSERT INTO Facility (facility_id, name, facility_type) VALUES (14, 'Ermesinde', 'REGULAR');
INSERT INTO Facility (facility_id, name, facility_type) VALUES (15, 'São Frutuoso', 'REGULAR');
INSERT INTO Facility (facility_id, name, facility_type) VALUES (16, 'São Pedro da Torre', 'REGULAR');
INSERT INTO Facility (facility_id, name, facility_type) VALUES (17, 'Viana do Castelo', 'REGULAR');
INSERT INTO Facility (facility_id, name, facility_type) VALUES (18, 'Famalicão', 'REGULAR');
INSERT INTO Facility (facility_id, name, facility_type) VALUES (19, 'Barroselas', 'REGULAR');
INSERT INTO Facility (facility_id, name, facility_type) VALUES (20, 'Nine', 'REGULAR');
INSERT INTO Facility (facility_id, name, facility_type) VALUES (21, 'Caminha', 'REGULAR');
INSERT INTO Facility (facility_id, name, facility_type) VALUES (11, 'Valença', 'REGULAR');
INSERT INTO Facility (facility_id, name, facility_type) VALUES (22, 'Carvalha', 'REGULAR');
INSERT INTO Facility (facility_id, name, facility_type) VALUES (23, 'Carreço', 'REGULAR');

-- ======= FACILITY_LINE =======
INSERT INTO FACILITY_LINE (Facilityfacility_id,Lineline_id) VALUES (14, 5);

-- ======= LINE =======
INSERT INTO LINE (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode) VALUES (1,7,5, 'Ramal São Bento - Campanhã', 'PT503933813', 116);
INSERT INTO LINE (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode) VALUES (7, 16, 11, 'Ramal Torre - Valença', 'PT503933813', 116);
INSERT INTO LINE (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode) VALUES (6, 21, 16, 'Ramal Caminha - Torre', 'PT503933813', 116);
INSERT INTO LINE (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode) VALUES (5, 17, 21, 'Ramal Viana - Caminha', 'PT503933813', 116);
INSERT INTO LINE (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode) VALUES (4, 8, 17, 'Ramal Barcelos - Viana', 'PT503933813', 116);
INSERT INTO LINE (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode) VALUES (3, 20, 8, 'Ramal Nine - Barcelos', 'PT503933813', 116);
INSERT INTO LINE (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode) VALUES (2,5,20, 'Ramal Campanhã - Nine', 'PT503933813', 116);

-- ======= SEGMENT =======
INSERT INTO SEGMENT (segment_id, line_id, length_m, gauge_code, segment_order, electrified_flag, SegmentTypetrackNumber, max_weight_kg_m) VALUES (1, 1, 2618, 116, 1, 'Y', 4, 8000);
INSERT INTO SEGMENT (segment_id, line_id, length_m, gauge_code, segment_order, electrified_flag, SegmentTypetrackNumber, max_weight_kg_m)VALUES (10, 1, 29003, 116, 2, 'Y', 2, 8000);
INSERT INTO SEGMENT (segment_id, line_id, length_m, gauge_code, segment_order, electrified_flag, SegmentTypetrackNumber, max_weight_kg_m)VALUES (11, 2, 10000, 116, 2, 'Y', 2, 8000);
INSERT INTO SEGMENT (segment_id, line_id, length_m, gauge_code, segment_order, electrified_flag, SegmentTypetrackNumber, max_weight_kg_m) VALUES (15, 3, 5286, 116, 1, 'Y', 2, 8000);
INSERT INTO SEGMENT (segment_id, line_id, length_m, gauge_code, segment_order, electrified_flag, SegmentTypetrackNumber, max_weight_kg_m) VALUES (16, 3, 6000, 116, 2, 'Y', 2, 8000);
INSERT INTO SEGMENT (segment_id, line_id, length_m, gauge_code, segment_order, electrified_flag, SegmentTypetrackNumber, max_weight_kg_m) VALUES (14, 4, 10387, 116, 1, 'Y', 2, 8000);
INSERT INTO SEGMENT (segment_id, line_id, length_m, gauge_code, segment_order, electrified_flag, SegmentTypetrackNumber, max_weight_kg_m) VALUES (12, 4, 12000, 116, 2, 'Y', 2, 8000);
INSERT INTO SEGMENT (segment_id, line_id, length_m, gauge_code, segment_order, electrified_flag, SegmentTypetrackNumber, max_weight_kg_m) VALUES (13, 4, 8000, 116, 3, 'Y', 2, 6400);
INSERT INTO SEGMENT (segment_id, line_id, length_m, gauge_code, segment_order, electrified_flag, SegmentTypetrackNumber, max_weight_kg_m) VALUES (20, 5, 6000, 116, 1, 'Y', 2, 8000);
INSERT INTO SEGMENT (segment_id, line_id, length_m, gauge_code, segment_order, electrified_flag, SegmentTypetrackNumber, max_weight_kg_m) VALUES (21, 5, 3000, 116, 2, 'Y', 2, 8000);
INSERT INTO SEGMENT (segment_id, line_id, length_m, gauge_code, segment_order, electrified_flag, SegmentTypetrackNumber, max_weight_kg_m) VALUES (22, 5, 15000, 116, 3, 'Y', 2, 8000);
INSERT INTO SEGMENT (segment_id, line_id, length_m, gauge_code, segment_order, electrified_flag, SegmentTypetrackNumber, max_weight_kg_m) VALUES (25, 6, 20829, 116, 1, 'Y', 2, 8000);
INSERT INTO SEGMENT (segment_id, line_id, length_m, gauge_code, segment_order, electrified_flag, SegmentTypetrackNumber, max_weight_kg_m) VALUES (26, 7, 4264, 116, 1, 'Y', 2, 8000);

-- ======= SEGMENT TYPE =======
INSERT INTO SegmentType (trackNumber, type) VALUES (4, 'DOUBLE');
INSERT INTO SegmentType (trackNumber, type) VALUES (1, 'SINGLE');
INSERT INTO SegmentType (trackNumber, type) VALUES (2, 'DOUBLE');

-- ======= GAUGE =======
INSERT INTO Gauge (code, width) VALUES (1668, 116);
INSERT INTO Gauge (code, width) VALUES (1435, 100);

-- ======= OWNER =======
INSERT INTO Owner (owner_id, name) VALUES ('PT503933813', 'Infraestruturas de Portugal, SA');

-- ======= LOCOMOTIVE =======
INSERT INTO LOCOMOTIVE (locomotive_id, operator_id, entry_year, locomotive_model_id, Gaugecode) VALUES (5621,'PT509017800', 1995, 'Eurosprinter',116);
INSERT INTO LOCOMOTIVE(locomotive_id, operator_id, entry_year, locomotive_model_id, Gaugecode) VALUES (5623,'PT509017800', 1995, 'Eurosprinter',116);
INSERT INTO LOCOMOTIVE(locomotive_id, operator_id, entry_year, locomotive_model_id, Gaugecode) VALUES (5630,'PT509017800', 1996, 'Eurosprinter',116);
INSERT INTO LOCOMOTIVE(locomotive_id, operator_id, entry_year, locomotive_model_id, Gaugecode) VALUES (1903,'PT509017800', 1981, 'CP 1900',116);

-- ======= LOCOMOTIVE MODEL =======
INSERT INTO LocomotiveModel (locomotive_model_id, power, acelaration, make, lenght, weight, heigth, bogies_number, max_speed) VALUES ('Eurosprinter', 5600, 70, 'Siemens',19.2,87, 4.375, 2,220);
INSERT INTO LocomotiveModel (locomotive_model_id, power, acelaration, make, lenght, weight, heigth, bogies_number, max_speed) VALUES ('CP 1900' , 1623, 70, 'Sorefame - Alsthom', 19.084, 117, 4.310, 2, 100);

-- ======= ELECTRIC_LOCOMOTIVE =======
INSERT INTO ELECTRIC_LOCOMOTIVE(Locomotivelocomotive_id, Voltage) VALUES (5621, 25);
INSERT INTO ELECTRIC_LOCOMOTIVE(Locomotivelocomotive_id, Voltage) VALUES (5623, 25);
INSERT INTO ELECTRIC_LOCOMOTIVE(Locomotivelocomotive_id, Voltage) VALUES (5630, 25);

-- ======= DIESEL_LOCOMOTIVE =======
INSERT INTO DIESEL_LOCOMOTIVE (Locomotivelocomotive_id, fuelCapacity) VALUES (1903, 4882);

-- ======= OPERATOR  =======
INSERT INTO Operator (operator_id, name) VALUES ('PT509017800', 'Medway - Operador Ferroviário de Mercadorias, S.A');

-- ======= LOCOMOTIVE_WAGON =======
INSERT INTO LOCOMOTIVE_WAGON ( Locomotivelocomotive_id , Wagonwagon_id) VALUES (5623, 356 3 077);

-- ======= WAGON =======
INSERT INTO WAGON (wagon_id, operator_id, freight_ID, service_year, WagonModelmodelID) VALUES (3563083, 'PT509017800', 963558, 1997, 988);
INSERT INTO WAGON (wagon_id, operator_id, freight_ID, service_year, WagonModelmodelID) VALUES (3563082, 'PT509017800', 456892, 1996, 987);
INSERT INTO WAGON (wagon_id, operator_id, freight_ID, service_year, WagonModelmodelID) VALUES (3563081, 'PT509017800', 195824, 1995, 985);
INSERT INTO WAGON (wagon_id, operator_id, freight_ID, service_year, WagonModelmodelID) VALUES (3563080, 'PT509017800', 963558, 1988, 1325);
INSERT INTO WAGON (wagon_id, operator_id, freight_ID, service_year, WagonModelmodelID) VALUES (3563079, 'PT509017800', 195824, 1991, 1278);
INSERT INTO WAGON (wagon_id, operator_id, freight_ID, service_year, WagonModelmodelID) VALUES (3563078, 'PT509017800', 456892, 1990, 1245);
INSERT INTO WAGON (wagon_id, operator_id, freight_ID, service_year, WagonModelmodelID) VALUES (3563077, 'PT509017800', 963558, 1987, 1104);

-- ======= WAGON MODEL =======
INSERT INTO WAGONMODEL (modelID, name, maker, bogies_number, lenght, height, width, weight_t, max_speed,payload, gauge_code,volume) VALUES (1245,'Tadgs 32 94 082 3', 'Metalsines',2,17240, 4270, 3072, 24, 120, 56, 116, 75);
INSERT INTO WAGONMODEL (modelID, name, maker, bogies_number, lenght, height, width, weight_t, max_speed,payload, gauge_code,volume) VALUES (1278,
'Tadgs 4194 074 1', 'Equimetal', 2, 9640,  4166, 3120, 14, 120, 26, 116, 38);
INSERT INTO WAGONMODEL (modelID, name, maker, bogies_number, lenght, height, width, weight_t, max_speed,payload, gauge_code,volume) VALUES (1325,
'Gabs 8194 181 1', 'Sepsa Corr', 2, 21700, 4170, 3180, 30, 100, 50, 116, 110);
INSERT INTO WAGONMODEL (modelID, name, maker, bogies_number, lenght, height, width, weight_t, max_speed,payload, gauge_code,volume) VALUES (1104,
'Regmms 32 94 356 3', 'Metalsines', 2, 14040, 2535, 3104, 21, 120, 61, 116, 76);
INSERT INTO WAGONMODEL (modelID, name, maker, bogies_number, lenght, height, width, weight_t, max_speed,payload, gauge_code,volume) VALUES (985,
'Lgs 22 94 4416 5', 'Metalsines', 1, 13680, 1080, 2850, 12, 120, 28, 116, 76);
INSERT INTO WAGONMODEL (modelID, name, maker, bogies_number, lenght, height, width, weight_t, max_speed,payload, gauge_code,volume) VALUES (987,
'Sgnss 12 94 455 2', 'Emef', 2, 18116, 1030, 2950, 22, 120, 68, 116, 76);
INSERT INTO WAGONMODEL (modelID, name, maker, bogies_number, lenght, height, width, weight_t, max_speed,payload, gauge_code,volume) VALUES (988,
'Sgnss 12 94 455 2','Emef', 2, 18116, 1030, 2950, 22, 120, 68, 100, 76);


-- ======= FREIGHT =======
INSERT INTO FREIGHT (freight_id, cargoType, RouterouteID) VALUES (195824, 'Contrusction Materials', 12408);
INSERT INTO FREIGHT (freight_id, cargoType, RouterouteID) VALUES (456892, 'Food', 25874);
INSERT INTO FREIGHT (freight_id, cargoType, RouterouteID) VALUES (963558, 'Minerals', 12408);

-- ======= ROUTE =======
INSERT INTO ROUTE (routeid, routetype) VALUES (25874, 'COMPLEX');
INSERT INTO ROUTE (routeid, routetype) VALUES (12408, 'SIMPLE');


-- ======= ROUTE STOPS =======
INSERT INTO ROUTE_STOPS (Facilityfacility_id, order_stop, RouterouteId) VALUES (14, 1,25874);
INSERT INTO ROUTE_STOPS (Facilityfacility_id, order_stop, RouterouteId) VALUES (5, 3,12408);


