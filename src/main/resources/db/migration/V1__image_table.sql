CREATE TABLE IMAGES (
                        ID BIGSERIAL PRIMARY KEY,
                        FILENAME VARCHAR(255) NOT NULL,
                        SIZE BIGINT,
                        EXTENSION VARCHAR(50),
                        LAST_UPDATE TIMESTAMP WITH TIME ZONE
);