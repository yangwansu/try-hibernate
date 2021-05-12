package org.slipp.masil.hibernate.supports;

import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;
import org.hibernate.jpa.boot.internal.PersistenceXmlParser;
import org.hibernate.jpa.boot.spi.Bootstrap;
import org.hibernate.jpa.boot.spi.EntityManagerFactoryBuilder;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.persistence.EntityManagerFactory;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class EntityManagerExtension implements Extension, BeforeEachCallback, AfterEachCallback {

    EntityManagerFactory entityManagerFactory;

    @Override
    public void beforeEach(ExtensionContext context) {
        Method testMethod = context.getTestMethod().get();
        Persistence persistence = testMethod.getAnnotation(Persistence.class);

        if(persistence == null) {
            return;
        }

        String[] classes = Arrays.stream(persistence.classes()).map(Class::getTypeName).toArray(String[]::new);
        entityManagerFactory = createEntityManagerFactory(persistence.name(), classes);
        EntityManageTemplate.set(entityManagerFactory);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
        EntityManageTemplate.clear();
    }

    static EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, String... classes) {
        //Persistence.createEntityManagerFactory("org.slipp.masil.jpa");
        List<ParsedPersistenceXmlDescriptor> units = PersistenceXmlParser.locatePersistenceUnits(new HashMap<>());

        assertThat(units).size().isEqualTo(1);
        ParsedPersistenceXmlDescriptor persistenceUnitDescriptor = units.stream().filter(u -> u.getName().equals(persistenceUnitName)).findFirst().orElse(null);

        assert persistenceUnitDescriptor != null;

        persistenceUnitDescriptor.addClasses(classes);

        EntityManagerFactoryBuilder emfBuilder = Bootstrap.getEntityManagerFactoryBuilder(persistenceUnitDescriptor, Collections.emptyMap(), (ClassLoader) null);
        emfBuilder.generateSchema();
        EntityManagerFactory emf = emfBuilder.build();
        return emf;
    }


}
