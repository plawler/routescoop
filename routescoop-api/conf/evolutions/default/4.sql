# --- !Ups

CREATE TABLE strava_oauth_tokens (
  userId VARCHAR(40) NOT NULL,
  accessToken VARCHAR(255) NOT NULL,
  expiresAt TIMESTAMP NOT NULL,
  refreshToken VARCHAR(255) NOT NULL,
  PRIMARY KEY (userId, accessToken),
  FOREIGN KEY fk_user_access_token(userId) REFERENCES users(id) ON DELETE CASCADE
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

# --- !Downs

DROP TABLE strava_oauth_tokens;
