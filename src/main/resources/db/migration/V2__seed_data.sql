INSERT INTO users (email, account_number) VALUES
    ('alice@cocos.capital', '10001'),
    ('bob@cocos.capital',   '10002');

INSERT INTO instruments (ticker, name, type) VALUES
    ('ARS',  'PESOS',                    'MONEDA'),
    ('DYCA', 'Dycasa S.A.',              'ACCIONES'),
    ('GGAL', 'Grupo Financiero Galicia', 'ACCIONES'),
    ('PAMP', 'Pampa Holding S.A.',       'ACCIONES'),
    ('YPFD', 'Y.P.F. S.A.',              'ACCIONES');

INSERT INTO marketdata (instrument_id, high, low, open, close, previous_close, date)
SELECT id, 205.00, 195.00, 200.00, 202.50, 198.00, CURRENT_DATE FROM instruments WHERE ticker = 'DYCA';
INSERT INTO marketdata (instrument_id, high, low, open, close, previous_close, date)
SELECT id, 260.00, 250.00, 255.00, 258.00, 252.00, CURRENT_DATE FROM instruments WHERE ticker = 'GGAL';
INSERT INTO marketdata (instrument_id, high, low, open, close, previous_close, date)
SELECT id, 105.00,  95.00, 100.00, 103.00,  98.00, CURRENT_DATE FROM instruments WHERE ticker = 'PAMP';
INSERT INTO marketdata (instrument_id, high, low, open, close, previous_close, date)
SELECT id, 425.00, 410.00, 420.00, 418.00, 415.00, CURRENT_DATE FROM instruments WHERE ticker = 'YPFD';

-- Seed cash for both demo users so they can trade immediately.
INSERT INTO orders (instrument_id, user_id, side, size, price, type, status, datetime)
SELECT i.id, u.id, 'CASH_IN', 1000000, 1.00, 'MARKET', 'FILLED', CURRENT_TIMESTAMP
FROM instruments i, users u
WHERE i.ticker = 'ARS' AND u.email = 'alice@cocos.capital';

INSERT INTO orders (instrument_id, user_id, side, size, price, type, status, datetime)
SELECT i.id, u.id, 'CASH_IN', 500000, 1.00, 'MARKET', 'FILLED', CURRENT_TIMESTAMP
FROM instruments i, users u
WHERE i.ticker = 'ARS' AND u.email = 'bob@cocos.capital';

-- Give Alice a starting position in GGAL so the portfolio has a non-empty holding.
INSERT INTO orders (instrument_id, user_id, side, size, price, type, status, datetime)
SELECT i.id, u.id, 'BUY', 50, 240.00, 'MARKET', 'FILLED', CURRENT_TIMESTAMP
FROM instruments i, users u
WHERE i.ticker = 'GGAL' AND u.email = 'alice@cocos.capital';
