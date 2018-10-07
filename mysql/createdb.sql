#USE sql7260160;
DROP TABLE ip_uploader;
CREATE TABLE ip_uploader (
	entry_id INT NOT NULL AUTO_INCREMENT PRIMARY KEYsql7260160,
	ip_address VARCHAR(39) NOT NULL
);

INSERT INTO ip_uploader (ip_address) VALUES ('194.166.157.228');

UPDATE ip_uploader SET ip_address='194.166.157.0' WHERE entry_id=1;