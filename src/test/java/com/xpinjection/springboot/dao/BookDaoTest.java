package com.xpinjection.springboot.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.google.common.collect.ImmutableMap;
import com.xpinjection.springboot.domain.Book;
import org.junit.Test;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author Alimenkou Mikalai
 */
public class BookDaoTest extends AbstractDaoTest<BookDao> {
    @Test
    public void ifThereIsNoBookWithSuchNameEmptyOptionalIsReturned() {
        assertThat(dao.findByName("unknown"), is(equalTo(Optional.empty())));
    }

    @Test
    @DataSet("books-by-name.xml")
    public void ifThereIsOnlyOneBookFoundByNameReturnIt() {
        Book expected = new Book("First", "Author");
        expected.setId(2L);
        assertThat(dao.findByName("First"),
                is(samePropertyValuesAs(Optional.of(expected))));
    }

    @Test(expected = IncorrectResultSizeDataAccessException.class)
    @DataSet("books-by-name.xml")
    public void ifSeveralBooksFoundByNameThrowException() {
        dao.findByName("Second");
    }

    @Test
    public void ifThereIsNoBookWithSuchAuthorEmptyListIsReturned() {
        assertThat(dao.findByAuthor("unknown"), is(empty()));
    }

    @Test
    public void ifBooksByAuthorAreFoundTheyAreReturned() {
        long id = addBookToDatabase("Title", "author");
        addBookToDatabase("Another title", "another author");

        Book book = new Book("Title", "author");
        book.setId(id);
        assertThat(dao.findByAuthor("author"),
                hasItem(samePropertyValuesAs(book)));
    }

    @Test
    @Sql("/books-for-the-same-author.sql")
    public void severalBooksForTheSameAuthorAreReturned() {
        Book first = new Book("First book", "author");
        first.setId(1L);
        Book second = new Book("Second book", "author");
        second.setId(2L);
        assertThat(dao.findByAuthor("author"), hasItems(samePropertyValuesAs(first), samePropertyValuesAs(second)));
    }

    @Test
    @DataSet("empty.xml")
    @ExpectedDataSet("expected-books.xml")
    @Commit
    public void booksMayBeStored() {
        Book saved = dao.save(new Book("The First", "Mikalai Alimenkou"));
        assertThat(saved.getId(), is(notNullValue()));
    }

    @Test
    @DataSet("stored-books.xml")
    public void ifBookAlreadyExistsItMayBeFoundUsingSeveralMethods() {
        Book book = new Book("Existing book", "Unknown");
        book.setId(13L);
        assertThat(dao.findAll(), hasItem(samePropertyValuesAs(book)));
        assertThat(dao.findById(13L), samePropertyValuesAs(Optional.of(book)));
        assertThat(dao.getOne(13L), samePropertyValuesAs(book));
        assertThat(dao.existsById(13L), is(true));
        assertThat(dao.findByAuthor("Unknown"), hasItem(samePropertyValuesAs(book)));
    }

    private long addBookToDatabase(String title, String author) {
        return addRecordToDatabase("book", ImmutableMap.of("name", title, "author", author));
    }
}