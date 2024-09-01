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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class WalletServiceTest
{
    @Mock
    private WalletRepository m_walletRepository;

    @InjectMocks
    private WalletService m_walletService;

    @BeforeEach
    public void SetUp()
    {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void TestCreateWallet()
    { }

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
