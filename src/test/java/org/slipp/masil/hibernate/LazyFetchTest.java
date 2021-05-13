package org.slipp.masil.hibernate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.LazyInitializationException;
import org.hibernate.proxy.HibernateProxy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slipp.masil.hibernate.supports.EntityManagerExtension;
import org.slipp.masil.hibernate.supports.Persistence;

import javax.persistence.*;

import static lombok.AccessLevel.PROTECTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.slipp.masil.hibernate.supports.EntityManageTemplate.get;

@ExtendWith(EntityManagerExtension.class)
public class LazyFetchTest {

    @Entity
    @Getter
    @Table(name = "Car")
    @RequiredArgsConstructor(staticName = "of")
    @NoArgsConstructor(access = PROTECTED, force = true)
    public static class Car {

        @Id @GeneratedValue
        private Long id;

        private final String name;

        @ManyToOne
        private final Type type;

    }

    @Getter
    @Entity
    @Table(name = "Book")
    @RequiredArgsConstructor(staticName = "of")
    @NoArgsConstructor(access = PROTECTED, force = true)
    public static class Book {

        @Id @GeneratedValue
        private Long id;

        private final String name;

        @ManyToOne(optional = false, fetch = FetchType.LAZY)
        private final Type type;

    }

    @Entity
    @Getter
    @Table(name = "Type")
    @RequiredArgsConstructor(staticName = "of")
    @NoArgsConstructor(access = PROTECTED, force = true)
    public static class Type {

        @Id
        @GeneratedValue
        private Long id;
        private final String name;
    }

    @Test
    @Persistence(name = "org.slipp.masil.jpa",
            classes = {Car.class, Type.class})
    void testEAGER() {
        Car car = get().transaction((em) -> {
            Type type = new Type("pink");
            em.persist(type);
            Car modelY = new Car("modelY", type);
            em.persist(modelY);
            return modelY;
        });

        get().transaction((em)-> {
            Car find = em.find(Car.class, car.getId());

            Type type = find.getType();

            assertThat(type).isNotInstanceOf(HibernateProxy.class);
        });
    }

    @Test
    @DisplayName("Lazy Fetched Object uses by HibernateProxy ")
    @Persistence(name = "org.slipp.masil.jpa",
            classes = {Book.class, Type.class})
    void testLazy() {
        Book book = get().transaction((em) -> {
            Type type = new Type("pink");
            em.persist(type);
            Book harryPotter = new Book("Harry potter", type);
            em.persist(harryPotter);
            return harryPotter;
        });

        get().transaction((em) -> {
            Book find = em.find(Book.class, book.getId());

            Type lazed = find.getType();

            assertThat(lazed).isInstanceOf(HibernateProxy.class);

        });

    }

    @Test
    @DisplayName("Without session Lazy Fetched Object ")
    @Persistence(name = "org.slipp.masil.jpa",
            classes = {Book.class, Type.class})
    void testLazyObjectWithoutSession() {
        Book book = get().transaction((em) -> {
            Type type = new Type("pink");
            em.persist(type);
            Book harryPotter = new Book("Harry potter", type);
            em.persist(harryPotter);
            return harryPotter;
        });

        Type type = get().transaction((em) -> {
            Book find = em.find(Book.class, book.getId());
            return find.getType();

        });

        assertThatThrownBy(type::getName)
                .isInstanceOf(LazyInitializationException.class)
                .hasMessageContaining("could not initialize proxy" )
                .hasMessageContaining("no Session");
    }


}
