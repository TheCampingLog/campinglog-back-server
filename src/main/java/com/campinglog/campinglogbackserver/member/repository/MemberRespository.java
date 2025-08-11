package com.campinglog.campinglogbackserver.member.repository;

import com.campinglog.campinglogbackserver.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRespository extends JpaRepository<Member, String> {

  Optional<Member> findByEmail(String email);

}
