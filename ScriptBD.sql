-- Crear base de datos y configurar charset/collation
CREATE DATABASE IF NOT EXISTS `prueba_puntored`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE `prueba_puntored`;

SET NAMES utf8mb4;

-- Opcional: para asegurar integridad transaccional
SET @@SESSION.sql_require_primary_key = 0;

-- Eliminar tablas si existen 
DROP TABLE IF EXISTS `payments`;
DROP TABLE IF EXISTS `allowed_ips`;
DROP TABLE IF EXISTS `ip_restriction_config`;

-- Tabla de Pagos
CREATE TABLE `payments` (
  `payment_id` BIGINT NOT NULL AUTO_INCREMENT,
  `reference` VARCHAR(30) NOT NULL,
  `external_id` VARCHAR(255) NOT NULL,
  `amount` DECIMAL(18,2) NOT NULL,
  `description` VARCHAR(512) NOT NULL,
  `due_date` DATETIME(6) NOT NULL,
  `status` VARCHAR(16) NOT NULL, -- Enum mapeado como texto: CREATED/PAID/CANCELED/EXPIRED
  `callback_url` VARCHAR(1024) NOT NULL,
  `update_description` VARCHAR(1024) NULL,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NULL,
  `paid_at` DATETIME(6) NULL,
  `canceled_at` DATETIME(6) NULL,
  `expired_at` DATETIME(6) NULL,
  `callback_attempts` INT NOT NULL DEFAULT 0,
  `next_callback_at` DATETIME(6) NULL,
  `last_callback_response` VARCHAR(1024) NULL,
  `acknowledged` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`payment_id`),
  UNIQUE KEY `ux_pay_reference` (`reference`),
  KEY `idx_pay_status` (`status`),
  KEY `idx_pay_due_date` (`due_date`),
  KEY `idx_pay_created_at` (`created_at`),
  CONSTRAINT `chk_pay_status` CHECK (`status` IN ('CREATED','PAID','CANCELED','EXPIRED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Tabla de IPs permitidas
CREATE TABLE `allowed_ips` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `ip_address` VARCHAR(64) NOT NULL,
  `enabled` TINYINT(1) NOT NULL DEFAULT 1,
  `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_allowed_ip` (`ip_address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Configuración de restricción de IPs (registro singleton con id=1)
CREATE TABLE `ip_restriction_config` (
  `id` BIGINT NOT NULL,                 -- siempre 1
  `enabled` TINYINT(1) NOT NULL DEFAULT 0,
  `updated_at` DATETIME(6) NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `chk_ip_enabled` CHECK (`enabled` IN (0,1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Set inicial para la configuración de IPs (deshabilitado por defecto)
INSERT INTO `ip_restriction_config` (`id`, `enabled`, `updated_at`)
VALUES (1, 0, NOW(6))
ON DUPLICATE KEY UPDATE `enabled`=VALUES(`enabled`), `updated_at`=VALUES(`updated_at`);

-- Opcional: permitir localhost por defecto (se puede omitir o cambiar)
-- INSERT INTO `allowed_ips` (`ip_address`, `enabled`) VALUES ('127.0.0.1', 1);
