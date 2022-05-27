package com.xpinjection.library.domain.dto;

import com.xpinjection.library.domain.Book;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class Books {
    private final Map<String, String> books;

    private Books(Map<String, String> books) {
        this.books = books;
    }

    public static Books fromMap(Map<String, String> books) {
        return new Books(books);
    }

    public static Books empty() {
        return new Books(new HashMap<>());
    }

    public List<Book> asList() {
        return books.entrySet().stream()
                .map(entry -> new Book(entry.getKey(), entry.getValue()))
                .collect(toList());
    }
}
