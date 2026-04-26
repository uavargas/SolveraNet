-- V1__init_schema.sql
-- Tabla inicial para probar la conexión y Flyway

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insertamos los 3 roles definidos en el Documento de Requerimientos
INSERT INTO roles (name, description) VALUES ('ADMIN', 'Administrador del sistema');
INSERT INTO roles (name, description) VALUES ('TECHNICIAN', 'Técnico de soporte TI');
INSERT INTO roles (name, description) VALUES ('USER', 'Usuario final');