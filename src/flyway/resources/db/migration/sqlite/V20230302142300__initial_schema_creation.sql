create table if not exists User(
  id varchar not null primary key,
  created_at datetime not null default CURRENT_TIMESTAMP,
  modified_at datetime not null default CURRENT_TIMESTAMP,
  email varchar not null unique,
  password varchar not null,
  is_admin boolean not null default 0
);

create table if not exists Library(
  id varchar not null primary key,
  created_at datetime not null default CURRENT_TIMESTAMP,
  modified_at datetime not null default CURRENT_TIMESTAMP,
  name varchar not null,
  description varchar not null default "",
  owner_id varchar not null,

  foreign key (owner_id) references User (id) on delete cascade
);

create table if not exists Collection(
  id varchar not null primary key,
  created_at datetime not null default CURRENT_TIMESTAMP,
  modified_at datetime not null default CURRENT_TIMESTAMP,
  name varchar not null,
  description varchar not null default "",
  library_id varchar not null,

  foreign key (library_id) references Library (id) on delete cascade
);

create table if not exists Series(
  id varchar not null primary key,
  created_at datetime not null default CURRENT_TIMESTAMP,
  modified_at datetime not null default CURRENT_TIMESTAMP,
  name varchar not null,
  description varchar not null default "",
  library_id varchar not null,

  foreign key (library_id) references Library (id) on delete cascade
);

create table if not exists Publisher(
  id varchar not null primary key,
  created_at datetime not null default CURRENT_TIMESTAMP,
  modified_at datetime not null default CURRENT_TIMESTAMP,
  name varchar not null,
  description varchar not null default "",
  library_id varchar not null,

  foreign key (library_id) references Library (id) on delete cascade
);

create table if not exists Store(
  id varchar not null primary key,
  created_at datetime not null default CURRENT_TIMESTAMP,
  modified_at datetime not null default CURRENT_TIMESTAMP,
  name varchar not null,
  description varchar not null default "",
  library_id varchar not null,

  foreign key (library_id) references Library (id) on delete cascade
);

create table if not exists Person(
  id varchar not null primary key,
  created_at datetime not null default CURRENT_TIMESTAMP,
  modified_at datetime not null default CURRENT_TIMESTAMP,
  name varchar not null,
  description varchar not null default "",
  library_id varchar not null,

  foreign key (library_id) references Library (id) on delete cascade
);

create table if not exists Tag(
  id varchar not null primary key,
  created_at datetime not null default CURRENT_TIMESTAMP,
  modified_at datetime not null default CURRENT_TIMESTAMP,
  name varchar not null,
  description varchar not null default "",
  library_id varchar not null,

  foreign key (library_id) references Library (id) on delete cascade
);

create table if not exists Contributor_Role(
  id varchar not null primary key,
  created_at datetime not null default CURRENT_TIMESTAMP,
  modified_at datetime not null default CURRENT_TIMESTAMP,
  name varchar not null,
  description varchar not null default "",
  library_id varchar not null,

  foreign key (library_id) references Library (id) on delete cascade
);

create table if not exists Book(
  id varchar not null primary key,
  created_at datetime not null default CURRENT_TIMESTAMP,
  modified_at datetime not null default CURRENT_TIMESTAMP,
  code varchar not null unique,
  barcode varchar default null,
  collection_id varchar not null,
  series_id varchar default null,
  store_id varchar default null,
  title varchar not null,
  is_in_library boolean not null default 1,
  paid_price_currency varchar(3) not null,
  paid_price_value float not null,
  bought_at datetime default null,
  notes varchar not null default '',

  foreign key (collection_id) references Collection (id) on delete set null,
  foreign key (series_id) references Series (id) on delete set null,
  foreign key (store_id) references Store (id) on delete set null
);

create table if not exists Book_Metadata(
  book_id varchar not null primary key,
  created_at datetime not null default CURRENT_TIMESTAMP,
  modified_at datetime not null default CURRENT_TIMESTAMP,
  number varchar not null default "0",
  dimension_width_cm float not null,
  dimension_height_cm float not null,
  label_price_currency varchar(3) not null,
  label_price_value float not null,
  synopsis varchar not null default "",
  page_count int not null default 0,

  foreign key (book_id) references Book (id) on delete cascade
);

create table if not exists Book_Metadata_Publisher(
  book_id varchar not null,
  publisher_id varchar not null,

  primary key (book_id, publisher_id),
  foreign key (book_id) references Book_Metadata (book_id) on delete cascade,
  foreign key (publisher_id) references Publisher (id) on delete cascade
);

create table if not exists Book_Contributor(
  book_id varchar not null,
  person_id varchar not null,
  role_id varchar not null,

  primary key (book_id, person_id, role_id),
  foreign key (book_id) references Book_Metadata (book_id) on delete cascade,
  foreign key (person_id) references Person (id) on delete cascade,
  foreign key (role_id) references Contributor_Role (id) on delete cascade
);

create table if not exists Book_Metadata_Tag(
  book_id varchar not null,
  tag_id varchar not null,

  primary key (book_id, tag_id),
  foreign key (book_id) references Book_Metadata (book_id) on delete cascade,
  foreign key (tag_id) references Tag (id) on delete cascade
);

create table if not exists User_Library_Sharing(
  user_id varchar not null,
  library_id varchar not null,

  primary key (user_id, library_id),
  foreign key (user_id) references User (id) on delete cascade,
  foreign key (library_id) references Library (id) on delete cascade
);

create table if not exists Read_Progress(
  id varchar not null primary key,
  book_id varchar not null,
  user_id varchar not null,
  created_at datetime not null default CURRENT_TIMESTAMP,
  modified_at datetime not null default CURRENT_TIMESTAMP,
  page int not null,
  started_at datetime default null,
  finished_at datetime default null,
  is_completed boolean not null,

  foreign key (book_id) references Book (id) on delete cascade,
  foreign key (user_id) references User (id) on delete cascade
);
