CREATE TABLE IF NOT EXISTS plans (
      id BIGSERIAL PRIMARY KEY,
      name VARCHAR(20) NOT NULL,
      max_requests_per_day INT NOT NULL,
      max_urls_per_key INT NOT NULL,
      max_bulk INT NOT NULL,
      updated_at TIMESTAMP
);

INSERT INTO plans (name, max_requests_per_day, max_urls_per_key, max_bulk)
VALUES ('FREE', 100, 10, 0),
       ('PRO', 1000, 1000, 100)



