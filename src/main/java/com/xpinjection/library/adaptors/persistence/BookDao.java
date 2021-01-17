package com.xpinjection.library.adaptors.persistence;

import com.xpinjection.library.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author Alimenkou Mikalai
 */
public interface BookDao extends JpaRepository<Book, Long> {
    List<Book> findByAuthor(String author);

    Optional<Book> findByName(String name);
}
