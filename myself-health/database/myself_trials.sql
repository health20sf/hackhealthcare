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
-- Table structure for table `trials`
--

DROP TABLE IF EXISTS `trials`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trials` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` char(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `category_id` int(11) NOT NULL,
  `summary` text COLLATE utf8_unicode_ci,
  `min_num_ppl` int(11) NOT NULL,
  `max_num_ppl` int(11) DEFAULT NULL,
  `duration_days` int(11) DEFAULT NULL,
  `treatment` text COLLATE utf8_unicode_ci,
  `control` text COLLATE utf8_unicode_ci,
  `dose` int(11) DEFAULT NULL,
  `dose_units` char(20) COLLATE utf8_unicode_ci DEFAULT NULL,
  `kpi` text COLLATE utf8_unicode_ci,
  `kpi_frequency` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `category_id` (`category_id`),
  CONSTRAINT `trials_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trials`
--

LOCK TABLES `trials` WRITE;
/*!40000 ALTER TABLE `trials` DISABLE KEYS */;
INSERT INTO `trials` VALUES (1,'Ketogenic Diet for Eczema',2,'keto diet for eczema reduction',10,99,56,'ketogenic diet of < 50 g carbs/day','Western diet',50,'g','questionnaire','weekly'),(2,'Vegetarian diet for weight loss',2,'Vegetarian, non-vegan diet for weight loss',1,999,56,'Apply a vegetarian diet for eight weeks','Western diet',0,'NA','questionnaire','weekly'),(3,'Experimental squat regimen',4,'Try 3x5 squats for strength gain',1,999,90,'Once-weekly set of 3x3 squats, no more than 10% increase per session, to see how improvement tracks','3x10',0,'NA','fitness log','weekly'),(4,'Blind trial of CoQ10 to address MS',1,'Compare 500 mg of CoQ10 vs placebo to address MS symptoms',1,99,30,'Take 500 mg of CoQ10 per day','placebo',500,'mg','questionnaire','weekly');
/*!40000 ALTER TABLE `trials` ENABLE KEYS */;
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
