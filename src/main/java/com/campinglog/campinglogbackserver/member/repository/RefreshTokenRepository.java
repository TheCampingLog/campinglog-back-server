package com.campinglog.campinglogbackserver.member.repository;

import com.campinglog.campinglogbackserver.member.entity.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByJti(String jti);

}
