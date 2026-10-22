CREATE TABLE digests (
    id            BIGSERIAL PRIMARY KEY,
    day           DATE        NOT NULL UNIQUE,
    items         JSONB       NOT NULL,
    model         TEXT        NOT NULL,
    article_count INT         NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);