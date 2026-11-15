-- =====================================================================
-- Catálogo do GAW: aplicação "indicadores" (painel de indicadores do HAT)
--
-- Uma permissão por indicador, no formato indicadores:<módulo>:ver (a mesma
-- lista de hat-api/seguranca/Permissoes.java e hat-dashboard/src/lib/acesso.ts).
-- Pode rodar mais de uma vez: só cria o que ainda não existe.
-- Aplicar no banco de autorização do GAW (gaw_autorizacao), como DBA:
--   docker compose exec -T postgres psql -v ON_ERROR_STOP=1 -U postgres -d gaw_autorizacao < gaw-catalogo-indicadores.sql
-- O cliente do login único (endereços de retorno e segredo) se cadastra pela
-- tela do GAW: Aplicações > Indicadores > Login único.
-- =====================================================================

begin;

insert into aplicacoes (codigo, nome)
select 'indicadores', 'Indicadores do HAT'
where not exists (select 1 from aplicacoes where codigo = 'indicadores');

with app as (select id from aplicacoes where codigo = 'indicadores'),
     m(codigo, nome) as (values
        ('movimentacao', 'Movimentação de leitos (HAT0002)'),
        ('exames-cardiologicos', 'Exames cardiológicos (HAT0005)'),
        ('exames-imagem', 'Exames de imagem (HAT0006)'),
        ('ocupacao-geral', 'Taxa de ocupação operacional (HAT0007)'),
        ('ocupacao-cnes', 'Taxa de ocupação oficial CNES (HAT0008)'),
        ('atendimentos', 'Atendimentos externos (HAT0009)'),
        ('censo', 'Censo hospitalar (HAT0010)'),
        ('centro-cirurgico', 'Centro cirúrgico (HAT0011)'),
        ('hemodinamica', 'Hemodinâmica (HAT0012)'))
insert into modulos (aplicacao_id, codigo, nome)
select app.id, m.codigo, m.nome from app, m
where not exists (select 1 from modulos x where x.aplicacao_id = app.id and x.codigo = m.codigo);

insert into permissoes (aplicacao_id, modulo_id, acao, descricao)
select mo.aplicacao_id, mo.id, 'ver', 'Ver o indicador: ' || mo.nome
from modulos mo join aplicacoes a on a.id = mo.aplicacao_id
where a.codigo = 'indicadores'
  and not exists (select 1 from permissoes p where p.modulo_id = mo.id and p.acao = 'ver');

commit;

select a.codigo || ':' || mo.codigo || ':' || p.acao as permissao, mo.nome
from permissoes p join modulos mo on mo.id = p.modulo_id join aplicacoes a on a.id = p.aplicacao_id
where a.codigo = 'indicadores' order by mo.codigo;
