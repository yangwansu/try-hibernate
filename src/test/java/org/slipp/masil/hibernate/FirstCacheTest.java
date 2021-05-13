package org.slipp.masil.hibernate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slipp.masil.hibernate.supports.EntityManagerExtension;
import org.slipp.masil.hibernate.supports.Persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.slipp.masil.hibernate.supports.EntityManageTemplate.get;

@ExtendWith(value = EntityManagerExtension.class)
public class FirstCacheTest {


    @Test
    @Persistence(
            name = "org.slipp.masil.jpa",
            classes = {Product.class})
    void first_level_cache() {
        Product product = new Product("macbook");

        get().transaction(em -> { em.persist(product);});

        get().transaction(em -> {
            Product found1 = em.find(Product.class, product.getId()); // from L1 cache
            Product found2 = em.find(Product.class, product.getId()); // from L1 cache

            Product found3 = em.getEntityManagerFactory().createEntityManager().find(Product.class, product.getId()); // from db

            em.detach(found1); //evict
            Product found4 = em.find(Product.class, product.getId()); // from db

            assertThat(found1).isNotSameAs(product);
            assertThat(found1).isNotNull();
            assertThat(found1).isSameAs(found2);
            assertThat(found1 == found2).isTrue();
            assertThat(found2).isNotSameAs(found3);
            assertThat(found2).isNotSameAs(found4);
        });

    }



}
