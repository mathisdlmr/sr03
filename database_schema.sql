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
INSERT INTO users VALUES (2,'Auriane', 'Gaba', 'auriane.gaba@etu.utc.fr','$2a$10$x31VfKgf2YGSpeel6GjMBueIbH5RvADnkxS7HNJjxlGMMp6VYohNO',true, true, NULL); /* password 123456789 */
INSERT INTO users VALUES (3,'Cédric', 'Martinet', 'cedric.matrinet@utc.fr','$2a$10$x31VfKgf2YGSpeel6GjMBueIbH5RvADnkxS7HNJjxlGMMp6VYohNO',true, true, NULL); /* password 123456789 */
INSERT INTO users VALUES (4,'Jean', 'Javascript', 'jean.javascript@mail.fr','$2a$10$x31VfKgf2YGSpeel6GjMBueIbH5RvADnkxS7HNJjxlGMMp6VYohNO',false, true, NULL); /* password 123456789 */
INSERT INTO users VALUES (5,'Garance', 'Go', 'garance.go@mail.fr','$2a$10$x31VfKgf2YGSpeel6GjMBueIbH5RvADnkxS7HNJjxlGMMp6VYohNO',false, true, NULL); /* password 123456789 */
INSERT INTO users VALUES (6,'Phillipe', 'Php', 'phillipe.php@mail.fr','$2a$10$x31VfKgf2YGSpeel6GjMBueIbH5RvADnkxS7HNJjxlGMMp6VYohNO',false, true, NULL); /* password 123456789 */
INSERT INTO users VALUES (7,'Césard', 'Css', 'cesard.css@mail.fr','$2a$10$x31VfKgf2YGSpeel6GjMBueIbH5RvADnkxS7HNJjxlGMMp6VYohNO',false, false, NULL); /* password 123456789 */
INSERT INTO users VALUES (8,'Jada', 'Java', 'jad.java@mail.fr','$2a$10$x31VfKgf2YGSpeel6GjMBueIbH5RvADnkxS7HNJjxlGMMp6VYohNO',false, false, NULL); /* password 123456789 */
INSERT INTO users VALUES (9,'Ruth', 'Rust', 'ruth.rust@mail.fr','$2a$10$x31VfKgf2YGSpeel6GjMBueIbH5RvADnkxS7HNJjxlGMMp6VYohNO',false, false, NULL); /* password 123456789 */

INSERT INTO chat (id, title, description, ends_at, creator_id) VALUES (1, 'Projet Front', 'Discussion autour du frontend', '2026-12-31 23:59:59', 1);
INSERT INTO chat (id, title, description, ends_at, creator_id) VALUES  (2, 'Projet Backend', 'Architecture et API', '2026-12-31 23:59:59', 1);
INSERT INTO chat (id, title, description, ends_at, creator_id) VALUES  (3, 'Projet Infra', 'Infra Kubernetes ou Docker Swarm ?', '2026-12-31 23:59:59', 2);
INSERT INTO chat (id, title, description, ends_at, creator_id) VALUES  (4, 'Migration PHP vers Node', 'Préparation de la migration', '2026-12-31 23:59:59', 2);
INSERT INTO chat (id, title, description, ends_at, creator_id) VALUES  (5, 'Refonte CSS', 'Amélioration du design', '2026-12-31 23:59:59', 3);
INSERT INTO chat (id, title, description, ends_at, creator_id) VALUES  (6, 'Java & Rust Club', 'Comparaison des langages', '2026-12-31 23:59:59', 3);

INSERT INTO invitation (user_id, chat_id) VALUES (2, 1), (3, 1);
INSERT INTO invitation (user_id, chat_id) VALUES (2, 2), (3, 2);
INSERT INTO invitation (user_id, chat_id) VALUES (1, 3), (3, 3);
INSERT INTO invitation (user_id, chat_id) VALUES (1, 4), (3, 4);
INSERT INTO invitation (user_id, chat_id) VALUES (1, 5), (2, 5);
INSERT INTO invitation (user_id, chat_id) VALUES (1, 6), (2, 6);