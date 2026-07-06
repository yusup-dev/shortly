-- Enforce data integrity that the JPA entities already assume but the
-- original Fase 0 migrations (V1/V2/V6) never created at the DB level.

-- users.email is looked up via findByEmail / existsByEmail and the User entity
-- declares it as unique. Without a DB constraint two concurrent registrations
-- could both pass the existsByEmail check and insert duplicate emails.
ALTER TABLE users
    ADD CONSTRAINT uq_users_email UNIQUE (email);

-- roles.name and plans.name are resolved via findByName(...) which returns a
-- single Optional; a duplicate name would break that lookup.
ALTER TABLE roles
    ADD CONSTRAINT uq_roles_name UNIQUE (name);

ALTER TABLE plans
    ADD CONSTRAINT uq_plans_name UNIQUE (name);
