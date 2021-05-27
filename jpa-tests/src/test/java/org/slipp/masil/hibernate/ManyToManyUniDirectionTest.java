package org.slipp.masil.hibernate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ListAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slipp.masil.hibernate.supports.EntityManagerExtension;
import org.slipp.masil.hibernate.supports.Persistence;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PROTECTED;
import static org.slipp.masil.hibernate.supports.EntityManageTemplate.get;

@ExtendWith(EntityManagerExtension.class)
public class ManyToManyUniDirectionTest {

    @Getter
    @Entity(name = "Note")
    @NoArgsConstructor(access = PROTECTED, force = true)
    public static class Note {

        public static Note of(String subject, String content) {
            return new Note(subject, content);
        }

        @Id
        @GeneratedValue
        private Long id;

        private String subject;
        private String content = "";

        @ManyToMany
        private Set<Tag> tags = new HashSet<>();

        private Note(String subject, String content) {
            setContent(content);
            setSubject(subject);
        }

        private void setSubject(String subject) {
            this.subject = subject;
        }

        private void setContent(String content) {
            this.content = content;
        }

        public void add(Tag tag) {
            tags.add(tag);
        }

        public void editContent(String content) {
            setContent(content);
        }
    }

    @Getter
    @Entity(name = "Tag")
    @RequiredArgsConstructor(staticName = "of")
    @NoArgsConstructor(access = PROTECTED, force = true)
    public static class Tag {
        @Id
        @GeneratedValue
        private Long id;

        private final String name;
    }

    Note note;

    Tag tag1;
    Tag tag2;
    Tag tag3;

    @BeforeEach
    @Persistence(name = "org.slipp.masil.jpa", classes = {Note.class, Tag.class})
    void setUp() {
        note = Note.of("subject", "content");

        tag1 = Tag.of("A");
        tag2 = Tag.of("B");
        tag3 = Tag.of("C");

        get().transaction((em) -> {
            em.persist(tag1);
            em.persist(tag2);
            em.persist(tag3);

            note.add(tag1);
            note.add(tag2);

            em.persist(note);
        });
    }


    @Test
    @Persistence(name = "org.slipp.masil.jpa", classes = {Note.class, Tag.class})
    void name2() {
        assertThat1(() -> get().transaction((em) -> {
            Note find = em.find(Note.class, note.getId());
            find.add(tag3);}))
                .areExactly(2, new Condition<>(SqlLine::isSelect, null))
                .areExactly(1, new Condition<>(SqlLine::isInsert, null))
        ;
    }

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class SqlLine {
        private final String line;

        public boolean isSelect() {
            return line.contains("select");
        }

        public boolean isInsert() {
            return line.contains("insert");
        }

        public boolean isDelete() {
            return line.contains("delete");
        }

        public boolean isUpdate() {
            return line.contains("update");
        }
    }

    private ListAssert<SqlLine> assertThat1(Runnable runnable) {
        PrintStream old = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        runnable.run();

        String str = outputStream.toString();
        List<SqlLine> lines = Arrays.stream(str.split("\n")).map(SqlLine::of).collect(Collectors.toList());
        System.setOut(old);
        System.out.println(str);
        return Assertions.assertThat(lines);
    }

    @Test
    @Persistence(name = "org.slipp.masil.jpa", classes = {Note.class, Tag.class})
    void name5() {
        get().transaction((em) -> {
            Note find = em.find(Note.class, note.getId());
            find.editContent("New Content");
        });
    }

}
