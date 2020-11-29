package com.xpinjection.springboot.web;

import com.xpinjection.springboot.domain.Book;
import com.xpinjection.springboot.service.BookService;
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
