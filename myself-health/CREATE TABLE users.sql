
CREATE TABLE users (
	id INT NOT NULL AUTO_INCREMENT,
	first_name CHAR(40), 
	middle_name CHAR(40), 
	last_name CHAR(40), 
	email_address CHAR(80)
	PRIMARY KEY (id)
)	ENGINE=INNODB
	DEFAULT CHARACTER SET = utf8
	COLLATE = utf8_unicode_ci;