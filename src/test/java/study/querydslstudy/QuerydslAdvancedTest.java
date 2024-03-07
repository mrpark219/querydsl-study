package study.querydslstudy;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydslstudy.dto.MemberDto;
import study.querydslstudy.dto.QMemberDto;
import study.querydslstudy.dto.UserDto;
import study.querydslstudy.entity.Member;
import study.querydslstudy.entity.QMember;
import study.querydslstudy.entity.Team;

import java.util.List;

import static study.querydslstudy.entity.QMember.member;

@SpringBootTest
@Transactional
public class QuerydslAdvancedTest {

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


	@DisplayName("단건 프로젝션")
	@Test
	void simpleProjection() {

		// when
		List<String> result = queryFactory
			.select(member.username)
			.from(member)
			.fetch();


		// then
		for(String s : result) {
			System.out.println("s = " + s);
		}
	}

	@DisplayName("튜블 프로젝션")
	@Test
	void tupleProjection() {

		// when
		List<Tuple> result = queryFactory
			.select(member.username, member.age)
			.from(member)
			.fetch();

		// then
		for(Tuple tuple : result) {
			String username = tuple.get(member.username);
			Integer age = tuple.get(member.age);
			System.out.println("username = " + username);
			System.out.println("age = " + age);
		}
	}

	@DisplayName("JPQL에서 DTO 조회")
	@Test
	void findDtoByJPQL() {

		// when
		List<MemberDto> result = em.createQuery(
				"select new study.querydslstudy.dto.MemberDto(m.username, m.age) " +
					"from Member m", MemberDto.class)
			.getResultList();

		// then
		for(MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	@DisplayName("Querydsl에서 DTO 조회 - setter")
	@Test
	void findDtoByQuerydsl_setter() {

		// when
		List<MemberDto> result = queryFactory
			.select(Projections.bean(
				MemberDto.class,
				member.username,
				member.age
			))
			.from(member)
			.fetch();

		// then
		for(MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	@DisplayName("Querydsl에서 DTO 조회 - fields")
	@Test
	void findDtoByQuerydsl_fields() {

		// when
		List<MemberDto> result = queryFactory
			.select(Projections.fields(
				MemberDto.class,
				member.username,
				member.age
			))
			.from(member)
			.fetch();

		// then
		for(MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	@DisplayName("Querydsl에서 DTO 조회 - constructor")
	@Test
	void findDtoByQuerydsl_constructor() {

		// when
		List<MemberDto> result = queryFactory
			.select(Projections.constructor(
				MemberDto.class,
				member.username,
				member.age
			))
			.from(member)
			.fetch();

		// then
		for(MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	@DisplayName("Querydsl에서 DTO 조회 - fields, as")
	@Test
	void findUserDtoByQuerydsl_fields() {

		QMember memberSub = new QMember("memberSub");

		// when
		List<UserDto> result = queryFactory
			.select(Projections.fields(
				UserDto.class,
				member.username.as("name"),
				ExpressionUtils.as(JPAExpressions
					.select(memberSub.age.max())
					.from(memberSub), "age")
			))
			.from(member)
			.fetch();

		// then
		for(UserDto userDto : result) {
			System.out.println("memberDto = " + userDto);
		}
	}

	@DisplayName("@QueryProjection")
	@Test
	void findDtoByQueryProjection() {

		// when
		List<MemberDto> result = queryFactory
			.select(new QMemberDto(member.username, member.age))
			.from(member)
			.fetch();

		// then
		for(MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}
}
