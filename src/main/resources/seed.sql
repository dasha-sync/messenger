create table users (
	id serial primary key,
	username text not null,
	email text not null,
	password text
)

create table chats (
	id serial primary key,
	name text not null
)

CREATE TABLE chat_members (
    id serial PRIMARY KEY,
    user_id BIGINT NOT NULL,
    chat_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (chat_id) REFERENCES chats(id) ON DELETE CASCADE,
    UNIQUE (user_id, chat_id)
)

CREATE TABLE user_contacts (
    id serial PRIMARY KEY,
    user_id BIGINT NOT NULL,
    contact_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (contact_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (user_id, contact_id)
)

CREATE TABLE messages (
    id serial PRIMARY KEY,
    user_id BIGINT NOT NULL,
    chat_id BIGINT NOT NULL,
    text text not null,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (chat_id) REFERENCES chats(id) ON DELETE CASCADE,
    UNIQUE (user_id, chat_id)
)

