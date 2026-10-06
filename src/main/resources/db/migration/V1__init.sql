-- waggle 서비스를 위한 테이블 스킴을 정의해주세요.
-- 참고:  한 번 실행된 마이그레이션 파일을 고치면 재 실행 시 Flyway 가 체크섬이 달라졌다며 실패합니다.
-- 아직 내용을 채우기 전이라면 `docker compose down -v` 로 DB 를 비우고 다시 띄우면 됩니다.
-- 파일을 고치는 대신 새 버전의 마이그레이션 파일을 만드는 것이 편합니다.

-- 1. 유저 테이블
CREATE TABLE `users` (
	`id`                   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '사용자 고유 ID',
	`email`                VARCHAR(255) NOT NULL COMMENT '이메일',
	`password`             VARCHAR(255) NOT NULL COMMENT '비밀번호',
	`name`                 VARCHAR(50)  NOT NULL COMMENT '이름',
	`github_username`      VARCHAR(100) NOT NULL COMMENT 'GitHub 사용자명',
	`role`                 VARCHAR(20)  NOT NULL COMMENT '역할 (ADMIN, STAFF, ROOKIE)',
	`status`               VARCHAR(20)  NOT NULL COMMENT '가입 상태 (PENDING, APPROVED, REJECTED)',
	`assigned_seminar_id`  BIGINT       NULL     COMMENT '운영진 담당 세미나 ID (FK, Nullable)',
    `created_at` DATETIME NOT NULL DEFAULT (UTC_TIMESTAMP() + INTERVAL 9 HOUR) COMMENT '가입 신청 일시',
	CONSTRAINT `PK_USERS` PRIMARY KEY (`id`),
    CONSTRAINT `UQ_USERS_EMAIL` UNIQUE (`email`)
) COMMENT='사용자 정보';

-- 2. 세미나 테이블
CREATE TABLE `seminars` (
	`id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '세미나 고유 ID',
	`title`             VARCHAR(255) NOT NULL COMMENT '세미나 제목 (필수)',
	`description`       TEXT         NULL     COMMENT '세미나 설명 (Markdown, 선택)',
	`capacity`          INT          NOT NULL COMMENT '수강 정원',
	`apply_start_at`    DATETIME     NOT NULL COMMENT '수강 신청 시작 일시',
	`apply_end_at`      DATETIME     NOT NULL COMMENT '수강 신청 종료 일시',
	`total_grace_days`  INT          NOT NULL COMMENT '기본 제공 총 Grace Day 수',
	`created_at`        DATETIME     NOT NULL DEFAULT (UTC_TIMESTAMP() + INTERVAL 9 HOUR) COMMENT '생성 일시',
	CONSTRAINT `PK_SEMINARS` PRIMARY KEY (`id`)
) COMMENT='세미나 정보';

-- 3. 수강 신청 테이블
CREATE TABLE `enrollments` (
	`id`                    BIGINT   NOT NULL AUTO_INCREMENT COMMENT '수강 신청 고유 ID',
	`remaining_grace_days`  INT      NOT NULL COMMENT '개인별 남은 Grace Day 수',
	`is_failed`             BOOLEAN  NOT NULL DEFAULT FALSE COMMENT '탈락 여부 (TRUE: 탈락, FALSE: 수강 중)',
	`created_at`            DATETIME NOT NULL DEFAULT (UTC_TIMESTAMP() + INTERVAL 9 HOUR) COMMENT '수강 신청 일시',
	`user_id`               BIGINT   NOT NULL COMMENT '루키 사용자 ID (FK)',
	`seminar_id`            BIGINT   NOT NULL COMMENT '세미나 고유 ID (FK)',
	CONSTRAINT `PK_ENROLLMENTS` PRIMARY KEY (`id`),
	CONSTRAINT `UQ_ENROLLMENT_USER_SEMINAR` UNIQUE (`user_id`, `seminar_id`)
) COMMENT='수강 신청 내역';

-- 4. 수업 회차 테이블
CREATE TABLE `sessions` (
	`id`                    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '수업 회차 고유 ID',
	`title`                 VARCHAR(255) NOT NULL COMMENT '회차 제목',
	`starts_at`             DATETIME     NOT NULL COMMENT '시작 일시 (정렬 기준)',
	`location`              VARCHAR(255) NULL     COMMENT '수업 장소 (선택)',
	`assignment_title`      VARCHAR(255) NOT NULL COMMENT '과제 제목',
	`lecture_content`   TEXT         NULL     COMMENT '수업 설명 (Markdown)',
	`assignment_content` TEXT        NULL     COMMENT '과제 설명 (Markdown)',
	`seminar_id`            BIGINT       NOT NULL COMMENT '소속 세미나 ID (FK)',
	CONSTRAINT `PK_SESSIONS` PRIMARY KEY (`id`)
) COMMENT='수업 회차';

-- 5. 과제 제출 기록 테이블
CREATE TABLE `submissions` (
	`id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '과제 제출 기록 고유 ID',
	`status`        VARCHAR(30) NOT NULL COMMENT '과제 상태 (PASSED, SUBMITTED_BUT_FAILED, NOT_SUBMITTED)',
	`session_id`    BIGINT      NOT NULL COMMENT '수업 회차 ID (FK)',
	`enrollment_id` BIGINT      NOT NULL COMMENT '수강 신청 ID (FK)',
	CONSTRAINT `PK_SUBMISSIONS` PRIMARY KEY (`id`),
	CONSTRAINT `UQ_SUBMISSION_SESSION_ENROLLMENT` UNIQUE (`session_id`, `enrollment_id`)
) COMMENT='회차별 과제 제출 기록';

-- 6. 출석 기록 테이블
CREATE TABLE `attendances` (
	`id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '출석 기록 고유 ID',
	`status`        VARCHAR(20) NOT NULL COMMENT '출석 상태 (PRESENT, ABSENT)',
	`enrollment_id` BIGINT      NOT NULL COMMENT '수강 신청 ID (FK)',
	`session_id`    BIGINT      NOT NULL COMMENT '수업 회차 ID (FK)',
	CONSTRAINT `PK_ATTENDANCES` PRIMARY KEY (`id`),
	CONSTRAINT `UQ_ATTENDANCE_SESSION_ENROLLMENT` UNIQUE (`session_id`, `enrollment_id`)
) COMMENT='회차별 출석 기록';

-- 외래키(FK) 제약조건 설정

-- users -> seminars (운영진 담당 세미나 삭제 시 NULL 처리)
ALTER TABLE `users` ADD CONSTRAINT `FK_seminars_TO_users_1` 
FOREIGN KEY (`assigned_seminar_id`) REFERENCES `seminars` (`id`) ON DELETE SET NULL;

-- enrollments -> users & seminars
ALTER TABLE `enrollments` ADD CONSTRAINT `FK_users_TO_enrollments_1` 
FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

ALTER TABLE `enrollments` ADD CONSTRAINT `FK_seminars_TO_enrollments_1` 
FOREIGN KEY (`seminar_id`) REFERENCES `seminars` (`id`) ON DELETE CASCADE;

-- sessions -> seminars
ALTER TABLE `sessions` ADD CONSTRAINT `FK_seminars_TO_sessions_1` 
FOREIGN KEY (`seminar_id`) REFERENCES `seminars` (`id`) ON DELETE CASCADE;

-- submissions -> sessions & enrollments
ALTER TABLE `submissions` ADD CONSTRAINT `FK_sessions_TO_submissions_1` 
FOREIGN KEY (`session_id`) REFERENCES `sessions` (`id`) ON DELETE CASCADE;

ALTER TABLE `submissions` ADD CONSTRAINT `FK_enrollments_TO_submissions_1` 
FOREIGN KEY (`enrollment_id`) REFERENCES `enrollments` (`id`) ON DELETE CASCADE;

-- attendances -> enrollments & sessions
ALTER TABLE `attendances` ADD CONSTRAINT `FK_enrollments_TO_attendances_1` 
FOREIGN KEY (`enrollment_id`) REFERENCES `enrollments` (`id`) ON DELETE CASCADE;

ALTER TABLE `attendances` ADD CONSTRAINT `FK_sessions_TO_attendances_1` 
FOREIGN KEY (`session_id`) REFERENCES `sessions` (`id`) ON DELETE CASCADE;