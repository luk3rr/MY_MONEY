/*
 * Filename: GoalServiceTest.java
 * Created on: December  7, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.moinex.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import org.moinex.entities.Goal;
import org.moinex.entities.WalletType;
import org.moinex.repositories.GoalRepository;
import org.moinex.repositories.WalletRepository;
import org.moinex.repositories.WalletTypeRepository;
import org.moinex.util.Constants;

@ExtendWith(MockitoExtension.class)
public class GoalServiceTest
{
    @Mock
    private WalletRepository m_walletRepository;

    @Mock
    private WalletTypeRepository m_walletTypeRepository;

    @Mock
    private GoalRepository m_goalRepository;

    @InjectMocks
    private GoalService m_goalService;

    private Goal       m_goal;
    private WalletType m_walletType;

    private Goal CreateGoal(Long          id,
                            String        name,
                            BigDecimal    initialBalance,
                            BigDecimal    targetBalance,
                            LocalDateTime targetDate,
                            String        motivation)
    {
        Goal goal = new Goal(id,
                             name,
                             initialBalance,
                             targetBalance,
                             targetDate,
                             motivation,
                             m_walletType);
        return goal;
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
        m_goal = CreateGoal(1L,
                            "Goal1",
                            BigDecimal.valueOf(100.0),
                            BigDecimal.valueOf(200.0),
                            LocalDateTime.now().plusDays(30),
                            "Motivation1");

        m_walletType = CreateWalletType(1L, Constants.GOAL_DEFAULT_WALLET_TYPE_NAME);
    }

    @Test
    @DisplayName("Test if the goal is created successfully")
    public void TestCreateGoal()
    {
        when(m_goalRepository.existsByName(m_goal.GetName())).thenReturn(false);

        when(m_walletTypeRepository.findByName(Constants.GOAL_DEFAULT_WALLET_TYPE_NAME))
            .thenReturn(Optional.of(m_walletType));

        m_goalService.CreateGoal(m_goal.GetName(),
                                 m_goal.GetInitialBalance(),
                                 m_goal.GetTargetBalance(),
                                 m_goal.GetTargetDate().toLocalDate(),
                                 m_goal.GetMotivation());

        // Capture the wallet object that was saved and check if the values are correct
        ArgumentCaptor<Goal> goalCaptor = ArgumentCaptor.forClass(Goal.class);

        verify(m_goalRepository).save(goalCaptor.capture());

        assertEquals(m_goal.GetName(), goalCaptor.getValue().GetName());

        assertEquals(m_goal.GetInitialBalance(),
                     goalCaptor.getValue().GetInitialBalance());

        assertEquals(m_goal.GetTargetBalance(),
                     goalCaptor.getValue().GetTargetBalance());
    }

    @Test
    @DisplayName("Test if the goal is not created when the name already exists")
    public void TestCreateGoalAlreadyExists()
    {
        when(m_goalRepository.existsByName(m_goal.GetName())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            m_goalService.CreateGoal(m_goal.GetName(),
                                     m_goal.GetInitialBalance(),
                                     m_goal.GetTargetBalance(),
                                     m_goal.GetTargetDate().toLocalDate(),
                                     m_goal.GetMotivation());
        });

        verify(m_goalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test if the goal is not created when the initial balance is negative")
    public void TestCreateGoalNegativeInitialBalance()
    {
        when(m_goalRepository.existsByName(m_goal.GetName())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            m_goalService.CreateGoal(m_goal.GetName(),
                                     BigDecimal.valueOf(-1.0),
                                     m_goal.GetTargetBalance(),
                                     m_goal.GetTargetDate().toLocalDate(),
                                     m_goal.GetMotivation());
        });

        verify(m_goalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test if the goal is not created when the target balance is negative")
    public void TestCreateGoalNegativeTargetBalance()
    {
        when(m_goalRepository.existsByName(m_goal.GetName())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            m_goalService.CreateGoal(m_goal.GetName(),
                                     m_goal.GetInitialBalance(),
                                     BigDecimal.valueOf(-1.0),
                                     m_goal.GetTargetDate().toLocalDate(),
                                     m_goal.GetMotivation());
        });

        verify(m_goalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test if the goal is not created when target balance zero")
    public void TestCreateGoalZeroBalance()
    {
        when(m_goalRepository.existsByName(m_goal.GetName())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            m_goalService.CreateGoal(m_goal.GetName(),
                                     BigDecimal.valueOf(0.0),
                                     BigDecimal.valueOf(0.0),
                                     m_goal.GetTargetDate().toLocalDate(),
                                     m_goal.GetMotivation());
        });

        verify(m_goalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test if the goal is not created when the target date is in the past")
    public void TestCreateGoalTargetDateInPast()
    {
        when(m_goalRepository.existsByName(m_goal.GetName())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            m_goalService.CreateGoal(m_goal.GetName(),
                                     m_goal.GetInitialBalance(),
                                     m_goal.GetTargetBalance(),
                                     LocalDate.now().minusDays(1),
                                     m_goal.GetMotivation());
        });

        verify(m_goalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test if the goal is not created when the target balance is less "
                 + "than the initial balance")
    public void
    TestCreateGoalTargetBalanceLessThanInitialBalance()
    {
        when(m_goalRepository.existsByName(m_goal.GetName())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            m_goalService.CreateGoal(
                m_goal.GetName(),
                m_goal.GetInitialBalance(),
                m_goal.GetInitialBalance().subtract(BigDecimal.valueOf(1.0)),
                m_goal.GetTargetDate().toLocalDate(),
                m_goal.GetMotivation());
        });

        verify(m_goalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test if the goal is not created when the wallet type does not exist")
    public void TestCreateGoalWalletTypeDoesNotExist()
    {
        when(m_goalRepository.existsByName(m_goal.GetName())).thenReturn(false);

        when(m_walletTypeRepository.findByName(Constants.GOAL_DEFAULT_WALLET_TYPE_NAME))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            m_goalService.CreateGoal(m_goal.GetName(),
                                     m_goal.GetInitialBalance(),
                                     m_goal.GetTargetBalance(),
                                     m_goal.GetTargetDate().toLocalDate(),
                                     m_goal.GetMotivation());
        });

        verify(m_goalRepository, never()).save(any());
    }

    // TODO: Delete goal tests

    @Test
    @DisplayName("Test if the goal is archived successfully")
    public void TestArchiveGoal()
    {
        when(m_goalRepository.findById(m_goal.GetId())).thenReturn(Optional.of(m_goal));

        m_goalService.ArchiveGoal(m_goal.GetId());

        verify(m_goalRepository).save(m_goal);
        assertTrue(m_goal.IsArchived());
    }

    @Test
    @DisplayName("Test if the goal is unarchived successfully")
    public void TestUnarchiveGoal()
    {
        m_goal.SetArchived(true);

        when(m_goalRepository.findById(m_goal.GetId())).thenReturn(Optional.of(m_goal));

        m_goalService.UnarchiveGoal(m_goal.GetId());

        verify(m_goalRepository).save(m_goal);
        assertTrue(!m_goal.IsArchived());
    }

    @Test
    @DisplayName("Test if the goal is not archived when it does not exist")
    public void TestArchiveGoalDoesNotExist()
    {
        when(m_goalRepository.findById(m_goal.GetId())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                     () -> { m_goalService.ArchiveGoal(m_goal.GetId()); });

        verify(m_goalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test if the goal is not unarchived when it does not exist")
    public void TestUnarchiveGoalDoesNotExist()
    {
        when(m_goalRepository.findById(m_goal.GetId())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                     () -> { m_goalService.UnarchiveGoal(m_goal.GetId()); });

        verify(m_goalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test if the goal is renamed successfully")
    public void TestRenameGoal()
    {
        when(m_goalRepository.findById(m_goal.GetId())).thenReturn(Optional.of(m_goal));

        String newName = m_goal.GetName() + " Renamed";
        m_goalService.RenameGoal(m_goal.GetId(), newName);

        verify(m_goalRepository).save(m_goal);
        assertEquals(newName, m_goal.GetName());
    }

    @Test
    @DisplayName("Test if the goal is not renamed when it does not exist")
    public void TestRenameGoalDoesNotExist()
    {
        when(m_goalRepository.findById(m_goal.GetId())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                     () -> { m_goalService.RenameGoal(m_goal.GetId(), "New Name"); });

        verify(m_goalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test if the goal is not renamed when the new name already exists")
    public void TestRenameGoalAlreadyExists()
    {
        when(m_goalRepository.findById(m_goal.GetId())).thenReturn(Optional.of(m_goal));

        when(m_goalRepository.existsByName(m_goal.GetName() + " Renamed"))
            .thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            m_goalService.RenameGoal(m_goal.GetId(), m_goal.GetName() + " Renamed");
        });

        verify(m_goalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test if the initial balance is updated successfully")
    public void TestUpdateInitialBalance()
    {
        when(m_goalRepository.findById(m_goal.GetId())).thenReturn(Optional.of(m_goal));

        BigDecimal newInitialBalance =
            m_goal.GetInitialBalance().add(BigDecimal.valueOf(100.0));

        m_goalService.ChangeInitialBalance(m_goal.GetId(), newInitialBalance);

        verify(m_goalRepository).save(m_goal);

        assertEquals(newInitialBalance, m_goal.GetInitialBalance());
    }

    @Test
    @DisplayName(
        "Test if the initial balance is not updated when the new balance is negative")
    public void
    TestUpdateInitialBalanceNegative()
    {
        assertThrows(RuntimeException.class, () -> {
            m_goalService.ChangeInitialBalance(m_goal.GetId(),
                                               BigDecimal.valueOf(-1.0));
        });

        verify(m_goalRepository, never()).save(any());
    }

    @Test
    @DisplayName(
        "Test if the initial balance is not updated when the goal does not exist")
    public void
    TestUpdateInitialBalanceDoesNotExist()
    {
        assertThrows(RuntimeException.class, () -> {
            m_goalService.ChangeInitialBalance(m_goal.GetId(),
                                               BigDecimal.valueOf(100.0));
        });

        verify(m_goalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test if the target balance is updated successfully")
    public void TestUpdateTargetBalance()
    {
        when(m_goalRepository.findById(m_goal.GetId())).thenReturn(Optional.of(m_goal));

        BigDecimal newTargetBalance =
            m_goal.GetTargetBalance().add(BigDecimal.valueOf(100.0));
        m_goalService.ChangeTargetBalance(m_goal.GetId(), newTargetBalance);

        verify(m_goalRepository).save(m_goal);
        assertEquals(newTargetBalance, m_goal.GetTargetBalance());
    }

    @Test
    @DisplayName(
        "Test if the target balance is not updated when the new balance is negative")
    public void
    TestUpdateTargetBalanceNegative()
    {
        assertThrows(RuntimeException.class, () -> {
            m_goalService.ChangeTargetBalance(m_goal.GetId(), BigDecimal.valueOf(-1.0));
        });

        verify(m_goalRepository, never()).save(any());
    }

    @Test
    @DisplayName(
        "Test if the target balance is not updated when the goal does not exist")
    public void
    TestUpdateTargetBalanceDoesNotExist()
    {
        when(m_goalRepository.findById(m_goal.GetId())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            m_goalService.ChangeTargetBalance(m_goal.GetId(),
                                              BigDecimal.valueOf(100.0));
        });

        verify(m_goalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test if the target date is updated successfully")
    public void TestUpdateTargetDate()
    {
        when(m_goalRepository.findById(m_goal.GetId())).thenReturn(Optional.of(m_goal));

        LocalDateTime newTargetDate = m_goal.GetTargetDate().plusDays(30);
        m_goalService.ChangeTargetDate(m_goal.GetId(), newTargetDate);

        verify(m_goalRepository).save(m_goal);
        assertEquals(newTargetDate, m_goal.GetTargetDate());
    }

    @Test
    @DisplayName("Test if the target date is not updated when the goal does not exist")
    public void TestUpdateTargetDateDoesNotExist()
    {
        when(m_goalRepository.findById(m_goal.GetId())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            m_goalService.ChangeTargetDate(m_goal.GetId(), LocalDateTime.now());
        });

        verify(m_goalRepository, never()).save(any());
    }

    @Test
    @DisplayName(
        "Test if the target date is not updated when the new date is in the past")
    public void
    TestUpdateTargetDateInPast()
    {
        when(m_goalRepository.findById(m_goal.GetId())).thenReturn(Optional.of(m_goal));

        assertThrows(RuntimeException.class, () -> {
            m_goalService.ChangeTargetDate(m_goal.GetId(),
                                           LocalDateTime.now().minusDays(1));
        });

        verify(m_goalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test if the motivation is updated successfully")
    public void TestUpdateMotivation()
    {
        when(m_goalRepository.findById(m_goal.GetId())).thenReturn(Optional.of(m_goal));

        String newMotivation = m_goal.GetMotivation() + " Updated";
        m_goalService.ChangeMotivation(m_goal.GetId(), newMotivation);

        verify(m_goalRepository).save(m_goal);
        assertEquals(newMotivation, m_goal.GetMotivation());
    }

    @Test
    @DisplayName("Test if the motivation is not updated when the goal does not exist")
    public void TestUpdateMotivationDoesNotExist()
    {
        when(m_goalRepository.findById(m_goal.GetId())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            m_goalService.ChangeMotivation(m_goal.GetId(), "New Motivation");
        });

        verify(m_goalRepository, never()).save(any());
    }
}
