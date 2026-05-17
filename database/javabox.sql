-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: May 17, 2026 at 03:22 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `javabox`
--

-- --------------------------------------------------------

--
-- Table structure for table `fullhouse_house_stats`
--

CREATE TABLE `fullhouse_house_stats` (
  `house_name` varchar(50) NOT NULL,
  `races_run` int(11) DEFAULT 0,
  `races_won` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `fullhouse_house_stats`
--

INSERT INTO `fullhouse_house_stats` (`house_name`, `races_run`, `races_won`) VALUES
('Cobblestone Keep', 1, 0),
('Gilded Cottage', 1, 0),
('Ironforge Mill', 1, 1),
('Shamrock Manor', 1, 0),
('The Emerald Pub', 1, 0),
('The Rusty Garrison', 1, 0),
('The Shelby Estate', 1, 0),
('Velvet Parlor', 1, 0);

-- --------------------------------------------------------

--
-- Table structure for table `fullhouse_player_stats`
--

CREATE TABLE `fullhouse_player_stats` (
  `player_id` int(11) NOT NULL,
  `bets_placed` int(11) DEFAULT 0,
  `bets_won` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `fullhouse_player_stats`
--

INSERT INTO `fullhouse_player_stats` (`player_id`, `bets_placed`, `bets_won`) VALUES
(1, 1, 0),
(2, 1, 0);

-- --------------------------------------------------------

--
-- Table structure for table `hangman_stats`
--

CREATE TABLE `hangman_stats` (
  `player_id` int(11) NOT NULL,
  `games_played` int(11) DEFAULT 0,
  `wins` int(11) DEFAULT 0,
  `current_streak` int(11) DEFAULT 0,
  `best_streak` int(11) DEFAULT 0,
  `total_pulls_survived` int(11) DEFAULT 0,
  `total_deaths` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `hangman_stats`
--

INSERT INTO `hangman_stats` (`player_id`, `games_played`, `wins`, `current_streak`, `best_streak`, `total_pulls_survived`, `total_deaths`) VALUES
(1, 1, 1, 1, 1, 6, 0);

-- --------------------------------------------------------

--
-- Table structure for table `knucklebones_stats`
--

CREATE TABLE `knucklebones_stats` (
  `player_id` int(11) NOT NULL,
  `games_played` int(11) DEFAULT 0,
  `wins` int(11) DEFAULT 0,
  `highest_score` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `knucklebones_stats`
--

INSERT INTO `knucklebones_stats` (`player_id`, `games_played`, `wins`, `highest_score`) VALUES
(1, 1, 1, 62),
(2, 1, 0, 23);

-- --------------------------------------------------------

--
-- Table structure for table `players`
--

CREATE TABLE `players` (
  `player_id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `players`
--

INSERT INTO `players` (`player_id`, `username`, `created_at`) VALUES
(1, 'Tester', '2026-05-17 00:28:48'),
(2, 'Tester1', '2026-05-17 00:44:21');

-- --------------------------------------------------------

--
-- Table structure for table `uttt_stats`
--

CREATE TABLE `uttt_stats` (
  `player_id` int(11) NOT NULL,
  `games_played` int(11) DEFAULT 0,
  `wins` int(11) DEFAULT 0,
  `draws` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `uttt_stats`
--

INSERT INTO `uttt_stats` (`player_id`, `games_played`, `wins`, `draws`) VALUES
(1, 1, 1, 0),
(2, 1, 0, 0);

-- --------------------------------------------------------

--
-- Table structure for table `windowswarm_stats`
--

CREATE TABLE `windowswarm_stats` (
  `player_id` int(11) NOT NULL,
  `highest_score` int(11) DEFAULT 0,
  `total_runs` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `windowswarm_stats`
--

INSERT INTO `windowswarm_stats` (`player_id`, `highest_score`, `total_runs`) VALUES
(1, 67, 1);

-- --------------------------------------------------------

--
-- Table structure for table `wordle_stats`
--

CREATE TABLE `wordle_stats` (
  `player_id` int(11) NOT NULL,
  `games_played` int(11) DEFAULT 0,
  `wins` int(11) DEFAULT 0,
  `current_streak` int(11) DEFAULT 0,
  `best_streak` int(11) DEFAULT 0,
  `dist_1` int(11) DEFAULT 0,
  `dist_2` int(11) DEFAULT 0,
  `dist_3` int(11) DEFAULT 0,
  `dist_4` int(11) DEFAULT 0,
  `dist_5` int(11) DEFAULT 0,
  `dist_6` int(11) DEFAULT 0,
  `dist_7` int(11) DEFAULT 0,
  `dist_8` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `wordle_stats`
--

INSERT INTO `wordle_stats` (`player_id`, `games_played`, `wins`, `current_streak`, `best_streak`, `dist_1`, `dist_2`, `dist_3`, `dist_4`, `dist_5`, `dist_6`, `dist_7`, `dist_8`) VALUES
(1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `fullhouse_house_stats`
--
ALTER TABLE `fullhouse_house_stats`
  ADD PRIMARY KEY (`house_name`);

--
-- Indexes for table `fullhouse_player_stats`
--
ALTER TABLE `fullhouse_player_stats`
  ADD PRIMARY KEY (`player_id`);

--
-- Indexes for table `hangman_stats`
--
ALTER TABLE `hangman_stats`
  ADD PRIMARY KEY (`player_id`);

--
-- Indexes for table `knucklebones_stats`
--
ALTER TABLE `knucklebones_stats`
  ADD PRIMARY KEY (`player_id`);

--
-- Indexes for table `players`
--
ALTER TABLE `players`
  ADD PRIMARY KEY (`player_id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- Indexes for table `uttt_stats`
--
ALTER TABLE `uttt_stats`
  ADD PRIMARY KEY (`player_id`);

--
-- Indexes for table `windowswarm_stats`
--
ALTER TABLE `windowswarm_stats`
  ADD PRIMARY KEY (`player_id`);

--
-- Indexes for table `wordle_stats`
--
ALTER TABLE `wordle_stats`
  ADD PRIMARY KEY (`player_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `players`
--
ALTER TABLE `players`
  MODIFY `player_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `fullhouse_player_stats`
--
ALTER TABLE `fullhouse_player_stats`
  ADD CONSTRAINT `fullhouse_player_stats_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`player_id`) ON DELETE CASCADE;

--
-- Constraints for table `hangman_stats`
--
ALTER TABLE `hangman_stats`
  ADD CONSTRAINT `hangman_stats_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`player_id`) ON DELETE CASCADE;

--
-- Constraints for table `knucklebones_stats`
--
ALTER TABLE `knucklebones_stats`
  ADD CONSTRAINT `knucklebones_stats_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`player_id`) ON DELETE CASCADE;

--
-- Constraints for table `uttt_stats`
--
ALTER TABLE `uttt_stats`
  ADD CONSTRAINT `uttt_stats_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`player_id`) ON DELETE CASCADE;

--
-- Constraints for table `windowswarm_stats`
--
ALTER TABLE `windowswarm_stats`
  ADD CONSTRAINT `windowswarm_stats_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`player_id`) ON DELETE CASCADE;

--
-- Constraints for table `wordle_stats`
--
ALTER TABLE `wordle_stats`
  ADD CONSTRAINT `wordle_stats_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`player_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
