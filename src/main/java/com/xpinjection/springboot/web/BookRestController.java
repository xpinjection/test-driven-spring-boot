package com.xpinjection.springboot.web;

import com.xpinjection.springboot.domain.Book;
import com.xpinjection.springboot.service.BookService;
import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Alimenkou Mikalai
 */
@RestController
@AllArgsConstructor
public class BookRestController {
    private final BookService bookService;

    @RequestMapping(method = RequestMethod.GET, path = "/books", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Book> findBooksByAuthor(@RequestParam @NotBlank @Valid String author) {
        return bookService.findBooksByAuthor(author);
    }
}
