package com.xpinjection.library.adaptors.persistence.entity;

import com.xpinjection.library.domain.Book;
import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@NoArgsConstructor
@RequiredArgsConstructor
@Setter @Getter
@Table(name = "expert")
public class ExpertEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private String name;
    @NonNull
    private String contact;
    @OneToMany
    @JoinTable(name = "recommendations",
            joinColumns = {@JoinColumn(name = "expert_id")},
            inverseJoinColumns = {@JoinColumn(name = "book_id")})
    private Set<Book> recommendations;
}
