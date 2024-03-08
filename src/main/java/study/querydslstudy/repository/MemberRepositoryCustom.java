package study.querydslstudy.repository;

import study.querydslstudy.dto.MemberSearchCondition;
import study.querydslstudy.dto.MemberTeamDto;

import java.util.List;

public interface MemberRepositoryCustom {

	List<MemberTeamDto> search(MemberSearchCondition condition);
}
