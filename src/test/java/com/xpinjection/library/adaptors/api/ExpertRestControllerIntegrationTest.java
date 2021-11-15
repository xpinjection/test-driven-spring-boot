package com.xpinjection.library.adaptors.api;

import com.xpinjection.library.domain.Expert;
import com.xpinjection.library.domain.Recommendation;
import com.xpinjection.library.exception.InvalidRecommendationException;
import com.xpinjection.library.service.ExpertService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ExpertRestController.class)
@ActiveProfiles("test")
public class ExpertRestControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpertService service;

    @Test
    void expertCouldBeAddedWithRecommendations() throws Exception {
        var expert = new Expert("Mikalai", "+38099023546");
        expert.addRecommendations(new Recommendation("Effective Java by Josh Bloch"));
        when(service.add(refEq(expert))).thenReturn(5L);

        addExpert("\"name\": \"Mikalai\"",
                "\"contact\": \"+38099023546\"",
                "\"recommendations\": [\"Effective Java by Josh Bloch\"]")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(5L));

        verify(service).add(refEq(expert));
    }

    @Test
    void ifExpertCouldNotBeStoredReturnBadRequest() throws Exception {
        Expert expert = new Expert("Mikalai", "+38099023546");
        expert.addRecommendations(new Recommendation("Effective Java by Josh Bloch"));
        when(service.add(refEq(expert)))
                .thenThrow(new InvalidRecommendationException("ERROR"));

        addExpert("\"name\": \"Mikalai\"",
                "\"contact\": \"+38099023546\"",
                "\"recommendations\": [\"Effective Java by Josh Bloch\"]")
                .andExpect(status().isBadRequest());
    }

    @Test
    void ifExpertNameIsBlankReturnBadRequest() throws Exception {
        addExpert("\"name\": \" \\n \\t \"",
                "\"contact\": \"+38099023546\"",
                "\"recommendations\": [\"Effective Java by Josh Bloch\"]")
                .andExpect(status().isBadRequest());
    }

    @Test
    void ifExpertContactIsBlankReturnBadRequest() throws Exception {
        addExpert("\"name\": \"Mikalai\"",
                "\"contact\": \" \\n \\t \"",
                "\"recommendations\": [\"Effective Java by Josh Bloch\"]")
                .andExpect(status().isBadRequest());
    }

    @Test
    void ifExpertDoesNotHaveRecommendationsReturnBadRequest() throws Exception {
        addExpert("\"name\": \"Mikalai\"",
                "\"contact\": \"+38099023546\"",
                "\"recommendations\": []")
                .andExpect(status().isBadRequest());
    }

    private ResultActions addExpert(String... lines) throws Exception {
        return mockMvc.perform(post("/experts")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(Stream.of(lines)
                        .collect(joining(",\n", "{\n", "\n}"))));
    }
}
