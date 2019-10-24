# --- !Ups

ALTER TABLE strava_oauth_tokens ADD COLUMN athleteId INT NOT NULL;

# --- !Downs

ALTER TABLE strava_oauth_tokens DROP COLUMN athleteId;
