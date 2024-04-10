--Creating database
CREATE DATABASE IF NOT EXISTS music_player_db;

--Creating user/password and granting access to required db
DROP USER IF EXISTS 'music_player_admin'@'localhost';

CREATE USER 'music_player_admin'@'localhost' IDENTIFIED BY 'music_player_admin';

GRANT ALL PRIVILEGES ON music_player_db.* TO 'music_player_admin'@'localhost';

-- Switch to the database
USE music_player_db;

SET GLOBAL FOREIGN_KEY_CHECKS = 0;

--Drop table if alredy exists
DROP TABLE IF EXISTS users_info;

-- Create the users table
CREATE TABLE IF NOT EXISTS users_info (
    id VARCHAR(36) PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(100) UNIQUE,
    password VARCHAR(255),
    active BOOLEAN DEFAULT TRUE
);

-- Create a trigger to automatically set the id column to a UUID value
--Note:Need super database super admin credential, can't do with user created for this user
CREATE TRIGGER before_insert_users_info
BEFORE INSERT ON users_info
FOR EACH ROW
SET NEW.id = UUID();

--Drop table if alredy exists
DROP TABLE IF EXISTS roles;

-- Create the roles table
CREATE TABLE IF NOT EXISTS roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE
);

--Drop table if alredy exists
DROP TABLE IF EXISTS user_roles;

-- Create the user_roles table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id VARCHAR(36),
    role_id INT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users_info(id) ON DELETE NO ACTION ON UPDATE NO ACTION,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

--Drop table if already exists
DROP TABLE IF EXISTS refreshtoken;

-- Create the users table
CREATE TABLE IF NOT EXISTS refreshtoken (
        id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
        expiry_date DATETIME(6) NOT NULL,
        token VARCHAR(36) NOT NULL UNIQUE,
        user_id VARCHAR(36) NOT NULL UNIQUE,
        FOREIGN KEY (user_id) REFERENCES users_info(id) ON DELETE NO ACTION ON UPDATE NO ACTION
)

--Drop table if already exists
DROP TABLE IF EXISTS albums;

-- Create the users table
CREATE TABLE IF NOT EXISTS albums(
    id INT PRIMARY KEY NOT NULL,
    name VARCHAR(50) NOT NULL,
);

--Drop table if already exists
DROP TABLE IF EXISTS musics;

-- Create the users table
CREATE TABLE IF NOT EXISTS musics (
    id UUID PRIMARY KEY NOT NULL,
    title VARCHAR(50) NOT NULL,
    location VARCHAR(100) NOT NULL,
    metadata JSON DEFAULT NULL,
    album_id INT DEFAULT NULL,
    creator_id VARCHAR(36) DEFAULT NULL,
    artist_name VARCHAR(50) DEFAULT NULL,
    FOREIGN KEY (creator_id) REFERENCES users_info(id) ON DELETE NO ACTION ON UPDATE NO ACTION,
    FOREIGN KEY (album_id) REFERENCES albums(id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TRIGGER before_insert_musics
BEFORE INSERT ON musics
FOR EACH ROW
SET NEW.id = UUID();

SET GLOBAL FOREIGN_KEY_CHECKS = 1;

-- Inserting a user into the users table without specifying active column (password is :Password@1234)
INSERT INTO users_info (first_name, last_name, email, password)
VALUES ('Shridhar', 'Choudhari', 'shridhar.c@cumulations.com', '{bcrypt}$2a$10$XY8iPZU1EsqQaf1DXBQI5upSQnDW17Bn72sPubgg2JvYLxbtOg4QG');
INSERT INTO users_info (first_name, last_name, email, password)
VALUES ('Shridhar_admin', 'Choudhari', 'shridhar.admin@cumulations.com', '{bcrypt}$2a$10$XY8iPZU1EsqQaf1DXBQI5upSQnDW17Bn72sPubgg2JvYLxbtOg4QG');
INSERT INTO users_info (first_name, last_name, email, password)
VALUES ('Shridhar_creator', 'Choudhari', 'shridhar.creator@cumulations.com', '{bcrypt}$2a$10$XY8iPZU1EsqQaf1DXBQI5upSQnDW17Bn72sPubgg2JvYLxbtOg4QG');

-- Inserting roles into the roles table
INSERT INTO roles (name) VALUES ('ROLE_ADMIN'), ('ROLE_USER'), ('ROLE_CREATOR');

-- Assigning multiple roles to a user
INSERT INTO user_roles (user_id, role_id) VALUES
('84b74dc2-f28b-11ee-ac0a-90ccdf44ec24', 1), -- Assigns role with id 1 to user shridhar_admin(Chnage uuid accoudingly)
('84b74dc2-f28b-11ee-ac0a-90ccdf44ec24', 2), -- Assigns role with id 2 to user shridhar_admin(Chnage uuid accoudingly)
('84b74dc2-f28b-11ee-ac0a-90ccdf44ec24', 3), -- Assigns role with id 3 to user shridhar_admin(Chnage uuid accoudingly)
('84bf3f0e-f28b-11ee-ac0a-90ccdf44ec24', 2), -- Assigns role with id 2 to user shridhar_creator(Chnage uuid accoudingly)
('84bf3f0e-f28b-11ee-ac0a-90ccdf44ec24', 3), -- Assigns role with id 3 to user shridhar_creator(Chnage uuid accoudingly)
('d14195d8-f28a-11ee-ac0a-90ccdf44ec24', 2); -- Assigns role with id 1 to user shridhar(Chnage uuid accoudingly)

-------------------------------------------------------------------------------------------------------------------------------------------------------------

