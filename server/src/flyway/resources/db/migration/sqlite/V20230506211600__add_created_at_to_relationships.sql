alter table Book_Publisher add created_at datetime default NULL;
alter table Book_Contributor add created_at datetime default NULL;
alter table Book_Tag add created_at datetime default NULL;

update Book_Publisher set created_at = CURRENT_TIMESTAMP;
update Book_Contributor set created_at = CURRENT_TIMESTAMP;
update Book_Tag set created_at = CURRENT_TIMESTAMP;
