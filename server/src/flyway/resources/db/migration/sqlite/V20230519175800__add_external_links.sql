alter table Person add website varchar default NULL;
alter table Person add twitter varchar default NULL;
alter table Person add instagram varchar default NULL;
alter table Person add facebook varchar default NULL;
alter table Person add pixiv varchar default NULL;
alter table Person add deviantart varchar default NULL;
alter table Person add youtube varchar default NULL;

alter table Store add website varchar default NULL;
alter table Store add twitter varchar default NULL;
alter table Store add instagram varchar default NULL;
alter table Store add facebook varchar default NULL;
alter table Store add youtube varchar default NULL;

alter table Publisher add website varchar default NULL;
alter table Publisher add store varchar default NULL;
alter table Publisher add twitter varchar default NULL;
alter table Publisher add instagram varchar default NULL;
alter table Publisher add facebook varchar default NULL;
alter table Publisher add youtube varchar default NULL;

alter table Series add website varchar default NULL;
alter table Series add my_anime_list varchar default NULL;
alter table Series add kitsu varchar default NULL;
alter table Series add anilist varchar default NULL;
alter table Series add twitter varchar default NULL;
alter table Series add instagram varchar default NULL;

alter table Book add amazon varchar default NULL;
alter table Book add open_library varchar default NULL;
alter table Book add skoob varchar default NULL;
alter table Book add goodreads varchar default NULL;
