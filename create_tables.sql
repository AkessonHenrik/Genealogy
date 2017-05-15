-- Author: Henrik Akesson

DROP SCHEMA IF EXISTS familytree CASCADE;
CREATE SCHEMA familytree;
CREATE TYPE familytree.MEDIATYPE AS ENUM ('video', 'audio', 'image', 'document');
CREATE TYPE familytree.VISIBILITY AS ENUM ('private', 'public', 'limited');
CREATE TYPE familytree.RELATIONSHIPTYPE AS ENUM ('partner', 'spouse', 'sibling', 'cousin', 'friend', 'other/unknown');
CREATE TYPE familytree.GENDER AS ENUM ('Male', 'Female', 'Other');

CREATE TABLE familytree.country (
  id   SERIAL PRIMARY KEY,
  name VARCHAR(30) UNIQUE
);
CREATE TABLE familytree.province (
  id   SERIAL PRIMARY KEY,
  name VARCHAR(30) UNIQUE
);
CREATE TABLE familytree.city (
  id   SERIAL PRIMARY KEY,
  name VARCHAR(30) UNIQUE
);
CREATE TABLE familytree.location (
  id         SERIAL PRIMARY KEY,
  countryId  INTEGER REFERENCES familytree.country,
  provinceId INTEGER REFERENCES familytree.province,
  cityId     INTEGER REFERENCES familytree.city,
  UNIQUE (countryId, provinceId, cityId)
);
CREATE TABLE familytree.company (
  id   SERIAL PRIMARY KEY,
  name VARCHAR(45) UNIQUE
);
CREATE TABLE familytree.time (
  id SERIAL PRIMARY KEY
);
CREATE TABLE familytree.singleTime (
  PRIMARY KEY (timeId),
  timeId INTEGER REFERENCES familytree.time,
  time   DATE
);
CREATE TABLE familytree.timeinterval (
  PRIMARY KEY (timeId),
  timeId    INTEGER REFERENCES familytree.time,
  beginTime DATE,
  endDate   DATE
);
CREATE TABLE familytree.timedEntity (
  id     SERIAL PRIMARY KEY,
  timeId INTEGER REFERENCES familytree.time
);
CREATE TABLE familytree.post (
  timedEntityId INTEGER REFERENCES familytree.timedEntity PRIMARY KEY
);
CREATE TABLE familytree.event (
  postId      INTEGER REFERENCES familytree.post PRIMARY KEY,
  name        VARCHAR(45),
  description TEXT,
  visibility  familytree.VISIBILITY
);
CREATE TABLE familytree.locatedEvent (
  PRIMARY KEY (eventId),
  eventId    INTEGER REFERENCES familytree.event,
  locationId INTEGER REFERENCES familytree.location (id)
);
CREATE TABLE familytree.workEvent (
  PRIMARY KEY (eventId),
  eventId      INTEGER REFERENCES familytree.event,
  positionHeld VARCHAR(45),
  companyId    INTEGER REFERENCES familytree.company (id),
  locationId   INTEGER REFERENCES familytree.location
);
CREATE TABLE familytree.moveEvent (
  PRIMARY KEY (eventId),
  eventId    INTEGER REFERENCES familytree.event,
  locationId INTEGER REFERENCES familytree.location
);
CREATE TABLE familytree.life (
  eventId INTEGER REFERENCES familytree.event PRIMARY KEY,
  born    INTEGER REFERENCES familytree.location,
  died    INTEGER REFERENCES familytree.location
);
CREATE TABLE familytree.peopleentity (
  timedEntityId INTEGER REFERENCES familytree.timedEntity PRIMARY KEY
);
CREATE TABLE familytree.profile (
  peopleEntityId INTEGER REFERENCES familytree.peopleentity PRIMARY KEY,
  firstName      VARCHAR(45),
  lastName       VARCHAR(45),
  profilePicture INTEGER,
  lifeId         INTEGER REFERENCES familytree.life,
  gender         familytree.GENDER
);
CREATE TABLE familytree.relationship (
  peopleentityId INTEGER REFERENCES familytree.peopleentity PRIMARY KEY,
  profile1       INTEGER REFERENCES familytree.profile,
  profile2       INTEGER REFERENCES familytree.profile,
  type           familytree.RELATIONSHIPTYPE
);
CREATE TABLE familytree.parentsOf (
  childId INTEGER REFERENCES familytree.profile PRIMARY KEY
);
CREATE TABLE familytree.adoptiveChild (
  parentsOfId    INTEGER REFERENCES familytree.parentsOf PRIMARY KEY,
  peopleEntityId INTEGER REFERENCES familytree.peopleentity
);
CREATE TABLE familytree.biologicalChild (
  parentsOfId    INTEGER REFERENCES familytree.parentsOf PRIMARY KEY,
  relationshipId INTEGER REFERENCES familytree.relationship
);
CREATE TABLE familytree.access (
  id SERIAL PRIMARY KEY
);
CREATE TABLE familytree.visibleBy (
  timedEntityId INTEGER REFERENCES familytree.timedEntity,
  accessId      INTEGER REFERENCES familytree.access,
  PRIMARY KEY (timedEntityId, accessId)
);
CREATE TABLE familytree.notVisibleBy (
  timedEntityId INTEGER REFERENCES familytree.timedEntity,
  accessId      INTEGER REFERENCES familytree.access,
  PRIMARY KEY (timedEntityId, accessId)
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
  profileId INTEGER REFERENCES familytree.profile PRIMARY KEY
);
CREATE TABLE familytree.ghost (
  profileId INTEGER REFERENCES familytree.profile PRIMARY KEY,
  owner     INTEGER REFERENCES familytree.account
);
CREATE TABLE familytree.timedEntityOwner (
  timedEntityId INTEGER REFERENCES familytree.timedEntity,
  profileId     INTEGER REFERENCES familytree.profile,
  PRIMARY KEY (timedEntityId, profileId)
);
CREATE TABLE familytree.media (
  postId INTEGER REFERENCES familytree.post PRIMARY KEY,
  path   VARCHAR(100),
  owner  INTEGER REFERENCES familytree.account,
  type   familytree.MEDIATYPE
);
ALTER TABLE familytree.profile
  ADD FOREIGN KEY (profilePicture)
REFERENCES familytree.media;
CREATE TABLE familytree.eventMedia (
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
  profileId INTEGER REFERENCES familytree.profile,
  postId    INTEGER REFERENCES familytree.post,
  PRIMARY KEY (profileId, postId)
);
CREATE TABLE familytree.pendingTagged (
  profileId INTEGER REFERENCES familytree.profile,
  postId    INTEGER REFERENCES familytree.post,
  PRIMARY KEY (profileId, postId)
);