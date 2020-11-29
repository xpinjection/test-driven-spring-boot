package com.xpinjection.springboot.service;

import com.xpinjection.springboot.adaptors.persistence.BookDao;
import com.xpinjection.springboot.adaptors.persistence.ExpertDao;
import com.xpinjection.springboot.adaptors.persistence.entity.ExpertEntity;
import com.xpinjection.springboot.domain.Recommendation;
import com.xpinjection.springboot.domain.Book;
import com.xpinjection.springboot.domain.Expert;
import com.xpinjection.springboot.exception.InvalidRecommendationException;
import lombok.AllArgsConstructor;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@AllArgsConstructor
@Service
@Transactional
public class ExpertServiceImpl implements ExpertService {
    private final BookDao bookDao;
    private final ExpertDao expertDao;

    @Override
    public long add(Expert expert) {
        var books = findRecommendedBooks(expert);
        if (books.isEmpty()) {
            throw new InvalidRecommendationException("No valid recommendations");
        }
        var saved = storeExpert(expert, books);
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

    private Optional<Book> findBookByName(String part) {
        try {
            return bookDao.findByName(part);
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new InvalidRecommendationException("Recommended book exists in several versions: "
                    + e.getActualSize());
        }
    }

    private boolean validateAuthor(String author, Book book) {
        if (!book.getAuthor().equals(author)) {
            throw new InvalidRecommendationException("Invalid author: " + author);
        }
        return true;
    }
}
