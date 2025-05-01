-- Drop dependent tables first due to foreign key constraints

DROP TABLE IF EXISTS user_passports;
DROP TABLE IF EXISTS user_languages;
DROP TABLE IF EXISTS user_preferred_climates;
DROP TABLE IF EXISTS user_interests;
DROP TABLE IF EXISTS budget_estimation_breakdowns;
DROP TABLE IF EXISTS budget_estimations;
DROP TABLE IF EXISTS travel_plan_destinations;
DROP TABLE IF EXISTS travel_plans;
DROP TABLE IF EXISTS user_recommendations;
DROP TABLE IF EXISTS user_travel_profiles;

DROP TABLE IF EXISTS account_history;
DROP TABLE IF EXISTS account;
DROP TABLE IF EXISTS retirement_detail;
DROP TABLE IF EXISTS app_user;
DROP TABLE IF EXISTS flyway_schema_history;
DROP TABLE IF EXISTS budget_breakdown_costs;

-- Drop sequences if you created any manually (optional)
DROP SEQUENCE IF EXISTS account_seq;
DROP SEQUENCE IF EXISTS account_history_seq;
DROP SEQUENCE IF EXISTS retirement_detail_seq;
DROP SEQUENCE IF EXISTS app_user_seq;
