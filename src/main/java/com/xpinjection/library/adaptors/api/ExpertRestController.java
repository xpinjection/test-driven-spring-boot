package com.xpinjection.library.adaptors.api;

import com.xpinjection.library.adaptors.api.dto.NewExpert;
import com.xpinjection.library.domain.dto.CreateExpertDto;
import com.xpinjection.library.exception.InvalidRecommendationException;
import com.xpinjection.library.service.ExpertService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name = "experts")
@RestController
@AllArgsConstructor
public class ExpertRestController {
    private final ExpertService service;

    @PostMapping(path = "/experts", produces = MediaType.APPLICATION_JSON_VALUE)
    NewExpert addExpert(@RequestBody @Valid CreateExpertDto command) {
        long id = service.addExpert(command);
        return new NewExpert(id);
    }

    @ExceptionHandler(InvalidRecommendationException.class)
    @ResponseStatus(value= HttpStatus.BAD_REQUEST, reason="Bad recommendations")
    void onSaveError() {
    }
}
