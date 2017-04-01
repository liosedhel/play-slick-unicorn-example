#Initial evolution

# --- !Ups
create table "users" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"email" VARCHAR NOT NULL,"first_name" VARCHAR NOT NULL,"last_name" VARCHAR NOT NULL);
create table "places" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"name" VARCHAR NOT NULL);
create table "games" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"organizer_id" BIGINT NOT NULL,"note" VARCHAR NOT NULL,"date" TIMESTAMP NOT NULL,"place_id" BIGINT NOT NULL);
alter table "games" add constraint "organizer_fk" foreign key("organizer_id") references "users"("id") on update NO ACTION on delete NO ACTION;
alter table "games" add constraint "place_fk" foreign key("place_id") references "places"("id") on update NO ACTION on delete NO ACTION;
create table "games_users" ("game_id" BIGINT NOT NULL,"user_id" BIGINT NOT NULL);
alter table "games_users" add constraint "games_users_pk" primary key("game_id","user_id");
create unique index "games_users_uniq_idx" on "games_users" ("game_id","user_id");
alter table "games_users" add constraint "game_fk" foreign key("game_id") references "games"("id") on update NO ACTION on delete NO ACTION;
alter table "games_users" add constraint "user_fk" foreign key("user_id") references "users"("id") on update NO ACTION on delete NO ACTION;


insert into "users" values (1, 'liosedhel@gmail.com', 'lios', 'edhel');
insert into "places" values (1, 'Park Jordana');
insert into "games" values (1, 1, 'first game', CURRENT_TIMESTAMP(), 1);
# --- !Downs

DROP table "games_users";
DROP table "games";
DROP table "places";
DROP table "users";

