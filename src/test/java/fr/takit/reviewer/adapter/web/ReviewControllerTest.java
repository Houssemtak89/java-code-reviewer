package fr.takit.reviewer.adapter.web;

import fr.takit.reviewer.application.QuotaExceededException;
import fr.takit.reviewer.application.ReviewService;
import fr.takit.reviewer.application.ReviewUnavailableException;
import fr.takit.reviewer.domain.Finding;
import fr.takit.reviewer.domain.InvalidSubmissionException;
import fr.takit.reviewer.domain.Review;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ReviewService service;

    @Test
    void returnsTheReview() throws Exception {
        when(service.review(eq("class A {}"), eq("10.0.0.1"), isNull())).thenReturn(new Review("ok", List.of(
                new Finding(Finding.Severity.MAJOR, Finding.Category.BUG, 1, "Bug", "Fix"))));

        mvc.perform(post("/api/review").contentType(MediaType.APPLICATION_JSON)
                        .header("X-Forwarded-For", "10.0.0.1, 172.16.0.1")
                        .content("{\"code\":\"class A {}\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("ok"))
                .andExpect(jsonPath("$.findings[0].severity").value("MAJOR"));
    }

    @Test
    void passesTheVisitorToken() throws Exception {
        when(service.review(any(), any(), eq("hf_abc"))).thenReturn(new Review("ok", List.of()));

        mvc.perform(post("/api/review").contentType(MediaType.APPLICATION_JSON)
                        .header("X-Api-Key", "hf_abc")
                        .content("{\"code\":\"class A {}\"}"))
                .andExpect(status().isOk());
        verify(service).review(eq("class A {}"), any(), eq("hf_abc"));
    }

    @Test
    void answers400WhenTheCodeIsMissingOrTooLong() throws Exception {
        mvc.perform(post("/api/review").contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Paste some Java code to review."));

        when(service.review(eq("long"), any(), any())).thenThrow(new InvalidSubmissionException("Too long."));
        mvc.perform(post("/api/review").contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"long\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Too long."));
    }

    @Test
    void answers429WhenTheQuotaIsUsedAnd503WhenTheModelFails() throws Exception {
        when(service.review(eq("a"), any(), any())).thenThrow(new QuotaExceededException("No more today."));
        when(service.review(eq("b"), any(), any())).thenThrow(new ReviewUnavailableException("Model down.", null));

        mvc.perform(post("/api/review").contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"a\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.detail").value("No more today."));
        mvc.perform(post("/api/review").contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"b\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail").value("Model down."));
    }
}
