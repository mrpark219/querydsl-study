package study.querydslstudy;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydslstudy.entity.Member;
import study.querydslstudy.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydslstudy.entity.QMember.member;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

	@Autowired
	EntityManager em;

	JPAQueryFactory queryFactory;

	@BeforeEach
	public void before() {

		queryFactory = new JPAQueryFactory(em);

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
	}

	@DisplayName("회원명으로 검색 - JPQL")
	@Test
	void startJPQL() {

		// given
		// BEFORE

		// when
		// member1을 찾아라
		Member findMember = em.createQuery(
				"select m " +
					"from Member m " +
					"where m.username = :username", Member.class)
			.setParameter("username", "member1")
			.getSingleResult();

		// then
		assertThat(findMember.getUsername()).isEqualTo("member1");
	}

	@DisplayName("회원명으로 검색 - Querydsl")
	@Test
	void startQuerydsl() {

		// given
		// BEFORE

		// when
		Member findMember = queryFactory
			.select(member)
			.from(member)
			.where(member.username.eq("member1"))
			.fetchOne();

		// then
		assertThat(findMember.getUsername()).isEqualTo("member1");
	}

	@DisplayName("검색 조건 쿼리")
	@Test
	void search() {

		// when
		Member findMember = queryFactory
			.selectFrom(member)
			.where(member.username.eq("member1")
				.and(member.age.eq(10)))
			.fetchOne();

		// then
		assertThat(findMember.getUsername()).isEqualTo("member1");
	}

	@DisplayName("검색 조건 쿼리2")
	@Test
	void searchAndParam() {

		// when
		Member findMember = queryFactory
			.selectFrom(member)
			.where(
				member.username.eq("member1"),
				member.age.eq(10))
			.fetchOne();

		// then
		assertThat(findMember.getUsername()).isEqualTo("member1");
	}
	
	@DisplayName("결과 조회")
	@Test
	void resultFetch() {
		List<Member> fetch = queryFactory
			.selectFrom(member)
			.fetch();

		Member fetchOne = queryFactory
			.selectFrom(member)
			.where(member.username.eq("member1"))
			.fetchOne();

		Member fetchFirst = queryFactory
			.selectFrom(member)
			.fetchFirst();

		QueryResults<Member> results = queryFactory
			.selectFrom(member)
			.fetchResults();

		results.getTotal();
		List<Member> content = results.getResults();

		long total = queryFactory
			.selectFrom(member)
			.fetchCount();
	}
}
