-- V1__initial_schema.sql
-- Initial PostgreSQL schema for Product Review Application
-- Matches JPA entity definitions with indexes from U30

-- Products table
CREATE TABLE products (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    description     TEXT NOT NULL,
    price           DOUBLE PRECISION NOT NULL,
    image_url       VARCHAR(512),
    average_rating  DOUBLE PRECISION DEFAULT 0.0,
    review_count    INTEGER DEFAULT 0
);

-- Product categories (ElementCollection junction table)
CREATE TABLE product_categories (
    product_id  BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    category    VARCHAR(255) NOT NULL,
    PRIMARY KEY (product_id, category)
);

CREATE INDEX idx_product_categories_product ON product_categories(product_id);

-- Reviews table
CREATE TABLE reviews (
    id              BIGSERIAL PRIMARY KEY,
    reviewer_name   VARCHAR(255),
    comment         TEXT,
    rating          INTEGER,
    helpful_count   INTEGER DEFAULT 0,
    created_at      TIMESTAMP,
    product_id      BIGINT REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX idx_review_product ON reviews(product_id);
CREATE INDEX idx_review_rating ON reviews(rating);
CREATE INDEX idx_review_product_rating ON reviews(product_id, rating);

-- Review votes table
CREATE TABLE review_votes (
    id          BIGSERIAL PRIMARY KEY,
    user_id     VARCHAR(255) NOT NULL,
    review_id   BIGINT NOT NULL,
    CONSTRAINT uk_review_vote_user_review UNIQUE (user_id, review_id),
    CONSTRAINT fk_review_votes_review FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE
);

CREATE INDEX idx_review_vote_user ON review_votes(user_id);
CREATE INDEX idx_review_vote_review ON review_votes(review_id);

-- Wishlist items table
CREATE TABLE wishlist_items (
    id          BIGSERIAL PRIMARY KEY,
    user_id     VARCHAR(255) NOT NULL,
    product_id  BIGINT NOT NULL,
    CONSTRAINT uk_wishlist_user_product UNIQUE (user_id, product_id),
    CONSTRAINT fk_wishlist_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX idx_wishlist_user ON wishlist_items(user_id);
CREATE INDEX idx_wishlist_product ON wishlist_items(product_id);

-- Notifications table
CREATE TABLE notifications (
    id          BIGSERIAL PRIMARY KEY,
    user_id     VARCHAR(255) NOT NULL,
    title       VARCHAR(255) NOT NULL,
    message     TEXT NOT NULL,
    is_read     BOOLEAN DEFAULT FALSE,
    created_at  TIMESTAMP DEFAULT NOW(),
    product_id  BIGINT,
    CONSTRAINT fk_notification_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
);

CREATE INDEX idx_notification_user ON notifications(user_id);
CREATE INDEX idx_notification_user_created ON notifications(user_id, created_at);
