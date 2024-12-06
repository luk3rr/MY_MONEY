/*
 * Filename: data.sql
 * Created on: October  3, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 *
 * Description: This file contains the initial data to be inserted in the database
 *
 * The data is inserted only if the table is empty
 */

INSERT INTO category (id, name, archived)
SELECT * FROM (SELECT 0, 'Outros', false) WHERE NOT EXISTS (SELECT 1 FROM category)
UNION ALL
SELECT * FROM (SELECT 1, 'Alimentação', false) WHERE NOT EXISTS (SELECT 1 FROM category)
UNION ALL
SELECT * FROM (SELECT 2, 'Transporte', false) WHERE NOT EXISTS (SELECT 1 FROM category)
UNION ALL
SELECT * FROM (SELECT 3, 'Lazer', false) WHERE NOT EXISTS (SELECT 1 FROM category)
UNION ALL
SELECT * FROM (SELECT 4, 'Saúde', false) WHERE NOT EXISTS (SELECT 1 FROM category)
UNION ALL
SELECT * FROM (SELECT 5, 'Educação', false) WHERE NOT EXISTS (SELECT 1 FROM category)
UNION ALL
SELECT * FROM (SELECT 6, 'Moradia', false) WHERE NOT EXISTS (SELECT 1 FROM category)
UNION ALL
SELECT * FROM (SELECT 7, 'Serviços', false) WHERE NOT EXISTS (SELECT 1 FROM category)
UNION ALL
SELECT * FROM (SELECT 8, 'Pets', false) WHERE NOT EXISTS (SELECT 1 FROM category)
UNION ALL
SELECT * FROM (SELECT 9, 'Investimentos', false) WHERE NOT EXISTS (SELECT 1 FROM category)
UNION ALL
SELECT * FROM (SELECT 10, 'Rendimentos', false) WHERE NOT EXISTS (SELECT 1 FROM category)
UNION ALL
SELECT * FROM (SELECT 11, 'Salário', false) WHERE NOT EXISTS (SELECT 1 FROM category)
UNION ALL
SELECT * FROM (SELECT 12, 'Vestuário', false) WHERE NOT EXISTS (SELECT 1 FROM category)
UNION ALL
SELECT * FROM (SELECT 13, 'Carro', false) WHERE NOT EXISTS (SELECT 1 FROM category)
UNION ALL
SELECT * FROM (SELECT 14, 'Documentos', false) WHERE NOT EXISTS (SELECT 1 FROM category);

INSERT INTO wallet_type (id, name, icon)
SELECT * FROM (SELECT 0, 'Others', 'others.png') WHERE NOT EXISTS (SELECT 1 FROM wallet_type)
UNION ALL
SELECT * FROM (SELECT 1, 'Broker', 'broker.png') WHERE NOT EXISTS (SELECT 1 FROM wallet_type)
UNION ALL
SELECT * FROM (SELECT 2, 'Checking Account', 'checking.png') WHERE NOT EXISTS (SELECT 1 FROM wallet_type)
UNION ALL
SELECT * FROM (SELECT 3, 'Criptocurrency', 'cryptocurrency.png') WHERE NOT EXISTS (SELECT 1 FROM wallet_type)
UNION ALL
SELECT * FROM (SELECT 4, 'Savings Account', 'savings.png') WHERE NOT EXISTS (SELECT 1 FROM wallet_type)
UNION ALL
SELECT * FROM (SELECT 5, 'Wallet', 'wallet.png') WHERE NOT EXISTS (SELECT 1 FROM wallet_type)
UNION ALL
SELECT * FROM (SELECT 6, 'Goal', 'goal.png') WHERE NOT EXISTS (SELECT 1 FROM wallet_type);

INSERT INTO credit_card_operator (id, name, icon)
SELECT * FROM (SELECT 0, 'Others', 'others.png') WHERE NOT EXISTS (SELECT 1 FROM credit_card_operator)
UNION ALL
SELECT * FROM (SELECT 1, 'Visa', 'visa.png') WHERE NOT EXISTS (SELECT 1 FROM credit_card_operator)
UNION ALL
SELECT * FROM (SELECT 2, 'MasterCard', 'mastercard.png') WHERE NOT EXISTS (SELECT 1 FROM credit_card_operator)
UNION ALL
SELECT * FROM (SELECT 3, 'American Express', 'amex.png') WHERE NOT EXISTS (SELECT 1 FROM credit_card_operator)
UNION ALL
SELECT * FROM (SELECT 4, 'Discover', 'discover.png') WHERE NOT EXISTS (SELECT 1 FROM credit_card_operator)
UNION ALL
SELECT * FROM (SELECT 5, 'Diners Club', 'diners.png') WHERE NOT EXISTS (SELECT 1 FROM credit_card_operator)
UNION ALL
SELECT * FROM (SELECT 6, 'JCB', 'jcb.png') WHERE NOT EXISTS (SELECT 1 FROM credit_card_operator)
UNION ALL
SELECT * FROM (SELECT 7, 'Elo', 'elo.png') WHERE NOT EXISTS (SELECT 1 FROM credit_card_operator)
UNION ALL
SELECT * FROM (SELECT 8, 'Hipercard', 'hipercard.png') WHERE NOT EXISTS (SELECT 1 FROM credit_card_operator);
