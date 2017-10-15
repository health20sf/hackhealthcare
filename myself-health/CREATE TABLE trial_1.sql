
CREATE TABLE trial_1 (
	id INT NOT NULL AUTO_INCREMENT,
	trial_id INT NOT NULL,
	user_id INT NOT NULL,
	cohort CHAR(20),
	observation INT NOT NULL,
	timestamp TIMESTAMP NOT NULL,
	data INT,
	PRIMARY KEY (id),
	FOREIGN KEY (trial_id) REFERENCES trials(id),
	FOREIGN KEY (user_id) REFERENCES users(id)
)	ENGINE=INNODB
	DEFAULT CHARACTER SET = utf8
	COLLATE = utf8_unicode_ci;