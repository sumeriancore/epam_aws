-- CREATE USER iam_database_user WITH LOGIN;
-- GRANT rds_iam TO iam_database_user;
GRANT USAGE ON SCHEMA public TO iam_database_user;
GRANT CREATE ON SCHEMA public TO iam_database_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO iam_database_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO iam_database_user;
ALTER DEFAULT PRIVILEGES FOR ROLE iam_database_user IN SCHEMA public
    GRANT ALL ON TABLES TO iam_database_user;

ALTER DEFAULT PRIVILEGES FOR ROLE iam_database_user IN SCHEMA public
    GRANT ALL ON SEQUENCES TO iam_database_user;
