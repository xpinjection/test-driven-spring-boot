package com.xpinjection.springboot.domain;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

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
    private Set<String> recommendations = new HashSet<>();

    public void addRecommendations(String... recommendations) {
        this.recommendations.addAll(asList(recommendations));
    }
}
