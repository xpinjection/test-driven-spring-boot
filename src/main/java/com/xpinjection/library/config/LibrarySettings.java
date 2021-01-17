package com.xpinjection.library.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "library")
public class LibrarySettings {
    @Min(0)
    private int size;
}
