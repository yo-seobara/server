package com.final2.yoseobara;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class YoseobaraApplication {

    public static void main(String[] args) {
        SpringApplication.run(YoseobaraApplication.class, args);
    }

}
