package org.slipp.masil.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicTest {

    EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void setUp() {
        entityManagerFactory = Persistence.createEntityManagerFactory("org.slipp.masil.jpa");
    }

    @AfterEach
    void tearDown() {
        entityManagerFactory.close();
    }


    @Test
    void session_of_hibernate_is_entity_manager_of_jpa() {
        assertThat(entityManagerFactory).isInstanceOf(SessionFactory.class);
        assertThat(entityManagerFactory.createEntityManager()).isInstanceOf(Session.class);
    }

    @Test
    void basic() {

        doTransaction((em) -> {
            Product product = new Product("macbook");
            em.persist(product);
            Product found0 = em.find(Product.class, product.getId());

            assertThat(found0).isSameAs(product);
        });
    }


    @Test
    void first_level_cache() {
        Product product = new Product("macbook");

        doTransaction(em -> em.persist(product));

        doTransaction(em -> {
            Product found1 = em.find(Product.class, product.getId()); // from L1 cache
            Product found2 = em.find(Product.class, product.getId()); // from L1 cache
            Product found3 = entityManagerFactory.createEntityManager().find(Product.class, product.getId()); // from db

            em.detach(found1); //evict
            Product found4 = em.find(Product.class, product.getId()); // from db

            assertThat(found1).isNotSameAs(product);
            assertThat(found1).isNotNull();
            assertThat(found1).isSameAs(found2);
            assertThat(found2).isNotSameAs(found3);
            assertThat(found2).isNotSameAs(found4);
        });

    }

    private void doTransaction(Consumer<EntityManager> consumer) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.getTransaction().begin();

            consumer.accept(em);

            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }


}
