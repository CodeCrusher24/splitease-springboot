CREATE TABLE groups (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        description VARCHAR(255),
                        currency VARCHAR(3) NOT NULL DEFAULT 'INR',
                        created_by BIGINT NOT NULL REFERENCES users(id),
                        created_at TIMESTAMP NOT NULL DEFAULT now(),
                        archived BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_groups_created_by ON groups(created_by);

CREATE TABLE group_members (
                               group_id BIGINT NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
                               user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               role VARCHAR(20) NOT NULL DEFAULT 'member',
                               joined_at TIMESTAMP NOT NULL DEFAULT now(),
                               PRIMARY KEY (group_id, user_id)
);

CREATE INDEX idx_group_members_user_id ON group_members(user_id);