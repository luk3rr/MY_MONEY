/*
 * Filename: data.sql
 * Created on: October  3, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 *
 * Description: This file contains the initial data to be inserted in the database
 */

INSERT OR IGNORE INTO category (id, name, archived) VALUES
(0, 'Outros', false),
(1, 'Alimentação', false),
(2, 'Transporte', false),
(3, 'Lazer', false),
(4, 'Saúde', false),
(5, 'Educação', false),
(6, 'Moradia', false),
(7, 'Serviços', false),
(8, 'Pets', false),
(9, 'Investimentos', false),
(10, 'Rendimentos', false),
(11, 'Salário', false),
(12, 'Vestuário', false),
(13, 'Carro', false),
(14, 'Documentos', false);

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
