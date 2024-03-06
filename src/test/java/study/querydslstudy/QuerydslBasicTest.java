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

	@DisplayName("회원 나이 내림차순, 회원 이름 올림차순, 회원 이름이 없다면 마지막에 출력")
	@Test
	void sort() {

		// given
		em.persist(new Member(null, 100));
		em.persist(new Member("member5", 100));
		em.persist(new Member("member6", 100));

		// when
		List<Member> result = queryFactory
			.selectFrom(member)
			.where(member.age.eq(100))
			.orderBy(member.age.desc(), member.username.asc().nullsLast())
			.fetch();

		// then
		Member member5 = result.get(0);
		Member member6 = result.get(1);
		Member memberNull = result.get(2);

		assertThat(member5.getUsername()).isEqualTo("member5");
		assertThat(member6.getUsername()).isEqualTo("member6");
		assertThat(memberNull.getUsername()).isNull();
	}

	@DisplayName("페이징1")
	@Test
	void paging1() {

		// when
		List<Member> result = queryFactory
			.selectFrom(member)
			.orderBy(member.username.desc())
			.offset(1) // 0부터 시작
			.limit(2)
			.fetch();

		// then
		assertThat(result).hasSize(2);
	}

	@DisplayName("페이징2")
	@Test
	void paging2() {

		// when
		QueryResults<Member> queryResults = queryFactory
			.selectFrom(member)
			.orderBy(member.username.desc())
			.offset(1) // 0부터 시작
			.limit(2)
			.fetchResults();

		// then
		assertThat(queryResults.getTotal()).isEqualTo(4);
		assertThat(queryResults.getLimit()).isEqualTo(2);
		assertThat(queryResults.getOffset()).isEqualTo(1);
		assertThat(queryResults.getResults()).hasSize(2);
	}
}
