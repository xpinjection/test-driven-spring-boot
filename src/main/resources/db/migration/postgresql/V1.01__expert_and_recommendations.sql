CREATE TABLE expert (
	id BIGSERIAL,
	name varchar(255) not null,
	contact varchar(255) not null
);

CREATE TABLE recommendations (
	expert_id BIGINT NOT NULL,
	book_id BIGINT NOT NULL,
    primary key (expert_id, book_id),
    foreign key (expert_id) references expert(id),
    foreign key (book_id) references book(id)
);
