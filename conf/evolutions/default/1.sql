#Initial evolution

# --- !Ups
create table "users" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"email" VARCHAR NOT NULL,"first_name" VARCHAR NOT NULL,"last_name" VARCHAR NOT NULL);
create table "places" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"name" VARCHAR NOT NULL);
create table "games" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"organizer_id" BIGINT NOT NULL,"note" VARCHAR NOT NULL,"date" TIMESTAMP NOT NULL,"place_id" BIGINT NOT NULL);
alter table "games" add constraint "organizer_fk" foreign key("organizer_id") references "users"("id") on update NO ACTION on delete NO ACTION;
alter table "games" add constraint "place_fk" foreign key("place_id") references "places"("id") on update NO ACTION on delete NO ACTION;


insert into "users" values (1, 'liosedhel@gmail.com', 'lios', 'edhel');
insert into "places" values (1, 'Park Jordana');
insert into "games" values (1, 1, 'first game', 'now', 1);
# --- !Downs

DROP table "games";
DROP table "users";


