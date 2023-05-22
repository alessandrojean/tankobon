alter table Store add column legal_name varchar not null default '';
alter table Store add column location varchar default NULL;
alter table Store add column type int default NULL;

alter table Publisher add column founding_year int default NULL;
alter table Publisher add column dissolution_year int default NULL;
