package com.xpinjection.library.service.impl;

import com.xpinjection.library.adaptors.persistence.BookDao;
import com.xpinjection.library.adaptors.persistence.ExpertDao;
import com.xpinjection.library.adaptors.persistence.entity.ExpertEntity;
import com.xpinjection.library.domain.Book;
import com.xpinjection.library.service.ExpertService;
import com.xpinjection.library.service.dto.CreateExpertDto;
import com.xpinjection.library.service.dto.Recommendation;
import com.xpinjection.library.service.exception.InvalidRecommendationException;
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
    public long addExpert(CreateExpertDto expert) {
        LOG.info("Add expert: {}", expert);
        var books = findRecommendedBooks(expert);
        if (books.isEmpty()) {
            LOG.error("Recommended books are not found");
            throw new InvalidRecommendationException("No valid recommendations");
        }
        var created = createExpert(expert, books);
        LOG.info("Expert is stored with ID: {}", created.getId());
        return created.getId();
    }

    private ExpertEntity createExpert(CreateExpertDto expert, Set<Book> books) {
        var entity = new ExpertEntity(expert.getName(), expert.getContact());
        entity.setRecommendations(books);
        return expertDao.save(entity);
    }

    private Set<Book> findRecommendedBooks(CreateExpertDto expert) {
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
            LOG.error("Author {} doesn't match the book: {}", author, book);
            throw new InvalidRecommendationException("Invalid author: " + author);
        }
        return true;
    }
}
