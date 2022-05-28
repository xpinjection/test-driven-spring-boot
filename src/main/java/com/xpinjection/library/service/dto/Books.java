package com.xpinjection.library.service.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

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

    public Stream<Map.Entry<String, String>> stream() {
        return books.entrySet().stream();
    }
}
