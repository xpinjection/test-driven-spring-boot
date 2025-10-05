package com.xpinjection.library.adaptors.api;

import com.xpinjection.library.config.ActuatorBasicSecurityConfig;
import com.xpinjection.library.service.ExpertService;
import com.xpinjection.library.service.dto.CreateExpertDto;
import com.xpinjection.library.service.dto.Recommendation;
import com.xpinjection.library.service.exception.InvalidRecommendationException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = ExpertRestController.class)
@ActiveProfiles("test")
@Import(ActuatorBasicSecurityConfig.class)
@EnableConfigurationProperties(WebEndpointProperties.class)
public class ExpertRestControllerIntegrationTest {
    private static final String NAME = "Mikalai";
    private static final String CONTACT = "+38099023546";
    private static final String RECOMMENDATIONS = "Effective Java by Josh Bloch";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpertService service;

    @Test
    void ifExpertIsValidThenItIsStored() throws Exception {
        var validExpert = new CreateExpertDto(NAME, CONTACT);
        validExpert.addRecommendations(new Recommendation(RECOMMENDATIONS));
        when(service.addExpert(validExpert)).thenReturn(5L);

        addExpert(NAME, CONTACT, RECOMMENDATIONS)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(5L));
    }

    @Nested
    class ErrorHandlingTests {
        @Test
        void ifExpertCanNotBeStoredThenReturnBadRequest() throws Exception {
            when(service.addExpert(notNull()))
                    .thenThrow(new InvalidRecommendationException("ERROR"));

            addExpert(NAME, CONTACT, RECOMMENDATIONS)
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class ValidationTests {
        static Stream<Arguments> validationRules() {
            return Stream.of(
                    arguments(named("blank name", " \n \t "), CONTACT, RECOMMENDATIONS),
                    arguments(named("blank contact", NAME), " \n \t ", RECOMMENDATIONS),
                    arguments(named("no recommendations", NAME), CONTACT, "")
            );
        }

        @ParameterizedTest(name = "{index}: {0}")
        @MethodSource("validationRules")
        void ifExpertIsInvalidThenReturnBadRequest(String name, String contact, String recommendations) throws Exception {
            addExpert(name, contact, recommendations)
                    .andExpect(status().isBadRequest());
        }
    }

    private ResultActions addExpert(String name, String contact, String recommendations) throws Exception {
        return mockMvc.perform(post("/experts")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(String.format("""
                        {
                          "name": "%s",
                          "contact": "%s",
                          "recommendations": [%s]
                        }""", name, contact, StringUtils.wrap(recommendations, '\"'))));
    }
}
