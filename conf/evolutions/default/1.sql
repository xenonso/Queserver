# --- !Ups

create table players (
  id bigint generated by default as identity(start with 1) not null,
  first_name varchar not null,
  last_name varchar not null,
  PRIMARY KEY(id)
);

create table matches (
  id bigint generated by default as identity(start with 1) not null,
  start_date time not null,
  end_date time not null,
  player_id bigint not null REFERENCES players(id),
  PRIMARY KEY(id)
);

# --- !Downs

drop table "players" if exists;
drop table "matches" if exists;
