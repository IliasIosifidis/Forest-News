package io.github.iliasiosifidis.digestservice.controllers;

import io.github.iliasiosifidis.digestservice.source.DigestService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class DigestController {

  private final DigestService digestService;

  public DigestController(DigestService digestService) {
    this.digestService = digestService;
  }

  @PostMapping("/dev/digests/{day}")
  void create(@PathVariable LocalDate day){
    digestService.createFor(day);
  }
}
