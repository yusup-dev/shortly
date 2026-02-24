ALTER TABLE users
    ADD COLUMN role_id BIGINT;

ALTER TABLE users
    ADD CONSTRAINT fk_users_role
        FOREIGN KEY (role_id)
            REFERENCES roles(id);

UPDATE users
SET role_id = (SELECT id FROM roles WHERE name = 'USER')
WHERE email = 'free@gmail.com';

UPDATE users
SET role_id = (SELECT id FROM roles WHERE name = 'USER')
WHERE email = 'pro@gmail.com';

UPDATE users
SET role_id = (SELECT id FROM roles WHERE name = 'ADMIN')
WHERE email = 'admin@gmail.com'
