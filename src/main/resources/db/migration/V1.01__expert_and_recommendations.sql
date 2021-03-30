CREATE TABLE expert (
    id      BIGSERIAL primary key,
    name    varchar(255) not null,
    contact varchar(255) not null
);

CREATE TABLE recommendations (
    expert_id BIGINT NOT NULL,
    book_id   BIGINT NOT NULL,
    primary key (expert_id, book_id),
    constraint fk_expert_id foreign key (expert_id) references expert (id),
    constraint fk_book_id foreign key (book_id) references book (id)
);
