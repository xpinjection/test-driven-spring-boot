package com.xpinjection.library.service;

import com.xpinjection.library.adaptors.persistence.BookDao;
import com.xpinjection.library.adaptors.persistence.ExpertDao;
import com.xpinjection.library.adaptors.persistence.entity.ExpertEntity;
import com.xpinjection.library.domain.Book;
import com.xpinjection.library.domain.dto.CreateExpertDto;
import com.xpinjection.library.domain.dto.Recommendation;
import com.xpinjection.library.exception.InvalidRecommendationException;
import com.xpinjection.library.service.impl.ExpertServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import java.util.Optional;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExpertServiceTest {
    private ExpertService service;

    @Mock
    private BookDao bookDao;
    @Mock
    private ExpertDao expertDao;

    private final CreateExpertDto expert = new CreateExpertDto("Mikalai", "a@b.com");
    private final ExpertEntity entity = new ExpertEntity("Mikalai", "a@b.com");

    @BeforeEach
    void init() {
        service = new ExpertServiceImpl(bookDao, expertDao);
    }

    @Test
    void expertIsStoredWithRecommendedBooksInAnyFormat() {
        var regular = expectBookFound("Spring in Action", "Arun Gupta");
        var humanFormat = expectBookFound("Hibernate in Action", "Sam Newman");
        entity.setRecommendations(newHashSet(regular, humanFormat));
        expectExpertIsStored(7L);

        expert.addRecommendations(new Recommendation("Spring in Action"),
                new Recommendation("Hibernate in Action by Sam Newman"));
        assertThat(service.addExpert(expert)).isEqualTo(7);
    }

    @Test
    void whenBookIsRecommendedInHumanFormatThenAuthorIsValidated() {
        expectBookFound("Spring in Action", "Arun Gupta");

        expert.addRecommendations(new Recommendation("Spring in Action by Sam Newman"));
        assertThatThrownBy(() -> service.addExpert(expert))
                .isInstanceOf(InvalidRecommendationException.class);
    }

    @Test
    void ifAllRecommendationsWasNotFoundThenThrowException() {
        when(bookDao.findByName("Spring in Action"))
                .thenReturn(Optional.empty());

        expert.addRecommendations(new Recommendation("Spring in Action"));
        assertThatThrownBy(() -> service.addExpert(expert))
                .isInstanceOf(InvalidRecommendationException.class);
    }

    @Test
    void ifRecommendedBookFailedOnFindingThenThrowException() {
        when(bookDao.findByName("Spring in Action"))
                .thenThrow(new IncorrectResultSizeDataAccessException(1));

        expert.addRecommendations(new Recommendation("Spring in Action"));
        assertThatThrownBy(() -> service.addExpert(expert))
                .isInstanceOf(InvalidRecommendationException.class);
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
