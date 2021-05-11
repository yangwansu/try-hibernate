package org.slipp.masil.hibernate;

import javax.persistence.EntityManagerFactory;

class EntityManageTemplate implements EntityManagerSupport {

    private static EntityManagerSupport INSTANCE;

    static {
        clear();
    }

    static void clear() {
        INSTANCE = () -> {
            throw new IllegalArgumentException("");
        };
    }

    static void set(EntityManagerFactory entityManagerFactory) {
        INSTANCE = new EntityManageTemplate(entityManagerFactory);
    }

    static EntityManagerSupport get() {
        return INSTANCE;
    }

    EntityManagerFactory entityManagerFactory;

    EntityManageTemplate(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }
}
