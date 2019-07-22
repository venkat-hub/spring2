CREATE TABLE IF NOT EXISTS people  (
    person_id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(20),
    last_name VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS people2  (
    person_id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(20),
    last_name VARCHAR(20)
);
truncate table people2;