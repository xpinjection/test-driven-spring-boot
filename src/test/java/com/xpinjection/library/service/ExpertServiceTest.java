package com.xpinjection.library.service;

import com.xpinjection.library.adaptors.persistence.BookDao;
import com.xpinjection.library.adaptors.persistence.ExpertDao;
import com.xpinjection.library.adaptors.persistence.entity.ExpertEntity;
import com.xpinjection.library.domain.Book;
import com.xpinjection.library.domain.Expert;
import com.xpinjection.library.domain.Recommendation;
import com.xpinjection.library.exception.InvalidRecommendationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import java.util.Optional;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExpertServiceTest {
    private ExpertService service;

    @Mock
    private BookDao bookDao;
    @Mock
    private ExpertDao expertDao;

    private Expert expert = new Expert("Mikalai", "a@b.com");
    private ExpertEntity entity = new ExpertEntity("Mikalai", "a@b.com");

    @Before
    public void init() {
        service = new ExpertServiceImpl(bookDao, expertDao);
    }

    @Test
    public void expertIsStoredWithRecommendedBooksInAnyFormat() {
        var regular = expectBookFound("Spring in Action", "Arun Gupta");
        var humanFormat = expectBookFound("Hibernate in Action", "Sam Newman");
        entity.setRecommendations(newHashSet(regular, humanFormat));
        expectExpertIsStored(7L);

        expert.addRecommendations(new Recommendation("Spring in Action"),
                new Recommendation("Hibernate in Action by Sam Newman"));
        assertThat(service.add(expert)).isEqualTo(7);
    }

    @Test(expected = InvalidRecommendationException.class)
    public void whenBookIsRecommendedInHumanFormatThenAuthorIsValidated() {
        expectBookFound("Spring in Action", "Arun Gupta");

        expert.addRecommendations(new Recommendation("Spring in Action by Sam Newman"));
        service.add(expert);
    }

    @Test(expected = InvalidRecommendationException.class)
    public void ifAllRecommendationsWasNotFoundThenThrowException() {
        when(bookDao.findByName("Spring in Action"))
                .thenReturn(Optional.empty());

        expert.addRecommendations(new Recommendation("Spring in Action"));
        service.add(expert);
    }

    @Test(expected = InvalidRecommendationException.class)
    public void ifRecommendedBookFailedOnFindingThenThrowException() {
        when(bookDao.findByName("Spring in Action"))
                .thenThrow(new IncorrectResultSizeDataAccessException(1));

        expert.addRecommendations(new Recommendation("Spring in Action"));
        service.add(expert);
    }

    private void expectExpertIsStored(long id) {
        var savedEntity = new ExpertEntity(entity.getName(), entity.getContact());
        savedEntity.setId(id);
        when(expertDao.save(refEq(entity))).thenReturn(savedEntity);
    }

    private Book expectBookFound(String name, String author) {
        var book = new Book(name, author);
        when(bookDao.findByName(name))
                .thenReturn(Optional.of(book));
        return book;
    }
}
