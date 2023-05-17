create table if not exists Image(
  id varchar not null primary key,
  created_at datetime not null default CURRENT_TIMESTAMP,
  modified_at datetime not null default CURRENT_TIMESTAMP,
  file_name varchar not null,
  width int not null,
  height int not null,
  aspect_ratio varchar not null,
  format varchar not null,
  mime_type varchar not null,
  time_hex varchar not null,
  blur_hash varchar not null
);
