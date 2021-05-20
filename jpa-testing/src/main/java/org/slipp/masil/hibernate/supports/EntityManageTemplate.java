package org.slipp.masil.hibernate.supports;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.function.Consumer;
import java.util.function.Function;

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

    public <T> T transaction(Function<EntityManager, T> func) {
        EntityManager em = getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();

            T t = func.apply(em);

            em.getTransaction().commit();

            return t;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void transaction(Consumer<EntityManager> consumer, Consumer<EntityManager> consumer2) {
        EntityManager em = getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            consumer.accept(em);
            em.getTransaction().commit();

            em.getTransaction().begin();
            consumer2.accept(em);
            em.getTransaction().commit();

        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
