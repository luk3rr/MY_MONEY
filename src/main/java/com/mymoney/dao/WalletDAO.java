/*
 * Filename: WalletDAO.java
 * Created on: August 26, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.dao;

import com.mymoney.app.Wallet;
import javax.persistence.EntityManager;

public class WalletDAO
{

    private EntityManager entityManager;

    public WalletDAO(EntityManager entityManager)
    {
        this.entityManager = entityManager;
    }

    public void save(Wallet wallet)
    {
        entityManager.getTransaction().begin();
        entityManager.persist(wallet);
        entityManager.getTransaction().commit();
    }

    public Wallet find(String name)
    {
        return entityManager.find(Wallet.class, name);
    }

    public void update(Wallet wallet)
    {
        entityManager.getTransaction().begin();
        entityManager.merge(wallet);
        entityManager.getTransaction().commit();
    }

    public void delete(Wallet wallet)
    {
        entityManager.getTransaction().begin();
        entityManager.remove(wallet);
        entityManager.getTransaction().commit();
    }
}
