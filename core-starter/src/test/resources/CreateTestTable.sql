
drop table test IF EXISTS;
create table test
(
id bigint primary key not null,
name varchar(20) null,
data_type int not null
);