package com.xpinjection.springboot.service;

import com.xpinjection.springboot.adaptors.persistence.BookDao;
import com.xpinjection.springboot.domain.Book;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.*;

/**
 * @author Alimenkou Mikalai
 */
@RunWith(MockitoJUnitRunner.class)
public class BookServiceImplTest {
    @Mock
    private BookDao dao;

    private BookService bookService;

    @Before
    public void init() {
        bookService = new BookServiceImpl(dao);
    }

    @Test
    public void ifNoBooksPassedEmptyListIsReturned() {
        assertThat(bookService.addBooks(Collections.emptyMap()), is(empty()));
    }

    @Test
    public void forEveryPairOfTitleAndAuthorBookIsCreatedAndStored() {
        var first = new Book("The first", "author");
        var second = new Book("The second", "another author");
        when(dao.save(notNull())).thenReturn(first).thenReturn(second);

        Map<String, String> books = new HashMap<>();
        books.put("The first", "author");
        books.put("The second", "another author");
        assertThat(bookService.addBooks(books), hasItems(first, second));
    }

    @Test
    public void ifNoBooksFoundForAuthorReturnEmptyList() {
        when(dao.findByAuthor("a")).thenReturn(emptyList());

        assertNoBooksFound("a");
        verify(dao, only()).findByAuthor("a");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ifAuthorIsEmptyThrowException() {
        bookService.findBooksByAuthor(" \t \n ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ifAuthorIsNullThrowException() {
        bookService.findBooksByAuthor(null);
    }

    @Test
    public void booksByAuthorShouldBeCached() {
        var book = new Book("The book", "author");
        when(dao.findByAuthor("a")).thenReturn(singletonList(book));
        when(dao.findByAuthor("a a")).thenReturn(emptyList());

        assertBooksByAuthor("a", book);
        assertBooksByAuthor("a", book);
        assertNoBooksFound("a a");
        verify(dao, times(1)).findByAuthor("a");
    }

    @Test
    public void ifCamelCaseDetectedThenSplitInvalidAuthorNameOnFirstAndLastName() {
        var book = new Book("The book", "Mikalai Alimenkou");
        when(dao.findByAuthor("Mikalai Alimenkou")).thenReturn(singletonList(book));

        assertBooksByAuthor("MikalaiAlimenkou", book);
    }

    @Test
    public void punctuationShouldBeIgnored() {
        var book = new Book("The book", "Who cares");
        when(dao.findByAuthor("Who cares?")).thenReturn(singletonList(book));

        assertBooksByAuthor("Who cares?", book);
    }

    @Test
    public void compositeLastNameIsNotSplit() {
        var book = new Book("The book", "Alfred McGregor");
        when(dao.findByAuthor("Alfred McGregor")).thenReturn(singletonList(book));

        assertBooksByAuthor("Alfred McGregor", book);
    }

    @Test
    public void authorNameShouldBeTrimmedBeforeUsage() {
        var book = new Book("The book", "Mikalai Alimenkou");
        when(dao.findByAuthor("Mikalai Alimenkou")).thenReturn(singletonList(book));

        assertBooksByAuthor(" \t Mikalai \n Alimenkou \t ", book);
    }

    private void assertBooksByAuthor(String author, Book book) {
        assertThat(bookService.findBooksByAuthor(author), hasItem(book));
    }

    private void assertNoBooksFound(String author) {
        assertThat(bookService.findBooksByAuthor(author), is(empty()));
    }
}
