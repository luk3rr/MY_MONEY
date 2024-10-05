/*
 * Filename: CreditCardDebtRepositoryTest.java
 * Created on: September  5, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mymoney.app.MainApplication;
import com.mymoney.entities.Category;
import com.mymoney.entities.CreditCard;
import com.mymoney.entities.CreditCardDebt;
import com.mymoney.entities.CreditCardOperator;
import com.mymoney.util.Constants;
import java.time.LocalDateTime;
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
    private CreditCardOperatorRepository m_creditCardOperatorRepository;

    @Autowired
    private CategoryRepository m_categoryRepository;

    private CreditCard         m_creditCard;
    private CreditCardOperator m_crcOperator;

    private CreditCard
    CreateCreditCard(String name, CreditCardOperator operator, Double maxDebt)
    {
        CreditCard creditCard = new CreditCard();
        creditCard.SetName(name);
        creditCard.SetMaxDebt(maxDebt);
        creditCard.SetBillingDueDay(10);
        creditCard.SetOperator(operator);
        m_creditCardRepository.save(creditCard);
        return creditCard;
    }

    private CreditCardOperator CreateCreditCardOperator(String name)
    {
        CreditCardOperator creditCardOperator = new CreditCardOperator();
        creditCardOperator.SetName(name);
        creditCardOperator.SetIcon("");
        m_creditCardOperatorRepository.save(creditCardOperator);
        return creditCardOperator;
    }

    private Category CreateCategory(String name)
    {
        Category category = new Category(name);
        m_categoryRepository.save(category);
        return category;
    }

    private CreditCardDebt CreateCreditCardDebt(CreditCard    m_creditCard,
                                                Double        totalAmount,
                                                LocalDateTime date)
    {
        CreditCardDebt creditCardDebt = new CreditCardDebt();
        creditCardDebt.SetCreditCard(m_creditCard);
        creditCardDebt.SetTotalAmount(totalAmount);
        creditCardDebt.SetDate(date);
        creditCardDebt.SetCategory(CreateCategory("category"));
        m_creditCardDebtRepository.save(creditCardDebt);
        return creditCardDebt;
    }

    @BeforeEach
    public void SetUp()
    {
        // Initialize the credit card
        m_crcOperator = CreateCreditCardOperator("Operator");
        m_creditCard  = CreateCreditCard("CreditCard", m_crcOperator, 1000.0);
    }

    @Test
    public void TestNoDebt()
    {
        // No debt yet
        assertEquals(0.0,
                     m_creditCardDebtRepository.GetTotalDebt(m_creditCard.GetId()),
                     Constants.EPSILON,
                     "Total debt must be 0.0");
    }

    @Test
    public void TestSingleDebt()
    {
        CreateCreditCardDebt(m_creditCard, 1000.0, LocalDateTime.now().plusDays(10));

        assertEquals(1000.0,
                     m_creditCardDebtRepository.GetTotalDebt(m_creditCard.GetId()),
                     Constants.EPSILON,
                     "Total debt must be 1000.0");
    }

    @Test
    public void TestMultipleDebts()
    {
        CreateCreditCardDebt(m_creditCard, 1000.0, LocalDateTime.now().plusDays(10));
        CreateCreditCardDebt(m_creditCard, 500.0, LocalDateTime.now().plusDays(5));

        assertEquals(1500.0,
                     m_creditCardDebtRepository.GetTotalDebt(m_creditCard.GetId()),
                     Constants.EPSILON,
                     "Total debt must be 1500.0");
    }

    @Test
    public void TestDebtsForMultipleCreditCards()
    {
        CreditCard creditCard1 = CreateCreditCard("CreditCard1", m_crcOperator, 1000.0);
        CreditCard creditCard2 = CreateCreditCard("CreditCard2", m_crcOperator, 2000.0);

        CreateCreditCardDebt(creditCard1, 1000.0, LocalDateTime.now().plusDays(10));
        CreateCreditCardDebt(creditCard2, 500.0, LocalDateTime.now().plusDays(5));

        assertEquals(1000.0,
                     m_creditCardDebtRepository.GetTotalDebt(creditCard1.GetId()),
                     Constants.EPSILON,
                     "Total debt for CreditCard1 must be 1000.0");

        assertEquals(500.0,
                     m_creditCardDebtRepository.GetTotalDebt(creditCard2.GetId()),
                     Constants.EPSILON,
                     "Total debt for CreditCard2 must be 500.0");
    }
}
