CREATE TABLE IF NOT EXISTS url_clicks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    url_id UUID NOT NULL,
    clicked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(64),
    country VARCHAR(100),
    device VARCHAR(50),
    os VARCHAR(50),
    browser VARCHAR(50),
    referrer_host VARCHAR(255),
    CONSTRAINT fk_url_clicks_url
        FOREIGN KEY (url_id)
        REFERENCES urls(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_url_clicks_url_id_clicked_at
    ON url_clicks (url_id, clicked_at);
