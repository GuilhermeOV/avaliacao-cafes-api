CREATE DATABASE IF NOT EXISTS avaliacao_cafes
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE avaliacao_cafes;

-- =========================================
-- USUARIOS (login da API com JWT)
-- =========================================
CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    criado_em DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- =========================================
-- CAFES (tabela base fornecida no enunciado)
-- =========================================
CREATE TABLE cafes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    endereco TEXT NOT NULL,
    bairro VARCHAR(100) NOT NULL,
    tipo_cafe_principal VARCHAR(50) NOT NULL
);

-- =========================================
-- REVIEWS (tabela base do enunciado + usuario_id
-- para saber quem e o dono da avaliacao)
-- =========================================
CREATE TABLE reviews (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cafe_id INT NOT NULL,
    usuario_id INT NOT NULL,
    autor VARCHAR(100) NOT NULL,
    nota_bebida INT NOT NULL,
    nota_ambiente INT NOT NULL,
    nota_atendimento INT NOT NULL,
    comentario TEXT,
    criado_em DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cafe_id) REFERENCES cafes(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CHECK (nota_bebida BETWEEN 1 AND 5),
    CHECK (nota_ambiente BETWEEN 1 AND 5),
    CHECK (nota_atendimento BETWEEN 1 AND 5)
);

-- =========================================
-- DADOS PARA TESTE
-- Senha de todos os usuarios de teste: 123456
-- =========================================
INSERT INTO usuarios (nome, email, senha_hash) VALUES
    ('Guilherme Vital', 'guilherme@email.com', 'pbkdf2$65536$bKIb/50zqdHC8QnNvT54pA==$HMC08kl6BOcIf4+IGTurqomRXztD2xsq9OLDzyHwYUk='),
    ('Vitor Gomes',     'vitor@email.com',     'pbkdf2$65536$bKIb/50zqdHC8QnNvT54pA==$HMC08kl6BOcIf4+IGTurqomRXztD2xsq9OLDzyHwYUk=');

INSERT INTO cafes (nome, endereco, bairro, tipo_cafe_principal) VALUES
    ('Grao Nobre',        'Rua das Flores, 120',      'Centro',     'espresso'),
    ('Cold Corner',       'Av. Brasil, 845',          'Jardim',     'cold brew'),
    ('Cafe da Esquina',   'Rua Sete de Setembro, 33', 'Centro',     'coado'),
    ('Barista & Co',      'Rua Goias, 501',           'Vila Nova',  'espresso');

INSERT INTO reviews (cafe_id, usuario_id, autor, nota_bebida, nota_ambiente, nota_atendimento, comentario) VALUES
    (1, 1, 'Guilherme Vital', 5, 4, 5, 'Espresso encorpado e atendimento rapido.'),
    (1, 2, 'Vitor Gomes',     4, 4, 3, 'Bom cafe, mas estava cheio.'),
    (2, 1, 'Guilherme Vital', 5, 5, 4, 'Melhor cold brew da cidade.'),
    (3, 2, 'Vitor Gomes',     3, 5, 5, 'Ambiente muito aconchegante.');
