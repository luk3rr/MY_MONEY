/*
 * Filename: dummy-data.sql
 * Created on: September 21, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

INSERT INTO credit_card_operator (id, name, icon_path) VALUES
(1, 'Visa', '/icons/visa.png'),
(2, 'MasterCard', '/icons/mastercard.png'),
(3, 'American Express', '/icons/amex.png'),
(4, 'Discover', '/icons/discover.png'),
(5, 'Diners Club', '/icons/diners.png');

INSERT INTO credit_card (id, operator_id, name, billing_due_day, max_debt, last_four_digits) VALUES
(1, 1, 'Visa Gold', 10, 5000.00, '1234'),
(2, 2, 'MasterCard Platinum', 15, 7500.00, '5678'),
(3, 3, 'Amex Green', 20, 3000.00, '9101'),
(4, 4, 'Discover Cashback', 25, 2000.00, '1121'),
(5, 5, 'Diners Club Rewards', 30, 4000.00, '3141');

INSERT INTO category (id, name) VALUES
(1, 'Alimentação'),
(2, 'Transporte'),
(3, 'Lazer'),
(4, 'Saúde'),
(5, 'Educação'),
(6, 'Moradia'),
(7, 'Serviços'),
(8, 'Outros');

INSERT INTO wallet (id, name, balance, archived) VALUES
(1, 'Principal', 1000.00, false),
(2, 'Viagem', 500.00, false),
(3, 'Emergência', 250.00, false),
(4, 'Lazer', 150.00, false),
(5, 'Economia', 2000.00, false);
