package com.xpinjection.springboot.service;

import com.xpinjection.springboot.adaptors.persistence.BookDao;
import com.xpinjection.springboot.adaptors.persistence.ExpertDao;
import com.xpinjection.springboot.adaptors.persistence.entity.ExpertEntity;
import com.xpinjection.springboot.domain.Book;
import com.xpinjection.springboot.domain.Expert;
import com.xpinjection.springboot.domain.Recommendation;
import com.xpinjection.springboot.exception.InvalidRecommendationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class ExpertServiceImpl implements ExpertService {
    private final BookDao bookDao;
    private final ExpertDao expertDao;

    @Override
    public long add(Expert expert) {
        LOG.info("Add expert: {}", expert);
        var books = findRecommendedBooks(expert);
        if (books.isEmpty()) {
            LOG.error("Recommended books are not found");
            throw new InvalidRecommendationException("No valid recommendations");
        }
        var saved = storeExpert(expert, books);
        LOG.info("Expert is stored with ID: {}", saved.getId());
        return saved.getId();
    }

    private ExpertEntity storeExpert(Expert expert, Set<Book> books) {
        var entity = new ExpertEntity(expert.getName(), expert.getContact());
        entity.setRecommendations(books);
        return expertDao.save(entity);
    }

    private Set<Book> findRecommendedBooks(Expert expert) {
        return expert.getRecommendations()
                    .stream()
                    .map(this::findBookByRecommendation)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(toSet());
    }

    private Optional<Book> findBookByRecommendation(Recommendation recommendation) {
        var parts = recommendation.getSentence().split(" by ");
        return findBookByName(parts[0])
                .filter(book -> parts.length == 1 ||
                        validateAuthor(parts[1], book));
    }

    private Optional<Book> findBookByName(String name) {
        try {
            return bookDao.findByName(name);
        } catch (IncorrectResultSizeDataAccessException e) {
            LOG.error("Recommended book exists in several versions: {}", e.getActualSize());
            throw new InvalidRecommendationException("Recommended book exists in several versions: "
                    + e.getActualSize());
        }
    }

    private boolean validateAuthor(String author, Book book) {
        if (!book.getAuthor().equals(author)) {
            LOG.error("Author {} doesn't map for the book: {}", author, book);
            throw new InvalidRecommendationException("Invalid author: " + author);
        }
        return true;
    }
}
