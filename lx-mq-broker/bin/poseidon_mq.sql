
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for position_offset
-- ----------------------------
DROP TABLE IF EXISTS `position_offset`;
CREATE TABLE `position_offset`  (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键id',
  `last_offset_id` bigint(20) NOT NULL DEFAULT -1 COMMENT '上一次的偏移量消费id',
  `current_offset_id` bigint(255) NOT NULL DEFAULT 0 COMMENT '当前消费的偏移量id',
  `update_time` datetime(3) NOT NULL DEFAULT '0001-01-01 00:00:00.000' ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '消费偏移量记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for subscription
-- ----------------------------
DROP TABLE IF EXISTS `subscription`;
CREATE TABLE `subscription`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT -1 COMMENT '租户id',
  `subscription_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '订阅名称',
  `topic_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'topic名称',
  `status` tinyint(3) NOT NULL DEFAULT 0 COMMENT '1-在线；0-下线',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '订阅模式;shared-广播;direct-直连',
  `meta_data` json NULL COMMENT '订阅者元数据信息',
  `create_time` datetime(3) NOT NULL DEFAULT '0001-01-01 00:00:00.000' ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT '0001-01-01 00:00:00.000' COMMENT '更新时间',
  `version` int(11) NOT NULL DEFAULT 0,
  `deleted` tinyint(3) NOT NULL DEFAULT 0 COMMENT '1-是;0-否',
  `delete_time` datetime NOT NULL DEFAULT '0001-01-01 00:00:00' COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `udx_tenant_subscription`(`tenant_id`, `subscription_name`, `topic_name`) USING BTREE COMMENT '租户，订阅唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for topic
-- ----------------------------
DROP TABLE IF EXISTS `topic`;
CREATE TABLE `topic`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户id',
  `topic_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主题名称',
  `type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'binlog',
  `meta_data` json NULL COMMENT 'topic元数据信息',
  `enable` tinyint(3) NOT NULL DEFAULT 0 COMMENT '是否可用 1-是;0-否',
  `create_time` datetime(3) NOT NULL ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT '0001-01-01 00:00:00.000' COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `oauth_client_details`;
CREATE TABLE `oauth_client_details`  (
  `id` bigint(20) NOT NULL COMMENT '主键id',
  `client_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'client编号',
  `client_secret` varchar(254) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密钥',
  `resource_ids` varchar(254) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '资源id',
  `grant_types` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '授权模式',
  `authorities` varchar(254) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '权限',
  `redirect_uri` varchar(254) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '回调uri',
  `access_token_validity` int(11) NOT NULL DEFAULT 0 COMMENT 'token过期时间',
  `refresh_token_validity` int(11) NOT NULL DEFAULT 0 COMMENT '刷新token过期时间',
  `additional_information` json NULL COMMENT '扩展信息，必须是json',
  `auto_approve_scopes` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '自动放行的scope',
  `create_time` datetime(3) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(3) NULL DEFAULT NULL COMMENT '更新时间',
  `deleted` tinyint(3) NOT NULL DEFAULT 0 COMMENT '是否已删除 0-否  1-是',
  `remark` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '备注',
  `scope` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '作用域',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户id',
  UNIQUE INDEX `udx_client_id`(`client_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '应用client' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for trace_log
-- ----------------------------
DROP TABLE IF EXISTS `trace_log`;
CREATE TABLE `trace_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户id',
  `topic_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '主题名称',
  `message_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '消息唯一id',
  `producer_log` json NULL COMMENT '生产者轨迹日志',
  `subscription_log` json NULL COMMENT '订阅者轨迹日志',
  `create_time` datetime(3) NOT NULL ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT '0001-01-01 00:00:00.000' COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `udx_message_id`(`message_id`) USING HASH COMMENT '唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '消息追踪' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
