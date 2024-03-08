package study.querydslstudy.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydslstudy.entity.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

}