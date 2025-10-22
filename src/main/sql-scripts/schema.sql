CREATE TABLE Bay (
                     bay_id       number(10) GENERATED AS IDENTITY,
                     warehouse_id number(10) NOT NULL,
                     capacity_n   number(10) NOT NULL,
                     PRIMARY KEY (bay_id));
CREATE TABLE Diesel_Locomotive (
                                   locomotive_id number(10) GENERATED AS IDENTITY,
                                   llcomotive_id number(10) NOT NULL,
                                   PRIMARY KEY (locomotive_id));
CREATE TABLE Electric_Locomotive (
                                     locomotive_id           number(10) GENERATED AS IDENTITY,
                                     Locomotivelocomotive_id number(10) NOT NULL,
                                     PRIMARY KEY (locomotive_id));
CREATE TABLE Freight (
                         freight_id         number(10) GENERATED AS IDENTITY,
                         initial_station_id number(10) NOT NULL UNIQUE,
                         final_station_id   number(10) NOT NULL UNIQUE,
                         route_id           number(10) NOT NULL,
                         PRIMARY KEY (freight_id));
CREATE TABLE Gauge (
                       gauge_code       varchar2(20) NOT NULL UNIQUE,
                       line_start_point number(10) NOT NULL,
                       line_end_point   number(10) NOT NULL,
                       linestation_id   number(10) NOT NULL,
                       width            number(10) NOT NULL UNIQUE,
                       description      number(10) NOT NULL UNIQUE,
                       line_id          number(10) NOT NULL,
                       CONSTRAINT pk_gauge
                           PRIMARY KEY (gauge_code,
                                        line_start_point,
                                        line_end_point),
                       CONSTRAINT ck_gauge_width
                           CHECK (width BETWEEN 500 AND 2000));
CREATE TABLE Line (
                      station_id number(10) NOT NULL UNIQUE,
                      start_station_id number(10) NOT NULL,
                      end_station_id   number(10) NOT NULL,
                      gauge_code varchar2(20) NOT NULL UNIQUE,
                      name       number(10) NOT NULL UNIQUE,
                      owner_id   number(10) NOT NULL,
                      start_id   number(10),
                      end_id     number(10),
                      CONSTRAINT pk_line
                          PRIMARY KEY (station_id,
                                       start_station_id,
                                       end_station_id),
                      CONSTRAINT ck_line_terminals
                          CHECK (start_station_id <> end_station_id));
CREATE TABLE Locomotive (
                            locomotive_id          number(10) GENERATED AS IDENTITY,
                            locomotive_type        varchar2(10) NOT NULL,
                            owner_id               number(10) NOT NULL,
                            gauge_code             varchar2(20) NOT NULL,
                            operator_id            number(10) NOT NULL,
                            entry_year             number(4) NOT NULL UNIQUE,
                            train_id               number(10) NOT NULL,
                            locomotive_model_id    number(10) NOT NULL,
                            gauge_line_start_point number(10) NOT NULL,
                            gauge_line_end_pointt  number(10) NOT NULL,
                            height                 number(10),
                            weight                 number(10),
                            lenght                 number(10),
                            CONSTRAINT pk_locomotive
                                PRIMARY KEY (locomotive_id),
                            CONSTRAINT ck_loco_year
                                CHECK (entry_year IS NULL OR (entry_year BETWEEN 1900 AND 2100)),
                            CONSTRAINT ck_loco_type
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
                         segment_id          NUMBER(10) GENERATED AS IDENTITY,
                         segment_type_code   NUMBER(10) NOT NULL,
                         length_km           NUMBER(7) NOT NULL,
                         line_id             NUMBER(10) NOT NULL,
                         gauge_code          VARCHAR2(20) NOT NULL,
                         linestation_id      NUMBER(10) NOT NULL,
                         linestartpoint      NUMBER(10) NOT NULL,
                         lineendpoint        NUMBER(10) NOT NULL,
                         gaugelinestartpoint NUMBER(10) NOT NULL,
                         gaugelineendpoint   NUMBER(10) NOT NULL,
                         segment_order       NUMBER(10),
                         electrified_flag    CHAR(1) DEFAULT 'N' NOT NULL,
                         track_type          VARCHAR2(6) NOT NULL,
                         CONSTRAINT pk_segment PRIMARY KEY (segment_id),
                         CONSTRAINT ck_seg_length   CHECK (length_km > 0),
                         CONSTRAINT ck_seg_stations CHECK (linestartpoint <> lineendpoint),
                         CONSTRAINT ck_seg_electr   CHECK (electrified_flag IN ('Y','N')),
                         CONSTRAINT ck_seg_track    CHECK (track_type IN ('SINGLE','DOUBLE'))
);
CREATE TABLE SegmentType (
                             track_type_code number(10) GENERATED AS IDENTITY,
                             description     varchar2(10) NOT NULL UNIQUE,
                             PRIMARY KEY (track_type_code));
CREATE TABLE Facility (
                          facility_id   number(10) GENERATED AS IDENTITY,
                          name         varchar2(120) NOT NULL UNIQUE,
                          facility_type varchar2(10) NOT NULL UNIQUE,
                          CONSTRAINT pk_station
                              PRIMARY KEY (facility_id),
                          CONSTRAINT ck_facility_type
                              CHECK (facility_type IN ('TERMINAL','REGULAR')));
CREATE TABLE Wagon (
                       wagon_id               number(10) GENERATED AS IDENTITY,
                       operator_id            number(10) NOT NULL UNIQUE,
                       gauge_code             varchar2(20) NOT NULL,
                       wagon_type_code        number(10) NOT NULL,
                       serial_code            varchar2(255) NOT NULL UNIQUE,
                       locomotive_id          number(10) NOT NULL,
                       payload                number(10) NOT NULL UNIQUE,
                       freight_ID             number(10) NOT NULL,
                       train_ID               number(10) NOT NULL,
                       gauge_line_start_point number(10) NOT NULL,
                       gauge_line_end_point   number(10) NOT NULL,
                       service_year           number(10) NOT NULL,
                       max_speed              number(10) NOT NULL,
                       weight                 number(10) NOT NULL,
                       height                 number(10) NOT NULL,
                       length                 number(10) NOT NULL,
                       CONSTRAINT pk_wagon
                           PRIMARY KEY (wagon_id));
CREATE TABLE WagonType (
                           wagon_type_code number(10) GENERATED AS IDENTITY,
                           description     number(10) NOT NULL UNIQUE,
                           max_weight_kg    NUMBER(10) NOT NULL,
                           max_volume_m3    NUMBER(10) NOT NULL,
                           CONSTRAINT pk_wagontype
                               PRIMARY KEY (wagon_type_code),
                           CONSTRAINT ck_wtype_wgt
                               CHECK (max_weight_kg > 0),
                           CONSTRAINT ck_wtype_vol
                               CHECK (max_volume_m3 > 0));
CREATE TABLE Warehouse (
                           warehouse_id number(10) GENERATED AS IDENTITY,
                           facility_id   number(10) NOT NULL,
                           PRIMARY KEY (warehouse_id));
ALTER TABLE Locomotive ADD CONSTRAINT FKLocomotive501990 FOREIGN KEY (operator_id) REFERENCES Operator (operator_id);
ALTER TABLE Segment ADD CONSTRAINT FKSegment294611 FOREIGN KEY (segment_type_code) REFERENCES SegmentType (track_type_code);
ALTER TABLE Bay ADD CONSTRAINT FKBay653780 FOREIGN KEY (warehouse_id) REFERENCES Warehouse (warehouse_id);
ALTER TABLE Warehouse ADD CONSTRAINT FKWarehouse44580 FOREIGN KEY (station_id) REFERENCES Station (station_id);
ALTER TABLE Locomotive ADD CONSTRAINT FKLocomotive675898 FOREIGN KEY (owner_id) REFERENCES Owner (owner_id);
ALTER TABLE Locomotive ADD CONSTRAINT FKLocomotive255468 FOREIGN KEY (gauge_code, gauge_line_start_point, gauge_line_end_pointt) REFERENCES Gauge (gauge_code, line_start_point, line_end_point);
ALTER TABLE Segment ADD CONSTRAINT FKSegment642028 FOREIGN KEY (gauge_code, gaugelinestartpoint, gaugelineendpoint) REFERENCES Gauge (gauge_code, line_start_point, line_end_point);
ALTER TABLE Diesel_Locomotive ADD CONSTRAINT FKDiesel_Loc309909 FOREIGN KEY (llcomotive_id) REFERENCES Locomotive (locomotive_id);
ALTER TABLE Route ADD CONSTRAINT FKRoute956766 FOREIGN KEY (route_type_id) REFERENCES Route_Type (route_type_id);
ALTER TABLE Freight ADD CONSTRAINT FKFreight967660 FOREIGN KEY (route_id) REFERENCES Route (route_id);
ALTER TABLE Locomotive ADD CONSTRAINT FKLocomotive52346 FOREIGN KEY (locomotive_model_id) REFERENCES LocomotiveModel (locomotive_model_id);
ALTER TABLE Locomotive_Wagon ADD CONSTRAINT FKLocomotive959080 FOREIGN KEY (Locomotivelocomotive_id) REFERENCES Locomotive (locomotive_id);
ALTER TABLE Segment ADD CONSTRAINT FKSegment501641 FOREIGN KEY (linestation_id, linestartpoint, lineendpoint) REFERENCES Line (station_id, startpoint, endpoint);
ALTER TABLE Line ADD CONSTRAINT FKLine56247 FOREIGN KEY (station_id) REFERENCES Station (station_id);
ALTER TABLE Line ADD CONSTRAINT FKLine692292 FOREIGN KEY (owner_id) REFERENCES Owner (owner_id);
ALTER TABLE Gauge ADD CONSTRAINT FKGauge234033 FOREIGN KEY (linestation_id, line_start_point, line_end_point) REFERENCES Line (station_id, startpoint, endpoint);
ALTER TABLE Locomotive_Wagon ADD CONSTRAINT FKLocomotive525886 FOREIGN KEY (Wagonwagon_id) REFERENCES Wagon (wagon_id);
ALTER TABLE Wagon ADD CONSTRAINT FKWagon928453 FOREIGN KEY (operator_id) REFERENCES Operator (operator_id);
ALTER TABLE Wagon ADD CONSTRAINT FKWagon221415 FOREIGN KEY (wagon_type_code) REFERENCES WagonType (wagon_type_code);
ALTER TABLE Wagon ADD CONSTRAINT FKWagon604797 FOREIGN KEY (gauge_code, gauge_line_start_point, gauge_line_end_point) REFERENCES Gauge (gauge_code, line_start_point, line_end_point);
ALTER TABLE Wagon ADD CONSTRAINT FKWagon80136 FOREIGN KEY (freight_ID) REFERENCES Freight (freight_id);
ALTER TABLE Electric_Locomotive ADD CONSTRAINT FKElectric_L446383 FOREIGN KEY (Locomotivelocomotive_id) REFERENCES Locomotive (locomotive_id);