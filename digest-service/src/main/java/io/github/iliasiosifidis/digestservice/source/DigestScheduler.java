package io.github.iliasiosifidis.digestservice.source;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DigestScheduler {

  private final DigestService digestService;

  public DigestScheduler(DigestService digestService) {
    this.digestService = digestService;
  }

  @Scheduled(
          cron = "0 0 " + DigestService.CUTOFF_HOUR + " * * *",
          zone = DigestService.ZONE_ID)
  void createDailyDigest(){
    digestService.createFor(LocalDate.now(DigestService.ZONE));
  }
}