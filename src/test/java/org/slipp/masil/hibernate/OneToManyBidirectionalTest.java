package org.slipp.masil.hibernate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slipp.masil.hibernate.supports.EntityManagerExtension;
import org.slipp.masil.hibernate.supports.Persistence;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PROTECTED;
import static org.slipp.masil.hibernate.supports.EntityManageTemplate.get;


@ExtendWith(EntityManagerExtension.class)
public class OneToManyBidirectionalTest {

    @Getter
    @Entity
    @Table(name = "Team")
    @NoArgsConstructor(access = PROTECTED, force = true)
    @RequiredArgsConstructor(staticName = "of")
    public static class Team {

        @Id
        @GeneratedValue
        private Long id;

        private final String name;

        @OneToMany(fetch = FetchType.EAGER)
        @JoinColumn(name = "team_id")
        private final List<Member> members = new ArrayList<>();

        public void addMember(Member member) {
            this.members.add(member.updateTeam(this));
        }
    }


    @Getter
    @Entity
    @Table(name = "Member")
    @NoArgsConstructor(access = PROTECTED, force = true)
    @AllArgsConstructor(staticName = "of")
    @RequiredArgsConstructor(staticName = "of")
    public static class Member {

        @Id
        @GeneratedValue
        private Long id;

        private final String name;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "team_id")
        private final Team team;

        public Member updateTeam(Team team) {
            return Member.of(getId(), getName(), team);
        }
    }


    @Test
    @Persistence(name = "org.slipp.masil.jpa", classes = {
            Team.class,
            Member.class
    })
    void bi_direction() {

        Team saved = get().transaction((em) -> {
            Team team = Team.of("red");
            em.persist(team);

            for (int i = 0; i < 3; i++) {
                Member member = Member.of("m" + i, team);
                em.persist(member);
                team.addMember(member);
            }

            return team;
        });

        get().transaction((em) -> {

            Team find = em.find(Team.class, saved.getId());

            List<String> a = find.getMembers().stream().map(Member::getName).collect(Collectors.toList());

            a.forEach(System.out::println);

        });

    }
}
