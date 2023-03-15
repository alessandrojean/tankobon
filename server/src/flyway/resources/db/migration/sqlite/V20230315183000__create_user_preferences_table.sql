create table User_Preference(
  user_id varchar not null,
  key varchar not null,
  value varchar not null,

  primary key (user_id, key),
  foreign key (user_id) references User (id) on delete cascade
);
