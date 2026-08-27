-- =====================================================
-- GlobalTrade Logistics — Schema (clean rebuild)
-- MySQL 8.x · Payara 6 · Jakarta EE 10 · EclipseLink
--
-- Follows the MySQL Workbench ERD export exactly:
--   * shipments.ships_id keeps its name (NOT ship_id)
--   * statuses/roles are VARCHAR(45), not MySQL ENUM
--   * shipment_containers keeps its surrogate AUTO_INCREMENT id
--
-- Differences from the raw export, all deliberate:
--   1. shipments.created_at added (entity requires it)
--   2. NOT NULL on fields the code never leaves null
--   3. UNIQUE on users.email, containers.container_number, ports.code
--   4. UNIQUE on the (shipments_id, containers_id) pair
--   5. Indexes on containers.status and shipments(status, eta)
--   6. utf8mb4 instead of utf8
--
-- >>> WARNING: this DROPS the database. If you already have
-- >>> data, run migration_01_globaltrade.sql instead.
--
--   mysql -u root -p < schema_globaltrade.sql
-- =====================================================

DROP DATABASE IF EXISTS `globaltrade`;
CREATE DATABASE `globaltrade`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
USE `globaltrade`;

-- -----------------------------------------------------
-- ports — reference data, seeded once
-- -----------------------------------------------------
CREATE TABLE `ports` (
                         `id`      INT         NOT NULL AUTO_INCREMENT,
                         `code`    VARCHAR(45) NOT NULL,
                         `name`    VARCHAR(45) NOT NULL,
                         `country` VARCHAR(45) NOT NULL,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `uq_ports_code` (`code`)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- ships
-- status: AT_PORT | IN_TRANSIT | MAINTENANCE
--   VARCHAR + @Enumerated(EnumType.STRING) rather than a MySQL
--   ENUM. Your ERD chose this and it is the better call:
--   adding an enum constant in Java needs no DDL change, and
--   EclipseLink maps it cleanly. Keep it.
-- current_port_id nullable: a ship may be at sea.
--   Written ONLY by ShipmentTimerBean.
-- -----------------------------------------------------
CREATE TABLE `ships` (
                         `id`              INT         NOT NULL AUTO_INCREMENT,
                         `name`            VARCHAR(45) NOT NULL,
                         `capacity`        INT         NOT NULL,
                         `status`          VARCHAR(45) NOT NULL,
                         `current_port_id` INT         NULL,
                         PRIMARY KEY (`id`),
                         INDEX `fk_ships_ports_idx` (`current_port_id` ASC),
                         CONSTRAINT `fk_ships_ports`
                             FOREIGN KEY (`current_port_id`) REFERENCES `ports` (`id`)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- users
-- email  = the JAAS caller principal name. UNIQUE is required:
--          the login module and UserAccountBean both use
--          getSingleResult() on it.
-- password = bcrypt hash, $2a$ prefix (jBCrypt rejects $2b$).
-- role   = CUSTOMER | COORDINATOR | ADMIN
-- -----------------------------------------------------
CREATE TABLE `users` (
                         `id`       INT          NOT NULL AUTO_INCREMENT,
                         `name`     VARCHAR(100) NOT NULL,
                         `email`    VARCHAR(200) NOT NULL,
                         `password` VARCHAR(256) NOT NULL,
                         `role`     VARCHAR(45)  NOT NULL,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `uq_users_email` (`email`)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- containers
-- status: AVAILABLE | RESERVED | IN_TRANSIT | UNAVAILABLE
-- ix_containers_status supports bookShipment()'s hot query.
-- -----------------------------------------------------
CREATE TABLE `containers` (
                              `id`               INT         NOT NULL AUTO_INCREMENT,
                              `container_number` VARCHAR(45) NOT NULL,
                              `status`           VARCHAR(45) NOT NULL,
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uq_containers_number` (`container_number`),
                              INDEX `ix_containers_status` (`status`)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- shipments
-- ships_id  — ERD name kept. Map explicitly in the entity:
--             @ManyToOne @JoinColumn(name = "ships_id")
-- status    — PENDING|CONFIRMED|IN_TRANSIT|DELAYED|DELIVERED
-- eta       — nullable until a coordinator confirms
-- created_at— ADDED vs the ERD export; entity requires it
-- ix_shipments_status_eta — serves the declarative timer's
--             15-minute query; EXPLAIN it before/after for the
--             performance section.
-- -----------------------------------------------------
CREATE TABLE `shipments` (
                             `id`                  INT         NOT NULL AUTO_INCREMENT,
                             `customer_id`         INT         NOT NULL,
                             `origin_port_id`      INT         NOT NULL,
                             `destination_port_id` INT         NOT NULL,
                             `ships_id`            INT         NULL,
                             `status`              VARCHAR(45) NOT NULL,
                             `eta`                 DATE        NULL,
                             `estimated_cost`      DOUBLE      NULL,
                             `created_at`          DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             PRIMARY KEY (`id`),
                             INDEX `fk_shipments_users1_idx`  (`customer_id` ASC),
                             INDEX `fk_shipments_ports1_idx`  (`origin_port_id` ASC),
                             INDEX `fk_shipments_ports2_idx`  (`destination_port_id` ASC),
                             INDEX `fk_shipments_ships1_idx`  (`ships_id` ASC),
                             INDEX `ix_shipments_status_eta`  (`status`, `eta`),
                             CONSTRAINT `fk_shipments_users1`
                                 FOREIGN KEY (`customer_id`) REFERENCES `users` (`id`),
                             CONSTRAINT `fk_shipments_ports1`
                                 FOREIGN KEY (`origin_port_id`) REFERENCES `ports` (`id`),
                             CONSTRAINT `fk_shipments_ports2`
                                 FOREIGN KEY (`destination_port_id`) REFERENCES `ports` (`id`),
                             CONSTRAINT `fk_shipments_ships1`
                                 FOREIGN KEY (`ships_id`) REFERENCES `ships` (`id`)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- shipment_containers — join table, no entity class
--
-- `id` MUST stay AUTO_INCREMENT: JPA's @ManyToMany inserts
-- only the two FK columns, so a non-generated PK fails every
-- insert. Your ERD already had this right.
--
-- Map on Shipment.containers:
--   @ManyToMany
--   @JoinTable(name = "shipment_containers",
--     joinColumns        = @JoinColumn(name = "shipments_id"),
--     inverseJoinColumns = @JoinColumn(name = "containers_id"))
-- -----------------------------------------------------
CREATE TABLE `shipment_containers` (
                                       `id`            INT NOT NULL AUTO_INCREMENT,
                                       `containers_id` INT NOT NULL,
                                       `shipments_id`  INT NOT NULL,
                                       PRIMARY KEY (`id`),
                                       UNIQUE KEY `uq_shipment_container` (`shipments_id`, `containers_id`),
                                       INDEX `fk_shipment_containers_containers1_idx` (`containers_id` ASC),
                                       INDEX `fk_shipment_containers_shipments1_idx`  (`shipments_id` ASC),
                                       CONSTRAINT `fk_shipment_containers_containers1`
                                           FOREIGN KEY (`containers_id`) REFERENCES `containers` (`id`),
                                       CONSTRAINT `fk_shipment_containers_shipments1`
                                           FOREIGN KEY (`shipments_id`) REFERENCES `shipments` (`id`)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- audit_logs
-- user_id NULL = system/timer action.
-- Written by AuditLogWriterBean under REQUIRES_NEW, so rows
-- persist even when the caller's transaction rolls back.
-- -----------------------------------------------------
CREATE TABLE `audit_logs` (
                              `id`          INT          NOT NULL AUTO_INCREMENT,
                              `user_id`     INT          NULL,
                              `action`      VARCHAR(200) NOT NULL,
                              `entity_type` VARCHAR(45)  NOT NULL,
                              `entity_id`   INT          NULL,
                              `details`     TEXT         NULL,
                              `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`),
                              INDEX `fk_audit_logs_users1_idx` (`user_id` ASC),
                              INDEX `ix_audit_created` (`created_at`),
                              CONSTRAINT `fk_audit_logs_users1`
                                  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- application DB user
-- -----------------------------------------------------
CREATE USER IF NOT EXISTS 'globaltrade'@'localhost' IDENTIFIED BY 'globaltrade';
GRANT SELECT, INSERT, UPDATE, DELETE ON `globaltrade`.* TO 'globaltrade'@'localhost';
FLUSH PRIVILEGES;

-- =====================================================
-- GlobalTrade Logistics — Seed Data
-- Run AFTER schema_globaltrade.sql (clean rebuild)
--        OR AFTER migration_01_globaltrade.sql (existing DB)
--
--   mysql -u root -p < seed_data.sql
--
-- CHANGES vs. the original seed:
--   1. bcrypt prefix $2b$ -> $2a$
--      jBCrypt throws IllegalArgumentException: Invalid salt
--      revision on $2b$. It never returns false -- it throws.
--      $2a$ and $2b$ are byte-identical for passwords under
--      72 bytes, so the swap is safe. Password is "123456".
--   2. created_at added to shipments (new column)
--   3. ships (2 more) so every seeded origin port has a ship
--      AT_PORT for updateStatus() to assign
--   4. containers 12 -> 20; 11 now AVAILABLE
--
-- ships_id is UNCHANGED -- it matches the ERD.
-- =====================================================

USE `globaltrade`;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `audit_logs`;
TRUNCATE TABLE `shipment_containers`;
TRUNCATE TABLE `shipments`;
TRUNCATE TABLE `containers`;
TRUNCATE TABLE `ships`;
TRUNCATE TABLE `users`;
TRUNCATE TABLE `ports`;
SET FOREIGN_KEY_CHECKS = 1;

-- -----------------------------------------------------
-- ports
-- -----------------------------------------------------
INSERT INTO `ports` (`id`, `code`, `name`, `country`) VALUES
                                                          (1, 'LKCMB', 'Colombo',   'Sri Lanka'),
                                                          (2, 'NLRTM', 'Rotterdam', 'Netherlands'),
                                                          (3, 'SGSIN', 'Singapore', 'Singapore'),
                                                          (4, 'USNYC', 'New York',  'United States'),
                                                          (5, 'AEJEA', 'Jebel Ali', 'United Arab Emirates');

-- -----------------------------------------------------
-- ships
-- current_port_id is the field ShipmentTimerBean updates.
--
-- Ships 4 and 5 are new. ShipmentOperationsBean.updateStatus()
-- assigns "a ship AT_PORT at the origin port" when moving a
-- shipment to IN_TRANSIT. With only ships 1-3, origins at
-- ports 3, 4 and 5 had no candidate and the call would NPE.
-- Port 5 (Jebel Ali) still has none -- deliberately, so the
-- "no ship available" branch stays reachable and testable.
-- -----------------------------------------------------
INSERT INTO `ships` (`id`, `name`, `capacity`, `status`, `current_port_id`) VALUES
                                                                                (1, 'MV Trade Voyager',    5000, 'AT_PORT',    1),
                                                                                (2, 'MV Ocean Pioneer',    4200, 'IN_TRANSIT', 3),
                                                                                (3, 'MV Atlantic Carrier', 6000, 'AT_PORT',    2),
                                                                                (4, 'MV Indian Star',      3800, 'AT_PORT',    3),
                                                                                (5, 'MV Gulf Mariner',     4500, 'AT_PORT',    4);

-- -----------------------------------------------------
-- users — all share the password "123456" (bcrypt cost 10)
-- Demo only. Never seed real passwords this way.
-- -----------------------------------------------------
INSERT INTO `users` (`id`, `name`, `email`, `password`, `role`) VALUES
                                                                    (1, 'ABC Electronics',     'ops@abc-electronics.com',     '$2a$10$2VjPBFJiaikJQSExgJeEqeMC68TwR14/jDuUzL4NW3vNsJAwfNxhK', 'CUSTOMER'),
                                                                    (2, 'Global Textiles Ltd', 'shipping@globaltextiles.com', '$2a$10$2VjPBFJiaikJQSExgJeEqeMC68TwR14/jDuUzL4NW3vNsJAwfNxhK', 'CUSTOMER'),
                                                                    (3, 'Nadia Perera',        'nadia@globaltrade.com',       '$2a$10$2VjPBFJiaikJQSExgJeEqeMC68TwR14/jDuUzL4NW3vNsJAwfNxhK', 'COORDINATOR'),
                                                                    (4, 'Kasun Silva',         'kasun@globaltrade.com',       '$2a$10$2VjPBFJiaikJQSExgJeEqeMC68TwR14/jDuUzL4NW3vNsJAwfNxhK', 'COORDINATOR'),
                                                                    (5, 'Admin User',          'admin@globaltrade.com',       '$2a$10$2VjPBFJiaikJQSExgJeEqeMC68TwR14/jDuUzL4NW3vNsJAwfNxhK', 'ADMIN'),
                                                                    (6, 'Priya Fernando',      'priya@techimports.com',       '$2a$10$2VjPBFJiaikJQSExgJeEqeMC68TwR14/jDuUzL4NW3vNsJAwfNxhK', 'CUSTOMER');

-- -----------------------------------------------------
-- containers
-- 1-5   IN_TRANSIT  (shipments 1, 2)
-- 6,7,9 RESERVED    (shipments 3, 5)
-- 10    UNAVAILABLE (maintenance)
-- 8, 11-20 AVAILABLE  -> 11 free
--
-- 11 free lets you book repeatedly AND still trigger
-- NoContainerAvailableException by requesting 12+.
-- The original seed left only 3 free, so every demo after
-- the first booking failed for the wrong reason.
-- -----------------------------------------------------
INSERT INTO `containers` (`id`, `container_number`, `status`) VALUES
                                                                  (1,  'MSCU1000011', 'IN_TRANSIT'),
                                                                  (2,  'MSCU1000022', 'IN_TRANSIT'),
                                                                  (3,  'MSCU1000033', 'IN_TRANSIT'),
                                                                  (4,  'MSCU1000044', 'IN_TRANSIT'),
                                                                  (5,  'MSCU1000055', 'IN_TRANSIT'),
                                                                  (6,  'MSCU1000066', 'RESERVED'),
                                                                  (7,  'MSCU1000077', 'RESERVED'),
                                                                  (8,  'MSCU1000088', 'AVAILABLE'),
                                                                  (9,  'MSCU1000099', 'RESERVED'),
                                                                  (10, 'MSCU1000100', 'UNAVAILABLE'),
                                                                  (11, 'MSCU1000111', 'AVAILABLE'),
                                                                  (12, 'MSCU1000122', 'AVAILABLE'),
                                                                  (13, 'MSCU1000133', 'AVAILABLE'),
                                                                  (14, 'MSCU1000144', 'AVAILABLE'),
                                                                  (15, 'MSCU1000155', 'AVAILABLE'),
                                                                  (16, 'MSCU1000166', 'AVAILABLE'),
                                                                  (17, 'MSCU1000177', 'AVAILABLE'),
                                                                  (18, 'MSCU1000188', 'AVAILABLE'),
                                                                  (19, 'MSCU1000199', 'AVAILABLE'),
                                                                  (20, 'MSCU1000200', 'AVAILABLE');

-- -----------------------------------------------------
-- shipments
-- estimated_cost = 1000.00 per container
-- Column is ships_id, matching the ERD.
-- -----------------------------------------------------
INSERT INTO `shipments`
(`id`, `customer_id`, `origin_port_id`, `destination_port_id`, `ships_id`, `status`, `eta`, `estimated_cost`, `created_at`) VALUES
                                                                                                                                (1, 1, 1, 2, 1,    'IN_TRANSIT', '2026-09-15', 3000.00, '2026-08-10 09:15:00'),
                                                                                                                                (2, 2, 3, 4, 2,    'DELAYED',    '2026-09-25', 2000.00, '2026-08-12 10:05:00'),
                                                                                                                                (3, 1, 2, 5, NULL, 'PENDING',    NULL,         2000.00, '2026-08-20 16:45:00'),
                                                                                                                                (4, 6, 1, 3, 3,    'DELIVERED',  '2026-08-01', 1000.00, '2026-07-15 08:30:00'),
                                                                                                                                (5, 2, 4, 1, NULL, 'CONFIRMED',  '2026-10-05', 1000.00, '2026-08-22 11:20:00');

-- -----------------------------------------------------
-- shipment_containers — `id` is AUTO_INCREMENT, so omitted
-- -----------------------------------------------------
INSERT INTO `shipment_containers` (`shipments_id`, `containers_id`) VALUES
                                                                        (1, 1), (1, 2), (1, 3),
                                                                        (2, 4), (2, 5),
                                                                        (3, 6), (3, 7),
                                                                        (4, 8),
                                                                        (5, 9);

-- -----------------------------------------------------
-- audit_logs — user_id NULL simulates timer-generated rows
-- -----------------------------------------------------
INSERT INTO `audit_logs` (`user_id`, `action`, `entity_type`, `entity_id`, `details`, `created_at`) VALUES
                                                                                                        (1,    'CREATE_SHIPMENT',        'Shipment', 1, 'Booking submitted for 3 containers, Colombo to Rotterdam',      '2026-08-10 09:15:00'),
                                                                                                        (3,    'UPDATE_SHIPMENT_STATUS', 'Shipment', 2, 'Status changed to DELAYED due to port congestion at Singapore', '2026-08-15 14:32:00'),
                                                                                                        (NULL, 'TIMER_STATUS_UPDATE',    'Shipment', 1, 'Auto-advanced ETA via scheduled timer',                         '2026-08-18 00:00:00'),
                                                                                                        (5,    'APPROVE_ACCOUNT',        'User',     6, 'New customer account approved',                                 '2026-08-05 11:00:00'),
                                                                                                        (1,    'CREATE_SHIPMENT',        'Shipment', 3, 'Booking submitted for 2 containers, Rotterdam to Jebel Ali',    '2026-08-20 16:45:00'),
                                                                                                        (NULL, 'TIMER_STATUS_UPDATE',    'Ship',     2, 'Auto-updated ship location during scheduled timer run',         '2026-08-21 00:00:00');

-- -----------------------------------------------------
-- VERIFY — expect 5 / 5 / 6 / 20 / 5 / 9 / 6, and 11 AVAILABLE
-- -----------------------------------------------------
-- SELECT 'ports' t, COUNT(*) n FROM ports
-- UNION ALL SELECT 'ships', COUNT(*) FROM ships
-- UNION ALL SELECT 'users', COUNT(*) FROM users
-- UNION ALL SELECT 'containers', COUNT(*) FROM containers
-- UNION ALL SELECT 'shipments', COUNT(*) FROM shipments
-- UNION ALL SELECT 'sc_join', COUNT(*) FROM shipment_containers
-- UNION ALL SELECT 'audit_logs', COUNT(*) FROM audit_logs
-- UNION ALL SELECT 'available', COUNT(*) FROM containers WHERE status='AVAILABLE';