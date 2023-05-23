alter table Person add column born_at date default NULL;
alter table Person add column died_at date default NULL;
alter table Person add column nationality varchar default NULL;
alter table Person add column native_name varchar not null default '';
