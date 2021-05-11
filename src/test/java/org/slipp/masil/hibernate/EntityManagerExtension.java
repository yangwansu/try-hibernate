package org.slipp.masil.hibernate;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EntityManagerExtension implements Extension, BeforeEachCallback, AfterEachCallback {

    EntityManagerFactory entityManagerFactory;

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        entityManagerFactory = Persistence.createEntityManagerFactory("org.slipp.masil.jpa");
        EntityManageTemplate.set(entityManagerFactory);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        entityManagerFactory.close();
        EntityManageTemplate.clear();
    }


}
