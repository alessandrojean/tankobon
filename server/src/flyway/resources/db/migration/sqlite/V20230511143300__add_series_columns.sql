alter table Series add type int default NULL;

create table if not exists Series_Alternative_Name(
  id varchar not null primary key,
  created_at datetime not null default CURRENT_TIMESTAMP,
  name varchar not null,
  language varchar not null,
  series_id varchar not null,

  foreign key (series_id) references Series (id) on delete cascade
);
