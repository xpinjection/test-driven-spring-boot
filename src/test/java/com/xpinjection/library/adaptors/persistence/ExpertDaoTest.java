package com.xpinjection.library.adaptors.persistence;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.xpinjection.library.adaptors.persistence.entity.ExpertEntity;
import com.xpinjection.library.domain.Book;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.transaction.TestTransaction;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisabledIfSystemProperty(named = "testcontainers.enabled", matches = "false")
public class ExpertDaoTest extends AbstractDaoTest<ExpertDao> {
    @Test
    @DataSet(executeStatementsBefore = "ALTER SEQUENCE expert_id_seq RESTART WITH 1",
            value = "stored-books.xml", cleanBefore = true,
            tableOrdering = {"recommendations", "expert", "book"},
            skipCleaningFor = "flyway_schema_history"
    )
    @ExpectedDataSet("expected-stored-expert.xml")
    @Commit
    void ifExpertHasValidRecommendationsThenItIsStored() {
        var expert = new ExpertEntity("Mikalai", "a@b.com");
        var book = new Book("Existing book", "Unknown");
        book.setId(13L);
        expert.setRecommendations(newHashSet(book));
        var saved = dao.save(expert);
        assertThat(saved.getId()).isNotNull();
        em.flush();
    }

    @Test
    void ifExpertRecommendsUnknownBookThenItCouldNotBeStored() {
        var expert = new ExpertEntity("Mikalai", "a@b.com");
        var book = new Book("Existing book", "Unknown");
        book.setId(17L);
        expert.setRecommendations(newHashSet(book));
        dao.save(expert);
        TestTransaction.flagForCommit();
        assertThatThrownBy(TestTransaction::end)
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DataSet(value = {"stored-books.xml", "expected-stored-expert.xml"})
    void ifExpertExistsTheItCouldBeFoundById() {
        assertThat(dao.findById(1L).orElseThrow(IllegalStateException::new))
                .hasFieldOrPropertyWithValue("name", "Mikalai");
    }
}
