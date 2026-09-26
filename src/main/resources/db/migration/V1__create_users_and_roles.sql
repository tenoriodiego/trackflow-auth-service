CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE UNIQUE INDEX uk_users_email
    ON users (LOWER(email));

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL
);

CREATE UNIQUE INDEX uk_roles_name
    ON roles (name);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,

    CONSTRAINT pk_user_roles
        PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
);

INSERT INTO roles (name)
VALUES
    ('ROLE_ADMIN'),
    ('ROLE_OPERATOR'),
    ('ROLE_CUSTOMER');