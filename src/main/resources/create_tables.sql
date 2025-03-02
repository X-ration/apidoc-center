-- create database
CREATE DATABASE IF NOT EXISTS `apidoc_center` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE apidoc_center;

-- create tables
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`(
    `id` BIGINT(64) NOT NULL AUTO_INCREMENT,
    `email` VARCHAR(256) NOT NULL UNIQUE,
    `username` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL UNIQUE,
    `password` CHAR(60) NOT NULL,
    `avatar_url` VARCHAR(256) NOT NULL,
    `description` VARCHAR(100) DEFAULT NULL,
    `user_type` VARCHAR(64) NOT NULL COMMENT '以逗号分隔的UserType枚举值',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `user_oauth2_github`;
CREATE TABLE `user_oauth2_github`(
    `id` BIGINT(64) NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT(64) NOT NULL DEFAULT 0,
    `github_id` INT(32) NOT NULL,
    `username` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL UNIQUE,
    `real_name` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL UNIQUE,
    `avatar_url` VARCHAR(256) NOT NULL,
    `email` VARCHAR(256) NOT NULL UNIQUE,
    `bio` VARCHAR(100) DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY(`id`)
)ENGINE=INNODB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `user_oauth2_huawei`;
CREATE TABLE `user_oauth2_huawei`(
    `id` BIGINT(64) NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT(64) NOT NULL DEFAULT 0,
    `display_name` VARCHAR(32) NOT NULL,
    `head_picture_url` VARCHAR(256) NOT NULL,
    `union_id` VARCHAR(100) NOT NULL,
    `open_id` VARCHAR(100) NOT NULL,
    `display_name_flag` tinyint(1) NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY(`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `remember_me_token`;
CREATE TABLE `remember_me_token` (
    `id` bigint(64) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username` varchar(64) NOT NULL,
    `series` varchar(64) NOT NULL,
    `token` varchar(64) NOT NULL,
    `last_used` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 项目相关
DROP TABLE IF EXISTS `project`;
CREATE TABLE `project` (
    `id` BIGINT(64) NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(32) NOT NULL,
    `description` VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `access_mode` ENUM('PUBLIC', 'PRIVATE') COMMENT '访问模式-公开还是私有',
    `create_user_id` BIGINT(64) NOT NULL,
    `update_user_id` BIGINT(64) NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `project_deployment`;
CREATE TABLE `project_deployment` (
    `id` BIGINT(64) NOT NULL AUTO_INCREMENT,
    `project_id` BIGINT(64) NOT NULL,
    `environment` VARCHAR(32) NOT NULL,
    `deployment_url` VARCHAR(256) NOT NULL,
    `is_enabled` BOOL NOT NULL DEFAULT TRUE,
    `create_user_id` BIGINT(64) NOT NULL,
    `update_user_id` BIGINT(64) NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='项目环境部署地址表';

DROP TABLE IF EXISTS `project_shared_user`;
CREATE TABLE `project_shared_user` (
    `id` BIGINT(64) NOT NULL AUTO_INCREMENT,
    `project_id` BIGINT(64) NOT NULL,
    `user_id` BIGINT(64) NOT NULL,
    `is_share` BOOL NOT NULL DEFAULT TRUE,
    `create_user_id` BIGINT(64) NOT NULL,
    `update_user_id` BIGINT(64) NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='私有项目允许访问的用户列表表';

-- 分组、接口相关
DROP TABLE IF EXISTS `project_group`;
CREATE TABLE `project_group` (
    `id` BIGINT(64) NOT NULL AUTO_INCREMENT,
    `project_id` BIGINT(64) NOT NULL,
    `name` VARCHAR(32) NOT NULL,
    `create_user_id` BIGINT(64) NOT NULL,
    `update_user_id` BIGINT(64) NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='项目分组表';

DROP TABLE IF EXISTS `group_interface`;
CREATE TABLE `group_interface` (
    `id` BIGINT(64) NOT NULL AUTO_INCREMENT,
    `group_id` BIGINT(64) NOT NULL,
    `name` VARCHAR(32) NOT NULL,
    `description` VARCHAR(100) DEFAULT NULL,
    `relative_path` VARCHAR(256) NOT NULL,
    `method` ENUM('GET','POST','PUT','DELETE','HEAD','OPTIONS','PATCH','TRACE') NOT NULL,
    `type` ENUM('FORM_URLENCODED','FORM_DATA','JSON','NO_BODY') NOT NULL,
    `response_type` ENUM('FILE','TEXT') NOT NULL,
    `create_user_id` BIGINT(64) NOT NULL,
    `update_user_id` BIGINT(64) NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='项目接口表';

DROP TABLE IF EXISTS `interface_field`;
CREATE TABLE `interface_field`
(
    `id` BIGINT(64) NOT NULL AUTO_INCREMENT,
    `interface_id` BIGINT(64) NOT NULL,
    `name` VARCHAR(32) NOT NULL,
    `description` VARCHAR(100) DEFAULT NULL,
    `type` ENUM('TEXT','FILE') NOT NULL DEFAULT 'TEXT',
    `required` BOOL NOT NULL DEFAULT TRUE,
    `create_user_id` BIGINT(64) NOT NULL,
    `update_user_id` BIGINT(64) NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='项目接口字段表';

DROP TABLE IF EXISTS `interface_header`;
CREATE TABLE `interface_header`
(
    `id` BIGINT(64) NOT NULL AUTO_INCREMENT,
    `interface_id` BIGINT(64) NOT NULL,
    `name` VARCHAR(32) NOT NULL,
    `description` VARCHAR(100) DEFAULT NULL,
    `required` BOOL NOT NULL DEFAULT TRUE,
    `create_user_id` BIGINT(64) NOT NULL,
    `update_user_id` BIGINT(64) NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='项目接口头部表';