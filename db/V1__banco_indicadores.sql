-- =====================================================================
-- Banco proprio do painel de indicadores (PostgreSQL 13+, mesmo servidor do GAW).
-- Rodar UMA vez como administrador do PostgreSQL. As tabelas (CNES_LEITO e CNES_HISTORICO)
-- sao criadas pela propria API ao subir, como na gestao de paineis e nas ferramentas.
-- Troque a senha antes de rodar e guarde-a no .env da API (INDICADORES_DB_PASSWORD).
-- =====================================================================
create role indicadores_app login password 'TROQUE-ESTA-SENHA';
create database indicadores owner indicadores_app encoding 'UTF8';
