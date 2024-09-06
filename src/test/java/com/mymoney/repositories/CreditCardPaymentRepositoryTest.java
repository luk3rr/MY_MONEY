/*
 * Filename: CreditCardPaymentRepositoryTest.java
 * Created on: September  4, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mymoney.app.MainApplication;
import com.mymoney.entities.CreditCard;
import com.mymoney.entities.CreditCardDebt;
import com.mymoney.entities.CreditCardPayment;
import com.mymoney.entities.Wallet;
import com.mymoney.util.Constants;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Tests for the CreditCardPaymentRepository
 */
@DataJpaTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { MainApplication.class })
@ActiveProfiles("test")
public class CreditCardPaymentRepositoryTest
{
    @Autowired
    private CreditCardPaymentRepository m_creditCardPaymentRepository;

    @Autowired
    private CreditCardRepository m_creditCardRepository;

    @Autowired
    private WalletRepository m_walletRepository;

    @Autowired
    private CreditCardDebtRepository m_creditCardDebtRepository;

    private CreditCard m_creditCard1;
    private CreditCard m_creditCard2;
    private Wallet     m_wallet;

    private CreditCard CreateCreditCard(String name, double maxDebt)
    {
        CreditCard creditCard = new CreditCard();
        creditCard.SetName(name);
        creditCard.SetMaxDebt(maxDebt);
        creditCard.SetBillingDueDay((short)10);
        m_creditCardRepository.save(creditCard);
        m_creditCardRepository.flush();
        return creditCard;
    }

    private Wallet CreateWallet(String name, double balance)
    {
        Wallet m_wallet = new Wallet();
        m_wallet.SetName(name);
        m_wallet.SetBalance(balance);
        m_walletRepository.save(m_wallet);
        m_walletRepository.flush();
        return m_wallet;
    }

    private CreditCardDebt CreateCreditCardDebt(CreditCard creditCard,
                                                double     totalAmount)
    {
        CreditCardDebt creditCardDebt = new CreditCardDebt();
        creditCardDebt.SetTotalAmount(totalAmount);
        creditCardDebt.SetCreditCard(creditCard);
        creditCardDebt.SetDate(LocalDate.now().plusDays(5));
        m_creditCardDebtRepository.save(creditCardDebt);
        m_creditCardDebtRepository.flush();
        return creditCardDebt;
    }

    private void
    CreateCreditCardPayment(CreditCardDebt debt, Wallet m_wallet, double amount)
    {
        CreditCardPayment creditCardPayment = new CreditCardPayment();
        creditCardPayment.SetAmount(amount);
        creditCardPayment.SetCreditCardDebt(debt);
        creditCardPayment.SetWallet(m_wallet);
        creditCardPayment.SetDate(LocalDate.now());
        creditCardPayment.SetInstallment((short)1);
        m_creditCardPaymentRepository.save(creditCardPayment);
        m_creditCardPaymentRepository.flush();
    }

    @BeforeEach
    public void SetUp()
    {
        // Initialize CreditCard and Wallet
        m_creditCard1 = CreateCreditCard("CreditCard1", 1000.0);
        m_creditCard2 = CreateCreditCard("CreditCard2", 1000.0);
        m_wallet      = CreateWallet("Wallet", 1000.0);
    }

    @Test
    public void TestNoPayments()
    {
        // No payments yet
        assertEquals(
            0.0,
            m_creditCardPaymentRepository.GetTotalPaidAmount(m_creditCard1.GetName()),
            Constants.EPSILON,
            "Total paid amount must be 0.0");
    }

    @Test
    public void TestSinglePayment()
    {
        // Create CreditCardDebt and Payment
        CreditCardDebt debt = CreateCreditCardDebt(m_creditCard1, 500.0);
        CreateCreditCardPayment(debt, m_wallet, 100.0);

        assertEquals(
            100.0,
            m_creditCardPaymentRepository.GetTotalPaidAmount(m_creditCard1.GetName()),
            Constants.EPSILON,
            "Total paid amount must be 100.0");
    }

    @Test
    public void TestMultiplePayments()
    {
        // Create CreditCardDebt and Payments
        CreditCardDebt debt = CreateCreditCardDebt(m_creditCard1, 500.0);
        CreateCreditCardPayment(debt, m_wallet, 100.0);
        CreateCreditCardPayment(debt, m_wallet, 200.0);

        assertEquals(
            300.0,
            m_creditCardPaymentRepository.GetTotalPaidAmount(m_creditCard1.GetName()),
            Constants.EPSILON,
            "Total paid amount must be 300.0");
    }

    @Test
    public void TestPaymentsForMultipleCreditCards()
    {
        // Create CreditCardDebt and Payments for both credit cards
        CreditCardDebt debt1 = CreateCreditCardDebt(m_creditCard1, 500.0);
        CreditCardDebt debt2 = CreateCreditCardDebt(m_creditCard2, 500.0);

        CreateCreditCardPayment(debt1, m_wallet, 100.0);
        CreateCreditCardPayment(debt1, m_wallet, 200.0);
        CreateCreditCardPayment(debt2, m_wallet, 255.0);

        assertEquals(
            300.0,
            m_creditCardPaymentRepository.GetTotalPaidAmount(m_creditCard1.GetName()),
            Constants.EPSILON,
            "Total paid amount must be 300.0");

        assertEquals(
            255.0,
            m_creditCardPaymentRepository.GetTotalPaidAmount(m_creditCard2.GetName()),
            Constants.EPSILON,
            "Total paid amount must be 255.0");
    }
}
