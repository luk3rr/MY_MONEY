/*
 * Filename: MainApplication.java
 * Created on: September  6, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.app;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan(basePackages = "com.mymoney.entities")
@EnableJpaRepositories(basePackages = "com.mymoney.repositories")
@SpringBootApplication(scanBasePackages = "com.mymoney")
public class MainApplication
{ }
