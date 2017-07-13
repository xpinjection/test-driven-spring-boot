package com.xpinjection.springboot.web;

import com.xpinjection.springboot.service.BookService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Alimenkou Mikalai
 */
@Controller
@AllArgsConstructor
public class BookController {
    private final BookService bookService;

    @RequestMapping(method = RequestMethod.GET, path = "/library.html")
    public String booksPage(Model model) {
        model.addAttribute("books", bookService.findAllBooks());
        return "library";
    }
}
