package com.xpinjection.springboot;

import com.xpinjection.springboot.service.BookService;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Alimenkou Mikalai
 */
@Component
@AllArgsConstructor
public class DemoRunner implements CommandLineRunner {
    private final BookService bookService;

    @Override
    public void run(String... strings) throws Exception {
        Map<String, String> books = new HashMap<>();
        books.put("Spring in Action", "Who knows?");
        books.put("Hibernate in Action", "Who cares?");
        bookService.addBooks(books).forEach(System.out::println);
    }
}
