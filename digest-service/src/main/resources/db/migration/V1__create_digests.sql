CREATE TABLE digests (
    id         BIGSERIAL PRIMARY KEY,
    day        DATE        NOT NULL UNIQUE,
    content    TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now())