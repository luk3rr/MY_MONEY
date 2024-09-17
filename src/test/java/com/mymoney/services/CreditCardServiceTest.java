/*
 * Filename: CreditCardServiceTest.java
 * Created on: September  6, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mymoney.entities.Category;
import com.mymoney.entities.CreditCard;
import com.mymoney.entities.CreditCardDebt;
import com.mymoney.entities.CreditCardPayment;
import com.mymoney.repositories.CategoryRepository;
import com.mymoney.repositories.CreditCardDebtRepository;
import com.mymoney.repositories.CreditCardPaymentRepository;
import com.mymoney.repositories.CreditCardRepository;
import com.mymoney.util.Constants;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CreditCardServiceTest
{
    @Mock
    private CreditCardDebtRepository m_creditCardDebtRepository;

    @Mock
    private CreditCardPaymentRepository m_creditCardPaymentRepository;

    @Mock
    private CreditCardRepository m_creditCardRepository;

    @Mock
    private CategoryRepository m_categoryRepository;

    @InjectMocks
    private CreditCardService m_creditCardService;

    private CreditCard m_creditCard;
    private Category   m_category;
    private LocalDate  m_date;
    private String     m_description;

    @BeforeAll
    public static void SetUp()
    {
        MockitoAnnotations.openMocks(CreditCardServiceTest.class);
    }

    @BeforeEach
    public void BeforeEach()
    {
        m_creditCard  = new CreditCard("Credit Card", 10, 1000.0);
        m_category    = new Category("Category");
        m_date        = LocalDate.now();
        m_description = "";
    }

    @Test
    @DisplayName("Test if the credit card is created successfully")
    public void TestCreateCreditCard()
    {
        when(m_creditCardRepository.save(any(CreditCard.class)))
            .thenReturn(m_creditCard);

        when(m_creditCardRepository.existsByName(m_creditCard.GetName()))
            .thenReturn(false);

        m_creditCardService.CreateCreditCard(m_creditCard.GetName(),
                                             m_creditCard.GetBillingDueDay(),
                                             m_creditCard.GetMaxDebt());

        // Capture the credit card that was saved and check if it is correct
        ArgumentCaptor<CreditCard> creditCardCaptor =
            ArgumentCaptor.forClass(CreditCard.class);

        verify(m_creditCardRepository).save(creditCardCaptor.capture());

        CreditCard creditCard = creditCardCaptor.getValue();

        assertEquals(m_creditCard.GetName(), creditCard.GetName());
        assertEquals(m_creditCard.GetBillingDueDay(), creditCard.GetBillingDueDay());
        assertEquals(m_creditCard.GetMaxDebt(), creditCard.GetMaxDebt());
    }

    @Test
    @DisplayName(
        "Test if the credit card is not created when the name is already in use")
    public void
    TestCreateCreditCardAlreadyExists()
    {
        when(m_creditCardRepository.existsByName(m_creditCard.GetName())).thenReturn(true);

        assertThrows(
            RuntimeException.class,
            ()
                -> m_creditCardService.CreateCreditCard(m_creditCard.GetName(),
                                                        m_creditCard.GetBillingDueDay(),
                                                        m_creditCard.GetMaxDebt()));

        // Verify that the credit card was not saved
        verify(m_creditCardRepository, never()).save(any());
    }

    @Test
    @DisplayName(
        "Test if the credit card is not created when the billing due day is invalid")
    public void
    TestCreateCreditCardInvalidDueDate()
    {
        when(m_creditCardRepository.existsByName(m_creditCard.GetName()))
            .thenReturn(false);

        // Case when the billing due day is less than 1
        m_creditCard.SetBillingDueDay(0);

        assertThrows(
            RuntimeException.class,
            ()
                -> m_creditCardService.CreateCreditCard(m_creditCard.GetName(),
                                                        m_creditCard.GetBillingDueDay(),
                                                        m_creditCard.GetMaxDebt()));

        // Case when the billing due day is greater than Constants.MAX_BILLING_DUE_DAY
        m_creditCard.SetBillingDueDay(Constants.MAX_BILLING_DUE_DAY + 1);

        assertThrows(
            RuntimeException.class,
            ()
                -> m_creditCardService.CreateCreditCard(m_creditCard.GetName(),
                                                        m_creditCard.GetBillingDueDay(),
                                                        m_creditCard.GetMaxDebt()));

        // Verify that the credit card was not saved
        verify(m_creditCardRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test if the credit card is not created when the max debt is negative")
    public void TestCreateCreditCardNegativeMaxDebt()
    {
        when(m_creditCardRepository.existsByName(m_creditCard.GetName()))
            .thenReturn(false);

        // Case when the max debt is negative
        m_creditCard.SetMaxDebt(-1.0);

        assertThrows(
            RuntimeException.class,
            ()
                -> m_creditCardService.CreateCreditCard(m_creditCard.GetName(),
                                                        m_creditCard.GetBillingDueDay(),
                                                        m_creditCard.GetMaxDebt()));

        // Verify that the credit card was not saved
        verify(m_creditCardRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test if the credit card is deleted successfully")
    public void TestDeleteCreditCard()
    {
        when(m_creditCardRepository.findById(m_creditCard.GetId()))
            .thenReturn(Optional.of(m_creditCard));

        m_creditCardService.DeleteCreditCard(m_creditCard.GetId());

        // Verify that the credit card was deleted
        verify(m_creditCardRepository).delete(m_creditCard);
    }

    @Test
    @DisplayName("Test if the credit card is not deleted when it does not exist")
    public void TestDeleteCreditCardDoesNotExist()
    {
        when(m_creditCardRepository.findById(m_creditCard.GetId()))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                     () -> m_creditCardService.DeleteCreditCard(m_creditCard.GetId()));

        // Verify that the credit card was not deleted
        verify(m_creditCardRepository, never()).delete(any(CreditCard.class));
    }

    @Test
    @DisplayName(
        "Test if the available credit is returned correctly when there is no debt")
    public void
    TestGetAvailableCredit()
    {
        when(m_creditCardRepository.findById(m_creditCard.GetId()))
            .thenReturn(Optional.of(m_creditCard));

        double availableCredit =
            m_creditCardService.GetAvailableCredit(m_creditCard.GetId());

        assertEquals(m_creditCard.GetMaxDebt(), availableCredit);
    }

    @Test
    @DisplayName(
        "Test if the available credit is returned correctly when there is a debt")
    public void
    TestGetAvailableCreditWithDebt()
    {
        Double previousFreeCredit = m_creditCard.GetMaxDebt();

        when(m_creditCardRepository.findById(m_creditCard.GetId()))
            .thenReturn(Optional.of(m_creditCard));

        when(m_creditCardDebtRepository.GetTotalDebt(m_creditCard.GetId()))
            .thenReturn(previousFreeCredit - previousFreeCredit / 2);

        when(m_creditCardPaymentRepository.GetTotalPaidAmount(m_creditCard.GetId()))
            .thenReturn(0.0);

        double availableCredit =
            m_creditCardService.GetAvailableCredit(m_creditCard.GetId());

        assertEquals(previousFreeCredit / 2, availableCredit, Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if the available credit is returned correctly when there is a "
                 + "debt and payments")
    public void
    TestGetAvailableCreditWithDebtAndPayments()
    {
        m_creditCard.SetMaxDebt(1000.0);

        when(m_creditCardRepository.findById(m_creditCard.GetId()))
            .thenReturn(Optional.of(m_creditCard));

        when(m_creditCardDebtRepository.GetTotalDebt(m_creditCard.GetId()))
            .thenReturn(300.0);

        when(m_creditCardPaymentRepository.GetTotalPaidAmount(m_creditCard.GetId()))
            .thenReturn(100.0);

        double availableCredit =
            m_creditCardService.GetAvailableCredit(m_creditCard.GetId());

        assertEquals(800.0, availableCredit, Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if exception is thrown when the credit card does not exist")
    public void TestGetAvailableCreditDoesNotExist()
    {
        when(m_creditCardRepository.findById(m_creditCard.GetId()))
            .thenReturn(Optional.empty());

        assertThrows(
            RuntimeException.class,
            () -> m_creditCardService.GetAvailableCredit(m_creditCard.GetId()));
    }

    @Test
    @DisplayName("Test if the debt is registered successfully")
    public void TestRegisterDebt()
    {
        m_creditCard.SetMaxDebt(1000.0);

        when(m_creditCardRepository.findById(m_creditCard.GetId()))
            .thenReturn(Optional.of(m_creditCard));

        when(m_categoryRepository.findById(m_category.GetId()))
            .thenReturn(Optional.of(m_category));

        m_creditCardService.RegisterDebt(m_creditCard.GetId(),
                                         m_category,
                                         m_date,
                                         100.0,
                                         1,
                                         m_description);

        // Verify that the debt was registered
        verify(m_creditCardDebtRepository).save(any(CreditCardDebt.class));

        // Verify that the payments were registered
        verify(m_creditCardPaymentRepository).save(any(CreditCardPayment.class));
    }

    @Test
    @DisplayName("Test if exception is thrown when the credit card does not exist")
    public void TestRegisterDebtCreditCardDoesNotExist()
    {
        when(m_creditCardRepository.findById(m_creditCard.GetId()))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                     ()
                         -> m_creditCardService.RegisterDebt(m_creditCard.GetId(),
                                                             m_category,
                                                             m_date,
                                                             100.0,
                                                             1,
                                                             m_description));

        // Verify that the debt was not registered
        verify(m_creditCardDebtRepository, never()).save(any(CreditCardDebt.class));
    }

    @Test
    @DisplayName("Test if exception is thrown when the category does not exist")
    public void TestRegisterDebtCategoryDoesNotExist()
    {
        when(m_creditCardRepository.findById(m_creditCard.GetId()))
            .thenReturn(Optional.of(m_creditCard));

        when(m_categoryRepository.findById(m_category.GetId()))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                     ()
                         -> m_creditCardService.RegisterDebt(m_creditCard.GetId(),
                                                             m_category,
                                                             m_date,
                                                             100.0,
                                                             1,
                                                             m_description));

        // Verify that the debt was not registered
        verify(m_creditCardDebtRepository, never()).save(any(CreditCardDebt.class));

        // Verify that the payments were not registered
        verify(m_creditCardPaymentRepository, never())
            .save(any(CreditCardPayment.class));
    }

    @Test
    @DisplayName("Test if exception is thrown when the value is negative")
    public void TestRegisterDebtNegativeValue()
    {
        when(m_creditCardRepository.findById(m_creditCard.GetId()))
            .thenReturn(Optional.of(m_creditCard));

        when(m_categoryRepository.findById(m_category.GetId()))
            .thenReturn(Optional.of(m_category));

        assertThrows(RuntimeException.class,
                     ()
                         -> m_creditCardService.RegisterDebt(m_creditCard.GetId(),
                                                             m_category,
                                                             m_date,
                                                             -1.0,
                                                             1,
                                                             m_description));

        // Verify that the debt was not registered
        verify(m_creditCardDebtRepository, never()).save(any(CreditCardDebt.class));

        // Verify that the payments were not registered
        verify(m_creditCardPaymentRepository, never())
            .save(any(CreditCardPayment.class));
    }

    @Test
    @DisplayName("Test if exception is thrown when the installment is less than 1")
    public void TestRegisterDebtInvalidInstallment()
    {
        when(m_creditCardRepository.findById(m_creditCard.GetId()))
            .thenReturn(Optional.of(m_creditCard));

        when(m_categoryRepository.findById(m_category.GetId()))
            .thenReturn(Optional.of(m_category));

        assertThrows(RuntimeException.class,
                     ()
                         -> m_creditCardService.RegisterDebt(m_creditCard.GetId(),
                                                             m_category,
                                                             m_date,
                                                             100.0,
                                                             0,
                                                             m_description));

        // Verify that the debt was not registered
        verify(m_creditCardDebtRepository, never()).save(any(CreditCardDebt.class));

        // Verify that the payments were not registered
        verify(m_creditCardPaymentRepository, never())
            .save(any(CreditCardPayment.class));
    }

    @Test
    @DisplayName("Test if exception is thrown when the installment is greater than "
                 + "Constants.MAX_INSTALLMENTS")
    public void
    TestRegisterDebtInvalidInstallment2()
    {
        when(m_creditCardRepository.findById(m_creditCard.GetId()))
            .thenReturn(Optional.of(m_creditCard));

        when(m_categoryRepository.findById(m_category.GetId()))
            .thenReturn(Optional.of(m_category));

        assertThrows(
            RuntimeException.class,
            ()
                -> m_creditCardService.RegisterDebt(m_creditCard.GetId(),
                                                    m_category,
                                                    m_date,
                                                    100.0,
                                                    Constants.MAX_INSTALLMENTS + 1,
                                                    m_description));

        // Verify that the debt was not registered
        verify(m_creditCardDebtRepository, never()).save(any(CreditCardDebt.class));

        // Verify that the payments were not registered
        verify(m_creditCardPaymentRepository, never())
            .save(any(CreditCardPayment.class));
    }

    @Test
    @DisplayName(
        "Test if exception is thrown when the credit card does not have enough "
        + "credit to register the debt")
    public void
    TestRegisterDebtNotEnoughCredit()
    {
        m_creditCard.SetMaxDebt(100.0);

        when(m_creditCardRepository.findById(m_creditCard.GetId()))
            .thenReturn(Optional.of(m_creditCard));

        when(m_categoryRepository.findById(m_category.GetId()))
            .thenReturn(Optional.of(m_category));

        assertThrows(RuntimeException.class,
                     ()
                         -> m_creditCardService.RegisterDebt(m_creditCard.GetId(),
                                                             m_category,
                                                             m_date,
                                                             200.0,
                                                             1,
                                                             m_description));

        // Verify that the debt was not registered
        verify(m_creditCardDebtRepository, never()).save(any(CreditCardDebt.class));

        // Verify that the payments were not registered
        verify(m_creditCardPaymentRepository, never())
            .save(any(CreditCardPayment.class));
    }

    @Test
    @DisplayName("Test if the payment is registered successfully")
    public void TestRegisterPayment()
    {
        // Setup mocks
        when(m_creditCardRepository.findById(m_creditCard.GetId()))
            .thenReturn(Optional.of(m_creditCard));

        when(m_categoryRepository.findById(m_category.GetId()))
            .thenReturn(Optional.of(m_category));

        // Capture the payment that was saved and check if it is correct
        ArgumentCaptor<CreditCardPayment> paymentCaptor =
            ArgumentCaptor.forClass(CreditCardPayment.class);

        m_creditCardService.RegisterDebt(m_creditCard.GetId(),
                                         m_category,
                                         m_date,
                                         100.0,
                                         5,
                                         m_description);

        // Verify if the payment was saved
        verify(m_creditCardPaymentRepository, times(5)).save(paymentCaptor.capture());

        // Get the captured payments and check if they are correct
        List<CreditCardPayment> capturedPayments = paymentCaptor.getAllValues();

        assertEquals(5, capturedPayments.size(), "The number of payments is incorrect");

        double expectedInstallmentValue = 100.0 / 5;

        for (int i = 0; i < capturedPayments.size(); i++)
        {
            CreditCardPayment payment           = capturedPayments.get(i);
            int               installmentNumber = i + 1;

            // Check if the payment amount is correct
            assertEquals(expectedInstallmentValue,
                         payment.GetAmount(),
                         Constants.EPSILON,
                         "The payment amount of installment " + installmentNumber +
                             " is incorrect");

            // Check if the installment number is correct
            assertEquals((short)installmentNumber,
                         payment.GetInstallment(),
                         "The installment number of installment " + installmentNumber +
                             " is incorrect");

            // Check if the payment date is correct
            LocalDate expectedPaymentDate =
                m_date.plusMonths(installmentNumber)
                    .withDayOfMonth(m_creditCard.GetBillingDueDay());

            assertEquals(expectedPaymentDate,
                         payment.GetDate(),
                         "The payment date of installment " + installmentNumber +
                             " is incorrect");

            // Check if wallet is set correctly as null
            assertEquals(null,
                         payment.GetWallet(),
                         "The wallet of installment " + installmentNumber +
                             " is incorrect");
        }
    }
}
