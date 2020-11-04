CREATE DATABASE reposcape;
USE reposcape;
CREATE USER 'reposcaper'@'localhost' IDENTIFIED WITH mysql_native_password BY 'repotrial';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, INDEX, DROP, ALTER, CREATE TEMPORARY TABLES, LOCK TABLES, REFERENCES, CREATE VIEW ON * TO 'reposcaper'@'localhost';
GRANT FILE ON *.* TO 'reposcaper'@'localhost';
