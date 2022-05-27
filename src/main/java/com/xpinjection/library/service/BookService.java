package com.xpinjection.library.service;

import com.xpinjection.library.domain.dto.BookDto;
import com.xpinjection.library.domain.dto.Books;

import java.util.List;

/**
 * @author Alimenkou Mikalai
 */
public interface BookService {
    List<BookDto> addBooks(Books books);

    List<BookDto> findBooksByAuthor(String author);

    List<BookDto> findAllBooks();
}
