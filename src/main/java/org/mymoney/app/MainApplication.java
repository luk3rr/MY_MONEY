/*
 * Filename: MainApplication.java
 * Created on: September  6, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.app;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main application class
 * TODO: Add a way to write a math expression and calculate the result in the value
 * fields
 * TODO: Add a listener to the value fields to make it possible to insert only digits
 */
@EntityScan(basePackages = "org.mymoney.entities")
@EnableJpaRepositories(basePackages = "org.mymoney.repositories")
@SpringBootApplication(scanBasePackages = "org.mymoney")
public class MainApplication
{
    public static void main(String[] args)
    {
        Application.launch(JavaFXApp.class, args);
    }
}
