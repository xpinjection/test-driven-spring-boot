package com.xpinjection.library.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "library")
public class LibrarySettings {
    @Min(0)
    /**
     * Initial size of the library
     */
    private int size;
}
