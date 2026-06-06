package com.cyberblog.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.cyberblog.backend.mapper")
public class CyberblogApplication {
    public static void main(String[] args) {
        SpringApplication.run(CyberblogApplication.class, args);
    }
}
