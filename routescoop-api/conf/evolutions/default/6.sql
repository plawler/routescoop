# --- !Ups

CREATE TABLE in_zone_stats (
    activityId VARCHAR(40) NOT NULL,
    zone VARCHAR(255) NOT NULL,
    avgInZone DOUBLE NOT NULL,
    secondsInZone INT NULL,
    pctInZone DOUBLE NULL,
    PRIMARY KEY (activityId, zone)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8;

# --- !Downs

DROP TABLE in_zone_stats;
