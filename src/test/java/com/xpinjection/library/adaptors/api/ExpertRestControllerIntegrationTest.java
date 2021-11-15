package com.xpinjection.library.adaptors.api;

import com.xpinjection.library.domain.Expert;
import com.xpinjection.library.domain.Recommendation;
import com.xpinjection.library.exception.InvalidRecommendationException;
import com.xpinjection.library.service.ExpertService;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

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

    private ObjectMapper objectMapper = new ObjectMapper();

    private Expert validExpert;

    @BeforeEach
    void setUp() {
        validExpert = new Expert("Mikalai", "+38099023546");
        validExpert.addRecommendations(new Recommendation("Effective Java by Josh Bloch"));
    }

    @Test
    void expertCouldBeAddedWithRecommendations() throws Exception {
        when(service.add(refEq(validExpert))).thenReturn(5L);

        addExpert(validExpert)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(5L));

        verify(service).add(refEq(validExpert));
    }

    @Test
    void ifExpertCouldNotBeStoredReturnBadRequest() throws Exception {
        when(service.add(refEq(validExpert)))
                .thenThrow(new InvalidRecommendationException("ERROR"));

        addExpert(validExpert)
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> validationRules() {
        return Stream.of(
                Arguments.of((Consumer<Expert>) expert -> expert.setName(" \n \t ")),
                Arguments.of((Consumer<Expert>) expert -> expert.setContact(" \n \t ")),
                Arguments.of((Consumer<Expert>) expert -> expert.setRecommendations(Set.of()))
        );
    }

    @ParameterizedTest
    @MethodSource("validationRules")
    void ifExpertIsInvalidReturnBadRequest(Consumer<Expert> invalidator) throws Exception {
        invalidator.accept(validExpert);

        addExpert(validExpert)
                .andExpect(status().isBadRequest());
    }

    private ResultActions addExpert(Expert expert) throws Exception {
        return mockMvc.perform(post("/experts")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(expert)));
    }
}
