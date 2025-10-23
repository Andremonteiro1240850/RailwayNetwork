CREATE TABLE Diesel_Locomotive (
                                   locomotive_id number(10) GENERATED AS IDENTITY,
                                   lcomotive_id  number(10) NOT NULL,
                                   PRIMARY KEY (locomotive_id));
CREATE TABLE Electric_Locomotive (
                                     locomotive_id           number(10) GENERATED AS IDENTITY,
                                     Locomotivelocomotive_id number(10) NOT NULL,
                                     PRIMARY KEY (locomotive_id));
CREATE TABLE Facility (
                          facility_id   number(10) GENERATED AS IDENTITY,
                          name          varchar2(120) NOT NULL UNIQUE,
                          facility_type varchar2(10) NOT NULL UNIQUE,
                          CONSTRAINT pk_facility
                              PRIMARY KEY (facility_id),
                          CONSTRAINT ck_station_type
                              CHECK (facility_type IN ('TERMINAL','REGULAR')));
CREATE TABLE Freight (
                         freight_id          number(10) GENERATED AS IDENTITY,
                         initial_facility_id number(10) NOT NULL UNIQUE,
                         final_facility_id   number(10) NOT NULL UNIQUE,
                         route_id            number(10) NOT NULL,
                         PRIMARY KEY (freight_id));
CREATE TABLE Gauge (
                       gauge_code  varchar2(20) NOT NULL,
                       width       number(5) NOT NULL UNIQUE,
                       description number(10) NOT NULL UNIQUE,
                       line_id     number(10) NOT NULL,
                       CONSTRAINT pk_gauge
                           PRIMARY KEY (gauge_code),
                       CONSTRAINT ck_gauge_width
                           CHECK (width BETWEEN 500 AND 2000));
CREATE TABLE Line (
                      line_id           number(10) NOT NULL,
                      facility_id       number(10) NOT NULL UNIQUE,
                      start_facility_id number(10) NOT NULL UNIQUE,
                      end_facility_id   number(10) NOT NULL UNIQUE,
                      gauge_code        varchar2(20) NOT NULL,
                      name              varchar2(255) NOT NULL UNIQUE,
                      owner_id          number(10) NOT NULL,
                      CONSTRAINT pk_line
                          PRIMARY KEY (line_id,
                                       facility_id),
                      CONSTRAINT ck_line_terminals
                          CHECK (start_facility_id <> end_facility_id));
CREATE TABLE Locomotive (
                            locomotive_id       number(10) GENERATED AS IDENTITY,
                            locomotive_type     varchar2(10) NOT NULL,
                            gauge_code          varchar2(20) NOT NULL,
                            operator_id         number(10) NOT NULL,
                            entry_year          number(4) NOT NULL UNIQUE,
                            locomotive_model_id number(10) NOT NULL,
                            height              number(10),
                            weight              number(10),
                            lenght              number(10),
                            make                number(10),
                            power               number(10),
                            fuel_capacity       number(10),
                            CONSTRAINT pk_locomotive
                                PRIMARY KEY (locomotive_id),
                            CONSTRAINT ck_loco_year
                                CHECK (entry_year IS NULL OR (entry_year BETWEEN 1900 AND 2100)),
                            CONSTRAINT ck_locomotive_type
                                CHECK (locomotive_type IN ('DIESEL','ELECTRIC')));
CREATE TABLE Locomotive_Wagon (
                                  Locomotivelocomotive_id number(10) NOT NULL,
                                  Wagonwagon_id           number(10) NOT NULL,
                                  PRIMARY KEY (Locomotivelocomotive_id,
                                               Wagonwagon_id));
CREATE TABLE LocomotiveModel (
                                 locomotive_model_id number(10) GENERATED AS IDENTITY,
                                 name                number(10),
                                 power               number(10),
                                 acelaration         number(10),
                                 fuel_capacity       number(10),
                                 max_weight          number(10),
                                 PRIMARY KEY (locomotive_model_id));
CREATE TABLE Operator (
                          operator_id number(10) GENERATED AS IDENTITY,
                          name        varchar2(255) NOT NULL UNIQUE,
                          CONSTRAINT pk_operator
                              PRIMARY KEY (operator_id));
CREATE TABLE Owner (
                       owner_id number(10) GENERATED AS IDENTITY,
                       name     varchar2(120) NOT NULL UNIQUE,
                       contact  varchar2(60) NOT NULL UNIQUE,
                       PRIMARY KEY (owner_id));
CREATE TABLE Route (
                       route_id      number(10) GENERATED AS IDENTITY,
                       route_type_id number(10) NOT NULL,
                       PRIMARY KEY (route_id));
CREATE TABLE Route_Type (
                            route_type_id number(10) GENERATED AS IDENTITY,
                            PRIMARY KEY (route_type_id));
CREATE TABLE Segment (
                         segment_id        number(10) GENERATED AS IDENTITY,
                         line_id           number(10) NOT NULL,
                         segment_type_code varchar2(20) NOT NULL,
                         length_m          number(10) NOT NULL
    gauge_code        varchar2(20) NOT NULL,
                         order_code        number(10),
                         electrified_flag  varchar(1) NOT NULL,
                         Linefacility_id   number(10) NOT NULL,
                         CONSTRAINT pk_segment
                             PRIMARY KEY (segment_id),
                         CONSTRAINT ck_seg_length
                             CHECK (length_m > 0),
                         CONSTRAINT ck_seg_electr
                             CHECK (electrified_flag IN ('Y','N')),
                         CONSTRAINT ck_seg_track
                             CHECK (segment_type_code IN ('SINGLE','DOUBLE')));
CREATE TABLE SegmentType (
                             track_type_code number(10) GENERATED AS IDENTITY,
                             description     varchar2(10) NOT NULL UNIQUE,
                             PRIMARY KEY (track_type_code));
CREATE TABLE Wagon (
                       wagon_id        number(10) GENERATED AS IDENTITY,
                       operator_id     number(10) NOT NULL UNIQUE,

                       wagon_type_code number(10) NOT NULL,
                       serial_code     varchar2(255) NOT NULL UNIQUE,
                       locomotive_id   number(10) NOT NULL,
                       freight_ID      number(10) NOT NULL,
                       service_year    number(10) NOT NULL,
                       CONSTRAINT pk_wagon
                           PRIMARY KEY (wagon_id));
CREATE TABLE WagonModel (
                            wagon_type_code number(10) GENERATED AS IDENTITY,
                            description     number(10) NOT NULL UNIQUE,
                            volume_m3       number(10) NOT NULL,
                            lenght          number(10),
                            height          number(10),
                            weight_t        number(10),
                            max_speed       number(10),
                            payload         number(10),
                            gauge_code      varchar2(20) NOT NULL,
                            CONSTRAINT pk_wagontype
                                PRIMARY KEY (wagon_type_code),
                            CONSTRAINT ck_wtype_wgt
                                CHECK (weight_t > 0),
                            CONSTRAINT ck_wtype_vol
                                CHECK (volume_m3 > 0));
CREATE TABLE Warehouse (
                           warehouse_id number(10) GENERATED AS IDENTITY,
                           facility_id  number(10) NOT NULL,
                           PRIMARY KEY (warehouse_id));
ALTER TABLE Locomotive ADD CONSTRAINT FKLocomotive501990 FOREIGN KEY (operator_id) REFERENCES Operator (operator_id);
ALTER TABLE Warehouse ADD CONSTRAINT FKWarehouse302124 FOREIGN KEY (facility_id) REFERENCES Facility (facility_id);
ALTER TABLE Locomotive ADD CONSTRAINT FKLocomotive985555 FOREIGN KEY (gauge_code) REFERENCES Gauge (gauge_code);
ALTER TABLE Diesel_Locomotive ADD CONSTRAINT FKDiesel_Loc906374 FOREIGN KEY (lcomotive_id) REFERENCES Locomotive (locomotive_id);
ALTER TABLE Route ADD CONSTRAINT FKRoute956766 FOREIGN KEY (route_type_id) REFERENCES Route_Type (route_type_id);
ALTER TABLE Freight ADD CONSTRAINT FKFreight967660 FOREIGN KEY (route_id) REFERENCES Route (route_id);
ALTER TABLE Locomotive ADD CONSTRAINT FKLocomotive52346 FOREIGN KEY (locomotive_model_id) REFERENCES LocomotiveModel (locomotive_model_id);
ALTER TABLE Locomotive_Wagon ADD CONSTRAINT FKLocomotive959080 FOREIGN KEY (Locomotivelocomotive_id) REFERENCES Locomotive (locomotive_id);
ALTER TABLE Line ADD CONSTRAINT FKLine692292 FOREIGN KEY (owner_id) REFERENCES Owner (owner_id);
ALTER TABLE Locomotive_Wagon ADD CONSTRAINT FKLocomotive525886 FOREIGN KEY (Wagonwagon_id) REFERENCES Wagon (wagon_id);
ALTER TABLE Wagon ADD CONSTRAINT FKWagon928453 FOREIGN KEY (operator_id) REFERENCES Operator (operator_id);
ALTER TABLE Wagon ADD CONSTRAINT FKWagon2333 FOREIGN KEY (wagon_type_code) REFERENCES WagonModel (wagon_type_code);
ALTER TABLE Wagon ADD CONSTRAINT FKWagon80136 FOREIGN KEY (freight_ID) REFERENCES Freight (freight_id);
ALTER TABLE Electric_Locomotive ADD CONSTRAINT FKElectric_L446383 FOREIGN KEY (Locomotivelocomotive_id) REFERENCES Locomotive (locomotive_id);
ALTER TABLE Line ADD CONSTRAINT FKLine313791 FOREIGN KEY (facility_id) REFERENCES Facility (facility_id);
ALTER TABLE Line ADD CONSTRAINT FKLine382156 FOREIGN KEY (gauge_code) REFERENCES Gauge (gauge_code);
ALTER TABLE Segment ADD CONSTRAINT FKSegment294611 FOREIGN KEY (segment_type_code) REFERENCES SegmentType (track_type_code);
ALTER TABLE Segment ADD CONSTRAINT FKSegment342432 FOREIGN KEY (gauge_code) REFERENCES Gauge (gauge_code);
ALTER TABLE Segment ADD CONSTRAINT FKSegment231508 FOREIGN KEY (line_id, Linefacility_id) REFERENCES Line (line_id, facility_id);
ALTER TABLE WagonModel ADD CONSTRAINT FKWagonModel726157 FOREIGN KEY (gauge_code) REFERENCES Gauge (gauge_code);