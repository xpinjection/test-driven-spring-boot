package com.xpinjection.springboot.domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author Alimenkou Mikalai
 */
@Entity
@Getter @Setter
@NoArgsConstructor
@RequiredArgsConstructor
@ToString
public class Book {
    @Id @GeneratedValue
    private Long id;

    @NonNull
    private String name;

    @NonNull
    private String author;
}
