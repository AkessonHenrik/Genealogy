set schema 'familytree';

insert into time
values (default); --1
insert into singletime (idtime, time)
values (1, '1991-08-27');
insert into accesscontrolledentity
values (default);

insert into post(idaccesscontrolledentity, idtime)
values (1, 1);
insert into event(idpost, name, description)
values (1, 'Born', 'Hehe');

insert into country(name)
values ('Sweden');
insert into city(name)
values ('Lund');
insert into location (idcountry, idcity)
values (1, 1);

insert into locatedevent(idevent, idlocation) values (1, 1);

insert into accesscontrolledentity
values (default);
insert into parentable(idaccesscontrolledentity)
values (2);
insert into profile(idparentable, title, firstname, lastname, idborn, idgender)
values (2, 'Mr', 'Henrik', 'Akesson', 1, 1);


insert into time values(default); --2
insert into singletime(idtime, time) values (2, '1989-09-30');

insert into accesscontrolledentity values(default);
insert into post(idaccesscontrolledentity, idtime) values (3, 2);
insert into event(idpost, name, description) values (3, 'Born', 'Hahaha');

insert into country(name) values('Morocco');
insert into province(name) values('Casablanca-Settat');
insert into city(name) values('Casablanca');
insert into location(idcountry, idprovince, idcity) values ( 2,1,2);

insert into locatedevent(idevent, idlocation) values (3,2);

insert into accesscontrolledentity values(default);

insert into parentable(idaccesscontrolledentity) values (4);
insert into profile(idparentable, title, firstname, lastname, idborn, idgender) values (4, 'Mrs', 'Imane', 'Louafa', 3, 2);

insert into accesscontrolledentity values(default); --5
insert into parentable(idaccesscontrolledentity) values (5);
insert into relationship(idparentable, idprofile1, idprofile2, idrelationshiptype) values (5, 2, 4, 2);

insert into time values(default); --3
insert into singletime(idtime, time) values (3, '2010-10-11');

insert into post(idaccesscontrolledentity, idtime) values (5, 3);
insert into event(idpost, name, description) values (5, 'Got together', '');

insert into country(name) values ('Switzerland');
insert into province(name) values('Vaud');
insert into city(name) values ('Lausanne');
insert into location(idcountry, idprovince, idcity) values(3,2,3);

insert into locatedevent(idevent, idlocation) values (5, 3);

set schema 'familytree';
insert into accesscontrolledentity values(default); --6
insert into parentable(idaccesscontrolledentity) values(6);
insert into profile(idparentable, title, firstname, lastname, idgender) values (6, 'Mrs', 'Kali', 'Louafakesson', 2);

insert into accesscontrolledentity values(default); --7
insert into parentchild(idaccesscontrolledentity, idparentable, idprofile) values (7, 5, 6);


insert into accesscontrolledentity values(default); --8
insert into parentable(idaccesscontrolledentity) values(8);
insert into profile(idparentable, title, firstname, lastname, idgender) values (8, 'Mr', 'Neo', 'Louafakesson', 1);

insert into accesscontrolledentity values(default); --9
insert into parentchild(idaccesscontrolledentity, idparentable, idprofile) values (9, 5, 8);

insert into accesscontrolledentity values(default); --10
insert into parentable(idaccesscontrolledentity) values(10);
insert into profile(idparentable, title, firstname, lastname, idgender) values (10, 'Mr', 'Torsten', 'Akesson', 1);


insert into accesscontrolledentity values(default); --11
insert into parentable(idaccesscontrolledentity) values(11);
insert into profile(idparentable, title, firstname, lastname, idgender) values (11, 'Mrs', 'Anne-Catherine', 'Akesson', 2);

insert into accesscontrolledentity values(default); --12
insert into parentable(idaccesscontrolledentity) values(12);
insert into relationship(idparentable, idprofile1, idprofile2) values (12, 10, 11);

insert into accesscontrolledentity values(default); --13
insert into parentchild(idaccesscontrolledentity, idparentable, idprofile) values (13, 12, 2);

insert into accesscontrolledentity values(default); --14
insert into parentable(idaccesscontrolledentity) values(14);
insert into profile(idparentable, title, firstname, lastname, idgender) values (14, 'Mrs', 'Pauline', 'Akesson', 2);

insert into accesscontrolledentity values(default); --15
insert into parentchild(idaccesscontrolledentity, idparentable, idprofile) values (15, 12, 14);


insert into accesscontrolledentity values(default); --16
insert into parentable(idaccesscontrolledentity) values(16);
insert into profile(idparentable, title, firstname, lastname, idgender) values (16, 'Mr', 'Tayeb', 'Louafa', 1);

insert into accesscontrolledentity values(default); --17
insert into parentchild(idaccesscontrolledentity, idparentable, idprofile) values (17, 16, 4);


insert into time values(default); --4
insert into singletime(idtime, time) values (4, '2017-09-15');

insert into accesscontrolledentity values(default); --18
insert into post(idaccesscontrolledentity, idtime) values (18, 4);
insert into event(idpost, name, description) values (18, 'Job', 'First software job');

insert into city(name) values ('Yverdon-les-Bains');
insert into location(idcountry, idprovince, idcity) values (3,2,4);
insert into locatedevent(idevent, idlocation) values (18, 4);
insert into company(name) values ('Heig-vd');
insert into workevent(idlocatedevent, idcompany, position) values (18, 1, 'Software engineer');
insert into accesscontrolledentityowner(idaccesscontrolledentity, idprofile) values (18, 2);

insert into accesscontrolledentityowner(idaccesscontrolledentity, idprofile) values (1,2);
insert into accesscontrolledentityowner(idaccesscontrolledentity, idprofile) values (3,4);
insert into accesscontrolledentityowner(idaccesscontrolledentity, idprofile) values (5,2);
insert into accesscontrolledentityowner(idaccesscontrolledentity, idprofile) values (5,4);


insert into accesscontrolledentity values(default);--19
insert into parentable(idaccesscontrolledentity) values(19);
insert into profile(idparentable, title, firstname, lastname, idgender) values (19, 'Mrs', 'Assia', 'Tazi', 2);

insert into accesscontrolledentity values(default); --20
insert into parentable(idaccesscontrolledentity) values(20);
insert into relationship(idparentable, idprofile1, idprofile2) values (20, 19, 16);

insert into accesscontrolledentity values(default); --21
insert into parentchild(idaccesscontrolledentity, idparentable, idprofile) values (21, 20, 4);


insert into time values(default); --5
insert into singletime(idtime, time) values (5, '2900-01-01');
insert into accesscontrolledentity values(default); --22
insert into post(idaccesscontrolledentity, idtime) values (22, 5);
insert into event(idpost, name, description) values (22, 'OIJ', 'oijoij');
update profile set iddied = 22 where idparentable=2;
insert into accesscontrolledentityowner(idaccesscontrolledentity, idprofile) values (22, 2);


delete from accesscontrolledentity where id = 17;

insert into accesscontrolledentity values(default); --23
insert into parentable(idaccesscontrolledentity) values(23);
insert into profile(idparentable, title, firstname, lastname, idgender) values (23, 'Mr', 'Florent', 'Prontera', 1);

insert into accesscontrolledentity values(default); --24
insert into parentable(idaccesscontrolledentity) values(24);
insert into relationship(idparentable, idprofile1, idprofile2) values (24, 23, 14);
