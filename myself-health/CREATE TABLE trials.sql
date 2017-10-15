
CREATE TABLE trials (
	id INT NOT NULL AUTO_INCREMENT,
	name CHAR(40), 
	category_id INT NOT NULL,
	summary TEXT(256),
	min_num_ppl INT NOT NULL,
	max_num_ppl INT,
	duration_days INT,
	treatment TEXT(256),
	control TEXT(256),
	dose INT,
	dose_units CHAR(20)
	kpi TEXT(256),
	kpi_frequency TEXT(256),
	PRIMARY KEY (id)
	FOREIGN KEY (category_id) REFERENCES categories(id),

)	ENGINE=INNODB
	DEFAULT CHARACTER SET = utf8
	COLLATE = utf8_unicode_ci;
