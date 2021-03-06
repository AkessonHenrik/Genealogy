-- Author: Henrik Akesson
-- Project: Treemily
-- Views only
-- Date: 18/10/2017

drop view
if exists vsingletime cascade;
create view vsingletime as
  select singletime.idtime as id,
         '{'         || array_to_string(array
         (
           select singletime.time
         ), ',') || '}' as time
  from singletime;

drop view
if exists vtimeinterval cascade;
create view vtimeinterval as
  select timeinterval.idtime as id,
         '{'         || array_to_string((array
         (
           select st1.time
         ) || array
         (
           select st2.time
         )), ',') || '}' as time
  from timeinterval
       left join vsingletime st1 on idsingletime1 = st1.id
       left join vsingletime st2 on idsingletime2 = st2.id;

drop view
if exists vcircasingletime cascade;
create view vcircasingletime as
  select circasingletime.idtime as id,
         '{'         || array_to_string(array
         (
           select time
           from vtimeinterval
           where vtimeinterval.id = circasingletime.idtimeinterval
         ), ',') || '}' as time
  from circasingletime;

drop view
if exists vcircatimeinterval cascade;
create view vcircatimeinterval as
  select circatimeinterval.idtime as id,
         ('{' ||
         (
           select vcircasingletime.time
           from circatimeinterval cti
                inner join vcircasingletime on vcircasingletime.id =
                  cti.idcircasingletime1
           where cti.idtime = circatimeinterval.idtime
         ) || ',' ||
         (
           select vcircasingletime.time
           from circatimeinterval cti
                inner join vcircasingletime on vcircasingletime.id =
                  cti.idcircasingletime2
           where cti.idtime = circatimeinterval.idtime
         ) || '}') as time
  from circatimeinterval;

drop view
if exists vsingletimeandcircasingletime cascade;
create view vsingletimeandcircasingletime as
  select idtime as id,
         ('{' ||
         (
           select vsingletime.time
           from singletimeandcircasingletime facti
                inner join vsingletime on vsingletime.id = facti.idsingletime
           where facti.idtime = singletimeandcircasingletime.idtime
         ) || ',' ||
         (
           select vcircasingletime.time
           from singletimeandcircasingletime facti
                inner join vcircasingletime on vcircasingletime.id =
                  facti.idcircasingletime
           where facti.idtime = singletimeandcircasingletime.idtime
         ) || '}') as time
  from singletimeandcircasingletime;

drop view
if exists vtime;
create view vtime as
  select time.id,
         coalesce(vsingletime.time, vtimeinterval.time, vcircasingletime.time,
           vcircatimeinterval.time, vsingletimeandcircasingletime.time) as time
  from time
       left join vsingletime on vsingletime.id = time.id
       left join vtimeinterval on vtimeinterval.id = time.id
       left join vcircasingletime on vcircasingletime.id = time.id
       left join vcircatimeinterval on vcircatimeinterval.id = time.id
       left join vsingletimeandcircasingletime on
         vsingletimeandcircasingletime.id = time.id;

drop view
if exists vpost cascade;
create view vpost as
  select accesscontrolledentity.id,
         accesscontrolledentity.visibility,
         vtime.time
  from post
       inner join vtime on vtime.id = post.idtime
       inner join accesscontrolledentity on accesscontrolledentity.id =
         idaccesscontrolledentity;

drop view
if exists vevent cascade;
create view vevent as
  select vpost.id,
         name,
         description,
         vpost.visibility,
         vpost.time,
         media.type,
         media.path
  from event
       inner join vpost on event.idpost = vpost.id
       left join eventmedia on eventmedia.idevent = vpost.id
       left join media on media.idpost = eventmedia.idmedia;

drop view
if exists vmedia cascade;
create view vmedia as
  select vpost.*,
         type,
         path
  from media
       inner join vpost on vpost.id = idpost;

drop view
if exists vlocation cascade;
create view vlocation as
  select location . id,
         country.name country,
         province.name province,
         city.name city
  from location
       inner join country on idcountry = country.id
       inner join province on idprovince = province.id
       inner join city on idcity = city.id;

drop view
if exists vlocatedevent cascade;
create view vlocatedevent as
  select vevent.*,
         vlocation.id as idlocation,
         vlocation.country as country,
         vlocation.province as province,
         vlocation.city as city
  from locatedevent
       inner join vevent on vevent.id = locatedevent.idevent
       inner join vlocation on idlocation = vlocation.id;

drop view
if exists vmoveevent cascade;
create view vmoveevent as
  select vlocatedevent.*
  from moveevent
       inner join vlocatedevent on vlocatedevent.id = idlocatedevent;

drop view
if exists vworkevent cascade;
create view vworkevent as
  select vlocatedevent.*,
         company.id idcompany,
         company.name companyname,
         position
  from workevent
       inner join vlocatedevent on vlocatedevent.id = idlocatedevent
       inner join company on idcompany = company.id;

drop view
if exists vfullevent cascade;
create view vfullevent as
  select distinct vevent.*,
         (case
            when workevent.idlocatedevent is not null then 'WorkEvent'
            when moveevent.idlocatedevent is not null then 'MoveEvent'
            when vlocatedevent.id is not null then 'LocatedEvent'
            when vevent.id is not null then 'Event'
          end) as "Event Type",
         vlocatedevent.idlocation,
         vlocatedevent.country,
         vlocatedevent.province,
         vlocatedevent.city,
         workevent.position,
         company.name as company
  from vevent
       left join vlocatedevent on vlocatedevent.id = vevent.id
       left join moveevent on moveevent.idlocatedevent = vlocatedevent.id
       left join workevent on workevent.idlocatedevent = vlocatedevent.id
       left join company on company.id = workevent.idcompany;

drop view
if exists vparentable cascade;
create view vparentable as
  select accesscontrolledentity.*
  from parentable
       inner join accesscontrolledentity on accesscontrolledentity.id =
         parentable.idaccesscontrolledentity;

drop view
if exists vprofile cascade;
create view vprofile as
  select vparentable.*,
         title,
         lastname,
         firstname,
         gender.name as gender,
         media.path as profilepicture,
         idborn,
         born.name bornname,
         born.description borndescription,
         born.visibility bornvisibility,
         born.time birthday,
         born.country "Born Country",
         born.province "Born Province",
         born.city "Born City",
         iddied,
         died.name diedname,
         died.description dieddescription,
         died.visibility diedvisibility,
         died.country "Died Country",
         died.province "Died Province",
         died.city "Died City",
         died.time deathday
  from profile
       inner join vparentable on vparentable.id = idparentable
       inner join gender on gender.id = idgender
       left join media on media.idpost = idmedia
       left join vfullevent born on born.id = idborn
       left join vfullevent died on died.id = iddied;

drop view
if exists vrelationship cascade;
create view vrelationship as
  select vparentable.*,
         idprofile1,
         idprofile2,
         relationshiptype.id as idrelationshiptype,
         relationshiptype.name as relationshiptype,
         vfullevent.name,
         vfullevent.description,
         vfullevent.time,
         vfullevent.idlocation,
         vfullevent.country,
         vfullevent.province,
         vfullevent.city
  from relationship
       inner join vparentable on vparentable.id = idparentable
       inner join relationshiptype on relationshiptype.id = idrelationshiptype
       left join vfullevent on vfullevent.id = vparentable.id;

drop view
if exists vaccount cascade;
create view vaccount as
  select account.id as accountid,
         email,
         password,
         vprofile.*
  from account
       left join vprofile on vprofile.id = idprofile;

drop view
if exists vghost cascade;
create view vghost AS
  select *
  from ghost
       inner join vprofile on vprofile.id = idprofile;

drop view
if exists vparentchild cascade;
create view vparentchild as
  select *
  from parentchild
       inner join accesscontrolledentity on accesscontrolledentity.id =
         idaccesscontrolledentity;

drop view
if exists vvisibleby cascade;
create view vvisibleby as
  select visibleby.*,
         coalesce(groupaccess.idgroup, profileaccess.idprofile) target
  from visibleby
       left join groupaccess on groupaccess.idaccess = visibleby.idaccess
       left join profileaccess on profileaccess.idaccess = visibleby.idaccess;

drop view
if exists vnotvisibleby cascade;
create view vnotvisibleby as
  select notvisibleby.*,
         coalesce(groupaccess.idgroup, profileaccess.idprofile) target
  from notvisibleby
       left join groupaccess on groupaccess.idaccess = notvisibleby.idaccess
       left join profileaccess on profileaccess.idaccess = notvisibleby.idaccess
         ;

drop view
if exists vtag cascade;
create view vtag as
  select id,
         idpost,
         idprofile,
         profile.lastname,
         profile.firstname
  from tag
       left join profile on profile.idparentable = tag.idprofile;

drop view
if exists vfullprofile cascade;
create view vfullprofile as
  select vprofile.*,
         vfullevent.id as eventid,
         vfullevent.name,
         vfullevent.description,
         vfullevent.visibility as "Event visibility",
         vfullevent.time,
         vfullevent.type as "Media type",
         vfullevent.path as "Media path",
         vfullevent."Event Type",
         vfullevent.idlocation,
         vfullevent.country,
         vfullevent.province,
         vfullevent.city,
         vfullevent.position,
         vfullevent.company
  from vprofile
       left join accesscontrolledentityowner on
         accesscontrolledentityowner.idprofile = vprofile.id
       left join accesscontrolledentity on accesscontrolledentity.id =
         accesscontrolledentityowner.idaccesscontrolledentity
       left join vfullevent on vfullevent.id = accesscontrolledentity.id;

drop view
if exists vtimetype cascade;
create view vtimetype as
  select
    time.id,
    case 
      when singletime.idtime is not null then 'SingleTime'
      when timeinterval.idtime is not null then 'TimeInterval'
      when circasingletime.idtime is not null then 'CircaSingleTime'
      when circatimeinterval.idtime is not null then 'CircaTimeInterval'
      when singletimeandcircasingletime is not null then 'SingleTimeAndCircaSingleTime'
    end
  from time
    left join singletime on time.id = singletime.idtime
    left join timeinterval on time.id = timeinterval.idtime
    left join circasingletime on time.id = circasingletime.idtime
    left join circatimeinterval on time.id = circatimeinterval.idtime
    left join singletimeandcircasingletime on time.id = singletimeandcircasingletime.idtime;
    