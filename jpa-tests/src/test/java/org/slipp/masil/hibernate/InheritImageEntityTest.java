package org.slipp.masil.hibernate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slipp.masil.hibernate.supports.EntityManageTemplate;
import org.slipp.masil.hibernate.supports.EntityManagerExtension;
import org.slipp.masil.hibernate.supports.Persistence;

import javax.persistence.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.slipp.masil.hibernate.supports.EntityManageTemplate.get;

@ExtendWith(EntityManagerExtension.class)
public class InheritImageEntityTest {


    @Getter
    @Entity
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Product {

        public static Product create(List<Image> images) {
            return new Product(null, images);
        }
        @Id
        @GeneratedValue
        private Long id;

        @OneToMany(fetch = FetchType.EAGER)
        @JoinColumn(name = "product_id")
        private List<Image> images;
    }

    @Entity
    @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
    @DiscriminatorColumn(name = "type")
    public static abstract class Image {
        @Id
        @GeneratedValue
        private Long id;

        public abstract String getPath();
    }

    @Entity
    @DiscriminatorValue("T")
    public static class Thumbnail extends Image {
        public static Image create() {
            return new Thumbnail();
        }
        @Override
        public String getPath() {
            return "Thumbnail";
        }
    }

    @Entity
    @DiscriminatorValue("J")
    public static class Jpeg extends Image {

        public static Image create() {
            return new Jpeg();
        }
        @Override
        public String getPath() {
            return "Jpeg";
        }
    }

    @Test
    @Persistence(
            name = "org.slipp.masil.jpa",
            classes = {Product.class, Image.class, Thumbnail.class, Jpeg.class})
    void inheritEntity() {

        List<Image> images = Lists.newArrayList(Thumbnail.create(), Jpeg.create());
        Product product = Product.create(images);

        Long savedId = get().transaction((em -> {
            images.forEach(em::persist);
            em.persist(product);
            return product.getId();
        }));


        Product find = get().transaction((em -> {
            return em.find(Product.class, savedId);
        }));

        assertThat(find.getImages()).hasAtLeastOneElementOfType(Thumbnail.class);
        assertThat(find.getImages()).hasAtLeastOneElementOfType(Jpeg.class);

    }
}
