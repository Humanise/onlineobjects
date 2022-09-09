-- -------------------------------------------------------------
-- TablePlus 4.8.2(436)
--
-- https://tableplus.com/
--
-- Database: onlineobjects_empty
-- Generation Time: 2022-09-09 07:26:24.0510
-- -------------------------------------------------------------


-- This script only contains the table creation statements and does not fully represent the table in the database. It's still missing: indices, triggers. Do not use it as a backup.

-- Table Definition
CREATE TABLE "public"."databasechangelog" (
    "id" varchar(255) NOT NULL,
    "author" varchar(255) NOT NULL,
    "filename" varchar(255) NOT NULL,
    "dateexecuted" timestamp NOT NULL,
    "orderexecuted" int4 NOT NULL,
    "exectype" varchar(10) NOT NULL,
    "md5sum" varchar(35),
    "description" varchar(255),
    "comments" varchar(255),
    "tag" varchar(255),
    "liquibase" varchar(20),
    "contexts" varchar(255),
    "labels" varchar(255),
    "deployment_id" varchar(10)
);

-- This script only contains the table creation statements and does not fully represent the table in the database. It's still missing: indices, triggers. Do not use it as a backup.

-- Table Definition
CREATE TABLE "public"."databasechangeloglock" (
    "id" int4 NOT NULL,
    "locked" bool NOT NULL,
    "lockgranted" timestamp,
    "lockedby" varchar(255),
    PRIMARY KEY ("id")
);

INSERT INTO "public"."databasechangelog" ("id", "author", "filename", "dateexecuted", "orderexecuted", "exectype", "md5sum", "description", "comments", "tag", "liquibase", "contexts", "labels", "deployment_id") VALUES
('baseline-1', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.432832', 1, 'EXECUTED', '8:e20522698832f54443dbfb67634494b8', 'createTable tableName=address', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-2', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.440504', 2, 'EXECUTED', '8:5bb2fdbee5be63ada8beec5d7db61a76', 'createTable tableName=application', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-3', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.444408', 3, 'EXECUTED', '8:1d7bdc574f19a0d3fea92d5ff2b8ecf8', 'createTable tableName=client', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-4', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.449243', 4, 'EXECUTED', '8:a0f9b8020b87c41395e6be288311aa35', 'createTable tableName=comment', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-5', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.45406', 5, 'EXECUTED', '8:b87f231098ab149310467965bfa8ae76', 'createTable tableName=compounddocument', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-6', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.460039', 6, 'EXECUTED', '8:7020038e9f710e5faed8c78f8dc2af09', 'createTable tableName=emailaddress', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-7', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.46526', 7, 'EXECUTED', '8:deee8a122af707c96494f4306d700a33', 'createTable tableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-8', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.469515', 8, 'EXECUTED', '8:881dccb081f9dbdb980b4abfb4e03285', 'createTable tableName=event', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-9', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.473423', 9, 'EXECUTED', '8:b97ef96d3fb6da07aa091ae5c9530bac', 'createTable tableName=pile', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-10', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.47822', 10, 'EXECUTED', '8:db7f8a74a6f257678dffc6fae7524207', 'createTable tableName=website', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-11', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.482123', 11, 'EXECUTED', '8:c9afb56589304f0cf2e7a504fceb8c9a', 'createTable tableName=item', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-12', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.487717', 12, 'EXECUTED', '8:dc73010d3cfb81b1f3e731c4ab142c15', 'createTable tableName=relation', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-13', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.493158', 13, 'EXECUTED', '8:b2f8cd4220ba7f8af82dd43779ce4653', 'createTable tableName=user', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-14', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.496782', 14, 'EXECUTED', '8:ce66d1f855a87ed261cb394ae69c4a57', 'createTable tableName=vote', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-15', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.500872', 15, 'EXECUTED', '8:593595e4f8c50a4ed1ca7aab3974b3bc', 'createTable tableName=word', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-16', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.505379', 16, 'EXECUTED', '8:a81920472cee059884fea2246a068974', 'createTable tableName=statement', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-17', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.509032', 17, 'EXECUTED', '8:ac213993aed60fde883c7872f833fd2a', 'createTable tableName=topic', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-18', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.514001', 18, 'EXECUTED', '8:4d77d16e49ade8e2bb368394b97b7b78', 'createTable tableName=invitation', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-19', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.518388', 19, 'EXECUTED', '8:acabbc675d7b7325f162e8c390c40d25', 'createTable tableName=webnode', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-20', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.52231', 20, 'EXECUTED', '8:3fdcd1af5f7590f71ed1acfe9e5efc7f', 'createTable tableName=webpage', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-21', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.526746', 21, 'EXECUTED', '8:afd771691a5bc80543d5251327d76ebf', 'createTable tableName=image', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-22', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.531501', 22, 'EXECUTED', '8:6df3fc05e8919f3ee8162737d6806770', 'createTable tableName=video', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-23', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.535923', 23, 'EXECUTED', '8:8230a940f0d5b35c4091953bc44ab132', 'createTable tableName=location', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-24', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.541499', 24, 'EXECUTED', '8:e7d49c1fd140b33d98002bfa17aa7353', 'createTable tableName=remoteaccount', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-25', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.545345', 25, 'EXECUTED', '8:48a08920d2d50414d41e6be3e9658d3f', 'createTable tableName=headerpart', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-26', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.55041', 26, 'EXECUTED', '8:a86542ddc5cee3afee903d8c7ef17f05', 'createTable tableName=hypothesis', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-27', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.556942', 27, 'EXECUTED', '8:1663b5d1a371f2df077a06fc9a3a88e5', 'createTable tableName=question', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-28', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.561992', 28, 'EXECUTED', '8:1699d9b64893b64400df0fe830673a1b', 'createTable tableName=internetaddress', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-29', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.583178', 29, 'EXECUTED', '8:fab5ff1b3d8368935fba77a1bdce918c', 'createTable tableName=language', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-30', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.612698', 30, 'EXECUTED', '8:f8fcc0117c10a47944ef89bc833810b7', 'createTable tableName=property', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-31', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.635994', 31, 'EXECUTED', '8:b10b2e54b2d2455727ec606c6fb7bd01', 'createTable tableName=lexicalcategory', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-32', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.646514', 32, 'EXECUTED', '8:b0b8fdfe6cc1740e2c2b3c6322ae8d90', 'createTable tableName=person', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-33', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.650806', 33, 'EXECUTED', '8:2979e2c1c1bf0f91ba804d4278509a57', 'createTable tableName=imagegallery', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-34', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.654796', 34, 'EXECUTED', '8:23f248650acbb697fb386ccf0a797da2', 'createTable tableName=webstructure', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-35', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.658693', 35, 'EXECUTED', '8:99adcb251b00ee8f9fd9460464c9228c', 'createTable tableName=rating', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-36', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.663058', 36, 'EXECUTED', '8:f40392379f1fe4d2cb2a04ab550e48e6', 'createTable tableName=imagepart', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-37', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.670011', 37, 'EXECUTED', '8:3a68730391e7dedef7c88c5c7474224a', 'createTable tableName=htmlpart', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-38', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.676706', 38, 'EXECUTED', '8:346fb876d6ab8a9e1f51b41823d60f16', 'createTable tableName=phonenumber', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-39', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.681663', 39, 'EXECUTED', '8:4fac12ca39199ff0df0bfc8ae289d290', 'createTable tableName=log', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-40', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.68669', 40, 'EXECUTED', '8:d99395a57ed0bd32cdaa97f2e4918194', 'createTable tableName=privilege', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-41', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.695848', 41, 'EXECUTED', '8:df650f156e91f241b5e00b8b3a0ea9ec', 'addForeignKeyConstraint baseTableName=pile, constraintName=fk18vjqbaebpsa3twxo1ns2ommc, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-42', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.700625', 42, 'EXECUTED', '8:c7ac524497303d57cfa2b462df56e37f', 'addForeignKeyConstraint baseTableName=compounddocument, constraintName=fk2e80bfc6e94a3d71, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-43', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.707026', 43, 'EXECUTED', '8:5da943f996c3813d35c50abd21d2df51', 'addForeignKeyConstraint baseTableName=website, constraintName=fk2h2i95i0brwnjeh3l7etw8b5m, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-44', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.711273', 44, 'EXECUTED', '8:06d1cfe310456dd5051fe7ce357657ad', 'addForeignKeyConstraint baseTableName=user, constraintName=fk36ebcbe94a3d71, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-45', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.715876', 45, 'EXECUTED', '8:5eb787fe64349c8552b9a16a2dbaa3e1', 'addForeignKeyConstraint baseTableName=vote, constraintName=fk3752eae94a3d71, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-46', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.720705', 46, 'EXECUTED', '8:807173bf676c944675c1591add2d27b5', 'addForeignKeyConstraint baseTableName=word, constraintName=fk37c70ae94a3d71, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-47', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.724871', 47, 'EXECUTED', '8:03596ef2042de77c4948ed5ec8cd0333', 'addForeignKeyConstraint baseTableName=comment, constraintName=fk38a5ee5fe94a3d71, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-48', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.729123', 48, 'EXECUTED', '8:768dd2f8d94a650dea021a7e3eb9e0de', 'addForeignKeyConstraint baseTableName=statement, constraintName=fk39embti9mal5s7gq5l8xvurlg, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-49', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.733539', 49, 'EXECUTED', '8:ef02771c8c5b19145b450123fb2733cd', 'addForeignKeyConstraint baseTableName=topic, constraintName=fk39v12y7xnarx9tdr8usf789yx, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-50', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.738151', 50, 'EXECUTED', '8:4a7f70a2fd9c0d3f085829b315c1ca6b', 'addForeignKeyConstraint baseTableName=invitation, constraintName=fk473f7799e94a3d71, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-51', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.742573', 51, 'EXECUTED', '8:0cf2d32e4c1395a4e0919c37322250fb', 'addForeignKeyConstraint baseTableName=webnode, constraintName=fk48f7af56e94a3d71, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-52', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.746718', 52, 'EXECUTED', '8:67b138de9ccf4480ce1a9afa0d1c54ce', 'addForeignKeyConstraint baseTableName=webpage, constraintName=fk48f863e3e94a3d71, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-53', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.751508', 53, 'EXECUTED', '8:f8b0c152d17ffbac74a02b656f7b50a5', 'addForeignKeyConstraint baseTableName=client, constraintName=fk4rhn1b71ma8pdgnp2nro4eewl, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-54', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.758011', 54, 'EXECUTED', '8:2a80369fa62ca151f6ce366460f5ea3b', 'addForeignKeyConstraint baseTableName=event, constraintName=fk5c6729ae94a3d71, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-55', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.763851', 55, 'EXECUTED', '8:f16a873eaa2b3ff9375e8c2c4459d848', 'addForeignKeyConstraint baseTableName=application, constraintName=fk5ca40550e94a3d71, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-56', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.77039', 56, 'EXECUTED', '8:6a07d0f78fac1644dc9a58e8caa1b3a9', 'addForeignKeyConstraint baseTableName=emailaddress, constraintName=fk5cf248d8e94a3d71, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-57', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.775698', 57, 'EXECUTED', '8:8f16a43f708dcf80c245d879bb4c0c90', 'addForeignKeyConstraint baseTableName=image, constraintName=fk5faa95be94a3d71, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-58', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.780543', 58, 'EXECUTED', '8:05e961668b373dd1757a2f6058d6aac5', 'addForeignKeyConstraint baseTableName=video, constraintName=fk6b0147be94a3d71, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-59', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.786217', 59, 'EXECUTED', '8:7a9581760ac47e034dfcb56a3228c28a', 'addForeignKeyConstraint baseTableName=location, constraintName=fk714f9fb5e94a3d71, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-60', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.791086', 60, 'EXECUTED', '8:ac6d79371c05f1e0c34d72c6fe3a2373', 'addForeignKeyConstraint baseTableName=remoteaccount, constraintName=fk7553d727e94a3d71, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-61', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.794727', 61, 'EXECUTED', '8:6fc4bfed0717c2fed91f515ab26b593a', 'addForeignKeyConstraint baseTableName=headerpart, constraintName=fk75eb3800e94a3d71, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-62', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.798491', 62, 'EXECUTED', '8:3818a7c81c02061b4ff74f28b0b03e87', 'addForeignKeyConstraint baseTableName=hypothesis, constraintName=fk8061199ce94a3d71, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-63', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.801989', 63, 'EXECUTED', '8:2ef13f23473132dd959a345b1f8f16f2', 'addForeignKeyConstraint baseTableName=question, constraintName=fk8vs64po987gff4774cibx3n3a, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-64', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.806827', 64, 'EXECUTED', '8:4256970b9357c1c9c7829865d1c04c35', 'addForeignKeyConstraint baseTableName=internetaddress, constraintName=fk96c65993e94a3d71, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-65', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.81022', 65, 'EXECUTED', '8:c74ceda556b2718574981b58d2e4df85', 'addForeignKeyConstraint baseTableName=language, constraintName=fk9fd29358e94a3d71, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-66', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.813683', 66, 'EXECUTED', '8:3310303a96da732c8319a4bbad0201dc', 'addForeignKeyConstraint baseTableName=property, constraintName=fk_property_entity, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-67', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.817508', 67, 'EXECUTED', '8:d62c73514bb4d82d64bddd61d2b9ae98', 'addForeignKeyConstraint baseTableName=lexicalcategory, constraintName=fkae7fce62e94a3d71, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-68', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.821326', 68, 'EXECUTED', '8:2fe4fd86aee44ba9943cc6cb48fa6b47', 'addForeignKeyConstraint baseTableName=person, constraintName=fkaod5nahntl9mkjg40fgpfo5n3, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-69', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.824907', 69, 'EXECUTED', '8:7f3b250b6f9ddc6ce308cbae5c2ad803', 'addForeignKeyConstraint baseTableName=imagegallery, constraintName=fkb4bca197e94a3d71, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-70', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.829096', 70, 'EXECUTED', '8:bf23ce232b2ff4a12b73d0e4c92cfb0d', 'addForeignKeyConstraint baseTableName=address, constraintName=fkbb979bf4e94a3d71, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-71', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.833945', 71, 'EXECUTED', '8:520ddf34a6ff073efa462593f17d062c', 'addForeignKeyConstraint baseTableName=webstructure, constraintName=fkbxuqdyptewrs24h0iy2yolld5, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-72', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.855153', 72, 'EXECUTED', '8:ea6a4ce162a4212dc6da1a97c53db5e0', 'addForeignKeyConstraint baseTableName=rating, constraintName=fkc815b19de94a3d71, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-73', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.863639', 73, 'EXECUTED', '8:b431c906fd0bfdf1173a5a7ffaba7339', 'addForeignKeyConstraint baseTableName=imagepart, constraintName=fkcbb4e7cee94a3d71, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-74', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.867844', 74, 'EXECUTED', '8:117f313dff3a65a4e26f24958722283d', 'addForeignKeyConstraint baseTableName=relation, constraintName=fkdef3f9fcac959b89, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-75', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.872683', 75, 'EXECUTED', '8:8a68529b41cf72f54755eb752c026ca8', 'addForeignKeyConstraint baseTableName=relation, constraintName=fkdef3f9fccfff80e, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-76', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.876772', 76, 'EXECUTED', '8:77be86ed8b921adf6ddd5aee4fd41ea8', 'addForeignKeyConstraint baseTableName=htmlpart, constraintName=fkebf39e1ee94a3d71, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-77', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.880618', 77, 'EXECUTED', '8:e324ce1640e3c7125534a1e278e8d4ae', 'addForeignKeyConstraint baseTableName=phonenumber, constraintName=fkef7fce37e94a3d71, referencedTableName=entity', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-78', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.884701', 78, 'EXECUTED', '8:6bcd6ec119c9c64b308e1c9bfd6aee0f', 'addForeignKeyConstraint baseTableName=relation, constraintName=fk2kg6f75t6f5jhdkkudgsattcc, referencedTableName=item', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-79', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.888828', 79, 'EXECUTED', '8:ef1e155695feecab9e2b7c6e1255caf2', 'createIndex indexName=relation_kind_index, tableName=relation', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-80', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.892133', 80, 'EXECUTED', '8:d75c7f1d279647e5bab2c645d00fdc84', 'createIndex indexName=relation_sub_entity_id_index, tableName=relation', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-81', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.895312', 81, 'EXECUTED', '8:176101ca0484888873e3a8e0bef3169f', 'createIndex indexName=relation_super_entity_id_index, tableName=relation', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-82', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.898738', 82, 'EXECUTED', '8:e6a37e6107ed0d63e9e9fc28b0a7384b', 'createIndex indexName=word_text_idx, tableName=word', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-83', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.903324', 83, 'EXECUTED', '8:22d9d499aa4a75917ded536cb8474d62', 'createIndex indexName=word_text_lower_idx, tableName=word', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-84', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.906567', 84, 'EXECUTED', '8:02612ef1cf538aa976216c95d434dbbe', 'createIndex indexName=property_entity_id_index, tableName=property', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-85', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.90967', 85, 'EXECUTED', '8:057a33062df7c0bfc85f9641e9dac332', 'createIndex indexName=property_key_index, tableName=property', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-86', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.913408', 86, 'EXECUTED', '8:291b97a3691722c3672d55a120fee41b', 'createIndex indexName=privilege_object_index, tableName=privilege', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-87', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.916629', 87, 'EXECUTED', '8:36ffae83733fc6c5ada3ed2d5a03080f', 'createIndex indexName=privilege_subject_index, tableName=privilege', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-88', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.919842', 88, 'EXECUTED', '8:e8bc32998069b3f8444b472a96ce5fcd', 'addForeignKeyConstraint baseTableName=entity, constraintName=fkb29de3e3165c5561, referencedTableName=item', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-89', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.923892', 89, 'EXECUTED', '8:c13d75d145708350abb1336c74550ab0', 'createSequence sequenceName=item_id_sequence', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-90', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.926891', 90, 'EXECUTED', '8:b1e0a68621e4b642c5288de91d25b972', 'createSequence sequenceName=log_id_sequence', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-91', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.929845', 91, 'EXECUTED', '8:559375c347ad36862cf6fce560fa16d8', 'createSequence sequenceName=privilege_id_sequence', '', NULL, '4.3.5', NULL, NULL, '2700789214'),
('baseline-92', 'jbm (generated)', 'database/2000-01-01-baseline.xml', '2022-09-09 07:19:49.933004', 92, 'EXECUTED', '8:0905503bb505bb28eb792334a8eaba8b', 'createSequence sequenceName=property_id_sequence', '', NULL, '4.3.5', NULL, NULL, '2700789214');

INSERT INTO "public"."databasechangeloglock" ("id", "locked", "lockgranted", "lockedby") VALUES
(1, 'f', NULL, NULL);

