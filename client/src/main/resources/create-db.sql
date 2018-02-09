CREATE TABLE SESSIONS (
  SESSION_ID NUMBER(2) PRIMARY KEY,

);

CREATE TABLE USAGES (
  USAGE_ID NUMBER(2) PRIMARY KEY,
  SESSION_ID


);

CREATE TABLE SESSION_TYPE (
  SESSION_TYPE_ID NUMBER(2) PRIMARY KEY,

);

CREATE TABLE DIAMETER_MESSAGES (
  MESSAGE_ID NUMBER(2) PRIMARY KEY,

);


CREATE TABLE REPORT_ENTITY_TYPE (
  REPORT_ENTITY_TYPE_ID NUMBER(2) PRIMARY KEY,
  NAME                  VARCHAR(50) NOT NULL
);

CREATE TABLE LANGUAGE_ENTITY (
  LANGUAGE_ENTITY_ID NUMBER(2) PRIMARY KEY,
  LANGUAGE_NAME      VARCHAR(64) NOT NULL
);

CREATE TABLE REPORT_ENTITY (
  REPORT_ENTITY_ID      NUMBER(20) PRIMARY KEY,
  REPORT_ENTITY_TYPE_ID NUMBER(2)     NOT NULL,
  REP_CODE              VARCHAR(15)   NOT NULL,
  NAME                  VARCHAR(32)   NOT NULL,
  IS_ACTIVE             BOOLEAN       NOT NULL,
  CLASS_NAME            VARCHAR(256)  NULL,
  COMPILED_CLASS        VARCHAR(512)  NULL,
  NOTE                  VARCHAR(1024) NULL,
  SRC                   BLOB,
  LANGUAGE_ENTITY_ID    NUMBER(2),
  FOREIGN KEY (REPORT_ENTITY_TYPE_ID) REFERENCES REPORT_ENTITY_TYPE (REPORT_ENTITY_TYPE_ID),
  FOREIGN KEY (LANGUAGE_ENTITY_ID) REFERENCES LANGUAGE_ENTITY (LANGUAGE_ENTITY_ID)
);

CREATE TABLE USER_ROLE_ENTITY (
  USER_ROLE_ENTITY_ID NUMBER(2) PRIMARY KEY,
  NAME                VARCHAR(256) NOT NULL
);

CREATE TABLE USER_ENTITY (
  USER_ENTITY_ID      NUMBER(20) PRIMARY KEY,
  LOGIN               VARCHAR(128) NOT NULL UNIQUE,
  PASSWORD            VARCHAR(256) NOT NULL,
  USER_ROLE_ENTITY_ID NUMBER(20)   NOT NULL,
  IS_ACTIVE           BOOLEAN      NOT NULL,
  NAME                VARCHAR(256) NOT NULL,
  NOTE                VARCHAR(256) NULL,
  FOREIGN KEY (USER_ROLE_ENTITY_ID) REFERENCES USER_ROLE_ENTITY (USER_ROLE_ENTITY_ID)
);

CREATE TABLE DATA_SOURCE_TYPE_ENTITY (
  DATA_SOURCE_TYPE_ENTITY_ID NUMBER(2) PRIMARY KEY,
  NAME                       VARCHAR(256) NOT NULL,
  DRIVER_CLASS_NAME          VARCHAR(512) NOT NULL
);

CREATE TABLE DATA_SOURCE_ENTITY (
  DATA_SOURCE_ENTITY_ID      NUMBER(2) PRIMARY KEY,
  DATA_SOURCE_TYPE_ENTITY_ID NUMBER(2)    NOT NULL,
  NAME                       VARCHAR(128) NOT NULL,
  URL                        VARCHAR(256) NOT NULL,
  USER_NAME                  VARCHAR(128) NULL,
  PASSWORD                   VARCHAR(128) NULL,
  PROPERTIES                 VARCHAR(4096),
  TEST_SELECT                VARCHAR(1024),
  FOREIGN KEY (DATA_SOURCE_TYPE_ENTITY_ID) REFERENCES DATA_SOURCE_TYPE_ENTITY (DATA_SOURCE_TYPE_ENTITY_ID)
);


CREATE TABLE REPORT_RESULT_ENTITY (
  REPORT_RESULT_ENTITY_ID NUMBER(20) PRIMARY KEY,
  REPORT_ENTITY_ID        NUMBER(20)    NOT NULL,
  NAME                    VARCHAR(64)   NOT NULL,
  GEN_DATE                TIMESTAMP     NOT NULL,
  IS_SUCCESS              BOOLEAN       NOT NULL,
  FILE_NAME_REF           VARCHAR(256)  NULL,
  TYPE                    VARCHAR(128)  NULL,
  NOTE                    VARCHAR(1024) NULL,
  FOREIGN KEY (REPORT_ENTITY_ID) REFERENCES REPORT_ENTITY (REPORT_ENTITY_ID)
);


CREATE TABLE FILE_SOURCE_ENTITY (
  FILE_SOURCE_ENTITY_ID NUMBER(10) PRIMARY KEY,
  NAME                  VARCHAR(128)  NOT NULL UNIQUE,
  DESCRIPTION           VARCHAR(256)  NULL,
  PATH                  VARCHAR(1024) NULL
);


INSERT INTO USER_ROLE_ENTITY (USER_ROLE_ENTITY_ID, NAME) VALUES (0, 'admin');
INSERT INTO USER_ROLE_ENTITY (USER_ROLE_ENTITY_ID, NAME) VALUES (1, 'moderate');
INSERT INTO USER_ROLE_ENTITY (USER_ROLE_ENTITY_ID, NAME) VALUES (2, 'business');

INSERT INTO USER_ENTITY (USER_ENTITY_ID, LOGIN, PASSWORD, USER_ROLE_ENTITY_ID, IS_ACTIVE, NAME, NOTE)
VALUES (1, 'admin', '827ccb0eea8a706c4c34a16891f84e7b', 1, TRUE, 'Base administrator', 'Default user');

INSERT INTO REPORT_ENTITY_TYPE (REPORT_ENTITY_TYPE_ID, NAME) VALUES (1, 'Report');
INSERT INTO REPORT_ENTITY_TYPE (REPORT_ENTITY_TYPE_ID, NAME) VALUES (2, 'Function');

INSERT INTO LANGUAGE_ENTITY (LANGUAGE_ENTITY_ID, LANGUAGE_NAME) VALUES (0, 'Java');
INSERT INTO LANGUAGE_ENTITY (LANGUAGE_ENTITY_ID, LANGUAGE_NAME) VALUES (1, 'Kotlin');


INSERT INTO DATA_SOURCE_TYPE_ENTITY (DATA_SOURCE_TYPE_ENTITY_ID, NAME, DRIVER_CLASS_NAME)
VALUES (1, 'H2', 'org.h2.jdbcx.JdbcConnectionPool');
INSERT INTO DATA_SOURCE_ENTITY (DATA_SOURCE_ENTITY_ID, DATA_SOURCE_TYPE_ENTITY_ID, NAME, URL, USER_NAME, PASSWORD, PROPERTIES, TEST_SELECT)
VALUES (1, 1, 'Helping DS', '', 'user', 'password', NULL, NULL);

COMMIT;
