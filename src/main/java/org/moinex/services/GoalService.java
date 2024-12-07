/*
 * Filename: GoalService.java
 * Created on: December  6, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.moinex.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import org.moinex.entities.Goal;
import org.moinex.entities.Wallet;
import org.moinex.entities.WalletType;
import org.moinex.repositories.GoalRepository;
import org.moinex.repositories.WalletRepository;
import org.moinex.repositories.WalletTransactionRepository;
import org.moinex.repositories.WalletTypeRepository;
import org.moinex.util.Constants;
import org.moinex.util.LoggerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class is responsible for managing goals
 */
@Service
public class GoalService
{
    @Autowired
    private GoalRepository m_goalRepository;

    @Autowired
    private WalletRepository m_walletRepository;

    @Autowired
    private WalletTypeRepository m_walletTypeRepository;

    private static final Logger logger = LoggerConfig.GetLogger();

    private GoalService() { }

    /**
     * Creates a new goal
     * @param name The name of the goal
     * @param initialBalance The initial balance of the goal
     * @param targetBalance The targetBalance balance of the goal
     * @param targetDate The targetBalance date of the goal
     * @param motivation The motivation for the goal
     * @return The id of the created goal
     * @throws RuntimeException If the name of the goal is empty, if a goal with the
     *     same name already exists or if the initial balance is negative
     */
    @Transactional
    public Long CreateGoal(String        name,
                           BigDecimal    initialBalance,
                           BigDecimal    targetBalance,
                           LocalDateTime targetDate,
                           String        motivation)
    {
        // Remove leading and trailing whitespaces
        name = name.strip();

        if (name.isBlank())
        {
            throw new RuntimeException("The name of the goal cannot be empty");
        }

        if (m_goalRepository.existsByName(name))
        {
            throw new RuntimeException("A goal with name " + name + " already exists");
        }

        if (initialBalance.compareTo(BigDecimal.ZERO) < 0)
        {
            throw new RuntimeException(
                "The initial balance of the goal cannot be negative");
        }

        if (targetBalance.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new RuntimeException(
                "The target balance of the goal must be greater than zero");
        }

        if (targetDate.isBefore(LocalDateTime.now()))
        {
            throw new RuntimeException(
                "The target date of the goal cannot be in the past");
        }

        if (initialBalance.compareTo(targetBalance) > 0)
        {
            throw new RuntimeException("The initial balance of the goal cannot be " +
                                       "greater than the target balance");
        }

        // All goals has the same wallet type
        WalletType walletType =
            m_walletTypeRepository.findByName(Constants.GOAL_DEFAULT_WALLET_TYPE_NAME)
                .orElseThrow(() -> new RuntimeException("Goal wallet type not found"));

        Goal goal = new Goal(name,
                             initialBalance,
                             targetBalance,
                             targetDate,
                             motivation,
                             walletType);

        logger.info("Goal " + name + " created with initial balance " + initialBalance);

        m_goalRepository.save(goal);

        return goal.GetId();
    }

    /**
     * Deletes a goal
     * @param idGoal The id of the goal to be deleted
     * @param transferToWalletId The id of the wallet to which the remaining balance
     *     will be transferred
     * @throws RuntimeException If the goal does not exist or if the wallet does not
     *    exist
     */
    @Transactional
    public void DeleteGoal(Long idGoal, Long transferToWalletId)
    {
        Goal goal = m_goalRepository.findById(idGoal).orElseThrow(
            () -> new RuntimeException("Goal with id " + idGoal + " not found"));

        Wallet wallet = m_walletRepository.findById(transferToWalletId)
                            .orElseThrow(()
                                             -> new RuntimeException(
                                                 "Wallet with id " +
                                                 transferToWalletId + " not found"));

        if (goal.GetBalance().compareTo(BigDecimal.ZERO) > 0)
        {
            wallet.SetBalance(wallet.GetBalance().add(goal.GetBalance()));

            m_walletRepository.save(wallet);

            logger.info("Remaining balance of goal " + goal.GetName() +
                        " transferred to wallet " + wallet.GetName());
        }

        m_goalRepository.delete(goal);

        logger.info("Goal " + goal.GetName() + " deleted");
    }

    /**
     * Archive a goal
     * @param idGoal The id of the goal to be archived
     * @throws RuntimeException If the goal does not exist
     * @note This method is used to archive a goal, which means that the goal
     * will not be deleted from the database, but it will not be used in the
     * application anymore
     */
    @Transactional
    public void ArchiveGoal(Long idGoal)
    {
        Goal goal = m_goalRepository.findById(idGoal).orElseThrow(
            ()
                -> new RuntimeException("Goal with id " + idGoal +
                                        " not found and cannot be archived"));

        goal.SetArchived(true);

        m_goalRepository.save(goal);

        logger.info("Goal with id " + idGoal + " archived");
    }

    /**
     * Unarchive a goal
     * @param idGoal The id of the goal to be unarchived
     * @throws RuntimeException If the goal does not exist
     * @note This method is used to unarchive a goal, which means that the goal
     * will be used in the application again
     */
    @Transactional
    public void UnarchiveGoal(Long idGoal)
    {
        Goal goal = m_goalRepository.findById(idGoal).orElseThrow(
            ()
                -> new RuntimeException("Goal with id " + idGoal +
                                        " not found and cannot be unarchived"));

        goal.SetArchived(false);

        m_goalRepository.save(goal);

        logger.info("Goal with id " + idGoal + " unarchived");
    }

    /**
     * Rename a goal
     * @param idGoal The id of the goal to be renamed
     * @param newName The new name of the goal
     * @throws RuntimeException If the goal does not exist, if the new name is empty
     *    or if a goal with the same name already exists
     */
    @Transactional
    public void RenameGoal(Long idGoal, String newName)
    {
        newName = newName.strip();

        if (newName.isBlank())
        {
            throw new RuntimeException("The name of the goal cannot be empty");
        }

        Goal goal = m_goalRepository.findById(idGoal).orElseThrow(
            () -> new RuntimeException("Goal with id " + idGoal + " not found"));

        if (m_goalRepository.existsByName(newName))
        {
            throw new RuntimeException("A goal with name " + newName +
                                       " already exists");
        }

        goal.SetName(newName);
        m_goalRepository.save(goal);

        logger.info("Goal with id " + idGoal + " renamed to " + newName);
    }

    /**
     * Change the initial balance of a goal
     * @param idGoal The id of the goal to have the initial balance changed
     * @param newInitialBalance The new initial balance of the goal
     * @throws RuntimeException If the goal does not exist or if the new initial balance
     *    is negative
     */
    @Transactional
    public void ChangeInitialBalance(Long idGoal, BigDecimal newInitialBalance)
    {
        if (newInitialBalance.compareTo(BigDecimal.ZERO) < 0)
        {
            throw new RuntimeException(
                "The initial balance of the goal cannot be negative");
        }

        Goal goal = m_goalRepository.findById(idGoal).orElseThrow(
            () -> new RuntimeException("Goal with id " + idGoal + " not found"));

        goal.SetInitialBalance(newInitialBalance);
        m_goalRepository.save(goal);

        logger.info("Goal with id " + idGoal + " initial balance changed to " +
                    newInitialBalance);
    }

    /**
     * Change the target balance of a goal
     * @param idGoal The id of the goal to have the target balance changed
     * @param newTargetBalance The new target balance of the goal
     * @throws RuntimeException If the goal does not exist or if the new target balance
     *    is negative
     */
    @Transactional
    public void ChangeTargetBalance(Long idGoal, BigDecimal newTargetBalance)
    {
        if (newTargetBalance.compareTo(BigDecimal.ZERO) < 0)
        {
            throw new RuntimeException(
                "The target balance of the goal cannot be negative");
        }

        Goal goal = m_goalRepository.findById(idGoal).orElseThrow(
            () -> new RuntimeException("Goal with id " + idGoal + " not found"));

        goal.SetTargetBalance(newTargetBalance);
        m_goalRepository.save(goal);

        logger.info("Goal with id " + idGoal + " target balance changed to " +
                    newTargetBalance);
    }

    /**
     * Change the target date of a goal
     * @param idGoal The id of the goal to have the target date changed
     * @param newTargetDate The new target date of the goal
     * @throws RuntimeException If the goal does not exist
     */
    @Transactional
    public void ChangeTargetDate(Long idGoal, LocalDateTime newTargetDate)
    {
        Goal goal = m_goalRepository.findById(idGoal).orElseThrow(
            () -> new RuntimeException("Goal with id " + idGoal + " not found"));

        if (newTargetDate.isBefore(LocalDateTime.now()))
        {
            throw new RuntimeException(
                "The target date of the goal cannot be in the past");
        }

        goal.SetTargetDate(newTargetDate);
        m_goalRepository.save(goal);

        logger.info("Goal with id " + idGoal + " target date changed to " +
                    newTargetDate);
    }

    /**
     * Change the motivation of a goal
     * @param idGoal The id of the goal to have the motivation changed
     * @param newMotivation The new motivation of the goal
     * @throws RuntimeException If the goal does not exist
     */
    @Transactional
    public void ChangeMotivation(Long idGoal, String newMotivation)
    {
        Goal goal = m_goalRepository.findById(idGoal).orElseThrow(
            () -> new RuntimeException("Goal with id " + idGoal + " not found"));

        goal.SetMotivation(newMotivation);
        m_goalRepository.save(goal);

        logger.info("Goal with id " + idGoal + " motivation changed to " +
                    newMotivation);
    }
}
