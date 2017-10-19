-- Author: Henrik Akesson
-- Project: Treemily
-- Views only
-- Date: 18/10/2017

drop view if exists vtimeinterval cascade;
create view vtimeinterval as
  select timeinterval.idtime as id,
         st1.time as begin,
         st2.time as end
    from timeinterval
        left join singletime st1 on idsingletime1 = st1.idtime
        left join singletime st2 on idsingletime2 = st2.idtime;

drop view if exists vtime cascade;
create view vtime as
  select
  	*
  from time
	left join singletime on singletime.idtime = time.id
    left join timeinterval on timeinterval.idtime = time.id
    left join circasingletime on circasingletime.idcirca = time.id
    left join circatimeinterval on circatimeinterval.idcirca = time.id

drop view if exists vpost cascade;
create view vpost as
  select accesscontrolledentity.id,
         accesscontrolledentity.visibility,
         vtime.time
  from post
       inner join vtime on vtime.id = post.idtime
       inner join accesscontrolledentity on accesscontrolledentity.id =
         idaccesscontrolledentity;

drop view if exists vevent cascade;
create view vevent as
  select vpost.*,
         name,
         description
  from event
       inner join vpost on event.idpost = vpost.id;

drop view if exists vmedia cascade;
create view vmedia as
  select vpost.*,
         type,
         path
  from media
       inner join vpost on vpost.id = idpost;

drop view if exists vlocation cascade;
create view vlocation as
  select location . id,
         country.name country,
         province.name province,
         city.name city
  from location
       inner join country on idcountry = country.id
       inner join province on idprovince = province.id
       inner join city on idcity = city.id;

drop view if exists vlocatedevent cascade;
create view vlocatedevent as
  select vevent.*,
         vlocation.id as idlocation,
         vlocation.country as country,
         vlocation.province as province,
         vlocation.city as city
  from locatedevent
       inner join vevent on vevent.id = locatedevent.idevent
       inner join vlocation on idlocation = vlocation.id;

drop view if exists vmoveevent cascade;
create view vmoveevent as
  select vlocatedevent.*
  from moveevent
       inner join vlocatedevent on vlocatedevent.id = idlocatedevent;

drop view if exists vworkevent cascade;
create view vworkevent as
  select vlocatedevent.*,
         company.id idcompany,
         company.name companyname,
         position
  from workevent
       inner join vlocatedevent on vlocatedevent.id = idlocatedevent
       inner join company on idcompany = company.id;

drop view if exists vparentable cascade;
create view vparentable as
  select accesscontrolledentity.*
  from parentable
       inner join accesscontrolledentity on accesscontrolledentity.id = parentable.idaccesscontrolledentity;

drop view if exists vprofile cascade;
create view vprofile as
  select title,
         lastname,
         firstname,
         gender.name as gender,
         ':' as ":",
         media.type,
         media.path,
         vparentable.*
  from profile
       inner join vparentable on vparentable.id = idparentable
       inner join gender on gender.id = idgender
       left join media on media.idpost = idmedia;


drop view if exists vrelationship cascade;
create view vrelationship as
  select vparentable.*,
         idprofile1,
         idprofile2,
         relationshiptype.id as relationshiptypeid,
         relationshiptype.name as relationshiptype
  from relationship
       inner join vparentable on vparentable.id = idparentable
       inner join relationshiptype on relationshiptype.id = idrelationshiptype;

drop view if exists vaccount cascade;
create view vaccount as
  select account.id as accountid,
         email,
         password,
         vprofile.*
  from account
       left join vprofile on vprofile.id = idprofile;

drop view if exists vghost cascade;
create view vghost AS
  select *
  from ghost
       inner join vprofile on vprofile.id = idprofile;

drop view if exists vparentchild cascade;
create view vparentchild as
  select *
  from parentchild
       inner join accesscontrolledentity on accesscontrolledentity.id = idaccesscontrolledentity;

drop view if exists vvisibleby cascade;
create view vvisibleby as
  select visibleby.*,
         coalesce(groupaccess.idgroup, profileaccess.idprofile) target
  from visibleby
       left join groupaccess on groupaccess.idaccess = visibleby.idaccess
       left join profileaccess on profileaccess.idaccess = visibleby.idaccess;

drop view if exists vnotvisibleby cascade;
create view vnotvisibleby as
  select notvisibleby.*,
         coalesce(groupaccess.idgroup, profileaccess.idprofile) target
  from notvisibleby
       left join groupaccess on groupaccess.idaccess = notvisibleby.idaccess
       left join profileaccess on profileaccess.idaccess = notvisibleby.idaccess
         ;