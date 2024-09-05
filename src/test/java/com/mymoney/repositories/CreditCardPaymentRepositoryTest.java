/*
 * Filename: CreditCardPaymentRepositoryTest.java
 * Created on: September  4, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mymoney.app.MainApplication;
import com.mymoney.app.entities.CreditCard;
import com.mymoney.app.entities.CreditCardDebt;
import com.mymoney.app.entities.CreditCardPayment;
import com.mymoney.app.entities.Wallet;
import com.mymoney.util.Constants;
import jakarta.annotation.security.RunAs;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Tests for the CreditCardPaymentRepository class
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

    @BeforeEach
    public void SetUp()
    { }

    @Test
    public void TestGetTotalPaidAmount()
    {
        CreditCard creditCard = new CreditCard();
        creditCard.SetName("Credit Card");
        creditCard.SetMaxDebt(1000.0);
        creditCard.SetBillingDueDay((short)10);
        m_creditCardRepository.save(creditCard);
        m_creditCardRepository.flush();

        Wallet wallet = new Wallet();
        wallet.SetName("Wallet");
        wallet.SetBalance(1000.0);
        m_walletRepository.save(wallet);
        m_walletRepository.flush();

        CreditCardDebt creditCardDebt = new CreditCardDebt();
        creditCardDebt.SetTotalAmount(500.0);
        creditCardDebt.SetCreditCard(creditCard);
        creditCardDebt.SetDate(LocalDate.now().plusDays(5));
        m_creditCardDebtRepository.save(creditCardDebt);
        m_creditCardDebtRepository.flush();

        CreditCardPayment creditCardPayment = new CreditCardPayment();
        creditCardPayment.SetAmount(100.0);
        creditCardPayment.SetCreditCardDebt(creditCardDebt);
        creditCardPayment.SetWallet(wallet);
        creditCardPayment.SetDate(LocalDate.now());
        creditCardPayment.SetInstallment((short)1);
        m_creditCardPaymentRepository.save(creditCardPayment);
        m_creditCardPaymentRepository.flush();

        Double totalPaidAmount =
            m_creditCardPaymentRepository.GetTotalPaidAmount("Credit Card");
        assertEquals(100.0, totalPaidAmount, Constants.EPSILON);
    }
}
