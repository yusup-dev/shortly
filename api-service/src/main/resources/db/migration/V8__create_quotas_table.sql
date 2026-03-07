CREATE TABLE IF NOT EXISTS quotas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    api_key_id UUID,
    max_requests_per_day INT NOT NULL,
    max_urls_per_key INT NOT NULL,
    max_bulk INT NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_quotas_api_keys
        FOREIGN KEY (api_key_id)
        REFERENCES api_keys(id)
);