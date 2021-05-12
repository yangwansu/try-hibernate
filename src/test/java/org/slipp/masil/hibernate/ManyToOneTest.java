package org.slipp.masil.hibernate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slipp.masil.hibernate.supports.EntityManagerExtension;
import org.slipp.masil.hibernate.supports.Persistence;

import javax.persistence.*;
import java.util.List;

import static lombok.AccessLevel.PROTECTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.slipp.masil.hibernate.supports.EntityManageTemplate.get;

@ExtendWith(EntityManagerExtension.class)
public class ManyToOneTest {

    @Entity
    @Getter
    @Table(name = "Member")
    @NoArgsConstructor(access = PROTECTED)
    public static class Member {

        @Id
        @GeneratedValue
        private Long id;

        private String name;

        @ManyToOne
        private Team team;

        public Member(String name, Team team) {
            this.name = name;
            this.team = team;
        }
    }

    @Entity
    @Getter
    @Table(name = "Team")
    @NoArgsConstructor(access = PROTECTED)
    public static class Team {

        @Id
        @GeneratedValue
        private Long id;
        private String name;

        public Team(String name) {
            this.name = name;
        }
    }


    @Test
    @Persistence(name = "org.slipp.masil.jpa",
            classes = {Member.class, Team.class})
    void basic() {
        Team team = new Team("pink");

        Member soyoon = new Member("soyoon", team);
        Member hayoon = new Member("hayoon", team);
        Member wansu = new Member("wansu", null); // manyToOne optional is true

        get().transaction((em) -> {
            em.persist(team);
            em.persist(hayoon);
            em.persist(soyoon);
            em.persist(wansu);
        });

        get().transaction((em) -> {
            //List<Member> members = em.createNativeQuery("select * from Member", Member.class).getResultList();
            List<Member> members = em.createQuery("FROM ManyToOneTest$Member", Member.class).getResultList();
            assertThat(members.get(0).getTeam()).isSameAs(members.get(1).getTeam()); // because L1 cache
        });
    }

    @Getter
    @Entity
    @NoArgsConstructor(access = PROTECTED)
    public static class User {

        @Id
        @GeneratedValue
        private Long id;

        private String name;

        @ManyToOne(optional = false)
        private Team team;

        public User(String name, Team team) {
            this.name = name;
            this.team = team;
        }
    }

    @Test
    @DisplayName("If set to false then a non-null relationship must always exist.")
    @Persistence(name = "org.slipp.masil.jpa",
            classes = {User.class, Team.class})
    void testOptional() {
        get().transaction((em) -> {
            User wansu = new User("wansu", null);
            assertThatThrownBy(() -> em.persist(wansu))
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining("not-null property references a null or transient value");
        });
    }
}
