package com.example.testAcl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class TestAclApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestAclApplication.class, args);
    }

}
