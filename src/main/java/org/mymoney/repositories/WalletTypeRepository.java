/*
 * Filename: WalletTypeRepository.java
 * Created on: September 29, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.repositories;

import org.mymoney.entities.WalletType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletTypeRepository extends JpaRepository<WalletType, Long> {

    /**
     * Find all wallet types ordered by name ascending
     * @return List of wallet types
     */
    List<WalletType> findAllByOrderByNameAsc();
}
