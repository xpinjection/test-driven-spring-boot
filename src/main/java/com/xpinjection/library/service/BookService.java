package com.xpinjection.library.service;

import com.xpinjection.library.domain.Book;
import com.xpinjection.library.domain.Books;

import java.util.List;

/**
 * @author Alimenkou Mikalai
 */
public interface BookService {
    List<Book> addBooks(Books books);

    List<Book> findBooksByAuthor(String author);

    List<Book> findAllBooks();
}
