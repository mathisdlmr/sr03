CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    firstname TEXT NOT NULL,
    lastname TEXT NOT NULL,
    mail TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    admin BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    avatar TEXT /* Utilisé pour stocker un avatar en base64 */
);

CREATE TABLE chat (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    ends_at DATETIME, /* SQLite ne semble pas supporter les opérations du type DEFAULT date('now','+1 hour')... */
    creator_id INTEGER NOT NULL,

    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE invitation (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    chat_id INTEGER NOT NULL,
    invitation_date DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (chat_id) REFERENCES chat(id) ON DELETE CASCADE,

    UNIQUE (user_id, chat_id)
);

INSERT INTO users VALUES (1,'Mathis', 'Delmaere', 'mathis.delmaere@etu.utc.fr','$2a$10$x31VfKgf2YGSpeel6GjMBueIbH5RvADnkxS7HNJjxlGMMp6VYohNO',true, true, NULL); /* password 123456789 */
