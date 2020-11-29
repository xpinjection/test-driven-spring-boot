package com.xpinjection.springboot.adaptors.persistence;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.xpinjection.springboot.adaptors.persistence.entity.ExpertEntity;
import com.xpinjection.springboot.domain.Book;
import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.transaction.TestTransaction;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

@DBUnit(mergeDataSets = true)
public class ExpertDaoTest extends AbstractDaoTest<ExpertDao> {
    @Test
    @DataSet(executeStatementsBefore = "ALTER TABLE expert ALTER COLUMN id RESTART WITH 1",
            value = "stored-books.xml", cleanBefore = true,
            tableOrdering = {"recommendations", "expert", "book"})
    @ExpectedDataSet("expected-stored-expert.xml")
    @Commit
    public void expertCanBeStored() {
        var expert = new ExpertEntity("Mikalai", "a@b.com");
        var book = new Book("Existing book", "Unknown");
        book.setId(13L);
        expert.setRecommendations(newHashSet(book));
        var saved = dao.save(expert);
        assertThat(saved.getId()).isNotNull();
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void unknownBookCanNotBeStoredAsRecommendation() {
        var expert = new ExpertEntity("Mikalai", "a@b.com");
        var book = new Book("Existing book", "Unknown");
        book.setId(17L);
        expert.setRecommendations(newHashSet(book));
        dao.save(expert);
        TestTransaction.flagForCommit();
        TestTransaction.end();
    }

    @Test
    @DataSet(value = {"stored-books.xml", "expected-stored-expert.xml"})
    public void expertCanBeFoundById() {
        assertThat(dao.findById(1L).orElseThrow(IllegalStateException::new))
                .hasFieldOrPropertyWithValue("name", "Mikalai");
    }
}
