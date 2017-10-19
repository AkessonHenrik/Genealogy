-- Author: Henrik Akesson
-- Project: Treemily
-- Tables only
-- Date: 18/10/2017

DROP SCHEMA
IF EXISTS familytree CASCADE;
CREATE SCHEMA familytree;
set schema 'familytree';

-- Types

create table gender(
    id serial primary key,
    name varchar(24),
    unique(name));

insert into gender(name)
values ('male');
insert into gender(name)
values ('female');
insert into gender(name)
values ('other/unknown');

create table relationshiptype(
    id serial primary key,
    name varchar(30));

insert into relationshiptype(name)
values ('spouse');
insert into relationshiptype(name)
values ('partner');
insert into relationshiptype(name)
values ('sibling');
insert into relationshiptype(name)
values ('friend');
insert into relationshiptype(name)
values ('other/unknown');
insert into relationshiptype(name)
values ('cousin');

create table mediatype(
    id serial primary key,
    name varchar(32),
    unique(name));
insert into mediatype(name)
values ('image');
insert into mediatype(name)
values ('video');
insert into mediatype(name)
values ('document');


-- Time
create table time(
    id serial primary key);

create table singletime(
    idtime int references time on delete cascade primary key,
    time date);

create table timeinterval(
    idtime int references time primary KEY,
    idsingletime1 int references singletime on delete cascade,
    idsingletime2 int references singletime on delete cascade,
    unique(idsingletime1, idsingletime2));


create table circa (
	idtime integer references time on delete cascade primary key
);
create table circasingletime (
	idcirca integer references circa on delete cascade primary key,
    idtimeinterval integer references timeinterval on delete cascade,
    unique(idtimeinterval)
);

create table circatimeinterval (
	idcirca integer references circa on delete cascade primary key,
    idcircasingletime1 integer references circasingletime on delete cascade,
    idcircasingletime2 integer references circasingletime on delete cascade,
    unique(idcircasingletime1, idcircasingletime2)
);


-- AccessControlledEntities

create table accesscontrolledentity (
	id serial primary key,
    visibility int default 0
);

create table post(
    idaccesscontrolledentity int references accesscontrolledentity on delete cascade primary key,
    idtime int references time);


create table event(
    idpost int references post primary KEY,
    name varchar(80),
    description text);


create table media(
    idpost int references post primary KEY,
    path varchar(120),
    type int);


create table country(
    id serial primary key,
    name varchar(80),
    unique(name));

create table province(
    id serial primary key,
    name varchar(80),
    unique(name));

create table city(
    id serial primary key,
    name varchar(80),
    unique(name));

create table location(
    id serial primary key,
    idcountry int references country,
    idprovince int references province,
    idcity int references city);



create table locatedevent(
    idevent int references event primary key,
    idlocation int references location);



create table moveevent(
    idlocatedevent int references locatedevent primary key);


create table company(
    id serial primary key,
    name varchar(80));

create table workevent(
    idlocatedevent int references locatedevent primary KEY,
    position varchar(80),
    idcompany int references company);


create table parentable(
    idaccesscontrolledentity int references accesscontrolledentity primary key);



create table profile(
    idparentable int references parentable primary key,
    title varchar(24),
    lastname varchar(80),
    firstname varchar(80),
    idgender int references gender,
    idmedia integer references media,
    idborn integer references event,
    iddied integer references event);



create table relationship(
    idparentable int references parentable primary key,
    idprofile1 int references profile not null,
    idprofile2 int references profile not null,
    idrelationshiptype int references relationshiptype);


create table account(
    id serial primary key,
    email varchar(80),
    password varchar(30),
    idprofile int references profile);


create table ghost(
    idprofile int references profile primary key,
    idaccount int references account);

create table parentchild(
    idaccesscontrolledentity int references accesscontrolledentity primary key,
    idparentable int references parentable,
    idprofile int references profile);



-- Access

create table "group"(
    id serial primary key,
    idaccount integer references account,
    name varchar(80));

create table access(
    id serial primary key);

create table groupaccess(
    idgroup integer references "group" not null,
    idaccess integer references access primary key);

create table profileaccess(
    idprofile integer references profile not null,
    idaccess integer references access primary key);

create table visibleby(
    idaccesscontrolledentity integer references accesscontrolledentity,
    idaccess integer references access,
    unique (idaccesscontrolledentity, idaccess));

create table notvisibleby(
    idaccesscontrolledentity integer references accesscontrolledentity,
    idaccess integer references access,
    unique (idaccesscontrolledentity, idaccess));

create table tag(
    id serial primary key,
    approved boolean default null,
    idpost integer references post,
    idprofile integer references profile,
    unique(idpost, idprofile));

create table comment(
    id serial primary key,
    content text,
    posted timestamp,
    idaccount integer references account not null);

create table accesscontrolledentityowner(
    id serial primary key,
    idprofile integer references profile,
    idaccesscontrolledentity integer references accesscontrolledentity,
    unique(idprofile, idaccesscontrolledentity));