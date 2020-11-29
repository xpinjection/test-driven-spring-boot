package com.xpinjection.springboot.adaptors.persistence;

import com.xpinjection.springboot.domain.Book;
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
