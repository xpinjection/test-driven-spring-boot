package com.xpinjection.springboot.service;

import com.xpinjection.springboot.dao.BookDao;
import com.xpinjection.springboot.dao.ExpertDao;
import com.xpinjection.springboot.dao.entity.ExpertEntity;
import com.xpinjection.springboot.dao.valueobject.Recommendation;
import com.xpinjection.springboot.domain.Book;
import com.xpinjection.springboot.domain.Expert;
import com.xpinjection.springboot.exception.InvalidRecommendationException;
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
        Book regular = expectBookFound("Spring in Action", "Arun Gupta");
        Book humanFormat = expectBookFound("Hibernate in Action", "Sam Newman");
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
        ExpertEntity savedEntity = new ExpertEntity(entity.getName(), entity.getContact());
        savedEntity.setId(id);
        when(expertDao.save(refEq(entity))).thenReturn(savedEntity);
    }

    private Book expectBookFound(String name, String author) {
        Book book = new Book(name, author);
        when(bookDao.findByName(name))
                .thenReturn(Optional.of(book));
        return book;
    }
}
