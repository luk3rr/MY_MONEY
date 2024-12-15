/*
 * Filename: GoalService.java
 * Created on: December  6, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.moinex.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import org.moinex.entities.Goal;
import org.moinex.entities.WalletType;
import org.moinex.repositories.GoalRepository;
import org.moinex.repositories.TransferRepository;
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
    private TransferRepository m_transfersRepository;

    @Autowired
    private WalletTransactionRepository m_walletTransactionRepository;

    @Autowired
    private WalletTypeRepository m_walletTypeRepository;

    private static final Logger logger = LoggerConfig.GetLogger();

    public GoalService() { }

    /**
     * Validates the date and balances of a goal
     * @param initialBalance The initial balance of the goal
     * @param targetBalance The target balance of the goal
     * @param targetDateTime The target date of the goal
     * @throws RuntimeException If the target date is in the past, if the initial
     *   balance is negative, if the target balance is negative or zero or if the
     *   initial balance is greater than the target balance
     */
    @Transactional
    public void ValidateDateAndBalances(BigDecimal    initialBalance,
                                        BigDecimal    targetBalance,
                                        LocalDateTime targetDateTime)
    {
        if (targetDateTime.isBefore(LocalDateTime.now()))
        {
            throw new RuntimeException(
                "The target date of the goal cannot be in the past");
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

        if (initialBalance.compareTo(targetBalance) > 0)
        {
            throw new RuntimeException("The initial balance of the goal cannot be "
                                       + "greater than the target balance");
        }
    }

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
    public Long CreateGoal(String     name,
                           BigDecimal initialBalance,
                           BigDecimal targetBalance,
                           LocalDate  targetDate,
                           String     motivation)
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

        if (m_walletRepository.existsByName(name))
        {
            throw new RuntimeException("A wallet with name " + name + " already exists");
        }

        LocalDateTime targetDateTime = targetDate.atStartOfDay();

        ValidateDateAndBalances(initialBalance, targetBalance, targetDateTime);

        // All goals has the same wallet type
        WalletType walletType =
            m_walletTypeRepository.findByName(Constants.GOAL_DEFAULT_WALLET_TYPE_NAME)
                .orElseThrow(() -> new RuntimeException("Goal wallet type not found"));

        Goal goal = new Goal(name,
                             initialBalance,
                             targetBalance,
                             targetDateTime,
                             motivation,
                             walletType);

        m_goalRepository.save(goal);

        logger.info("Goal " + name + " created with initial balance " + initialBalance);

        return goal.GetId();
    }

    /**
     * Delete a goal
     * @param idGoal The id of the goal to be deleted
     * @throws RuntimeException If the goal does not exist or if the goal has
     *     transactions
     */
    @Transactional
    public void DeleteGoal(Long idGoal)
    {
        Goal goal = m_goalRepository.findById(idGoal).orElseThrow(
            () -> new RuntimeException("Goal with id " + idGoal + " not found"));

        if (m_walletTransactionRepository.GetTransactionCountByWallet(idGoal) > 0 ||
            m_transfersRepository.GetTransferCountByWallet(idGoal) > 0)
        {
            throw new RuntimeException(
                "Goal wallet with id " + idGoal +
                " has transactions and cannot be deleted. Remove "
                + "the transactions first or archive the goal");
        }

        m_goalRepository.delete(goal);

        logger.info("Goal " + goal.GetName() + " was permanently deleted");
    }

    /**
     * Updates a goal
     * @param goal The goal to be updated
     * @throws RuntimeException If the goal does not exist, if the name of the goal is
     *   empty, if a goal with the same name already exists, if the initial balance
     *   is negative, if the target balance is negative or zero, if the initial balance
     *   is greater than the target balance or if the target date is in the past
     */
    @Transactional
    public void UpdateGoal(Goal goal)
    {
        Goal oldGoal =
            m_goalRepository.findById(goal.GetId())
                .orElseThrow(()
                                 -> new RuntimeException("Goal with id " +
                                                         goal.GetId() + " not found"));

        // Remove leading and trailing whitespaces
        goal.SetName(goal.GetName().strip());

        if (goal.GetName().isBlank())
        {
            throw new RuntimeException("The name of the goal cannot be empty");
        }

        if (!goal.GetName().equals(oldGoal.GetName()) &&
            m_goalRepository.existsByName(goal.GetName()))
        {
            throw new RuntimeException("A goal with name " + goal.GetName() +
                                       " already exists");
        }


        if (m_walletRepository.existsByName(goal.GetName()))
        {
            throw new RuntimeException("A wallet with name " + goal.GetName() +
                                       " already exists");
        }

        ValidateDateAndBalances(goal.GetInitialBalance(),
                                goal.GetTargetBalance(),
                                goal.GetTargetDate());

        oldGoal.SetName(goal.GetName());
        oldGoal.SetInitialBalance(goal.GetInitialBalance());
        oldGoal.SetBalance(goal.GetBalance());
        oldGoal.SetTargetBalance(goal.GetTargetBalance());
        oldGoal.SetTargetDate(goal.GetTargetDate());
        oldGoal.SetMotivation(goal.GetMotivation());
        oldGoal.SetArchived(goal.IsArchived());

        m_goalRepository.save(goal);

        logger.info("Goal with id " + goal.GetId() + " updated successfully");
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
        goal.SetCompletionDate(LocalDateTime.now());

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
        goal.SetCompletionDate(null);

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

    /**
     * Get all goals
     */
    public List<Goal> GetGoals()
    {
        return m_goalRepository.findAll();
    }

    /**
     * Get goal by id
     * @param idGoal The id of the goal to be retrieved
     * @return The goal with the given id
     * @throws RuntimeException If the goal does not exist
     */
    public Goal GetGoalById(Long idGoal)
    {
        return m_goalRepository.findById(idGoal).orElseThrow(
            () -> new RuntimeException("Goal with id " + idGoal + " not found"));
    }
}
