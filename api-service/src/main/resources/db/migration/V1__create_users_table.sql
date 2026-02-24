CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    updated_at TIMESTAMP
);

INSERT INTO users (email, password, status)
VALUES
    ('free@gmail.com', '$2a$12$CDOMmK7/MXI4JMUPZp28r.DtdoGt8Ohe1I0rfYl9tMfC4o0SwVMTm', 'ACTIVE'),
    ('pro@gmail.com', '$2a$12$CDOMmK7/MXI4JMUPZp28r.DtdoGt8Ohe1I0rfYl9tMfC4o0SwVMTm', 'ACTIVE'),
    ('admin@gmail.com', '$2a$12$CDOMmK7/MXI4JMUPZp28r.DtdoGt8Ohe1I0rfYl9tMfC4o0SwVMTm', 'ACTIVE');