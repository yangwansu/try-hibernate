package org.slipp.masil.hibernate;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slipp.masil.hibernate.supports.EntityManagerExtension;
import org.slipp.masil.hibernate.supports.Persistence;

import javax.persistence.*;

import static lombok.AccessLevel.PROTECTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slipp.masil.hibernate.supports.EntityManageTemplate.get;

@ExtendWith(EntityManagerExtension.class)
public class OneToOneTest {

    @Getter
    @Entity
    @Table(name = "Post")
    @RequiredArgsConstructor(staticName = "of")
    @NoArgsConstructor(access = PROTECTED, force = true)
    public static class Post {

        @Id
        @GeneratedValue
        private Long id;

        private final String content;

        @OneToOne
        private final Author author;
    }

    @Getter
    @Entity
    @Table(name = "Author")
    @RequiredArgsConstructor(staticName = "of")
    @NoArgsConstructor(access = PROTECTED, force = true)
    public static class Author {
        @Id
        @GeneratedValue
        private Long id;

        private final String name;
    }

    @Test
    @Persistence(name = "org.slipp.masil.jpa",
            classes = {Post.class, Author.class})
    void basic() {
        Post saved = get().transaction((em) -> {
            Author author = Author.of("Wansu");
            Post post = Post.of("Blah Blah", author);
            em.persist(author);
            em.persist(post);

            return post;
        });


        Post find = get().transaction((em) -> {
            return em.find(Post.class, saved.getId());
        });

        assertThat(saved.getId()).isEqualTo(find.getId());
    }
}
