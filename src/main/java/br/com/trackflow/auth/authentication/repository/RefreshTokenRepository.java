
package br.com.trackflow.auth.authentication.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.trackflow.auth.authentication.entity.RefreshToken;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE RefreshToken token
               SET token.revokedAt = :now,
                   token.replacedByHash = :replacementHash
             WHERE token.tokenHash = :tokenHash
               AND token.revokedAt IS NULL
               AND token.expiresAt > :now
            """)
    int revokeIfActive(
            @Param("tokenHash") String tokenHash,
            @Param("replacementHash") String replacementHash,
            @Param("now") LocalDateTime now);
}
