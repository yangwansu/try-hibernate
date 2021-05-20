package org.slipp.masil.hibernate;

import lombok.*;
import org.junit.jupiter.api.BeforeEach;
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

@ExtendWith(value = EntityManagerExtension.class)
public class DirtyCheckingTest {


    @Getter
    @Builder
    @Entity(name = "Book")
    @NoArgsConstructor(access = PROTECTED)
    @AllArgsConstructor
    public static class Book {

        @Id
        @GeneratedValue
        private Long id;
        @Setter
        private String name;

        public void doSomething() {
            name = "changed";
        }
    }

    private Long bookId;

    @BeforeEach
    @Persistence(
            name = "org.slipp.masil.jpa",
            classes = {Book.class})
    void setUp() {
        bookId = get().transaction((em) -> {
            Book book = Book.builder().name("Grit").build();
            em.persist(book);
            return book.getId();
        });
    }

    @Test
    @Persistence(
            name = "org.slipp.masil.jpa",
            classes = {Book.class})
    void basic() {
        get().transaction((em)->{
            Book find = em.find(Book.class, bookId);
            find.setName("apple");
            //commit!! dirty checking !!
        });

        get().transaction((em)->{
            Book find = em.find(Book.class, bookId);
            assertThat(find.getName()).isEqualTo("apple");
            find.doSomething(); // changed name "changed"
            //commit!! dirty checking !!
        });

        get().transaction((em)->{
            Book find = em.find(Book.class, bookId);
            assertThat(find.getName()).isEqualTo("changed");
        });
    }

    @Test
    @Persistence(
            name = "org.slipp.masil.jpa",
            classes = {Book.class})
    void name() {

        get().transaction((em)->{
            Book find = em.find(Book.class, bookId);
            find.setName("apple");
            //commit!! dirty checking !!
        }, (em-> {
            Book find = em.find(Book.class, bookId); // from L1 cache
            em.detach(find);

            System.out.println("====");
            find = em.find(Book.class, bookId); // from DB
            assertThat(find.getName()).isEqualTo("apple");
        }));


    }
}
