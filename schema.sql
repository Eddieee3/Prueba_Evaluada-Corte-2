-- crear primero la base:  CREATE DATABASE papeleria_campus;
-- luego ejecutar esto conectado a papeleria_campus

CREATE TABLE IF NOT EXISTS producto (
    id_producto SERIAL PRIMARY KEY,
    codigo      VARCHAR(10)   NOT NULL UNIQUE,
    nombre      VARCHAR(80)   NOT NULL,
    categoria   VARCHAR(30)   NOT NULL,
    precio      NUMERIC(10,2) NOT NULL CHECK (precio > 0),
    stock       INTEGER       NOT NULL CHECK (stock >= 0)
);

-- datos de prueba
INSERT INTO producto (codigo, nombre, categoria, precio, stock) VALUES
('PAP001', 'Cuaderno profesional 100 hojas', 'Papelería', 45.50, 30),
('PAP002', 'Lapiz HB caja 12', 'Papelería', 38.00, 15),
('TEC001', 'Memoria USB 32GB', 'Tecnología', 129.90, 8),
('TEC002', 'Mouse inalambrico', 'Tecnología', 210.00, 0),
('ACC001', 'Mochila escolar', 'Accesorios', 450.00, 5),
('ACC002', 'Termo 750ml', 'Accesorios', 175.25, 12)
ON CONFLICT (codigo) DO NOTHING;
