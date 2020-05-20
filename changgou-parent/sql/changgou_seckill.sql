/*
Navicat MySQL Data Transfer

Source Server         : 128
Source Server Version : 50644
Source Host           : 192.168.200.128:3306
Source Database       : changgou_seckill

Target Server Type    : MYSQL
Target Server Version : 50644
File Encoding         : 65001

Date: 2019-07-07 14:09:52
*/
CREATE DATABASE IF NOT EXISTS `changgou_seckill`  DEFAULT CHARACTER SET utf8;

USE `changgou_seckill`;
SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for tb_seckill_activity
-- ----------------------------
DROP TABLE IF EXISTS `tb_seckill_activity`;
CREATE TABLE `tb_seckill_activity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `title` varchar(50) DEFAULT NULL COMMENT '秒杀活动标题',
  `status` varchar(1) DEFAULT NULL COMMENT '状态',
  `startDate` date DEFAULT NULL COMMENT '开始日期',
  `endDate` date DEFAULT NULL COMMENT '截至日期',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tb_seckill_activity
-- ----------------------------
INSERT INTO `tb_seckill_activity` VALUES ('1', '26-30日活动', '1', '2019-06-25', '2019-06-30');
INSERT INTO `tb_seckill_activity` VALUES ('2', '7-1日活动', '1', '2019-07-01', '2019-07-01');

-- ----------------------------
-- Table structure for tb_seckill_goods
-- ----------------------------
DROP TABLE IF EXISTS `tb_seckill_goods`;
CREATE TABLE `tb_seckill_goods` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `sku_id` varchar(20) DEFAULT NULL COMMENT 'skuId',
  `seckill_price` int(11) DEFAULT NULL COMMENT '秒杀价格',
  `seckill_num` int(11) DEFAULT NULL COMMENT '秒杀数量',
  `seckill_surplus` int(11) DEFAULT NULL COMMENT '剩余数量',
  `seckill_limit` int(11) DEFAULT NULL COMMENT '限购数量',
  `time_id` int(11) DEFAULT NULL COMMENT '秒杀时间段id',
  `activity_id` bigint(20) DEFAULT NULL COMMENT '秒杀活动id',
  `sku_name` varchar(200) DEFAULT NULL COMMENT 'sku商品名称',
  `sku_sn` varchar(100) DEFAULT NULL COMMENT 'sn',
  `sku_price` int(11) DEFAULT NULL COMMENT '原价格',
  `sku_image` varchar(200) DEFAULT NULL COMMENT '秒杀商品图片',
  `seq` int(11) DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tb_seckill_goods
-- ----------------------------
INSERT INTO `tb_seckill_goods` VALUES ('1', '100000003145', '2', '100', '30', '2', '5', '1', '手机', '111', '95900', 'https://img14.360buyimg.com/n1/s546x546_jfs/t28906/30/1571661431/255345/986f5fcb/5ce4148aN55586a52.jpg', null);

-- ----------------------------
-- Table structure for tb_seckill_time
-- ----------------------------
DROP TABLE IF EXISTS `tb_seckill_time`;
CREATE TABLE `tb_seckill_time` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) DEFAULT NULL COMMENT '时间段名称',
  `start_time` varchar(8) DEFAULT NULL COMMENT '开始时间',
  `end_time` varchar(8) DEFAULT NULL COMMENT '截至时间',
  `status` varchar(1) DEFAULT NULL COMMENT '状态',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tb_seckill_time
-- ----------------------------
INSERT INTO `tb_seckill_time` VALUES ('1', '0:00', '00:00:00', '10:00:00', '1');
INSERT INTO `tb_seckill_time` VALUES ('2', '10:00', '10:00:00', '12:00:00', '1');
INSERT INTO `tb_seckill_time` VALUES ('3', '12:00', '12:00:00', '14:00:00', '1');
INSERT INTO `tb_seckill_time` VALUES ('4', '14:00', '14:00:00', '16:00:00', '1');
INSERT INTO `tb_seckill_time` VALUES ('5', '16:00', '16:00:00', '23:59:59', '1');
