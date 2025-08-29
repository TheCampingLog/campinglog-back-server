package com.campinglog.campinglogbackserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication()
public class CampinglogBackServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(CampinglogBackServerApplication.class, args);
  }

}
