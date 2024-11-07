/*
 * Filename: WalletServiceTest.java
 * Created on: August 31, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import org.mymoney.entities.Wallet;
import org.mymoney.entities.WalletType;
import org.mymoney.repositories.CategoryRepository;
import org.mymoney.repositories.TransferRepository;
import org.mymoney.repositories.WalletRepository;
import org.mymoney.repositories.WalletTransactionRepository;
import org.mymoney.repositories.WalletTypeRepository;
import org.mymoney.util.Constants;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest
{
    @Mock
    private WalletRepository m_walletRepository;

    @Mock
    private WalletTypeRepository m_walletTypeRepository;

    @Mock
    private TransferRepository m_transferRepository;

    @Mock
    private CategoryRepository m_categoryRepository;

    @Mock
    private WalletTransactionRepository m_walletTransactionRepository;

    @InjectMocks
    private WalletService m_walletService;

    private Wallet     m_wallet1;
    private Wallet     m_wallet2;
    private WalletType m_walletType1;
    private WalletType m_walletType2;

    private Wallet CreateWallet(Long id, String name, BigDecimal balance)
    {
        Wallet wallet = new Wallet(id, name, balance);
        return wallet;
    }

    private WalletType CreateWalletType(Long id, String name)
    {
        WalletType walletType = new WalletType(id, name);
        return walletType;
    }

    @BeforeAll
    public static void SetUp()
    {
        MockitoAnnotations.openMocks(WalletServiceTest.class);
    }

    @BeforeEach
    public void BeforeEach()
    {
        m_wallet1 = CreateWallet(1L, "Wallet1", new BigDecimal("1000"));
        m_wallet2 = CreateWallet(2L, "Wallet2", new BigDecimal("2000"));

        m_walletType1 = CreateWalletType(1L, "Type1");
        m_walletType2 = CreateWalletType(2L, "Type2");
    }

    @Test
    @DisplayName("Test if the wallet is created successfully")
    public void TestCreateWallet()
    {
        when(m_walletRepository.existsByName(m_wallet1.GetName())).thenReturn(false);

        m_walletService.CreateWallet(m_wallet1.GetName(), m_wallet1.GetBalance());

        // Capture the wallet object that was saved and check if the values are correct
        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);

        verify(m_walletRepository).save(walletCaptor.capture());

        assertEquals(m_wallet1.GetName(), walletCaptor.getValue().GetName());

        assertEquals(m_wallet1.GetBalance().doubleValue(),
                     walletCaptor.getValue().GetBalance().doubleValue(),
                     Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if the wallet is not created when the name is already in use")
    public void TestCreateWalletAlreadyExists()
    {
        when(m_walletRepository.existsByName(m_wallet1.GetName())).thenReturn(true);

        assertThrows(RuntimeException.class,
                     ()
                         -> m_walletService.CreateWallet(m_wallet1.GetName(),
                                                         m_wallet1.GetBalance()));

        // Verify that the wallet was not saved
        verify(m_walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Test if the wallet is deleted successfully")
    public void TestDeleteWallet()
    {
        when(m_walletRepository.findById(m_wallet1.GetId()))
            .thenReturn(Optional.of(m_wallet1));

        m_walletService.DeleteWallet(m_wallet1.GetId());

        // Verify that the wallet was deleted
        verify(m_walletRepository).delete(m_wallet1);
    }

    @Test
    @DisplayName("Test if the wallet is not deleted when it does not exist")
    public void TestDeleteWalletDoesNotExist()
    {
        when(m_walletRepository.findById(m_wallet1.GetId()))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                     () -> m_walletService.DeleteWallet(m_wallet1.GetId()));

        // Verify that the wallet was not deleted
        verify(m_walletRepository, never()).delete(any(Wallet.class));
    }

    @DisplayName("Test if the wallet is archived successfully")
    @Test
    public void TestArchiveWallet()
    {
        when(m_walletRepository.findById(m_wallet1.GetId()))
            .thenReturn(Optional.of(m_wallet1));

        when(m_walletRepository.save(any(Wallet.class))).thenReturn(m_wallet1);

        m_walletService.ArchiveWallet(m_wallet1.GetId());

        // Check if the wallet was archived
        verify(m_walletRepository).save(m_wallet1);
        assertTrue(m_wallet1.IsArchived());
    }

    @Test
    @DisplayName(
        "Test if exception is thrown when trying to archive a non-existent wallet")
    public void
    TestArchiveWalletDoesNotExist()
    {
        when(m_walletRepository.findById(m_wallet1.GetId()))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                     () -> m_walletService.ArchiveWallet(m_wallet1.GetId()));

        // Verify that the wallet was not archived
        verify(m_walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Test if the wallet has been renamed successfully")
    public void TestRenameWallet()
    {
        when(m_walletRepository.findById(m_wallet1.GetId()))
            .thenReturn(Optional.of(m_wallet1));

        when(m_walletRepository.save(any(Wallet.class))).thenReturn(m_wallet1);

        String newName = "NewWalletName";

        m_walletService.RenameWallet(m_wallet1.GetId(), newName);

        // Check if the wallet was renamed
        verify(m_walletRepository).save(m_wallet1);
        assertEquals(newName, m_wallet1.GetName());
    }

    @Test
    @DisplayName(
        "Test if exception is thrown when trying to rename a non-existent wallet")
    public void
    TestRenameWalletDoesNotExist()
    {
        when(m_walletRepository.findById(m_wallet1.GetId()))
            .thenReturn(Optional.empty());

        assertThrows(
            RuntimeException.class,
            () -> m_walletService.RenameWallet(m_wallet1.GetId(), "NewWalletName"));

        // Verify that the wallet was not renamed
        verify(m_walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Test if exception is thrown when trying to rename a wallet to an "
                 + "already existing name")
    public void
    TestRenameWalletAlreadyExists()
    {
        when(m_walletRepository.findById(m_wallet1.GetId()))
            .thenReturn(Optional.of(m_wallet1));

        when(m_walletRepository.existsByName(m_wallet2.GetName())).thenReturn(true);

        assertThrows(
            RuntimeException.class,
            () -> m_walletService.RenameWallet(m_wallet1.GetId(), m_wallet2.GetName()));

        // Verify that the wallet was not renamed
        verify(m_walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Test if the wallet type is changed successfully")
    public void TestChangeWalletType()
    {
        when(m_walletRepository.findById(m_wallet1.GetId()))
            .thenReturn(Optional.of(m_wallet1));

        when(m_walletTypeRepository.existsById(m_walletType2.GetId())).thenReturn(true);

        when(m_walletRepository.save(any(Wallet.class))).thenReturn(m_wallet1);

        // Define the wallet type of the wallet to be changed
        m_wallet1.SetType(m_walletType1);

        m_walletService.ChangeWalletType(m_wallet1.GetId(), m_walletType2);

        // Check if the wallet type was updated
        verify(m_walletRepository).save(m_wallet1);
        assertEquals(m_walletType2.GetId(), m_wallet1.GetType().GetId());
    }

    @Test
    @DisplayName("Test if exception is thrown when trying to change the wallet type of "
                 + "a non-existent wallet")
    public void
    TestChangeWalletTypeWalletDoesNotExist()
    {
        when(m_walletRepository.findById(m_wallet1.GetId()))
            .thenReturn(Optional.empty());

        assertThrows(
            RuntimeException.class,
            () -> m_walletService.ChangeWalletType(m_wallet1.GetId(), m_walletType2));

        // Verify that the wallet type was not updated
        verify(m_walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Test if exception is thrown when trying to change the wallet type to "
                 + "a non-existent type")
    public void
    TestChangeWalletTypeDoesNotExist()
    {
        when(m_walletRepository.findById(m_wallet1.GetId()))
            .thenReturn(Optional.of(m_wallet1));

        when(m_walletTypeRepository.existsById(m_walletType2.GetId()))
            .thenReturn(false);

        assertThrows(
            RuntimeException.class,
            () -> m_walletService.ChangeWalletType(m_wallet1.GetId(), m_walletType2));

        assertThrows(RuntimeException.class,
                     () -> m_walletService.ChangeWalletType(m_wallet1.GetId(), null));

        // Verify that the wallet type was not updated
        verify(m_walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Test if exception is thrown when trying to change the wallet type of "
                 + "a wallet to the same type")
    public void
    TestChangeWalletTypeSameType()
    {
        // Define the wallet type of the wallet to be changed
        m_wallet1.SetType(m_walletType1);

        when(m_walletRepository.findById(m_wallet1.GetId()))
            .thenReturn(Optional.of(m_wallet1));

        when(m_walletTypeRepository.existsById(m_wallet1.GetType().GetId()))
            .thenReturn(true);

        assertThrows(RuntimeException.class,
                     ()
                         -> m_walletService.ChangeWalletType(m_wallet1.GetId(),
                                                             m_wallet1.GetType()));

        // Verify that the wallet type was not updated
        verify(m_walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Test if the wallet balance is updated successfully")
    public void TestUpdateWalletBalance()
    {
        when(m_walletRepository.findById(m_wallet1.GetId()))
            .thenReturn(Optional.of(m_wallet1));
        when(m_walletRepository.save(any(Wallet.class))).thenReturn(m_wallet1);

        BigDecimal newBalance = new BigDecimal("2000.0");

        m_walletService.UpdateWalletBalance(m_wallet1.GetId(), newBalance);

        // Check if the wallet balance was updated
        verify(m_walletRepository).save(m_wallet1);
        assertEquals(newBalance.doubleValue(),
                     m_wallet1.GetBalance().doubleValue(),
                     Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if exception is thrown when trying to update the balance of a "
                 + "non-existent wallet")
    public void
    TestUpdateWalletBalanceDoesNotExist()
    {
        when(m_walletRepository.findById(m_wallet1.GetId()))
            .thenReturn(Optional.empty());

        assertThrows(
            RuntimeException.class,
            () -> m_walletService.UpdateWalletBalance(m_wallet1.GetId(),
                                                      new BigDecimal("1000.0")));

        // Verify that the wallet balance was not updated
        verify(m_walletRepository, never()).save(any(Wallet.class));
    }
}
