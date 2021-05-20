package org.slipp.masil.hibernate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.PersistentObjectException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slipp.masil.hibernate.supports.EntityManagerExtension;
import org.slipp.masil.hibernate.supports.Persistence;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.concurrent.atomic.AtomicReference;

import static lombok.AccessLevel.PROTECTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.slipp.masil.hibernate.supports.EntityManageTemplate.get;

@ExtendWith(EntityManagerExtension.class)
public class DetachedTest {
    @Getter
    @Entity(name = "Team")
    @NoArgsConstructor(access = PROTECTED, force = true)
    public static class Team {

        @Id
        @GeneratedValue
        private Long id;
        @Setter
        private String name;

        public Team(String name) {
            this.name = name;
        }
    }

    @Getter
    @Entity(name = "User")
    @NoArgsConstructor(access = PROTECTED, force = true)
    @RequiredArgsConstructor
    public static class User {

        @Id
        @GeneratedValue
        private Long id;

        private final String name;

        @ManyToOne
        private final Team team;

    }

    @Test
    @DisplayName("detached entity passed to persist")
    @Persistence(name = "org.slipp.masil.jpa",
            classes = {User.class, Team.class})
    void detached() {

        Team team = get().transaction(em -> {
            Team pink = new Team("pink");
            em.persist(pink);
            return pink;
        });

        User user = get().transaction(em -> {
            User wansu = new User("wansu", team);
            em.persist(wansu);
            assertTrue(em.contains(wansu));
            return wansu;
        });


        //see transaction(...)  em.close()
        //User out of transaction will be detached.

        get().transaction(em -> {

            assertFalse(em.contains(user)); //check detached
            assertThatThrownBy(() -> em.persist(user))
                    .hasRootCauseInstanceOf(PersistentObjectException.class)
                    .hasMessageContaining("detached entity passed to persist");
        });

        User user2 = get().transaction(em -> {
            team.setName("red");

            User wansu = new User("wansu", team);
            em.persist(wansu);

            return wansu;
        });

        User user3 = get().transaction(em -> {
            return em.find(User.class, user2.getId());
        });

        Team teamOfUser2 = user2.getTeam();
        Team teamOfUser3 = user3.getTeam();

        assertThat(teamOfUser2.getName()).isNotEqualTo(teamOfUser3.getName());

    }


    @Test
    @Persistence(name = "org.slipp.masil.jpa",
            classes = {User.class, Team.class})
    void name() {

        AtomicReference<User> wansu = new AtomicReference<>();
        AtomicReference<Team> team = new AtomicReference<>();
        get().transaction(
                (em) -> {
                    Team pink = new Team("pink");
                    team.set(pink);
                    em.persist(pink);

                    wansu.set(new User("wansu", team.get()));
                    em.persist(wansu.get());
                },
                (em) -> {
                    // different transaction
                    assertTrue(em.contains(team.get())); // not detached because em is not close
                    assertTrue(em.contains(wansu.get())); // not detached because em is not close

                    team.get().setName("red");


                    em.persist(wansu.get());

                });
    }
}
