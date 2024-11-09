/*
 * Filename: wallet_type.java
 * Created on: September 29, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.moinex.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Represents a wallet type
 * A wallet type is a category of wallets
 */
@Entity
@Table(name = "wallet_type")
public class WalletType
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 50, unique = true)
    private String name;

    @Column(name = "icon", nullable = true, length = 30)
    private String icon;

    /**
     * Default constructor for JPA
     */
    public WalletType() { }

    /**
     * Constructor for testing purposes
     * @param id The id of the wallet type
     * @param name The name of the wallet type
     */
    public WalletType(Long id, String name)
    {
        this.id = id;
        this.name = name;
    }

    /**
     * Constructor for WalletType
     * @param name The name of the wallet type
     */
    public WalletType(String name)
    {
        this.name = name;
    }

    /**
     * Get the id of the wallet type
     * @return The id of the wallet type
     */
    public Long GetId()
    {
        return id;
    }

    /**
     * Get the name of the wallet type
     * @return The name of the wallet type
     */
    public String GetName()
    {
        return name;
    }

    /**
     * Get the icon of the wallet type
     * @return The icon of the wallet type
     */
    public String GetIcon()
    {
        return icon;
    }

    /**
     * Set the name of the wallet type
     * @param name The name of the wallet type
     */
    public void SetName(String name)
    {
        this.name = name;
    }

    /**
     * Set the icon of the wallet type
     * @param icon The icon of the wallet type
     */
    public void SetIcon(String icon)
    {
        this.icon = icon;
    }
}
