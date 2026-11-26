CREATE TABLE expenses (
                          id BIGSERIAL PRIMARY KEY,
                          group_id BIGINT NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
                          paid_by BIGINT NOT NULL REFERENCES users(id),
                          description VARCHAR(255) NOT NULL,
                          amount_minor BIGINT NOT NULL,
                          currency VARCHAR(3) NOT NULL DEFAULT 'INR',
                          category VARCHAR(50) NOT NULL DEFAULT 'general',
                          split_type VARCHAR(20) NOT NULL,
                          expense_date DATE NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT now(),
                          deleted_at TIMESTAMP
);

CREATE INDEX idx_expenses_group_id ON expenses(group_id);
CREATE INDEX idx_expenses_expense_date ON expenses(expense_date);

CREATE TABLE expense_splits (
                                id BIGSERIAL PRIMARY KEY,
                                expense_id BIGINT NOT NULL REFERENCES expenses(id) ON DELETE CASCADE,
                                user_id BIGINT NOT NULL REFERENCES users(id),
                                share_minor BIGINT NOT NULL
);

CREATE INDEX idx_expense_splits_expense_id ON expense_splits(expense_id);
CREATE INDEX idx_expense_splits_user_id ON expense_splits(user_id);

CREATE TABLE settlements (
                             id BIGSERIAL PRIMARY KEY,
                             group_id BIGINT NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
                             from_user BIGINT NOT NULL REFERENCES users(id),
                             to_user BIGINT NOT NULL REFERENCES users(id),
                             amount_minor BIGINT NOT NULL,
                             created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_settlements_group_id ON settlements(group_id);

CREATE TABLE activity_logs (
                               id BIGSERIAL PRIMARY KEY,
                               group_id BIGINT NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
                               actor_id BIGINT NOT NULL REFERENCES users(id),
                               action VARCHAR(50) NOT NULL,
                               payload JSONB NOT NULL,
                               created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_activity_logs_group_id ON activity_logs(group_id);