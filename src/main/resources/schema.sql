SET FOREIGN_KEY_CHECKS = 0;

-- 동적 SQL을 실행하기 위한 변수 선언
SET @stmt = NULL;

-- 모든 테이블에서 데이터를 삭제하는 동적 SQL 생성
SELECT GROUP_CONCAT('DELETE FROM `', table_name, '`;') INTO @stmt
FROM information_schema.tables
WHERE table_schema = 'test_db';

-- NULL이 아닌 경우 실행
IF @stmt IS NOT NULL THEN
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
END IF;

-- 모든 테이블을 삭제하는 동적 SQL 생성
SELECT GROUP_CONCAT('DROP TABLE `', table_name, '`;') INTO @stmt
FROM information_schema.tables
WHERE table_schema = 'test_db';

-- NULL이 아닌 경우 실행
IF @stmt IS NOT NULL THEN
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
END IF;

SET FOREIGN_KEY_CHECKS = 1;



-- 사용자 테이블 생성
CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     email VARCHAR(320) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(10) NOT NULL UNIQUE,
    profile_image TEXT,
    is_resigned BOOLEAN NOT NULL DEFAULT FALSE
    );

-- 게시글 테이블 생성
CREATE TABLE IF NOT EXISTS posts (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     user_id BIGINT NOT NULL,
                                     title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    image TEXT,
    likes INT DEFAULT 0,
    views INT DEFAULT 0,
    post_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

-- 댓글 테이블 생성
CREATE TABLE IF NOT EXISTS comment (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       posts_id BIGINT NOT NULL,
                                       user_id BIGINT NOT NULL,
                                       comment TEXT,
                                       comment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       FOREIGN KEY (posts_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

-- 좋아요 테이블 생성
CREATE TABLE IF NOT EXISTS post_likes (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          user_id BIGINT NOT NULL,
                                          post_id BIGINT NOT NULL,
                                          UNIQUE (user_id, post_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
    );
