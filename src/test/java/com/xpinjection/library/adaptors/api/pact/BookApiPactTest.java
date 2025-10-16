package com.xpinjection.library.adaptors.api.pact;

import au.com.dius.pact.core.pactbroker.ConsumerVersionSelectors;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.AllowOverridePactUrl;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.StateChangeAction;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerConsumerVersionSelectors;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider;
import com.xpinjection.library.adaptors.api.BookRestController;
import com.xpinjection.library.config.ActuatorBasicSecurityConfig;
import com.xpinjection.library.service.BookService;
import com.xpinjection.library.service.dto.BookDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;

@EnabledIfSystemProperty(named = "pactbroker.enabled", matches = "true")
@DisabledIfSystemProperty(named = "pactbroker.enabled", matches = "false")
@PactBroker(enablePendingPacts = "true", providerTags = {"master"})
@AllowOverridePactUrl
@Provider("com.xpinjection.library")
@WebMvcTest(BookRestController.class)
@ActiveProfiles("test")
@Import(ActuatorBasicSecurityConfig.class)
@EnableConfigurationProperties(WebEndpointProperties.class)
class BookApiPactTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private BookService bookService;

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @PactBrokerConsumerVersionSelectors
    public static List<ConsumerVersionSelectors> consumerVersionSelectors() {
        return List.of(new ConsumerVersionSelectors.Tag("master"));
    }

    @BeforeEach
    void before(PactVerificationContext context) {
        var target = new MockMvcTestTarget(mockMvc);
        context.setTarget(target);
    }

    @State(value = "books exist for author", action = StateChangeAction.SETUP)
    void bookExistsForAuthor(Map<String, String> params) {
        var author = params.get("author");
        when(bookService.findBooksByAuthor(author))
                .thenReturn(singletonList(new BookDto(5L, "Head first Spring", author)));
    }

    @State(value = "no books for author", action = StateChangeAction.SETUP)
    void noBooksForAuthor(Map<String, String> params) {
        var author = params.get("author");
        when(bookService.findBooksByAuthor(author)).thenReturn(emptyList());
    }
}
