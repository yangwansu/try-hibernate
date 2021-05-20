package org.slipp.masil.hibernate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slipp.masil.hibernate.supports.EntityManagerExtension;
import org.slipp.masil.hibernate.supports.Persistence;

import javax.persistence.*;
import java.util.List;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PROTECTED;
import static org.slipp.masil.hibernate.supports.EntityManageTemplate.get;


@ExtendWith(EntityManagerExtension.class)
public class OneToManyUnidirectionalTest {

    @Getter
    @Entity(name = "product")
    @RequiredArgsConstructor(staticName = "of")
    @NoArgsConstructor(access = PROTECTED, force = true)
    public static class Product {
        @Id
        @GeneratedValue
        private Long id;

        private final String name;

        @OneToMany(fetch = FetchType.LAZY)
        @JoinColumn(name = "product_id")
        private final List<Category> categories;
    }

    @Getter
    @Entity(name = "category")
    @RequiredArgsConstructor(staticName = "of")
    @NoArgsConstructor(access = PROTECTED, force = true)
    public static class Category {

        @Id
        @GeneratedValue
        private Long id;

        private final String name;
    }

    @Test
    @Persistence(name = "org.slipp.masil.jpa", classes = {
            Product.class,
            Category.class
    })
    void uni_direction() {

        Product saved = get().transaction((em) -> {
            List<Category> categories = Lists.newArrayList();
            for (int i = 0; i < 2; i++) {
                Category c = Category.of("c "+i);
                em.persist(c);
                categories.add(c);
            }

            Product product = Product.of("product1", categories);
            em.persist(product);

            return product;
        });

        get().transaction((em) -> {

            Product find = em.find(Product.class, saved.getId());

            List<String> a = find.getCategories().stream().map(Category::getName).collect(Collectors.toList());

            a.forEach(System.out::println);
        });


    }

}
