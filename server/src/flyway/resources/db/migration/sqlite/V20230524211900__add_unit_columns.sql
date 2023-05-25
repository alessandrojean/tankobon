alter table Book add column dimension_unit int not null default 0;
alter table Book add column weight_unit int not null default 0;
alter table Book add column dimension_depth float not null default 0;

alter table Book rename column dimension_width_cm to dimension_width;
alter table Book rename column dimension_height_cm to dimension_height;
alter table Book rename column weight_kg to weight;
