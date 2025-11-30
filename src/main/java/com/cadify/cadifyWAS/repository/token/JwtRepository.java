package com.cadify.cadifyWAS.repository.token;

import com.cadify.cadifyWAS.model.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JwtRepository extends JpaRepository<Token, String> {
    Optional<Token> findByMemberKey(String memberKey);
    Optional<Token> findByRefreshToken(String refreshToken);

    @Modifying
    @Query("DELETE FROM Token t WHERE t.refreshToken = :refreshToken")
    int deleteByRefreshToken(@Param("refreshToken") String refreshToken);
}