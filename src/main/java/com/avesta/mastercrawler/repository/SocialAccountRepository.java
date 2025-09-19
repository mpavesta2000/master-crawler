package com.avesta.mastercrawler.repository;

import com.avesta.mastercrawler.model.SocialAccount;
import com.avesta.mastercrawler.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    List<SocialAccount> findByUserAndIsActiveTrue(Users user);

    List<SocialAccount> findByPlatformAndIsActiveTrue(SocialAccount.SocialPlatform platform);

    List<SocialAccount> findByIsGlobalTrueAndIsActiveTrue();

    Optional<SocialAccount> findByUserAndPlatformAndIsActiveTrue(Users user, SocialAccount.SocialPlatform platform);

    @Query("SELECT sa FROM SocialAccount sa WHERE sa.platform = :platform AND sa.isActive = true AND (sa.isGlobal = true OR sa.user = :user)")
    List<SocialAccount> findAvailableAccountsForUserAndPlatform(@Param("user") Users user, @Param("platform") SocialAccount.SocialPlatform platform);

    Optional<SocialAccount> findByPlatformAndIsGlobalTrueAndIsActiveTrue(SocialAccount.SocialPlatform platform);
    
    List<SocialAccount> findByPlatformAndUser(SocialAccount.SocialPlatform platform, Users user);
}
