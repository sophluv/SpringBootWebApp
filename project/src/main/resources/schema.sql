-- User table 
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY
    name VARCHAR(255) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(10)
);

-- Media table 
CREATE TABLE IF NOT EXISTS media (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    release_date DATE NOT NULL,
    average_rating DECIMAL(3, 1) CHECK (average_rating BETWEEN 0 AND 10),
    type VARCHAR(20) CHECK (type IN ('Movie', 'TV Show'))
);

-- Users and media relationship table
CREATE TABLE IF NOT EXISTS user_media (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    media_id BIGINT REFERENCES media(id)
);