Create database
CREATE DATABASE traffic_management_system;
USE traffic_management_system;

INSERT INTO account (id,username,email ,password, role)
VALUES ('nhtk412@', "nguyenhuutuankhang412@gmail.com",'Tuankhang412@', "USER");


Table: account
CREATE TABLE account (
    id VARCHAR(12) PRIMARY KEY COMMENT 'CCCD làm ID của tài khoản',
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('USER','ADMIN','MODERATOR') NOT NULL DEFAULT 'USER',
    last_login DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

Table: login_history (theo dõi lịch sử đăng nhập)
CREATE TABLE login_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_id VARCHAR(12) NOT NULL,
    login_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    logout_time DATETIME,
    ip_address VARCHAR(50),
    device_info VARCHAR(255),
    login_status ENUM('SUCCESS', 'FAILED') NOT NULL,
    FOREIGN KEY (account_id) REFERENCES account(id)
);

Table: person (Sử dụng CCCD làm ID)
CREATE TABLE person (
    id VARCHAR(12) PRIMARY KEY COMMENT 'CCCD làm ID của người dân',
    full_name VARCHAR(100) NOT NULL,
    birth_date DATE,
    gender ENUM('MALE','FEMALE'),
    address VARCHAR(255),
    phone_number VARCHAR(20)
);

Table: car
CREATE TABLE car (
    id INT AUTO_INCREMENT PRIMARY KEY,
    license_plate VARCHAR(20) NOT NULL UNIQUE,
    brand VARCHAR(50),
    color VARCHAR(30),
    owner_id VARCHAR(12) NOT NULL,
    FOREIGN KEY (owner_id) REFERENCES person(id)
);

Table: motorcycle
CREATE TABLE motorcycle (
    id INT AUTO_INCREMENT PRIMARY KEY,
    license_plate VARCHAR(20) NOT NULL UNIQUE,
    brand VARCHAR(50),
    color VARCHAR(30),
    owner_id VARCHAR(12) NOT NULL,
    FOREIGN KEY (owner_id) REFERENCES person(id)
);

Table: face_data
CREATE TABLE face_data (
    id INT AUTO_INCREMENT PRIMARY KEY,
    person_id VARCHAR(12) NOT NULL,
    tracking_id INT,
    bounding_box_left FLOAT,
    bounding_box_top FLOAT,
    bounding_box_right FLOAT,
    bounding_box_bottom FLOAT,
    rotation_x FLOAT,
    rotation_y FLOAT,
    rotation_z FLOAT,
    right_eye_open_probability FLOAT,
    left_eye_open_probability FLOAT,
    smiling_probability FLOAT,
    left_eye_x FLOAT,
    left_eye_y FLOAT,
    right_eye_x FLOAT,
    right_eye_y FLOAT,
    nose_x FLOAT,
    nose_y FLOAT,
    mouth_left_x FLOAT,
    mouth_left_y FLOAT,
    mouth_right_x FLOAT,
    mouth_right_y FLOAT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (person_id) REFERENCES person(id)
);

Table: car_violations (Bảng vi phạm dành riêng cho xe hơi)
CREATE TABLE car_violations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    violation_time DATETIME NOT NULL,
    violation_type VARCHAR(255) NOT NULL,
    description TEXT,
    penalty_type VARCHAR(255),
    fine_amount DECIMAL(10,2),
    violator_id VARCHAR(12) NOT NULL,
    officer_id VARCHAR(12) NOT NULL,
    car_id INT NOT NULL,
    FOREIGN KEY (violator_id) REFERENCES person(id),
    FOREIGN KEY (officer_id) REFERENCES account(id),
    FOREIGN KEY (car_id) REFERENCES car(id)
);

Table: motorcycle_violations (Bảng vi phạm dành riêng cho xe máy)
CREATE TABLE motorcycle_violations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    violation_time DATETIME NOT NULL,
    violation_type VARCHAR(255) NOT NULL,
    description TEXT,
    penalty_type VARCHAR(255),
    fine_amount DECIMAL(10,2),
    violator_id VARCHAR(12) NOT NULL,
    officer_id VARCHAR(12) NOT NULL,
    motorcycle_id INT NOT NULL,
    FOREIGN KEY (violator_id) REFERENCES person(id),
    FOREIGN KEY (officer_id) REFERENCES account(id),
    FOREIGN KEY (motorcycle_id) REFERENCES motorcycle(id)
);

Table: car_scan_logs (Chi tiết quét dành riêng cho xe hơi)
CREATE TABLE car_scan_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    scan_timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    license_plate VARCHAR(20) NOT NULL,
    operator_id VARCHAR(12) NOT NULL,
    car_id INT,
    FOREIGN KEY (operator_id) REFERENCES account(id),
    FOREIGN KEY (car_id) REFERENCES car(id)
);

Simplified Table: motorcycle_scan_logs
CREATE TABLE motorcycle_scan_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    scan_timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    license_plate VARCHAR(20) NOT NULL,
    operator_id VARCHAR(12) NOT NULL,
    motorcycle_id INT,
    FOREIGN KEY (operator_id) REFERENCES account(id),
    FOREIGN KEY (motorcycle_id) REFERENCES motorcycle(id)
);

Thêm các chỉ mục (indexes) để tối ưu truy vấn
Indexes cho bảng car_violations
CREATE INDEX idx_car_violations_car_id ON car_violations(car_id);
CREATE INDEX idx_car_violations_violator ON car_violations(violator_id);
CREATE INDEX idx_car_violations_time ON car_violations(violation_time);

Indexes cho bảng motorcycle_violations
CREATE INDEX idx_mc_violations_mc_id ON motorcycle_violations(motorcycle_id);
CREATE INDEX idx_mc_violations_violator ON motorcycle_violations(violator_id);
CREATE INDEX idx_mc_violations_time ON motorcycle_violations(violation_time);

Indexes cho bảng car_scan_logs
CREATE INDEX idx_car_scan_time ON car_scan_logs(scan_timestamp);
CREATE INDEX idx_car_scan_plate ON car_scan_logs(license_plate);

Indexes cho bảng motorcycle_scan_logs
CREATE INDEX idx_mc_scan_time ON motorcycle_scan_logs(scan_timestamp);
CREATE INDEX idx_mc_scan_plate ON motorcycle_scan_logs(license_plate);

Indexes cho các bảng quan hệ chính
CREATE INDEX idx_car_owner ON car(owner_id);
CREATE INDEX idx_motorcycle_owner ON motorcycle(owner_id);
CREATE INDEX idx_face_person ON face_data(person_id);

-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
ALTER TABLE person
ADD COLUMN path_face VARCHAR(255) COMMENT 'Đường dẫn ảnh khuôn mặt';

 -- id VARCHAR(12) PRIMARY KEY COMMENT 'CCCD làm ID của người dân',
--     full_name VARCHAR(100) NOT NULL,
--     birth_date DATE,
--     gender ENUM('MALE','FEMALE'),
--     address VARCHAR(255),
--     phone_number VARCHAR(20)
 
INSERT INTO person
VALUES
('058205002155', 'Nguyen Huu Tuan Khang', '2005-12-04', 'MALE', 'Binh Duong', '0366408263', 'D:\\Programming_Language\\Python\\LearingFastAPI\\imgmatch_api\\Data\\058205002155.jpg');