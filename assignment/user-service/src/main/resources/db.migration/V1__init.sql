CREATE TABLE users (
                       id BIGSERIAL NOT NULL,
                       name VARCHAR(255) NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(255),
                       user_type VARCHAR(255),
                       PRIMARY KEY (id)
);
