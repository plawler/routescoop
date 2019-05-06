# power_efforts indices

# --- !Ups

ALTER TABLE power_efforts ADD INDEX idx_intervalLengthInSeconds_watts (intervalLengthInSeconds, criticalPower);

# --- !Downs

DROP INDEX idx_intervalLengthInSeconds_watts ON power_efforts;
