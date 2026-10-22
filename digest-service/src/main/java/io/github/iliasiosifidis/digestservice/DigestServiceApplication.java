package io.github.iliasiosifidis.digestservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DigestServiceApplication {
  static void main(String[] args) {
    SpringApplication.run(DigestServiceApplication.class, args);
  }
}