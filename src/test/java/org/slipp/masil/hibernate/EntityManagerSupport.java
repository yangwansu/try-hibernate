package org.slipp.masil.hibernate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.function.Consumer;

public interface EntityManagerSupport {


    default void transaction(Consumer<EntityManager> consumer) {
        EntityManager em = getEntityManagerFactory().createEntityManager();
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

    EntityManagerFactory getEntityManagerFactory();
}
