# Users schema

# --- !Ups

CREATE TABLE users (
  id VARCHAR(40) NOT NULL,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  picture VARCHAR(255) NULL,
  stravaToken VARCHAR(255) NULL,
  stravaId INT NULL,
  PRIMARY KEY (id)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE data_sync_requests (
  id VARCHAR(40) NOT NULL,
  userId VARCHAR(40) NOT NULL,
  startedAt TIMESTAMP NOT NULL,
  completedAt TIMESTAMP NULL,
  PRIMARY KEY (id),
  FOREIGN KEY fk_users(userId) REFERENCES users(id) ON DELETE CASCADE
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

# --- !Downs

DROP TABLE users;
DROP TABLE data_sync_requests;