package com.xpinjection.springboot.domain;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

@Data
public class Expert {
    @NotBlank
    private final String name;
    @NotBlank
    private final String contact;
    @NotEmpty
    private Set<Recommendation> recommendations = new HashSet<>();

    public void addRecommendations(Recommendation... recommendations) {
        this.recommendations.addAll(asList(recommendations));
    }
}
