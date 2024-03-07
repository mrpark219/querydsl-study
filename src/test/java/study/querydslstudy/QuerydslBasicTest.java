package study.querydslstudy;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydslstudy.entity.Member;
import study.querydslstudy.entity.QMember;
import study.querydslstudy.entity.Team;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.assertThat;
import static study.querydslstudy.entity.QMember.member;
import static study.querydslstudy.entity.QTeam.team;

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

	@DisplayName("집합")
	@Test
	void aggregation() {

		// when
		List<Tuple> result = queryFactory
			.select(
				member.count(),
				member.age.sum(),
				member.age.avg(),
				member.age.max(),
				member.age.min())
			.from(member)
			.fetch();

		// then
		Tuple tuple = result.get(0);
		assertThat(tuple.get(member.count())).isEqualTo(4);
		assertThat(tuple.get(member.age.sum())).isEqualTo(100);
		assertThat(tuple.get(member.age.avg())).isEqualTo(25);
		assertThat(tuple.get(member.age.max())).isEqualTo(40);
		assertThat(tuple.get(member.age.min())).isEqualTo(10);
	}

	@DisplayName("팀의 이름과 각 팀의 평균 연령을 구해라")
	@Test
	void group() {

		// when
		List<Tuple> result = queryFactory
			.select(team.name, member.age.avg())
			.from(member)
			.join(member.team, team)
			.groupBy(team.name)
			.fetch();

		// then
		Tuple teamA = result.get(0);
		Tuple teamB = result.get(1);

		assertThat(teamA.get(team.name)).isEqualTo("teamA");
		assertThat(teamA.get(member.age.avg())).isEqualTo(15);

		assertThat(teamB.get(team.name)).isEqualTo("teamB");
		assertThat(teamB.get(member.age.avg())).isEqualTo(35);
	}

	@DisplayName("팀 A에 소속된 모든 회원 조회")
	@Test
	void join() {

		// when
		List<Member> result = queryFactory
			.selectFrom(member)
			.join(member.team, team)
			.where(team.name.eq("teamA"))
			.fetch();

		// then
		assertThat(result)
			.extracting("username")
			.containsExactly("member1", "member2");
	}

	@DisplayName("세타 조인 - 회원의 이름이 팀 이름과 같은 회원 조회")
	@Test
	void theta_join() {

		// given
		em.persist(new Member(("teamA")));
		em.persist(new Member(("teamB")));
		em.persist(new Member(("teamC")));

		// when
		List<Member> result = queryFactory
			.select(member)
			.from(member, team)
			.where(member.username.eq(team.name))
			.fetch();

		// then
		assertThat(result)
			.extracting("username")
			.containsExactly("teamA", "teamB");
	}

	@DisplayName("회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회")
	@Test
	void join_on_filtering() {

		// when
		List<Tuple> result = queryFactory
			.select(member, team)
			.from(member)
			.leftJoin(member.team, team)
			.on(team.name.eq("teamA"))
			.fetch();

		// then
		for(Tuple tuple : result) {
			System.out.println("Tuple: " + tuple);
		}
	}

	@DisplayName("세타 조인 - 회원의 이름이 팀 이름과 같은 회원 조회")
	@Test
	void join_on_no_relation() {

		// given
		em.persist(new Member(("teamA")));
		em.persist(new Member(("teamB")));
		em.persist(new Member(("teamC")));

		// when
		List<Tuple> result = queryFactory
			.select(member, team)
			.from(member)
			.leftJoin(team)
			.on(member.username.eq(team.name))
			.fetch();

		// then
		for(Tuple tuple : result) {
			System.out.println("Tuple: " + tuple);
		}
	}

	@PersistenceUnit
	EntityManagerFactory emf;

	@DisplayName("페지 조인 미적용")
	@Test
	void fetchJoinNo() {

		em.flush();
		em.clear();

		// when
		Member findMember = queryFactory
			.selectFrom(member)
			.where(member.username.eq("member1"))
			.fetchOne();

		// then
		boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
		assertThat(loaded).isFalse();
	}

	@DisplayName("페지 조인 적용")
	@Test
	void fetchJoinUse() {

		em.flush();
		em.clear();

		// when
		Member findMember = queryFactory
			.selectFrom(member)
			.join(member.team, team).fetchJoin()
			.where(member.username.eq("member1"))
			.fetchOne();

		// then
		boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
		assertThat(loaded).isTrue();
	}

	@DisplayName("나이가 가장 많은 회원 조회")
	@Test
	void subQuery() {

		QMember memberSub = new QMember("memberSub");

		// when
		List<Member> result = queryFactory
			.selectFrom(member)
			.where(member.age.eq(
				select(memberSub.age.max())
					.from(memberSub)
			))
			.fetch();

		// then
		assertThat(result)
			.extracting("age")
			.containsExactly(40);
	}

	@DisplayName("나이가 평균 이상인 회원 조회")
	@Test
	void subQueryGoe() {

		QMember memberSub = new QMember("memberSub");

		// when
		List<Member> result = queryFactory
			.selectFrom(member)
			.where(member.age.goe(
				select(memberSub.age.avg())
					.from(memberSub)
			))
			.fetch();

		// then
		assertThat(result)
			.extracting("age")
			.containsExactly(30, 40);
	}

	@DisplayName("10살보다 많은 회원 조회")
	@Test
	void subQueryIn() {

		QMember memberSub = new QMember("memberSub");

		// when
		List<Member> result = queryFactory
			.selectFrom(member)
			.where(member.age.in(
				select(memberSub.age)
					.from(memberSub)
					.where(memberSub.age.gt(10))
			))
			.fetch();

		// then
		assertThat(result)
			.extracting("age")
			.containsExactly(20, 30, 40);
	}

	@DisplayName("회원 이름과 평균 나이를 같이 조회")
	@Test
	void selectSubQuery() {

		QMember memberSub = new QMember("memberSub");

		// when
		List<Tuple> result = queryFactory
			.select(member.username,
				select(memberSub.age.avg())
					.from(memberSub))
			.from(member)
			.fetch();


		// then
		for(Tuple tuple : result) {
			System.out.println("Tuple: " + tuple);
		}
	}

	@DisplayName("case 문 - 기본")
	@Test
	void basicCase() {

		// when
		List<String> result = queryFactory
			.select(member.age
				.when(10).then("열살")
				.when(20).then("스무살")
				.otherwise("기타"))
			.from(member)
			.fetch();

		// then
		for(String s : result) {
			System.out.println("s: " + s);
		}
	}

	@DisplayName("case 문 - 심화")
	@Test
	void complexCase() {

		// when
		List<String> result = queryFactory
			.select(new CaseBuilder()
				.when(member.age.between(0, 20)).then("0~20살")
				.when(member.age.between(21, 30)).then("21~30살")
				.otherwise("기타"))
			.from(member)
			.fetch();

		// then
		for(String s : result) {
			System.out.println("s: " + s);
		}
	}

	@DisplayName("상수")
	@Test
	void constant() {

		// when
		List<Tuple> result = queryFactory
			.select(member.username, Expressions.constant("A"))
			.from(member)
			.fetch();

		// then
		for(Tuple tuple : result) {
			System.out.println("tuple: " + tuple);
		}
	}

	@DisplayName("문자 더하기")
	@Test
	void concat() {

	    // when
		List<String> result = queryFactory
			.select(member.username.concat("_").concat(member.age.stringValue()))
			.from(member)
			.where(member.username.eq("member1"))
			.fetch();

	    // then
		for(String s : result) {
			System.out.println("s: " + s);
		}
	}
}
