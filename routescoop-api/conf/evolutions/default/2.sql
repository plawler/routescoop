# calendar table

# --- !Ups

CREATE TABLE ints (
  i TINYINT
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE days (
  dt DATE NOT NULL PRIMARY KEY
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

INSERT INTO ints
VALUES (0), (1), (2), (3), (4), (5), (6), (7), (8), (9);

INSERT INTO days
SELECT DATE('2000-01-01') + INTERVAL a.i * 10000 + b.i * 1000 + c.i * 100 + d.i * 10 + e.i DAY
FROM ints a
       JOIN ints b
       JOIN ints c
       JOIN ints d
       JOIN ints e
WHERE DATE('2000-01-01') + INTERVAL a.i * 10000 + b.i * 1000 + c.i * 100 + d.i * 10 + e.i DAY < '2050-01-01'
ORDER BY 1;

ALTER TABLE strava_activities ADD INDEX idx_userId_startedAt (userId, startedAt);

# --- !Downs

DROP TABLE days;
DROP TABLE ints;
DROP INDEX idx_userId_startedAt ON strava_activities;
