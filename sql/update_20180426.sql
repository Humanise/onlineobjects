create table client (id int8 not null, uuid varchar(255), primary key (id));
alter table client add constraint FKAF12F3CBE94A3D71 foreign key (id) references entity;