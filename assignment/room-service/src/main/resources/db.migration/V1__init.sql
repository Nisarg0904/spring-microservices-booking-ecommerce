CREATE TABLE rooms (
                       id VARCHAR(255) NOT NULL,
                       room_name VARCHAR(255) NOT NULL,
                       capacity INT NOT NULL,
                       features TEXT,
                       availability BOOLEAN NOT NULL,
                       PRIMARY KEY (id)
);
