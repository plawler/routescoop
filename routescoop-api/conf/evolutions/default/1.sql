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
  distance DOUBLE NOT NULL,
  movingTime INT NOT NULL,
  elapsedTime INT NOT NULL,
  totalElevationGain DOUBLE NOT NULL,
  activityType VARCHAR(255) NOT NULL,
  startedAt TIMESTAMP NOT NULL,
  timezone VARCHAR(255) NOT NULL,
  startLat DOUBLE NOT NULL,
  startLong DOUBLE NOT NULL,
  trainer BOOLEAN NOT NULL,
  commute BOOLEAN NOT NULL,
  manual BOOLEAN NOT NULL,
  averageSpeed DOUBLE NOT NULL,
  maxSpeed DOUBLE NOT NULL,
  externalId VARCHAR(255) NULL,
  endLat DOUBLE NULL,
  endLong DOUBLE NULL,
  mapPolyLine TEXT NULL,
  mapPolyLineSummary TEXT NULL,
  averageCadence DOUBLE NULL,
  averageTemp INT NULL,
  averageWatts INT NULL,
  weightedAverageWatts INT NULL,
  kilojoules DOUBLE NULL,
  deviceWatts BOOLEAN NULL,
  averageHeartRate DOUBLE NULL,
  maxHeartRate DOUBLE NULL,
  workoutType INT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY fk_users_strava_activities(userId) REFERENCES users(id) ON DELETE CASCADE
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE strava_laps (
  id VARCHAR(40) NOT NULL,
  activityId VARCHAR(40) NOT NULL,
  stravaId INT NOT NULL,
  stravaActivityId INT NOT NULL,
  athleteId INT NOT NULL,
  resourceState INT NOT NULL,
  name VARCHAR(50) NOT NULL,
  elapsedTime INT NOT NULL,
  movingTime INT NOT NULL,
  startedAt TIMESTAMP NOT NULL,
  distance DOUBLE NOT NULL,
  startIndex INT NOT NULL,
  endIndex INT NOT NULL,
  lapIndex INT NOT NULL,
  totalElevationGain DOUBLE NOT NULL,
  averageSpeed DOUBLE NOT NULL,
  maxSpeed DOUBLE NOT NULL,
  averageWatts DOUBLE NOT NULL,
  deviceWatts BOOLEAN NULL,
  averageCadence  DOUBLE NULL,
  averageHeartRate  DOUBLE NULL,
  maxHeartRate  DOUBLE NULL,
  PRIMARY KEY (id),
  FOREIGN KEY fk_strava_activities_strava_laps(activityId) REFERENCES strava_activities(id) ON DELETE CASCADE
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


# --- !Downs

DROP TABLE users;
DROP TABLE user_data_syncs;
DROP TABLE strava_activities;
DROP TABLE strava_laps;
