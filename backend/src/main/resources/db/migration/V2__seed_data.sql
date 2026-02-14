-- V2__seed_data.sql
-- Initial product data matching DataInitializer.java

-- ============================================================
-- Products
-- ============================================================
INSERT INTO products (id, name, description, price, image_url, average_rating, review_count) VALUES
(1,  'iPhone 15 Pro',             'The latest iPhone with A17 Pro chip and Titanium design.',                999.99,  'https://images.unsplash.com/photo-1695048133142-1a20484d2569?auto=format&fit=crop&q=80&w=800', 0.0, 0),
(2,  'Samsung Galaxy S24 Ultra',  'AI-powered smartphone with S-Pen.',                                     1199.99, 'https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?auto=format&fit=crop&q=80&w=800', 0.0, 0),
(3,  'Google Pixel 8 Pro',        'The best of Google AI and camera.',                                       899.99, 'https://images.unsplash.com/photo-1696446701796-da61225697cc?auto=format&fit=crop&q=80&w=800', 0.0, 0),
(4,  'MacBook Air M2',            'Strikingly thin design and incredible speed.',                            1099.00, 'https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?auto=format&fit=crop&q=80&w=800', 0.0, 0),
(5,  'Dell XPS 13',               'Compact and powerful ultrabook.',                                         1299.00, 'https://images.unsplash.com/photo-1593642702821-c8da6771f0c6?auto=format&fit=crop&q=80&w=800', 0.0, 0),
(6,  'Asus ROG Zephyrus',         'Gaming power in a slim chassis.',                                        1799.00, 'https://images.unsplash.com/photo-1603302576837-37561b2e2302?auto=format&fit=crop&q=80&w=800', 0.0, 0),
(7,  'iPad Pro 12.9',             'The ultimate iPad experience with M2 chip.',                              1099.00, 'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?auto=format&fit=crop&q=80&w=800', 0.0, 0),
(8,  'Samsung Galaxy Tab S9',     'Dynamic AMOLED 2X display for stunning visuals.',                          799.99, 'https://images.unsplash.com/photo-1585790050230-5dd28404ccb9?auto=format&fit=crop&q=80&w=800', 0.0, 0),
(9,  'Microsoft Surface Pro 9',   'Laptop power, tablet flexibility.',                                       999.99, 'https://images.unsplash.com/photo-1542744094-3a31f272c490?auto=format&fit=crop&q=80&w=800', 0.0, 0),
(10, 'iPad Air 5',                'Light. Bright. Full of might.',                                            599.00, 'https://images.unsplash.com/photo-1589739900243-4b52cd9b104e?auto=format&fit=crop&q=80&w=800', 0.0, 0),
(11, 'Apple Watch Series 9',      'Smarter, brighter, and more powerful.',                                    399.00, 'https://images.unsplash.com/photo-1546868871-7041f2a55e12?auto=format&fit=crop&q=80&w=800', 0.0, 0),
(12, 'Samsung Galaxy Watch 6',    'Advanced sleep coaching and heart monitoring.',                             299.00, 'https://images.unsplash.com/photo-1579586337278-3befd40fd17a?auto=format&fit=crop&q=80&w=800', 0.0, 0),
(13, 'Razer DeathAdder V3',       'Ultra-lightweight ergonomic esports mouse.',                               149.99, 'https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&q=80&w=800', 0.0, 0),
(14, 'Keychron Q1 Pro',           'Custom mechanical keyboard with QMK/VIA support.',                         199.00, 'https://images.unsplash.com/photo-1595225476474-87563907a212?auto=format&fit=crop&q=80&w=800', 0.0, 0),
(15, 'Alienware 34 Monitor',      'Curved QD-OLED gaming monitor.',                                          899.00, 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&q=80&w=800', 0.0, 0),
(16, 'PS5 DualSense Controller',  'Immersive haptic feedback and dynamic triggers.',                           69.99, 'https://images.unsplash.com/photo-1606318801954-d46d46d3360a?auto=format&fit=crop&q=80&w=800', 0.0, 0),
(17, 'Sony WH-1000XM5',           'Industry-leading noise canceling headphones.',                             349.99, 'https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?auto=format&fit=crop&q=80&w=800', 0.0, 0),
(18, 'AirPods Pro 2',             'Adaptive Audio and Active Noise Cancellation.',                            249.00, 'https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?auto=format&fit=crop&q=80&w=800', 0.0, 0),
(19, 'JBL Flip 6',                'Bold sound for every adventure.',                                          129.95, 'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?auto=format&fit=crop&q=80&w=800', 0.0, 0),
(20, 'Sonos Era 100',             'Next-gen acoustics and new levels of connectivity.',                       249.00, 'https://images.unsplash.com/photo-1545454675-3531b543be5d?auto=format&fit=crop&q=80&w=800', 0.0, 0),
(21, 'Anker 737 Power Bank',      'Ultra-powerful two-way charging.',                                         149.99, 'https://images.unsplash.com/photo-1609091839311-d5365f9ff1c5?auto=format&fit=crop&q=80&w=800', 0.0, 0),
(22, 'Logitech MX Master 3S',     'Performance wireless mouse.',                                               99.99, 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?auto=format&fit=crop&q=80&w=800', 0.0, 0),
(23, 'Bellroy Tech Kit',           'Organize your cables and accessories.',                                     59.00, 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&q=80&w=800', 0.0, 0),
(24, 'Nomad Base One',             'Premium MagSafe charger.',                                                  99.95, 'https://images.unsplash.com/photo-1616348436168-de43ad0db179?auto=format&fit=crop&q=80&w=800', 0.0, 0);

-- Reset sequence to continue after seeded IDs
SELECT setval('products_id_seq', 24);

-- ============================================================
-- Product Categories
-- ============================================================
INSERT INTO product_categories (product_id, category) VALUES
-- iPhone 15 Pro
(1, 'Electronics'), (1, 'Smartphones'),
-- Samsung Galaxy S24 Ultra
(2, 'Electronics'), (2, 'Smartphones'),
-- Google Pixel 8 Pro
(3, 'Electronics'), (3, 'Smartphones'),
-- MacBook Air M2
(4, 'Laptops'), (4, 'Electronics'),
-- Dell XPS 13
(5, 'Laptops'), (5, 'Electronics'),
-- Asus ROG Zephyrus
(6, 'Laptops'), (6, 'Gaming'),
-- iPad Pro 12.9
(7, 'Tablets'), (7, 'Electronics'),
-- Samsung Galaxy Tab S9
(8, 'Tablets'), (8, 'Electronics'),
-- Microsoft Surface Pro 9
(9, 'Tablets'), (9, 'Laptops'),
-- iPad Air 5
(10, 'Tablets'), (10, 'Electronics'),
-- Apple Watch Series 9
(11, 'Wearables'), (11, 'Electronics'),
-- Samsung Galaxy Watch 6
(12, 'Wearables'), (12, 'Electronics'),
-- Razer DeathAdder V3
(13, 'Gaming'), (13, 'Accessories'),
-- Keychron Q1 Pro
(14, 'Gaming'), (14, 'Accessories'),
-- Alienware 34 Monitor
(15, 'Gaming'), (15, 'Electronics'),
-- PS5 DualSense Controller
(16, 'Gaming'), (16, 'Accessories'),
-- Sony WH-1000XM5
(17, 'Audio'), (17, 'Electronics'),
-- AirPods Pro 2
(18, 'Audio'), (18, 'Electronics'), (18, 'Accessories'),
-- JBL Flip 6
(19, 'Audio'), (19, 'Electronics'),
-- Sonos Era 100
(20, 'Audio'), (20, 'Electronics'),
-- Anker 737 Power Bank
(21, 'Accessories'), (21, 'Electronics'),
-- Logitech MX Master 3S
(22, 'Accessories'), (22, 'Electronics'),
-- Bellroy Tech Kit
(23, 'Accessories'),
-- Nomad Base One
(24, 'Accessories'), (24, 'Electronics');

-- ============================================================
-- Reviews (deterministic seed matching DataInitializer spirit)
-- ============================================================

-- iPhone 15 Pro (extra reviews for pagination testing)
INSERT INTO reviews (reviewer_name, comment, rating, helpful_count, created_at, product_id) VALUES
('Michael', 'Great product, highly recommended!', 5, 0, NOW(), 1),
('Sarah',   'Not bad, but a bit expensive.', 4, 0, NOW(), 1),
('David',   'Fast delivery and good quality.', 5, 0, NOW(), 1),
('Emma',    'I love the design.', 5, 0, NOW(), 1),
('James',   'Performance is top notch.', 4, 0, NOW(), 1),
('Olivia',  'Battery drains a bit fast.', 3, 0, NOW(), 1),
('Robert',  'Screen is beautiful.', 5, 0, NOW(), 1),
('Sophia',  'Worth every penny.', 5, 0, NOW(), 1),
('William', 'Just okay.', 3, 0, NOW(), 1),
('Isabella','Exceeded my expectations.', 5, 0, NOW(), 1),
('Michael', 'Great product, highly recommended! (Test Review 1)', 5, 0, NOW(), 1),
('Sarah',   'Not bad, but a bit expensive. (Test Review 2)', 4, 0, NOW(), 1),
('David',   'Fast delivery and good quality. (Test Review 3)', 5, 0, NOW(), 1),
('Emma',    'I love the design. (Test Review 4)', 4, 0, NOW(), 1),
('James',   'Performance is top notch. (Test Review 5)', 5, 0, NOW(), 1),
('Olivia',  'Battery drains a bit fast. (Test Review 6)', 3, 0, NOW(), 1),
('Robert',  'Screen is beautiful. (Test Review 7)', 4, 0, NOW(), 1),
('Sophia',  'Worth every penny. (Test Review 8)', 5, 0, NOW(), 1),
('William', 'Just okay. (Test Review 9)', 3, 0, NOW(), 1),
('Isabella','Exceeded my expectations. (Test Review 10)', 5, 0, NOW(), 1),
('Michael', 'Great product, highly recommended! (Test Review 11)', 4, 0, NOW(), 1),
('Sarah',   'Not bad, but a bit expensive. (Test Review 12)', 3, 0, NOW(), 1),
('David',   'Fast delivery and good quality. (Test Review 13)', 5, 0, NOW(), 1),
('Emma',    'I love the design. (Test Review 14)', 5, 0, NOW(), 1),
('James',   'Performance is top notch. (Test Review 15)', 4, 0, NOW(), 1),
('Olivia',  'Battery drains a bit fast. (Test Review 16)', 3, 0, NOW(), 1),
('Robert',  'Screen is beautiful. (Test Review 17)', 5, 0, NOW(), 1),
('Sophia',  'Worth every penny. (Test Review 18)', 4, 0, NOW(), 1),
('William', 'Just okay. (Test Review 19)', 3, 0, NOW(), 1),
('Isabella','Exceeded my expectations. (Test Review 20)', 5, 0, NOW(), 1),

-- Samsung Galaxy S24 Ultra
('Michael', 'Great product, highly recommended!', 5, 0, NOW(), 2),
('Emma',    'I love the design.', 4, 0, NOW(), 2),
('James',   'Performance is top notch.', 5, 0, NOW(), 2),
('Sophia',  'Worth every penny.', 4, 0, NOW(), 2),
('William', 'Just okay.', 3, 0, NOW(), 2),

-- Google Pixel 8 Pro
('Sarah',   'Not bad, but a bit expensive.', 4, 0, NOW(), 3),
('David',   'Fast delivery and good quality.', 5, 0, NOW(), 3),
('Olivia',  'Battery drains a bit fast.', 3, 0, NOW(), 3),
('Robert',  'Screen is beautiful.', 5, 0, NOW(), 3),

-- MacBook Air M2
('Michael', 'Great product, highly recommended!', 5, 0, NOW(), 4),
('Sarah',   'Not bad, but a bit expensive.', 4, 0, NOW(), 4),
('Emma',    'I love the design.', 5, 0, NOW(), 4),
('Isabella','Exceeded my expectations.', 5, 0, NOW(), 4),

-- Dell XPS 13
('David',   'Fast delivery and good quality.', 4, 0, NOW(), 5),
('James',   'Performance is top notch.', 5, 0, NOW(), 5),
('Sophia',  'Worth every penny.', 4, 0, NOW(), 5),

-- Asus ROG Zephyrus
('Michael', 'Great product, highly recommended!', 5, 0, NOW(), 6),
('Robert',  'Screen is beautiful.', 5, 0, NOW(), 6),
('William', 'Just okay.', 3, 0, NOW(), 6),
('Isabella','Exceeded my expectations.', 4, 0, NOW(), 6),

-- iPad Pro 12.9
('Sarah',   'Not bad, but a bit expensive.', 4, 0, NOW(), 7),
('David',   'Fast delivery and good quality.', 5, 0, NOW(), 7),
('Emma',    'I love the design.', 5, 0, NOW(), 7),
('Olivia',  'Battery drains a bit fast.', 3, 0, NOW(), 7),
('Sophia',  'Worth every penny.', 5, 0, NOW(), 7),

-- Samsung Galaxy Tab S9
('Michael', 'Great product, highly recommended!', 4, 0, NOW(), 8),
('James',   'Performance is top notch.', 4, 0, NOW(), 8),
('Robert',  'Screen is beautiful.', 5, 0, NOW(), 8),

-- Microsoft Surface Pro 9
('Sarah',   'Not bad, but a bit expensive.', 3, 0, NOW(), 9),
('David',   'Fast delivery and good quality.', 4, 0, NOW(), 9),
('Emma',    'I love the design.', 5, 0, NOW(), 9),
('William', 'Just okay.', 3, 0, NOW(), 9),

-- iPad Air 5
('Michael', 'Great product, highly recommended!', 5, 0, NOW(), 10),
('Olivia',  'Battery drains a bit fast.', 3, 0, NOW(), 10),
('Isabella','Exceeded my expectations.', 5, 0, NOW(), 10),

-- Apple Watch Series 9
('Sarah',   'Not bad, but a bit expensive.', 4, 0, NOW(), 11),
('David',   'Fast delivery and good quality.', 5, 0, NOW(), 11),
('James',   'Performance is top notch.', 4, 0, NOW(), 11),
('Sophia',  'Worth every penny.', 5, 0, NOW(), 11),

-- Samsung Galaxy Watch 6
('Michael', 'Great product, highly recommended!', 4, 0, NOW(), 12),
('Emma',    'I love the design.', 5, 0, NOW(), 12),
('Robert',  'Screen is beautiful.', 4, 0, NOW(), 12),

-- Razer DeathAdder V3
('David',   'Fast delivery and good quality.', 5, 0, NOW(), 13),
('James',   'Performance is top notch.', 5, 0, NOW(), 13),
('William', 'Just okay.', 3, 0, NOW(), 13),
('Isabella','Exceeded my expectations.', 4, 0, NOW(), 13),

-- Keychron Q1 Pro
('Michael', 'Great product, highly recommended!', 5, 0, NOW(), 14),
('Sarah',   'Not bad, but a bit expensive.', 4, 0, NOW(), 14),
('Olivia',  'Battery drains a bit fast.', 3, 0, NOW(), 14),
('Sophia',  'Worth every penny.', 5, 0, NOW(), 14),

-- Alienware 34 Monitor
('David',   'Fast delivery and good quality.', 5, 0, NOW(), 15),
('Emma',    'I love the design.', 5, 0, NOW(), 15),
('Robert',  'Screen is beautiful.', 5, 0, NOW(), 15),
('William', 'Just okay.', 3, 0, NOW(), 15),

-- PS5 DualSense Controller
('Michael', 'Great product, highly recommended!', 5, 0, NOW(), 16),
('James',   'Performance is top notch.', 5, 0, NOW(), 16),
('Isabella','Exceeded my expectations.', 4, 0, NOW(), 16),

-- Sony WH-1000XM5
('Sarah',   'Not bad, but a bit expensive.', 4, 0, NOW(), 17),
('David',   'Fast delivery and good quality.', 5, 0, NOW(), 17),
('Olivia',  'Battery drains a bit fast.', 3, 0, NOW(), 17),
('Sophia',  'Worth every penny.', 5, 0, NOW(), 17),
('William', 'Just okay.', 3, 0, NOW(), 17),

-- AirPods Pro 2
('Michael', 'Great product, highly recommended!', 5, 0, NOW(), 18),
('Emma',    'I love the design.', 4, 0, NOW(), 18),
('Robert',  'Screen is beautiful.', 4, 0, NOW(), 18),
('Isabella','Exceeded my expectations.', 5, 0, NOW(), 18),

-- JBL Flip 6
('Sarah',   'Not bad, but a bit expensive.', 3, 0, NOW(), 19),
('David',   'Fast delivery and good quality.', 4, 0, NOW(), 19),
('James',   'Performance is top notch.', 5, 0, NOW(), 19),

-- Sonos Era 100
('Michael', 'Great product, highly recommended!', 5, 0, NOW(), 20),
('Olivia',  'Battery drains a bit fast.', 3, 0, NOW(), 20),
('Sophia',  'Worth every penny.', 5, 0, NOW(), 20),
('William', 'Just okay.', 3, 0, NOW(), 20),

-- Anker 737 Power Bank
('David',   'Fast delivery and good quality.', 5, 0, NOW(), 21),
('Emma',    'I love the design.', 4, 0, NOW(), 21),
('Robert',  'Screen is beautiful.', 4, 0, NOW(), 21),

-- Logitech MX Master 3S
('Michael', 'Great product, highly recommended!', 5, 0, NOW(), 22),
('Sarah',   'Not bad, but a bit expensive.', 4, 0, NOW(), 22),
('James',   'Performance is top notch.', 5, 0, NOW(), 22),
('Isabella','Exceeded my expectations.', 5, 0, NOW(), 22),

-- Bellroy Tech Kit
('David',   'Fast delivery and good quality.', 4, 0, NOW(), 23),
('Olivia',  'Battery drains a bit fast.', 3, 0, NOW(), 23),
('Sophia',  'Worth every penny.', 4, 0, NOW(), 23),

-- Nomad Base One
('Michael', 'Great product, highly recommended!', 5, 0, NOW(), 24),
('Emma',    'I love the design.', 4, 0, NOW(), 24),
('William', 'Just okay.', 3, 0, NOW(), 24);

-- ============================================================
-- Update product stats (averageRating and reviewCount)
-- ============================================================
UPDATE products p SET
    review_count = sub.cnt,
    average_rating = ROUND(sub.avg_rating::numeric, 1)
FROM (
    SELECT product_id, COUNT(*) AS cnt, AVG(rating) AS avg_rating
    FROM reviews
    GROUP BY product_id
) sub
WHERE p.id = sub.product_id;
