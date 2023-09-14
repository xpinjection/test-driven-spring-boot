package com.xpinjection.library.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

@Data
@RequiredArgsConstructor
public class CreateExpertDto {
    @NotBlank
    @NonNull
    private String name;
    @NotBlank
    @NonNull
    private String contact;
    @NotEmpty
    private Set<Recommendation> recommendations = new HashSet<>();

    public void addRecommendations(Recommendation... recommendations) {
        this.recommendations.addAll(asList(recommendations));
    }
}
