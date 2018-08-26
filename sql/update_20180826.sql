create table log (id int8 not null, time timestamp, level int4, type int4, subject int8, object int8, data varchar(255), primary key (id));
create sequence log_id_sequence;