-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: myself
-- ------------------------------------------------------
-- Server version	5.7.19-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `trial_2`
--

DROP TABLE IF EXISTS `trial_2`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trial_2` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `trial_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `cohort` char(20) COLLATE utf8_unicode_ci DEFAULT NULL,
  `observation` int(11) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `data` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `trial_id` (`trial_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `trial_2_ibfk_1` FOREIGN KEY (`trial_id`) REFERENCES `trials` (`id`),
  CONSTRAINT `trial_2_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trial_2`
--

LOCK TABLES `trial_2` WRITE;
/*!40000 ALTER TABLE `trial_2` DISABLE KEYS */;
INSERT INTO `trial_2` VALUES (1,1,1,'A (treatment)',1,'2017-10-14 07:00:00',200),(2,1,2,'A (treatment)',1,'2017-10-14 07:00:00',160),(3,1,3,'B (control)',1,'2017-10-14 07:00:00',125),(4,1,1,'A (treatment)',2,'2017-10-21 07:00:00',190),(5,1,2,'A (treatment)',2,'2017-10-21 07:00:00',159),(6,1,3,'B (control)',2,'2017-10-21 07:00:00',125),(7,1,1,'A (treatment)',3,'2017-10-28 07:00:00',180),(8,1,2,'A (treatment)',3,'2017-10-28 07:00:00',158),(9,1,3,'B (control)',3,'2017-10-28 07:00:00',125),(10,1,1,'A (treatment)',4,'2017-11-04 07:00:00',170),(11,1,2,'A (treatment)',4,'2017-11-04 07:00:00',157),(12,1,3,'B (control)',4,'2017-11-04 07:00:00',125),(13,1,1,'A (treatment)',5,'2017-11-11 08:00:00',160),(14,1,2,'A (treatment)',5,'2017-11-11 08:00:00',156),(15,1,3,'B (control)',5,'2017-11-11 08:00:00',125);
/*!40000 ALTER TABLE `trial_2` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-10-15 13:59:31
