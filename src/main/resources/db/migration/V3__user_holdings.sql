ALTER TABLE users ADD COLUMN available_cash NUMERIC(18, 2) NOT NULL DEFAULT 0;

CREATE TABLE user_holdings
(
    id               SERIAL PRIMARY KEY,
    user_id          INTEGER        NOT NULL REFERENCES users (id),
    instrument_id    INTEGER        NOT NULL REFERENCES instruments (id),
    available_shares INTEGER        NOT NULL DEFAULT 0,
    held_shares      INTEGER        NOT NULL DEFAULT 0,
    total_buy_cost   NUMERIC(18, 2) NOT NULL DEFAULT 0,
    UNIQUE (user_id, instrument_id)
);

CREATE INDEX idx_user_holdings_user ON user_holdings (user_id);

-- Derive available_cash from existing FILLED/NEW orders
UPDATE users u
SET available_cash = (SELECT COALESCE(SUM(
                                              CASE
                                                  WHEN o.side IN ('CASH_IN', 'SELL') AND o.status = 'FILLED'
                                                      THEN o.size * o.price
                                                  WHEN o.side IN ('CASH_OUT', 'BUY') AND o.status = 'FILLED'
                                                      THEN -(o.size * o.price)
                                                  WHEN o.side = 'BUY' AND o.status = 'NEW'
                                                      THEN -(o.size * o.price)
                                                  ELSE 0
                                                  END), 0)
                      FROM orders o
                      WHERE o.user_id = u.id);

-- Derive holdings from existing FILLED/NEW orders for non-cash instruments
INSERT INTO user_holdings (user_id, instrument_id, available_shares, held_shares, total_buy_cost)
SELECT o.user_id,
       o.instrument_id,
       SUM(CASE
               WHEN o.side = 'BUY' AND o.status = 'FILLED' THEN o.size
               WHEN o.side = 'SELL' AND o.status = 'FILLED' THEN -o.size
               WHEN o.side = 'SELL' AND o.status = 'NEW' THEN -o.size
               ELSE 0
           END) AS available_shares,
       SUM(CASE
               WHEN o.side = 'BUY' AND o.status = 'FILLED' THEN o.size
               WHEN o.side = 'SELL' AND o.status = 'FILLED' THEN -o.size
               ELSE 0
           END) AS held_shares,
       SUM(CASE
               WHEN o.side = 'BUY' AND o.status = 'FILLED' THEN o.size * o.price
               ELSE 0
           END) AS total_buy_cost
FROM orders o
         JOIN instruments i ON i.id = o.instrument_id
WHERE i.type != 'CURRENCY'
GROUP BY o.user_id, o.instrument_id
HAVING SUM(CASE
               WHEN o.side = 'BUY' AND o.status = 'FILLED' THEN o.size
               WHEN o.side = 'SELL' AND o.status = 'FILLED' THEN -o.size
               ELSE 0
           END) > 0;
