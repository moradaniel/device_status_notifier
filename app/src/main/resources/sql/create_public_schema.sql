/*
 Source Server         : localhost_5432_device_status_dev
 Source Server Type    : PostgreSQL
 Source Server Version : 110012
 Source Host           : localhost:5432
 Source Catalog        : device_status_dev
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 110012
 File Encoding         : 65001

 Date: 01/07/2022 09:24:44
*/


-- ----------------------------
-- Sequence structure for devices_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."devices_id_seq" CASCADE;
CREATE SEQUENCE "public"."devices_id_seq"
    INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
ALTER SEQUENCE "public"."devices_id_seq" OWNER TO "device_status_user";

-- ----------------------------
-- Table structure for devices
-- ----------------------------
DROP TABLE IF EXISTS "public"."devices" CASCADE;
CREATE TABLE "public"."devices" (
                                    "id" int8 NOT NULL DEFAULT nextval('devices_id_seq'::regclass),
                                    "name" varchar(255) COLLATE "pg_catalog"."default",
                                    "status" varchar(255) COLLATE "pg_catalog"."default" NOT NULL
)
;
ALTER TABLE "public"."devices" OWNER TO "device_status_user";

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."devices_id_seq"
    OWNED BY "public"."devices"."id";
SELECT setval('"public"."devices_id_seq"', 1, false);

-- ----------------------------
-- Uniques structure for table devices
-- ----------------------------
ALTER TABLE "public"."devices" ADD CONSTRAINT "uk_eocaa1xu64cgt3kvqnpbtkftw" UNIQUE ("name");

-- ----------------------------
-- Primary Key structure for table devices
-- ----------------------------
ALTER TABLE "public"."devices" ADD CONSTRAINT "devices_pkey" PRIMARY KEY ("id");
