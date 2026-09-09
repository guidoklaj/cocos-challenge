CREATE TABLE users (
    id             SERIAL PRIMARY KEY,
    email          VARCHAR(255) NOT NULL UNIQUE,
    account_number VARCHAR(50)  NOT NULL UNIQUE
);

CREATE TABLE instruments (
    id     SERIAL PRIMARY KEY,
    ticker VARCHAR(20)  NOT NULL,
    name   VARCHAR(255) NOT NULL,
    type   VARCHAR(20)  NOT NULL
);

CREATE INDEX idx_instruments_ticker ON instruments (ticker);
CREATE INDEX idx_instruments_name ON instruments (name);

CREATE TABLE orders (
    id            SERIAL PRIMARY KEY,
    instrument_id INTEGER        NOT NULL REFERENCES instruments (id),
    user_id       INTEGER        NOT NULL REFERENCES users (id),
    side          VARCHAR(10)    NOT NULL,
    size          INTEGER        NOT NULL,
    price         NUMERIC(18, 2) NOT NULL,
    type          VARCHAR(10)    NOT NULL,
    status        VARCHAR(10)    NOT NULL,
    datetime      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_user ON orders (user_id);
CREATE INDEX idx_orders_user_status ON orders (user_id, status);
CREATE INDEX idx_orders_user_instrument_status ON orders (user_id, instrument_id, status);

CREATE TABLE marketdata (
    id             SERIAL PRIMARY KEY,
    instrument_id  INTEGER        NOT NULL REFERENCES instruments (id),
    high           NUMERIC(18, 2) NOT NULL,
    low            NUMERIC(18, 2) NOT NULL,
    open           NUMERIC(18, 2) NOT NULL,
    close          NUMERIC(18, 2) NOT NULL,
    previous_close NUMERIC(18, 2) NOT NULL,
    date           DATE           NOT NULL
);

CREATE INDEX idx_marketdata_instrument_date ON marketdata (instrument_id, date DESC);
