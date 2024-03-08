package study.querydslstudy.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydslstudy.dto.MemberSearchCondition;
import study.querydslstudy.dto.MemberTeamDto;
import study.querydslstudy.entity.Member;
import study.querydslstudy.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

	@Autowired
	EntityManager em;

	@Autowired
	MemberJpaRepository memberJpaRepository;

	@DisplayName("basicTest")
	@Test
	void basicTest() {

		// given
		Member member = new Member("member1", 10);
		memberJpaRepository.save(member);

		// when
		Member findMember = memberJpaRepository.findById(member.getId()).get();
		List<Member> result1 = memberJpaRepository.findAll();
		List<Member> result2 = memberJpaRepository.findByUsername("member1");

		// then
		assertThat(findMember).isEqualTo(member);
		assertThat(result1).containsExactly(member);
		assertThat(result2).containsExactly(member);
	}

	@DisplayName("basicQuerydslTest")
	@Test
	void basicQuerydslTest() {

		// given
		Member member = new Member("member1", 10);
		memberJpaRepository.save(member);

		// when
		Member findMember = memberJpaRepository.findById(member.getId()).get();
		List<Member> result1 = memberJpaRepository.findAll_Querydsl();
		List<Member> result2 = memberJpaRepository.findByUsername_Querydsl("member1");

		// then
		assertThat(findMember).isEqualTo(member);
		assertThat(result1).containsExactly(member);
		assertThat(result2).containsExactly(member);
	}
	
	@DisplayName("searchByBuilder")
	@Test
	void searchByBuilder() {
	    
	    // given
		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		em.persist(teamA);
		em.persist(teamB);

		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 20, teamA);

		Member member3 = new Member("member3", 30, teamB);
		Member member4 = new Member("member4", 40, teamB);
		em.persist(member1);
		em.persist(member2);
		em.persist(member3);
		em.persist(member4);

		MemberSearchCondition condition = new MemberSearchCondition();
		condition.setAgeGoe(20);
		condition.setAgeLoe(40);
		condition.setTeamName("teamB");

	    // when
		List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(condition);

		// then
	    assertThat(result).extracting("username").containsExactly("member3", "member4");
	}
}