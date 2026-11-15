package io.github.iliasiosifidis.digestservice.controllers;

import io.github.iliasiosifidis.digestservice.source.DigestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@WebMvcTest(DigestController.class)
class DigestControllerTest {

  @Autowired
  private MockMvcTester mvc;

  @MockitoBean
  private DigestService digestService;

  @Test
  void latest_returnsTheDigestAsJson(){
    given(digestService.latest()).willReturn(Optional.of(new DigestService.DigestResponse(
            LocalDate.of(2026, 11, 12), "Story one\nStory two", Instant.parse("2026-11-12T19:05:00Z"))));
    assertThat(mvc.get().uri("/api/digests/latest"))
            .hasStatusOk()
            .bodyJson()
            .extractingPath("$.day").isEqualTo("2026-11-12");
  }

  @Test
  void latest_returns404WhenThereIsNoDigestYet(){
    given(digestService.latest()).willReturn(Optional.empty());
    assertThat(mvc.get().uri("/api/digests/latest")).hasStatus(HttpStatus.NOT_FOUND);
  }

  @Test
  void create_triggersTheDigestForTheGivenDay(){
    assertThat(mvc.post().uri("/dev/digests/2026-11-12")).hasStatusOk();
    verify(digestService).createFor(LocalDate.of(2026,11,12));
  }
}