/*
 * Filename: data.sql
 * Created on: October  3, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 *
 * Description: This file contains the initial data to be inserted in the database
 */

INSERT OR IGNORE INTO category (id, name) VALUES
(0, 'Outros'),
(1, 'Alimentação'),
(2, 'Transporte'),
(3, 'Lazer'),
(4, 'Saúde'),
(5, 'Educação'),
(6, 'Moradia'),
(7, 'Serviços'),
(8, 'Pets'),
(9, 'Investimentos'),
(10, 'Rendimentos'),
(11, 'Salário'),
(12, 'Vestuário'),
(13, 'Carro'),
(14, 'Documentos');

INSERT OR IGNORE INTO wallet_type (id, name, icon) VALUES
(0, 'Others', 'others.png'),
(1, 'Broker', 'broker.png'),
(2, 'Checking Account', 'checking.png'),
(3, 'Criptocurrency', 'cryptocurrency.png'),
(4, 'Savings Account', 'savings.png'),
(5, 'Wallet', 'wallet.png');

INSERT OR IGNORE INTO credit_card_operator (id, name, icon) VALUES
(0, 'Others', 'others.png'),
(1, 'Visa', 'visa.png'),
(2, 'MasterCard', 'mastercard.png'),
(3, 'American Express', 'amex.png'),
(4, 'Discover', 'discover.png'),
(5, 'Diners Club', 'diners.png'),
(6, 'JCB', 'jcb.png'),
(7, 'Elo', 'elo.png'),
(8, 'Hipercard', 'hipercard.png');
