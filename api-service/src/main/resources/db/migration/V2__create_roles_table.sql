CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL,
    description VARCHAR(255) NOT NULL
);

INSERT INTO roles (name, description)
VALUES
    ('USER', 'End user of SaaS'),
    ('ADMIN', 'Internal system administrator');