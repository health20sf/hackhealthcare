CREATE TABLE categories (
	id INT NOT NULL AUTO_INCREMENT,
	name CHAR(40),
	description CHAR(80),
	PRIMARY KEY (id)
)	ENGINE=INNODB
	DEFAULT CHARACTER SET = utf8
	COLLATE = utf8_unicode_ci;
