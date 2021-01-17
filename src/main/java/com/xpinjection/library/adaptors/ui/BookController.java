package com.xpinjection.library.adaptors.ui;

import com.xpinjection.library.service.BookService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Alimenkou Mikalai
 */
@Controller
@AllArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping("/library.html")
    String booksPage(Model model) {
        model.addAttribute("books", bookService.findAllBooks());
        return "library";
    }
}
