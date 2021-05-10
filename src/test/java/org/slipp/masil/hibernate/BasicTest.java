package org.slipp.masil.hibernate;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicTest {

    EntityManagerFactory entityManagerFactory;
    EntityManager em;

    @BeforeEach
    void setUp() {
        entityManagerFactory = Persistence.createEntityManagerFactory("org.slipp.masil.jpa");
        em = entityManagerFactory.createEntityManager();
    }

    @AfterEach
    void tearDown() {
        entityManagerFactory.close();
    }


    @Test
    void basic() {
        Product product = new Product("macbook");

        em.getTransaction().begin();
        em.persist(product);
        em.getTransaction().commit();
        em.close();

        em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        List<Product> result = em.createQuery( "from Product", Product.class ).getResultList();
        result.forEach(System.out::println);
        em.getTransaction().commit();
        em.close();

        assertThat(product.getId()).isNotNull();

    }
}
