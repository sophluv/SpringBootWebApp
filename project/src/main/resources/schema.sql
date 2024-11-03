-- User table to store user details
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(10)
);

-- Media table to store media details
CREATE TABLE IF NOT EXISTS media (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    release_date DATE NOT NULL,
    average_rating DECIMAL(3, 1) CHECK (average_rating BETWEEN 0 AND 10),
    type VARCHAR(20) CHECK (type IN ('Movie', 'TV Show'))
);

-- Junction table for the many-to-many relationship between users and media
CREATE TABLE IF NOT EXISTS user_media (
    user_id INT NOT NULL,
    media_id INT NOT NULL,
    PRIMARY KEY (user_id, media_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (media_id) REFERENCES media(id) ON DELETE CASCADE
);
