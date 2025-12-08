CREATE SCHEMA IF NOT EXISTS bot;


CREATE TABLE IF NOT EXISTS bot.users (
    id BIGINT PRIMARY KEY  
);


CREATE TABLE IF NOT EXISTS bot.schedule (
    user_id BIGINT NOT NULL,
    day_of_week INT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    subject_name VARCHAR(255) NOT NULL,
    order_index INT NOT NULL,
    PRIMARY KEY (user_id, day_of_week, subject_name),
    FOREIGN KEY (user_id) REFERENCES bot.users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bot.homeworks (
    user_id BIGINT NOT NULL,
    subject_name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    entry_order INT NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (user_id, subject_name, entry_order),
    FOREIGN KEY (user_id) REFERENCES bot.users(id) ON DELETE CASCADE    
);


