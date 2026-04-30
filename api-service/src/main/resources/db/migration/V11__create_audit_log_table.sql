CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    actor_type VARCHAR(50),
    actor_id UUID,
    action_type VARCHAR(50),
    target_type VARCHAR(50),
    target_id UUID,
    ip_address VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO audit_logs (actor_type, actor_id, action_type, target_type, target_id, ip_address)
VALUES
  ('USER','0319aeb0-8212-4777-83d3-804d739f71be', 'CREATE_SHORT_URL', 'URL', '30a27b20-5025-4b01-9f67-42ee067e1fdd', '192.168.1.1')

