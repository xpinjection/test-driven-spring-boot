package com.xpinjection.library.service.impl;

import com.xpinjection.library.adaptors.persistence.BookDao;
import com.xpinjection.library.domain.Book;
import com.xpinjection.library.service.BookService;
import com.xpinjection.library.service.dto.BookDto;
import com.xpinjection.library.service.dto.Books;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static net.logstash.logback.marker.Markers.append;

/**
 * @author Alimenkou Mikalai
 */
@Slf4j
@Service
@Transactional
public class BookServiceImpl implements BookService {
    private final BookDao bookDao;
    private final ConcurrentMap<String, List<Book>> cache = new ConcurrentHashMap<>();

    public BookServiceImpl(BookDao bookDao) {
        this.bookDao = bookDao;
    }

    @Override
    public List<BookDto> addBooks(Books books) {
        LOG.info("Adding books: {}", books);
        return toDto(books.stream()
                .map(entry -> new Book(entry.getKey(), entry.getValue()))
                .map(bookDao::save));
    }

    @Override
    public List<BookDto> findBooksByAuthor(String author) {
            LOG.info(append("operation", "search"),
                    "Try to find books by author: {}", value("author", author));
            Assert.hasText(author, "Author is empty!");
            var normalizedAuthor = normalizeAuthorName(author);
            var books = cache.computeIfAbsent(normalizedAuthor, bookDao::findByAuthor).stream();
            return toDto(books);
    }

    @Override
    public List<BookDto> findAllBooks() {
        LOG.info("Finding all books");
        return toDto(bookDao.findAll().stream());
    }

    private String normalizeAuthorName(String author) {
        var authorName = StringUtils.normalizeSpace(author);
        return isSingleWord(authorName) ? splitOnFirstAndLastNames(authorName) : authorName;
    }

    private boolean isSingleWord(String correctAuthor) {
        return !StringUtils.containsWhitespace(correctAuthor);
    }

    private String splitOnFirstAndLastNames(String author) {
        var parts = StringUtils.splitByCharacterTypeCamelCase(author);
        var firstName = parts[0];
        if (parts.length == 1) {
            return firstName;
        }
        var lastName = StringUtils.substringAfter(author, firstName);
        return String.join(" ", firstName, lastName);
    }

    private List<BookDto> toDto(Stream<Book> books) {
        return books.map(BookServiceImpl::toDto)
                .toList();
    }

    public static BookDto toDto(Book book) {
        return new BookDto(book.getId(), book.getName(), book.getAuthor());
    }
}
