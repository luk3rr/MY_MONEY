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

INSERT INTO wallet_transaction (id, wallet_id, category_id, type, status, date, amount, description) VALUES
(1, 1, 1, 'EXPENSE', 'CONFIRMED', '2024-01-15 00:00:00', 50.00, 'Compra de alimentos no supermercado'),
(2, 1, 2, 'EXPENSE', 'CONFIRMED', '2024-01-18 00:00:00', 20.00, 'Táxi para o aeroporto'),
(3, 2, 3, 'EXPENSE', 'PENDING', '2024-02-05 00:00:00', 150.00, 'Reserva de hotel para a viagem'),
(4, 3, 4, 'EXPENSE', 'CONFIRMED', '2024-02-10 00:00:00', 200.00, 'Consulta médica de emergência'),
(5, 4, 5, 'EXPENSE', 'CONFIRMED', '2024-02-12 00:00:00', 500.00, 'Pagamento de curso cancelado'),
(6, 5, 6, 'INCOME', 'CONFIRMED', '2024-02-20 00:00:00', 1000.00, 'Depósito na poupança'),
(7, 1, 7, 'EXPENSE', 'CONFIRMED', '2024-03-01 00:00:00', 100.00, 'Pagamento de serviço de internet'),
(8, 1, 8, 'EXPENSE', 'CONFIRMED', '2024-03-05 00:00:00', 60.00, 'Compra de itens diversos'),
(9, 2, 1, 'INCOME', 'PENDING', '2024-03-08 00:00:00', 250.00, 'Reembolso de alimentação'),
(10, 3, 4, 'EXPENSE', 'CONFIRMED', '2024-03-15 00:00:00', 300.00, 'Compra de medicamentos'),
(11, 4, 3, 'EXPENSE', 'CONFIRMED', '2024-03-20 00:00:00', 75.00, 'Ingresso de cinema para lazer'),
(12, 5, 2, 'INCOME', 'CONFIRMED', '2024-03-25 00:00:00', 120.00, 'Reembolso de transporte de trabalho'),
(13, 1, 5, 'EXPENSE', 'CONFIRMED', '2024-04-03 00:00:00', 45.00, 'Café da manhã em padaria'),
(14, 2, 6, 'INCOME', 'CONFIRMED', '2024-04-07 00:00:00', 350.00, 'Salário recebido'),
(15, 3, 4, 'EXPENSE', 'PENDING', '2024-04-10 00:00:00', 90.00, 'Compra de roupas'),
(16, 4, 1, 'EXPENSE', 'CONFIRMED', '2024-04-12 00:00:00', 250.00, 'Conta de luz'),
(17, 5, 2, 'EXPENSE', 'CONFIRMED', '2024-04-15 00:00:00', 120.00, 'Manutenção de carro'),
(18, 1, 7, 'EXPENSE', 'CONFIRMED', '2024-05-01 00:00:00', 130.00, 'Jantar em restaurante'),
(19, 2, 8, 'EXPENSE', 'CONFIRMED', '2024-05-05 00:00:00', 40.00, 'Compra de material de escritório'),
(20, 3, 3, 'INCOME', 'PENDING', '2024-05-08 00:00:00', 400.00, 'Bônus de desempenho'),
(21, 4, 4, 'EXPENSE', 'CONFIRMED', '2024-05-10 00:00:00', 60.00, 'Abastecimento de carro'),
(22, 5, 1, 'EXPENSE', 'CONFIRMED', '2024-05-15 00:00:00', 300.00, 'Compra de eletrônicos'),
(23, 1, 2, 'INCOME', 'CONFIRMED', '2024-06-01 00:00:00', 200.00, 'Devolução de empréstimo'),
(24, 2, 7, 'EXPENSE', 'PENDING', '2024-06-05 00:00:00', 80.00, 'Serviço de jardinagem'),
(25, 3, 8, 'EXPENSE', 'CONFIRMED', '2024-06-10 00:00:00', 150.00, 'Compra de material esportivo'),
(26, 4, 5, 'INCOME', 'CONFIRMED', '2024-06-15 00:00:00', 500.00, 'Reembolso médico'),
(27, 5, 6, 'EXPENSE', 'CONFIRMED', '2024-06-20 00:00:00', 600.00, 'Pagamento de aluguel'),
(28, 1, 3, 'EXPENSE', 'CONFIRMED', '2024-07-01 00:00:00', 70.00, 'Compra de livro'),
(29, 2, 4, 'INCOME', 'CONFIRMED', '2024-07-05 00:00:00', 320.00, 'Recebimento de serviços prestados'),
(30, 3, 1, 'EXPENSE', 'CONFIRMED', '2024-07-10 00:00:00', 200.00, 'Compra de móveis'),
(31, 4, 2, 'EXPENSE', 'PENDING', '2024-07-15 00:00:00', 60.00, 'Assinatura de serviço de streaming'),
(32, 5, 7, 'EXPENSE', 'CONFIRMED', '2024-07-20 00:00:00', 50.00, 'Presente para amigo'),
(33, 1, 8, 'INCOME', 'CONFIRMED', '2024-08-01 00:00:00', 150.00, 'Devolução de fiança'),
(34, 2, 5, 'EXPENSE', 'CONFIRMED', '2024-08-05 00:00:00', 30.00, 'Compra de material de limpeza'),
(35, 3, 6, 'EXPENSE', 'PENDING', '2024-08-10 00:00:00', 100.00, 'Serviço de manutenção de TI'),
(36, 4, 4, 'EXPENSE', 'CONFIRMED', '2024-08-15 00:00:00', 150.00, 'Conta de água'),
(37, 5, 1, 'EXPENSE', 'CONFIRMED', '2024-08-20 00:00:00', 400.00, 'Viagem de férias'),
(38, 1, 2, 'EXPENSE', 'CONFIRMED', '2024-09-01 00:00:00', 60.00, 'Jantar fora de casa'),
(39, 2, 3, 'EXPENSE', 'PENDING', '2024-09-05 00:00:00', 50.00, 'Compra de remédios'),
(40, 3, 7, 'INCOME', 'CONFIRMED', '2024-09-10 00:00:00', 300.00, 'Pagamento por consultoria'),
(41, 4, 8, 'EXPENSE', 'CONFIRMED', '2024-09-15 00:00:00', 180.00, 'Compra de eletrodomésticos'),
(42, 5, 5, 'EXPENSE', 'CONFIRMED', '2024-09-20 00:00:00', 500.00, 'Curso de aperfeiçoamento profissional'),
(43, 1, 1, 'EXPENSE', 'CONFIRMED', '2023-10-05 00:00:00', 100.00, 'Pagamento de academia'),
(44, 2, 2, 'EXPENSE', 'CONFIRMED', '2023-11-10 00:00:00', 250.00, 'Compra de vestuário'),
(45, 3, 3, 'INCOME', 'CONFIRMED', '2023-12-15 00:00:00', 600.00, 'Bônus de fim de ano'),
(46, 4, 4, 'EXPENSE', 'CONFIRMED', '2023-11-20 00:00:00', 50.00, 'Presente de aniversário'),
(47, 5, 5, 'EXPENSE', 'CONFIRMED', '2023-10-25 00:00:00', 80.00, 'Assinatura de revista'),
(48, 1, 6, 'EXPENSE', 'CONFIRMED', '2023-12-30 00:00:00', 300.00, 'Jantar de Réveillon'),
(49, 2, 7, 'INCOME', 'CONFIRMED', '2023-11-05 00:00:00', 1200.00, 'Pagamento de freelance'),
(50, 3, 8, 'EXPENSE', 'CONFIRMED', '2024-01-07 00:00:00', 200.00, 'Compra de eletrônicos'),
(51, 4, 1, 'EXPENSE', 'PENDING', '2024-01-15 00:00:00', 40.00, 'Manutenção de bicicleta'),
(52, 5, 2, 'EXPENSE', 'CONFIRMED', '2024-02-10 00:00:00', 75.00, 'Compras em loja de conveniência'),
(53, 1, 3, 'INCOME', 'CONFIRMED', '2024-03-05 00:00:00', 500.00, 'Reembolso de despesas corporativas'),
(54, 2, 4, 'EXPENSE', 'CONFIRMED', '2024-04-12 00:00:00', 100.00, 'Conta de gás'),
(55, 3, 5, 'INCOME', 'CONFIRMED', '2024-05-08 00:00:00', 250.00, 'Pagamento por serviços prestados'),
(56, 4, 6, 'EXPENSE', 'CONFIRMED', '2024-06-15 00:00:00', 400.00, 'Compra de ingressos para show'),
(57, 5, 7, 'EXPENSE', 'CONFIRMED', '2024-07-20 00:00:00', 130.00, 'Manutenção de jardim'),
(58, 1, 8, 'EXPENSE', 'CONFIRMED', '2024-08-03 00:00:00', 75.00, 'Compra de material escolar'),
(59, 2, 1, 'INCOME', 'CONFIRMED', '2024-09-10 00:00:00', 800.00, 'Comissão de vendas'),
(60, 3, 2, 'EXPENSE', 'CONFIRMED', '2024-09-15 00:00:00', 100.00, 'Assinatura de serviços online');

-- Dívidas passadas (já pagas)
INSERT INTO credit_card_debt (id, crc_id, category_id, date, total_amount, description)
VALUES
(1, 1, 1, '2023-05-10', 200.00, 'Compra de supermercado'),
(2, 1, 2, '2023-06-10', 150.00, 'Transporte público'),
(3, 2, 3, '2023-07-15', 500.00, 'Viagem de férias'),
(4, 3, 4, '2023-08-20', 300.00, 'Consulta médica'),
(5, 4, 5, '2023-09-25', 250.00, 'Material escolar');

-- Pagamentos para as dívidas passadas
INSERT INTO credit_card_payment (id, wallet_id, debt_id, date, amount, installment)
VALUES
(1, 1, 1, '2023-05-11', 100.00, 1),
(2, 1, 1, '2023-06-11', 100.00, 2),
(3, 1, 2, '2023-07-11', 75.00, 1),
(4, 1, 2, '2023-08-11', 75.00, 2),
(5, 2, 3, '2023-08-11', 250.00, 1),
(6, 2, 3, '2023-09-11', 250.00, 2),
(7, 3, 4, '2023-09-11', 150.00, 1),
(8, 3, 4, '2023-10-11', 150.00, 2),
(9, 4, 5, '2023-10-11', 125.00, 1),
(10, 4, 5, '2023-11-11', 125.00, 2);

-- Dívidas futuras (não pagas)
INSERT INTO credit_card_debt (id, crc_id, category_id, date, total_amount, description)
VALUES
(6, 1, 1, '2024-09-10', 180.00, 'Compra de supermercado'),
(7, 2, 2, '2024-09-15', 400.00, 'Passagem de ônibus'),
(8, 3, 3, '2025-09-20', 600.00, 'Viagem planejada'),
(9, 4, 4, '2025-09-25', 320.00, 'Consulta médica'),
(10, 5, 5, '2025-09-30', 270.00, 'Compra de livros');

-- Pagamentos para as dívidas futuras
INSERT INTO credit_card_payment (id, wallet_id, debt_id, date, amount, installment)
VALUES
(11, NULL, 6, '2024-10-11', 90.00, 1),
(12, NULL, 6, '2024-11-11', 90.00, 2),
(13, NULL, 7, '2024-10-11', 200.00, 1),
(14, NULL, 7, '2024-11-11', 200.00, 2),
(15, NULL, 8, '2024-10-11', 300.00, 1),
(16, NULL, 8, '2024-11-11', 300.00, 2),
(17, NULL, 9, '2024-10-11', 320.00, 1),
(19, NULL, 10, '2024-10-11', 27.00, 1),
(20, NULL, 10, '2024-11-11', 27.00, 2),
(21, NULL, 10, '2024-12-11', 27.00, 3),
(22, NULL, 10, '2025-01-11', 27.00, 4),
(23, NULL, 10, '2025-02-11', 27.00, 5),
(24, NULL, 10, '2025-03-11', 27.00, 6),
(25, NULL, 10, '2025-04-11', 27.00, 7),
(26, NULL, 10, '2025-05-11', 27.00, 8),
(27, NULL, 10, '2025-06-11', 27.00, 9),
(28, NULL, 10, '2025-08-11', 27.00, 10);
