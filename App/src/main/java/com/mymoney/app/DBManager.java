/*
 * Filename: DBManager.java
 * Created on: March 10, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {
    private static final String DB_URL = "jdbc:sqlite:my_money.db";
    private static Connection m_connection;

    public static Connection GetConnection() {
        if (m_connection == null) {
            try {
                m_connection = DriverManager.getConnection(DB_URL);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return m_connection;
    }

    public static void CloseConnection() {
        if (m_connection != null) {
            try {
                m_connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void CreateTables() {
        try {
            Connection connection = GetConnection();

            String createWalletsTable = "CREATE TABLE IF NOT EXISTS wallets (uuid TEXT PRIMARY KEY, name TEXT, balance REAL)";
            String createCategoriesTable = "CREATE TABLE IF NOT EXISTS categories (uuid TEXT PRIMARY KEY, name TEXT)";
            String createTransactionsTable = "CREATE TABLE IF NOT EXISTS transactions (uuid TEXT PRIMARY KEY, account TEXT, category TEXT, value REAL, date TEXT, description TEXT)";

            connection.createStatement().execute(createWalletsTable);
            connection.createStatement().execute(createCategoriesTable);
            connection.createStatement().execute(createTransactionsTable);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void RunQuery(String query) {
        try {
            Connection connection = GetConnection();
            connection.createStatement().execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
