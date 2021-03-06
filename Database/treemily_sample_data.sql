-- Author: Henrik Akesson
-- Project: Treemily
-- Sample data only
-- Date: 19/10/2017

delete from time cascade;

insert into time(id) values(default);
insert into singletime(idtime, time) values (1, '1991-01-01');

insert into time(id) values(default);
insert into singletime(idtime, time) values (2, '1991-02-01');

insert into time(id) values(default);
insert into singletime(idtime, time) values (3, '1992-01-01');

insert into time(id) values(default);
insert into singletime(idtime, time) values (4, '1992-02-01');

insert into time(id) values(default);
insert into timeinterval(idtime, idsingletime1, idsingletime2) values (5, 1, 2);

insert into time(id) values(default);
insert into timeinterval(idtime, idsingletime1, idsingletime2) values (6, 3, 4);

insert into time(id) values(default);
insert into circasingletime(idtime, idtimeinterval) values (7, 5);

insert into time(id) values(default);
insert into circasingletime(idtime, idtimeinterval) values (8, 6);

insert into time(id) values(default);
insert into circatimeinterval(idtime, idcircasingletime1, idcircasingletime2) values (9, 7, 8);

insert into time(id) values(default);
insert into singletimeandcircasingletime(idtime, idsingletime, idcircasingletime) values (10, 1, 7);

insert into time(id) values(default);
insert into singletimeandcircasingletime(idtime, idsingletime, idcircasingletime) values(11, 1, 8);


insert into time(id) values(default);
insert into timeinterval(idtime, idsingletime1, idsingletime2) values (12, 1, 3);
insert into time(id) values(default);
insert into timeinterval(idtime, idsingletime1, idsingletime2) values (13, 2, 4);

insert into time(id) values(default);
insert into circasingletime(idtime, idtimeinterval) values(14, 12);
insert into time(id) values(default);
insert into circasingletime(idtime, idtimeinterval) values(15, 13);

insert into time(id) values(default);
insert into circatimeinterval(idtime, idcircasingletime1, idcircasingletime2) values(16, 14, 15);

insert into time(id) values(default);
insert into singletimeandcircasingletime(idtime, idsingletime, idcircasingletime) values (17, 2, 15);

insert into accesscontrolledentity values(default);

insert into post(idaccesscontrolledentity, idtime) values (1, 17);
insert into event(idpost, name, description) values (1, 'First event', 'This event has a fixed beginning date and an approximate end date');
insert into country(name) values('France');
insert into province(name) values('Pays-de-la-Loire');
insert into city(name) values ('Le Mans');
insert into location(idcountry, idprovince, idcity) values (1,1,1);

insert into locatedevent(idevent, idlocation) values (1,1);
insert into moveevent(idlocatedevent) values(1);

insert into company(name) values ('Heig-vd');
insert into workevent(idlocatedevent, idcompany, position) values(1, 1, 'Directeur');


insert into accesscontrolledentity values (default);
insert into parentable(idaccesscontrolledentity) values(2);
insert into profile(idparentable, firstname, lastname, title, idgender) values (2, 'Henrik', 'Akesson', 'Mr', 1);


insert into accesscontrolledentity values (default);
insert into parentable(idaccesscontrolledentity) values(3);
insert into profile(idparentable, firstname, lastname, title, idgender) values (3, 'Imane', 'Louafa', 'Mrs', 2);

insert into accesscontrolledentity(id, visibility) values (default, 1);
insert into parentable(idaccesscontrolledentity) values (4);
insert into relationship(idparentable, idprofile1, idprofile2) values (4, 2, 3);

insert into tag(idpost, idprofile) values (1, 3);
insert into tag(idpost, idprofile) values (1, 2);


insert into accesscontrolledentity(id) values(default);
insert into post(idaccesscontrolledentity, idtime) values (5, 17);
insert into event(idpost, name, description) values (5, 'MoveEvent', 'First move event');
insert into country(name) values ('Suisse');
insert into province(name) values('Vaud');
insert into city(name) values ('Lausanne');
insert into location(idcountry, idprovince, idcity) values (2,2,2);
insert into locatedevent(idlocation, idevent) values (2, 5);
insert into moveevent(idlocatedevent) values (5);

insert into accesscontrolledentity(id) values (default);
insert into post(idaccesscontrolledentity, idtime) values (6, 16);
insert into media(idpost, type, path) values (6, 0, 'path/to/image');
insert into eventmedia(idevent, idmedia) values (5, 6);

insert into accesscontrolledentity(id) values (default);
insert into post(idaccesscontrolledentity, idtime) values (7, 16);
insert into media(idpost, type, path) values (7, 0, 'path/to/image2');
insert into eventmedia(idevent, idmedia) values (5, 7);