-- ── Users ─────────────────────────────────────────────────────────────────
-- password = 'password123' BCrypt encoded
INSERT INTO users (email, password, name, role, created_at)
VALUES
    ('admin@ecommerce.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin User', 'ADMIN', NOW()),
    ('john@example.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'John Doe',   'USER',  NOW()),
    ('jane@example.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Jane Smith', 'USER',  NOW())
    ON CONFLICT (email) DO NOTHING;

-- ── Products ───────────────────────────────────────────────────────────────
INSERT INTO products (name, description, category, image_url, price, stock, rating, review_count)
VALUES
    -- TECH
    ('MacBook Pro 14"',   'Apple M3 chip, 16GB RAM, 512GB SSD',         'TECH',    'macbook.jpg',   1999.00, 15,  4.8, 120),
    ('Samsung Galaxy S24','6.2" AMOLED, 256GB, Android 14',             'TECH',    'galaxy.jpg',     899.00, 30,  4.6,  95),
    ('Sony WH-1000XM5',   'Industry-leading noise cancelling headset',  'TECH',    'sony.jpg',       349.00, 50,  4.7,  80),
    ('iPad Air M2',       '11" Liquid Retina, 256GB, Wi-Fi',            'TECH',    'ipad.jpg',       749.00, 20,  4.5,  60),
    ('Dell XPS 15',       'Intel i9, RTX 4060, 32GB RAM',               'TECH',    'dell.jpg',      1799.00, 10,  4.4,  45),

    -- APPAREL
    ('Nike Air Max 270',  'Lightweight running shoes, sizes 7-13',      'APPAREL', 'nike.jpg',       150.00, 100, 4.3,  200),
    ('Levi 501 Jeans',    'Classic straight fit, indigo blue',          'APPAREL', 'levis.jpg',       79.00, 200, 4.2,  310),
    ('North Face Parka',  'Waterproof, windproof, -20°C rated',         'APPAREL', 'northface.jpg',  299.00,  40, 4.6,  88),

    -- BOOKS
    ('Clean Code',        'Robert C. Martin — software craftsmanship',  'BOOKS',   'cleancode.jpg',   35.00, 500, 4.7,  950),
    ('System Design Interview', 'Alex Xu — Vol 1 & 2 bundle',          'BOOKS',   'sysdesign.jpg',   45.00, 300, 4.8,  620),
    ('The Pragmatic Programmer', '20th Anniversary Edition',            'BOOKS',   'pragprog.jpg',    40.00, 400, 4.6,  530),

    -- HOME
    ('Dyson V15 Detect',  'Laser dust detection, 60min battery',        'HOME',    'dyson.jpg',       749.00, 25,  4.7,  175),
    ('Instant Pot Duo 7-in-1', '6Qt multi-use pressure cooker',        'HOME',    'instantpot.jpg',   99.00, 80,  4.5,  420)
    ON CONFLICT DO NOTHING;