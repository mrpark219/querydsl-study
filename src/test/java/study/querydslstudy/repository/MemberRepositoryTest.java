package study.querydslstudy.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import study.querydslstudy.dto.MemberSearchCondition;
import study.querydslstudy.dto.MemberTeamDto;
import study.querydslstudy.entity.Member;
import study.querydslstudy.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydslstudy.entity.QMember.member;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

	@Autowired
	EntityManager em;

	@Autowired
	MemberRepository memberRepository;

	@DisplayName("basicTest")
	@Test
	void basicTest() {

		// given
		Member member = new Member("member1", 10);
		memberRepository.save(member);

		// when
		Member findMember = memberRepository.findById(member.getId()).get();
		List<Member> result1 = memberRepository.findAll();
		List<Member> result2 = memberRepository.findByUsername("member1");

		// then
		assertThat(findMember).isEqualTo(member);
		assertThat(result1).containsExactly(member);
		assertThat(result2).containsExactly(member);
	}

	@DisplayName("search")
	@Test
	void search() {

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
		List<MemberTeamDto> result = memberRepository.search(condition);

		// then
		assertThat(result).extracting("username").containsExactly("member3", "member4");
	}

	@DisplayName("searchPageSimple")
	@Test
	void searchPageSimple() {

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
		PageRequest pageRequest = PageRequest.of(0, 3);

		// when
		Page<MemberTeamDto> result = memberRepository.searchPageSimple(condition, pageRequest);

		// then
		assertThat(result.getSize()).isEqualTo(3);
		assertThat(result.getContent()).extracting("username").containsExactly("member1", "member2", "member3");
	}

	@DisplayName("querydslPredicateExecutor")
	@Test
	void querydslPredicateExecutor() {

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

	    // when
		Iterable<Member> result = memberRepository.findAll(
			member.age.between(10, 40)
				.and(member.username.eq("member1"))
		);

		// then
		for(Member member : result) {
			System.out.println("member = " + member);
		}
	}
}