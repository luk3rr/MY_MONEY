/*
 * Filename: CreditCardDebtRepositoryTest.java
 * Created on: September  5, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mymoney.app.MainApplication;
import com.mymoney.app.entities.CreditCard;
import com.mymoney.app.entities.CreditCardDebt;
import com.mymoney.app.entities.Wallet;
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
 * Tests for the CreditCardDebtRepository
 */
@DataJpaTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { MainApplication.class })
@ActiveProfiles("test")
public class CreditCardDebtRepositoryTest
{

    @Autowired
    private CreditCardDebtRepository m_creditCardDebtRepository;

    @Autowired
    private CreditCardRepository m_creditCardRepository;

    @Autowired
    private WalletRepository m_walletRepository;

    private CreditCard m_creditCard;

    private CreditCard CreateCreditCard(String name, double maxDebt)
    {
        CreditCard m_creditCard = new CreditCard();
        m_creditCard.SetName(name);
        m_creditCard.SetMaxDebt(maxDebt);
        m_creditCard.SetBillingDueDay((short)10);
        m_creditCardRepository.save(m_creditCard);
        m_creditCardRepository.flush();
        return m_creditCard;
    }

    private CreditCardDebt
    CreateCreditCardDebt(CreditCard m_creditCard, double totalAmount, LocalDate date)
    {
        CreditCardDebt creditCardDebt = new CreditCardDebt();
        creditCardDebt.SetCreditCard(m_creditCard);
        creditCardDebt.SetTotalAmount(totalAmount);
        creditCardDebt.SetDate(date);
        m_creditCardDebtRepository.save(creditCardDebt);
        m_creditCardDebtRepository.flush();
        return creditCardDebt;
    }

    @BeforeEach
    public void SetUp()
    {
        // Initialize the credit card
        m_creditCard = CreateCreditCard("CreditCard", 1000.0);
    }

    @Test
    public void TestNoDebt()
    {
        // No debt yet
        assertEquals(0.0,
                     m_creditCardDebtRepository.GetTotalDebt(m_creditCard.GetName()),
                     Constants.EPSILON,
                     "Total debt must be 0.0");
    }

    @Test
    public void TestSingleDebt()
    {
        CreateCreditCardDebt(m_creditCard, 1000.0, LocalDate.now().plusDays(10));

        assertEquals(1000.0,
                     m_creditCardDebtRepository.GetTotalDebt(m_creditCard.GetName()),
                     Constants.EPSILON,
                     "Total debt must be 1000.0");
    }

    @Test
    public void TestMultipleDebts()
    {
        CreateCreditCardDebt(m_creditCard, 1000.0, LocalDate.now().plusDays(10));
        CreateCreditCardDebt(m_creditCard, 500.0, LocalDate.now().plusDays(5));

        assertEquals(1500.0,
                     m_creditCardDebtRepository.GetTotalDebt(m_creditCard.GetName()),
                     Constants.EPSILON,
                     "Total debt must be 1500.0");
    }

    @Test
    public void TestDebtsForMultipleCreditCards()
    {
        CreditCard creditCard1 = CreateCreditCard("CreditCard1", 1000.0);
        CreditCard creditCard2 = CreateCreditCard("CreditCard2", 2000.0);

        CreateCreditCardDebt(creditCard1, 1000.0, LocalDate.now().plusDays(10));
        CreateCreditCardDebt(creditCard2, 500.0, LocalDate.now().plusDays(5));

        assertEquals(1000.0,
                     m_creditCardDebtRepository.GetTotalDebt(creditCard1.GetName()),
                     Constants.EPSILON,
                     "Total debt for CreditCard1 must be 1000.0");

        assertEquals(500.0,
                     m_creditCardDebtRepository.GetTotalDebt(creditCard2.GetName()),
                     Constants.EPSILON,
                     "Total debt for CreditCard2 must be 500.0");
    }
}
