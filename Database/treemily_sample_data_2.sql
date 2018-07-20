insert into time
values (default);
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
insert into province(name)
values ('Skane');
insert into city(name)
values ('Lund');
insert into location (idcountry, idprovince, idcity)
values (1, 1, 1);

insert into locatedevent(idevent, idlocation) values (1, 1);

insert into accesscontrolledentity
values (default);
insert into parentable(idaccesscontrolledentity)
values (2);
insert into profile(idparentable, title, firstname, lastname, idborn, idgender)
values (2, 'Mr', 'Henrik', 'Akesson', 1, 1);


insert into time values(default);
insert into singletime(idtime, time) values (2, '1989-09-30');

insert into accesscontrolledentity values(default);
insert into post(idaccesscontrolledentity, idtime) values (3, 2);
insert into event(idpost, name, description) values (3, 'Born', 'Hahaha');

insert into country(name) values('Morocco');
insert into province(name) values('Casablanca-Settat');
insert into city(name) values('Casablanca');
insert into location(idcountry, idprovince, idcity) values ( 2,2,2);

insert into locatedevent(idevent, idlocation) values (3,2);

insert into accesscontrolledentity values(default);

insert into parentable(idaccesscontrolledentity) values (4);
insert into profile(idparentable, title, firstname, lastname, idborn, idgender) values (4, 'Mrs', 'Imane', 'Louafa', 3, 2);

insert into accesscontrolledentity values(default); --5
insert into parentable(idaccesscontrolledentity) values (5);
insert into relationship(idparentable, idprofile1, idprofile2, idrelationshiptype) values (5, 2, 4, 2);

insert into time values(default);
insert into singletime(idtime, time) values (3, '2010-10-11');

insert into post(idaccesscontrolledentity, idtime) values (5, 3);
insert into event(idpost, name, description) values (5, 'Got together', '');

insert into country(name) values ('Switzerland');
insert into province(name) values('Vaud');
insert into city(name) values ('Lausanne');
insert into location(idcountry, idprovince, idcity) values(3,3,3);

insert into locatedevent(idevent, idlocation) values (5, 3);
