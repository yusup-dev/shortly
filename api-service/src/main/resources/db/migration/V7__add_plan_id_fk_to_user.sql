ALTER TABLE users
    ADD COLUMN plan_id BIGINT;

ALTER TABLE users
    ADD CONSTRAINT fk_users_plans
        FOREIGN KEY (plan_id)
            REFERENCES plans(id)