-- Author: Henrik Akesson

DROP SCHEMA IF EXISTS familytree CASCADE;
CREATE SCHEMA familytree;
set schema 'familytree';

CREATE TABLE country (
  id   SERIAL PRIMARY KEY,
  name VARCHAR(30) UNIQUE NOT NULL
);
CREATE TABLE province (
  id   SERIAL PRIMARY KEY,
  name VARCHAR(30) UNIQUE NOT NULL
);
CREATE TABLE city (
  id   SERIAL PRIMARY KEY,
  name VARCHAR(30) UNIQUE NOT NULL
);
CREATE TABLE cityProvince (
  id         SERIAL PRIMARY KEY,
  cityId     INTEGER REFERENCES city,
  provinceId INTEGER REFERENCES province,
  UNIQUE (cityId, provinceId)
);
CREATE TABLE provinceCountry (
  id         SERIAL PRIMARY KEY,
  provinceId INTEGER REFERENCES province,
  countryId  INTEGER REFERENCES country,
  UNIQUE (provinceId, countryId)
);

CREATE TABLE location (
  id                SERIAL PRIMARY KEY,
  cityProvinceId    INTEGER REFERENCES cityProvince,
  provinceCountryId INTEGER REFERENCES provinceCountry,
  UNIQUE (cityProvinceId, provinceCountryId)
);
CREATE TABLE company (
  id   SERIAL PRIMARY KEY,
  name VARCHAR(45) UNIQUE NOT NULL
);
CREATE TABLE time (
  id SERIAL PRIMARY KEY
);
CREATE TABLE singleTime (
  timeId INTEGER REFERENCES time PRIMARY KEY,
  time   DATE NOT NULL
);
CREATE TABLE timeinterval (
  timeId    INTEGER REFERENCES time PRIMARY KEY,
  beginTime DATE NOT NULL,
  endDate   DATE NOT NULL
);
CREATE TABLE timedEntity (
  id         SERIAL PRIMARY KEY,
  timeId     INTEGER REFERENCES time NOT NULL,
  visibility INTEGER DEFAULT 0
);
CREATE TABLE post (
  timedEntityId INTEGER REFERENCES timedEntity PRIMARY KEY
);
CREATE TABLE event (
  postId      INTEGER REFERENCES post PRIMARY KEY,
  name        VARCHAR(45) NOT NULL,
  description TEXT
);
CREATE TABLE locatedEvent (
  eventId    INTEGER REFERENCES event PRIMARY KEY,
  locationId INTEGER REFERENCES location (id)
);
CREATE TABLE workEvent (
  eventId      INTEGER REFERENCES event PRIMARY KEY,
  positionHeld VARCHAR(45),
  companyId    INTEGER REFERENCES company (id),
  locationId   INTEGER REFERENCES location
);
CREATE TABLE moveEvent (
  eventId    INTEGER REFERENCES event PRIMARY KEY,
  locationId INTEGER REFERENCES location NOT NULL
);
CREATE TABLE peopleentity (
  timedEntityId INTEGER REFERENCES timedEntity PRIMARY KEY
);
CREATE TABLE profile (
  peopleEntityId INTEGER REFERENCES peopleentity PRIMARY KEY,
  firstName      VARCHAR(45)                                NOT NULL,
  lastName       VARCHAR(45)                                NOT NULL,
  profilePicture INTEGER                                    NOT NULL,
  born           INTEGER REFERENCES locatedEvent NOT NULL,
  died           INTEGER REFERENCES locatedEvent,
  gender         INTEGER                                    NOT NULL
);
CREATE TABLE relationship (
  peopleentityid       INTEGER REFERENCES peopleentity PRIMARY KEY,
  profile1 INTEGER REFERENCES profile,
  profile2 INTEGER REFERENCES profile,
  type     INTEGER
);
CREATE TABLE parentsOf (
  timedEntityId INTEGER REFERENCES timedEntity PRIMARY KEY,
  childId       INTEGER REFERENCES profile      NOT NULL,
  parentsId     INTEGER REFERENCES peopleentity NOT NULL,
  parentType    INTEGER                                    NOT NULL,
  UNIQUE (childId, parentsId)
);
CREATE TABLE access (
  id SERIAL PRIMARY KEY
);
CREATE TABLE visibleBy (
  id            SERIAL PRIMARY KEY,
  timedEntityId INTEGER REFERENCES timedEntity,
  accessId      INTEGER REFERENCES access,
  UNIQUE (timedEntityId, accessId)
);
CREATE TABLE notVisibleBy (
  id            SERIAL PRIMARY KEY,
  timedEntityId INTEGER REFERENCES timedEntity,
  accessId      INTEGER REFERENCES access,
  UNIQUE (timedEntityId, accessId)
);
CREATE TABLE "group" (
  id    SERIAL PRIMARY KEY,
  name  VARCHAR(20),
  owner INTEGER REFERENCES profile
);
CREATE TABLE groupAccess (
  id       SERIAL PRIMARY KEY,
  groupId  INTEGER REFERENCES "group",
  accessId INTEGER REFERENCES access
);
CREATE TABLE profileAccess (
  id        SERIAL PRIMARY KEY,
  accessId  INTEGER REFERENCES access,
  profileId INTEGER REFERENCES profile
);
CREATE TABLE account (
  profileId INTEGER REFERENCES profile PRIMARY KEY,
  email     VARCHAR(60) NOT NULL,
  password  VARCHAR(20) NOT NULL
);
CREATE TABLE ghost (
  profileId INTEGER REFERENCES profile PRIMARY KEY,
  owner     INTEGER REFERENCES account
);
CREATE TABLE timedEntityOwner (
  id                     SERIAL PRIMARY KEY,
  timedEntityId          INTEGER REFERENCES timedEntity,
  peopleOrRelationshipId INTEGER REFERENCES peopleentity,
  UNIQUE (timedEntityId, peopleOrRelationshipId)
);
CREATE TABLE media (
  postId INTEGER REFERENCES post PRIMARY KEY,
  path   VARCHAR(100),
  type   INTEGER
);
ALTER TABLE profile
  ADD FOREIGN KEY (profilePicture)
REFERENCES media;
CREATE TABLE eventMedia (
  id      SERIAL PRIMARY KEY,
  mediaId INTEGER REFERENCES media,
  eventId INTEGER REFERENCES event,
  UNIQUE (mediaId, eventId)
);
CREATE TABLE comment (
  id        SERIAL PRIMARY KEY,
  postId    INTEGER REFERENCES post,
  commenter INTEGER REFERENCES account,
  postedOn  TIMESTAMP
);
CREATE TABLE tagged (
  id        SERIAL PRIMARY KEY,
  profileId INTEGER REFERENCES profile,
  postId    INTEGER REFERENCES post,
  UNIQUE (profileId, postId)
);
CREATE TABLE pendingTagged (
  id        SERIAL PRIMARY KEY,
  profileId INTEGER REFERENCES profile,
  postId    INTEGER REFERENCES post,
  UNIQUE (profileId, postId)
);
