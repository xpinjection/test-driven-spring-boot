package com.xpinjection.library.adaptors.api;

import com.xpinjection.library.domain.dto.CreateExpertDto;
import com.xpinjection.library.domain.dto.Recommendation;
import com.xpinjection.library.exception.InvalidRecommendationException;
import com.xpinjection.library.service.ExpertService;
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

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
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

    private CreateExpertDto validExpert;

    @BeforeEach
    void setUp() {
        validExpert = new CreateExpertDto("Mikalai", "+38099023546");
        validExpert.addRecommendations(new Recommendation("Effective Java by Josh Bloch"));
    }

    @Test
    void expertCouldBeAddedWithRecommendations() throws Exception {
        when(service.addExpert(refEq(validExpert))).thenReturn(5L);

        addExpert(validExpert)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(5L));

        verify(service).addExpert(refEq(validExpert));
    }

    @Test
    void ifExpertCouldNotBeStoredReturnBadRequest() throws Exception {
        when(service.addExpert(refEq(validExpert)))
                .thenThrow(new InvalidRecommendationException("ERROR"));

        addExpert(validExpert)
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> validationRules() {
        return Stream.of(
                arguments(named("blank name", (Consumer<CreateExpertDto>) expert -> expert.setName(" \n \t "))),
                arguments(named("blank contact", (Consumer<CreateExpertDto>) expert -> expert.setContact(" \n \t "))),
                arguments(named("no recommendations", (Consumer<CreateExpertDto>) expert -> expert.setRecommendations(Set.of())))
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("validationRules")
    void ifExpertIsInvalidReturnBadRequest(Consumer<CreateExpertDto> invalidator) throws Exception {
        invalidator.accept(validExpert);

        addExpert(validExpert)
                .andExpect(status().isBadRequest());
    }

    private ResultActions addExpert(CreateExpertDto expert) throws Exception {
        var recommendations = expert.getRecommendations().stream()
                .map(Recommendation::getSentence)
                .map(s -> "\"" + s + "\"")
                .collect(joining(","));

        return mockMvc.perform(post("/experts")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(String.format("""
                        {
                          "name": "%s",
                          "contact": "%s",
                          "recommendations": [%s]
                        }""", expert.getName(), expert.getContact(), recommendations)));
    }
}
