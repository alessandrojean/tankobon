pragma foreign_keys = 0;

alter table Book rename to Temp;

create table if not exists Book(
  id varchar not null primary key,
  created_at datetime not null default CURRENT_TIMESTAMP,
  modified_at datetime not null default CURRENT_TIMESTAMP,
  code varchar not null,
  barcode varchar default null,
  collection_id varchar not null,
  store_id varchar default null,
  series_id varchar default null,
  title varchar not null,
  number varchar not null default "0",
  is_in_library boolean not null default 1,
  dimension_width float not null,
  dimension_height float not null,
  paid_price_currency varchar(3) not null,
  paid_price_value float not null,
  label_price_currency varchar(3) not null,
  label_price_value float not null,
  bought_at datetime default null,
  billed_at datetime default null,
  arrived_at datetime default null,
  synopsis varchar not null default '',
  page_count int not null default 0,
  notes varchar not null default '',
  source_key int default null,
  source_book_id varchar default null,
  subtitle varchar not null default '',
  weight float not null default 0,
  amazon varchar default NULL,
  open_library varchar default NULL,
  skoob varchar default NULL,
  goodreads varchar default NULL,
  guia_dos_quadrinhos varchar default NULL,
  dimension_unit int not null default 0,
  weight_unit int not null default 0,
  dimension_depth float not null default 0,

  foreign key (collection_id) references Collection (id) on delete cascade,
  foreign key (store_id) references Store (id) on delete set null,
  foreign key (series_id) references Series (id) on delete set null
);

insert into Book(
id,
created_at,
modified_at,
code,
barcode,
collection_id,
store_id,
series_id,
title,
number,
is_in_library,
dimension_width,
dimension_height,
paid_price_currency,
paid_price_value,
label_price_currency,
label_price_value,
bought_at,
billed_at,
arrived_at,
synopsis,
page_count,
notes,
source_key,
source_book_id,
subtitle,
weight,
amazon,
open_library,
skoob,
goodreads,
guia_dos_quadrinhos,
dimension_unit,
weight_unit,
dimension_depth
)
select
id,
created_at,
modified_at,
code,
barcode,
collection_id,
store_id,
series_id,
title,
number,
is_in_library,
dimension_width,
dimension_height,
paid_price_currency,
paid_price_value,
label_price_currency,
label_price_value,
bought_at,
billed_at,
arrived_at,
synopsis,
page_count,
notes,
source_key,
source_book_id,
subtitle,
weight,
amazon,
open_library,
skoob,
goodreads,
guia_dos_quadrinhos,
dimension_unit,
weight_unit,
dimension_depth
from Temp;

drop table if exists Temp;

alter table Read_Progress rename to Temp;

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

insert into Read_Progress(
id,
book_id,
user_id,
created_at,
modified_at,
page,
started_at,
finished_at,
is_completed
)
select
id,
book_id,
user_id,
created_at,
modified_at,
page,
started_at,
finished_at,
is_completed
from Temp;

drop table if exists Temp;

alter table Book_Contributor rename to Temp;

create table if not exists Book_Contributor(
  id varchar not null,
  book_id varchar not null,
  person_id varchar not null,
  role_id varchar not null,
  created_at datetime default NULL,

  primary key (id, book_id, person_id, role_id),
  foreign key (book_id) references Book (id) on delete cascade,
  foreign key (person_id) references Person (id) on delete cascade,
  foreign key (role_id) references Contributor_Role (id) on delete cascade
);

insert into Book_Contributor(
id,
book_id,
person_id,
role_id,
created_at
)
select
id,
book_id,
person_id,
role_id,
created_at
from Temp;

drop table if exists Temp;

alter table Book_Publisher rename to Temp;

create table if not exists Book_Publisher(
  book_id varchar not null,
  publisher_id varchar not null,
  created_at datetime default NULL,

  primary key (book_id, publisher_id),
  foreign key (book_id) references Book (id) on delete cascade,
  foreign key (publisher_id) references Publisher (id) on delete cascade
);

insert into Book_Publisher(
book_id,
publisher_id,
created_at
)
select
book_id,
publisher_id,
created_at
from Temp;

drop table if exists Temp;

alter table Book_Tag rename to Temp;

create table if not exists Book_Tag(
  book_id varchar not null,
  tag_id varchar not null,
  created_at datetime default NULL,

  primary key (book_id, tag_id),
  foreign key (book_id) references Book (id) on delete cascade,
  foreign key (tag_id) references Tag (id) on delete cascade
);

insert into Book_Tag(
book_id,
tag_id,
created_at
)
select
book_id,
tag_id,
created_at
from Temp;

drop table if exists Temp;

pragma foreign_keys = 1;
