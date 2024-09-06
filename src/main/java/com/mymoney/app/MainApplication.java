package com.mymoney.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.mymoney.entities")
@EnableJpaRepositories(basePackages = "com.mymoney.repositories")
public class MainApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(MainApplication.class, args);
    }
}
