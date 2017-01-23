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

CREATE TABLE user_data_syncs (
  id VARCHAR(40) NOT NULL,
  userId VARCHAR(40) NOT NULL,
  startedAt TIMESTAMP NOT NULL,
  completedAt TIMESTAMP NULL,
  PRIMARY KEY (id),
  FOREIGN KEY fk_users_data_sync_requests(userId) REFERENCES users(id) ON DELETE CASCADE
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE strava_activities (
  id VARCHAR(40) NOT NULL,
  userId VARCHAR(40) NOT NULL,
  stravaId INT NOT NULL,
  athleteId INT NOT NULL,
  name VARCHAR(255) NOT NULL,
  distance FLOAT NOT NULL,
  movingTime INT NOT NULL,
  elapsedTime INT NOT NULL,
  totalElevationGain FLOAT NOT NULL,
  activityType VARCHAR(255) NOT NULL,
  startedAt TIMESTAMP NOT NULL,
  timezone VARCHAR(255) NOT NULL,
  startLat FLOAT NOT NULL,
  startLong FLOAT NOT NULL,
  trainer BOOLEAN NOT NULL,
  commute BOOLEAN NOT NULL,
  manual BOOLEAN NOT NULL,
  averageSpeed FLOAT NOT NULL,
  maxSpeed FLOAT NOT NULL,
  externalId VARCHAR(255) NULL,
  endLat FLOAT NULL,
  endLong FLOAT NULL,
  mapPolyLine TEXT NULL,
  mapPolyLineSummary TEXT NULL,
  averageCadence FLOAT NULL,
  averageTemp INT NULL,
  averageWatts INT NULL,
  weightedAverageWatts INT NULL,
  kilojoules FLOAT NULL,
  deviceWatts BOOLEAN NULL,
  averageHeartRate FLOAT NULL,
  maxHeartRate FLOAT NULL,
  workoutType INT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY fk_users_strava_activities(userId) REFERENCES users(id) ON DELETE CASCADE
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


# --- !Downs

DROP TABLE users;
DROP TABLE user_data_syncs;
DROP TABLE strava_activities;
