-- Create database
CREATE DATABASE IF NOT EXISTS library_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE library_db;

-- User table
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `real_name` VARCHAR(50) NOT NULL,
  `email` VARCHAR(100) DEFAULT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `role` ENUM('ADMIN','READER') NOT NULL DEFAULT 'READER',
  `status` TINYINT NOT NULL DEFAULT 1,
  `avatar` VARCHAR(255) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Category table
CREATE TABLE IF NOT EXISTS `category` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Book table
CREATE TABLE IF NOT EXISTS `book` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `isbn` VARCHAR(20) NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `author` VARCHAR(100) NOT NULL,
  `publisher` VARCHAR(100) DEFAULT NULL,
  `category_id` INT DEFAULT NULL,
  `publish_date` DATE DEFAULT NULL,
  `description` TEXT,
  `cover_image` VARCHAR(255) DEFAULT NULL,
  `total_quantity` INT NOT NULL DEFAULT 1,
  `available_quantity` INT NOT NULL DEFAULT 1,
  `location` VARCHAR(100) DEFAULT NULL,
  `price` DECIMAL(10,2) DEFAULT NULL COMMENT '图书价格(罚款上限)',
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_isbn` (`isbn`),
  KEY `idx_category` (`category_id`),
  KEY `idx_title_author` (`title`,`author`),
  CONSTRAINT `fk_book_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Borrow record table
CREATE TABLE IF NOT EXISTS `borrow_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `book_id` BIGINT NOT NULL,
  `borrow_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `due_date` DATETIME NOT NULL,
  `return_date` DATETIME DEFAULT NULL,
  `status` ENUM('BORROWED','RETURNED','RENEWED','OVERDUE') NOT NULL DEFAULT 'BORROWED',
  `renew_count` TINYINT DEFAULT 0,
  `fine_amount` DECIMAL(10,2) DEFAULT 0.00,
  `return_operator` BIGINT DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_book` (`book_id`),
  KEY `idx_status` (`status`),
  KEY `idx_due_date` (`due_date`),
  CONSTRAINT `fk_borrow_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_borrow_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Operation log table
CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `operator_id` BIGINT DEFAULT NULL,
  `module` VARCHAR(50) DEFAULT NULL,
  `type` VARCHAR(50) DEFAULT NULL,
  `content` VARCHAR(500) DEFAULT NULL,
  `ip` VARCHAR(50) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_operator` (`operator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert default admin user (password: admin123, BCrypt encrypted)
INSERT INTO `user` (`username`, `password`, `real_name`, `email`, `role`, `status`, `create_time`) VALUES
('admin', '$2a$10$/G.1wL4SnMm/yWmahWyvieeesQ6z8hGynojE/pvX.8qsMEY7Dipyu', 'System Admin', 'admin@library.com', 'ADMIN', 1, NOW());

-- Insert default categories
INSERT INTO `category` (`name`, `description`) VALUES
('Literature', 'Novels, poetry, and literary works'),
('Technology', 'Computer science, engineering, and technical books'),
('History', 'Historical events and biographies'),
('Science', 'Natural sciences and mathematics'),
('Philosophy', 'Philosophy and psychology'),
('Arts', 'Art, music, and photography'),
('Business', 'Business, economics, and management'),
('Education', 'Educational materials and textbooks');

-- Insert sample books
INSERT INTO `book` (`isbn`, `title`, `author`, `publisher`, `category_id`, `publish_date`, `description`, `cover_image`, `total_quantity`, `available_quantity`, `location`, `status`) VALUES
('978-0-13-468599-1', 'The Great Gatsby', 'F. Scott Fitzgerald', 'Scribner', 1, '1925-04-10', 'A novel set in the Jazz Age', 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=300', 5, 5, 'A-101', 1),
('978-0-13-235088-4', 'Clean Code', 'Robert C. Martin', 'Prentice Hall', 2, '2008-01-01', 'A handbook of agile software craftsmanship', 'https://images.unsplash.com/photo-1516116216624-53e697fedbea?w=300', 3, 3, 'B-202', 1),
('978-0-13-110362-7', 'A Brief History of Time', 'Stephen Hawking', 'Bantam', 4, '1988-04-01', 'A landmark volume in science popularization', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300', 4, 4, 'C-303', 1),
('978-0-14-143951-8', '1984', 'George Orwell', 'Secker & Warburg', 1, '1949-06-08', 'A dystopian social science fiction', 'https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=300', 6, 6, 'A-102', 1),
('978-0-06-112008-4', 'To Kill a Mockingbird', 'Harper Lee', 'J.B. Lippincott & Co.', 1, '1960-07-11', 'A story of racial injustice and childhood innocence', 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=300', 5, 5, 'A-103', 1),
('978-0-13-595705-7', 'Design Patterns', 'Erich Gamma et al.', 'Addison-Wesley', 2, '1994-10-31', 'Elements of reusable object-oriented software', 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=300', 3, 3, 'B-203', 1),
('978-0-30-727822-4', 'Sapiens', 'Yuval Noah Harari', 'Harper', 3, '2011-01-01', 'A brief history of humankind', 'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=300', 4, 4, 'D-404', 1),
('978-0-14-028329-7', 'The Art of War', 'Sun Tzu', 'Oxford University Press', 3, '1971-01-01', 'Ancient Chinese military treatise', 'https://images.unsplash.com/photo-1553775282-20af80779df7?w=300', 8, 8, 'D-405', 1);
