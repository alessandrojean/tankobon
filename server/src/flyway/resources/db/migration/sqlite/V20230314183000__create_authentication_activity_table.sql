create table Authentication_Activity(
  id varchar not null primary key,
  user_id varchar null default null,
  email varchar null default null,
  ip varchar null default null,
  user_agent varchar null default null,
  success boolean not null,
  error varchar null default null,
  timestamp datetime not null default CURRENT_TIMESTAMP,
  source varchar null default null,

  foreign key (user_id) references User (id) on delete set null
);
