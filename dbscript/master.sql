-- MySQL dump 10.13  Distrib 8.0.28, for macos11 (x86_64)
--
-- Host: localhost    Database: testdb
-- ------------------------------------------------------
-- Server version	8.0.28

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `eh_org_permission`
--

DROP TABLE IF EXISTS `eh_org_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eh_org_permission` (
  `id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `org_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `permission_id` varchar(50) NOT NULL,
  `add_who` varchar(45) DEFAULT NULL,
  `add_date` datetime DEFAULT NULL,
  `edit_who` varchar(45) DEFAULT NULL,
  `edit_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `eh_org_permission_uk` (`org_id`,`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eh_org_permission`
--

LOCK TABLES `eh_org_permission` WRITE;
/*!40000 ALTER TABLE `eh_org_permission` DISABLE KEYS */;
INSERT INTO `eh_org_permission` VALUES ('1562851229925675010','0','1562830840956747777','jessica','2022-08-25 13:15:44','jessica','2022-08-25 13:15:44'),('1562851229929869314','0','1562831233786871809','jessica','2022-08-25 13:15:44','jessica','2022-08-25 13:15:44'),('1562851229929869315','0','1562831351806197762','jessica','2022-08-25 13:15:44','jessica','2022-08-25 13:15:44'),('1562851229934063618','0','1562831550473601025','jessica','2022-08-25 13:15:44','jessica','2022-08-25 13:15:44'),('1565443884535721986','1','2001','jessica','2022-09-01 16:58:01','jessica','2022-09-01 16:58:01'),('1565443884565082113','1','2102','jessica','2022-09-01 16:58:01','jessica','2022-09-01 16:58:01'),('1565535136018116609','1565535068007477249','2001','jessica','2022-09-01 23:00:37','jessica','2022-09-01 23:00:37'),('1565535136026505218','1565535068007477249','2102','jessica','2022-09-01 23:00:37','jessica','2022-09-01 23:00:37'),('1565535161536262146','1565464895783219202','2001','jessica','2022-09-01 23:00:43','jessica','2022-09-01 23:00:43'),('1565537038294364161','1565535068007477249','1565535749107920898','jessica','2022-09-01 23:08:11','jessica','2022-09-01 23:08:11'),('1565537038298558466','1','1565535749107920898','jessica','2022-09-01 23:08:11','jessica','2022-09-01 23:08:11'),('1569818161274978305','1569818026319052801','2001','jessica','2022-09-13 18:39:50','jessica','2022-09-13 18:39:50'),('1570652529564389378','1565535068007477249','1570652411603783682','jessica','2022-09-16 01:55:19','jessica','2022-09-16 01:55:19');
/*!40000 ALTER TABLE `eh_org_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eh_organization`
--

DROP TABLE IF EXISTS `eh_organization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eh_organization` (
  `id` varchar(50) NOT NULL,
  `code` varchar(100) NOT NULL,
  `name` varchar(500) NOT NULL,
  `parent_id` varchar(50) DEFAULT NULL,
  `address2` varchar(500) DEFAULT NULL,
  `contact1` varchar(100) DEFAULT NULL,
  `contact2` varchar(100) DEFAULT NULL,
  `address1` varchar(500) DEFAULT NULL,
  `db_name` varchar(45) DEFAULT NULL,
  `add_who` varchar(45) DEFAULT NULL,
  `add_date` datetime DEFAULT NULL,
  `edit_who` varchar(45) DEFAULT NULL,
  `edit_date` datetime DEFAULT NULL,
  `version` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `eh_organization_code_uindex` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eh_organization`
--

LOCK TABLES `eh_organization` WRITE;
/*!40000 ALTER TABLE `eh_organization` DISABLE KEYS */;
INSERT INTO `eh_organization` VALUES ('0','ALL_ORG','All Organizations (System Level)',NULL,NULL,'','',NULL,NULL,NULL,NULL,'jessica','2022-09-20 12:04:36',10),('1','HQ','总部','0',NULL,NULL,NULL,'HQ',NULL,'jessica','2022-08-08 22:20:09','jessica','2022-09-19 18:47:13',23),('1565464895783219202','wdwdw','北京分公司','1',NULL,NULL,NULL,NULL,NULL,'jessica','2022-09-01 18:21:31','jessica','2022-09-20 13:47:58',11),('1565535068007477249','SH','上海分公司','1',NULL,NULL,NULL,NULL,NULL,'jessica','2022-09-01 23:00:21','jessica','2022-09-16 01:55:19',7),('1569818026319052801','FENGTAI','丰台分公司','1565464895783219202',NULL,NULL,NULL,NULL,NULL,'jessica','2022-09-13 18:39:18','jessica','2022-09-13 18:39:50',2);
/*!40000 ALTER TABLE `eh_organization` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eh_permission`
--

DROP TABLE IF EXISTS `eh_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eh_permission` (
  `id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `module_id` varchar(45) DEFAULT NULL,
  `authority` varchar(50) NOT NULL,
  `type` varchar(10) NOT NULL COMMENT 'P: Permission D: Directory',
  `display_name` varchar(500) DEFAULT NULL,
  `parent_id` varchar(50) NOT NULL,
  `add_who` varchar(45) DEFAULT NULL,
  `add_date` datetime DEFAULT NULL,
  `edit_who` varchar(45) DEFAULT NULL,
  `edit_date` datetime DEFAULT NULL,
  `version` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eh_permission`
--

LOCK TABLES `eh_permission` WRITE;
/*!40000 ALTER TABLE `eh_permission` DISABLE KEYS */;
INSERT INTO `eh_permission` VALUES ('0',NULL,'','D','All Permissions','-1',NULL,NULL,'jessica','2022-08-25 13:25:22',3),('1','SYSTEM','','D','SYSTEM','0','jessica','2022-08-25 11:47:45','jessica','2022-09-16 16:59:43',2),('1562830576862396417','SYSTEM','','D','SECURITY','1','jessica','2022-08-25 11:53:40','jessica','2022-08-25 11:53:40',1),('1562830840956747777','SYSTEM','SECURITY_USER','P','USER','1562830576862396417','jessica','2022-08-25 11:54:43','jessica','2022-09-19 09:08:27',2),('1562831233786871809','SYSTEM','SECURITY_ORG','P','ORGANIZATION','1562830576862396417','jessica','2022-08-25 11:56:17','jessica','2022-08-25 11:56:17',1),('1562831351806197762','SYSTEM','SECURITY_ROLE','P','ROLE','1562830576862396417','jessica','2022-08-25 11:56:45','jessica','2022-08-25 11:56:45',1),('1562831550473601025','SYSTEM','SECURITY_PERMISSION','P','PERMISSION','1562830576862396417','jessica','2022-08-25 11:57:32','jessica','2022-08-25 11:57:32',1),('1565535749107920898','WMS','PICKDETAIL','P','PICKDETAIL','21','jessica','2022-09-01 23:03:03','jessica','2022-09-01 23:08:11',2),('1570652411603783682','TMS','EEE','P','wdwd','3','jessica','2022-09-16 01:54:51','jessica','2022-09-16 01:54:51',1),('2','WMS','','D','WMS','0','jessica','2022-08-25 11:48:11','jessica','2022-08-25 11:48:11',1),('20','WMS','','D','INBOUND','2',NULL,NULL,NULL,NULL,0),('2001','WMS','ASN','P','ASN','20',NULL,NULL,'jessica','2022-08-24 18:15:42',9),('21','WMS','','D','OUTBOUND','2',NULL,NULL,NULL,NULL,0),('2102','WMS','SO_WRITE','P','SO','21',NULL,NULL,'jessica','2022-09-01 23:02:32',10),('3','TMS','','D','TMS','0','jessica','2022-08-25 11:48:23','jessica','2022-08-25 11:48:23',1);
/*!40000 ALTER TABLE `eh_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eh_role`
--

DROP TABLE IF EXISTS `eh_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eh_role` (
  `id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `org_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `role_name` varchar(50) NOT NULL,
  `display_name` varchar(500) DEFAULT NULL,
  `add_who` varchar(45) DEFAULT NULL,
  `add_date` datetime DEFAULT NULL,
  `edit_who` varchar(45) DEFAULT NULL,
  `edit_date` datetime DEFAULT NULL,
  `version` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eh_role`
--

LOCK TABLES `eh_role` WRITE;
/*!40000 ALTER TABLE `eh_role` DISABLE KEYS */;
INSERT INTO `eh_role` VALUES ('0','1','ADMIN','管理员',NULL,NULL,'jessica','2022-09-01 13:23:08',7),('1','1','RECEIPTUSER','收货用户',NULL,NULL,NULL,NULL,0),('1536754109867098113','1','SOUSER','出库用户',NULL,NULL,NULL,NULL,0),('1539814132042178561','1','administrator','administrator','1515880093879160833','2022-06-22 23:34:32','1515880093879160833','2022-06-22 23:35:14',0),('1565537536594456578','1565535068007477249','RECEIVER','收货员','jessica','2022-09-01 23:10:10','jessica','2022-09-01 23:10:54',2),('1567576314225930242','0','SYS_ADMIN','超级管理员','jessica','2022-09-07 14:11:32','jessica','2022-09-19 19:04:32',36),('1569818209618526210','1569818026319052801','EEE','EEE','jessica','2022-09-13 18:40:02','jessica','2022-09-16 02:01:13',2),('1569818448580608001','1565464895783219202','WDWD','WWW','jessica','2022-09-13 18:40:59','jessica','2022-09-20 13:49:02',2),('1570652468080087041','1565535068007477249','WDWD','WDWWWW','jessica','2022-09-16 01:55:04','jessica','2022-09-16 01:55:46',2);
/*!40000 ALTER TABLE `eh_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eh_role_permission`
--

DROP TABLE IF EXISTS `eh_role_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eh_role_permission` (
  `id` varchar(50) NOT NULL,
  `role_id` varchar(50) NOT NULL,
  `permission_id` varchar(50) NOT NULL,
  `add_who` varchar(45) DEFAULT NULL,
  `add_date` datetime DEFAULT NULL,
  `edit_who` varchar(45) DEFAULT NULL,
  `edit_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eh_role_permission`
--

LOCK TABLES `eh_role_permission` WRITE;
/*!40000 ALTER TABLE `eh_role_permission` DISABLE KEYS */;
INSERT INTO `eh_role_permission` VALUES ('1564839402679590914','1564834676965675010','2001','jessica','2022-08-31 00:56:02','jessica','2022-08-31 00:56:02'),('1565389807985647617','0','2001','jessica','2022-09-01 13:23:08','jessica','2022-09-01 13:23:08'),('1565389807989841922','0','2102','jessica','2022-09-01 13:23:08','jessica','2022-09-01 13:23:08'),('1565537724411195393','1565537536594456578','2001','jessica','2022-09-01 23:10:54','jessica','2022-09-01 23:10:54'),('1570652642852540417','1570652468080087041','1570652411603783682','jessica','2022-09-16 01:55:46','jessica','2022-09-16 01:55:46'),('1570654015606296578','1569818209618526210','2001','jessica','2022-09-16 02:01:13','jessica','2022-09-16 02:01:13'),('1571998703294615553','1567576314225930242','1562830840956747777','jessica','2022-09-19 19:04:32','jessica','2022-09-19 19:04:32'),('1571998703298809858','1567576314225930242','1562831233786871809','jessica','2022-09-19 19:04:32','jessica','2022-09-19 19:04:32'),('1571998703307198465','1567576314225930242','1562831351806197762','jessica','2022-09-19 19:04:32','jessica','2022-09-19 19:04:32'),('1571998703311392770','1567576314225930242','1562831550473601025','jessica','2022-09-19 19:04:32','jessica','2022-09-19 19:04:32'),('1572281692540895234','1569818448580608001','2001','jessica','2022-09-20 13:49:02','jessica','2022-09-20 13:49:02'),('2','1','2102',NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `eh_role_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eh_user`
--

DROP TABLE IF EXISTS `eh_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eh_user` (
  `id` varchar(50) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(500) NOT NULL,
  `enabled` tinyint(1) NOT NULL,
  `domain_username` varchar(50) DEFAULT NULL,
  `auth_type` varchar(10) NOT NULL,
  `add_date` datetime DEFAULT NULL,
  `add_who` varchar(45) DEFAULT NULL,
  `edit_date` datetime DEFAULT NULL,
  `edit_who` varchar(45) DEFAULT NULL,
  `account_locked` tinyint(1) DEFAULT '0',
  `password_changed_time` datetime DEFAULT NULL,
  `first_name` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `last_name` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `version` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eh_user`
--

LOCK TABLES `eh_user` WRITE;
/*!40000 ALTER TABLE `eh_user` DISABLE KEYS */;
INSERT INTO `eh_user` VALUES ('1','admin','$2a$10$WXVrcGImpa/N/FW5dBzTt..NNhXcQ1OXE58Dr0jQDNM6yEWbDopG.',1,'','BASIC',NULL,NULL,NULL,'',NULL,0,NULL,NULL,0),('1515880093879160833','jessica','$2a$10$SMKt80BE/xDFYKtQbkhkTeeWKqmE.yk/9Qrl9x8LpUB50ghTM0akS',1,'','BASIC',NULL,NULL,'2022-08-15 21:05:50','jessica',0,0,'Jessica','Li',45),('1515887611598606337','john','$2a$10$jzoJ4YZqgaPQh0h7yWmuiewuBqLwXJyCPhKKIB03Fuwm95tt/FoUa',1,'CN=john,CN=Users,DC=infor,DC=com','LDAP',NULL,NULL,'2022-08-25 14:14:18','jessica',0,0,'john','w',1),('1539081947215675394','figo','$2a$10$hBGPN.4omZS9XYnFMNp1H.xi4Obo.9obZ24WiWrn48Fc4ofEE5DU.',1,'','BASIC',NULL,NULL,'2022-09-01 18:22:49','jessica',0,0,'figo','cheng',1),('1539262322114076673','sean','$2a$10$ksfvjx.Wfn6gcxNVyArLSOO5dJrtj0iEWBvwHxepL8woZPceosBLu',1,NULL,'BASIC',NULL,NULL,NULL,NULL,0,0,'Sean','Cheng',0),('1554574938805960706','jack','$2a$10$Y5bng5zsu3QAxslWydWYZ.Zv/A90GHxUgKT.x5Lbc.twyrWNxyK5m',1,NULL,'BASIC','2022-08-02 17:08:43','jessica','2022-08-02 17:08:43','jessica',0,0,'Jack','Li',1);
/*!40000 ALTER TABLE `eh_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eh_user_role`
--

DROP TABLE IF EXISTS `eh_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eh_user_role` (
  `id` varchar(50) NOT NULL,
  `user_id` varchar(50) NOT NULL,
  `role_id` varchar(50) NOT NULL,
  `add_who` varchar(45) DEFAULT NULL,
  `add_date` datetime DEFAULT NULL,
  `edit_who` varchar(45) DEFAULT NULL,
  `edit_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eh_user_role`
--

LOCK TABLES `eh_user_role` WRITE;
/*!40000 ALTER TABLE `eh_user_role` DISABLE KEYS */;
INSERT INTO `eh_user_role` VALUES ('1539078907439988738','1','1536754109867098113',NULL,NULL,NULL,NULL),('1539080822810853378','1','0',NULL,NULL,NULL,NULL),('1539080822819241986','1','1',NULL,NULL,NULL,NULL),('1562867592585056258','1515887611598606337','1536754109867098113','jessica','2022-08-25 14:20:45','jessica','2022-08-25 14:20:45'),('1565538471307685889','1515887611598606337','1565537536594456578','jessica','2022-09-01 23:13:53','jessica','2022-09-01 23:13:53'),('1565539252823961601','1539081947215675394','1565537536594456578','jessica','2022-09-01 23:16:59','jessica','2022-09-01 23:16:59'),('1565539278723788802','1539081947215675394','1','jessica','2022-09-01 23:17:05','jessica','2022-09-01 23:17:05'),('1568297969734692866','1515880093879160833','1567576314225930242','jessica','2022-09-09 13:59:08','jessica','2022-09-09 13:59:08'),('1569756212973580290','1515887611598606337','1','jessica','2022-09-13 14:33:40','jessica','2022-09-13 14:33:40'),('1569805963181965313','1515880093879160833','1565537536594456578','jessica','2022-09-13 17:51:22','jessica','2022-09-13 17:51:22'),('1569818603543363586','1515880093879160833','1569818209618526210','jessica','2022-09-13 18:41:36','jessica','2022-09-13 18:41:36'),('1570652681897316354','1515880093879160833','1570652468080087041','jessica','2022-09-16 01:55:55','jessica','2022-09-16 01:55:55'),('1570781863688081410','1515887611598606337','1569818209618526210','jessica','2022-09-16 10:29:15','jessica','2022-09-16 10:29:15'),('1572281507953770498','1515880093879160833','1569818448580608001','jessica','2022-09-20 13:48:18','jessica','2022-09-20 13:48:18');
/*!40000 ALTER TABLE `eh_user_role` ENABLE KEYS */;
UNLOCK TABLES;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- -- Dump completed on 2022-09-21  9:31:49
-- mysqldump -u root -p hq < /Users/johnw/WorkFolder/EnhantecProducts/enhantec-platform/dbscript/master.sql
-- mysqldump -u root -p sh < /Users/johnw/WorkFolder/EnhantecProducts/enhantec-platform/dbscript/master.sql
-- mysqldump -u root -p bj < /Users/johnw/WorkFolder/EnhantecProducts/enhantec-platform/dbscript/master.sql
-- mysqldump -u root -p bj_ft < /Users/johnw/WorkFolder/EnhantecProducts/enhantec-platform/dbscript/master.sql