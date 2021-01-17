package com.xpinjection.library.adaptors.api;

import com.xpinjection.library.domain.Book;
import com.xpinjection.library.service.BookService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author Alimenkou Mikalai
 */
@RestController
@AllArgsConstructor
public class BookRestController {
    private final BookService bookService;

    @GetMapping(path = "/books", produces = MediaType.APPLICATION_JSON_VALUE)
    List<Book> findBooksByAuthor(@RequestParam @NotBlank @Valid String author) {
        return bookService.findBooksByAuthor(author);
    }
}
