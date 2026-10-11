-- ============================================================
-- V1__create_chat_schema.sql
-- Charset & Session Setup
-- ============================================================
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET character_set_connection = utf8mb4;
-- SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. USERS
-- ============================================================
CREATE TABLE users (
                       id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                       username        VARCHAR(50)     NOT NULL,
                       email           VARCHAR(255)    NOT NULL,
                       password_hash   VARCHAR(255)    NOT NULL,
                       display_name    VARCHAR(100)    NOT NULL,
                       avatar_url      VARCHAR(500)    DEFAULT NULL,
                       status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
                       last_seen_at    DATETIME(3)     DEFAULT NULL,
                       created_at      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                       updated_at      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

                       PRIMARY KEY (id),
                       UNIQUE KEY uk_users_username (username),
                       UNIQUE KEY uk_users_email (email),

                       CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'BANNED'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- ============================================================
-- 2. ROLES
-- ============================================================
CREATE TABLE roles (
                       id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                       name        VARCHAR(50)     NOT NULL,
                       description VARCHAR(255)    DEFAULT NULL,

                       PRIMARY KEY (id),
                       UNIQUE KEY uk_roles_name (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- ============================================================
-- 3. USER_ROLES
-- ============================================================
CREATE TABLE user_roles (
                            user_id     BIGINT UNSIGNED NOT NULL,
                            role_id     BIGINT UNSIGNED NOT NULL,
                            assigned_at DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

                            PRIMARY KEY (user_id, role_id),
                            KEY idx_user_roles_role (role_id),

                            CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
                            CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- ============================================================
-- 4. FRIENDSHIPS
-- ============================================================
CREATE TABLE friendships (
                             id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                             requester_id  BIGINT UNSIGNED NOT NULL,
                             addressee_id  BIGINT UNSIGNED NOT NULL,
                             status        VARCHAR(20)     NOT NULL DEFAULT 'PENDING',

    -- Generated columns for efficient unique pair constraint
                             user_low_id   BIGINT UNSIGNED GENERATED ALWAYS AS (LEAST(requester_id, addressee_id)) STORED,
                             user_high_id  BIGINT UNSIGNED GENERATED ALWAYS AS (GREATEST(requester_id, addressee_id)) STORED,

                             created_at    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                             updated_at    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

                             PRIMARY KEY (id),
                             UNIQUE KEY uk_friendships_pair (user_low_id, user_high_id),
                             KEY idx_friendships_addressee (addressee_id, status),
                             KEY idx_friendships_requester (requester_id, status),

                             CONSTRAINT fk_friendships_requester FOREIGN KEY (requester_id) REFERENCES users (id) ON DELETE RESTRICT,
                             CONSTRAINT fk_friendships_addressee FOREIGN KEY (addressee_id) REFERENCES users (id) ON DELETE RESTRICT,

                             CONSTRAINT ck_friendships_self CHECK (requester_id <> addressee_id),
                             CONSTRAINT ck_friendships_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'BLOCKED'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- ============================================================
-- 5. CONVERSATIONS
-- ============================================================
CREATE TABLE conversations (
                               id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                               type              VARCHAR(20)     NOT NULL,
                               name              VARCHAR(100)    DEFAULT NULL,
                               avatar_url        VARCHAR(500)    DEFAULT NULL,
                               direct_key        VARCHAR(64)     DEFAULT NULL,
                               created_by        BIGINT UNSIGNED DEFAULT NULL,
                               last_message_id   BIGINT UNSIGNED DEFAULT NULL,
                               last_message_at   DATETIME(3)     DEFAULT NULL,
                               created_at        DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                               updated_at        DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

                               PRIMARY KEY (id),
                               UNIQUE KEY uk_conversations_direct_key (direct_key),
                               KEY idx_conversations_last_message_at (last_message_at),

                               CONSTRAINT fk_conversations_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL,

                               CONSTRAINT ck_conversations_type CHECK (type IN ('DIRECT', 'GROUP')),
                               CONSTRAINT ck_conversations_direct_key CHECK (
                                   (type = 'DIRECT' AND direct_key IS NOT NULL)
                                       OR (type = 'GROUP' AND direct_key IS NULL)
                                   )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- ============================================================
-- 6. MESSAGES
-- ============================================================
CREATE TABLE messages (
                          id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                          conversation_id     BIGINT UNSIGNED NOT NULL,
                          sender_id           BIGINT UNSIGNED DEFAULT NULL,
                          client_message_id   VARCHAR(64)     DEFAULT NULL,
                          type                VARCHAR(20)     NOT NULL DEFAULT 'TEXT',
                          content             TEXT            DEFAULT NULL,
                          reply_to_message_id BIGINT UNSIGNED DEFAULT NULL,
                          created_at          DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                          updated_at          DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
                          deleted_at          DATETIME(3)     DEFAULT NULL,

                          PRIMARY KEY (id),
                          KEY idx_messages_conversation_id (conversation_id, id),
                          KEY idx_messages_sender (sender_id),
                          KEY idx_messages_reply_to (reply_to_message_id),
                          UNIQUE KEY uk_messages_client_id (sender_id, client_message_id),

                          CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (id) ON DELETE CASCADE,
                          CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE RESTRICT,
                          CONSTRAINT fk_messages_reply_to FOREIGN KEY (reply_to_message_id) REFERENCES messages (id) ON DELETE SET NULL,

                          CONSTRAINT ck_messages_type CHECK (type IN ('TEXT', 'IMAGE', 'FILE', 'SYSTEM'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- ============================================================
-- 7. CONVERSATIONS → LAST MESSAGE FK (Circular Dependency Fix)
-- ============================================================
ALTER TABLE conversations
    ADD CONSTRAINT fk_conversations_last_message
        FOREIGN KEY (last_message_id) REFERENCES messages (id) ON DELETE SET NULL;

-- ============================================================
-- 8. CONVERSATION_MEMBERS
-- ============================================================
CREATE TABLE conversation_members (
                                      id                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                      conversation_id      BIGINT UNSIGNED NOT NULL,
                                      user_id              BIGINT UNSIGNED NOT NULL,
                                      role                 VARCHAR(20)     NOT NULL DEFAULT 'MEMBER',
                                      last_read_message_id BIGINT UNSIGNED DEFAULT NULL,
                                      muted_until          DATETIME(3)     DEFAULT NULL,
                                      joined_at            DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                      left_at              DATETIME(3)     DEFAULT NULL,

                                      PRIMARY KEY (id),
                                      UNIQUE KEY uk_members_conversation_user (conversation_id, user_id),
                                      KEY idx_members_user (user_id, left_at),

                                      CONSTRAINT fk_members_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (id) ON DELETE CASCADE,
                                      CONSTRAINT fk_members_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
                                      CONSTRAINT fk_members_last_read FOREIGN KEY (last_read_message_id) REFERENCES messages (id) ON DELETE SET NULL,

                                      CONSTRAINT ck_members_role CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- ============================================================
-- 9. ATTACHMENTS
-- ============================================================
CREATE TABLE attachments (
                             id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                             message_id  BIGINT UNSIGNED NOT NULL,
                             file_name   VARCHAR(255)    NOT NULL,
                             file_url    VARCHAR(1000)   NOT NULL,
                             mime_type   VARCHAR(100)    DEFAULT NULL,
                             file_size   BIGINT UNSIGNED DEFAULT NULL,
                             created_at  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

                             PRIMARY KEY (id),
                             KEY idx_attachments_message (message_id),

                             CONSTRAINT fk_attachments_message FOREIGN KEY (message_id) REFERENCES messages (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- ============================================================
-- 10. NOTIFICATIONS
-- ============================================================
CREATE TABLE notifications (
                               id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                               user_id        BIGINT UNSIGNED NOT NULL,
                               type           VARCHAR(50)     NOT NULL,
                               title          VARCHAR(255)    NOT NULL,
                               content        TEXT            DEFAULT NULL,
                               reference_type VARCHAR(30)     DEFAULT NULL,
                               reference_id   BIGINT UNSIGNED DEFAULT NULL,
                               is_read        BOOLEAN         NOT NULL DEFAULT FALSE,
                               read_at        DATETIME(3)     DEFAULT NULL,
                               created_at     DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

                               PRIMARY KEY (id),
                               KEY idx_notifications_user_read (user_id, is_read, created_at),

                               CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- ============================================================
-- 11. SEED DATA: ROLES
-- ============================================================
INSERT INTO roles (name, description) VALUES
                                          ('ROLE_USER',  'Người dùng thông thường'),
                                          ('ROLE_ADMIN', 'Quản trị hệ thống');

-- Re-enable foreign key checks
-- SET FOREIGN_KEY_CHECKS = 1;