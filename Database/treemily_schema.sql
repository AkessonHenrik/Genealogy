-- Author: Henrik Akesson

DROP SCHEMA IF EXISTS familytree CASCADE;
CREATE SCHEMA familytree;
CREATE TYPE familytree.MEDIATYPE AS ENUM ('video', 'audio', 'image', 'document');
CREATE TYPE familytree.RELATIONSHIPTYPE AS ENUM ('partner', 'spouse', 'sibling', 'cousin', 'friend', 'other/unknown');
-- CREATE TYPE familytree.GENDER AS ENUM ('Male', 'Female', 'Other');

CREATE TABLE familytree.country (
  id   SERIAL PRIMARY KEY,
  name VARCHAR(30) UNIQUE NOT NULL
);
CREATE TABLE familytree.province (
  id   SERIAL PRIMARY KEY,
  name VARCHAR(30) UNIQUE NOT NULL
);
CREATE TABLE familytree.city (
  id   SERIAL PRIMARY KEY,
  name VARCHAR(30) UNIQUE NOT NULL
);
CREATE TABLE familytree.cityProvince (
  id         SERIAL PRIMARY KEY,
  cityId     INTEGER REFERENCES familytree.city,
  provinceId INTEGER REFERENCES familytree.province,
  UNIQUE (cityId, provinceId)
);
CREATE TABLE familytree.provinceCountry (
  id         SERIAL PRIMARY KEY,
  provinceId INTEGER REFERENCES familytree.province,
  countryId  INTEGER REFERENCES familytree.country,
  UNIQUE (provinceId, countryId)
);

CREATE TABLE familytree.location (
  id                SERIAL PRIMARY KEY,
  cityProvinceId    INTEGER REFERENCES familytree.cityProvince,
  provinceCountryId INTEGER REFERENCES familytree.provinceCountry,
  UNIQUE (cityProvinceId, provinceCountryId)
);
CREATE TABLE familytree.company (
  id   SERIAL PRIMARY KEY,
  name VARCHAR(45) UNIQUE NOT NULL
);
CREATE TABLE familytree.time (
  id SERIAL PRIMARY KEY
);
CREATE TABLE familytree.singleTime (
  timeId INTEGER REFERENCES familytree.time PRIMARY KEY,
  time   DATE NOT NULL
);
CREATE TABLE familytree.timeinterval (
  timeId    INTEGER REFERENCES familytree.time PRIMARY KEY,
  beginTime DATE NOT NULL,
  endDate   DATE NOT NULL
);
CREATE TABLE familytree.timedEntity (
  id         SERIAL PRIMARY KEY,
  timeId     INTEGER REFERENCES familytree.time NOT NULL,
  visibility INTEGER DEFAULT 0
);
CREATE TABLE familytree.post (
  timedEntityId INTEGER REFERENCES familytree.timedEntity PRIMARY KEY
);
CREATE TABLE familytree.event (
  postId      INTEGER REFERENCES familytree.post PRIMARY KEY,
  name        VARCHAR(45) NOT NULL,
  description TEXT
);
CREATE TABLE familytree.locatedEvent (
  eventId    INTEGER REFERENCES familytree.event PRIMARY KEY,
  locationId INTEGER REFERENCES familytree.location (id)
);
CREATE TABLE familytree.workEvent (
  eventId      INTEGER REFERENCES familytree.event PRIMARY KEY,
  positionHeld VARCHAR(45),
  companyId    INTEGER REFERENCES familytree.company (id),
  locationId   INTEGER REFERENCES familytree.location
);
CREATE TABLE familytree.moveEvent (
  eventId    INTEGER REFERENCES familytree.event PRIMARY KEY,
  locationId INTEGER REFERENCES familytree.location NOT NULL
);
CREATE TABLE familytree.peopleentity (
  timedEntityId INTEGER REFERENCES familytree.timedEntity PRIMARY KEY
);
CREATE TABLE familytree.profile (
  peopleEntityId INTEGER REFERENCES familytree.peopleentity PRIMARY KEY,
  firstName      VARCHAR(45)                                NOT NULL,
  lastName       VARCHAR(45)                                NOT NULL,
  profilePicture INTEGER                                    NOT NULL,
  born           INTEGER REFERENCES familytree.locatedEvent NOT NULL,
  died           INTEGER REFERENCES familytree.locatedEvent,
  gender         INTEGER                                    NOT NULL
);
CREATE TABLE familytree.relationship (
  peopleentityid       INTEGER REFERENCES familytree.peopleentity PRIMARY KEY,
  profile1 INTEGER REFERENCES familytree.profile,
  profile2 INTEGER REFERENCES familytree.profile,
  type     INTEGER
);
CREATE TABLE familytree.parentsOf (
  timedEntityId INTEGER REFERENCES familytree.timedEntity PRIMARY KEY,
  childId       INTEGER REFERENCES familytree.profile      NOT NULL,
  parentsId     INTEGER REFERENCES familytree.peopleentity NOT NULL,
  parentType    INTEGER                                    NOT NULL,
  UNIQUE (childId, parentsId)
);
CREATE TABLE familytree.access (
  id SERIAL PRIMARY KEY
);
CREATE TABLE familytree.visibleBy (
  id            SERIAL PRIMARY KEY,
  timedEntityId INTEGER REFERENCES familytree.timedEntity,
  accessId      INTEGER REFERENCES familytree.access,
  UNIQUE (timedEntityId, accessId)
);
CREATE TABLE familytree.notVisibleBy (
  id            SERIAL PRIMARY KEY,
  timedEntityId INTEGER REFERENCES familytree.timedEntity,
  accessId      INTEGER REFERENCES familytree.access,
  UNIQUE (timedEntityId, accessId)
);
CREATE TABLE familytree."group" (
  id    SERIAL PRIMARY KEY,
  name  VARCHAR(20),
  owner INTEGER REFERENCES familytree.profile
);
CREATE TABLE familytree.groupAccess (
  id       SERIAL PRIMARY KEY,
  groupId  INTEGER REFERENCES familytree."group",
  accessId INTEGER REFERENCES familytree.access
);
CREATE TABLE familytree.profileAccess (
  id        SERIAL PRIMARY KEY,
  accessId  INTEGER REFERENCES familytree.access,
  profileId INTEGER REFERENCES familytree.profile
);
CREATE TABLE familytree.account (
  profileId INTEGER REFERENCES familytree.profile PRIMARY KEY,
  email     VARCHAR(60) NOT NULL,
  password  VARCHAR(20) NOT NULL
);
CREATE TABLE familytree.ghost (
  profileId INTEGER REFERENCES familytree.profile PRIMARY KEY,
  owner     INTEGER REFERENCES familytree.account
);
CREATE TABLE familytree.timedEntityOwner (
  id                     SERIAL PRIMARY KEY,
  timedEntityId          INTEGER REFERENCES familytree.timedEntity,
  peopleOrRelationshipId INTEGER REFERENCES familytree.peopleentity,
  UNIQUE (timedEntityId, peopleOrRelationshipId)
);
CREATE TABLE familytree.media (
  postId INTEGER REFERENCES familytree.post PRIMARY KEY,
  path   VARCHAR(100),
  type   INTEGER
);
ALTER TABLE familytree.profile
  ADD FOREIGN KEY (profilePicture)
REFERENCES familytree.media;
CREATE TABLE familytree.eventMedia (
  id      SERIAL PRIMARY KEY,
  mediaId INTEGER REFERENCES familytree.media,
  eventId INTEGER REFERENCES familytree.event,
  UNIQUE (mediaId, eventId)
);
CREATE TABLE familytree.comment (
  id        SERIAL PRIMARY KEY,
  postId    INTEGER REFERENCES familytree.post,
  commenter INTEGER REFERENCES familytree.account,
  postedOn  TIMESTAMP
);
CREATE TABLE familytree.tagged (
  id        SERIAL PRIMARY KEY,
  profileId INTEGER REFERENCES familytree.profile,
  postId    INTEGER REFERENCES familytree.post,
  UNIQUE (profileId, postId)
);
CREATE TABLE familytree.pendingTagged (
  id        SERIAL PRIMARY KEY,
  profileId INTEGER REFERENCES familytree.profile,
  postId    INTEGER REFERENCES familytree.post,
  UNIQUE (profileId, postId)
);
