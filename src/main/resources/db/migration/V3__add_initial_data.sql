--dummy data for playground
INSERT INTO app_user (id, country, username) VALUES (1, 'ES', 'lucas');
INSERT INTO retirement_detail (id, income_per_month_desired, life_expcetation, retirement_date, user_id)
VALUES (1, 4000.00, '2075-12-31', '2050-01-01', 1);

--Travel
insert into user_travel_profiles (birth,budget_level,gender,name,travel_style,id) values ('1995-03-21','MEDIUM','MALE','Lucas','relaxed',1);

insert into user_interests (interest,user_id,id) values ('nature',1,1);
insert into user_interests (interest,user_id,id) values ('hiking',1,2);
insert into user_interests (interest,user_id,id) values ('food',1,3);
insert into user_interests (interest,user_id,id) values ('couple',1,4);
insert into user_interests (interest,user_id,id) values ('less touristic',1,5);

insert into user_languages (user_profile_id,language) values (1,'english');
insert into user_languages (user_profile_id,language) values (1,'portuguese');
insert into user_languages (user_profile_id,language) values (1,'spanish');

insert into user_preferred_climates (user_profile_id,climate) values (1,'spring');

insert into user_passports (user_profile_id,country_code) values (1,'ES');
insert into user_passports (user_profile_id,country_code) values (1,'BR');