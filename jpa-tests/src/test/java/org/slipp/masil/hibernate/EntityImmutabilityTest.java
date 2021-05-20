package org.slipp.masil.hibernate;

import lombok.*;
import org.hibernate.annotations.Immutable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slipp.masil.hibernate.supports.EntityManagerExtension;
import org.slipp.masil.hibernate.supports.Persistence;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import static lombok.AccessLevel.PROTECTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slipp.masil.hibernate.supports.EntityManageTemplate.get;

@ExtendWith(EntityManagerExtension.class)
public class EntityImmutabilityTest {



    @Getter
    @Builder
    @Entity(name ="Book")
    @Immutable
    @AllArgsConstructor
    @NoArgsConstructor(access = PROTECTED, force = true)
    public static class Book {
        @Id @GeneratedValue
        private Long id;
        @Setter
        private String name;
    }

    private Book book;

    @BeforeEach
    @Persistence(name = "org.slipp.masil.jpa", classes = {Book.class})
    void setUp() {
        book = get().transaction((em) -> {
            Book springBoot = Book.builder().name("Spring boot").build();
            em.persist(springBoot);
            return springBoot;
        });
    }

    @Test
    @DisplayName("In immutability Entity, a Hibernate is not going to optimize to dirty check")
    @Persistence(name = "org.slipp.masil.jpa", classes = {Book.class})
    void does_not_check_dirty() {
        get().transaction((em)-> {
            Book find = em.find(Book.class, book.getId());
            find.setName("Foo");
            //does not update
        });

        get().transaction((em)-> {
            Book find = em.find(Book.class, book.getId());

            assertThat(find.getName()).isNotEqualTo("Foo");
            assertThat(find.getName()).isEqualTo(book.getName());
        });




    }
}
