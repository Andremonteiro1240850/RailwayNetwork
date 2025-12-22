-- Tabelas básicas sem dependências
CREATE TABLE Owner (
                       owner_id varchar2(255) NOT NULL,
                       name     varchar2(120) NOT NULL,
                       PRIMARY KEY (owner_id)
);

CREATE TABLE Operator (
                          operator_id varchar2(255) NOT NULL,
                          name        varchar2(255) NOT NULL,
                          CONSTRAINT pk_operator
                              PRIMARY KEY (operator_id)
);

CREATE TABLE Facility_type (
                               type_id number(10) NOT NULL,
                               name    number(10) NOT NULL UNIQUE,
                               PRIMARY KEY (type_id)
);

CREATE TABLE WagonType (
                           type_id   number(10) NOT NULL,
                           type_name varchar2(255) NOT NULL,
                           PRIMARY KEY (type_id)
);

CREATE TABLE BuildingType (
                              ID   number(10) NOT NULL,
                              name varchar2(255) NOT NULL UNIQUE,
                              PRIMARY KEY (ID)
);

CREATE TABLE SegmentType (
                             trackType      varchar2(255) NOT NULL,
                             numberOfTracks number(10) NOT NULL,
                             PRIMARY KEY (trackType)
);

CREATE TABLE Gauge (
                       code  number(10) NOT NULL,
                       width number(10) NOT NULL,
                       CONSTRAINT pk_gauge
                           PRIMARY KEY (code),
                       CONSTRAINT ck_gauge_width
                           CHECK (width BETWEEN 500 AND 2000)
);

CREATE TABLE Siding (
                        SIding_id number(10) NOT NULL,
                        "Size"    number(10) NOT NULL,
                        PRIMARY KEY (SIding_id)
);

-- Tabelas que dependem das básicas
CREATE TABLE Facility (
                          facility_id          number(10) NOT NULL,
                          name                 varchar2(120) NOT NULL UNIQUE,
                          Facility_typetype_id number(10) NOT NULL,
                          CONSTRAINT pk_station
                              PRIMARY KEY (facility_id)
);

CREATE TABLE LocomotiveModel (
                                 locomotive_model_id varchar2(255) NOT NULL,
                                 power               number(10) NOT NULL,
                                 acelaration         number(10) NOT NULL,
                                 make                varchar2(255) NOT NULL,
                                 lenght              number(10) NOT NULL,
                                 weight              number(10) NOT NULL,
                                 heigth              number(10) NOT NULL,
                                 max_speed           number(10) NOT NULL,
                                 bogies_number       number(10) NOT NULL,
                                 PRIMARY KEY (locomotive_model_id)
);

CREATE TABLE WagonModel (
                            modelID          number(10) NOT NULL,
                            name             varchar2(255) NOT NULL,
                            maker            varchar2(255) NOT NULL,
                            bogies_number    number(10) NOT NULL,
                            lenght           number(10) NOT NULL,
                            height           number(10) NOT NULL,
                            width            number(10) NOT NULL,
                            weight_t         number(10) NOT NULL,
                            max_speed        number(10) NOT NULL,
                            payload          number(10) NOT NULL,
                            volume           number(10) NOT NULL,
                            WagonTypetype_id number(10) NOT NULL,
                            CONSTRAINT pk_wagontype
                                PRIMARY KEY (modelID),
                            CONSTRAINT ck_wtype_wgt
                                CHECK (weight_t > 0),
                            CONSTRAINT ck_wtype_vol
                                CHECK (volume > 0)
);

-- Tabelas que dependem das anteriores
CREATE TABLE Line (
                      line_id           number(10) NOT NULL,
                      start_facility_id number(10) NOT NULL,
                      end_facility_id   number(10) NOT NULL,
                      name              varchar2(255) NOT NULL UNIQUE,
                      owner_id          varchar2(255) NOT NULL,
                      Gaugecode         number(10) NOT NULL,
                      CONSTRAINT pk_line
                          PRIMARY KEY (line_id),
                      CONSTRAINT ck_line_terminals
                          CHECK (start_facility_id <> end_facility_id)
);

CREATE TABLE Building (
                          ID                  number(10) NOT NULL,
                          name                varchar2(255) NOT NULL UNIQUE,
                          numberOfBuildings              number(10) NOT NULL,
                          BuildingTypeID      number(10) NOT NULL,
                          Facilityfacility_id number(10) NOT NULL,
                          PRIMARY KEY (ID)
);

CREATE TABLE Train (
                       train_id    number(10) NOT NULL,
                       operator_id varchar2(255) NOT NULL,
                       max_length  number(10) NOT NULL,
                       PRIMARY KEY (train_id,
                                    operator_id)
);

CREATE TABLE Wagon (
                       wagon_id          number(10) NOT NULL,
                       operator_id       varchar2(255) NOT NULL,
                       service_year      number(10) NOT NULL,
                       WagonModelmodelID number(10) NOT NULL,
                       CONSTRAINT pk_wagon
                           PRIMARY KEY (wagon_id)
);

CREATE TABLE Segment (
                         segment_id             number(10) NOT NULL,
                         line_id                number(10) NOT NULL,
                         length_m               number(10) NOT NULL,
                         segment_order          number(10) NOT NULL,
                         electrified_flag       char(1) NOT NULL,
                         SegmentTypetrackNumber number(10) NOT NULL,
                         SegmentTypetrackType   varchar2(255) NOT NULL,
                         SidingSIding_id        number(10),
                         CONSTRAINT pk_segment
                             PRIMARY KEY (segment_id),
                         CONSTRAINT ck_seg_length
                             CHECK (length_m > 0),
                         CONSTRAINT ck_seg_electr
                             CHECK (electrified_flag IN ('Y','N')),
                         CONSTRAINT ck_seg_track
                             CHECK (SegmentTypetrackType IN ('SINGLE','DOUBLE'))
);

-- Adicionar constraint UNIQUE para segment_order (após criar a tabela)
ALTER TABLE Segment ADD CONSTRAINT uq_segment_order UNIQUE (segment_order);

-- Tabelas especializadas (herança)
CREATE TABLE Diesel_Locomotive (
                                   LocomotiveModellocomotive_model_id varchar2(255) NOT NULL,
                                   fuelCapacity                       number(10) NOT NULL,
                                   PRIMARY KEY (LocomotiveModellocomotive_model_id)
);

CREATE TABLE Electric_Locomotive (
                                     LocomotiveModellocomotive_model_id varchar2(255) NOT NULL,
                                     Voltage                            number(10) NOT NULL,
                                     PRIMARY KEY (LocomotiveModellocomotive_model_id)
);

-- Tabelas com mais dependências
CREATE TABLE Locomotive (
                            locomotive_id       number(10) NOT NULL,
                            operator_id         varchar2(255) NOT NULL,
                            entry_year          number(4) NOT NULL,
                            locomotive_model_id varchar2(255) NOT NULL,
                            Traintrain_id       number(10) NOT NULL,
                            Trainoperator_id    varchar2(255) NOT NULL,
                            CONSTRAINT pk_locomotive
                                PRIMARY KEY (locomotive_id),
                            CONSTRAINT ck_loco_year
                                CHECK (entry_year BETWEEN 1900 AND 2100)
);

CREATE TABLE Train_route (
                             Facility_id                  number(10) NOT NULL,
                             Train_id                     number(10) NOT NULL,
                             "Order"                      number(10) NOT NULL,
                             TrainLocomotivelocomotive_id number(10) NOT NULL,
                             Trainoperator_id             varchar2(255) NOT NULL,
                             departure_time               number(10) NOT NULL,
                             Arrival_Time                 number(10) NOT NULL,
                             PRIMARY KEY (Facility_id,
                                          Train_id,
                                          TrainLocomotivelocomotive_id,
                                          Trainoperator_id)
);

CREATE TABLE FreighTrain (
                             Freigth_id                   number(10) NOT NULL,
                             Traintrain_id                number(10) NOT NULL,
                             TrainLocomotivelocomotive_id number(10) NOT NULL,
                             Trainoperator_id             varchar2(255) NOT NULL,
                             PRIMARY KEY (Freigth_id,
                                          Traintrain_id)
);

CREATE TABLE Freight (
                         freight_id               number(10) NOT NULL,
                         Originfacility_id        number(10) NOT NULL,
                         Destinationfacility_id   number(10) NOT NULL,
                         FreighTrainFreigth_id    number(10) NOT NULL,
                         createDate               timestamp(0) NOT NULL,
                         FreighTrainTraintrain_id number(10) NOT NULL,
                         PRIMARY KEY (freight_id)
);

-- Tabelas de relacionamento muitos-para-muitos
CREATE TABLE Locomotive_Gauge (
                                  Locomotivelocomotive_id number(10) NOT NULL,
                                  Gaugecode               number(10) NOT NULL,
                                  PRIMARY KEY (Locomotivelocomotive_id,
                                               Gaugecode)
);

CREATE TABLE Locomotive_Train (
                                  Locomotivelocomotive_id number(10) NOT NULL,
                                  Traintrain_id           number(10) NOT NULL,
                                  Trainoperator_id        varchar2(255) NOT NULL,
                                  Adddate                 timestamp(0) NOT NULL,
                                  PRIMARY KEY (Locomotivelocomotive_id,
                                               Traintrain_id,
                                               Trainoperator_id)
);

CREATE TABLE Gauge_Wagon (
                             Gaugecode     number(10) NOT NULL,
                             Wagonwagon_id number(10) NOT NULL,
                             PRIMARY KEY (Gaugecode,
                                          Wagonwagon_id)
);

CREATE TABLE Wagon_In_Freight (
                                  Wagon_id   number(10) NOT NULL,
                                  Freight_id number(10) NOT NULL,
                                  PRIMARY KEY (Wagon_id,
                                               Freight_id)
);

-- Tabelas de log
CREATE TABLE TrainLog (
                          Traintrain_id         number(10) NOT NULL,
                          Trainoperator_id      varchar2(255) NOT NULL,
                          Arrival               timestamp(0) NOT NULL,
                          TrainLogTraintrain_id number(10) NOT NULL,
                          PRIMARY KEY (Traintrain_id)
);

CREATE TABLE TrainLogDeparture (
                                   TrainLogTraintrain_id number(10) NOT NULL,
                                   Departure             timestamp(0) NOT NULL,
                                   PRIMARY KEY (TrainLogTraintrain_id)
);

CREATE TABLE FreightLog (
                            Freightfreight_id number(10) NOT NULL,
                            unloadFreightDate timestamp(0) NOT NULL,
                            PRIMARY KEY (Freightfreight_id)
);

ALTER TABLE Locomotive ADD CONSTRAINT FKLocomotive501990 FOREIGN KEY (operator_id) REFERENCES Operator (operator_id);
ALTER TABLE Locomotive ADD CONSTRAINT FKLocomotive52346 FOREIGN KEY (locomotive_model_id) REFERENCES LocomotiveModel (locomotive_model_id);
ALTER TABLE Line ADD CONSTRAINT FKLine692292 FOREIGN KEY (owner_id) REFERENCES Owner (owner_id);
ALTER TABLE Wagon ADD CONSTRAINT FKWagon928453 FOREIGN KEY (operator_id) REFERENCES Operator (operator_id);
ALTER TABLE Segment ADD CONSTRAINT FKSegment419961 FOREIGN KEY (line_id) REFERENCES Line (line_id);
ALTER TABLE Wagon ADD CONSTRAINT FKWagon415380 FOREIGN KEY (WagonModelmodelID) REFERENCES WagonModel (modelID);
ALTER TABLE Line ADD CONSTRAINT FKLine520403 FOREIGN KEY (Gaugecode) REFERENCES Gauge (code);
ALTER TABLE Line ADD CONSTRAINT FKLine468673 FOREIGN KEY (start_facility_id) REFERENCES Facility (facility_id);
ALTER TABLE Train ADD CONSTRAINT FKTrain198524 FOREIGN KEY (operator_id) REFERENCES Operator (operator_id);
ALTER TABLE Diesel_Locomotive ADD CONSTRAINT FKDiesel_Loc890141 FOREIGN KEY (LocomotiveModellocomotive_model_id) REFERENCES LocomotiveModel (locomotive_model_id);
ALTER TABLE Electric_Locomotive ADD CONSTRAINT FKElectric_L109895 FOREIGN KEY (LocomotiveModellocomotive_model_id) REFERENCES LocomotiveModel (locomotive_model_id);
ALTER TABLE Facility ADD CONSTRAINT FKFacility132103 FOREIGN KEY (Facility_typetype_id) REFERENCES Facility_type (type_id);
ALTER TABLE Train_route ADD CONSTRAINT FKTrain_rout123748 FOREIGN KEY (Facility_id) REFERENCES Facility (facility_id);
ALTER TABLE Train_route ADD CONSTRAINT FKTrain_rout634390 FOREIGN KEY (Train_id, Trainoperator_id) REFERENCES Train (train_id, operator_id);
ALTER TABLE Wagon_In_Freight ADD CONSTRAINT FKWagon_In_F984764 FOREIGN KEY (Wagon_id) REFERENCES Wagon (wagon_id);
ALTER TABLE Wagon_In_Freight ADD CONSTRAINT FKWagon_In_F376007 FOREIGN KEY (Freight_id) REFERENCES Freight (freight_id);
ALTER TABLE Building ADD CONSTRAINT FKBuilding466711 FOREIGN KEY (BuildingTypeID) REFERENCES BuildingType (ID);
ALTER TABLE Building ADD CONSTRAINT FKBuilding137571 FOREIGN KEY (Facilityfacility_id) REFERENCES Facility (facility_id);
ALTER TABLE WagonModel ADD CONSTRAINT FKWagonModel883591 FOREIGN KEY (WagonTypetype_id) REFERENCES WagonType (type_id);
ALTER TABLE Freight ADD CONSTRAINT FKFreight98400 FOREIGN KEY (Originfacility_id) REFERENCES Facility (facility_id);
ALTER TABLE Freight ADD CONSTRAINT FKFreight481888 FOREIGN KEY (Destinationfacility_id) REFERENCES Facility (facility_id);
ALTER TABLE Locomotive_Gauge ADD CONSTRAINT FKLocomotive722234 FOREIGN KEY (Locomotivelocomotive_id) REFERENCES Locomotive (locomotive_id);
ALTER TABLE Locomotive_Gauge ADD CONSTRAINT FKLocomotive660884 FOREIGN KEY (Gaugecode) REFERENCES Gauge (code);
ALTER TABLE Gauge_Wagon ADD CONSTRAINT FKGauge_Wago413095 FOREIGN KEY (Gaugecode) REFERENCES Gauge (code);
ALTER TABLE Gauge_Wagon ADD CONSTRAINT FKGauge_Wago134878 FOREIGN KEY (Wagonwagon_id) REFERENCES Wagon (wagon_id);
ALTER TABLE Segment ADD CONSTRAINT FKSegment61156 FOREIGN KEY (SegmentTypetrackType) REFERENCES SegmentType (trackType);
ALTER TABLE Segment ADD CONSTRAINT FKSegment266428 FOREIGN KEY (SidingSIding_id) REFERENCES Siding (SIding_id);
ALTER TABLE Freight ADD CONSTRAINT FKFreight567510 FOREIGN KEY (FreighTrainFreigth_id, FreighTrainTraintrain_id) REFERENCES FreighTrain (Freigth_id, Traintrain_id);
ALTER TABLE FreighTrain ADD CONSTRAINT FKFreighTrai401789 FOREIGN KEY (Traintrain_id, Trainoperator_id) REFERENCES Train (train_id, operator_id);
ALTER TABLE Locomotive_Train ADD CONSTRAINT FKLocomotive229151 FOREIGN KEY (Locomotivelocomotive_id) REFERENCES Locomotive (locomotive_id);
ALTER TABLE Locomotive_Train ADD CONSTRAINT FKLocomotive952775 FOREIGN KEY (Traintrain_id, Trainoperator_id) REFERENCES Train (train_id, operator_id);
ALTER TABLE TrainLog ADD CONSTRAINT FKTrainLog867807 FOREIGN KEY (Traintrain_id, Trainoperator_id) REFERENCES Train (train_id, operator_id);
ALTER TABLE TrainLogDeparture ADD CONSTRAINT FKTrainLogDe819706 FOREIGN KEY (TrainLogTraintrain_id) REFERENCES TrainLog (Traintrain_id);
ALTER TABLE FreightLog ADD CONSTRAINT FKFreightLog241845 FOREIGN KEY (Freightfreight_id) REFERENCES Freight (freight_id);
ALTER TABLE Line ADD CONSTRAINT End_facility FOREIGN KEY (end_facility_id) REFERENCES Facility (facility_id);

/* ===========================================
   BEGIN: DROP TABLES (IF NECESSARY)
   =========================================== */
-- DROP TABLE FreightLog CASCADE CONSTRAINTS;
-- DROP TABLE TrainLogDeparture CASCADE CONSTRAINTS;
-- DROP TABLE TrainLog CASCADE CONSTRAINTS;
-- DROP TABLE Train_route CASCADE CONSTRAINTS;
-- DROP TABLE Wagon_In_Freight CASCADE CONSTRAINTS;
-- DROP TABLE Gauge_Wagon CASCADE CONSTRAINTS;
-- DROP TABLE Locomotive_Gauge CASCADE CONSTRAINTS;
-- DROP TABLE Locomotive_Train CASCADE CONSTRAINTS;
-- DROP TABLE FreighTrain CASCADE CONSTRAINTS;
-- DROP TABLE Freight CASCADE CONSTRAINTS;
-- DROP TABLE Segment CASCADE CONSTRAINTS;
-- DROP TABLE Siding CASCADE CONSTRAINTS;
-- DROP TABLE SegmentType CASCADE CONSTRAINTS;
-- DROP TABLE Line CASCADE CONSTRAINTS;
-- DROP TABLE Wagon CASCADE CONSTRAINTS;
-- DROP TABLE WagonModel CASCADE CONSTRAINTS;
-- DROP TABLE WagonType CASCADE CONSTRAINTS;
-- DROP TABLE Locomotive CASCADE CONSTRAINTS;
-- DROP TABLE Diesel_Locomotive CASCADE CONSTRAINTS;
-- DROP TABLE Electric_Locomotive CASCADE CONSTRAINTS;
-- DROP TABLE LocomotiveModel CASCADE CONSTRAINTS;
-- DROP TABLE Train CASCADE CONSTRAINTS;
-- DROP TABLE Building CASCADE CONSTRAINTS;
-- DROP TABLE BuildingType CASCADE CONSTRAINTS;
-- DROP TABLE Facility CASCADE CONSTRAINTS;
-- DROP TABLE Facility_type CASCADE CONSTRAINTS;
-- DROP TABLE Gauge CASCADE CONSTRAINTS;
-- DROP TABLE Operator CASCADE CONSTRAINTS;
-- DROP TABLE Owner CASCADE CONSTRAINTS;
/* ===========================================
   END: DROP TABLES
   =========================================== */
