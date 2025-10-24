CREATE TABLE Diesel_Locomotive (
                                   Locomotivelocomotive_id number(10) NOT NULL,
                                   fuelCapacity            number(10) NOT NULL,
                                   PRIMARY KEY (Locomotivelocomotive_id));
CREATE TABLE Electric_Locomotive (
                                     Locomotivelocomotive_id number(10) NOT NULL,
                                     Voltage                 number(10) NOT NULL,
                                     PRIMARY KEY (Locomotivelocomotive_id));
CREATE TABLE Facility (
                          facility_id   number(10) GENERATED AS IDENTITY,
                          name          varchar2(120) NOT NULL UNIQUE,
                          facility_type varchar2(10) NOT NULL,
                          CONSTRAINT pk_station
                              PRIMARY KEY (facility_id),
                          CONSTRAINT ck_station_type
                              CHECK (facility_type IN ('TERMINAL','REGULAR')));
CREATE TABLE Facility_Line (
                               Facilityfacility_id number(10) NOT NULL,
                               Lineline_id         number(10) NOT NULL,
                               PRIMARY KEY (Facilityfacility_id,
                                            Lineline_id));
CREATE TABLE Freight (
                         freight_id   number(10) GENERATED AS IDENTITY,
                         cargoType    varchar2(255) NOT NULL,
                         RouterouteID number(10) NOT NULL,
                         PRIMARY KEY (freight_id));
CREATE TABLE Gauge (
                       code  number(10) GENERATED AS IDENTITY,
                       width number(10) NOT NULL,
                       CONSTRAINT pk_gauge
                           PRIMARY KEY (code),
                       CONSTRAINT ck_gauge_width
                           CHECK (width BETWEEN 500 AND 2000));
CREATE TABLE Line (
                      line_id           number(10) GENERATED AS IDENTITY,
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
                            locomotive_id       number(10) GENERATED AS IDENTITY,
                            operator_id         varchar2(255) NOT NULL,
                            entry_year          number(4) NOT NULL,
                            locomotive_model_id varchar2(255) NOT NULL,
                            Gaugecode           number(10) NOT NULL,
                            CONSTRAINT pk_locomotive
                                PRIMARY KEY (locomotive_id),
                            CONSTRAINT ck_loco_year
                                CHECK (entry_year BETWEEN 1900 AND 2100),
                            CONSTRAINT ck_loco_type
                                CHECK (locomotive_model_id IN ('DIESEL','ELECTRIC')));
CREATE TABLE Locomotive_Wagon (
                                  Locomotivelocomotive_id number(10) NOT NULL,
                                  Wagonwagon_id           number(10) NOT NULL,
                                  PRIMARY KEY (Locomotivelocomotive_id,
                                               Wagonwagon_id));
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
                          operator_id varchar2(255) NOT NULL,
                          name        varchar2(255) NOT NULL,
                          CONSTRAINT pk_operator
                              PRIMARY KEY (operator_id));
CREATE TABLE Owner (
                       owner_id varchar2(255) NOT NULL,
                       name     varchar2(120) NOT NULL,
                       PRIMARY KEY (owner_id));
CREATE TABLE Route (
                       routeID   number(10) GENERATED AS IDENTITY,
                       routeType varchar2(255) NOT NULL,
                       PRIMARY KEY (routeID));
CREATE TABLE Route_Stops (
                             Facilityfacility_id number(10) NOT NULL,
                             order_stop          number(10) NOT NULL UNIQUE,
                             RouterouteID        number(10) NOT NULL,
                             PRIMARY KEY (Facilityfacility_id));
CREATE TABLE Segment (
                         segment_id             number(10) GENERATED AS IDENTITY,
                         line_id                number(10) NOT NULL,
                         length_m               number(10) NOT NULL,
                         gauge_code             number(10) NOT NULL,
                         segment_order          number(10) NOT NULL,
                         electrified_flag       char(1) NOT NULL,
                         SegmentTypetrackNumber number(10) NOT NULL,
                         CONSTRAINT pk_segment
                             PRIMARY KEY (segment_id),
                         CONSTRAINT ck_seg_length
                             CHECK (length_m > 0),

                         CONSTRAINT ck_seg_electr
                             CHECK (electrified_flag IN ('Y','N')),

CREATE TABLE SegmentType (
                             trackNumber number(10) GENERATED AS IDENTITY,
                             type        varchar2(255) NOT NULL,
                             PRIMARY KEY (trackNumber));
CREATE TABLE Wagon (
                       wagon_id          number(10) GENERATED AS IDENTITY,
                       operator_id       varchar2(255) NOT NULL,
                       freight_ID        number(10),
                       service_year      number(10) NOT NULL,
                       WagonModelmodelID number(10) NOT NULL,
                       CONSTRAINT pk_wagon
                           PRIMARY KEY (wagon_id));
CREATE TABLE WagonModel (
                            modelID       number(10) GENERATED AS IDENTITY,
                            name          varchar2(255) NOT NULL,
                            maker         varchar2(255) NOT NULL,
                            bogies_number number(10) NOT NULL,
                            lenght        number(10) NOT NULL,
                            height        number(10) NOT NULL,
                            width         number(10) NOT NULL,
                            weight_t      number(10) NOT NULL,
                            max_speed     number(10) NOT NULL,
                            payload       number(10) NOT NULL,
                            gauge_code    number(10) NOT NULL,
                            volume        number(10) NOT NULL,
                            CONSTRAINT pk_wagontype
                                PRIMARY KEY (modelID),
                            CONSTRAINT ck_wtype_wgt
                                CHECK (weight_t > 0),
                            CONSTRAINT ck_wtype_vol
                                CHECK (volume > 0));
ALTER TABLE Locomotive ADD CONSTRAINT FKLocomotive501990 FOREIGN KEY (operator_id) REFERENCES Operator (operator_id);
ALTER TABLE Locomotive ADD CONSTRAINT FKLocomotive52346 FOREIGN KEY (locomotive_model_id) REFERENCES LocomotiveModel (locomotive_model_id);
ALTER TABLE Locomotive_Wagon ADD CONSTRAINT FKLocomotive959080 FOREIGN KEY (Locomotivelocomotive_id) REFERENCES Locomotive (locomotive_id);
ALTER TABLE Line ADD CONSTRAINT FKLine692292 FOREIGN KEY (owner_id) REFERENCES Owner (owner_id);
ALTER TABLE Locomotive_Wagon ADD CONSTRAINT FKLocomotive525886 FOREIGN KEY (Wagonwagon_id) REFERENCES Wagon (wagon_id);
ALTER TABLE Wagon ADD CONSTRAINT FKWagon928453 FOREIGN KEY (operator_id) REFERENCES Operator (operator_id);
ALTER TABLE Wagon ADD CONSTRAINT FKWagon80136 FOREIGN KEY (freight_ID) REFERENCES Freight (freight_id);
ALTER TABLE Segment ADD CONSTRAINT FKSegment159053 FOREIGN KEY (SegmentTypetrackNumber) REFERENCES SegmentType (trackNumber);
ALTER TABLE Segment ADD CONSTRAINT FKSegment120144 FOREIGN KEY (gauge_code) REFERENCES Gauge (code);
ALTER TABLE Segment ADD CONSTRAINT FKSegment419961 FOREIGN KEY (line_id) REFERENCES Line (line_id);
ALTER TABLE WagonModel ADD CONSTRAINT FKWagonModel503869 FOREIGN KEY (gauge_code) REFERENCES Gauge (code);
ALTER TABLE Line ADD CONSTRAINT FKLine520403 FOREIGN KEY (Gaugecode) REFERENCES Gauge (code);
ALTER TABLE Locomotive ADD CONSTRAINT FKLocomotive917003 FOREIGN KEY (Gaugecode) REFERENCES Gauge (code);
ALTER TABLE Diesel_Locomotive ADD CONSTRAINT FKDiesel_Loc226630 FOREIGN KEY (Locomotivelocomotive_id) REFERENCES Locomotive (locomotive_id);
ALTER TABLE Electric_Locomotive ADD CONSTRAINT FKElectric_L446383 FOREIGN KEY (Locomotivelocomotive_id) REFERENCES Locomotive (locomotive_id);
ALTER TABLE Wagon ADD CONSTRAINT FKWagon415380 FOREIGN KEY (WagonModelmodelID) REFERENCES WagonModel (modelID);
ALTER TABLE Route_Stops ADD CONSTRAINT FKRoute_Stop809322 FOREIGN KEY (Facilityfacility_id) REFERENCES Facility (facility_id);
ALTER TABLE Route_Stops ADD CONSTRAINT FKRoute_Stop833992 FOREIGN KEY (RouterouteID) REFERENCES Route (routeID);
ALTER TABLE Facility_Line ADD CONSTRAINT FKFacility_L661163 FOREIGN KEY (Facilityfacility_id) REFERENCES Facility (facility_id);
ALTER TABLE Facility_Line ADD CONSTRAINT FKFacility_L950460 FOREIGN KEY (Lineline_id) REFERENCES Line (line_id);
ALTER TABLE Freight ADD CONSTRAINT FKFreight180461 FOREIGN KEY (RouterouteID) REFERENCES Route (routeID);
