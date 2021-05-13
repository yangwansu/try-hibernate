package org.slipp.masil.hibernate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.assertj.core.util.Lists;
import org.hibernate.internal.SessionFactoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slipp.masil.hibernate.supports.EntityManagerExtension;
import org.slipp.masil.hibernate.supports.Persistence;

import javax.persistence.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PROTECTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slipp.masil.hibernate.supports.EntityManageTemplate.get;


@ExtendWith(EntityManagerExtension.class)
public class NPlusOneTest {

    @Getter
    @Entity
    @Table(name = "product")
    @RequiredArgsConstructor(staticName = "of")
    @NoArgsConstructor(access = PROTECTED, force = true)
    public static class Product {
        @Id
        @GeneratedValue
        private Long id;

        private final String name;

        @OneToMany(fetch = FetchType.EAGER)
        @JoinColumn(name = "product_id")
        private final List<Category> categories;
    }

    @Getter
    @Entity
    @Table(name = "category")
    @RequiredArgsConstructor(staticName = "of")
    @NoArgsConstructor(access = PROTECTED, force = true)
    public static class Category {

        @Id
        @GeneratedValue
        private Long id;

        private final String name;
    }

    @BeforeEach
    @Persistence(name = "org.slipp.masil.jpa", classes = {
            Product.class,
            Category.class
    })
    void setUp() {
        get().transaction((em)-> {
            for (int n = 0; n < 3; n++) {
                List<Category> categories = Lists.newArrayList();
                for (int i = 0; i < 2; i++) {
                    Category c = Category.of("c " + i);
                    em.persist(c);
                    categories.add(c);
                }

                Product product = Product.of("product "+n, categories);
                em.persist(product);
            }
        });
    }

    @Test
    @Persistence(name = "org.slipp.masil.jpa", classes = {
            Product.class,
            Category.class
    })
    void n_plus_one() {

        get().transaction((em) -> {

            List<Product> all = em.createQuery("from NPlusOneTest$Product", Product.class).getResultList();

            List<String> a = all.stream().flatMap(p->p.getCategories().stream()).map(Category::getName).collect(Collectors.toList());

            a.forEach(System.out::println);
        });


    }

    @Test
    @Persistence(name = "org.slipp.masil.jpa", classes = {
            Product.class,
            Category.class
    })
    void join_fetch() {
        //join fetch

        get().transaction((em) -> {

            TypedQuery<Product> query = em.createQuery("from NPlusOneTest$Product p left join fetch p.categories", Product.class);
            List<Product> all = query.getResultList();

            List<String> a = all.stream().flatMap(p->p.getCategories().stream()).map(Category::getName).collect(Collectors.toList());

            a.forEach(System.out::println);
        });
    }

    private Connection getConnection(SessionFactoryImpl sessionFactory) {
        Connection connection;
        try {
            connection = sessionFactory.getJdbcServices().getBootstrapJdbcConnectionAccess().obtainConnection();
            assertThat(connection.isClosed()).isFalse();
            return connection;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new IllegalStateException(throwables.getMessage());
        }
    }
}
