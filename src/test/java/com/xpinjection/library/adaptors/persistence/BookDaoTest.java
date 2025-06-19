package com.xpinjection.library.adaptors.persistence;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.google.common.collect.ImmutableMap;
import com.xpinjection.library.domain.Book;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Alimenkou Mikalai
 */
@DisabledIfSystemProperty(named = "testcontainers.enabled", matches = "false")
public class BookDaoTest extends AbstractDaoTest<BookDao> {
    @Nested
    class FindByNameTests {
        @Test
        void ifThereIsNoBookWithSuchNameThenEmptyOptionalIsReturned() {
            assertThat(dao.findByName("unknown")).isEmpty();
        }

        @Test
        @DataSet("books-by-name.xml")
            //@DataSet(provider = BooksByNameDataSetVerbose.class)
        void ifThereIsOnlyOneBookFoundByNameThenReturnIt() {
            var expected = new Book("First", "Author");
            expected.setId(2L);
            assertThat(dao.findByName("First")).get()
                    .usingRecursiveComparison().isEqualTo(expected);
        }

        @Test
        @DataSet("books-by-name.xml")
        void ifSeveralBooksFoundByNameThenThrowException() {
            assertThatThrownBy(() -> dao.findByName("Second"))
                    .isInstanceOf(IncorrectResultSizeDataAccessException.class);
        }
    }

    @Nested
    class FindByAuthorTests {
        @Test
        void ifThereIsNoBookWithSuchAuthorThenEmptyListIsReturned() {
            assertThat(dao.findByAuthor("unknown")).isEmpty();
        }

        @Test
        void ifBooksByAuthorAreFoundThenTheyAreAllReturned() {
            var id = addBookToDatabase("Title", "author");
            addBookToDatabase("Another title", "another author");

            var book = new Book("Title", "author");
            book.setId(id);
            assertThat(dao.findByAuthor("author")).usingRecursiveFieldByFieldElementComparator().contains(book);
        }

        @Test
        @Sql("/books-for-the-same-author.sql")
        void ifSeveralBooksForTheSameAuthorThenTheyAreAllReturned() {
            var first = new Book("First book", "author");
            first.setId(1L);
            var second = new Book("Second book", "author");
            second.setId(2L);
            assertThat(dao.findByAuthor("author")).usingRecursiveFieldByFieldElementComparator().contains(first, second);
        }

        private long addBookToDatabase(String title, String author) {
            return addRecordToDatabase("book", ImmutableMap.of("name", title, "author", author));
        }
    }

    @Test
    @DataSet("empty.xml")
    @ExpectedDataSet("expected-books.xml")
    void ifBooksAreUniqueThenTheyAreStored() {
        var saved = dao.save(new Book("The First", "Mikalai Alimenkou"));
        assertThat(saved.getId()).isNotNull();
        em.flush();
    }

    @Test
    @DataSet("stored-books.xml")
    void ifBookAlreadyExistsThenItCouldBeFoundUsingSeveralMethods() {
        var book = new Book("Existing book", "Unknown");
        book.setId(13L);
        assertThat(dao.findAll()).usingRecursiveFieldByFieldElementComparator().contains(book);
        assertThat(dao.findById(13L)).get().usingRecursiveComparison().isEqualTo(book);
        assertThat(dao.getReferenceById(13L)).usingRecursiveComparison().isEqualTo(book);
        assertThat(dao.existsById(13L)).isTrue();
        assertThat(dao.findByAuthor("Unknown")).usingRecursiveFieldByFieldElementComparator().contains(book);
    }

    /*public static class BooksByNameDataSet implements DataSetProvider {
        @Override
        public IDataSet provide() {
            return new DataSetBuilder().table("book")
                    .columns("id", "name", "author")
                    .values(2, "First", "Author")
                    .values(3, "Second", "Author")
                    .values(4, "Second", "Another author").build();
        }
    }*/

    /*public static class BooksByNameDataSetVerbose implements DataSetProvider {
        @Override
        public IDataSet provide() {
            return new DataSetBuilder().table("book")
                    .row()
                        .column("id", 2)
                        .column("name", "First")
                        .column("author", "Author")
                    .row()
                        .column("id", 3)
                        .column("name", "Second")
                        .column("author", "Author")
                    .row()
                        .column("id", 4)
                        .column("name", "Second")
                        .column("author", "Another author")
                    .build();
        }
    }*/
}