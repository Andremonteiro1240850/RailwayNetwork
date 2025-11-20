CREATE TABLE Diesel_Locomotive (
                                   LocomotiveModellocomotive_model_id varchar2(255) NOT NULL,
                                   fuelCapacity                       number(10) NOT NULL);
CREATE TABLE Electric_Locomotive (
                                     LocomotiveModellocomotive_model_id varchar2(255) NOT NULL,
                                     Voltage                            number(10) NOT NULL);
CREATE TABLE Facility (
                          facility_id          number(10) ,
                          name                 varchar2(120) NOT NULL UNIQUE,
                          Facility_typetype_id number(10) NOT NULL,
                          CONSTRAINT pk_station
                              PRIMARY KEY (facility_id),
                          CONSTRAINT ck_station_type
                              CHECK (Facility_typetype_id IN ('TERMINAL','REGULAR')));
CREATE TABLE Facility_type (
                               type_id number(10) ,
                               name    number(10) NOT NULL UNIQUE,
                               PRIMARY KEY (type_id));
CREATE TABLE Freight (
                         freight_id                   number(10) ,
                         cargoType                    varchar2(255) NOT NULL,
                         Traintrain_id                number(10) NOT NULL,
                         TrainLocomotivelocomotive_id number(10) NOT NULL,
                         TrainWagon_id                number(10) NOT NULL,
                         PRIMARY KEY (freight_id));
CREATE TABLE Gauge (
                       code  number(10) ,
                       width number(10) NOT NULL,
                       CONSTRAINT pk_gauge
                           PRIMARY KEY (code),
                       CONSTRAINT ck_gauge_width
                           CHECK (width BETWEEN 500 AND 2000));
CREATE TABLE Line (
                      line_id           number(10) ,
                      start_facility_id number(10) NOT NULL,
                      end_facility_id   number(10) NOT NULL,
                      name              varchar2(255) NOT NULL UNIQUE,
                      owner_id          varchar2(255) NOT NULL,
                      Gaugecode         number(10) NOT NULL,
                      CONSTRAINT pk_line
                          PRIMARY KEY (line_id),
                      CONSTRAINT ck_line_terminals
                          CHECK (start_facility_id <> end_facility_id));
CREATE TABLE Locomotive (
                            locomotive_id       number(10) ,
                            operator_id         varchar2(255) NOT NULL,
                            entry_year          number(4) NOT NULL,
                            locomotive_model_id varchar2(255) NOT NULL,
                            Gaugecode           number(10) NOT NULL,
                            CONSTRAINT pk_locomotive
                                PRIMARY KEY (locomotive_id),
                            CONSTRAINT ck_loco_year
                                CHECK (entry_year IS NULL OR (entry_year BETWEEN 1900 AND 2100)));

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
                                 PRIMARY KEY (locomotive_model_id));
CREATE TABLE Operator (
                          operator_id   varchar2(255) NOT NULL,
                          name          varchar2(255) NOT NULL,
                          Traintrain_id number(10) NOT NULL,
                          CONSTRAINT pk_operator
                              PRIMARY KEY (operator_id));
CREATE TABLE Owner (
                       owner_id varchar2(255) NOT NULL,
                       name     varchar2(120) NOT NULL,
                       PRIMARY KEY (owner_id));
CREATE TABLE Segment (
                         segment_id             number(10) ,
                         line_id                number(10) NOT NULL UNIQUE,
                         length_m               number(10) NOT NULL,
                         segment_order          number(10) NOT NULL UNIQUE,
                         electrified_flag       char(1) NOT NULL,
                         SegmentTypetrackNumber number(10) NOT NULL,
                         CONSTRAINT pk_segment
                             PRIMARY KEY (segment_id),
                         CONSTRAINT ck_seg_length
                             CHECK (length_m > 0),
                         CONSTRAINT ck_seg_electr
                             CHECK (electrified_flag IN ('Y','N')));

CREATE TABLE SegmentType (
                             trackNumber     number(10) ,
                             number_of_traks varchar2(255) NOT NULL,
                             PRIMARY KEY (trackNumber));
CREATE TABLE Train (
                       train_id                number(10) ,
                       Locomotivelocomotive_id number(10) NOT NULL,
                       Wagon_id                number(10) NOT NULL,
                       operator_id             varchar2(255) NOT NULL,
                       "date"                  number(10),
                       time                    number(10),
                       PRIMARY KEY (train_id));
CREATE TABLE Train_route (
                             Facility_id number(10) NOT NULL,
                             Train_id    number(10) NOT NULL,
                             "Order"     number(10) NOT NULL,
                             PRIMARY KEY (Facility_id,
                                          Train_id));
CREATE TABLE Wagon (
                       wagon_id          number(10) ,
                       operator_id       varchar2(255) NOT NULL,
                       service_year      number(10) NOT NULL,
                       WagonModelmodelID number(10) NOT NULL,
                       Gaugecode         number(10) NOT NULL,
                       CONSTRAINT pk_wagon
                           PRIMARY KEY (wagon_id));
CREATE TABLE Wagon_Freight (
                               Wagon_id   number(10) NOT NULL,
                               Freight_id number(10) NOT NULL,
                               PRIMARY KEY (Wagon_id,
                                            Freight_id));
CREATE TABLE WagonModel (
                            modelID       number(10) ,
                            name          varchar2(255) NOT NULL,
                            maker         varchar2(255) NOT NULL,
                            bogies_number number(10) NOT NULL,
                            lenght        number(10) NOT NULL,
                            height        number(10) NOT NULL,
                            width         number(10) NOT NULL,
                            weight_t      number(10) NOT NULL,
                            max_speed     number(10) NOT NULL,
                            payload       number(10) NOT NULL,
                            volume        number(10) NOT NULL,
                            CONSTRAINT pk_wagontype
                                PRIMARY KEY (modelID),
                            CONSTRAINT ck_wtype_wgt
                                CHECK (weight_t > 0),
                            CONSTRAINT ck_wtype_vol
                                CHECK (volume > 0));

ALTER TABLE Locomotive ADD CONSTRAINT FKLocomotive501990 FOREIGN KEY (operator_id) REFERENCES Operator (operator_id);
ALTER TABLE Locomotive ADD CONSTRAINT FKLocomotive52346 FOREIGN KEY (locomotive_model_id) REFERENCES LocomotiveModel (locomotive_model_id);
ALTER TABLE Train ADD CONSTRAINT FKTrain159120 FOREIGN KEY (Locomotivelocomotive_id) REFERENCES Locomotive (locomotive_id);
ALTER TABLE Line ADD CONSTRAINT FKLine692292 FOREIGN KEY (owner_id) REFERENCES Owner (owner_id);
ALTER TABLE Train ADD CONSTRAINT FKTrain508917 FOREIGN KEY (Wagon_id) REFERENCES Wagon (wagon_id);
ALTER TABLE Wagon ADD CONSTRAINT FKWagon928453 FOREIGN KEY (operator_id) REFERENCES Operator (operator_id);
ALTER TABLE Segment ADD CONSTRAINT FKSegment159053 FOREIGN KEY (SegmentTypetrackNumber) REFERENCES SegmentType (trackNumber);
ALTER TABLE Segment ADD CONSTRAINT FKSegment419961 FOREIGN KEY (line_id) REFERENCES Line (line_id);
ALTER TABLE Locomotive ADD CONSTRAINT FKLocomotive917003 FOREIGN KEY (Gaugecode) REFERENCES Gauge (code);
ALTER TABLE Wagon ADD CONSTRAINT FKWagon415380 FOREIGN KEY (WagonModelmodelID) REFERENCES WagonModel (modelID);
ALTER TABLE Line ADD CONSTRAINT FKLine520403 FOREIGN KEY (Gaugecode) REFERENCES Gauge (code);
ALTER TABLE Line ADD CONSTRAINT FKLine468673 FOREIGN KEY (start_facility_id) REFERENCES Facility (facility_id);
ALTER TABLE Train ADD CONSTRAINT FKTrain198524 FOREIGN KEY (operator_id) REFERENCES Operator (operator_id);
ALTER TABLE Diesel_Locomotive ADD CONSTRAINT FKDiesel_Loc890141 FOREIGN KEY (LocomotiveModellocomotive_model_id) REFERENCES LocomotiveModel (locomotive_model_id);
ALTER TABLE Electric_Locomotive ADD CONSTRAINT FKElectric_L109895 FOREIGN KEY (LocomotiveModellocomotive_model_id) REFERENCES LocomotiveModel (locomotive_model_id);
ALTER TABLE Wagon ADD CONSTRAINT FKWagon490540 FOREIGN KEY (Gaugecode) REFERENCES Gauge (code);
ALTER TABLE Facility ADD CONSTRAINT FKFacility132103 FOREIGN KEY (Facility_typetype_id) REFERENCES Facility_type (type_id);
ALTER TABLE Freight ADD CONSTRAINT FKFreight390491 FOREIGN KEY (Traintrain_id) REFERENCES Train (train_id);
ALTER TABLE Train_route ADD CONSTRAINT FKTrain_rout123748 FOREIGN KEY (Facility_id) REFERENCES Facility (facility_id);
ALTER TABLE Train_route ADD CONSTRAINT FKTrain_rout870091 FOREIGN KEY (Train_id) REFERENCES Train (train_id);
ALTER TABLE Wagon_Freight ADD CONSTRAINT FKWagon_Frei453527 FOREIGN KEY (Wagon_id) REFERENCES Wagon (wagon_id);
ALTER TABLE Wagon_Freight ADD CONSTRAINT FKWagon_Frei909305 FOREIGN KEY (Freight_id) REFERENCES Freight (freight_id);
ALTER TABLE Line ADD CONSTRAINT end_facility   FOREIGN KEY (end_facility_id)   REFERENCES FACILITY(facility_id);