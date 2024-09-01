/*
 * Filename: WalletServiceTest.java
 * Created on: August 31, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mymoney.app.entities.Transfer;
import com.mymoney.app.entities.Wallet;
import com.mymoney.repositories.TransferRepository;
import com.mymoney.repositories.WalletRepository;
import java.time.LocalDate;
import java.util.jar.Attributes.Name;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest
{
    @Mock
    private WalletRepository m_walletRepository;

    @Mock
    private TransferRepository m_transferRepository;

    @InjectMocks
    private WalletService m_walletService;

    @BeforeAll
    public static void SetUp()
    {
        MockitoAnnotations.openMocks(WalletServiceTest.class);
    }

    @BeforeEach
    public void BeforeEach()
    { }

    @DisplayName("Test if the wallet is created successfully")
    @Test
    public void TestCreateWallet()
    {
        String walletName    = "My Wallet";
        double walletBalance = 1000.0;

        Wallet wallet = new Wallet(walletName, walletBalance);

        when(m_walletRepository.existsById(walletName)).thenReturn(false);
        when(m_walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        m_walletService.CreateWallet(walletName, walletBalance);

        verify(m_walletRepository).save(any(Wallet.class));
    }

    @Test
    public void TestDeleteWalletSoftDelete()
    { }

    @Test
    public void TestDeleteWalletHardDelete()
    { }

    @Test
    public void TestUpdateWalletBalance()
    { }

    @Test
    public void TestTransferMoneySuccess()
    { }

    @Test
    public void TestTransferMoneyFailure()
    { }
}
