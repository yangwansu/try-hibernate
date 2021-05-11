package org.slipp.masil.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.persistence.EntityManagerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.slipp.masil.hibernate.EntityManageTemplate.get;

@ExtendWith(value = EntityManagerExtension.class)
public class EntityManagerTest{

    @Test
    void session_of_hibernate_is_entity_manager_of_jpa() {
        EntityManagerFactory entityManagerFactory = get().getEntityManagerFactory();

        assertThat(entityManagerFactory).isInstanceOf(SessionFactory.class);
        assertThat(entityManagerFactory.createEntityManager()).isInstanceOf(Session.class);
    }

    @Test
    void basic() {
        get().transaction((em) -> {
            Product product = new Product("macbook");
            em.persist(product);
            Product found0 = em.find(Product.class, product.getId());

            assertThat(found0).isSameAs(product);
        });
    }

}
