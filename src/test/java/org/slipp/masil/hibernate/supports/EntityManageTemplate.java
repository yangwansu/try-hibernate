package org.slipp.masil.hibernate.supports;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.function.Consumer;

public class EntityManageTemplate {

    private static EntityManageTemplate INSTANCE;

    static {
        clear();
    }

    static void clear() {
        INSTANCE = null;
    }

    static void set(EntityManagerFactory entityManagerFactory) {
        INSTANCE = new EntityManageTemplate(entityManagerFactory);
    }

    public static EntityManageTemplate get() {
        if(INSTANCE == null) {
            throw new IllegalStateException("template is not init");
        }
        return INSTANCE;
    }

    EntityManagerFactory entityManagerFactory;

    EntityManageTemplate(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public void transaction(Consumer<EntityManager> consumer) {
        EntityManager em = getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();

            consumer.accept(em);

            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
