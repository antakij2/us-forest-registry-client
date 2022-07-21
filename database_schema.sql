CREATE TABLE FOREST (
	forest_no	varchar(10),
	name	varchar(30) UNIQUE NOT NULL,
	area	real NOT NULL,
	acid_level	real NOT NULL,
	mbr_xmin	real NOT NULL,
	mbr_xmax	real NOT NULL,
	mbr_ymin	real NOT NULL,
	mbr_ymax	real NOT NULL,
	PRIMARY KEY (forest_no)
);

CREATE TABLE STATE (
	name	varchar(30),
	abbreviation	varchar(2),
	area	real,
	population	integer,
	PRIMARY KEY (abbreviation)
);

CREATE TABLE COVERAGE (
	forest_no	varchar(10),
	state	varchar(2),
	percentage	real NOT NULL,
	area	real NOT NULL,
	PRIMARY KEY (forest_no, state),
	FOREIGN KEY (forest_no) REFERENCES FOREST(forest_no),
	FOREIGN KEY (state) REFERENCES STATE(abbreviation)
);

CREATE TABLE ROAD (
	road_no	varchar(10),
	name	varchar(30) NOT NULL,
	length	real NOT NULL,
	PRIMARY KEY (road_no)
);

CREATE TABLE INTERSECTION (
	forest_no	varchar(10),
	road_no	varchar(10),
	PRIMARY KEY (forest_no, road_no),
	FOREIGN KEY (forest_no) REFERENCES FOREST(forest_no),
	FOREIGN KEY (road_no) REFERENCES ROAD(road_no)
);

CREATE TABLE WORKER (
	ssn	varchar(9),
	name	varchar(30) UNIQUE NOT NULL,
	rank	integer NOT NULL,
	employing_state	varchar(2) NOT NULL,
	PRIMARY KEY (ssn),
	FOREIGN KEY (employing_state) REFERENCES STATE(abbreviation)
);

CREATE TABLE SENSOR (
	sensor_id	integer,
	x	real NOT NULL,
	y	real NOT NULL,
	last_charged	timestamp NOT NULL,
	maintainer	varchar(9) DEFAULT NULL,
	last_read	timestamp NOT NULL,
	energy	real NOT NULL,
	PRIMARY KEY (sensor_id),
	FOREIGN KEY (maintainer) REFERENCES WORKER(ssn),
	CONSTRAINT coordinates UNIQUE(x, y)
);

CREATE TABLE REPORT (
	sensor_id	integer,
	report_time	timestamp,
	temperature	real NOT NULL,
	PRIMARY KEY (sensor_id, report_time),
	FOREIGN KEY (sensor_id) REFERENCES SENSOR(sensor_id)
);
