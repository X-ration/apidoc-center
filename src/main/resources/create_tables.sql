-- create database
CREATE DATABASE IF NOT EXISTS `apidoc_center` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE apidoc_center;

-- create tables
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`(
    `id` BIGINT(64) NOT NULL AUTO_INCREMENT,
    `email` VARCHAR(256) NOT NULL UNIQUE,
    `nickname` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `avatar_url` VARCHAR(256) NOT NULL,
    `password` CHAR(60) NOT NULL,
    `user_type` ENUM('NORMAL','OAUTH2_GITHUB','OAUTH2_HUAWEI'),
    `is_enabled` BOOL NOT NULL DEFAULT TRUE,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY(`id`)
)ENGINE=INNODB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `user_authority`;
CREATE TABLE `user_authority` (
    `id` bigint(64) NOT NULL AUTO_INCREMENT,
    `user_id` bigint(64) NOT NULL,
    `authority` enum('ROLE_USER','ROLE_ADMIN') NOT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;