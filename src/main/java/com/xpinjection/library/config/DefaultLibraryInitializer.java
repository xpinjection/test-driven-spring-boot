package com.xpinjection.library.config;

import com.xpinjection.library.domain.Books;
import com.xpinjection.library.service.BookService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;

@Slf4j
@Component
@AllArgsConstructor
public class DefaultLibraryInitializer implements ApplicationRunner {
    private final BookService bookService;
    private final LibrarySettings settings;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (args.containsOption("debug")) {
            LOG.info("Application is started in DEBUG mode");
        }
        var books = IntStream.range(1, settings.getSize()).boxed()
                .collect(toMap(o -> "Book #" + o, o -> "Author #" + o));
        bookService.addBooks(Books.fromMap(books));
        LOG.info("Configured library size is {}", settings.getSize());
    }
}
