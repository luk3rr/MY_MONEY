/*
 * Filename: GoalRepository.java
 * Created on: December  6, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.moinex.repositories;

import org.moinex.entities.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    /**
     * Check if a goal with the given name exists
     * @param name The name of the goal
     * @return True if a goal with the given name exists, false otherwise
     */
    Boolean existsByName(String name);
}
