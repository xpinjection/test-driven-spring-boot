package com.xpinjection.springboot.web;

import com.xpinjection.springboot.domain.Expert;
import com.xpinjection.springboot.exception.InvalidRecommendationException;
import com.xpinjection.springboot.service.ExpertService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(ExpertRestController.class)
@ActiveProfiles("test")
public class ExpertRestControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpertService service;

    @Test
    public void expertCouldBeAddedWithRecommendations() throws Exception {
        Expert expert = new Expert("Mikalai", "+38099023546");
        expert.addRecommendations("Effective Java by Josh Bloch");
        when(service.add(refEq(expert))).thenReturn(5L);

        addExpert("\"name\": \"Mikalai\"",
                "\"contact\": \"+38099023546\"",
                "\"recommendations\": [\"Effective Java by Josh Bloch\"]")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id").value(5L));

        verify(service).add(refEq(expert));
    }

    @Test
    public void ifExpertCouldNotBeStoredReturnBadRequest() throws Exception {
        Expert expert = new Expert("Mikalai", "+38099023546");
        expert.addRecommendations("Effective Java by Josh Bloch");
        when(service.add(refEq(expert)))
                .thenThrow(new InvalidRecommendationException("ERROR"));

        addExpert("\"name\": \"Mikalai\"",
                "\"contact\": \"+38099023546\"",
                "\"recommendations\": [\"Effective Java by Josh Bloch\"]")
                .andExpect(status().isBadRequest());
    }

    @Test
    public void ifExpertNameIsBlankReturnBadRequest() throws Exception {
        addExpert("\"name\": \" \\n \\t \"",
                "\"contact\": \"+38099023546\"",
                "\"recommendations\": [\"Effective Java by Josh Bloch\"]")
                .andExpect(status().isBadRequest());
    }

    @Test
    public void ifExpertContactIsBlankReturnBadRequest() throws Exception {
        addExpert("\"name\": \"Mikalai\"",
                "\"contact\": \" \\n \\t \"",
                "\"recommendations\": [\"Effective Java by Josh Bloch\"]")
                .andExpect(status().isBadRequest());
    }

    @Test
    public void ifExpertDoesNotHaveRecommendationsReturnBadRequest() throws Exception {
        addExpert("\"name\": \"Mikalai\"",
                "\"contact\": \"+38099023546\"",
                "\"recommendations\": []")
                .andExpect(status().isBadRequest());
    }

    private ResultActions addExpert(String... lines) throws Exception {
        return mockMvc.perform(post("/experts")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(Stream.of(lines)
                        .collect(joining(",\n", "{\n", "\n}"))));
    }
}
