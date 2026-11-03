-- 와장 계정은 가입 절차 없이 초기 데이터로 생성합니다.
INSERT INTO `users` (
    `email`,
    `password`,
    `name`,
    `github_username`,
    `role`,
    `status`,
    `assigned_seminar_id`
) VALUES (
    'admin@wafflestudio.com',
    '$2a$10$FQIyvcYDluX6SVPCzGY5oekCo6iLyM7RZJJK48kfxGBtq5hWj4qTW',
    '와장',
    'waffle-admin',
    'ADMIN',
    'APPROVED',
    NULL
);
