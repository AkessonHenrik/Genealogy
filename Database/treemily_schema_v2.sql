-- Author: Henrik Akesson
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
    idtime int references time primary key,
    time date);

create view vsingletime as
  select idtime as id,
         time
  from singletime;

create table timeinterval(
    idtime int references time primary KEY,
    idsingletime1 int references singletime,
    idsingletime2 int references singletime,
    unique(idsingletime1, idsingletime2));

create view vtimeinterval as
  select timeinterval.idtime as id,
         st1.time as begin,
         st2.time as end
    from timeinterval
        left join vsingletime st1 on idsingletime1 = st1.id
        left join vsingletime st2 on idsingletime2 = st2.id;

create view vtime as
  select time . id,
         case
           when vsingletime.id is not null then array
         (
           select time
           from vsingletime
           where id = time . id
         )
           when vtimeinterval.id is not null then array((
                                                          select
                                                            vtimeinterval.begin
                                                          from vtimeinterval
                                                          where id = time . id
         )) || array
         (
           select vtimeinterval.end
           from vtimeinterval
           where id = time . id
         )
         end as time
  from time
       left join vsingletime on vsingletime.id = time . id
       left join vtimeinterval on vtimeinterval.id = time . id;

-- TimedEntities

create table timedentity(
    id serial primary key,
    idtime int references time,
    visibility int default 0);
create view vtimedentity as
  select timedentity.id,
         vtime.time
  from timedentity
       inner join vtime on vtime.id = timedentity.idtime;

create table post(
    idtimedentity int references timedentity primary key);

create view vpost as
  select vtimedentity.*
  from post
       inner join vtimedentity on post.idtimedentity = vtimedentity.id;

create table event(
    idpost int references post primary KEY,
    name varchar(80),
    description text);

create view vevent as
  select vpost.*,
         name,
         description
  from event
       inner join vpost on event.idpost = vpost.id;

create table media(
    idpost int references post primary KEY,
    path varchar(120),
    type int);

create view vmedia as
  select vpost.*,
         type,
         path
  from media
       inner join vpost on vpost.id = idpost;

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

create view vlocation as
  select location . id,
         country.name country,
         province.name province,
         city.name city
  from location
       inner join country on idcountry = country.id
       inner join province on idprovince = province.id
       inner join city on idcity = city.id;

create table locatedevent(
    idevent int references event primary key,
    idlocation int references location);

create view vlocatedevent as
  select vevent.*,
         vlocation.id as idlocation,
         vlocation.country as country,
         vlocation.province as province,
         vlocation.city as city
  from locatedevent
       inner join vevent on vevent.id = locatedevent.idevent
       inner join vlocation on idlocation = vlocation.id;

create table moveevent(
    idlocatedevent int references locatedevent primary key);

create view vmoveevent as
  select vlocatedevent.*
  from moveevent
       inner join vlocatedevent on vlocatedevent.id = idlocatedevent;

create table company(
    id serial primary key,
    name varchar(80));

create table workevent(
    idlocatedevent int references locatedevent primary KEY,
    position varchar(80),
    idcompany int references company);

create view vworkevent as
  select vlocatedevent.*,
         company.id idcompany,
         company.name companyname,
         position
  from workevent
       inner join vlocatedevent on vlocatedevent.id = idlocatedevent
       inner join company on idcompany = company.id;

create table parentable(
    idtimedentity int references timedentity primary key);

create view vparentable as
  select vtimedentity.*
  from parentable
       inner join vtimedentity on vtimedentity.id = parentable.idtimedentity;

create table profile(
    idparentable int references parentable primary key,
    title varchar(24),
    lastname varchar(80),
    firstname varchar(80),
    idgender int references gender,
    idmedia integer references media,
    idborn integer references event,
    iddied integer references event);

create view vprofile as
  select title,
         lastname,
         firstname,
         gender.name as gender,
         ':',
         media.type,
         media.path,
         vparentable.*
  from profile
       inner join vparentable on vparentable.id = idparentable
       inner join gender on gender.id = idgender
       left join media on media.idpost = idmedia;

create table relationship(
    idparentable int references parentable primary key,
    idprofile1 int references profile not null,
    idprofile2 int references profile not null,
    idrelationshiptype int references relationshiptype);

create view vrelationship as
  select vparentable.*,
         idprofile1,
         idprofile2,
         relationshiptype.id as relationshiptypeid,
         relationshiptype.name as relationshiptype
  from relationship
       inner join vparentable on vparentable.id = idparentable
       inner join relationshiptype on relationshiptype.id = idrelationshiptype;

create table account(
    id serial primary key,
    email varchar(80),
    password varchar(30),
    idprofile int references profile);

create view vaccount as
  select account.id as accountid,
         email,
         password,
         vprofile.*
  from account
       left join vprofile on vprofile.id = idprofile;

create table ghost(
    idprofile int references profile primary key,
    idaccount int references account);

create view vghost AS
  select *
  from ghost
       inner join vprofile on vprofile.id = idprofile;

create table parentchild(
    idtimedentity int references timedentity primary key,
    idparentable int references parentable,
    idprofile int references profile);

create view vparentchild as
  select *
  from parentchild
       inner join vtimedentity on vtimedentity.id = idtimedentity;


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
    idtimedentity integer references timedentity,
    idaccess integer references access,
    unique (idtimedentity, idaccess));

create table notvisibleby(
    idtimedentity integer references timedentity,
    idaccess integer references access,
    unique (idtimedentity, idaccess));

create view vvisibleby as
  select visibleby.*,
         coalesce(groupaccess.idgroup, profileaccess.idprofile) target
  from visibleby
       left join groupaccess on groupaccess.idaccess = visibleby.idaccess
       left join profileaccess on profileaccess.idaccess = visibleby.idaccess;

create view vnotvisibleby as
  select notvisibleby.*,
         coalesce(groupaccess.idgroup, profileaccess.idprofile) target
  from notvisibleby
       left join groupaccess on groupaccess.idaccess = notvisibleby.idaccess
       left join profileaccess on profileaccess.idaccess = notvisibleby.idaccess
         ;

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

create table timedentityowner(
    id serial primary key,
    idprofile integer references profile,
    idtimedentity integer references timedentity,
    unique(idprofile, idtimedentity));