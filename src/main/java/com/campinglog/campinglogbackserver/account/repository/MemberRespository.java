package com.campinglog.campinglogbackserver.account.repository;

import com.campinglog.campinglogbackserver.account.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRespository extends JpaRepository<Member, String> {

  Optional<Member> findByEmail(String email);

}
